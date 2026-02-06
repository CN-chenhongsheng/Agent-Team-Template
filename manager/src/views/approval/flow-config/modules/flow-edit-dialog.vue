<!-- 流程编辑弹窗 - 可视化流程编辑器 -->
<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogType === 'add' ? '新建审批流程' : '编辑审批流程'"
    class="flow-edit-dialog"
    width="90%"
    top="5vmin"
    :close-on-click-modal="false"
    destroy-on-close
    @closed="handleClosed"
    @opened="handleDialogOpened"
  >
    <div class="dialog-content">
      <!-- 可视化流程编辑区域 -->
      <div class="flow-canvas-wrapper">
        <div class="canvas-container" ref="canvasContainerRef">
          <!-- LogicFlow 画布 -->
          <ArtLogicFlow
            ref="logicFlowRef"
            :data="logicFlowData"
            :readonly="false"
            :show-toolbar="false"
            :nodeRegistrar="registerApprovalNodes"
            :auto-resize="true"
            layout-direction="vertical"
            :node-spacing-x="200"
            :node-spacing-y="150"
            height="100%"
            @node:click="handleNodeClick"
            @edge:click="handleEdgeClick"
            @node:dragstart="handleNodeDragStart"
            @node:dragend="handleNodeDragEnd"
          />

          <!-- LogicFlow 工具栏 -->
          <LogicFlowToolbar
            :show-zoom-in="true"
            :show-zoom-out="true"
            :show-reset-zoom="true"
            :show-fit-view="true"
            :show-undo="false"
            :show-redo="false"
            :show-mini-map="true"
            :show-fullscreen="true"
            :show-export="true"
            :mini-map-visible="miniMapVisible"
            :is-fullscreen="isFullscreen"
            @zoom-in="handleZoomIn"
            @zoom-out="handleZoomOut"
            @reset-zoom="handleResetZoom"
            @fit-view="handleFitView"
            @toggle-minimap="handleToggleMiniMap"
            @toggle-fullscreen="handleToggleFullscreen"
            @export-image="handleExportImage"
          />

          <!-- 节点编辑卡片 -->
          <NodeEditorCard
            ref="nodeEditorRef"
            v-model:visible="nodeEditorVisible"
            :node-data="selectedNodeData"
            :node-type="selectedNodeType"
            :position="nodeEditorPosition"
            :flow-data="formData"
            :business-type-options="businessTypeOptionsList"
            :dialog-type="dialogType"
            @update="handleNodeUpdate"
            @delete="handleNodeDelete"
            @select-approver="openAssigneeSelect"
            @update-flow="handleFlowInfoUpdate"
            @update:position="handleNodeEditorPositionUpdate"
          />
        </div>
      </div>
    </div>

    <template #footer>
      <ElButton @click="dialogVisible = false" v-ripple>取消</ElButton>
      <ElButton type="primary" :loading="submitLoading" @click="handleSubmit" v-ripple>
        保存流程
      </ElButton>
    </template>

    <!-- 审批人选择弹窗 -->
    <AssigneeSelectDialog
      v-model="assigneeDialogVisible"
      :selected-ids="currentSelectedIds"
      @confirm="handleAssigneeConfirm"
    />
  </ElDialog>
</template>

