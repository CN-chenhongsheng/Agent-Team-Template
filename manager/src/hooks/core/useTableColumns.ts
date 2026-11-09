/**
 * useTableColumns - 表格列配置管理
 *
 * 提供动态的表格列配置管理能力，支持运行时灵活控制列的显示、隐藏、排序等操作。
 * 通常与 useTable 配合使用，为表格提供完整的列管理功能。
 *
 * ## 主要功能
 *
 * 1. 列显示控制 - 动态显示/隐藏列，支持批量操作
 * 2. 列排序 - 拖拽或编程方式重新排列列顺序
 * 3. 列配置管理 - 新增、删除、更新列配置
 * 4. 特殊列支持 - 自动处理 selection、expand、index 等特殊列
 * 5. 状态持久化 - 保持列的显示状态，支持重置到初始状态
 *
 * ## 使用示例
 *
 * ```typescript
 * const { columns, columnChecks, toggleColumn, reorderColumns } = useTableColumns(() => [
 *   { prop: 'name', label: '姓名', visible: true },
 *   { prop: 'email', label: '邮箱', visible: true },
 *   { prop: 'status', label: '状态', visible: false }
 * ])
 *
 * // 切换列显示
 * toggleColumn('email', false)
 *
 * // 重新排序
 * reorderColumns(0, 2)
 * ```
 *
 * @module useTableColumns
 * @author 陈鸿昇
 */

import { ref, computed, watch, h } from 'vue'
import { useRoute } from 'vue-router'
import { $t } from '@/locales'
import { ElTooltip } from 'element-plus'
import type { ColumnOption } from '@/types/component'
import { isActionButtonConfig, renderActionButtons } from '@/utils/table/actionButtons'

/**
 * 特殊列类型
 */
const SPECIAL_COLUMNS: Record<string, { prop: string; label: string }> = {
  selection: { prop: '__selection__', label: $t('table.column.selection') },
  expand: { prop: '__expand__', label: $t('table.column.expand') },
  index: { prop: '__index__', label: $t('table.column.index') }
}

/**
 * 持久化的列状态（顺序 + 可见性 + 对齐）
 */
export interface PersistedColumnState {
  key: string
  visible: boolean
  align?: 'left' | 'center' | 'right'
}

const ALIGN_VALUES = ['left', 'center', 'right'] as const

/**
 * 获取列的唯一标识
 */
export const getColumnKey = <T>(col: ColumnOption<T>) =>
  SPECIAL_COLUMNS[col.type as keyof typeof SPECIAL_COLUMNS]?.prop ?? (col.prop as string)

/**
 * 获取列的显示状态
 * 优先使用 visible 字段，如果不存在则使用 checked 字段
 */
export const getColumnVisibility = <T>(col: ColumnOption<T>): boolean => {
  // visible 优先级高于 checked
  if (col.visible !== undefined) {
    return col.visible
  }
  // 如果 visible 未定义，使用 checked，默认为 true
  return col.checked ?? true
}

/**
 * 获取列的检查状态
 */
export const getColumnChecks = <T>(columns: ColumnOption<T>[]) =>
  columns.map((col) => {
    const special = col.type && SPECIAL_COLUMNS[col.type]
    const visibility = getColumnVisibility(col)

    if (special) {
      return { ...col, prop: special.prop, label: special.label, checked: true, visible: true }
    }
    return { ...col, checked: visibility, visible: visibility }
  })

/**
 * 按 key → 序号 映射排序：已知 key 按序号排列，新 key 保持相对顺序追加到末尾
 */
const sortColumnsByKeyOrder = <T>(
  columns: ColumnOption<T>[],
  orderMap: Map<string, number>
): ColumnOption<T>[] => [
  ...columns
    .filter((c) => orderMap.has(getColumnKey(c)))
    .sort((a, b) => orderMap.get(getColumnKey(a))! - orderMap.get(getColumnKey(b))!),
  ...columns.filter((c) => !orderMap.has(getColumnKey(c)))
]

/**
 * 动态列配置接口
 */
