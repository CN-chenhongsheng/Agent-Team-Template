<template>
  <template v-for="(part, index) in parts" :key="index">
    <mark v-if="part.match" class="permission-highlight">{{ part.text }}</mark>
    <template v-else>{{ part.text }}</template>
  </template>
</template>

<script setup lang="ts">
  import { splitHighlight } from './helper'

  defineOptions({ name: 'PermissionHighlightText' })

  const props = defineProps<{
    text: string
    keyword: string
  }>()

  const parts = computed(() => splitHighlight(props.text, props.keyword))
</script>

<style scoped lang="scss">
  .permission-highlight {
    padding: 0 1px;
    color: inherit;
    background: var(--el-color-warning-light-7);
    border-radius: 2px;
  }
</style>
