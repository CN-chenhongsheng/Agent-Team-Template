/**
 * 流程编辑器类型定义
 */

import type { ApprovalNode } from '@/api/approval-manage'

/** 保存时传递的完整流程数据 */
export interface FlowSaveData {
  flowId?: number
  flowName: string
  flowCode: string
  businessType: string
  description: string
  nodes: ApprovalNode[]
}

/**
 * 流程编辑器 Props
 */
export interface FlowEditorProps {
  /** 流程 ID（编辑模式时传入） */
  flowId?: number
  /** 流程名称 */
  flowName?: string
  /** 流程编码 */
  flowCode?: string
  /** 业务类型 */
  businessType?: string
  /** 流程描述/备注 */
  description?: string
  /** 流程数据（编辑模式时传入） */
  flowData?: ApprovalNode[]
  /** 是否为新增模式 */
  isAdd?: boolean
}

/**
 * 流程编辑器 Emits
 */
export interface FlowEditorEmits {
  /** 保存成功 */
  (e: 'success', data: FlowSaveData): void
  /** 取消编辑 */
  (e: 'cancel'): void
  /** 关闭编辑器 */
  (e: 'close'): void
}

/**
 * 跨标签页通信消息类型
 */
export type FlowEditorMessageType = 'flow-saved' | 'flow-closed' | 'flow-error'

/**
 * 跨标签页通信消息
 */
export interface FlowEditorMessage {
  /** 消息类型 */
  type: FlowEditorMessageType
  /** 流程 ID */
  flowId?: number
  /** 流程名称 */
  flowName?: string
  /** 时间戳 */
  timestamp: number
  /** 错误信息（type 为 flow-error 时） */
  error?: string
}

/**
 * 缓存数据结构
 */
export interface FlowCacheData {
  /** 流程 ID */
  flowId?: number
  /** 流程名称 */
  flowName?: string
  /** 流程数据 */
  flowData?: ApprovalNode[]
  /** 是否为新增模式 */
  isAdd?: boolean
  /** 缓存时间戳 */
  cachedAt: number
  /** 过期时间（毫秒） */
  expiresIn: number
}

/**
 * 流程编辑器配置
 */
export interface FlowEditorConfig {
  /** 缓存过期时间（毫秒），默认 1 小时 */
  cacheExpireTime?: number
  /** Broadcast Channel 频道名 */
  channelName?: string
  /** 是否自动保存 */
  autoSave?: boolean
  /** 自动保存间隔（毫秒） */
  autoSaveInterval?: number
}
