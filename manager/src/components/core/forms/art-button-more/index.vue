<!-- 更多按钮 -->
<template>
  <div>
    <ElDropdown v-if="hasAnyAuthItem">
      <div
        :class="[
          'art-button-more inline-flex items-center justify-center min-w-8 h-8 px-2.5 mr-2.5 text-sm c-p rounded-md align-middle cursor-pointer',
          isTextMode
            ? 'text-mode'
            : isCompactIconMode
              ? 'compact-icon-mode'
              : 'large-mode bg-g-200 dark:bg-g-300/45'
        ]"
        :style="buttonStyle"
      >
        <ArtSvgIcon v-if="!isTextMode" icon="ri:more-2-fill" />
        <span v-else class="button-text">更多</span>
      </div>
      <template #dropdown>
        <ElDropdownMenu>
          <template v-for="item in list" :key="item.key">
            <ElDropdownItem
              v-if="!item.auth || hasAuth(item.auth)"
              :disabled="item.disabled"
              @click="handleClick(item)"
            >
              <div class="flex-c gap-2" :style="{ color: item.color }">
                <ArtSvgIcon v-if="item.icon" :icon="item.icon" />
                <span>{{ item.label }}</span>
              </div>
            </ElDropdownItem>
          </template>
        </ElDropdownMenu>
      </template>
    </ElDropdown>
  </div>
</template>

<script setup lang="ts">
  import { storeToRefs } from 'pinia'
  import { useTableStore } from '@/store/modules/table'
  import { useAuth } from '@/hooks/core/useAuth'
  import { TableSizeEnum } from '@/enums/formEnum'

  defineOptions({ name: 'ArtButtonMore' })

  const { hasAuth } = useAuth()

  // 获取表格大小
  const tableStore = useTableStore()
  const { tableSize } = storeToRefs(tableStore)

  // 判断是否为文字模式（仅 SMALL 模式）
  const isTextMode = computed(() => {
    return tableSize.value === TableSizeEnum.SMALL
  })

  // 判断是否为紧凑图标模式（DEFAULT 模式）
  const isCompactIconMode = computed(() => {
    return tableSize.value === TableSizeEnum.DEFAULT
  })

  // 动态按钮样式
  const buttonStyle = computed(() => {
    const style: Record<string, string> = {}

    if (isTextMode.value) {
      // 文字模式（SMALL）：背景透明，只显示文字颜色
      style.backgroundColor = 'transparent'
      style.color = 'var(--el-text-color-regular)'
    } else if (isCompactIconMode.value) {
      // 紧凑图标模式（DEFAULT）：背景透明，只显示图标颜色
      style.backgroundColor = 'transparent'
      style.color = 'var(--el-text-color-regular)'
    } else {
      // 宽松模式（LARGE）：使用原有样式（bg-g-200）
      // 样式通过 class 控制，这里不需要额外设置
    }

    return style
  })

  export interface ButtonMoreItem {
    /** 按钮标识，可用于点击事件 */
    key: string | number
    /** 按钮文本 */
    label: string
    /** 是否禁用 */
    disabled?: boolean
    /** 权限标识 */
    auth?: string
    /** 图标组件 */
    icon?: string
    /** 文本颜色 */
    color?: string
    /** 图标颜色（优先级高于 color） */
    iconColor?: string
  }

  interface Props {
    /** 下拉项列表 */
    list: ButtonMoreItem[]
    /** 整体权限控制 */
    auth?: string
  }

  const props = withDefaults(defineProps<Props>(), {})

  // 检查是否有任何有权限的 item
  const hasAnyAuthItem = computed(() => {
    return props.list.some((item) => !item.auth || hasAuth(item.auth))
  })

  const emit = defineEmits<{
    (e: 'click', item: ButtonMoreItem): void
  }>()

  const handleClick = (item: ButtonMoreItem) => {
    emit('click', item)
  }
</script>

<style scoped lang="scss">
  .art-button-more {
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

  // 宽松模式（LARGE）：带背景的图标（样式通过 Tailwind 类控制）
  .art-button-more.large-mode {
    transition: background-color 0.3s cubic-bezier(0.4, 0, 0.2, 1);

    &:hover {
      background-color: var(--g-300);
    }

    &.dark-mode:hover {
      background-color: rgb(255 255 255 / 10%);
    }
  }

  // 紧凑图标模式（DEFAULT）：透明背景的小图标
  .art-button-more.compact-icon-mode {
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
  .art-button-more.text-mode {
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
