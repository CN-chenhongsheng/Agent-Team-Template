/**
 * 大文件切片上传 - 类型定义
 *
 * @module utils/upload/types
 * @author 陈鸿昇
 */

/** 上传状态枚举 */
export enum UploadState {
  /** 空闲状态 */
  IDLE = 'idle',
  /** 计算哈希中 */
  HASHING = 'hashing',
  /** 预检中 */
  PRECHECKING = 'prechecking',
  /** 上传中 */
  UPLOADING = 'uploading',
  /** 已暂停 */
  PAUSED = 'paused',
  /** 合并中 */
  MERGING = 'merging',
  /** 已完成 */
  COMPLETED = 'completed',
  /** 已取消 */
  ABORTED = 'aborted',
  /** 失败 */
  FAILED = 'failed'
}

/** 上传进度信息 */
export interface UploadProgress {
  /** 当前阶段 */
  phase: 'hashing' | 'uploading' | 'merging'
  /** 总体进度百分比 (0-100) */
  percent: number
  /** 已上传字节数 */
  uploadedBytes: number
  /** 总字节数 */
  totalBytes: number
  /** 已上传分片数 */
  uploadedChunks: number
  /** 总分片数 */
  totalChunks: number
  /** 上传速度 (bytes/s) */
  speed: number
  /** 预估剩余时间 (秒) */
  remainingTime: number
}

/** 上传结果 */
export interface UploadResult {
  /** 文件访问 URL */
  url: string
  /** 文件名 */
  name: string
  /** 文件大小 */
  size: number
  /** 文件哈希 */
  hash: string
}

/** 上传器配置选项 */
export interface UploaderOptions {
  /** 分片大小（字节），默认 5MB */
  chunkSize?: number
  /** 最大并发数，默认 3 */
  maxConcurrent?: number
  /** 每个分片最大重试次数，默认 3 */
  maxRetries?: number
  /** 重试基础延迟（毫秒），默认 1000 */
  retryBaseDelay?: number
  /** 进度回调 */
  onProgress?: (progress: UploadProgress) => void
  /** 状态变化回调 */
  onStateChange?: (state: UploadState) => void
  /** 哈希计算进度回调 */
  onHashProgress?: (percent: number) => void
}

/** 预检接口请求参数 */
export interface PrecheckRequest {
  /** 文件 MD5 哈希 */
  fileHash: string
  /** 原始文件名 */
  fileName: string
  /** 文件大小 */
  fileSize: number
  /** 分片总数 */
  totalChunks: number
}

/** 预检接口响应数据 */
export interface PrecheckResponse {
  /** 是否可以秒传 */
  canSkip: boolean
  /** 已上传的分片索引列表 */
  uploadedChunks: number[]
  /** 秒传时返回的文件 URL */
  fileUrl?: string
}

/** 分片上传请求参数 */
export interface ChunkUploadRequest {
  /** 分片文件 */
  file: Blob
  /** 文件 MD5 哈希 */
  fileHash: string
  /** 分片索引（从 0 开始） */
  chunkIndex: number
  /** 分片总数 */
  totalChunks: number
}

/** 合并请求参数 */
export interface MergeRequest {
  /** 文件 MD5 哈希 */
  fileHash: string
  /** 原始文件名 */
  fileName: string
  /** 分片总数 */
  totalChunks: number
}

/** 合并响应数据 */
export interface MergeResponse {
  /** 文件访问 URL */
  url: string
  /** 文件名 */
  name: string
  /** 文件大小 */
  size: number
}

/** IndexedDB 上传记录 */
export interface UploadRecord {
  /** 文件哈希（主键） */
  fileHash: string
  /** 文件名 */
  fileName: string
  /** 文件大小 */
  fileSize: number
  /** 分片总数 */
  totalChunks: number
  /** 已上传的分片索引列表 */
  uploadedChunks: number[]
  /** 创建时间戳 */
  createdAt: number
  /** 最后更新时间戳 */
  updatedAt: number
}

/** Web Worker 消息类型 */
export interface HashWorkerMessage {
  /** 消息类型 */
  type: 'progress' | 'done' | 'error'
  /** 哈希计算进度 (0-1) */
  progress?: number
  /** 计算完成的哈希值 */
  hash?: string
  /** 错误信息 */
  error?: string
}

/** 分片任务 */
export interface ChunkTask {
  /** 分片索引 */
  index: number
  /** 分片数据 */
  blob: Blob
  /** 重试次数 */
  retries: number
}
