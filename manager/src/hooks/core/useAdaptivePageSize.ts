/**
 * useAdaptivePageSize - 自适应分页尺寸计算
 *
 * 根据表格容器的可用高度和当前表格尺寸配置，自动计算最佳的分页 pageSize。
 * 确保表格数据能够正好填满可视区域，避免出现大量空白或需要滚动。
 *
 * ## 主要功能
 *
 * 1. 动态计算 - 根据容器高度和表格尺寸自动计算最佳 pageSize
 * 2. 响应式更新 - 容器高度或表格尺寸变化时自动重新计算
 * 3. 标准梯度 - 匹配到标准的 pageSize 选项 (10, 20, 30, 50, 100)
 * 4. 智能适配 - 考虑表头、分页器、边距等元素的占用空间
 *
 * ## 使用场景
 *
 * - 需要根据屏幕尺寸自动调整每页显示条数的表格
 * - 希望充分利用可视区域，减少滚动的后台管理页面
 * - 响应式布局中的表格分页优化
 *
 * @module useAdaptivePageSize
 * @author 陈鸿昇
 */

import { ref, computed, watch, onMounted, onUnmounted, type Ref, unref } from 'vue'
import { useResizeObserver } from '@vueuse/core'
import { storeToRefs } from 'pinia'
import { useTableStore } from '@/store/modules/table'
import { TableSizeEnum } from '@/enums/formEnum'

/**
 * 自适应分页尺寸配置接口
 */
interface AdaptivePageSizeOptions {
  /** 容器元素引用或 CSS 选择器 */
  container?: Ref<HTMLElement | undefined> | string
  /** 自定义行高（可选，不提供则根据 tableSize 自动计算） */
  customRowHeight?: number
  /** 最小 pageSize（默认 10） */
  minPageSize?: number
  /** 最大 pageSize（默认 100） */
  maxPageSize?: number
  /** 标准 pageSize 梯度（默认 [10, 20, 30, 50, 100]） */
  pageSizeOptions?: number[]
  /** 是否启用调试日志 */
  debug?: boolean
}

/**
 * 表格行高映射（根据 Element Plus 表格实际渲染的行高）
 */
const ROW_HEIGHT_MAP: Record<TableSizeEnum, number> = {
  [TableSizeEnum.SMALL]: 40,
  [TableSizeEnum.DEFAULT]: 48,
  [TableSizeEnum.LARGE]: 56
}

/**
 * 高度计算常量
 */
const HEIGHT_CONSTANTS = {
  /** 表头高度 */
  TABLE_HEADER: 48,
  /** 分页器高度 */
  PAGINATION: 40,
  /** 边距预留 */
  MARGIN: 20
}

/**
 * 自适应分页尺寸计算器类
 */
class AdaptivePageSizeCalculator {
  private containerHeight: Ref<number>
  private rowHeight: Ref<number>
  private options: Required<Omit<AdaptivePageSizeOptions, 'container' | 'customRowHeight'>>

  constructor(
    containerHeight: Ref<number>,
    rowHeight: Ref<number>,
    options: Required<Omit<AdaptivePageSizeOptions, 'container' | 'customRowHeight'>>
  ) {
    this.containerHeight = containerHeight
    this.rowHeight = rowHeight
    this.options = options
  }

  /**
   * 计算可用行数
   */
  private calculateAvailableRows(): number {
    const availableHeight =
      this.containerHeight.value -
      HEIGHT_CONSTANTS.TABLE_HEADER -
      HEIGHT_CONSTANTS.PAGINATION -
      HEIGHT_CONSTANTS.MARGIN

    if (availableHeight <= 0 || this.rowHeight.value <= 0) {
      return 0 // Return 0 rows (not a pageSize value)
    }

    const rows = Math.floor(availableHeight / this.rowHeight.value)

    if (this.options.debug) {
      console.log('[useAdaptivePageSize] 计算详情:', {
        容器高度: this.containerHeight.value,
        可用高度: availableHeight,
        行高: this.rowHeight.value,
        可用行数: rows
      })
    }

    return rows
  }

  /**
   * 匹配到最佳的标准 pageSize
   * 策略：在标准 pageSize 列表中选择「最接近可见行数」的值
   * - 避免在接近临界值时过早跳到更大的档位（例如 23 行直接跳到 30）
   * - 可以让中等屏幕更倾向于 20 条/页，大屏自然匹配到 30/50/100
   */
  calculate(): number {
    const availableRows = this.calculateAvailableRows()

    // 如果容器高度无效（返回 0），返回 0 交由上层决定兜底策略
    // 上层 useTable 中会忽略 0（newSize > 0 才会生效），从而保留自己的 initialPageSize（默认 20），
    // 等容器高度计算完成后再次触发，这样大屏可以得到 30/50 小屏可以得到 20，
    // 同时避免首屏被错误地强制成 minPageSize(10)。
    if (availableRows === 0) {
      return 0
    }

    // 新策略：选择与 availableRows 距离最近的标准 pageSize
    // 例如 availableRows=23，则更倾向于 20（差值 3）而不是 30（差值 7）
    let matchedSize = this.options.minPageSize
    let minDiff = Number.POSITIVE_INFINITY

    for (const size of this.options.pageSizeOptions) {
      const diff = Math.abs(size - availableRows)
      if (diff < minDiff) {
        minDiff = diff
        matchedSize = size
      } else if (diff === minDiff && size > matchedSize) {
        // 差值相同则取更大的一个，避免过小的 pageSize
        matchedSize = size
      }
    }

    // 确保不超过最大值（如果 maxPageSize 未定义，则不限制）
    const finalSize = Math.min(matchedSize, this.options.maxPageSize || Infinity)

    if (this.options.debug) {
      console.log('[useAdaptivePageSize] 匹配结果:', {
        可用行数: availableRows,
        匹配值: matchedSize,
        最终值: finalSize,
        说明: matchedSize >= availableRows ? '有滚动（铺满）' : '无滚动（填充）'
      })
    }

    return finalSize
  }
}

