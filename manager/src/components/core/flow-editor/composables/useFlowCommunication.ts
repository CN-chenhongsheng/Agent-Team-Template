/**
 * 流程编辑器跨标签页通信 Hook
 * 使用 Broadcast Channel API 实现跨标签页通信
 */

import { onUnmounted } from 'vue'
import type { FlowEditorMessage, FlowEditorMessageType } from '../types'

/**
 * 默认频道名
 */
const DEFAULT_CHANNEL_NAME = 'flow-editor-channel'

/**
 * 跨标签页通信管理
 */
export function useFlowCommunication(channelName: string = DEFAULT_CHANNEL_NAME) {
  let channel: BroadcastChannel | null = null
  let isSupported = false

  // 检查浏览器是否支持 Broadcast Channel
  if (typeof BroadcastChannel !== 'undefined') {
    isSupported = true
    try {
      channel = new BroadcastChannel(channelName)
    } catch (error) {
      console.error('创建 Broadcast Channel 失败:', error)
      isSupported = false
    }
  } else {
    console.warn('当前浏览器不支持 Broadcast Channel API')
  }

  /**
   * 发送消息
   */
  const sendMessage = (
    type: FlowEditorMessageType,
    data?: { flowId?: number; flowName?: string; error?: string }
  ): void => {
    if (!isSupported || !channel) {
      console.warn('Broadcast Channel 不可用，无法发送消息')
      return
    }

    try {
      const message: FlowEditorMessage = {
        type,
        flowId: data?.flowId,
        flowName: data?.flowName,
        error: data?.error,
        timestamp: Date.now()
      }
      channel.postMessage(message)
    } catch (error) {
      console.error('发送消息失败:', error)
    }
  }

  /**
   * 监听消息
   */
  const onMessage = (callback: (message: FlowEditorMessage) => void): void => {
    if (!isSupported || !channel) {
      console.warn('Broadcast Channel 不可用，无法监听消息')
      return
    }

    try {
      channel.onmessage = (event: MessageEvent<FlowEditorMessage>) => {
        callback(event.data)
      }
    } catch (error) {
      console.error('监听消息失败:', error)
    }
  }

  /**
   * 关闭频道
   */
  const closeChannel = (): void => {
    if (channel) {
      try {
        channel.close()
        channel = null
      } catch (error) {
        console.error('关闭频道失败:', error)
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
