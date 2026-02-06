<!--
  BuildingFloorNav 楼栋楼层导航组件
  @description 左侧手风琴式楼栋楼层导航，用于可视化视图
  @author 陈鸿昇
-->
<template>
  <div class="building-floor-nav">
    <ElScrollbar class="building-floor-nav__scrollbar">
      <div class="building-floor-nav__list">
        <!-- 楼栋列表 -->
        <div v-for="building in options" :key="building.id" class="building-item">
          <!-- 楼栋标题 -->
          <div
            class="building-item__header"
            :class="{ 'is-expanded': expandedBuilding === building.id }"
            @click="toggleBuilding(building)"
          >
            <div class="building-item__header-left">
              <ArtSvgIcon icon="ri:building-line" class="building-item__icon" />
              <span class="building-item__name">{{ building.name }}</span>
            </div>
            <ArtSvgIcon
              icon="ri:arrow-down-s-line"
              class="building-item__arrow"
              :class="{ 'is-expanded': expandedBuilding === building.id }"
            />
          </div>

          <!-- 楼层列表（展开时显示） -->
          <Transition name="accordion">
            <div v-show="expandedBuilding === building.id" class="building-item__floors">
              <div
                v-for="floor in building.floors"
                :key="floor.id"
                class="floor-item"
                :class="{ 'is-active': isFloorActive(floor.id) }"
                @click="selectFloor(floor, building)"
              >
                <ArtSvgIcon :icon="getFloorIcon(floor)" class="floor-item__icon" />
                <span class="floor-item__name">{{ floor.name }}</span>
              </div>
            </div>
          </Transition>
        </div>

        <!-- 空状态 -->
        <div v-if="options.length === 0" class="building-floor-nav__empty">
          <ArtSvgIcon icon="ri:building-line" class="text-2xl text-g-400 mb-2" />
          <span class="text-xs text-g-400">暂无楼栋数据</span>
        </div>
      </div>
    </ElScrollbar>
  </div>
</template>

<script setup lang="ts">
  import { ref, watch, onMounted } from 'vue'
  import { ElScrollbar } from 'element-plus'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import type { BuildingOption, FloorOption, FloorSelectValue } from '../types'

  defineOptions({ name: 'BuildingFloorNav' })

  // ==================== Props ====================
  interface Props {
    /** 选中的楼层值 */
    modelValue?: FloorSelectValue
    /** 楼栋选项列表 */
    options: BuildingOption[]
  }

  const props = withDefaults(defineProps<Props>(), {
    modelValue: null,
    options: () => []
  })

  // ==================== Emits ====================
  const emit = defineEmits<{
    (e: 'update:modelValue', value: FloorSelectValue): void
    (e: 'change', value: FloorSelectValue): void
  }>()

  // ==================== State ====================

  /** 当前展开的楼栋ID */
  const expandedBuilding = ref<string | null>(null)

  // ==================== Methods ====================

  /**
   * 切换楼栋展开/折叠
   */
  const toggleBuilding = (building: BuildingOption): void => {
    if (expandedBuilding.value === building.id) {
      // 已展开，则折叠
      expandedBuilding.value = null
    } else {
      // 展开该楼栋
      expandedBuilding.value = building.id
    }
  }

  /**
   * 选择楼层
   */
  // eslint-disable-next-line @typescript-eslint/no-unused-vars -- building kept for call signature
  const selectFloor = (floor: FloorOption, building: BuildingOption): void => {
    const value = floor.id as FloorSelectValue
    emit('update:modelValue', value)
    emit('change', value)
  }

  /**
   * 判断楼层是否选中
   */
  const isFloorActive = (floorId: number | string): boolean => {
    return props.modelValue === floorId
  }

  /**
   * 获取楼层图标
   */
  const getFloorIcon = (floor: FloorOption): string => {
    // "全部"选项使用不同图标
    if (typeof floor.id === 'string' && String(floor.id).startsWith('all:')) {
      return 'ri:layout-grid-line'
    }
    return 'ri:home-line'
  }

  /**
   * 根据当前选中值自动展开对应楼栋
   */
  const autoExpandBuilding = (): void => {
    if (!props.modelValue) return

    for (const building of props.options) {
      for (const floor of building.floors || []) {
        if (floor.id === props.modelValue) {
          expandedBuilding.value = building.id
          return
        }
      }
    }
  }

  /**
   * 默认选中第一个楼栋的"全部"
   */
  const selectDefaultFloor = (): void => {
    if (props.modelValue) return
    if (props.options.length === 0) return

    const firstBuilding = props.options[0]
    expandedBuilding.value = firstBuilding.id

    // 找到"全部"选项
    const allFloor = firstBuilding.floors?.find(
      (f) => typeof f.id === 'string' && String(f.id).startsWith('all:')
    )
    if (allFloor) {
      emit('update:modelValue', allFloor.id as FloorSelectValue)
      emit('change', allFloor.id as FloorSelectValue)
    } else if (firstBuilding.floors?.length > 0) {
      // 没有"全部"选项则选第一个楼层
      const firstFloor = firstBuilding.floors[0]
      emit('update:modelValue', firstFloor.id as FloorSelectValue)
      emit('change', firstFloor.id as FloorSelectValue)
    }
  }

  // ==================== Watch ====================

  watch(
    () => props.modelValue,
    () => {
      autoExpandBuilding()
    },
    { immediate: true }
  )

  watch(
    () => props.options,
    (newOptions) => {
      if (newOptions.length > 0 && !props.modelValue) {
        selectDefaultFloor()
      }
    },
    { immediate: true }
  )

  // ==================== Lifecycle ====================

  onMounted(() => {
    if (props.options.length > 0 && !props.modelValue) {
      selectDefaultFloor()
    }
  })
