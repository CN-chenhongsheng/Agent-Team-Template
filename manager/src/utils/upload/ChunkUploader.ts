/**
 * 大文件切片上传器
 *
 * 核心功能：
 * - 文件切片（5MB/片）
 * - Web Worker 计算 MD5
 * - Promise 池并发控制（最大 3）
 * - IndexedDB 持久化进度
 * - 断点续传双端校验
 * - 失败自动重试（指数退避）
 *
 * @module utils/upload/ChunkUploader
 * @author 陈鸿昇
 */

import {
  CHUNK_SIZE,
  MAX_CONCURRENT,
  MAX_RETRIES,
  RETRY_BASE_DELAY,
  PROGRESS_THROTTLE,
  SPEED_SAMPLE_TIME
} from './constants'
import { uploadStorage } from './storage'
import type {
  UploadState,
  UploadProgress,
  UploadResult,
  UploaderOptions,
  UploadRecord,
  ChunkTask,
  HashWorkerMessage
} from './types'
import {
  fetchUploadPrecheck,
  fetchUploadChunk,
  fetchVerifyChunks,
  fetchMergeChunks,
  fetchAbortUpload
} from '@/api/chunk-upload'

// 动态导入 Worker（Vite 支持）
import HashWorker from './hash.worker?worker'

/**
 * 大文件切片上传器类
 */
export class ChunkUploader {
  // 配置
  private chunkSize: number
  private maxConcurrent: number
  private maxRetries: number
  private retryBaseDelay: number

  // 回调
  private onProgress?: (progress: UploadProgress) => void
  private onStateChange?: (state: UploadState) => void
  private onHashProgress?: (percent: number) => void

  // 状态
  private state: UploadState = 'idle'
  private isPaused = false
  private isAborted = false

  // 当前上传任务信息
  private currentFile: File | null = null
  private currentHash = ''
  private chunks: Blob[] = []
  private uploadedChunks: Set<number> = new Set()
  private pendingTasks: ChunkTask[] = []
  private activeTasks: Map<number, Promise<void>> = new Map()

  // 进度统计
  private uploadStartTime = 0
  private lastProgressTime = 0
  private lastUploadedBytes = 0
  private currentSpeed = 0

  // beforeunload 处理器引用
  private beforeUnloadHandler: ((e: BeforeUnloadEvent) => void) | null = null

  constructor(options: UploaderOptions = {}) {
    this.chunkSize = options.chunkSize || CHUNK_SIZE
    this.maxConcurrent = options.maxConcurrent || MAX_CONCURRENT
    this.maxRetries = options.maxRetries || MAX_RETRIES
    this.retryBaseDelay = options.retryBaseDelay || RETRY_BASE_DELAY
    this.onProgress = options.onProgress
    this.onStateChange = options.onStateChange
    this.onHashProgress = options.onHashProgress
  }

  /**
   * 上传文件
   * @param file 要上传的文件
   * @returns 上传结果
   */
  async upload(file: File): Promise<UploadResult> {
    // 重置状态
    this.reset()
    this.currentFile = file
    this.setupBeforeUnload()

    try {
      // 1. 计算文件哈希
      this.setState('hashing')
      this.currentHash = await this.calculateHash(file)

      // 2. 切分文件
      this.chunks = this.sliceFile(file)
      const totalChunks = this.chunks.length

      // 3. 预检
      this.setState('prechecking')
      const precheckResult = await fetchUploadPrecheck({
        fileHash: this.currentHash,
        fileName: file.name,
        fileSize: file.size,
        totalChunks
      })

      // 秒传
      if (precheckResult.canSkip && precheckResult.fileUrl) {
        this.setState('completed')
        await uploadStorage.deleteRecord(this.currentHash)
        return {
          url: precheckResult.fileUrl,
          name: file.name,
          size: file.size,
          hash: this.currentHash
        }
      }

      // 4. 断点续传校验
      const serverUploadedChunks = new Set(precheckResult.uploadedChunks || [])
      const localRecord = await uploadStorage.getRecord(this.currentHash)

      // 双端校验：取交集
      if (localRecord) {
        const localUploadedChunks = new Set(localRecord.uploadedChunks)
        // 实际已上传的分片 = 本地记录 ∩ 服务端记录
        for (const index of localUploadedChunks) {
          if (serverUploadedChunks.has(index)) {
            this.uploadedChunks.add(index)
          }
        }
      } else {
        // 没有本地记录，使用服务端记录
        this.uploadedChunks = serverUploadedChunks
      }

      // 保存/更新本地记录
      await uploadStorage.saveRecord({
        fileHash: this.currentHash,
        fileName: file.name,
        fileSize: file.size,
        totalChunks,
        uploadedChunks: Array.from(this.uploadedChunks),
        createdAt: localRecord?.createdAt || Date.now(),
        updatedAt: Date.now()
      })

      // 5. 构建待上传任务
      this.buildPendingTasks(totalChunks)

      // 6. 开始上传
      this.setState('uploading')
      this.uploadStartTime = Date.now()
      await this.uploadChunks()

      // 检查是否被中止
      if (this.isAborted) {
        throw new Error('上传已取消')
      }

      // 7. 合并
      this.setState('merging')
      const mergeResult = await fetchMergeChunks({
        fileHash: this.currentHash,
        fileName: file.name,
        totalChunks
      })

      // 8. 清理本地记录
      await uploadStorage.deleteRecord(this.currentHash)

      this.setState('completed')
      return {
        url: mergeResult.url,
        name: mergeResult.name,
        size: mergeResult.size,
        hash: this.currentHash
      }
    } catch (error) {
      if (!this.isAborted) {
        this.setState('failed')
      }
      throw error
    } finally {
      this.cleanupBeforeUnload()
    }
  }

