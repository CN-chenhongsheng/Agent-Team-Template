/**
 * 可视化视图逻辑 Composable
 * @description 管理宿舍可视化视图的状态和数据加载
 * @author 陈鸿昇
 */

import { ref, computed } from 'vue'
import type { RoomWithBeds, ViewType, VisualStats, BuildingOption } from '../types'
import { RoomStatus } from '../types'

/**
 * 可视化视图 Hook
 */
export function useVisualView() {
  // ==================== 状态定义 ====================

  /** 当前选中的楼层ID */
  const currentFloorId = ref<number | null>(null)

  /** 当前视图类型 */
  const currentView = ref<ViewType>('visual')

  /** 房间列表数据 */
  const rooms = ref<RoomWithBeds[]>([])

  /** 加载状态 */
  const loading = ref(false)

  /** 楼栋选项 */
  const buildingOptions = ref<BuildingOption[]>([])

  // ==================== 计算属性 ====================

  /**
   * 统计数据
   */
  const stats = computed<VisualStats>(() => {
    const total = rooms.value.length

    // 空闲房间：入住人数为0
    const available = rooms.value.filter(
      (r) => r.roomStatus !== RoomStatus.MAINTENANCE && r.currentOccupancy === 0
    ).length

    // 满员房间：入住人数等于床位数
    const full = rooms.value.filter(
      (r) => r.roomStatus !== RoomStatus.MAINTENANCE && r.currentOccupancy >= r.bedCount
    ).length

    // 维修中房间
    const maintenance = rooms.value.filter((r) => r.roomStatus === RoomStatus.MAINTENANCE).length

    // 部分入住：排除空闲、满员、维修中
    const partial = total - available - full - maintenance

    // 计算入住率
    const totalBeds = rooms.value.reduce((sum, r) => sum + r.bedCount, 0)
    const occupiedBeds = rooms.value.reduce((sum, r) => sum + r.currentOccupancy, 0)
    const occupancyRate = totalBeds > 0 ? Math.round((occupiedBeds / totalBeds) * 100) : 0

    return {
      total,
      available,
      partial,
      full,
      maintenance,
      occupancyRate
    }
  })

  /**
   * 是否为表格视图
   */
  const isTableView = computed(() => currentView.value === 'table')

  /**
   * 是否为可视化视图
   */
  const isVisualView = computed(() => currentView.value === 'visual')

  /**
   * 是否有数据
   */
  const hasData = computed(() => rooms.value.length > 0)

  /**
   * 当前选中的楼栋名称
   */
  const currentFloorName = computed(() => {
    if (!currentFloorId.value) return ''

    for (const building of buildingOptions.value) {
      for (const floor of building.floors) {
        if (floor.id === currentFloorId.value) {
          return `${building.name} - ${floor.name}`
        }
      }
    }
    return ''
  })

  // ==================== 方法 ====================

  /**
   * 设置房间数据
   * @param data 房间列表
   */
  const setRooms = (data: RoomWithBeds[]) => {
    rooms.value = data || []
  }

  /**
   * 设置楼栋选项
   * @param options 楼栋选项列表
   */
  const setBuildingOptions = (options: BuildingOption[]) => {
    buildingOptions.value = options || []
  }

  /**
   * 设置当前楼层
   * @param floorId 楼层ID
   */
  const setCurrentFloor = (floorId: number | null) => {
    currentFloorId.value = floorId
  }

  /**
   * 切换视图
   * @param view 视图类型
   */
  const switchView = (view: ViewType) => {
    currentView.value = view
  }

  /**
   * 设置加载状态
   * @param status 加载状态
   */
  const setLoading = (status: boolean) => {
    loading.value = status
  }

  /**
   * 清空数据
   */
  const clearData = () => {
    rooms.value = []
    currentFloorId.value = null
  }

  /**
   * 刷新数据
   * @param fetchFn 获取数据的函数
   */
  const refresh = async (fetchFn: (floorId: number) => Promise<RoomWithBeds[]>) => {
    if (!currentFloorId.value) return

    loading.value = true
    try {
      const data = await fetchFn(currentFloorId.value)
      rooms.value = data || []
    } finally {
      loading.value = false
    }
  }

  /**
   * 加载楼层数据
   * @param floorId 楼层ID
   * @param fetchFn 获取数据的函数
   */
  const loadFloorData = async (
    floorId: number,
    fetchFn: (floorId: number) => Promise<RoomWithBeds[]>
  ) => {
    currentFloorId.value = floorId
    loading.value = true
    try {
      const data = await fetchFn(floorId)
      rooms.value = data || []
    } finally {
      loading.value = false
    }
  }

  return {
    // 状态
    currentFloorId,
    currentView,
    rooms,
    loading,
    buildingOptions,

    // 计算属性
    stats,
    isTableView,
    isVisualView,
    hasData,
    currentFloorName,

    // 方法
    setRooms,
    setBuildingOptions,
    setCurrentFloor,
    switchView,
    setLoading,
    clearData,
    refresh,
    loadFloorData
  }
}
