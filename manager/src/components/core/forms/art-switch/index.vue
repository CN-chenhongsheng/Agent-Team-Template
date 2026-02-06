<!-- 开关组件，带默认文本 -->
<template>
  <ElSwitch
    :model-value="modelValue"
    :active-text="props.activeText"
    :inactive-text="props.inactiveText"
    v-bind="$attrs"
    @update:model-value="handleChange"
  />
</template>

<script setup lang="ts">
  import { ElSwitch } from 'element-plus'

  defineOptions({ name: 'ArtSwitch' })

  interface Props {
    modelValue?: boolean | string | number
    /** 激活时的文本 */
    activeText?: string
    /** 未激活时的文本 */
    inactiveText?: string
  }

  const props = withDefaults(defineProps<Props>(), {
    activeText: '✓',
    inactiveText: '✗'
  })

  const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean | string | number): void
    (e: 'change', value: boolean | string | number): void
  }>()

  const handleChange = (value: boolean | string | number) => {
    emit('update:modelValue', value)
    emit('change', value)
  }
</script>