  /**
   * 暂停上传
   */
  pause(): void {
    if (this.state === 'uploading') {
      this.isPaused = true
      this.setState('paused')
    }
  }

  /**
   * 恢复上传
   */
  async resume(): Promise<void> {
    if (this.state === 'paused' && this.currentFile) {
      this.isPaused = false
      this.setState('uploading')
      await this.uploadChunks()
    }
  }

  /**
   * 取消上传
   */
  async abort(): Promise<void> {
    this.isAborted = true
    this.isPaused = true
    this.setState('aborted')

    // 清理服务端临时文件
    if (this.currentHash) {
      try {
        await fetchAbortUpload(this.currentHash)
      } catch (error) {
        console.warn('[ChunkUploader] Failed to abort on server:', error)
      }
    }

    // 清理本地记录
    if (this.currentHash) {
      await uploadStorage.deleteRecord(this.currentHash)
    }
  }

  /**
   * 获取当前状态
   */
  getState(): UploadState {
    return this.state
  }

  // ========== 私有方法 ==========

  /**
   * 重置状态
   */
  private reset(): void {
    this.state = 'idle'
    this.isPaused = false
    this.isAborted = false
    this.currentFile = null
    this.currentHash = ''
    this.chunks = []
    this.uploadedChunks = new Set()
    this.pendingTasks = []
    this.activeTasks = new Map()
    this.uploadStartTime = 0
    this.lastProgressTime = 0
    this.lastUploadedBytes = 0
    this.currentSpeed = 0
  }

  /**
   * 设置状态
   */
  private setState(state: UploadState): void {
    this.state = state
    this.onStateChange?.(state)
  }

  /**
   * 切分文件
   */
  private sliceFile(file: File): Blob[] {
    const chunks: Blob[] = []
    let offset = 0

    while (offset < file.size) {
      const end = Math.min(offset + this.chunkSize, file.size)
      chunks.push(file.slice(offset, end))
      offset = end
    }

    return chunks
  }

