<!-- LogicFlow 流程图组件 -->
<template>
  <div ref="wrapperRef" class="art-logic-flow-wrapper" :class="{ 'is-fullscreen': isFullscreen }">
    <div
      ref="containerRef"
      class="art-logic-flow-container"
      :class="{ readonly: props.readonly }"
      :style="containerStyle"
    ></div>

    <!-- 工具栏 -->
    <LogicFlowToolbar
      v-if="showToolbar"
      :show-zoom-in="props.showZoomIn"
      :show-zoom-out="props.showZoomOut"
      :show-reset-zoom="props.showResetZoom"
      :show-fit-view="props.showFitView"
      :show-undo="props.showUndo"
      :show-redo="props.showRedo"
      :show-mini-map="props.showMiniMap"
      :show-fullscreen="props.showFullscreen"
      :show-export="props.showExport"
      :mini-map-visible="miniMapVisible"
      :is-fullscreen="isFullscreen"
      @zoom-in="handleZoomIn"
      @zoom-out="handleZoomOut"
      @reset-zoom="handleResetZoom"
      @fit-view="handleFitView"
      @undo="handleUndo"
      @redo="handleRedo"
      @toggle-minimap="handleToggleMiniMap"
      @toggle-fullscreen="handleToggleFullscreen"
      @export-image="handleExportImage"
    />
  </div>
</template>

