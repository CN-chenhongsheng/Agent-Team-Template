/**
 * 大文件切片上传模块
 *
 * 提供大文件切片上传功能，支持：
 * - 文件切片（5MB/片）
 * - Web Worker 计算 MD5
 * - Promise 池并发控制（最大 3）
 * - IndexedDB 持久化进度
 * - 断点续传双端校验
 * - 失败自动重试（指数退避）
 *
 * @example
 * ```typescript
 * import { ChunkUploader } from '@/utils/upload'
 *
 * const uploader = new ChunkUploader({
 *   onProgress: (progress) => {
 *     console.log(`上传进度: ${progress.percent}%`)
 *   },
 *   onStateChange: (state) => {
 *     console.log(`状态变化: ${state}`)
 *   }
 * })
 *
 * // 上传文件
 * const result = await uploader.upload(file)
 * console.log('上传完成:', result.url)
 *
 * // 暂停上传
 * uploader.pause()
 *
 * // 恢复上传
 * await uploader.resume()
 *
 * // 取消上传
 * await uploader.abort()
 * ```
 *
 * @module utils/upload
 * @author 陈鸿昇
 */

// 导出核心类
export { ChunkUploader, default } from './ChunkUploader'

// 导出存储类
export { uploadStorage } from './storage'

// 导出类型
export type {
  UploadState,
  UploadProgress,
  UploadResult,
  UploaderOptions,
  UploadRecord,
  PrecheckRequest,
  PrecheckResponse,
  ChunkUploadRequest,
  MergeRequest,
  MergeResponse,
  HashWorkerMessage,
  ChunkTask
} from './types'

// 导出枚举
export { UploadState as UploadStateEnum } from './types'

// 导出常量
export {
  CHUNK_SIZE,
  MAX_CONCURRENT,
  MAX_RETRIES,
  RETRY_BASE_DELAY,
  EXPIRED_DAYS,
  DB_NAME,
  DB_VERSION,
  STORE_NAME,
  BACKUP_KEY_PREFIX,
  UPLOAD_API_BASE,
  API_PRECHECK,
  API_CHUNK,
  API_VERIFY,
  API_MERGE,
  API_ABORT
} from './constants'
