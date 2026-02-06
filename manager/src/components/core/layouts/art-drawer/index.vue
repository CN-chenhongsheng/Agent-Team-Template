<!-- 通用 Drawer 抽屉组件 -->
<template>
  <ElDrawer
    v-model="drawerVisible"
    :title="title"
    :size="size"
    :direction="direction"
    :lock-scroll="lockScroll"
    :with-header="withHeader"
    :before-close="handleBeforeClose"
    :destroy-on-close="destroyOnClose"
    :close-on-click-modal="closeOnClickModal"
    :modal="modal"
    :modal-class="modalClass"
    :z-index="zIndex"
    @open="handleOpen"
    @close="handleClose"
    class="art-drawer"
    resizable
  >
    <template v-if="withHeader" #header>
      <slot name="header">
        <div class="drawer-header">
          <span class="drawer-title">{{ title }}</span>
        </div>
      </slot>
    </template>

    <div v-loading="loading" class="drawer-body">
      <slot name="body">
        <slot />
      </slot>
    </div>

    <template v-if="showFooter" #footer>
      <slot name="footer" />
    </template>
  </ElDrawer>
</template>

<script setup lang="ts">
  import { computed } from 'vue'
  import { ElDrawer } from 'element-plus'

  defineOptions({ name: 'ArtDrawer' })

  interface Props {
    /** 是否显示 */
    modelValue: boolean
    /** 标题 */
    title?: string
    /** 宽度（direction 为 rtl/ltr 时）或高度（direction 为 ttb/btt 时） */
    size?: string | number
    /** Drawer 打开的方向 */
    direction?: 'rtl' | 'ltr' | 'ttb' | 'btt'
    /** 是否在 body 元素上锁定滚动 */
    lockScroll?: boolean
    /** 是否显示 header */
    withHeader?: boolean
    /** 关闭前的回调函数 */
    beforeClose?: () => Promise<boolean> | boolean
    /** 控制是否在关闭 Drawer 之后将子元素全部销毁 */
    destroyOnClose?: boolean
    /** 是否可以通过点击 modal 关闭 Drawer */
    closeOnClickModal?: boolean
    /** 是否显示遮罩层 */
    modal?: boolean
    /** 遮罩层的自定义类名 */
    modalClass?: string
    /** 设置 Drawer 的 z-index */
    zIndex?: number
    /** 加载状态 */
    loading?: boolean
    /** 是否显示 footer 插槽 */
    showFooter?: boolean
  }

  interface Emits {
    (e: 'update:modelValue', value: boolean): void
    (e: 'open'): void
    (e: 'close'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    modelValue: false,
    title: '',
    size: '450',
    direction: 'rtl',
    lockScroll: true,
    withHeader: true,
    destroyOnClose: false,
    closeOnClickModal: true,
    modal: true,
    loading: false,
    showFooter: false
  })

  const emit = defineEmits<Emits>()

  const drawerVisible = computed({
    get: () => props.modelValue,
    set: (value: boolean) => emit('update:modelValue', value)
  })

  const handleBeforeClose = async (done: () => void) => {
    if (props.beforeClose) {
      const result = await props.beforeClose()
      if (result !== false) {
        done()
      }
    } else {
      done()
    }
  }

  const handleOpen = () => {
    emit('open')
  }

  const handleClose = () => {
    emit('close')
  }
</script>

<style lang="scss" scoped>
  .drawer-header {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .drawer-title {
      font-size: 16px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }
  }

  .drawer-body {
    min-height: 100px;
    padding: 0;
  }
</style>

<style lang="scss">
  // 全局样式，用于 Drawer 内容区域
  .art-drawer {
    .el-drawer__body {
      padding: 20px;
      overflow-y: auto;

      // 自定义滚动条样式
      &::-webkit-scrollbar {
        width: 6px;
      }

      &::-webkit-scrollbar-track {
        background: var(--el-fill-color-lighter);
        border-radius: 3px;
      }

      &::-webkit-scrollbar-thumb {
        background: var(--el-border-color-darker);
        border-radius: 3px;

        &:hover {
          background: var(--el-text-color-placeholder);
        }
      }
    }

    .el-drawer__header {
      padding-bottom: 16px;
      margin-bottom: 20px;
      font-size: 18px;
      font-weight: 600;
    }
  }
</style>
