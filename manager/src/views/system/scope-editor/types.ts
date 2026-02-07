/**
 * 管理范围编辑器类型定义
 */

/** 缓存数据结构 */
export interface ScopeCacheData {
  /** 用户 ID */
  userId: number
  /** 用户名 */
  username: string
  /** 用户昵称 */
  nickname: string
  /** 当前管理范围 JSON */
  manageScope: string
  /** 用户完整数据（用于提交更新） */
  userData: Record<string, any>
  /** 缓存时间戳 */
  cachedAt: number
  /** 过期时间（毫秒） */
  expiresIn: number
}

/** 跨标签页通信消息类型 */
export type ScopeEditorMessageType = 'scope-saved' | 'scope-closed' | 'scope-error'

/** 跨标签页通信消息 */
export interface ScopeEditorMessage {
  /** 消息类型 */
  type: ScopeEditorMessageType
  /** 用户 ID */
  userId?: number
  /** 用户昵称 */
  nickname?: string
  /** 时间戳 */
  timestamp: number
  /** 错误信息（type 为 scope-error 时） */
  error?: string
}
