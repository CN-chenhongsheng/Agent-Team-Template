<!--
  RoomCard 房间卡片组件
  @description 展示房间信息和床位布局
  @author 陈鸿昇
-->
<template>
  <div :class="cardClasses" @click="handleClick">
    <!-- 房间号 + 状态指示点 -->
    <div class="flex-cb">
      <div class="flex-c gap-2">
        <span class="text-sm font-medium text-g-900">{{ room.roomNumber }}</span>
        <!-- 状态指示点 -->
        <ElTooltip :content="statusText" placement="top">
          <span class="status-dot" :class="statusDotClass"></span>
        </ElTooltip>
      </div>
      <div class="flex-c gap-1">
        <!-- 设施图标 -->
        <ElTooltip v-if="room.hasAirConditioner" content="空调" placement="top">
          <ArtSvgIcon icon="ri:temp-cold-line" class="text-xs text-g-500" />
        </ElTooltip>
        <ElTooltip v-if="room.hasBathroom" content="独立卫生间" placement="top">
          <ArtSvgIcon icon="ri:drop-line" class="text-xs text-g-500" />
        </ElTooltip>
        <ElTooltip v-if="room.hasBalcony" content="阳台" placement="top">
          <ArtSvgIcon icon="ri:window-line" class="text-xs text-g-500" />
        </ElTooltip>
      </div>
    </div>

    <!-- 床位网格（最多显示2行） -->
    <div class="bed-grid mt-2 grid gap-1" :style="gridStyle">
      <BedSlot
        v-for="bed in visibleBeds"
        :key="bed.id"
        :bed="bed"
        @click="handleBedClick"
        @empty-bed-click="handleEmptyBedClick"
      />
      <!-- 空床位占位（如果床位数据不足，且在可见范围内） -->
      <template v-if="visibleBeds.length < visibleBedCount && visibleBeds.length < actualBedCount">
        <div
          v-for="i in Math.min(
            visibleBedCount - visibleBeds.length,
            actualBedCount - visibleBeds.length
          )"
          :key="`empty-${i}`"
          class="size-8 flex-cc rounded border border-dashed border-g-300 bg-g-100/50"
        >
          <span class="text-xs text-g-400">-</span>
        </div>
      </template>
      <!-- 超出提示 -->
      <ElTooltip v-if="hasMoreBeds" :content="`还有 ${hiddenBedCount} 个床位`" placement="top">
        <div class="size-8 flex-cc rounded bg-g-100 text-g-500 text-xs font-medium">
          +{{ hiddenBedCount }}
        </div>
      </ElTooltip>
    </div>

    <!-- 底部信息 -->
    <div class="mt-2 flex-cb text-xs">
      <span class="text-g-600">{{ room.currentOccupancy }}/{{ actualBedCount }}</span>
      <span :class="statusTextClass">{{ statusText }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { computed } from 'vue'
  import { ElTooltip } from 'element-plus'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import BedSlot from './BedSlot.vue'
  import type { RoomWithBeds, BedWithStudent } from '../types'
  import { useStatusColors } from '../composables/useStatusColors'

  defineOptions({ name: 'RoomCard' })

  // ==================== Props ====================
  interface Props {
    /** 房间数据 */
    room: RoomWithBeds
    /** 是否可点击 */
    clickable?: boolean
  }

  const props = withDefaults(defineProps<Props>(), {
    clickable: true
  })

  // ==================== Emits ====================
  const emit = defineEmits<{
    (e: 'click', room: RoomWithBeds): void
    (e: 'bed-click', bed: BedWithStudent, room: RoomWithBeds): void
    (e: 'empty-bed-click', bed: BedWithStudent, room: RoomWithBeds): void
  }>()

  // ==================== Composables ====================
  const { getRoomDisplayStatus, getRoomStatusTextClass, getRoomStatusText } = useStatusColors()

  // ==================== Computed ====================

  /** 房间显示状态 */
  const displayStatus = computed(() => getRoomDisplayStatus(props.room))

  /** 状态文本 */
  const statusText = computed(() => getRoomStatusText(props.room))

  /** 状态文字颜色类 */
  const statusTextClass = computed(() => getRoomStatusTextClass(props.room))

  /** 状态指示点样式类 */
  const statusDotClass = computed(() => {
    const status = displayStatus.value
    const dotColors: Record<string, string> = {
      available: 'bg-success',
      partial: 'bg-primary',
      full: 'bg-info',
      maintenance: 'bg-warning',
      reserved: 'bg-secondary'
    }
    return dotColors[status] || 'bg-g-400'
  })

  /** 卡片样式类 */
  const cardClasses = computed(() => {
    const baseClasses = [
      'room-card',
      'rounded-custom-sm',
      'p-3',
      'bg-box',
      'border',
      'border-g-200',
      'dark:border-g-400'
    ]

    // 交互类
    const interactionClasses = props.clickable ? 'cursor-pointer room-card--interactive' : ''

    return [...baseClasses, interactionClasses].filter(Boolean).join(' ')
  })

  /** 床位网格列数（固定4列） */
  const gridCols = computed(() => 4)

  /** 最大可见床位数（2行 x 4列 = 8个，留1个位置给 +N 提示） */
  const maxVisibleBeds = 8

  /** 实际床位总数（优先使用beds数组长度，确保数据准确） */
  const actualBedCount = computed(() => {
    // 优先使用实际的 beds 数组长度，因为 bedCount 字段可能不同步
    return props.room.beds?.length || props.room.bedCount || 0
  })

  /** 实际可见床位数量 */
  const visibleBedCount = computed(() => {
    const total = actualBedCount.value
    // 如果总数超过8个，留一个位置给 "+N" 提示
    if (total > maxVisibleBeds) return maxVisibleBeds - 1
    return total
  })

  /** 可见的床位列表 */
  const visibleBeds = computed(() => {
    return props.room.beds.slice(0, visibleBedCount.value)
  })

  /** 是否有更多床位 */
  const hasMoreBeds = computed(() => actualBedCount.value > maxVisibleBeds)

  /** 隐藏的床位数量 */
  const hiddenBedCount = computed(() => {
    if (!hasMoreBeds.value) return 0
    return actualBedCount.value - visibleBedCount.value
  })

  /** 网格样式 */
  const gridStyle = computed(() => ({
    gridTemplateColumns: `repeat(${gridCols.value}, minmax(0, 1fr))`
  }))

  // ==================== Methods ====================

  /** 点击房间卡片 */
  const handleClick = () => {
    if (!props.clickable) return
    emit('click', props.room)
  }

  /** 点击床位 */
  const handleBedClick = (bed: BedWithStudent) => {
    emit('bed-click', bed, props.room)
  }

  /** 点击空床位（分配学生） */
  const handleEmptyBedClick = (bed: BedWithStudent) => {
    emit('empty-bed-click', bed, props.room)
  }
</script>

<style lang="scss" scoped>
  // 卡片基础样式
  .room-card {
    display: flex;
    flex-direction: column;
    min-height: 120px; // 确保卡片最小高度一致
    transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);

    // 可交互状态
    &--interactive {
      cursor: pointer;

      &:hover {
        border-color: var(--art-gray-300);
        box-shadow:
          0 2px 4px -1px rgb(0 0 0 / 4%),
          0 4px 8px -2px rgb(0 0 0 / 3%);
      }

      &:active {
        box-shadow: 0 1px 3px -1px rgb(0 0 0 / 4%);
      }
    }
  }

  // 床位网格区域
  .bed-grid {
    flex: 1;
    align-content: start;
  }

  // 暗黑模式悬浮阴影
  .dark .room-card--interactive:hover {
    border-color: var(--art-gray-500);
    box-shadow:
      0 4px 6px -1px rgb(0 0 0 / 25%),
      0 10px 15px -3px rgb(0 0 0 / 20%),
      0 0 0 1px rgb(255 255 255 / 5%);
  }

  // 状态指示点
  .status-dot {
    display: inline-block;
    flex-shrink: 0;
    width: 8px;
    height: 8px;
    border-radius: 50%;
  }

  // 卡片悬停时指示点动画
  .room-card:hover .status-dot {
    animation: pulse-dot 1.5s ease-in-out infinite;
  }

  @keyframes pulse-dot {
    0%,
    100% {
      opacity: 1;
      transform: scale(1);
    }

    50% {
      opacity: 0.7;
      transform: scale(1.2);
    }
  }
</style>