<script setup lang="ts">
  import LogicFlow from '@logicflow/core'
  import '@logicflow/core/dist/index.css'
  import { MiniMap, Snapshot } from '@logicflow/extension'
  import '@logicflow/extension/lib/style/index.css'
  import { onMounted, onUnmounted, watch, nextTick } from 'vue'
  import { useWindowSize, useFullscreen } from '@vueuse/core'
  import { useSettingStore } from '@/store/modules/setting'
  import { storeToRefs } from 'pinia'
  import { SystemThemeEnum } from '@/enums/appEnum'
  import { ElMessage } from 'element-plus'
  import type {
    LogicFlowData,
    LogicFlowConfig,
    NodeClickEvent,
    EdgeClickEvent,
    DataChangeEvent
  } from './types'
  import { getDefaultConfig, getReadonlyConfig, getDarkThemeConfig } from './config'
  import { validateLogicFlowData, autoLayout } from './utils'
  import { registerGlassNode } from './custom-nodes'
  import { registerCustomEdge } from './custom-edges'
  import LogicFlowToolbar from './toolbar.vue'

  defineOptions({ name: 'ArtLogicFlow' })

  interface Props {
    /** 流程图数据 */
    data?: LogicFlowData
    /** LogicFlow 配置 */
    config?: LogicFlowConfig
    /** 是否只读模式 */
    readonly?: boolean
    /** 是否允许调整边（拖拽连接） */
    allowEdgeAdjustment?: boolean
    /** 容器高度 */
    height?: string | number
    /** 容器宽度 */
    width?: string | number
    /** 是否自适应容器大小 */
    autoResize?: boolean
    /** 是否显示工具栏 */
    showToolbar?: boolean
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
    /** 布局方向：'vertical' 从上到下，'horizontal' 从左到右 */
    layoutDirection?: 'vertical' | 'horizontal'
    /** 节点水平间距（用于布局） */
    nodeSpacingX?: number
    /** 节点垂直间距（用于布局） */
    nodeSpacingY?: number
    /** 自定义节点注册函数（可选） */
    nodeRegistrar?: (lf: LogicFlow) => void
  }

  const props = withDefaults(defineProps<Props>(), {
    data: () => ({ nodes: [], edges: [] }),
    config: () => ({}),
    readonly: false,
    allowEdgeAdjustment: true,
    height: '600px',
    width: '100%',
    autoResize: true,
    showToolbar: true,
    showZoomIn: true,
    showZoomOut: true,
    showResetZoom: true,
    showFitView: true,
    showUndo: true,
    showRedo: true,
    showMiniMap: true,
    showFullscreen: true,
    showExport: true,
    layoutDirection: undefined,
    nodeSpacingX: 250,
    nodeSpacingY: 120,
    nodeRegistrar: undefined
  })

  interface Emits {
    (e: 'node:click', event: NodeClickEvent): void
    (e: 'node:add', node: any): void
    (e: 'node:delete', node: any): void
    (e: 'node:dragstart', event: any): void
    (e: 'node:dragend', event: any): void
    (e: 'edge:click', event: EdgeClickEvent): void
    (e: 'edge:add', edge: any): void
    (e: 'edge:delete', edge: any): void
    (e: 'data:change', event: DataChangeEvent): void
  }

  const emit = defineEmits<Emits>()

  const wrapperRef = ref<HTMLDivElement>()
  const containerRef = ref<HTMLDivElement>()
  let lfInstance: LogicFlow | null = null
  const { width: windowWidth, height: windowHeight } = useWindowSize()
  const settingStore = useSettingStore()
  const { systemThemeType } = storeToRefs(settingStore)

  const miniMapVisible = ref(false)
  const { isFullscreen, toggle: toggleFullscreen } = useFullscreen(wrapperRef)

  // 历史记录栈
  const historyStack = ref<LogicFlowData[]>([])
  const historyIndex = ref(-1)
  const isHistoryAction = ref(false)
  // 是否已初始化（用于判断是否需要自动 fitView）
  const isInitialized = ref(false)

  // 容器样式
  const containerStyle = computed(() => {
    const style: Record<string, string> = {}
    if (props.height) {
      style.height = typeof props.height === 'number' ? `${props.height}px` : props.height
    }
    if (props.width) {
      style.width = typeof props.width === 'number' ? `${props.width}px` : props.width
    }
    return style
  })

  /**
   * 获取合并后的配置
   */
  const getMergedConfig = (): any => {
    let baseConfig: LogicFlowConfig

    if (props.readonly) {
      baseConfig = getReadonlyConfig()
    } else {
      baseConfig = getDefaultConfig()
    }

    // 应用主题
    if (systemThemeType.value === SystemThemeEnum.DARK) {
      baseConfig = { ...baseConfig, ...getDarkThemeConfig() }
    }

    // 合并用户配置，LogicFlow 需要 container 属性
    return {
      ...baseConfig,
      ...props.config,
      container: containerRef.value
    }
  }

  /**
   * 初始化 LogicFlow
   */
  const initLogicFlow = async () => {
    if (!containerRef.value) return

    await nextTick()

    const config = getMergedConfig()

    // 注册插件
    LogicFlow.use(Snapshot)
    LogicFlow.use(MiniMap)

    lfInstance = new LogicFlow(config)

    // 注册自定义节点
    if (props.nodeRegistrar) {
      props.nodeRegistrar(lfInstance)
    } else {
      registerGlassNode(lfInstance)
    }

    // 注册自定义边
    registerCustomEdge(lfInstance)

    // 注册事件监听
    setupEventListeners()

    // 渲染数据
    if (props.data && props.data.nodes.length > 0) {
      const validation = validateLogicFlowData(props.data)
      if (validation.valid) {
        // 如果指定了布局方向，先进行自动布局
        let dataToRender = props.data
        if (props.layoutDirection) {
          dataToRender = autoLayout(props.data, {
            direction: props.layoutDirection,
            nodeSpacingX: props.nodeSpacingX,
            nodeSpacingY: props.nodeSpacingY,
            startX: 100,
            startY: 100
          })
        }
        lfInstance.render(dataToRender)
        // 渲染后自动居中适应视图
        await nextTick()
        // 先调整画布大小，确保 lf-graph 填充父容器
        lfInstance.resize()
        // 然后居中适应视图
        lfInstance.fitView(50) // 50px 边距
        // 限制最大缩放比例为 0.8（80%），防止节点少时被过度放大
        const tm = lfInstance.graphModel.transformModel
        if (tm.SCALE_X > 0.8) {
          const { width, height } = lfInstance.graphModel
          const centerX = width / 2
          const centerY = height / 2
          const ratio = 0.8 / tm.SCALE_X
          tm.SCALE_X = 0.8
          tm.SCALE_Y = 0.8
          tm.TRANSLATE_X = centerX - (centerX - tm.TRANSLATE_X) * ratio
          tm.TRANSLATE_Y = centerY - (centerY - tm.TRANSLATE_Y) * ratio
        }
        // 初始化历史记录
        saveToHistory()
        // 标记为已初始化
        isInitialized.value = true
      } else {
        console.warn('[ArtLogicFlow] 数据验证失败:', validation.errors)
      }
    }
  }

  /**
   * 设置事件监听
   */
  const setupEventListeners = () => {
    if (!lfInstance) return

    // 节点 mousedown 事件（用于跟踪点击开始位置）
    lfInstance.on('node:mousedown', ({ e }: any) => {
      // 通过自定义事件传递 mousedown 信息
      if (e) {
        ;(e.target as any).__lfMouseDownPos = { x: e.clientX, y: e.clientY }
        ;(e.target as any).__lfMouseDownTime = Date.now()
      }
    })

    // 节点点击
    lfInstance.on('node:click', ({ data, e }: any) => {
      emit('node:click', { node: data, e })
    })

    // 节点添加
    lfInstance.on('node:add', ({ data }: any) => {
      emit('node:add', data)
      emitDataChange('add')
    })

    // 节点删除
    lfInstance.on('node:delete', ({ data }: any) => {
      emit('node:delete', data)
      emitDataChange('delete')
    })

    // 边点击
    lfInstance.on('edge:click', ({ data, e }: any) => {
      emit('edge:click', { edge: data, e })
    })

    // 边 hover 事件
    const currentLfInstance = lfInstance
    lfInstance.on('edge:mouseenter', ({ data }: any) => {
      if (currentLfInstance) {
        currentLfInstance.setProperties(data.id, { isHovered: true })
      }
    })

    lfInstance.on('edge:mouseleave', ({ data }: any) => {
      if (currentLfInstance) {
        currentLfInstance.setProperties(data.id, { isHovered: false })
      }
    })

    // 边添加
    lfInstance.on('edge:add', ({ data }: any) => {
      emit('edge:add', data)
      emitDataChange('add')
    })

    // 边删除
    lfInstance.on('edge:delete', ({ data }: any) => {
      emit('edge:delete', data)
      emitDataChange('delete')
    })

    // 节点移动
    lfInstance.on('node:mousemove', () => {
      emitDataChange('move')
    })

    // 节点拖拽开始
    lfInstance.on('node:dragstart', (data: any) => {
      emit('node:dragstart', data)
      // 添加全局 mouseup 监听，确保在容器外部也能结束拖拽
      document.addEventListener('mouseup', handleGlobalMouseUp, true)
    })

    // 节点拖拽结束
    lfInstance.on('node:dragend', (data: any) => {
      // 清除拖拽节点的 mousedown 位置信息，避免影响后续点击
      if (data && data.e && data.e.target) {
        delete (data.e.target as any).__lfMouseDownPos
        delete (data.e.target as any).__lfMouseDownTime
      }
      emit('node:dragend', data)
      // 移除全局 mouseup 监听
      document.removeEventListener('mouseup', handleGlobalMouseUp, true)
    })
  }

  /**
   * 处理全局 mouseup 事件，确保拖拽能正确结束
   */
  const handleGlobalMouseUp = () => {
    if (!lfInstance) return

    // 如果 LogicFlow 正在拖拽，强制结束拖拽
    const graphModel = (lfInstance as any).graphModel
    if (graphModel && graphModel.dragNode) {
      // 获取拖拽节点数据
      const dragNode = graphModel.dragNode
      const dragNodeData = dragNode ? { data: dragNode.getData(), e: null } : {}

      // 清除拖拽状态
      graphModel.dragNode = null

      // 触发拖拽结束事件 - 使用 LogicFlow 的事件系统
      // 这会触发我们注册的 node:dragend 监听器
      lfInstance.emit('node:dragend', dragNodeData)
      // 移除全局监听
      document.removeEventListener('mouseup', handleGlobalMouseUp, true)
    } else {
      // 如果没有拖拽状态，也移除监听（防止内存泄漏）
      document.removeEventListener('mouseup', handleGlobalMouseUp, true)
    }
  }

  /**
   * 触发数据变化事件
   */
  const emitDataChange = (type: DataChangeEvent['type']) => {
    if (!lfInstance) return
    const data = lfInstance.getGraphData() as any as LogicFlowData
    emit('data:change', { data, type })
    // 保存历史记录（非移动操作）
    if (type !== 'move') {
      saveToHistory()
    }
  }

  /**
   * 调整画布大小
   */
  const resizeLogicFlow = () => {
    if (!lfInstance || !containerRef.value) return
    nextTick(() => {
      lfInstance?.resize()
    })
  }

  // 监听窗口大小变化
  if (props.autoResize) {
    watch([windowWidth, windowHeight], () => {
      resizeLogicFlow()
    })
  }

  // 监听数据变化
  watch(
    () => props.data,
    async (newData) => {
      if (!lfInstance || !newData) return
      const validation = validateLogicFlowData(newData)
      if (validation.valid) {
        // 保存当前视图状态（缩放和偏移）
        const transformModel = lfInstance.graphModel.transformModel
        const savedTransform = {
          SCALE_X: transformModel.SCALE_X,
          SCALE_Y: transformModel.SCALE_Y,
          TRANSLATE_X: transformModel.TRANSLATE_X,
          TRANSLATE_Y: transformModel.TRANSLATE_Y
        }

        // 如果指定了布局方向，先进行自动布局
        let dataToRender = newData
        if (props.layoutDirection) {
          dataToRender = autoLayout(newData, {
            direction: props.layoutDirection,
            nodeSpacingX: props.nodeSpacingX,
            nodeSpacingY: props.nodeSpacingY,
            startX: 100,
            startY: 100
          })
        }
        lfInstance.render(dataToRender)

        // 如果已初始化，恢复之前的视图状态；否则自动居中
        await nextTick()
        // 先调整画布大小，确保 lf-graph 填充父容器（特别是容器尺寸变化时）
        lfInstance.resize()

        if (isInitialized.value) {
          // 恢复之前的视图状态，保持用户当前的缩放和位置
          const transformModel = lfInstance.graphModel.transformModel
          transformModel.SCALE_X = savedTransform.SCALE_X
          transformModel.SCALE_Y = savedTransform.SCALE_Y
          transformModel.TRANSLATE_X = savedTransform.TRANSLATE_X
          transformModel.TRANSLATE_Y = savedTransform.TRANSLATE_Y
        } else {
          // 首次渲染，自动居中
          lfInstance.fitView(50)
          // 限制最大缩放比例为 0.8（80%），防止节点少时被过度放大
          const tm = lfInstance.graphModel.transformModel
          if (tm.SCALE_X > 0.8) {
            // fitView 已经计算好了居中的偏移量，但缩放过大。
            // 按比例缩小缩放并调整偏移，保持节点在画布中心。
            const { width, height } = lfInstance.graphModel
            const centerX = width / 2
            const centerY = height / 2
            const ratio = 0.8 / tm.SCALE_X
            tm.SCALE_X = 0.8
            tm.SCALE_Y = 0.8
            // 以画布中心为基准缩放偏移量
            tm.TRANSLATE_X = centerX - (centerX - tm.TRANSLATE_X) * ratio
            tm.TRANSLATE_Y = centerY - (centerY - tm.TRANSLATE_Y) * ratio
          }
          isInitialized.value = true
        }
      }
    },
    { deep: true }
  )

  // 监听只读模式变化
  watch(
    () => props.readonly,
    () => {
      if (lfInstance) {
        lfInstance.updateEditConfig({
          isSilentMode: props.readonly,
          adjustNodePosition: !props.readonly,
          adjustEdgeStartAndEnd: !props.readonly && props.allowEdgeAdjustment
        })
      }
    }
  )

  // 监听布局方向变化
  watch(
    () => [props.layoutDirection, props.nodeSpacingX, props.nodeSpacingY],
    async () => {
      if (!lfInstance || !props.data || !props.layoutDirection) return
      const validation = validateLogicFlowData(props.data)
      if (validation.valid) {
        const dataToRender = autoLayout(props.data, {
          direction: props.layoutDirection,
          nodeSpacingX: props.nodeSpacingX,
          nodeSpacingY: props.nodeSpacingY,
          startX: 100,
          startY: 100
        })
        lfInstance.render(dataToRender)
        await nextTick()
        lfInstance.fitView(50)
        // 限制最大缩放比例为 0.8（80%）
        const tm = lfInstance.graphModel.transformModel
        if (tm.SCALE_X > 0.8) {
          const { width, height } = lfInstance.graphModel
          const centerX = width / 2
          const centerY = height / 2
          const ratio = 0.8 / tm.SCALE_X
          tm.SCALE_X = 0.8
          tm.SCALE_Y = 0.8
          tm.TRANSLATE_X = centerX - (centerX - tm.TRANSLATE_X) * ratio
          tm.TRANSLATE_Y = centerY - (centerY - tm.TRANSLATE_Y) * ratio
        }
      }
    }
  )

  // ============ 历史记录管理 ============

  /**
   * 保存到历史记录
   */
  const saveToHistory = () => {
    if (!lfInstance || isHistoryAction.value) return

    const data = lfInstance.getGraphData() as any as LogicFlowData
    // 删除当前位置之后的历史记录
    historyStack.value = historyStack.value.slice(0, historyIndex.value + 1)
    // 添加新记录
    historyStack.value.push(JSON.parse(JSON.stringify(data)))
    historyIndex.value = historyStack.value.length - 1
  }

  // ============ 工具栏事件处理 ============

  const handleZoomIn = () => {
    if (!lfInstance) return
    lfInstance.zoom(true)
  }

  const handleZoomOut = () => {
    if (!lfInstance) return
    lfInstance.zoom(false)
  }

  const handleResetZoom = () => {
    if (!lfInstance) return
    lfInstance.resetZoom()
  }

  const handleFitView = () => {
    if (!lfInstance) return
    lfInstance.fitView(50)
  }

  const handleUndo = () => {
    if (!lfInstance || historyIndex.value <= 0) return

    isHistoryAction.value = true
    historyIndex.value--
    const data = historyStack.value[historyIndex.value]
    lfInstance.render(data)
    nextTick(() => {
      isHistoryAction.value = false
    })
  }

  const handleRedo = () => {
    if (!lfInstance || historyIndex.value >= historyStack.value.length - 1) return

    isHistoryAction.value = true
    historyIndex.value++
    const data = historyStack.value[historyIndex.value]
    lfInstance.render(data)
    nextTick(() => {
      isHistoryAction.value = false
    })
  }

  const handleToggleMiniMap = () => {
    miniMapVisible.value = !miniMapVisible.value
    if (lfInstance) {
      const extension = lfInstance.extension as any
      if (extension?.miniMap) {
        if (miniMapVisible.value) {
          extension.miniMap.show()
        } else {
          extension.miniMap.hide()
        }
      }
    }
  }

  const handleToggleFullscreen = () => {
    toggleFullscreen()
  }

  const handleExportImage = async () => {
    if (!lfInstance) return

    try {
      const extension = lfInstance.extension as any
      if (extension?.snapshot) {
        extension.snapshot.getSnapshot('logicflow-export', 'png')
        ElMessage.success('图片导出成功')
      } else {
        // 备用方案：使用 getSnapshot 方法
        const snapshot = (lfInstance as any).getSnapshot?.()
        if (snapshot) {
          const link = document.createElement('a')
          link.download = 'logicflow-export.png'
          link.href = snapshot
          link.click()
          ElMessage.success('图片导出成功')
        }
      }
    } catch (error) {
      console.error('导出图片失败:', error)
      ElMessage.error('导出图片失败')
    }
  }

  // 暴露方法
  defineExpose({
    /**
     * 获取 LogicFlow 实例
     */
    getInstance: () => lfInstance,
    /**
     * 获取当前流程图数据
     */
    getData: (): LogicFlowData | null => {
      if (!lfInstance) return null
      const data = lfInstance.getGraphData() as any
      return data as LogicFlowData
    },
    /**
     * 设置流程图数据
     */
    setData: (data: LogicFlowData) => {
      if (!lfInstance) return
      const validation = validateLogicFlowData(data)
      if (validation.valid) {
        lfInstance.render(data)
      } else {
        console.error('[ArtLogicFlow] 数据验证失败:', validation.errors)
      }
    },
    /**
     * 添加节点
     */
    addNode: (node: any) => {
      if (!lfInstance) return
      lfInstance.addNode(node)
    },
    /**
     * 添加边
     */
    addEdge: (edge: any) => {
      if (!lfInstance) return
      lfInstance.addEdge(edge)
    },
    /**
     * 删除节点
     */
    deleteNode: (nodeId: string) => {
      if (!lfInstance) return
      lfInstance.deleteNode(nodeId)
    },
    /**
     * 删除边
     */
    deleteEdge: (edgeId: string) => {
      if (!lfInstance) return
      lfInstance.deleteEdge(edgeId)
    },
    /**
     * 放大
     */
    zoomIn: () => {
      if (!lfInstance) return
      lfInstance.zoom(true)
    },
    /**
     * 缩小
     */
    zoomOut: () => {
      if (!lfInstance) return
      lfInstance.zoom(false)
    },
    /**
     * 重置缩放
     */
    resetZoom: () => {
      if (!lfInstance) return
      lfInstance.resetZoom()
    },
    /**
     * 适应视图
     * @param padding 边距（像素）
     */
    fitView: (padding?: number) => {
      if (!lfInstance) return
      lfInstance.fitView(padding)
    },
    /**
     * 清空画布
     */
    clear: () => {
      if (!lfInstance) return
      lfInstance.clearData()
    },
    /**
     * 调整大小
     */
    resize: () => {
      resizeLogicFlow()
    },
    /**
     * 重置初始化状态，使下次 data 变化时走 fitView 分支而非恢复旧 transform
     */
    resetInitState: () => {
      isInitialized.value = false
    }
  })

  onMounted(() => {
    initLogicFlow()
  })

  onUnmounted(() => {
    // 清理全局事件监听器
    document.removeEventListener('mouseup', handleGlobalMouseUp, true)

    if (lfInstance) {
      lfInstance.destroy()
      lfInstance = null
    }
  })
