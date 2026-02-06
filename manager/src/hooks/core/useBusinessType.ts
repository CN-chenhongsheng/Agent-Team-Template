/**
 * useBusinessType - 业务类型管理
 *
 * 提供统一的业务类型获取和管理功能，数据从系统字典获取。
 * 基于 dict.ts Store 实现，提供业务友好的 API 封装。
 *
 * ## 主要功能
 *
 * 1. 获取业务类型列表 - 从字典Store获取，支持缓存
 * 2. 根据值获取标签 - 辅助方法，用于显示
 * 3. 加载状态管理 - 提供加载状态供组件使用
 *
 * @module useBusinessType
 * @author 陈鸿昇
 */

import { computed, readonly } from 'vue'
import { useDictStore } from '@/store/modules/dict'

/** 业务类型选项 */
export interface BusinessTypeOption {
  label: string
  value: string
}

/** 字典编码 */
const DICT_CODE = 'business_type'

/** 默认值（兜底） */
const DEFAULT_OPTIONS: BusinessTypeOption[] = [
  { label: '入住申请', value: 'check_in' },
  { label: '调宿申请', value: 'transfer' },
  { label: '退宿申请', value: 'check_out' },
  { label: '留宿申请', value: 'stay' }
]

/**
 * 业务类型管理 Hook
 */
export function useBusinessType() {
  const dictStore = useDictStore()

  /** 业务类型选项列表（从 Store 计算得出） */
  const businessTypeOptions = computed<BusinessTypeOption[]>(() => {
    const data = dictStore.getDictData(DICT_CODE)

    // 如果没有数据，返回默认值
    if (!data || data.length === 0) {
      return DEFAULT_OPTIONS
    }

    // 转换为选项格式
    return data.map((item) => ({
      label: item.label,
      value: item.value
    }))
  })

  /** 加载状态（从 Store 获取） */
  const loading = computed(() => {
    const cache = dictStore.dictCache.get(DICT_CODE)
    return cache?.loading ?? false
  })

  /** 是否已加载（从 Store 获取） */
  const isLoaded = computed(() => {
    const cache = dictStore.dictCache.get(DICT_CODE)
    return cache ? cache.data.length > 0 : false
  })

  /**
   * 获取业务类型列表
   * @param forceRefresh 是否强制刷新缓存
   * @returns 业务类型选项列表
   */
  async function fetchBusinessTypes(forceRefresh = false): Promise<BusinessTypeOption[]> {
    try {
      // 从 Store 加载字典数据
      await dictStore.loadDictData(DICT_CODE, forceRefresh)

      // 返回格式化后的选项（computed 会自动更新）
      return businessTypeOptions.value
    } catch (error) {
      console.error('[useBusinessType] 获取业务类型失败:', error)
      // 请求失败时返回默认值（兼容旧数据）
      return DEFAULT_OPTIONS
    }
  }

  /**
   * 根据业务类型值获取标签
   * @param value 业务类型值
   * @returns 业务类型标签，未找到返回原值
   */
  function getBusinessTypeLabel(value: string): string {
    const option = businessTypeOptions.value.find((item) => item.value === value)
    return option?.label || value
  }

  return {
    /** 业务类型选项列表（只读） */
    businessTypeOptions: readonly(businessTypeOptions),
    /** 加载状态（只读） */
    loading: readonly(loading),
    /** 是否已加载（只读） */
    isLoaded: readonly(isLoaded),
    /** 获取业务类型列表 */
    fetchBusinessTypes,
    /** 根据值获取标签 */
    getBusinessTypeLabel
  }
}

export default useBusinessType
