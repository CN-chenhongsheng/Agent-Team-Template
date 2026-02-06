/**
 * 宿舍管理组件导出
 * @description 导出宿舍可视化视图相关组件
 * @author 陈鸿昇
 */

// 主组件
export { default as DormVisualView } from './dorm-visual-view/index.vue'

// 子组件
export { default as BedSlot } from './dorm-visual-view/components/BedSlot.vue'
export { default as RoomCard } from './dorm-visual-view/components/RoomCard.vue'
export { default as RoomGrid } from './dorm-visual-view/components/RoomGrid.vue'
export { default as ViewSwitcher } from './dorm-visual-view/components/ViewSwitcher.vue'
export { default as FloorSelector } from './dorm-visual-view/components/FloorSelector.vue'
export { default as BuildingFloorNav } from './dorm-visual-view/components/BuildingFloorNav.vue'
export { default as StatusLegend } from './dorm-visual-view/components/StatusLegend.vue'
export { default as StudentSelectDialog } from './dorm-visual-view/components/StudentSelectDialog.vue'

// Composables
export { useVisualView } from './dorm-visual-view/composables/useVisualView'
export { useStatusColors } from './dorm-visual-view/composables/useStatusColors'

// Types
export * from './dorm-visual-view/types'
