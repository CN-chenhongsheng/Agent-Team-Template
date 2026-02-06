/**
 * useUserOnlineStatus - 用户在线状态管理 Hook
 *
 * 基于 useSSE 实现的用户在线状态实时监控。
 * 自动订阅用户在线状态变化，并提供状态查询接口。
 *
 * ## 主要功能
 *
 * 1. 实时状态订阅 - 自动订阅用户在线状态变化
 * 2. 状态映射管理 - 维护用户 ID 到在线状态的映射
 * 3. 状态查询 - 提供便捷的状态查询方法
 *
 * ## 使用示例
 *
 * ```typescript
 * const { onlineStatusMap, isUserOnline } = useUserOnlineStatus()
 *
 * // 查询用户是否在线
 * const status = isUserOnline(123)
 * ```
 *
 * @module useUserOnlineStatus
 * @author 陈鸿昇
 */

import { ref, computed, watch } from 'vue'
import { useSSE } from './useSSE'
import { useUserStore } from '@/store/modules/user'

/**
 * 在线状态更新数据
 */
export interface OnlineStatusUpdate {
  userId: number
  isOnline: boolean
}

/**
 * 用户在线状态 Hook
 */
export function useUserOnlineStatus() {
  // 用户在线状态映射：userId -> isOnline
  const onlineStatusMap = ref<Record<number, boolean>>({})

  // 使用 SSE 连接订阅在线状态流
  const { isConnected, error, connect, disconnect } = useSSE({
    url: '/api/v1/system/user/online/stream',
    withToken: true,
    autoConnect: true,
    autoReconnect: true,
    reconnectDelay: 3000,
    maxReconnectAttempts: 5,
    events: {
      'status-update': (data: OnlineStatusUpdate) => {
        if (data && typeof data.userId === 'number' && typeof data.isOnline === 'boolean') {
          onlineStatusMap.value[data.userId] = data.isOnline
          console.log(
            `[UserOnlineStatus] 用户 ${data.userId} 现在是 ${data.isOnline ? '在线' : '离线'}`
          )
        }
      },
      init: (data: any) => {
        console.log('[UserOnlineStatus] SSE 连接成功:', data)
      }
    },
    onError: (err) => {
      console.error('[UserOnlineStatus] 连接错误:', err)
    },
    onConnect: () => {
      console.log('[UserOnlineStatus] 连接到在线状态流')
    },
    onDisconnect: () => {
      console.log('[UserOnlineStatus] 断开连接到在线状态流')
    }
  })

  // 监听 token 变化，当 token 清空时自动断开 SSE 连接
  const userStore = useUserStore()
  watch(
    () => userStore.accessToken,
    (newToken, oldToken) => {
      // 当 token 从有值变为空时，断开 SSE 连接
      if (!newToken && oldToken) {
        console.log('[UserOnlineStatus] Token 已清空，断开 SSE 连接')
        disconnect()
      }
    }
  )

  /**
   * 查询用户是否在线
   * @param userId 用户 ID
   * @returns 是否在线，如果状态未知则返回 undefined
   */
  const isUserOnline = (userId: number): boolean | undefined => {
    return onlineStatusMap.value[userId]
  }

  /**
   * 获取所有在线用户 ID 列表
   */
  const getOnlineUserIds = computed(() => {
    return Object.entries(onlineStatusMap.value)
      .filter(([, isOnline]) => isOnline === true)
      .map(([userId]) => Number(userId))
  })

  /**
   * 获取所有离线用户 ID 列表
   */
  const getOfflineUserIds = computed(() => {
    return Object.entries(onlineStatusMap.value)
      .filter(([, isOnline]) => isOnline === false)
      .map(([userId]) => Number(userId))
  })

  /**
   * 获取在线用户数量
   */
  const onlineCount = computed(() => getOnlineUserIds.value.length)

  /**
   * 获取离线用户数量
   */
  const offlineCount = computed(() => getOfflineUserIds.value.length)

  return {
    /** 在线状态映射 */
    onlineStatusMap: readonly(onlineStatusMap),
    /** 连接状态 */
    isConnected,
    /** 连接错误 */
    error,
    /** 查询用户是否在线 */
    isUserOnline,
    /** 在线用户 ID 列表 */
    getOnlineUserIds,
    /** 离线用户 ID 列表 */
    getOfflineUserIds,
    /** 在线用户数量 */
    onlineCount,
    /** 离线用户数量 */
    offlineCount,
    /** 手动连接 */
    connect,
    /** 手动断开 */
    disconnect
  }
}
