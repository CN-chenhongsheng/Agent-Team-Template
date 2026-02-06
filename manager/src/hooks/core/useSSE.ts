/**
 * useSSE - Server-Sent Events (SSE) 通用连接 Hook
 *
 * 提供完整的 SSE 连接管理能力，支持事件监听、自动重连、错误处理等功能。
 * 可用于实时数据推送、在线状态监控、消息通知等场景。
 *
 * ## 主要功能
 *
 * 1. 连接管理 - 自动连接/断开、生命周期管理
 * 2. 事件监听 - 支持自定义事件类型和处理器
 * 3. 错误处理 - 自动错误处理和重连机制
 * 4. URL 构建 - 支持 token 等参数自动注入
 * 5. 状态管理 - 连接状态、错误状态等
 *
 * ## 使用示例
 *
 * ```typescript
 * const { connect, disconnect, isConnected } = useSSE({
 *   url: '/api/v1/system/user/online/stream',
 *   events: {
 *     'status-update': (data) => {
 *       console.log('Status updated:', data)
 *     },
 *     'init': (data) => {
 *       console.log('Connected:', data)
 *     }
 *   },
 *   onError: (error) => {
 *     console.error('SSE error:', error)
 *   },
 *   autoConnect: true
 * })
 * ```
 *
 * @module useSSE
 * @author 陈鸿昇
 */

