<!--
  StatusLegend 状态图例组件
  @description 展示所有状态的颜色含义
  @author 陈鸿昇
-->
<template>
  <div class="flex-c flex-wrap gap-4 text-sm text-g-600 border-t-d pt-3 mt-4">
    <span v-for="item in legendItems" :key="item.key" class="flex-c gap-1.5">
      <span class="size-3 rounded-full" :class="item.colorClass"></span>
      <span>{{ item.label }}</span>
    </span>
  </div>
</template>

<script setup lang="ts">
  defineOptions({ name: 'StatusLegend' })

  // ==================== Props ====================
  interface Props {
    /** 显示模式: room-房间图例, bed-床位图例, all-全部 */
    mode?: 'room' | 'bed' | 'all'
  }

  const props = withDefaults(defineProps<Props>(), {
    mode: 'all'
  })

  // ==================== Data ====================

  /** 房间状态图例 */
  const roomLegendItems = [
    { key: 'room-available', label: '空闲', colorClass: 'bg-success' },
    { key: 'room-partial', label: '部分入住', colorClass: 'bg-primary' },
    { key: 'room-full', label: '满员', colorClass: 'bg-info' },
    { key: 'room-maintenance', label: '维修中', colorClass: 'bg-warning' }
  ]

  /** 床位状态图例 */
  const bedLegendItems = [
    { key: 'bed-available', label: '空闲', colorClass: 'bg-g-400' },
    { key: 'bed-occupied', label: '已入住', colorClass: 'bg-primary' },
    { key: 'bed-maintenance', label: '维修中', colorClass: 'bg-warning' },
    { key: 'bed-reserved', label: '已预订', colorClass: 'bg-secondary' }
  ]

  /** 所有图例项 */
  const allLegendItems = [
    { key: 'available', label: '空闲', colorClass: 'bg-success' },
    { key: 'occupied', label: '已入住', colorClass: 'bg-primary' },
    { key: 'full', label: '满员', colorClass: 'bg-info' },
    { key: 'maintenance', label: '维修中', colorClass: 'bg-warning' },
    { key: 'reserved', label: '已预订', colorClass: 'bg-secondary' }
  ]

  // ==================== Computed ====================

  /** 当前显示的图例项 */
  const legendItems = computed(() => {
    switch (props.mode) {
      case 'room':
        return roomLegendItems
      case 'bed':
        return bedLegendItems
      default:
        return allLegendItems
    }
  })
</script>

<style lang="scss" scoped>
  // 所有样式通过 Tailwind 类实现
</style>
