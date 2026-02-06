/**
 * 大文件切片上传 - Web Worker 文件哈希计算
 *
 * 在独立线程中计算文件 MD5 哈希，避免阻塞主线程
 *
 * @module utils/upload/hash.worker
 * @author 陈鸿昇
 */

import SparkMD5 from 'spark-md5'

/** Worker 消息类型 */
interface WorkerInputMessage {
  file: File
  chunkSize?: number
}

interface WorkerOutputMessage {
  type: 'progress' | 'done' | 'error'
  progress?: number
  hash?: string
  error?: string
}

// 默认分片大小：2MB（用于哈希计算，与上传分片大小不同）
const DEFAULT_HASH_CHUNK_SIZE = 2 * 1024 * 1024

/**
 * 计算文件 MD5 哈希
 * @param file 文件对象
 * @param chunkSize 分片大小
 */
async function calculateHash(file: File, chunkSize: number): Promise<string> {
  const spark = new SparkMD5.ArrayBuffer()
  const totalChunks = Math.ceil(file.size / chunkSize)
  let currentChunk = 0

  while (currentChunk < totalChunks) {
    const start = currentChunk * chunkSize
    const end = Math.min(start + chunkSize, file.size)
    const chunk = file.slice(start, end)

    try {
      const buffer = await chunk.arrayBuffer()
      spark.append(buffer)

      currentChunk++

      // 发送进度
      const progress = Math.min(currentChunk / totalChunks, 1)
      self.postMessage({
        type: 'progress',
        progress
      } as WorkerOutputMessage)
    } catch (error) {
      throw new Error(`Failed to read chunk ${currentChunk}: ${error}`)
    }
  }

  return spark.end()
}

// 监听主线程消息
self.onmessage = async (event: MessageEvent<WorkerInputMessage>) => {
  const { file, chunkSize = DEFAULT_HASH_CHUNK_SIZE } = event.data

  try {
    const hash = await calculateHash(file, chunkSize)

    self.postMessage({
      type: 'done',
      hash
    } as WorkerOutputMessage)
  } catch (error) {
    self.postMessage({
      type: 'error',
      error: error instanceof Error ? error.message : String(error)
    } as WorkerOutputMessage)
  }
}

// 导出空对象使 TypeScript 满意（Web Worker 模块）
export {}
