<!-- 表格按钮 -->
<template>
  <div
    :class="[
      'art-button-table inline-flex items-center justify-center min-w-8 h-8 px-2.5 mr-2.5 text-sm c-p rounded-md align-middle',
      isTextMode ? 'text-mode' : isCompactIconMode ? 'compact-icon-mode' : buttonClass
    ]"
    :style="buttonStyle"
    @click="handleClick"
  >
    <ArtSvgIcon v-if="!isTextMode" :icon="iconContent" />
    <span v-else class="button-text">{{ buttonText }}</span>
  </div>
</template>

<script setup lang="ts">
  import { storeToRefs } from 'pinia'
  import { useTableStore } from '@/store/modules/table'
  import { useDictStore } from '@/store/modules/dict'
  import { TableSizeEnum } from '@/enums/formEnum'

  defineOptions({ name: 'ArtButtonTable' })

  interface Props {
    /** 按钮类型 */
    type?: string
    /** 按钮图标 */
    icon?: string
    /** 按钮样式类 */
    iconClass?: string
    /** icon 颜色 */
    iconColor?: string
    /** 按钮背景色 */
    buttonBgColor?: string
  }

  const props = withDefaults(defineProps<Props>(), {})

  const emit = defineEmits<{
    (e: 'click'): void
  }>()

  // 获取表格大小
  const tableStore = useTableStore()
  const { tableSize } = storeToRefs(tableStore)

  // 获取字典 Store
  const dictStore = useDictStore()

  // 判断是否为文字模式（仅 SMALL 模式）
  const isTextMode = computed(() => {
    return tableSize.value === TableSizeEnum.SMALL
  })

  // 判断是否为紧凑图标模式（DEFAULT 模式）
  const isCompactIconMode = computed(() => {
    return tableSize.value === TableSizeEnum.DEFAULT
  })

  // 从 Store 获取按钮配置（响应式）
  const buttonConfigs = computed(() => {
    return dictStore.getButtonConfigs()
  })

  // 组件挂载时确保配置已加载（首次使用时会触发加载，后续使用缓存）
  onMounted(() => {
    dictStore.ensureButtonConfigsLoaded()
  })

  // 获取图标内容
  const iconContent = computed(() => {
    return props.icon || (props.type ? buttonConfigs.value[props.type]?.icon : '') || ''
  })

  // 获取按钮文本
  const buttonText = computed(() => {
    return props.type ? buttonConfigs.value[props.type]?.text || '' : ''
  })

  // 获取按钮样式类
  const buttonClass = computed(() => {
    return props.iconClass || (props.type ? buttonConfigs.value[props.type]?.class : '') || ''
  })

  // 动态按钮样式
  const buttonStyle = computed(() => {
    const style: Record<string, string> = {}

    if (isTextMode.value) {
      // 文字模式（SMALL）：背景透明，只显示文字颜色
      style.backgroundColor = 'transparent'
      if (props.iconColor) {
        style.color = props.iconColor
      } else if (props.type) {
        // 根据按钮类型设置文字颜色
        const typeColors: Record<string, string> = {
          add: 'var(--el-color-primary)',
          edit: 'var(--el-color-info)',
          delete: 'var(--el-color-danger)',
          view: 'var(--el-color-info)',
          more: 'var(--el-text-color-regular)'
        }
        style.color = typeColors[props.type] || 'var(--el-text-color-regular)'
      }
    } else if (isCompactIconMode.value) {
      // 紧凑图标模式（DEFAULT）：背景透明，只显示图标颜色
      style.backgroundColor = 'transparent'
      if (props.iconColor) {
        style.color = props.iconColor
      } else if (props.type) {
        // 根据按钮类型设置图标颜色
        const typeColors: Record<string, string> = {
          add: 'var(--el-color-primary)',
          edit: 'var(--el-color-info)',
          delete: 'var(--el-color-danger)',
          view: 'var(--el-color-info)',
          more: 'var(--el-text-color-regular)'
        }
        style.color = typeColors[props.type] || 'var(--el-text-color-regular)'
      }
    } else {
      // 宽松模式（LARGE）：使用原有样式
      if (props.buttonBgColor) {
        style.backgroundColor = props.buttonBgColor
      }
      if (props.iconColor) {
        style.color = props.iconColor
      }
    }

    return style
  })

  const handleClick = () => {
    emit('click')
  }
</script>

<style scoped lang="scss">
  .art-button-table {
    :deep(.art-svg-icon) {
      transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      transform-origin: center;
    }

    &:hover :deep(.art-svg-icon) {
      transform: scale(1.1);
    }

    &:active :deep(.art-svg-icon) {
      transform: scale(0.95);
    }
  }

  // 紧凑图标模式（DEFAULT）：透明背景的小图标
  .art-button-table.compact-icon-mode {
    background-color: transparent !important;
    transition: background-color 0.3s cubic-bezier(0.4, 0, 0.2, 1);

    &:hover {
      background-color: rgb(0 0 0 / 5%) !important;
    }

    &.dark-mode:hover {
      background-color: rgb(255 255 255 / 5%) !important;
    }
  }

  // 文字模式（SMALL）：显示中文文字
  .art-button-table.text-mode {
    min-width: auto;
    padding: 0 8px;
    background-color: transparent !important;
    transition: background-color 0.3s cubic-bezier(0.4, 0, 0.2, 1);

    .button-text {
      font-size: 12px;
      line-height: 1;
      white-space: nowrap;
      user-select: none;
    }

    &:hover {
      background-color: rgb(0 0 0 / 5%) !important;
    }

    &.dark-mode:hover {
      background-color: rgb(255 255 255 / 5%) !important;
    }
  }
</style>