export interface DynamicColumnConfig<T = any> {
  /**
   * 新增列（支持单个或批量）
   * @param column 列配置或列配置数组
   * @param index 可选的插入位置，默认末尾（批量时为第一个列的位置）
   */
  addColumn: (column: ColumnOption<T> | ColumnOption<T>[], index?: number) => void
  /**
   * 删除列（支持单个或批量）
   * @param prop 列的唯一标识或标识数组
   */
  removeColumn: (prop: string | string[]) => void
  /**
   * 切换列显示状态（支持单个或批量）
   * @param prop 列的唯一标识或标识数组
   * @param visible 可选的显示状态，默认取反
   */
  toggleColumn: (prop: string | string[], visible?: boolean) => void

  /**
   * 更新列（支持单个或批量）
   * @param prop 列的唯一标识或更新配置数组
   * @param updates 列配置更新（当 prop 为字符串时使用）
   */
  updateColumn: (
    prop: string | Array<{ prop: string; updates: Partial<ColumnOption<T>> }>,
    updates?: Partial<ColumnOption<T>>
  ) => void
  /**
   * 批量更新列（兼容旧版本，推荐使用 updateColumn 的数组模式）
   * @param updates 列更新配置
   * @deprecated 推荐使用 updateColumn 的数组模式
   */
  batchUpdateColumns: (updates: Array<{ prop: string; updates: Partial<ColumnOption<T>> }>) => void
  /**
   * 重新排序列
   * @param fromIndex 源索引
   * @param toIndex 目标索引
   */
  reorderColumns: (fromIndex: number, toIndex: number) => void
  /**
   * 获取列配置
   * @param prop 列的唯一标识
   * @returns 列配置
   */
  getColumnConfig: (prop: string) => ColumnOption<T> | undefined
  /**
   * 获取所有列配置
   * @returns 所有列配置
   */
  getAllColumns: () => ColumnOption<T>[]
  /**
   * 重置所有列
   */
  resetColumns: () => void
}

