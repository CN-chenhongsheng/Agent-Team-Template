/**
 * 管理范围编辑器跨标签页通信 Hook
 * 使用 Broadcast Channel API 实现跨标签页通信
 */

import { onUnmounted } from 'vue'
import type { ScopeEditorMessage, ScopeEditorMessageType } from '../types'

/** 默认频道名 */
const DEFAULT_CHANNEL_NAME = 'scope-editor-channel'

/**
 * 跨标签页通信管理
 */
export function useScopeCommunication(channelName: string = DEFAULT_CHANNEL_NAME) {
  let channel: BroadcastChannel | null = null
  let isSupported = false

  // 检查浏览器是否支持 Broadcast Channel
  if (typeof BroadcastChannel !== 'undefined') {
    isSupported = true
    try {
      channel = new BroadcastChannel(channelName)
    } catch (error) {
      console.error('[useScopeCommunication] 创建 Broadcast Channel 失败:', error)
      isSupported = false
    }
  } else {
    console.warn('[useScopeCommunication] 当前浏览器不支持 Broadcast Channel API')
  }

  /** 发送消息 */
  const sendMessage = (
    type: ScopeEditorMessageType,
    data?: { userId?: number; nickname?: string; error?: string }
  ): void => {
    if (!isSupported || !channel) {
      console.warn('[useScopeCommunication] Broadcast Channel 不可用，无法发送消息')
      return
    }

    try {
      const message: ScopeEditorMessage = {
        type,
        userId: data?.userId,
        nickname: data?.nickname,
        error: data?.error,
        timestamp: Date.now()
      }
      channel.postMessage(message)
    } catch (error) {
      console.error('[useScopeCommunication] 发送消息失败:', error)
    }
  }

  /** 监听消息 */
  const onMessage = (callback: (message: ScopeEditorMessage) => void): void => {
    if (!isSupported || !channel) {
      console.warn('[useScopeCommunication] Broadcast Channel 不可用，无法监听消息')
      return
    }

    try {
      channel.onmessage = (event: MessageEvent<ScopeEditorMessage>) => {
        callback(event.data)
      }
    } catch (error) {
      console.error('[useScopeCommunication] 监听消息失败:', error)
    }
  }

  /** 关闭频道 */
  const closeChannel = (): void => {
    if (channel) {
      try {
        channel.close()
        channel = null
      } catch (error) {
        console.error('[useScopeCommunication] 关闭频道失败:', error)
      }
    }
  }

  // 组件卸载时自动关闭频道
  onUnmounted(() => {
    closeChannel()
  })

  return {
    isSupported,
    sendMessage,
    onMessage,
    closeChannel
  }
}
