<!--
  ViewSwitcher 视图切换器组件
  @description 表格/平面图视图切换（基于 ArtViewSwitcher 封装）
  @author 陈鸿昇
-->
<template>
  <ArtViewSwitcher
    :model-value="modelValue"
    :options="viewOptions"
    @update:model-value="handleChange"
  />
</template>

<script setup lang="ts">
  import ArtViewSwitcher from '@/components/core/base/art-view-switcher/index.vue'
  import type { ViewType } from '../types'

  defineOptions({ name: 'ViewSwitcher' })

  // ==================== Props ====================

  interface Props {
    /** 当前视图类型 */
    modelValue: ViewType
  }

  defineProps<Props>()

  // ==================== Emits ====================

  const emit = defineEmits<{
    (e: 'update:modelValue', value: ViewType): void
  }>()

  // ==================== Constants ====================

  /** 视图选项配置 */
  const viewOptions = [
    { value: 'table', label: '表格', icon: 'ri:table-line' },
    { value: 'visual', label: '平面图', icon: 'ri:map-2-line' }
  ]

  // ==================== Methods ====================

  /** 处理视图切换 */
  const handleChange = (value: string) => {
    emit('update:modelValue', value as ViewType)
  }
</script>
