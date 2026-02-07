/**
 * 管理范围编辑器数据缓存管理 Hook
 * 使用 sessionStorage 实现数据缓存，支持过期时间控制
 */

import type { ScopeCacheData } from '../types'

/** 默认缓存过期时间：1 小时（毫秒） */
const DEFAULT_CACHE_EXPIRE_TIME = 60 * 60 * 1000

/** 缓存键前缀 */
const CACHE_KEY_PREFIX = 'scope-editor-'

/** 生成缓存键 */
function getCacheKey(userId: number): string {
  return `${CACHE_KEY_PREFIX}${userId}`
}

/**
 * 管理范围编辑器缓存管理
 */
export function useScopeCache(expireTime: number = DEFAULT_CACHE_EXPIRE_TIME) {
  /** 保存缓存 */
  const saveCache = (data: Omit<ScopeCacheData, 'cachedAt' | 'expiresIn'>): void => {
    try {
      const cacheData: ScopeCacheData = {
        ...data,
        cachedAt: Date.now(),
        expiresIn: expireTime
      }
      const key = getCacheKey(data.userId)
      sessionStorage.setItem(key, JSON.stringify(cacheData))
    } catch (error) {
      console.error('[useScopeCache] 保存缓存失败:', error)
    }
  }

  /** 加载缓存 */
  const loadCache = (userId: number): ScopeCacheData | null => {
    try {
      const key = getCacheKey(userId)
      const cached = sessionStorage.getItem(key)

      if (!cached) return null

      const cacheData: ScopeCacheData = JSON.parse(cached)

      // 检查是否过期
      if (Date.now() - cacheData.cachedAt > cacheData.expiresIn) {
        clearCache(userId)
        return null
      }

      return cacheData
    } catch (error) {
      console.error('[useScopeCache] 加载缓存失败:', error)
      return null
    }
  }

  /** 清除缓存 */
  const clearCache = (userId: number): void => {
    try {
      const key = getCacheKey(userId)
      sessionStorage.removeItem(key)
    } catch (error) {
      console.error('[useScopeCache] 清除缓存失败:', error)
    }
  }

  return {
    saveCache,
    loadCache,
    clearCache
  }
}