import { ref, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/store/modules/user'
import { fetchRefreshToken } from '@/api/auth'

/**
 * SSE 事件处理器类型
 */
export type SSEEventHandler = (data: any) => void

/**
 * SSE 配置选项
 */
export interface UseSSEOptions {
  /** SSE 端点 URL（相对路径或完整 URL） */
  url: string
  /** 事件处理器映射 */
  events?: Record<string, SSEEventHandler>
  /** 错误处理回调 */
  onError?: (error: Event) => void
  /** 连接成功回调 */
  onConnect?: () => void
  /** 断开连接回调 */
  onDisconnect?: () => void
  /** 是否自动连接（组件挂载时） */
  autoConnect?: boolean
  /** 是否在 URL 中自动添加 token 参数 */
  withToken?: boolean
  /** 自定义 URL 参数 */
  params?: Record<string, string>
  /** 是否启用自动重连 */
  autoReconnect?: boolean
  /** 重连延迟（毫秒） */
  reconnectDelay?: number
  /** 最大重连次数 */
  maxReconnectAttempts?: number
}

/**
 * SSE Hook 返回值
 */
export interface UseSSEReturn {
  /** 连接状态 */
  isConnected: Readonly<Ref<boolean>>
  /** 连接错误 */
  error: Readonly<Ref<Event | null>>
  /** 手动连接 */
  connect: () => Promise<void>
  /** 手动断开 */
  disconnect: () => void
  /** 重新连接 */
  reconnect: () => void
  /** EventSource 实例（用于高级操作） */
  eventSource: Readonly<Ref<EventSource | null>>
}

/**
 * 通用 SSE 连接 Hook
 */
export function useSSE(options: UseSSEOptions): UseSSEReturn {
  const {
    url,
    events = {},
    onError,
    onConnect,
    onDisconnect,
    autoConnect = true,
    withToken = false,
    params = {},
    autoReconnect = false,
    reconnectDelay = 3000,
    maxReconnectAttempts = 5
  } = options

  const isConnected = ref(false)
  const error = ref<Event | null>(null)
  const eventSource = ref<EventSource | null>(null)
  let reconnectAttempts = 0
  let reconnectTimer: NodeJS.Timeout | null = null

  /**
   * 构建完整的 SSE URL
   */
  const buildUrl = (): string => {
    const baseURL = import.meta.env.VITE_API_URL || ''
    const fullUrl = url.startsWith('http') ? url : `${baseURL}${url}`
    const urlParams = new URLSearchParams()

    // 添加 token（如果需要）
    if (withToken) {
      const userStore = useUserStore()
      const token = userStore.accessToken
      if (token) {
        urlParams.append('token', token)
      }
    }

    // 添加自定义参数
    Object.entries(params).forEach(([key, value]) => {
      urlParams.append(key, value)
    })

    const queryString = urlParams.toString()
    return queryString ? `${fullUrl}?${queryString}` : fullUrl
  }

  /**
   * 连接 SSE
   */
  const connect = async () => {
    // 如果已连接，先断开
    if (eventSource.value) {
      disconnect()
    }

    try {
      // 如果需要 token，确保 token 已刷新
      if (withToken) {
        const userStore = useUserStore()
        // 如果用户已登录但 token 为空（页面刷新导致），先刷新 token
        if (userStore.isLogin && !userStore.accessToken) {
          try {
            console.log('[SSE] Token 为空，正在刷新...')
            const newAccessToken = await fetchRefreshToken()
            userStore.setToken(newAccessToken)
            console.log('[SSE] Token 刷新成功')
          } catch (err) {
            console.error('[SSE] Token 刷新失败:', err)
            error.value = err as Event
            onError?.(err as Event)
            return
          }
        }
      }

      const sseUrl = buildUrl()

      eventSource.value = new EventSource(sseUrl)
      error.value = null
      reconnectAttempts = 0

      // 注册事件监听器
      Object.entries(events).forEach(([eventName, handler]) => {
        eventSource.value?.addEventListener(eventName, (event: MessageEvent) => {
          try {
            // 尝试解析 JSON，如果失败则使用原始数据
            let data = event.data
            if (event.data) {
              try {
                // 先尝试解析为 JSON
                data = JSON.parse(event.data)
              } catch {
                // 如果不是 JSON，直接使用原始字符串
                data = event.data
              }
            }
            handler(data)
          } catch (err) {
            console.error(`[SSE] Failed to handle event "${eventName}":`, err)
            // 即使处理出错，也尝试传递原始数据
            handler(event.data)
          }
        })
      })

      // 连接成功
      eventSource.value.addEventListener('open', () => {
        console.log('[SSE] 连接成功')
        isConnected.value = true
        error.value = null
        reconnectAttempts = 0
        onConnect?.()
      })

      // 连接错误
      eventSource.value.onerror = (err: Event) => {
        console.error('[SSE] 连接错误:', err)
        error.value = err
        isConnected.value = false
        onError?.(err)

        // 自动重连
        if (autoReconnect && reconnectAttempts < maxReconnectAttempts) {
          reconnectAttempts++
          console.log(`[SSE] 尝试重新连接 (${reconnectAttempts}/${maxReconnectAttempts})...`)
          reconnectTimer = setTimeout(() => {
            disconnect()
            connect().catch((connectErr) => {
              console.error('[SSE] 重连失败:', connectErr)
            })
          }, reconnectDelay)
        } else if (reconnectAttempts >= maxReconnectAttempts) {
          console.error('[SSE] 最大重连次数达到')
        }
      }
    } catch (err) {
      console.error('[SSE] 创建连接失败:', err)
      error.value = err as Event
      onError?.(err as Event)
    }
  }

  /**
   * 断开连接
   */
  const disconnect = () => {
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }

    if (eventSource.value) {
      console.log('[SSE] 断开连接')
      eventSource.value.close()
      eventSource.value = null
      isConnected.value = false
      onDisconnect?.()
    }
  }

  /**
   * 重新连接
   */
  const reconnect = () => {
    disconnect()
    reconnectAttempts = 0
    setTimeout(() => {
      connect().catch((err) => {
        console.error('[SSE] 重新连接失败:', err)
      })
    }, reconnectDelay)
  }

  // 自动连接
  if (autoConnect) {
    onMounted(() => {
      connect().catch((err) => {
        console.error('[SSE] 自动连接失败:', err)
      })
    })
  }

  // 组件卸载时断开连接
  onUnmounted(() => {
    disconnect()
  })

  return {
    isConnected: readonly(isConnected),
    error: readonly(error),
    connect,
    disconnect,
    reconnect,
    eventSource: readonly(eventSource)
  }
}
