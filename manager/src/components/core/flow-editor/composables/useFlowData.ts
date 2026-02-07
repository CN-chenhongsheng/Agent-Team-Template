/**
 * 流程编辑器数据缓存管理 Hook
 * 使用 sessionStorage 实现数据缓存，支持过期时间控制
 */

import type { FlowCacheData } from '../types'

/**
 * 默认缓存过期时间：1 小时（毫秒）
 */
const DEFAULT_CACHE_EXPIRE_TIME = 60 * 60 * 1000

/**
 * 缓存键前缀
 */
const CACHE_KEY_PREFIX = 'flow-editor-'

/**
 * 生成缓存键
 */
function getCacheKey(flowId?: number): string {
  return `${CACHE_KEY_PREFIX}${flowId || 'new'}`
}

/**
 * 流程数据缓存管理
 */
export function useFlowData(expireTime: number = DEFAULT_CACHE_EXPIRE_TIME) {
  /**
   * 保存缓存
   */
  const saveCache = (data: Omit<FlowCacheData, 'cachedAt' | 'expiresIn'>): void => {
    try {
      const cacheData: FlowCacheData = {
        ...data,
        cachedAt: Date.now(),
        expiresIn: expireTime
      }
      const key = getCacheKey(data.flowId)
      sessionStorage.setItem(key, JSON.stringify(cacheData))
    } catch (error) {
      console.error('保存缓存失败:', error)
    }
  }

  /**
   * 加载缓存
   */
  const loadCache = (flowId?: number): FlowCacheData | null => {
    try {
      const key = getCacheKey(flowId)
      const cached = sessionStorage.getItem(key)

      if (!cached) {
        return null
      }

      const cacheData: FlowCacheData = JSON.parse(cached)

      // 检查是否过期
      const now = Date.now()
      const isExpired = now - cacheData.cachedAt > cacheData.expiresIn

      if (isExpired) {
        // 缓存已过期，清除并返回 null
        clearCache(flowId)
        return null
      }

      return cacheData
    } catch (error) {
      console.error('加载缓存失败:', error)
      return null
    }
  }

  /**
   * 清除缓存
   */
  const clearCache = (flowId?: number): void => {
    try {
      const key = getCacheKey(flowId)
      sessionStorage.removeItem(key)
    } catch (error) {
      console.error('清除缓存失败:', error)
    }
  }

  /**
   * 清除所有流程编辑器缓存
   */
  const clearAllCache = (): void => {
    try {
      const keys = Object.keys(sessionStorage)
      keys.forEach((key) => {
        if (key.startsWith(CACHE_KEY_PREFIX)) {
          sessionStorage.removeItem(key)
        }
      })
    } catch (error) {
      console.error('清除所有缓存失败:', error)
    }
  }

  /**
   * 清理过期缓存
   */
  const cleanExpiredCache = (): void => {
    try {
      const keys = Object.keys(sessionStorage)
      const now = Date.now()

      keys.forEach((key) => {
        if (key.startsWith(CACHE_KEY_PREFIX)) {
          const cached = sessionStorage.getItem(key)
          if (cached) {
            try {
              const cacheData: FlowCacheData = JSON.parse(cached)
              const isExpired = now - cacheData.cachedAt > cacheData.expiresIn
              if (isExpired) {
                sessionStorage.removeItem(key)
              }
            } catch {
              // 解析失败，删除该缓存
              sessionStorage.removeItem(key)
            }
          }
        }
      })
    } catch (error) {
      console.error('清理过期缓存失败:', error)
    }
  }

  return {
    saveCache,
    loadCache,
    clearCache,
    clearAllCache,
    cleanExpiredCache
  }
}
