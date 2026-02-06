<!--
  DormVisualView 宿舍可视化视图主组件
  @description 房间和床位的可视化平面图展示，支持视图切换
  @author 陈鸿昇
-->
<template>
  <div class="dorm-visual-view">
    <!-- 固定头部区域 -->
    <div class="dorm-visual-header">
      <!-- 顶部工具栏（可选） -->
      <div v-if="showToolbar" class="flex-cb flex-wrap gap-4 mb-4">
        <!-- 左侧：楼栋楼层选择 -->
        <div v-if="showFloorSelector" class="flex-c gap-3">
          <FloorSelector
            v-model="currentFloorId"
            :options="buildingOptions"
            @change="handleFloorChange"
          />
          <span v-if="currentFloorName" class="text-sm text-g-600">
            {{ currentFloorName }}
          </span>
        </div>

        <!-- 右侧：视图切换 -->
        <ViewSwitcher
          v-if="showViewSwitcher"
          v-model="currentView"
          @update:model-value="handleViewChange"
        />
      </div>

      <!-- 统计信息条（环形图 + 标签 + 图例） -->
      <div
        v-if="showStats && hasData"
        class="stats-bar flex flex-col gap-3 px-4 py-3 rounded-lg bg-g-50 dark:bg-g-200/50"
      >
        <!-- 上方：统计数据 -->
        <div class="flex-c gap-6">
          <!-- 左侧：环形进度图 -->
          <div class="stats-ring flex-c gap-3">
            <ElProgress
              type="circle"
              :percentage="stats.occupancyRate"
              :width="56"
              :stroke-width="5"
              :color="occupancyRateColor"
            >
              <template #default>
                <span class="text-sm font-semibold">{{ stats.occupancyRate }}%</span>
              </template>
            </ElProgress>
            <div class="flex flex-col">
              <span class="text-xs text-g-500">入住率</span>
              <span class="text-sm font-medium text-g-700"
                >{{ stats.occupiedBeds }}/{{ stats.totalBeds }} 床</span
              >
            </div>
          </div>

          <!-- 分隔线 -->
          <div class="h-10 w-px bg-g-200 dark:bg-g-300"></div>

          <!-- 中间：房间状态标签 -->
          <div class="stats-tags flex-c flex-wrap gap-3">
            <div class="stat-tag stat-tag--total">
              <span class="stat-tag__value">{{ stats.total }}</span>
              <span class="stat-tag__label">总房间</span>
            </div>
            <div class="stat-tag stat-tag--success">
              <span class="stat-tag__dot"></span>
              <span class="stat-tag__value">{{ stats.available }}</span>
              <span class="stat-tag__label">空闲</span>
            </div>
            <div class="stat-tag stat-tag--primary">
              <span class="stat-tag__dot"></span>
              <span class="stat-tag__value">{{ stats.partial }}</span>
              <span class="stat-tag__label">部分入住</span>
            </div>
            <div class="stat-tag stat-tag--info">
              <span class="stat-tag__dot"></span>
              <span class="stat-tag__value">{{ stats.full }}</span>
              <span class="stat-tag__label">满员</span>
            </div>
            <div v-if="stats.maintenance > 0" class="stat-tag stat-tag--warning">
              <span class="stat-tag__dot"></span>
              <span class="stat-tag__value">{{ stats.maintenance }}</span>
              <span class="stat-tag__label">维修中</span>
            </div>
          </div>
        </div>

        <!-- 下方：床位状态图例 -->
        <div class="stats-legend flex-c gap-5 pt-2 border-t border-g-200 dark:border-g-300">
          <span class="text-xs text-g-400">床位状态：</span>
          <div class="flex-c gap-4 text-xs text-g-600">
            <span class="flex-c gap-1.5">
              <span class="size-2.5 rounded-sm bg-g-300 dark:bg-g-400"></span>
              <span>空闲</span>
            </span>
            <span class="flex-c gap-1.5">
              <span class="size-2.5 rounded-sm bg-primary"></span>
              <span>已入住</span>
            </span>
            <span class="flex-c gap-1.5">
              <span class="size-2.5 rounded-sm bg-warning"></span>
              <span>维修中</span>
            </span>
            <span class="flex-c gap-1.5">
              <span class="size-2.5 rounded-sm bg-secondary"></span>
              <span>已预订</span>
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 可滚动内容区域 -->
    <div class="dorm-visual-content">
      <!-- 可视化视图 -->
      <div v-show="isVisualView" class="h-full">
        <RoomGrid
          :rooms="rooms"
          :loading="loading"
          @room-click="handleRoomClick"
          @bed-click="handleBedClick"
          @empty-bed-click="handleEmptyBedClick"
        />
      </div>

      <!-- 表格视图插槽 -->
      <div v-show="isTableView">
        <slot name="table">
          <div class="py-8 text-center text-g-500"> 请在此处放置表格组件 </div>
        </slot>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, computed, watch, onMounted } from 'vue'
  import { ElProgress } from 'element-plus'
  import FloorSelector from './components/FloorSelector.vue'
  import ViewSwitcher from './components/ViewSwitcher.vue'
  import RoomGrid from './components/RoomGrid.vue'
  import type {
    ViewType,
    RoomWithBeds,
    BedWithStudent,
    BuildingOption,
    FloorSelectValue
  } from './types'
  import { useVisualView } from './composables/useVisualView'

  defineOptions({ name: 'DormVisualView' })

  // ==================== Props ====================
  interface Props {
    /** 楼栋选项列表 */
    buildingOptions?: BuildingOption[]
    /** 房间数据（外部传入） */
    rooms?: RoomWithBeds[]
    /** 加载状态（外部控制） */
    loading?: boolean
    /** 默认楼层ID */
    defaultFloorId?: number
    /** 是否显示统计卡片 */
    showStats?: boolean
    /** 是否显示视图切换 */
    showViewSwitcher?: boolean
    /** 是否显示楼层选择器 */
    showFloorSelector?: boolean
    /** 默认视图 */
    defaultView?: ViewType
  }

  const props = withDefaults(defineProps<Props>(), {
    buildingOptions: () => [],
    rooms: () => [],
    loading: false,
    showStats: true,
    showViewSwitcher: true,
    showFloorSelector: true,
    defaultView: 'visual'
  })

  // ==================== Computed ====================

  /** 是否显示顶部工具栏 */
  const showToolbar = computed(() => props.showFloorSelector || props.showViewSwitcher)

  // ==================== Emits ====================
  const emit = defineEmits<{
    (e: 'room-click', room: RoomWithBeds): void
    (e: 'bed-click', bed: BedWithStudent, room: RoomWithBeds): void
    (e: 'empty-bed-click', bed: BedWithStudent, room: RoomWithBeds): void
    (e: 'view-change', view: ViewType): void
    (e: 'floor-change', value: FloorSelectValue): void
  }>()

  // ==================== Composables ====================
  const visualView = useVisualView()

  // ==================== State ====================

  /** 当前视图 */
  const currentView = ref<ViewType>(props.defaultView)

  /** 当前楼层ID（支持多种选择格式） */
  const currentFloorId = ref<FloorSelectValue>(props.defaultFloorId || null)

  // ==================== Computed ====================

  /** 是否为表格视图 */
  const isTableView = computed(() => currentView.value === 'table')

  /** 是否为可视化视图 */
  const isVisualView = computed(() => currentView.value === 'visual')

  /** 房间数据（优先使用 props，否则使用 composable） */
  const rooms = computed(() => props.rooms || visualView.rooms.value)

  /** 加载状态 */
  const loading = computed(() => props.loading || visualView.loading.value)

  /** 是否有数据 */
  const hasData = computed(() => rooms.value.length > 0)

  /** 统计数据 */
  const stats = computed(() => {
    const roomList = rooms.value
    const total = roomList.length

    // 空闲房间
    const available = roomList.filter((r) => r.currentOccupancy === 0).length

    // 满员房间
    const full = roomList.filter((r) => r.currentOccupancy >= r.bedCount).length

    // 维修中
    const maintenance = roomList.filter((r) => r.roomStatus === 3).length

    // 部分入住
    const partial = total - available - full - maintenance

    // 入住率
    const totalBeds = roomList.reduce((sum, r) => sum + r.bedCount, 0)
    const occupiedBeds = roomList.reduce((sum, r) => sum + r.currentOccupancy, 0)
    const occupancyRate = totalBeds > 0 ? Math.round((occupiedBeds / totalBeds) * 100) : 0

    return { total, available, partial, full, maintenance, occupancyRate, totalBeds, occupiedBeds }
  })

  /** 入住率颜色（根据百分比动态变化） */
  const occupancyRateColor = computed(() => {
    const rate = stats.value.occupancyRate
    if (rate >= 90) return 'var(--el-color-danger)'
    if (rate >= 70) return 'var(--el-color-warning)'
    if (rate >= 50) return 'var(--el-color-primary)'
    return 'var(--el-color-success)'
  })

  /** 当前楼层名称 */
  const currentFloorName = computed(() => {
    if (!currentFloorId.value) return ''

    for (const building of props.buildingOptions) {
      for (const floor of building.floors || []) {
        if (floor.id === currentFloorId.value) {
          return `${building.name} - ${floor.name}`
        }
      }
    }
    return ''
  })

  // ==================== Watch ====================

  // 监听默认楼层变化
  watch(
    () => props.defaultFloorId,
    (newVal) => {
      if (newVal && newVal !== currentFloorId.value) {
        currentFloorId.value = newVal
      }
    }
  )

  // ==================== Methods ====================

  /** 视图切换处理 */
  const handleViewChange = (view: ViewType) => {
    currentView.value = view
    emit('view-change', view)
  }

  /** 楼层选择变更 */
  const handleFloorChange = (value: FloorSelectValue) => {
    currentFloorId.value = value
    emit('floor-change', value)
  }

  /** 点击房间 */
  const handleRoomClick = (room: RoomWithBeds) => {
    emit('room-click', room)
  }

  /** 点击床位 */
  const handleBedClick = (bed: BedWithStudent, room: RoomWithBeds) => {
    emit('bed-click', bed, room)
  }

  /** 点击空床位（分配学生） */
  const handleEmptyBedClick = (bed: BedWithStudent, room: RoomWithBeds) => {
    emit('empty-bed-click', bed, room)
  }

  // ==================== Lifecycle ====================
  onMounted(() => {
    // 如果有默认楼层，触发加载
    if (props.defaultFloorId) {
      emit('floor-change', props.defaultFloorId)
    }
  })

  // ==================== Expose ====================
  defineExpose({
    /** 当前视图 */
    currentView,
    /** 当前楼层ID */
    currentFloorId,
    /** 切换视图 */
    switchView: handleViewChange,
    /** 切换楼层 */
    switchFloor: handleFloorChange
  })
