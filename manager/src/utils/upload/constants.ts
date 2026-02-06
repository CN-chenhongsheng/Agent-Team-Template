/**
 * 大文件切片上传 - 常量配置
 *
 * @module utils/upload/constants
 * @author 陈鸿昇
 */

/** 默认分片大小：5MB */
export const CHUNK_SIZE = 5 * 1024 * 1024

/** 最大并发上传数 */
export const MAX_CONCURRENT = 3

/** 每个分片最大重试次数 */
export const MAX_RETRIES = 3

/** 重试基础延迟（毫秒） */
export const RETRY_BASE_DELAY = 1000

/** 过期天数（超过此天数的上传记录将被清理） */
export const EXPIRED_DAYS = 7

/** IndexedDB 数据库名称 */
export const DB_NAME = 'chunk-upload-db'

/** IndexedDB 数据库版本 */
export const DB_VERSION = 1

/** IndexedDB 存储名称 */
export const STORE_NAME = 'upload-records'

/** LocalStorage 紧急备份键名前缀 */
export const BACKUP_KEY_PREFIX = 'chunk-upload-backup-'

/** 上传接口基础路径 */
export const UPLOAD_API_BASE = '/api/v1/upload'

/** 预检接口路径 */
export const API_PRECHECK = `${UPLOAD_API_BASE}/precheck`

/** 分片上传接口路径 */
export const API_CHUNK = `${UPLOAD_API_BASE}/chunk`

/** 校验接口路径 */
export const API_VERIFY = `${UPLOAD_API_BASE}/verify`

/** 合并接口路径 */
export const API_MERGE = `${UPLOAD_API_BASE}/merge`

/** 取消上传接口路径 */
export const API_ABORT = `${UPLOAD_API_BASE}/abort`

/** 进度更新节流时间（毫秒） */
export const PROGRESS_THROTTLE = 100

/** 速度计算采样时间（毫秒） */
export const SPEED_SAMPLE_TIME = 1000