export function useTableColumns<T = any>(
  columnsFactory: () => ColumnOption<T>[]
): {
  columns: any
  columnChecks: any
} & DynamicColumnConfig<T> {
  const dynamicColumns = ref<ColumnOption<T>[]>(columnsFactory())
  const columnChecks = ref<ColumnOption<T>[]>(getColumnChecks(dynamicColumns.value))

  // —— 列布局持久化：顺序 + 可见性 + 对齐（按路由路径隔离）——
  const route = useRoute()
  const getTableColumnsStorageKey = (): string | null => {
    try {
      return route.path ? `table-columns:${route.path}` : null
    } catch {
      return null
    }
  }

  const loadPersistedColumnState = (): PersistedColumnState[] => {
    if (typeof window === 'undefined') return []
    const key = getTableColumnsStorageKey()
    if (!key) return []
    try {
      const raw = window.localStorage.getItem(key)
      if (!raw) return []
      const parsed = JSON.parse(raw)
      if (!Array.isArray(parsed)) return []
      return parsed.filter(
        (p): p is PersistedColumnState =>
          !!p && typeof p.key === 'string' && typeof p.visible === 'boolean'
      )
    } catch (e) {
      console.warn('[TableColumns] Failed to load persisted column state:', e)
      return []
    }
  }

  const applyPersistedColumnState = (states: PersistedColumnState[]): void => {
    if (!states.length) return
    const stateMap = new Map(states.map((s) => [s.key, s]))
    const orderMap = new Map(states.map((s, i) => [s.key, i]))
    const sorted = sortColumnsByKeyOrder(columnChecks.value, orderMap)
    columnChecks.value = sorted.map((c) => {
      const state = stateMap.get(getColumnKey(c))
      if (!state) return c
      return {
        ...c,
        visible: state.visible,
        checked: state.visible,
        ...(state.align && ALIGN_VALUES.includes(state.align) ? { align: state.align } : {})
      }
    })
  }

  applyPersistedColumnState(loadPersistedColumnState())

  // 持久化保存：顺序 + 可见性 + 对齐（deep 监听，拖拽/勾选/对齐三种变更均可捕获）
  watch(
    columnChecks,
    (checks) => {
      const key = getTableColumnsStorageKey()
      if (!key) return
      try {
        // 与工厂默认值相同的 align 视为"未设置"，只持久化用户显式改过的值
        const factoryAlignMap = new Map(dynamicColumns.value.map((c) => [getColumnKey(c), c.align]))
        const payload: PersistedColumnState[] = checks.map((c) => {
          const colKey = getColumnKey(c)
          const aligned = c.align && c.align !== factoryAlignMap.get(colKey)
          return {
            key: colKey,
            visible: getColumnVisibility(c),
            ...(aligned ? { align: c.align } : {})
          }
        })
        window.localStorage.setItem(key, JSON.stringify(payload))
      } catch (e) {
        console.warn('[TableColumns] Failed to save column state:', e)
      }
    },
    { deep: true }
  )

  // 当 dynamicColumns 变动时，重新生成 columnChecks 且保留用户布局（顺序、显示状态、对齐）
  watch(
    dynamicColumns,
    (newCols) => {
      const prevState = new Map(
        columnChecks.value.map((c) => [
          getColumnKey(c),
          { visibility: getColumnVisibility(c), align: c.align }
        ])
      )
      const prevIndex = new Map(columnChecks.value.map((c, i) => [getColumnKey(c), i]))
      const sorted = sortColumnsByKeyOrder(getColumnChecks(newCols), prevIndex)
      columnChecks.value = sorted.map((c) => {
        const key = getColumnKey(c)
        const prev = prevState.get(key)
        const visibility = prev ? prev.visibility : getColumnVisibility(c)
        // 检查项中的 align 优先于 dynamicColumns 中传入的 align
        const align = prev?.align !== undefined ? prev.align : c.align
        return {
          ...c,
          checked: visibility,
          visible: visibility,
          ...(align !== undefined ? { align } : {})
        }
      })
    },
    { deep: true }
  )

  // 当前显示列（基于 columnChecks 的 checked 或 visible）
  const columns = computed(() => {
    const colMap = new Map(dynamicColumns.value.map((c) => [getColumnKey(c), c]))
    const visibleColumns = columnChecks.value
      .filter((c) => getColumnVisibility(c))
      .map((c) => {
        const base = colMap.get(getColumnKey(c))
        if (!base) return undefined
        // 仅合并用户对齐设置；undefined 视为默认左对齐，不显式写出
        return c.align ? { ...base, align: c.align } : base
      })
      .filter(Boolean) as ColumnOption<T>[]

    // 自动处理操作列的 formatter 和表头提示
    return visibleColumns.map((col) => {
      const processedCol = { ...col }

      // 1. 处理表头提示 (labelTooltip)
      if (processedCol.labelTooltip) {
        const originalLabel = processedCol.label
        processedCol.renderHeader = () => {
          return h('div', { class: 'flex items-center justify-center gap-1' }, [
            h('span', originalLabel),
            h(
              ElTooltip,
              {
                content: processedCol.labelTooltip,
                placement: 'top'
              },
              {
                default: () =>
                  h('i', { class: 'i-ri:question-line text-gray-400 cursor-help text-sm' })
              }
            )
          ])
        }
      }

      // 2. 处理操作列
      if (processedCol.prop === 'action' && processedCol.formatter) {
        const originalFormatter = processedCol.formatter
        processedCol.formatter = (row: T) => {
          const result = originalFormatter(row)
          // 如果 formatter 返回的是按钮配置数组，自动渲染
          if (isActionButtonConfig(result)) {
            return renderActionButtons(result)
          }
          // 否则返回原始结果
          return result
        }
      }
      return processedCol
    })
  })

  // 支持 updater 返回新数组或直接在传入数组上 mutate
  const setDynamicColumns = (updater: (cols: ColumnOption<T>[]) => void | ColumnOption<T>[]) => {
    const copy = [...dynamicColumns.value]
    const result = updater(copy)
    dynamicColumns.value = Array.isArray(result) ? result : copy
  }

  return {
    columns,
    columnChecks,

    /**
     * 新增列（支持单个或批量）
     */
    addColumn: (column: ColumnOption<T> | ColumnOption<T>[], index?: number) => {
      const columnsToAdd = Array.isArray(column) ? column : [column]
      const resolveIndex = (length: number) =>
        typeof index === 'number' && index >= 0 && index <= length ? index : length
      setDynamicColumns((cols) => {
        const next = [...cols]
        next.splice(resolveIndex(next.length), 0, ...columnsToAdd)
        return next
      })
      // 同步插入检查项，使显示位置与插入位置一致（显示顺序由 columnChecks 决定）
      const nextChecks = [...columnChecks.value]
      nextChecks.splice(resolveIndex(nextChecks.length), 0, ...getColumnChecks(columnsToAdd))
      columnChecks.value = nextChecks
    },

    /**
     * 删除列（支持单个或批量）
     */
    removeColumn: (prop: string | string[]) =>
      setDynamicColumns((cols) => {
        const propsToRemove = Array.isArray(prop) ? prop : [prop]
        return cols.filter((c) => !propsToRemove.includes(getColumnKey(c)))
      }),

    /**
     * 更新列（支持单个或批量）
     */
    updateColumn: (
      prop: string | Array<{ prop: string; updates: Partial<ColumnOption<T>> }>,
      updates?: Partial<ColumnOption<T>>
    ) => {
      // 批量模式：prop 是数组
      if (Array.isArray(prop)) {
        setDynamicColumns((cols) => {
          const map = new Map(prop.map((u) => [u.prop, u.updates]))
          return cols.map((c) => {
            const key = getColumnKey(c)
            const upd = map.get(key)
            return upd ? { ...c, ...upd } : c
          })
        })
        // 同步 align 到检查项，保证运行时更新不被重建逻辑用旧值覆盖
        const alignMap = new Map(
          prop.filter((u) => u.updates.align !== undefined).map((u) => [u.prop, u.updates.align])
        )
        if (alignMap.size) {
          columnChecks.value = columnChecks.value.map((c) => {
            const nextAlign = alignMap.get(getColumnKey(c))
            return nextAlign !== undefined ? { ...c, align: nextAlign } : c
          })
        }
      }
      // 单个模式：prop 是字符串
      else if (updates) {
        setDynamicColumns((cols) =>
          cols.map((c) => (getColumnKey(c) === prop ? { ...c, ...updates } : c))
        )
        if (updates.align !== undefined) {
          columnChecks.value = columnChecks.value.map((c) =>
            getColumnKey(c) === prop ? { ...c, align: updates.align } : c
          )
        }
      }
    },

    /**
     * 切换列显示状态（支持单个或批量）
     */
    toggleColumn: (prop: string | string[], visible?: boolean) => {
      const propsToToggle = Array.isArray(prop) ? prop : [prop]
      const next = [...columnChecks.value]

      propsToToggle.forEach((p) => {
        const i = next.findIndex((c) => getColumnKey(c) === p)
        if (i > -1) {
          const currentVisibility = getColumnVisibility(next[i])
          const newVisibility = visible ?? !currentVisibility
          // 同时更新 checked 和 visible 以保持兼容性
          next[i] = { ...next[i], checked: newVisibility, visible: newVisibility }
        }
      })

      columnChecks.value = next
    },

    /**
     * 重置所有列（恢复工厂顺序、可见性与对齐，并清除本页持久化状态）
     */
    resetColumns: () => {
      const fresh = columnsFactory()
      dynamicColumns.value = fresh
      // 同步复原检查项，避免重建逻辑把旧布局重新套用回来
      columnChecks.value = getColumnChecks(fresh)
      const storageKey = getTableColumnsStorageKey()
      if (storageKey) {
        try {
          window.localStorage.removeItem(storageKey)
        } catch (e) {
          console.warn('[TableColumns] Failed to clear persisted column state:', e)
        }
      }
    },

    /**
     * 批量更新列（兼容旧版本）
     * @deprecated 推荐使用 updateColumn 的数组模式
     */
    batchUpdateColumns: (updates) =>
      setDynamicColumns((cols) => {
        const map = new Map(updates.map((u) => [u.prop, u.updates]))
        return cols.map((c) => {
          const key = getColumnKey(c)
          const upd = map.get(key)
          return upd ? { ...c, ...upd } : c
        })
      }),

    /**
     * 重新排序列
     */
    reorderColumns: (fromIndex: number, toIndex: number) => {
      const reorder = (list: ColumnOption<T>[]): ColumnOption<T>[] => {
        if (
          fromIndex < 0 ||
          fromIndex >= list.length ||
          toIndex < 0 ||
          toIndex >= list.length ||
          fromIndex === toIndex
        ) {
          return list
        }
        const next = [...list]
        const [moved] = next.splice(fromIndex, 1)
        next.splice(toIndex, 0, moved)
        return next
      }
      setDynamicColumns(reorder)
      // 显示顺序由 columnChecks 决定，需同步重排
      columnChecks.value = reorder(columnChecks.value)
    },

    /**
     * 获取列配置
     */
    getColumnConfig: (prop: string) => dynamicColumns.value.find((c) => getColumnKey(c) === prop),

    /**
     * 获取所有列配置
     */
    getAllColumns: () => [...dynamicColumns.value]
  }
}