/**
 * 自适应分页尺寸 Hook
 *
 * @param options 配置选项
 * @returns 分页尺寸相关数据和方法
 *
 * @example
 * ```typescript
 * // 方式一：传入容器引用
 * const containerRef = ref<HTMLElement>()
 * const { pageSize, pageSizes } = useAdaptivePageSize({ container: containerRef })
 *
 * // 方式二：传入 CSS 选择器
 * const { pageSize, pageSizes } = useAdaptivePageSize({ container: '.el-card__body' })
 *
 * // 方式三：自定义配置
 * const { pageSize, pageSizes } = useAdaptivePageSize({
 *   container: containerRef,
 *   customRowHeight: 50,
 *   minPageSize: 10,
 *   maxPageSize: 200,
 *   debug: true
 * })
 * ```
 */
export function useAdaptivePageSize(options: AdaptivePageSizeOptions = {}) {
  const tableStore = useTableStore()
  const { tableSize } = storeToRefs(tableStore)

  // 默认配置
  const defaultOptions = {
    minPageSize: 10,
    maxPageSize: 100,
    pageSizeOptions: [10, 20, 30, 50, 100],
    debug: false
  }

  // 过滤掉 undefined 值，避免覆盖默认值
  const filteredOptions = Object.fromEntries(
    Object.entries(options).filter(([, v]) => v !== undefined)
  )
  const mergedOptions = { ...defaultOptions, ...filteredOptions }

  // 容器高度
  const containerHeight = ref(0)
  // 容器元素引用
  const containerElement = ref<HTMLElement>()

  // 计算行高
  const rowHeight = computed(() => {
    if (options.customRowHeight) {
      return options.customRowHeight
    }
    return ROW_HEIGHT_MAP[tableSize.value] || ROW_HEIGHT_MAP[TableSizeEnum.DEFAULT]
  })

  // 计算 pageSize
  const pageSize = computed(() => {
    const calculator = new AdaptivePageSizeCalculator(containerHeight, rowHeight, mergedOptions)
    return calculator.calculate()
  })

  // 动态生成 pageSizes 数组（基于计算出的 pageSize）
  const pageSizes = computed(() => {
    const sizes = mergedOptions.pageSizeOptions.filter(
      (size) => size >= mergedOptions.minPageSize && size <= mergedOptions.maxPageSize
    )
    // 确保当前 pageSize 在选项中
    if (!sizes.includes(pageSize.value)) {
      sizes.push(pageSize.value)
      sizes.sort((a, b) => a - b)
    }
    return sizes
  })

  /**
   * 查找容器元素
   */
  const findContainer = () => {
    if (!options.container) {
      // 如果没有传入容器，尝试自动查找
      const defaultContainer = document.querySelector('.el-card__body') as HTMLElement
      if (defaultContainer) {
        containerElement.value = defaultContainer
      }
      return
    }

    if (typeof options.container === 'string') {
      // CSS 选择器
      const element = document.querySelector(options.container) as HTMLElement
      if (element) {
        containerElement.value = element
      } else if (mergedOptions.debug) {
        console.warn(`[useAdaptivePageSize] 未找到容器元素: ${options.container}`)
      }
    } else {
      // Ref
      const element = unref(options.container)
      if (element) {
        containerElement.value = element
      }
    }
  }

  /**
   * 更新容器高度
   */
  const updateContainerHeight = () => {
    if (containerElement.value) {
      containerHeight.value = containerElement.value.clientHeight
      if (mergedOptions.debug) {
        console.log('[useAdaptivePageSize] 容器高度更新:', containerHeight.value)
      }
    }
  }

  // 监听容器元素变化
  let stopResizeObserver: (() => void) | null = null

  watch(
    containerElement,
    (newElement) => {
      // 停止旧的观察器
      if (stopResizeObserver) {
        stopResizeObserver()
        stopResizeObserver = null
      }

      if (newElement) {
        // 立即更新一次高度
        updateContainerHeight()

        // 使用 ResizeObserver 监听高度变化
        stopResizeObserver = useResizeObserver(newElement, () => {
          updateContainerHeight()
        }).stop
      }
    },
    { immediate: true }
  )

  // 如果传入的是 Ref，监听其变化
  if (options.container && typeof options.container !== 'string') {
    watch(
      options.container,
      () => {
        findContainer()
      },
      { immediate: false }
    )
  }

  // 组件挂载时查找容器
  onMounted(() => {
    findContainer()
  })

  // 组件卸载时清理
  onUnmounted(() => {
    if (stopResizeObserver) {
      stopResizeObserver()
    }
  })

  return {
    /** 计算出的最佳 pageSize */
    pageSize,
    /** 可用的 pageSizes 选项数组 */
    pageSizes,
    /** 容器高度（用于调试） */
    containerHeight,
    /** 当前行高（用于调试） */
    rowHeight,
    /** 手动刷新容器高度 */
    refresh: updateContainerHeight
  }
}