<script setup lang="ts">
  import { ref, reactive, computed, watch, nextTick, onUnmounted, onMounted } from 'vue'
  import { useFullscreen } from '@vueuse/core'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import {
    fetchAddFlow,
    fetchUpdateFlow,
    fetchGetFlowDetail,
    type ApprovalFlow,
    type ApprovalNode,
    type ApprovalAssignee
  } from '@/api/approval-manage'
  import { registerApprovalNodes } from '@/components/core/charts/art-logic-flow/approval-node'
  import ArtLogicFlow from '@/components/core/charts/art-logic-flow/index.vue'
  import LogicFlowToolbar from '@/components/core/charts/art-logic-flow/toolbar.vue'
  import { useBusinessType } from '@/hooks'
  import {
    approvalNodesToLogicFlow,
    createNewApprovalNode,
    reorderNodes,
    syncNodePositions
  } from './flow-data-utils'
  import type { LogicFlowData } from '@/components/core/charts/art-logic-flow/types'
  import NodeEditorCard from './node-editor-card.vue'
  import AssigneeSelectDialog from './assignee-select-dialog.vue'

  const props = defineProps<{
    modelValue: boolean
    dialogType: 'add' | 'edit'
    flowData: ApprovalFlow | null
  }>()

  const emit = defineEmits<{
    'update:modelValue': [value: boolean]
    success: []
  }>()

  const dialogVisible = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
  })

  // Refs
  const canvasContainerRef = ref<HTMLElement>()
  const logicFlowRef = ref<InstanceType<typeof ArtLogicFlow>>()
  const nodeEditorRef = ref<InstanceType<typeof NodeEditorCard>>()

  // State
  const submitLoading = ref(false)
  const activeCollapse = ref(['info'])

  // 工具栏状态
  const miniMapVisible = ref(false)
  const { isFullscreen, toggle: toggleFullscreen } = useFullscreen(canvasContainerRef)

  // 节点编辑器状态
  const nodeEditorVisible = ref(false)
  const selectedNodeData = ref<ApprovalNode | null>(null)
  const selectedNodeId = ref<string | null>(null)
  const selectedNodeType = ref<'start' | 'approval' | 'end' | null>(null)
  const nodeEditorPosition = ref({ x: 0, y: 0 })

  // 拖拽状态跟踪
  const isDragging = ref(false)
  const dragStartPos = ref({ x: 0, y: 0 })

  // 审批人选择状态
  const assigneeDialogVisible = ref(false)
  const currentSelectedIds = ref<number[]>([])

  // 业务类型（从字典获取）
  const { businessTypeOptions, fetchBusinessTypes } = useBusinessType()

  // 组件挂载时获取业务类型
  onMounted(() => {
    fetchBusinessTypes()
  })

  // 转换为可变数组，用于传递给组件
  const businessTypeOptionsList = computed(() => [...businessTypeOptions.value])

  // 初始表单数据
  const initialFormData = (): ApprovalFlow => ({
    flowName: '',
    flowCode: '',
    businessType: '',
    description: '',
    status: 1,
    nodes: []
  })

  const formData = reactive<ApprovalFlow>(initialFormData())

  // 用于 LogicFlow 组件的数据 - 改为 ref，手动控制更新
  const logicFlowData = ref<LogicFlowData>({ nodes: [], edges: [] })

  // 手动更新 logicFlowData 的函数
  const regenerateFlowData = () => {
    logicFlowData.value = approvalNodesToLogicFlow(formData.nodes || [], selectedNodeId.value, {
      flowName: formData.flowName,
      businessType: formData.businessType,
      status: formData.status
    })
  }

  // 键盘事件处理函数（用于删除节点）
  let keyDownHandler: ((e: KeyboardEvent) => void) | null = null

  // 节点删除按钮点击事件处理函数
  let handleNodeDeleteFromButton: ((e: Event) => void) | null = null

  // 获取 LogicFlow 实例
  const getLogicFlowInstance = () => logicFlowRef.value?.getInstance()

  // 备用机制：全局 mouseup 监听器，确保拖拽状态被清除
  const handleGlobalMouseUpForDrag = () => {
    if (isDragging.value) {
      isDragging.value = false
      dragStartPos.value = { x: 0, y: 0 }
      // 移除监听器
      document.removeEventListener('mouseup', handleGlobalMouseUpForDrag, true)
    }
  }

  // 处理节点拖拽开始
  const handleNodeDragStart = (event: any) => {
    // 记录拖拽开始状态和位置
    isDragging.value = true
    if (event.e) {
      dragStartPos.value = { x: event.e.clientX, y: event.e.clientY }
    }
    // 添加全局 mouseup 监听器作为备用机制
    document.addEventListener('mouseup', handleGlobalMouseUpForDrag, true)
  }

  // 处理节点点击
  const handleNodeClick = (event: any) => {
    const data = event.node
    const clickEvent = event.e

    // 延迟检查拖拽状态（因为 dragstart 可能在 click 之后触发）
    // 如果正在拖拽，不显示卡片
    setTimeout(() => {
      // 如果正在拖拽，不显示卡片
      if (isDragging.value) {
        return
      }

      // 拖拽已结束，检查是否是拖拽：通过检查 mousedown 和 click 之间的移动距离和时间
      // 只有在拖拽未开始时才检查移动距离，避免拖拽后的点击被误判
      if (clickEvent && clickEvent.target) {
        const mouseDownPos = (clickEvent.target as any).__lfMouseDownPos
        const mouseDownTime = (clickEvent.target as any).__lfMouseDownTime

        if (mouseDownPos && mouseDownTime) {
          // 检查时间间隔，如果超过 300ms，认为是新的点击，不是拖拽
          const timeDiff = Date.now() - mouseDownTime
          if (timeDiff < 300) {
            const moveDistance = Math.sqrt(
              Math.pow(clickEvent.clientX - mouseDownPos.x, 2) +
                Math.pow(clickEvent.clientY - mouseDownPos.y, 2)
            )
            // 如果移动距离超过 5px，认为是拖拽，不显示卡片
            if (moveDistance > 5) {
              return
            }
          }
        }
      }

      // 确认是点击后，清除 mousedown 位置信息，避免影响后续点击
      if (clickEvent && clickEvent.target) {
        delete (clickEvent.target as any).__lfMouseDownPos
        delete (clickEvent.target as any).__lfMouseDownTime
      }

      // 继续处理点击事件
      const { type, id, x, y, properties } = data

      // 处理开始节点
      if (type === 'start-node') {
        selectedNodeId.value = id
        selectedNodeType.value = 'start'
        selectedNodeData.value = null

        // 计算编辑器位置
        const canvasRect = canvasContainerRef.value?.getBoundingClientRect()
        const lfInstance = getLogicFlowInstance()
        if (canvasRect && lfInstance) {
          const { transformModel } = lfInstance.graphModel
          const { SCALE_X, SCALE_Y, TRANSLATE_X, TRANSLATE_Y } = transformModel
          const screenX = x * SCALE_X + TRANSLATE_X
          const screenY = y * SCALE_Y + TRANSLATE_Y
          nodeEditorPosition.value = { x: screenX, y: screenY }
        }

        nodeEditorVisible.value = true
        updateNodeSelection()
        return
      }

      // 处理结束节点（只显示选中状态，不弹出编辑器）
      if (type === 'end-node') {
        selectedNodeId.value = id
        selectedNodeType.value = 'end'
        selectedNodeData.value = null
        nodeEditorVisible.value = false
        updateNodeSelection()
        return
      }

      // 处理审批节点
      if (type !== 'approval-node') {
        clearSelection()
        return
      }

      // 更新选中状态
      selectedNodeId.value = id
      selectedNodeType.value = 'approval'

      // 找到对应的节点数据
      const nodeIndex = formData.nodes?.findIndex((n) => `approval-${n.id}` === id)
      if (nodeIndex !== undefined && nodeIndex >= 0 && formData.nodes) {
        selectedNodeData.value = { ...formData.nodes[nodeIndex] }
      } else {
        // 新节点（还没有ID）
        selectedNodeData.value = {
          nodeName: properties?.nodeName || '',
          nodeOrder: properties?.nodeOrder || 1,
          nodeType: properties?.nodeType || 1,
          rejectAction: properties?.rejectAction || 1,
          assignees: properties?.assignees || []
        }
      }

      // 计算编辑器位置（画布坐标转换为容器坐标）
      const canvasRect = canvasContainerRef.value?.getBoundingClientRect()
      const lfInstance = getLogicFlowInstance()
      if (canvasRect && lfInstance) {
        const { transformModel } = lfInstance.graphModel
        const { SCALE_X, SCALE_Y, TRANSLATE_X, TRANSLATE_Y } = transformModel

        // 将 LogicFlow 坐标转换为屏幕坐标
        const screenX = x * SCALE_X + TRANSLATE_X
        const screenY = y * SCALE_Y + TRANSLATE_Y

        nodeEditorPosition.value = {
          x: screenX,
          y: screenY
        }
      }

      nodeEditorVisible.value = true

      // 更新节点选中状态
      updateNodeSelection()
    }, 50)
  }

  // 更新节点选中状态
  const updateNodeSelection = () => {
    const lfInstance = getLogicFlowInstance()
    if (!lfInstance) return

    // 更新所有节点的选中状态
    const graphData = lfInstance.getGraphData() as any
    graphData.nodes.forEach((node: any) => {
      const isSelected = node.id === selectedNodeId.value
      lfInstance?.setProperties(node.id, { isSelected })
    })
  }

  // 清除选中状态
  const clearSelection = () => {
    selectedNodeId.value = null
    selectedNodeData.value = null
    selectedNodeType.value = null
    nodeEditorVisible.value = false
    updateNodeSelection()
  }

  // 处理流程基本信息更新（从开始节点编辑）
  const handleFlowInfoUpdate = (flowInfo: {
    flowName?: string
    flowCode?: string
    businessType?: string
    status?: number
    description?: string
  }) => {
    if (flowInfo.flowName !== undefined) {
      formData.flowName = flowInfo.flowName
    }
    if (flowInfo.flowCode !== undefined) {
      formData.flowCode = flowInfo.flowCode
    }
    if (flowInfo.businessType !== undefined) {
      formData.businessType = flowInfo.businessType
    }
    if (flowInfo.status !== undefined) {
      formData.status = flowInfo.status
    }
    if (flowInfo.description !== undefined) {
      formData.description = flowInfo.description
    }

    // 更新开始节点的显示
    const lfInstance = getLogicFlowInstance()
    if (lfInstance) {
      lfInstance.setProperties('start-node', {
        flowName: formData.flowName,
        businessType: formData.businessType
      })
    }
  }

  // 处理节点编辑器位置更新
  const handleNodeEditorPositionUpdate = (newPosition: { x: number; y: number }) => {
    nodeEditorPosition.value = newPosition
  }

  // 处理节点更新
  const handleNodeUpdate = (updatedNode: ApprovalNode) => {
    if (!formData.nodes) return

    // 查找并更新节点 - 简化逻辑，直接通过 LogicFlow 节点的 nodeOrder 查找
    // 因为新节点没有 id，必须使用 nodeOrder 来定位
    const sortedNodes = [...formData.nodes].sort((a, b) => a.nodeOrder - b.nodeOrder)

    let index = -1

    // 优先通过 LogicFlow 节点属性中的 nodeOrder 查找（最可靠的方式）
    const lfInstance = getLogicFlowInstance()
    if (selectedNodeId.value && lfInstance) {
      try {
        const lfNodeModel = lfInstance.getNodeModelById(selectedNodeId.value)
        if (lfNodeModel) {
          const lfProperties = (lfNodeModel as any).properties || {}
          const lfNodeOrder = lfProperties.nodeOrder
          if (lfNodeOrder !== undefined) {
            index = sortedNodes.findIndex((n) => n.nodeOrder === lfNodeOrder)
          }
        }
      } catch (error) {
        console.warn('[FlowEditDialog] 无法从 LogicFlow 获取节点属性:', error)
      }
    }

    // 备用方案：如果节点有 id，通过 id 查找
    if (index < 0 && updatedNode.id) {
      index = sortedNodes.findIndex((n) => n.id && n.id === updatedNode.id)
    }

    if (index >= 0) {
      // 在排序后的数组中找到了节点
      const sortedNode = sortedNodes[index]

      // 如果节点有 id，使用 id 在原数组中精确匹配
      if (sortedNode.id) {
        const actualIndex = formData.nodes.findIndex((n) => n.id === sortedNode.id)
        if (actualIndex >= 0) {
          const originalNodeOrder = formData.nodes[actualIndex].nodeOrder
          formData.nodes[actualIndex] = {
            ...formData.nodes[actualIndex],
            ...updatedNode,
            nodeOrder: originalNodeOrder
          }
        }
      } else {
        // 如果没有 id（新节点），直接更新排序后的数组，然后替换原数组
        // 这样可以确保节点顺序正确
        const originalNodeOrder = sortedNode.nodeOrder
        sortedNodes[index] = { ...sortedNode, ...updatedNode, nodeOrder: originalNodeOrder }
        formData.nodes = sortedNodes
      }
    } else {
      // 备用方法：在排序后的数组中通过 updatedNode.nodeOrder 查找
      const sortedIndex = sortedNodes.findIndex(
        (n) => !n.id && n.nodeOrder === updatedNode.nodeOrder
      )
      if (sortedIndex >= 0) {
        const originalNodeOrder = sortedNodes[sortedIndex].nodeOrder
        sortedNodes[sortedIndex] = {
          ...sortedNodes[sortedIndex],
          ...updatedNode,
          nodeOrder: originalNodeOrder
        }
        formData.nodes = sortedNodes
      }
    }

    // 更新 LogicFlow 节点属性
    if (selectedNodeId.value && lfInstance) {
      lfInstance.setProperties(selectedNodeId.value, {
        nodeName: updatedNode.nodeName,
        nodeType: updatedNode.nodeType,
        rejectAction: updatedNode.rejectAction,
        assignees: updatedNode.assignees,
        assigneeCount: updatedNode.assignees?.length || 0
      })
    }
  }

  // 处理节点删除
  const handleNodeDelete = async () => {
    // 开始节点和结束节点不能删除
    if (selectedNodeType.value === 'start' || selectedNodeType.value === 'end') {
      ElMessage.warning('开始节点和结束节点不能删除')
      return
    }

    if (!formData.nodes || !selectedNodeId.value || selectedNodeType.value !== 'approval') return

    // 找到要删除的节点 - 尝试多种匹配方式
    let index = -1

    // 方法1: 通过 LogicFlow 实例查找节点属性（优先使用此方法）
    // 从 LogicFlow 节点获取 nodeOrder，因为新节点可能没有 id
    const lfInstance = getLogicFlowInstance()
    if (lfInstance && selectedNodeId.value) {
      try {
        const lfNodeModel = lfInstance.getNodeModelById(selectedNodeId.value)
        if (lfNodeModel) {
          const lfProperties = (lfNodeModel as any).properties || {}

          // 优先使用 originalId（如果节点已保存到数据库）
          const lfOriginalId = lfProperties.originalId
          if (lfOriginalId) {
            index = formData.nodes.findIndex((n) => n.id === lfOriginalId)
          }

          // 如果通过 originalId 找不到，使用 nodeOrder（适用于新创建的节点）
          if (index < 0 && lfProperties.nodeOrder !== undefined) {
            index = formData.nodes.findIndex((n) => n.nodeOrder === lfProperties.nodeOrder)
          }
        }
      } catch (error) {
        console.warn('[FlowEditDialog] 无法从 LogicFlow 获取节点属性:', error)
      }
    }

    // 方法2: 通过 LogicFlow ID 格式匹配 (approval-{id}) - 仅当节点有 id 时
    if (index < 0 && selectedNodeId.value) {
      // 如果 selectedNodeId 是 'approval-{id}' 格式，提取 id
      if (selectedNodeId.value.startsWith('approval-')) {
        const extractedId = selectedNodeId.value.replace('approval-', '')
        index = formData.nodes.findIndex((n) => n.id && String(n.id) === extractedId)
      }
    }

    // 方法3: 通过节点顺序匹配（备用方法，用于新创建的节点）
    if (index < 0 && selectedNodeData.value?.nodeOrder !== undefined) {
      const nodeOrder = selectedNodeData.value.nodeOrder
      index = formData.nodes.findIndex((n) => n.nodeOrder === nodeOrder)
    }

    if (index < 0) {
      console.warn('[FlowEditDialog] 无法找到要删除的节点，selectedNodeId:', selectedNodeId.value)
      return
    }

    const nodeName = formData.nodes[index]?.nodeName || '节点'

    try {
      // 确认删除
      await ElMessageBox.confirm(`确定要删除节点 "${nodeName}" 吗？`, '删除节点', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      })

      // 执行删除
      formData.nodes.splice(index, 1)
      formData.nodes = reorderNodes(formData.nodes)

      clearSelection()

      // 删除后需要重新生成 flowData
      regenerateFlowData()

      ElMessage.success('节点已删除')
    } catch {
      // 用户取消删除
    }
  }

  // 处理边（连接线）点击 - 在边中间添加节点
  // 重构后的简化逻辑：直接通过 nodeOrder 计算插入位置
  const handleEdgeClick = (event: any) => {
    const edgeData = event.edge
    const lfInstance = getLogicFlowInstance()
    if (!lfInstance || !formData.nodes) return

    const { sourceNodeId, targetNodeId } = edgeData

    // 步骤0：先同步当前所有节点的位置（关键！避免添加节点时丢失已拖拽的位置）
    const currentLfData = lfInstance.getGraphData() as LogicFlowData
    formData.nodes = syncNodePositions(formData.nodes, currentLfData)

    // 步骤1：获取源节点和目标节点的 nodeOrder
    let sourceOrder = 0 // 开始节点的 order 视为 0
    let targetOrder = 0

    // 获取源节点的 nodeOrder
    if (sourceNodeId === 'start-node') {
      sourceOrder = 0
    } else {
      try {
        const sourceModel = lfInstance.getNodeModelById(sourceNodeId)
        if (sourceModel) {
          sourceOrder = (sourceModel as any).properties?.nodeOrder || 0
        }
      } catch (error) {
        console.warn('[handleEdgeClick] 无法获取源节点属性:', error)
      }
    }

    // 获取目标节点的 nodeOrder
    if (targetNodeId === 'end-node') {
      // 结束节点的 order 视为 最大nodeOrder + 1
      const maxOrder =
        formData.nodes.length > 0 ? Math.max(...formData.nodes.map((n) => n.nodeOrder)) : 0
      targetOrder = maxOrder + 1
    } else {
      try {
        const targetModel = lfInstance.getNodeModelById(targetNodeId)
        if (targetModel) {
          targetOrder = (targetModel as any).properties?.nodeOrder || 0
        }
      } catch (error) {
        console.warn('[handleEdgeClick] 无法获取目标节点属性:', error)
      }
    }

    // 步骤2：计算新节点的 nodeOrder（在源和目标之间）
    const newNodeOrder = (sourceOrder + targetOrder) / 2

    // 获取源节点和目标节点的数据，计算新节点的位置（在边的中点）
    let sourceNode, targetNode
    try {
      sourceNode = lfInstance.getNodeModelById(sourceNodeId)
      targetNode = lfInstance.getNodeModelById(targetNodeId)
    } catch (error) {
      console.warn('[handleEdgeClick] 无法获取节点模型:', error)
      return
    }

    // 获取节点数据
    const sourceData = (sourceNode as any).getData()
    const targetData = (targetNode as any).getData()

    // 计算中点位置
    const newX = (sourceData.x + targetData.x) / 2
    const newY = (sourceData.y + targetData.y) / 2

    // 步骤3：创建新节点并添加到数组（带位置）
    const newNode = createNewApprovalNode(newNodeOrder, newX, newY)
    formData.nodes.push(newNode)

    // 步骤4：按 nodeOrder 排序
    formData.nodes.sort((a, b) => a.nodeOrder - b.nodeOrder)

    // 步骤5：重新分配整数 nodeOrder (1, 2, 3...)
    formData.nodes = reorderNodes(formData.nodes)

    // 步骤6：清除选中状态
    clearSelection()

    // 重新生成 flowData
    regenerateFlowData()

    ElMessage.success('节点已添加，请点击节点进行编辑')
  }

  /**
   * 处理节点拖拽结束事件
   * 将拖拽后的位置同步到 formData.nodes
   */
  const handleNodeDragEnd = (event?: any) => {
    // 清除拖拽状态
    isDragging.value = false
    dragStartPos.value = { x: 0, y: 0 }
    // 移除备用监听器
    document.removeEventListener('mouseup', handleGlobalMouseUpForDrag, true)

    // 清除 mousedown 位置信息，避免影响后续点击
    // 注意：这个清除已经在 art-logic-flow/index.vue 中处理了
    // 这里保留是为了兼容性，如果 event 中有 target，也清除一下
    if (event && event.e && event.e.target) {
      delete (event.e.target as any).__lfMouseDownPos
      delete (event.e.target as any).__lfMouseDownTime
    }

    const lfInstance = getLogicFlowInstance()
    if (!lfInstance || !formData.nodes) return

    // 获取当前 LogicFlow 数据（包含最新位置）
    const currentLfData = lfInstance.getGraphData() as LogicFlowData

    // 同步位置到 formData.nodes
    formData.nodes = syncNodePositions(formData.nodes, currentLfData)

    console.log('[DragEnd] 节点位置已同步到 formData.nodes')
  }

  // ============ 工具栏功能处理函数 ============

  // 放大
  const handleZoomIn = () => {
    logicFlowRef.value?.zoomIn()
  }

  // 缩小
  const handleZoomOut = () => {
    logicFlowRef.value?.zoomOut()
  }

  // 重置缩放
  const handleResetZoom = () => {
    logicFlowRef.value?.resetZoom()
  }

  // 适应视图
  const handleFitView = () => {
    logicFlowRef.value?.fitView(50)
  }

  // 切换小地图
  const handleToggleMiniMap = () => {
    const lfInstance = getLogicFlowInstance()
    if (!lfInstance) return
    miniMapVisible.value = !miniMapVisible.value
    const extension = (lfInstance as any).extension
    if (extension?.miniMap) {
      if (miniMapVisible.value) {
        extension.miniMap.show()
      } else {
        extension.miniMap.hide()
      }
    }
  }

  // 切换全屏
  const handleToggleFullscreen = () => {
    toggleFullscreen()
  }

  // 导出图片
  const handleExportImage = async () => {
    const lfInstance = getLogicFlowInstance()
    if (!lfInstance) return

    try {
      const extension = (lfInstance as any).extension
      if (extension?.snapshot) {
        extension.snapshot.getSnapshot('approval-flow', 'png')
        ElMessage.success('图片导出成功')
      } else {
        // 备用方案：使用 getSnapshot 方法
        const snapshot = (lfInstance as any).getSnapshot?.()
        if (snapshot) {
          const link = document.createElement('a')
          link.download = 'approval-flow.png'
          link.href = snapshot
          link.click()
          ElMessage.success('图片导出成功')
        } else {
          ElMessage.warning('导出功能暂不可用')
        }
      }
    } catch (error) {
      console.error('导出图片失败:', error)
      ElMessage.error('导出图片失败')
    }
  }

  // 打开审批人选择
  const openAssigneeSelect = () => {
    // 获取当前已选中的用户ID列表（只保留用户类型）
    if (selectedNodeData.value?.assignees) {
      currentSelectedIds.value = selectedNodeData.value.assignees
        .filter((a) => a.assigneeType === 2) // 只保留用户类型
        .map((a) => a.assigneeId)
    } else {
      currentSelectedIds.value = []
    }

    assigneeDialogVisible.value = true
  }

  // 处理审批人选择确认
  const handleAssigneeConfirm = (selectedUsers: Array<{ id: number; name: string }>) => {
    if (!selectedNodeData.value) return

    // 只保存用户类型的审批人
    const newAssignees: ApprovalAssignee[] = selectedUsers.map((user) => ({
      assigneeType: 2, // 固定为用户类型
      assigneeId: user.id,
      assigneeName: user.name
    }))

    // 更新节点编辑器
    if (nodeEditorRef.value) {
      nodeEditorRef.value.updateAssignees(newAssignees)
    }
  }

  // 监听弹窗打开
  watch(
    () => props.modelValue,
    async (val) => {
      if (val) {
        // 重置状态
        clearSelection()
        activeCollapse.value = ['info']

        if (props.dialogType === 'edit' && props.flowData?.id) {
          // 加载流程详情
          try {
            const res = await fetchGetFlowDetail(props.flowData.id)
            Object.assign(formData, res)
          } catch (error) {
            console.error('加载流程详情失败:', error)
          }
        } else {
          Object.assign(formData, initialFormData())
        }

        // 数据加载后重新生成 flowData
        regenerateFlowData()

        // 居中操作将在 @opened 事件中处理，确保弹窗完全打开后再执行
      }
    }
  )

  // 弹窗打开完成后的处理
  const handleDialogOpened = async () => {
    // 等待画布完全渲染后再居中
    await nextTick()
    await new Promise((resolve) => setTimeout(resolve, 200))
    await nextTick()

    // 使用组件暴露的 fitView 方法，与工具栏按钮效果一致
    const lfInstance = logicFlowRef.value?.getInstance()
    if (lfInstance) {
      // 先调整画布大小，确保 lf-graph 填充父容器
      lfInstance.resize()
      // 然后居中适应视图，使用更大的 padding 让缩放更小（显示更多内容）
      lfInstance.fitView(200)
      // 进一步缩小缩放比例（缩小到约 40%）
      await nextTick()
      // 多次缩小以达到目标缩放比例
      for (let i = 0; i < 15; i++) {
        lfInstance.zoom(false)
      }
    }
  }

  // 弹窗关闭时清理
  const handleClosed = () => {
    Object.assign(formData, initialFormData())
    clearSelection()
  }

  // 提交表单
  const handleSubmit = async () => {
    try {
      // 验证流程基本信息
      if (!formData.flowName?.trim()) {
        ElMessage.warning('请输入流程名称')
        return
      }
      if (!formData.businessType) {
        ElMessage.warning('请选择业务类型')
        return
      }

      // 验证节点
      if (!formData.nodes || formData.nodes.length === 0) {
        ElMessage.warning('请至少添加一个审批节点')
        return
      }

      // 验证每个节点
      for (let i = 0; i < formData.nodes.length; i++) {
        const node = formData.nodes[i]
        if (!node.nodeName?.trim()) {
          ElMessage.warning(`节点 ${i + 1} 未设置名称`)
          return
        }
        if (!node.assignees || node.assignees.length === 0) {
          ElMessage.warning(`节点 "${node.nodeName}" 未配置审批人`)
          return
        }
      }

      // 提交前同步当前位置
      const lfInstance = getLogicFlowInstance()
      if (lfInstance && formData.nodes) {
        const currentLfData = lfInstance.getGraphData() as LogicFlowData
        formData.nodes = syncNodePositions(formData.nodes, currentLfData)

        // 清除临时 ID（提交到后端时不需要）
        formData.nodes.forEach((node) => {
          if (node.tempId) {
            delete node.tempId
          }
        })
      }

      submitLoading.value = true

      if (props.dialogType === 'add') {
        await fetchAddFlow(formData)
      } else {
        await fetchUpdateFlow(formData.id!, formData)
      }

      dialogVisible.value = false
      emit('success')
    } catch (error) {
      console.error('保存失败:', error)
    } finally {
      submitLoading.value = false
    }
  }

  // 监听弹窗打开/关闭，添加/移除键盘事件监听
  watch(dialogVisible, (isOpen) => {
    if (isOpen) {
      // 弹窗打开时添加键盘事件监听
      keyDownHandler = (e: KeyboardEvent) => {
        // 如果焦点在输入框等可编辑元素上，不处理
        const target = e.target as HTMLElement
        if (
          target.tagName === 'INPUT' ||
          target.tagName === 'TEXTAREA' ||
          target.isContentEditable
        ) {
          return
        }

        // Delete 或 Backspace 键删除节点
        // 兼容不同浏览器的键值：e.key, e.code, e.keyCode
        const isDeleteKey = e.key === 'Delete' || e.code === 'Delete' || e.keyCode === 46
        const isBackspaceKey = e.key === 'Backspace' || e.code === 'Backspace' || e.keyCode === 8

        if (isDeleteKey || isBackspaceKey) {
          // 检查是否选中了审批节点（开始节点和结束节点不能删除）
          if (selectedNodeId.value && selectedNodeType.value === 'approval') {
            e.preventDefault()
            e.stopPropagation()
            handleNodeDelete()
          }
        }
      }
      document.addEventListener('keydown', keyDownHandler)
    } else {
      // 弹窗关闭时移除键盘事件监听
      if (keyDownHandler) {
        document.removeEventListener('keydown', keyDownHandler)
        keyDownHandler = null
      }
    }
  })

  // 组件卸载时清理
  onUnmounted(() => {
    // 清理键盘事件监听
    if (keyDownHandler) {
      document.removeEventListener('keydown', keyDownHandler)
      keyDownHandler = null
    }

    // 清理 DOM 事件监听
    if (handleNodeDeleteFromButton) {
      document.removeEventListener('logicflow-node-delete', handleNodeDeleteFromButton)
      handleNodeDeleteFromButton = null
    }
  })
