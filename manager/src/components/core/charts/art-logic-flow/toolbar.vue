<!-- LogicFlow 工具栏组件 -->
<template>
  <div class="lf-toolbar" :class="{ 'is-vertical': vertical }">
    <!-- 缩放控制组 -->
    <div v-if="props.showZoomIn || props.showZoomOut || props.showResetZoom" class="toolbar-group">
      <button class="toolbar-btn" @click="handleZoomIn" title="放大" :disabled="!props.showZoomIn">
        <ArtSvgIcon icon="ri-zoom-in-line" :size="18" />
      </button>
      <button
        class="toolbar-btn"
        @click="handleZoomOut"
        title="缩小"
        :disabled="!props.showZoomOut"
      >
        <ArtSvgIcon icon="ri-zoom-out-line" :size="18" />
      </button>
      <button
        class="toolbar-btn"
        @click="handleResetZoom"
        title="重置缩放"
        :disabled="!props.showResetZoom"
      >
        <ArtSvgIcon icon="ri-refresh-line" :size="18" />
      </button>
    </div>

    <!-- 视图控制组 -->
    <div v-if="props.showFitView || props.showUndo || props.showRedo" class="toolbar-group">
      <button
        class="toolbar-btn"
        @click="handleFitView"
        title="适应视图"
        :disabled="!props.showFitView"
      >
        <ArtSvgIcon icon="ri-focus-3-line" :size="18" />
      </button>
      <button class="toolbar-btn" @click="handleUndo" title="撤销" :disabled="!props.showUndo">
        <ArtSvgIcon icon="ri-arrow-go-back-line" :size="18" />
      </button>
      <button class="toolbar-btn" @click="handleRedo" title="重做" :disabled="!props.showRedo">
        <ArtSvgIcon icon="ri-arrow-go-forward-line" :size="18" />
      </button>
    </div>

    <!-- 功能控制组 -->
    <div v-if="props.showMiniMap || props.showFullscreen || props.showExport" class="toolbar-group">
      <button
        class="toolbar-btn"
        :class="{ active: miniMapVisible }"
        @click="handleToggleMiniMap"
        title="小地图"
        :disabled="!props.showMiniMap"
      >
        <ArtSvgIcon icon="ri-map-2-line" :size="18" />
      </button>
      <button
        class="toolbar-btn"
        :class="{ active: isFullscreen }"
        @click="handleToggleFullscreen"
        title="全屏"
        :disabled="!props.showFullscreen"
      >
        <ArtSvgIcon
          :icon="isFullscreen ? 'ri-fullscreen-exit-line' : 'ri-fullscreen-line'"
          :size="18"
        />
      </button>
      <button
        class="toolbar-btn"
        @click="handleExportImage"
        title="导出图片"
        :disabled="!props.showExport"
      >
        <ArtSvgIcon icon="ri-image-line" :size="18" />
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'

  defineOptions({ name: 'LogicFlowToolbar' })

  interface Props {
    /** 是否禁用 */
    disabled?: boolean
    /** 是否垂直布局 */
    vertical?: boolean
    /** 是否显示放大按钮 */
    showZoomIn?: boolean
    /** 是否显示缩小按钮 */
    showZoomOut?: boolean
    /** 是否显示重置缩放按钮 */
    showResetZoom?: boolean
    /** 是否显示适应视图按钮 */
    showFitView?: boolean
    /** 是否显示撤销按钮 */
    showUndo?: boolean
    /** 是否显示重做按钮 */
    showRedo?: boolean
    /** 是否显示小地图按钮 */
    showMiniMap?: boolean
    /** 是否显示全屏按钮 */
    showFullscreen?: boolean
    /** 是否显示导出图片按钮 */
    showExport?: boolean
    /** 小地图是否可见 */
    miniMapVisible?: boolean
    /** 是否全屏 */
    isFullscreen?: boolean
  }

  const props = withDefaults(defineProps<Props>(), {
    vertical: true,
    showZoomIn: true,
    showZoomOut: true,
    showResetZoom: true,
    showFitView: true,
    showUndo: true,
    showRedo: true,
    showMiniMap: true,
    showFullscreen: true,
    showExport: true,
    miniMapVisible: false,
    isFullscreen: false
  })

  interface Emits {
    (e: 'zoom-in'): void
    (e: 'zoom-out'): void
    (e: 'reset-zoom'): void
    (e: 'fit-view'): void
    (e: 'undo'): void
    (e: 'redo'): void
    (e: 'toggle-minimap'): void
    (e: 'toggle-fullscreen'): void
    (e: 'export-image'): void
  }

  const emit = defineEmits<Emits>()

  const handleZoomIn = () => emit('zoom-in')
  const handleZoomOut = () => emit('zoom-out')
  const handleResetZoom = () => emit('reset-zoom')
  const handleFitView = () => emit('fit-view')
  const handleUndo = () => emit('undo')
  const handleRedo = () => emit('redo')
  const handleToggleMiniMap = () => emit('toggle-minimap')
  const handleToggleFullscreen = () => emit('toggle-fullscreen')
  const handleExportImage = () => emit('export-image')
</script>

<style scoped lang="scss">
  .lf-toolbar {
    position: absolute;
    top: 12px;
    right: 12px;
    z-index: 10;
    display: flex;
    flex-direction: column;
    gap: 8px;

    // 水平布局
    &:not(.is-vertical) {
      top: auto;
      right: 12px;
      bottom: 12px;
      flex-direction: row;
    }
  }

  .toolbar-group {
    display: flex;
    flex-direction: column;
    gap: 4px;
    padding: 6px;
    background: color-mix(in srgb, var(--el-bg-color) 85%, transparent);
    backdrop-filter: blur(12px);
    border: 1px solid var(--el-border-color-light);
    border-radius: 8px;
    box-shadow:
      0 2px 12px rgb(0 0 0 / 8%),
      0 0 1px rgb(0 0 0 / 10%);

    .is-vertical & {
      flex-direction: column;
    }

    :not(.is-vertical) & {
      flex-direction: row;
    }
  }

  .toolbar-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    padding: 0;
    color: var(--el-text-color-regular);
    cursor: pointer;
    background: transparent;
    border: none;
    border-radius: 6px;
    transition: all 0.2s ease;

    &:hover:not(:disabled) {
      color: var(--el-color-primary);
      background: var(--el-fill-color-light);
    }

    &:active:not(:disabled) {
      background: var(--el-fill-color);
      transform: scale(0.95);
    }

    &:disabled {
      cursor: not-allowed;
      opacity: 0.4;
    }

    &.active {
      color: var(--el-color-primary);
      background: var(--el-color-primary-light-9);
    }
  }

  // 暗色主题已通过 CSS 变量自动适配，无需额外样式
</style>