</script>

<style lang="scss" scoped>
  .dorm-visual-view {
    display: flex;
    flex-direction: column;
    height: 100%;
    min-height: 300px;
  }

  // 固定头部区域（统计数据等）
  .dorm-visual-header {
    flex-shrink: 0;
    padding-bottom: 16px;
  }

  // 可滚动内容区域
  .dorm-visual-content {
    flex: 1;
    min-height: 0; // 重要：允许 flex 子元素收缩
    overflow: hidden auto;

    // 滚动条样式（与全局表格滚动条一致）
    &::-webkit-scrollbar {
      width: 8px;
    }

    &::-webkit-scrollbar-track {
      background-color: var(--art-gray-200);
    }

    &::-webkit-scrollbar-thumb {
      background-color: #ccc;
      border-radius: 5px;
      transition: all 0.2s;

      &:hover {
        background-color: #b0abab;
      }
    }
  }

  // 暗黑模式滚动条
  .dark .dorm-visual-content {
    &::-webkit-scrollbar-track {
      background-color: var(--default-bg-color);
    }

    &::-webkit-scrollbar-thumb {
      background-color: var(--art-gray-300);
    }
  }

  // 统计栏样式
  .stats-bar {
    border: 1px solid var(--art-gray-200);
  }

  // 统计标签样式
  .stat-tag {
    display: inline-flex;
    gap: 6px;
    align-items: center;
    padding: 6px 12px;
    background: var(--default-box-color);
    border: 1px solid var(--art-gray-200);
    border-radius: 20px;
    transition: all 0.2s ease;

    &:hover {
      box-shadow: 0 2px 8px rgb(0 0 0 / 8%);
      transform: translateY(-1px);
    }

    &__dot {
      flex-shrink: 0;
      width: 8px;
      height: 8px;
      border-radius: 50%;
    }

    &__value {
      font-size: 15px;
      font-weight: 600;
      line-height: 1;
    }

    &__label {
      font-size: 12px;
      line-height: 1;
      color: var(--art-gray-500);
    }

    // 总数样式
    &--total {
      background: linear-gradient(135deg, var(--art-gray-100) 0%, var(--art-gray-50) 100%);

      .stat-tag__value {
        color: var(--art-gray-800);
      }
    }

    // 空闲 - 绿色
    &--success {
      background: linear-gradient(
        135deg,
        color-mix(in srgb, var(--el-color-success) 12%, transparent) 0%,
        color-mix(in srgb, var(--el-color-success) 5%, transparent) 100%
      );
      border-color: color-mix(in srgb, var(--el-color-success) 25%, transparent);

      .stat-tag__dot {
        background: var(--el-color-success);
      }

      .stat-tag__value {
        color: var(--el-color-success);
      }
    }

    // 部分入住 - 主题色
    &--primary {
      background: linear-gradient(
        135deg,
        color-mix(in srgb, var(--theme-color) 12%, transparent) 0%,
        color-mix(in srgb, var(--theme-color) 5%, transparent) 100%
      );
      border-color: color-mix(in srgb, var(--theme-color) 25%, transparent);

      .stat-tag__dot {
        background: var(--theme-color);
      }

      .stat-tag__value {
        color: var(--theme-color);
      }
    }

    // 满员 - 蓝色
    &--info {
      background: linear-gradient(
        135deg,
        color-mix(in srgb, var(--el-color-info) 12%, transparent) 0%,
        color-mix(in srgb, var(--el-color-info) 5%, transparent) 100%
      );
      border-color: color-mix(in srgb, var(--el-color-info) 25%, transparent);

      .stat-tag__dot {
        background: var(--el-color-info);
      }

      .stat-tag__value {
        color: var(--el-color-info);
      }
    }

    // 维修中 - 橙色
    &--warning {
      background: linear-gradient(
        135deg,
        color-mix(in srgb, var(--el-color-warning) 12%, transparent) 0%,
        color-mix(in srgb, var(--el-color-warning) 5%, transparent) 100%
      );
      border-color: color-mix(in srgb, var(--el-color-warning) 25%, transparent);

      .stat-tag__dot {
        background: var(--el-color-warning);
      }

      .stat-tag__value {
        color: var(--el-color-warning);
      }
    }
  }

  // 暗黑模式适配
  :deep(.dark) .stats-bar,
  .dark .stats-bar {
    border-color: var(--art-gray-300);
  }

  .dark .stat-tag {
    border-color: var(--art-gray-300);
  }
</style>