</script>

<style scoped lang="scss">
  .art-logic-flow-wrapper {
    position: relative;
    width: 100%;
    height: 100%;

    // 全屏模式
    &.is-fullscreen {
      position: fixed;
      inset: 0;
      z-index: 9999;
      background: #fff;

      .art-logic-flow-container {
        height: 100% !important;
        border-radius: 0;
      }
    }
  }

  .art-logic-flow-container {
    position: relative;
    width: 100%;
    height: 100%;
    overflow: hidden;
    background-color: var(--el-fill-color-light) !important;
    border-radius: 4px;

    // 强制 LogicFlow 所有内部元素透明
    :deep(.lf-container),
    :deep(.lf-canvas),
    :deep(.lf-canvas-overlay),
    :deep(.lf-background),
    :deep(.lf-grid),
    :deep(lf-graph),
    :deep(svg) {
      background: transparent !important;
      background-color: transparent !important;
    }

    // 确保 lf-graph 填充父容器
    :deep(.lf-graph) {
      width: 100% !important;
      height: 100% !important;
    }

    // 隐藏网格 SVG
    :deep(.lf-grid) {
      display: none !important;
    }

    // 节点样式适配
    :deep(.lf-node) {
      cursor: pointer;

      // 强制移除 rect 的默认 stroke 和 fill
      rect {
        fill: transparent !important;
        stroke: none !important;
      }

      // 移除默认的 hover 效果
      &:hover {
        rect {
          filter: none;
          stroke: none !important;
          transform: none;
        }
      }
    }

    // 隐藏 LogicFlow 默认的选中边框（虚线框）
    :deep(.lf-node-select-rect),
    :deep(.lf-outline),
    :deep(.lf-node-anchor),
    :deep(.lf-anchor),
    :deep(.lf-resize-control),
    :deep(.lf-rotate-control) {
      display: none !important;
      visibility: hidden !important;
      opacity: 0 !important;
    }

    // 文本样式适配
    :deep(.lf-node-text) {
      display: none !important;
    }

    // 边样式适配
    :deep(.lf-edge) {
      path {
        transition: all 0.3s ease;
      }

      &:hover {
        path {
          stroke: var(--el-color-primary);
          stroke-width: 2px;
        }
      }
    }

    // 隐藏边的锚点
    :deep(.lf-edge-anchor),
    :deep(.lf-adjust-point) {
      display: none !important;
    }

    // 只读模式样式
    &.readonly {
      :deep(.lf-node) {
        cursor: default;
      }
    }
  }

  // 暗色主题适配
  :global(.dark) {
    .art-logic-flow-wrapper.is-fullscreen {
      background: #1a1a1a;
    }

    .art-logic-flow-container {
      background-image: none !important;
    }
  }
</style>
