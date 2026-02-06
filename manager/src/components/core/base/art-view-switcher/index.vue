<!--
  ArtViewSwitcher 视图切换器组件
  @description 通用视图/模式切换组件，支持自定义选项
  @author 陈鸿昇
-->
<template>
  <div class="art-view-switcher">
    <button
      v-for="option in options"
      :key="option.value"
      type="button"
      class="art-view-switcher__btn"
      :class="{ 'is-active': modelValue === option.value }"
      @click="switchView(option.value)"
    >
      <ArtSvgIcon v-if="option.icon" :icon="option.icon" />
      <span>{{ option.label }}</span>
    </button>
  </div>
</template>

<script setup lang="ts">
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'

  defineOptions({ name: 'ArtViewSwitcher' })

  // ==================== Types ====================

  /** 切换选项 */
  export interface SwitchOption<T extends string = string> {
    /** 选项值 */
    value: T
    /** 显示文本 */
    label: string
    /** 可选图标（iconify 格式） */
    icon?: string
  }

  // ==================== Props ====================

  interface Props {
    /** 当前选中值 */
    modelValue: string
    /** 选项列表 */
    options: SwitchOption[]
  }

  const props = defineProps<Props>()

  // ==================== Emits ====================

  const emit = defineEmits<{
    (e: 'update:modelValue', value: string): void
  }>()

  // ==================== Methods ====================

  /** 切换视图 */
  const switchView = (value: string) => {
    if (props.modelValue !== value) {
      emit('update:modelValue', value)
    }
  }
</script>

<style lang="scss" scoped>
  // 样式通过全局类 .art-view-switcher 实现，见 app.scss
</style>