</script>

<style scoped lang="scss">
  /* 关键：ElDialog 的结构由 ElementPlus 渲染，.el-dialog__body 不带当前 scoped 标记。
     这里用全局选择器 + 给 ElDialog 加专用 class，确保样式一定命中。 */
  :global(.flow-edit-dialog) {
    .el-dialog__body {
      display: flex;
      flex-direction: column;
      min-height: 700px;
    }
  }

  .dialog-content {
    display: flex;
    flex-direction: column;

    /* 直接给内容区一个可计算高度，避免 Dialog body 结构差异导致 100% 失效 */
    height: 700px;
    min-height: 700px;
    min-height: 0;
  }

  .flow-info-collapse {
    background: #f8fafc;
    border: none;

    :deep(.el-collapse-item__header) {
      height: auto;
      padding: 12px 20px;
      line-height: 1.5;
      background: #f8fafc;
      border-bottom: 1px solid #e2e8f0;
    }

    :deep(.el-collapse-item__wrap) {
      background: #fff;
      border-bottom: 1px solid #e2e8f0;
    }

    :deep(.el-collapse-item__content) {
      padding: 20px;
    }

    .collapse-title {
      display: flex;
      gap: 8px;
      align-items: center;
      font-weight: 500;
      color: #334155;

      i {
        font-size: 18px;
        color: #64748b;
      }
    }
  }

  .flow-form {
    :deep(.el-form-item) {
      margin-bottom: 16px;
    }
  }

  .flow-canvas-wrapper {
    display: flex;
    flex: 1;
    flex-direction: column;
    height: 100%;
    min-height: 0;

    .canvas-container {
      position: relative;
      flex: 1;
      width: 100%;
      height: 100%;
      min-height: 700px;
      overflow: hidden;
      background-color: var(--el-fill-color-light);
      border: 1px solid var(--el-border-color-light);
      border-radius: var(--el-border-radius-base);
    }
  }

  // 暗色主题适配
  :global(.dark) {
    .flow-info-collapse {
      background: #1e293b;

      :deep(.el-collapse-item__header) {
        background: #1e293b;
        border-bottom-color: #334155;
      }

      :deep(.el-collapse-item__wrap) {
        background: #0f172a;
        border-bottom-color: #334155;
      }

      .collapse-title {
        color: #e2e8f0;

        i {
          color: #94a3b8;
        }
      }
    }

    .flow-canvas-wrapper {
      .canvas-container {
        background-image: none !important;
      }
    }
  }
</style>