</script>

<style lang="scss" scoped>
  .building-floor-nav {
    display: flex;
    flex-direction: column;
    width: 200px;
    height: 100%;
    background: var(--default-box-color);

    &__scrollbar {
      flex: 1;
      overflow: hidden;
    }

    &__list {
      padding: 8px;
    }

    &__empty {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 40px 16px;
    }
  }

  .building-item {
    margin-bottom: 4px;

    &__header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 10px 12px;
      cursor: pointer;
      background: transparent;
      border-radius: 8px;
      transition: all 0.2s ease;

      &:hover {
        background: var(--art-gray-100);
      }

      &.is-expanded {
        background: var(--art-gray-100);
      }
    }

    &__header-left {
      display: flex;
      gap: 8px;
      align-items: center;
      min-width: 0;
    }

    &__icon {
      flex-shrink: 0;
      font-size: 16px;
      color: var(--art-gray-600);
    }

    &__name {
      overflow: hidden;
      font-size: 13px;
      font-weight: 500;
      color: var(--art-gray-700);
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    &__arrow {
      flex-shrink: 0;
      font-size: 16px;
      color: var(--art-gray-400);
      transition: transform 0.2s ease;

      &.is-expanded {
        transform: rotate(180deg);
      }
    }

    &__floors {
      padding-left: 8px;
      overflow: hidden;
    }
  }

  .floor-item {
    display: flex;
    gap: 8px;
    align-items: center;
    padding: 8px 12px;
    margin: 2px 0;
    cursor: pointer;
    background: transparent;
    border-radius: 6px;
    transition: all 0.2s ease;

    &:hover {
      background: var(--art-gray-100);
    }

    &.is-active {
      color: var(--theme-color);
      background: color-mix(in srgb, var(--theme-color) 12%, transparent);

      .floor-item__icon {
        color: var(--theme-color);
      }

      .floor-item__name {
        font-weight: 500;
        color: var(--theme-color);
      }
    }

    &__icon {
      flex-shrink: 0;
      font-size: 14px;
      color: var(--art-gray-500);
    }

    &__name {
      overflow: hidden;
      font-size: 12px;
      color: var(--art-gray-600);
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  // 手风琴动画
  .accordion-enter-active,
  .accordion-leave-active {
    max-height: 500px;
    opacity: 1;
    transition: all 0.25s ease;
  }

  .accordion-enter-from,
  .accordion-leave-to {
    max-height: 0;
    opacity: 0;
  }

  // 暗黑模式适配
  .dark .building-floor-nav {
    border-color: var(--art-gray-300);
  }

  .dark .building-item__header:hover,
  .dark .building-item__header.is-expanded {
    background: var(--art-gray-200);
  }

  .dark .floor-item:hover {
    background: var(--art-gray-200);
  }
</style>
