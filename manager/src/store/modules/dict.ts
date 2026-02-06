/**
 * 字典数据状态管理模块
 *
 * 提供字典数据的全局缓存和状态管理
 *
 * ## 主要功能
 *
 * - 字典数据缓存（按字典编码）
 * - 防止重复请求（Promise 缓存）
 * - 响应式数据共享
 * - 字典数据刷新
 *
 * ## 使用场景
 *
 * - 表格按钮配置等字典数据的统一管理
 * - 避免多个组件重复加载同一字典数据
 * - 在字典管理页面更新后刷新缓存
 *
 * @module store/modules/dict
 * @author 陈鸿昇
 */
import { defineStore } from 'pinia'
import { reactive } from 'vue'
import { fetchGetDictDataList } from '@/api/system-manage'

interface DictDataCache {
  /** 字典数据列表 */
  data: Api.SystemManage.DictDataListItem[]
  /** 是否正在加载 */
  loading: boolean
  /** 加载 Promise（用于防止重复请求） */
  loadPromise: Promise<Api.SystemManage.DictDataListItem[]> | null
  /** 加载时间戳 */
  timestamp: number
}

interface ButtonConfig {
  icon: string
  text: string
  class: string
}

export const useDictStore = defineStore('dictStore', () => {
  // 字典数据缓存 Map<dictCode, DictDataCache>
  const dictCache = reactive<Map<string, DictDataCache>>(new Map())

  /**
   * 加载字典数据（单个）
   * @param dictCode 字典编码
   * @param force 是否强制重新加载（忽略缓存）
   * @returns 字典数据列表
   */
  const loadDictData = async (
    dictCode: string,
    force = false
  ): Promise<Api.SystemManage.DictDataListItem[]> => {
    if (!dictCode) {
      return []
    }

    const cache = dictCache.get(dictCode)

    // 如果有缓存且不强制刷新，直接返回缓存数据
    if (cache && !force && cache.data.length > 0) {
      return cache.data
    }

    // 如果正在加载，返回同一个 Promise（防止重复请求）
    if (cache?.loading && cache.loadPromise) {
      return cache.loadPromise
    }

    // 初始化缓存项
    if (!cache) {
      dictCache.set(dictCode, {
        data: [],
        loading: true,
        loadPromise: null,
        timestamp: 0
      })
    } else {
      cache.loading = true
    }

    const currentCache = dictCache.get(dictCode)!

    // 创建加载 Promise
    const loadPromise = fetchGetDictDataList(dictCode)
      .then((response) => {
        // 新的 API 返回格式是 Record<string, DictDataList>
        // 需要从响应对象中提取对应字典编码的数据
        if (response && typeof response === 'object') {
          const data = response[dictCode]
          if (data && Array.isArray(data)) {
            currentCache.data = data
            currentCache.timestamp = Date.now()
          } else {
            currentCache.data = []
          }
        } else {
          currentCache.data = []
        }
        return currentCache.data
      })
      .catch((error) => {
        console.error(`加载字典数据失败 [${dictCode}]:`, error)
        currentCache.data = []
        return []
      })
      .finally(() => {
        currentCache.loading = false
        currentCache.loadPromise = null
      })

    // 缓存 Promise
    currentCache.loadPromise = loadPromise

    return loadPromise
  }

  /**
   * 批量加载字典数据
   * @param dictCodes 字典编码数组
   * @param force 是否强制重新加载（忽略缓存）
   * @returns 字典数据Map，key为字典编码，value为对应的字典数据列表
   */
  const loadDictDataBatch = async (
    dictCodes: string[],
    force = false
  ): Promise<Record<string, Api.SystemManage.DictDataListItem[]>> => {
    if (dictCodes.length === 0) {
      return {}
    }

    // 检查是否所有字典都已有缓存
    const allCached = dictCodes.every((code) => {
      const cache = dictCache.get(code)
      return cache && !force && cache.data.length > 0
    })

    // 如果全部缓存且不强制刷新，直接从缓存返回
    if (allCached) {
      const result: Record<string, Api.SystemManage.DictDataListItem[]> = {}
      dictCodes.forEach((code) => {
        result[code] = dictCache.get(code)!.data
      })
      return result
    }

    // 批量加载（一个接口请求）
    const response = await fetchGetDictDataList(dictCodes)

    // 更新缓存
    if (response && typeof response === 'object') {
      dictCodes.forEach((code) => {
        const data = response[code]
        if (data && Array.isArray(data)) {
          // 更新或创建缓存
          const cache = dictCache.get(code)
          if (cache) {
            cache.data = data
            cache.timestamp = Date.now()
          } else {
            dictCache.set(code, {
              data: data,
              loading: false,
              loadPromise: null,
              timestamp: Date.now()
            })
          }
        }
      })
    }

    return response || {}
  }

  /**
   * 获取字典数据（如果已缓存则直接返回，否则加载）
   * @param dictCode 字典编码
   * @returns 字典数据列表
   */
  const getDictData = (dictCode: string): Api.SystemManage.DictDataListItem[] => {
    const cache = dictCache.get(dictCode)
    return cache?.data || []
  }

  /**
   * 刷新字典数据
   * @param dictCode 字典编码
   */
  const refreshDictData = async (dictCode: string): Promise<void> => {
    await loadDictData(dictCode, true)
  }

  /**
   * 清空字典缓存
   * @param dictCode 字典编码（可选，不传则清空所有）
   */
  const clearDictCache = (dictCode?: string): void => {
    if (dictCode) {
      dictCache.delete(dictCode)
    } else {
      dictCache.clear()
    }
  }

  /**
   * 获取按钮配置（针对 table_button_config 字典的专用方法）
   * @returns 按钮配置对象
   */
  const getButtonConfigs = (): Record<string, ButtonConfig> => {
    const dictCode = 'table_button_config'
    const data = getDictData(dictCode)

    // 默认配置（兜底）
    const defaultConfigs: Record<string, ButtonConfig> = {
      add: { icon: 'ri:add-fill', text: '新增', class: 'bg-theme/12 text-theme' },
      edit: { icon: 'ri:pencil-line', text: '编辑', class: 'bg-secondary/12 text-secondary' },
      delete: { icon: 'ri:delete-bin-5-line', text: '删除', class: 'bg-error/12 text-error' },
      view: { icon: 'ri:eye-line', text: '查看', class: 'bg-info/12 text-info' },
      more: { icon: 'ri:more-2-fill', text: '更多', class: '' },
      reset: {
        icon: 'ri:shield-keyhole-line',
        text: '重置',
        class: 'bg-secondary/12 text-secondary'
      },
      share: { icon: 'ri:share-line', text: '分配', class: 'bg-info/12 text-info' }
    }

    // 如果还没有加载数据，尝试加载（异步，不等待）
    if (data.length === 0) {
      loadDictData(dictCode).catch(() => {
        // 加载失败时使用默认配置
      })
    }

    // 从字典数据构建配置对象
    const configs: Record<string, ButtonConfig> = {}
    data.forEach((item) => {
      if (item.value && item.status === 1) {
        configs[item.value] = {
          icon: item.remark || '',
          text: item.label || '',
          class: item.cssClass || ''
        }
      }
    })

    // 合并配置，默认配置作为兜底
    return { ...defaultConfigs, ...configs }
  }

  /**
   * 确保按钮配置已加载（如果未加载则加载）
   */
  const ensureButtonConfigsLoaded = async (): Promise<void> => {
    const dictCode = 'table_button_config'
    const cache = dictCache.get(dictCode)
    if (!cache || cache.data.length === 0) {
      await loadDictData(dictCode)
    }
  }

  return {
    dictCache,
    loadDictData,
    loadDictDataBatch,
    getDictData,
    refreshDictData,
    clearDictCache,
    getButtonConfigs,
    ensureButtonConfigsLoaded
  }
})
