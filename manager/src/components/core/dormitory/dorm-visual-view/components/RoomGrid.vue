<!--
  RoomGrid 房间网格组件
  @description 响应式网格布局展示房间列表
  @author 陈鸿昇
-->
<template>
  <div class="room-grid-container h-full" v-loading="loading">
    <!-- 空状态 -->
    <div v-if="!loading && !rooms.length" class="room-grid-empty h-full">
      <ElEmpty description="暂无房间数据" class="h-full">
        <template #image>
          <ArtSvgIcon icon="ri:home-line" class="text-6xl text-g-400" />
        </template>
      </ElEmpty>
    </div>

    <!-- 房间网格 -->
    <div
      v-else
      class="grid gap-4 grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6"
    >
      <RoomCard
        v-for="room in rooms"
        :key="room.id"
        :room="room"
        @click="handleRoomClick"
        @bed-click="handleBedClick"
        @empty-bed-click="handleEmptyBedClick"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ElEmpty } from 'element-plus'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import RoomCard from './RoomCard.vue'
  import type { RoomWithBeds, BedWithStudent } from '../types'

  defineOptions({ name: 'RoomGrid' })

  // ==================== Props ====================
  interface Props {
    /** 房间列表 */
    rooms: RoomWithBeds[]
    /** 加载状态 */
    loading?: boolean
  }

  withDefaults(defineProps<Props>(), {
    rooms: () => [],
    loading: false
  })

  // ==================== Emits ====================
  const emit = defineEmits<{
    (e: 'room-click', room: RoomWithBeds): void
    (e: 'bed-click', bed: BedWithStudent, room: RoomWithBeds): void
    (e: 'empty-bed-click', bed: BedWithStudent, room: RoomWithBeds): void
  }>()

  // ==================== Methods ====================

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
</script>

<style lang="scss" scoped>
  .room-grid-container {
    min-height: 200px;
  }

  .room-grid-empty {
    @apply py-8;
  }
</style>