  /**
   * 计算文件哈希（使用 Web Worker）
   */
  private calculateHash(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const worker = new HashWorker()

      worker.onmessage = (event: MessageEvent<HashWorkerMessage>) => {
        const { type, progress, hash, error } = event.data

        if (type === 'progress' && progress !== undefined) {
          this.onHashProgress?.(Math.round(progress * 100))
        } else if (type === 'done' && hash) {
          worker.terminate()
          resolve(hash)
        } else if (type === 'error') {
          worker.terminate()
          reject(new Error(error || '哈希计算失败'))
        }
      }

      worker.onerror = (error) => {
        worker.terminate()
        reject(error)
      }

      worker.postMessage({ file })
    })
  }

  /**
   * 构建待上传任务列表
   */
  private buildPendingTasks(totalChunks: number): void {
    this.pendingTasks = []

    for (let i = 0; i < totalChunks; i++) {
      if (!this.uploadedChunks.has(i)) {
        this.pendingTasks.push({
          index: i,
          blob: this.chunks[i],
          retries: 0
        })
      }
    }
  }

  /**
   * 并发上传分片
   */
  private async uploadChunks(): Promise<void> {
    const totalChunks = this.chunks.length

    // Promise 池并发控制
    while (this.pendingTasks.length > 0 || this.activeTasks.size > 0) {
      // 检查暂停/中止
      if (this.isPaused || this.isAborted) {
        // 等待所有活动任务完成
        await Promise.all(this.activeTasks.values())
        return
      }

      // 填充活动任务池
      while (
        this.pendingTasks.length > 0 &&
        this.activeTasks.size < this.maxConcurrent &&
        !this.isPaused &&
        !this.isAborted
      ) {
        const task = this.pendingTasks.shift()!
        const promise = this.uploadSingleChunk(task, totalChunks)
        this.activeTasks.set(task.index, promise)

        // 任务完成后从活动池移除
        promise.finally(() => {
          this.activeTasks.delete(task.index)
        })
      }

      // 等待任意一个任务完成
      if (this.activeTasks.size > 0) {
        await Promise.race(this.activeTasks.values())
      }
    }
  }

  /**
   * 上传单个分片（带重试）
   */
  private async uploadSingleChunk(task: ChunkTask, totalChunks: number): Promise<void> {
    try {
      await fetchUploadChunk(task.blob, this.currentHash, task.index, totalChunks)

      // 上传成功
      this.uploadedChunks.add(task.index)

      // 更新本地存储
      await uploadStorage.addUploadedChunk(this.currentHash, task.index)

      // 更新进度
      this.updateProgress(totalChunks)
    } catch (error) {
      // 重试逻辑
      if (task.retries < this.maxRetries) {
        task.retries++
        const delay = this.retryBaseDelay * Math.pow(2, task.retries - 1)
        console.warn(
          `[ChunkUploader] Chunk ${task.index} failed, retrying in ${delay}ms (${task.retries}/${this.maxRetries})`
        )

        await this.sleep(delay)

        // 重新加入待上传队列
        if (!this.isPaused && !this.isAborted) {
          this.pendingTasks.unshift(task)
        }
      } else {
        console.error(`[ChunkUploader] Chunk ${task.index} failed after ${this.maxRetries} retries`)
        throw error
      }
    }
  }

  /**
   * 更新进度
   */
  private updateProgress(totalChunks: number): void {
    const now = Date.now()

    // 节流
    if (now - this.lastProgressTime < PROGRESS_THROTTLE) {
      return
    }

    const uploadedChunksCount = this.uploadedChunks.size
    const uploadedBytes = uploadedChunksCount * this.chunkSize
    const totalBytes = this.currentFile?.size || 0

    // 计算速度
    const timeDelta = now - this.lastProgressTime
    if (timeDelta >= SPEED_SAMPLE_TIME) {
      const bytesDelta = uploadedBytes - this.lastUploadedBytes
      this.currentSpeed = (bytesDelta / timeDelta) * 1000
      this.lastUploadedBytes = uploadedBytes
      this.lastProgressTime = now
    } else if (this.lastProgressTime === 0) {
      this.lastProgressTime = now
      this.lastUploadedBytes = uploadedBytes
    }

    // 计算剩余时间
    const remainingBytes = totalBytes - uploadedBytes
    const remainingTime = this.currentSpeed > 0 ? remainingBytes / this.currentSpeed : 0

    const progress: UploadProgress = {
      phase: 'uploading',
      percent: Math.round((uploadedChunksCount / totalChunks) * 100),
      uploadedBytes: Math.min(uploadedBytes, totalBytes),
      totalBytes,
      uploadedChunks: uploadedChunksCount,
      totalChunks,
      speed: this.currentSpeed,
      remainingTime
    }

    this.onProgress?.(progress)
  }

  /**
   * 设置 beforeunload 处理器
   */
  private setupBeforeUnload(): void {
    this.beforeUnloadHandler = () => {
      // 紧急备份到 localStorage
      if (this.currentHash && this.currentFile) {
        const record: UploadRecord = {
          fileHash: this.currentHash,
          fileName: this.currentFile.name,
          fileSize: this.currentFile.size,
          totalChunks: this.chunks.length,
          uploadedChunks: Array.from(this.uploadedChunks),
          createdAt: Date.now(),
          updatedAt: Date.now()
        }
        uploadStorage.backupToLocalStorage(record)
      }
    }

    window.addEventListener('beforeunload', this.beforeUnloadHandler)
  }

  /**
   * 清理 beforeunload 处理器
   */
  private cleanupBeforeUnload(): void {
    if (this.beforeUnloadHandler) {
      window.removeEventListener('beforeunload', this.beforeUnloadHandler)
      this.beforeUnloadHandler = null
    }
  }

  /**
   * 延迟函数
   */
  private sleep(ms: number): Promise<void> {
    return new Promise((resolve) => setTimeout(resolve, ms))
  }
}

export default ChunkUploader
