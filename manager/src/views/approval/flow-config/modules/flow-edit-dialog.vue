<!-- 流程编辑弹窗 - 可视化流程编辑器 -->
<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogType === 'add' ? '新建审批流程' : '编辑审批流程'"
    class="flow-edit-dialog"
    fullscreen
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
            :show-fullscreen="false"
            :show-export="true"
            :mini-map-visible="miniMapVisible"
            @zoom-in="handleZoomIn"
            @zoom-out="handleZoomOut"
            @reset-zoom="handleResetZoom"
            @fit-view="handleFitView"
            @toggle-minimap="handleToggleMiniMap"
            @export-image="handleExportImage"
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
  import { ElMessage, ElMessageBox } from 'element-plus'
  import {
    fetchAddFlow,
    fetchUpdateFlow,
    fetchGetFlowDetail,
    type ApprovalFlow,
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

  // State
  const submitLoading = ref(false)

  // 工具栏状态
  const miniMapVisible = ref(false)

  // 节点选中状态
  const selectedNodeId = ref<string | null>(null)
  const selectedNodeType = ref<'start' | 'approval' | 'end' | null>(null)

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

    // 添加 CustomEvent 监听器
    document.addEventListener('lf-node-toggle-expand', handleToggleExpandEvent)
    document.addEventListener('lf-node-update', handleNodeUpdateEvent)
    document.addEventListener('lf-start-node-update', handleStartNodeUpdateEvent)
    document.addEventListener('lf-node-select-approver', handleSelectApproverEvent)
    document.addEventListener('logicflow-node-delete', handleNodeDeleteEvent)
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
      flowCode: formData.flowCode,
      businessType: formData.businessType,
      description: formData.description,
      status: formData.status,
      dialogType: props.dialogType,
      businessTypeOptions: businessTypeOptionsList.value
    })
  }

  // 键盘事件处理函数（用于删除节点）
  let keyDownHandler: ((e: KeyboardEvent) => void) | null = null

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

    setTimeout(() => {
      if (isDragging.value) return

      const clickEvent = event.e
      if (clickEvent && clickEvent.target) {
        const mouseDownPos = (clickEvent.target as any).__lfMouseDownPos
        const mouseDownTime = (clickEvent.target as any).__lfMouseDownTime
        if (mouseDownPos && mouseDownTime) {
          const timeDiff = Date.now() - mouseDownTime
          if (timeDiff < 300) {
            const moveDistance = Math.sqrt(
              Math.pow(clickEvent.clientX - mouseDownPos.x, 2) +
                Math.pow(clickEvent.clientY - mouseDownPos.y, 2)
            )
            if (moveDistance > 5) return
          }
        }
      }

      if (clickEvent && clickEvent.target) {
        delete (clickEvent.target as any).__lfMouseDownPos
        delete (clickEvent.target as any).__lfMouseDownTime
      }

      const { type, id } = data
      selectedNodeId.value = id

      if (type === 'start-node') {
        selectedNodeType.value = 'start'
      } else if (type === 'end-node') {
        selectedNodeType.value = 'end'
      } else if (type === 'approval-node') {
        selectedNodeType.value = 'approval'
      } else {
        selectedNodeId.value = null
        selectedNodeType.value = null
      }

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
    selectedNodeType.value = null
    updateNodeSelection()
  }

  // 处理节点删除
  const handleNodeDelete = async () => {
    if (selectedNodeType.value === 'start' || selectedNodeType.value === 'end') {
      ElMessage.warning('开始节点和结束节点不能删除')
      return
    }

    if (!formData.nodes || !selectedNodeId.value || selectedNodeType.value !== 'approval') return

    let index = -1
    const lfInstance = getLogicFlowInstance()
    if (lfInstance && selectedNodeId.value) {
      try {
        const lfNodeModel = lfInstance.getNodeModelById(selectedNodeId.value)
        if (lfNodeModel) {
          const lfProperties = (lfNodeModel as any).properties || {}
          if (lfProperties.originalId) {
            index = formData.nodes.findIndex((n) => n.id === lfProperties.originalId)
          }
          if (index < 0 && lfProperties.tempId) {
            index = formData.nodes.findIndex((n) => n.tempId === lfProperties.tempId)
          }
          if (index < 0 && lfProperties.nodeOrder !== undefined) {
            index = formData.nodes.findIndex((n) => n.nodeOrder === lfProperties.nodeOrder)
          }
        }
      } catch (error) {
        console.warn('[FlowEditDialog] 无法从 LogicFlow 获取节点属性:', error)
      }
    }

    if (index < 0 && selectedNodeId.value?.startsWith('approval-')) {
      const extractedId = selectedNodeId.value.replace('approval-', '')
      index = formData.nodes.findIndex((n) => n.id && String(n.id) === extractedId)
    }

    if (index < 0) {
      console.warn('[FlowEditDialog] 无法找到要删除的节点')
      return
    }

    const nodeName = formData.nodes[index]?.nodeName || '节点'

    try {
      await ElMessageBox.confirm(`确定要删除节点 "${nodeName}" 吗？`, '删除节点', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      })

      formData.nodes.splice(index, 1)
      formData.nodes = reorderNodes(formData.nodes)
      clearSelection()
      regenerateFlowData()
      ElMessage.success('节点已删除')
    } catch {
      // 用户取消
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

  // 处理审批人选择确认
  const handleAssigneeConfirm = (selectedUsers: Array<{ id: number; name: string }>) => {
    if (!selectedNodeId.value) return

    const newAssignees: ApprovalAssignee[] = selectedUsers.map((user) => ({
      assigneeType: 2,
      assigneeId: user.id,
      assigneeName: user.name
    }))

    // 通过 LogicFlow setProperties 更新节点
    const lfInstance = getLogicFlowInstance()
    if (lfInstance) {
      lfInstance.setProperties(selectedNodeId.value, {
        assignees: newAssignees,
        assigneeCount: newAssignees.length
      })
    }

    // 同步到 formData.nodes
    if (formData.nodes) {
      try {
        const model = lfInstance?.getNodeModelById(selectedNodeId.value)
        if (model) {
          const nodeProps = (model as any).properties || {}
          let targetIndex = -1
          if (nodeProps.originalId) {
            targetIndex = formData.nodes.findIndex((n) => n.id === nodeProps.originalId)
          }
          if (targetIndex < 0 && nodeProps.tempId) {
            targetIndex = formData.nodes.findIndex((n) => n.tempId === nodeProps.tempId)
          }
          if (targetIndex < 0 && nodeProps.nodeOrder !== undefined) {
            targetIndex = formData.nodes.findIndex((n) => n.nodeOrder === nodeProps.nodeOrder)
          }
          if (targetIndex >= 0) {
            formData.nodes[targetIndex] = {
              ...formData.nodes[targetIndex],
              assignees: newAssignees
            }
          }
        }
      } catch (error) {
        console.warn('[FlowEditDialog] 更新审批人失败:', error)
      }
    }
  }

  // ============ CustomEvent 监听器（与 HtmlNode 内 Vue 组件通信）============

  // 节点展开/折叠（不影响其他卡片）
  const handleToggleExpandEvent = (e: Event) => {
    const { nodeId } = (e as CustomEvent).detail
    const lfInstance = getLogicFlowInstance()
    if (!lfInstance) return

    try {
      const model = lfInstance.getNodeModelById(nodeId)
      if (!model) return
      const nodeProps = (model as any).properties || {}
      const newExpanded = !nodeProps.isExpanded

      // 切换当前节点的展开状态
      lfInstance.setProperties(nodeId, { isExpanded: newExpanded })

      // 通过 setAttributes 重新计算节点高度
      if (typeof (model as any).setAttributes === 'function') {
        ;(model as any).setAttributes()
      }

      // 强制刷新连接到该节点的所有边的路径
      nextTick(() => {
        const edges = lfInstance.graphModel.getNodeEdges(nodeId)
        edges.forEach((edge: any) => {
          if (typeof edge.initPoints === 'function') {
            edge.initPoints()
          }
        })
      })
    } catch (error) {
      console.warn('[FlowEditDialog] 展开/折叠节点失败:', error)
    }
  }

  // 审批节点数据更新（来自 approval-node-card.vue）
  const handleNodeUpdateEvent = (e: Event) => {
    const { nodeId, data } = (e as CustomEvent).detail
    const lfInstance = getLogicFlowInstance()
    if (!lfInstance || !formData.nodes) return

    // 更新 LogicFlow 节点属性
    lfInstance.setProperties(nodeId, {
      nodeName: data.nodeName,
      nodeType: data.nodeType,
      rejectAction: data.rejectAction,
      assignees: data.assignees,
      assigneeCount: data.assignees?.length || 0
    })

    // 同步到 formData.nodes
    // 从 LogicFlow 节点获取 originalId 或 tempId
    try {
      const model = lfInstance.getNodeModelById(nodeId)
      if (!model) return
      const nodeProps = (model as any).properties || {}

      let targetIndex = -1

      // 通过 originalId 查找
      if (nodeProps.originalId) {
        targetIndex = formData.nodes.findIndex((n) => n.id === nodeProps.originalId)
      }

      // 通过 tempId 查找（新节点）
      if (targetIndex < 0 && nodeProps.tempId) {
        targetIndex = formData.nodes.findIndex((n) => n.tempId === nodeProps.tempId)
      }

      // 通过 nodeOrder 查找
      if (targetIndex < 0 && nodeProps.nodeOrder !== undefined) {
        targetIndex = formData.nodes.findIndex((n) => n.nodeOrder === nodeProps.nodeOrder)
      }

      if (targetIndex >= 0) {
        formData.nodes[targetIndex] = {
          ...formData.nodes[targetIndex],
          nodeName: data.nodeName,
          nodeType: data.nodeType,
          rejectAction: data.rejectAction,
          assignees: [...(data.assignees || [])]
        }
      }
    } catch (error) {
      console.warn('[FlowEditDialog] 更新节点数据失败:', error)
    }
  }

  // 开始节点数据更新（来自 start-node-card.vue）
  const handleStartNodeUpdateEvent = (e: Event) => {
    const { data } = (e as CustomEvent).detail
    // 更新 formData
    if (data.flowName !== undefined) formData.flowName = data.flowName
    if (data.flowCode !== undefined) formData.flowCode = data.flowCode
    if (data.businessType !== undefined) formData.businessType = data.businessType
    if (data.description !== undefined) formData.description = data.description

    // 更新开始节点显示
    const lfInstance = getLogicFlowInstance()
    if (lfInstance) {
      lfInstance.setProperties('start-node', {
        flowName: formData.flowName,
        flowCode: formData.flowCode,
        businessType: formData.businessType,
        description: formData.description
      })
    }
  }

  // 选择审批人（来自 approval-node-card.vue）
  const handleSelectApproverEvent = (e: Event) => {
    const { nodeId } = (e as CustomEvent).detail
    selectedNodeId.value = nodeId
    selectedNodeType.value = 'approval'

    // 获取当前节点的已选审批人
    const lfInstance = getLogicFlowInstance()
    if (lfInstance) {
      try {
        const model = lfInstance.getNodeModelById(nodeId)
        if (model) {
          const nodeProps = (model as any).properties || {}
          const assignees = nodeProps.assignees || []
          currentSelectedIds.value = assignees
            .filter((a: any) => a.assigneeType === 2)
            .map((a: any) => a.assigneeId)
        }
      } catch {
        currentSelectedIds.value = []
      }
    }

    assigneeDialogVisible.value = true
  }

  // 节点删除（来自 approval-node-card.vue 的删除按钮）
  const handleNodeDeleteEvent = async (e: Event) => {
    const { nodeId, properties } = (e as CustomEvent).detail
    if (!formData.nodes) return

    const nodeName = properties?.nodeName || '节点'

    // 找到要删除的节点
    let index = -1
    const lfInstance = getLogicFlowInstance()
    if (lfInstance) {
      try {
        const model = lfInstance.getNodeModelById(nodeId)
        if (model) {
          const nodeProps = (model as any).properties || {}
          if (nodeProps.originalId) {
            index = formData.nodes.findIndex((n) => n.id === nodeProps.originalId)
          }
          if (index < 0 && nodeProps.tempId) {
            index = formData.nodes.findIndex((n) => n.tempId === nodeProps.tempId)
          }
          if (index < 0 && nodeProps.nodeOrder !== undefined) {
            index = formData.nodes.findIndex((n) => n.nodeOrder === nodeProps.nodeOrder)
          }
        }
      } catch (error) {
        console.warn('[FlowEditDialog] 查找删除节点失败:', error)
      }
    }

    if (index < 0) {
      console.warn('[FlowEditDialog] 无法找到要删除的节点')
      return
    }

    try {
      await ElMessageBox.confirm(`确定要删除节点 "${nodeName}" 吗？`, '删除节点', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      })

      formData.nodes.splice(index, 1)
      formData.nodes = reorderNodes(formData.nodes)
      clearSelection()
      regenerateFlowData()
      ElMessage.success('节点已删除')
    } catch {
      // 用户取消
    }
  }

  // 监听弹窗打开
  watch(
    () => props.modelValue,
    async (val) => {
      if (val) {
        // 重置状态
        clearSelection()

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
    // 等待弹窗动画完全结束、DOM 布局稳定
    await nextTick()
    await new Promise((resolve) => setTimeout(resolve, 300))
    await nextTick()

    const lfInstance = logicFlowRef.value?.getInstance()
    if (!lfInstance) return

    // 弹窗动画结束后容器尺寸才正确，先 resize
    lfInstance.resize()

    // 重置初始化状态，再重新触发 data 变化，
    // 让 ArtLogicFlow 的 data watcher 以 isInitialized=false 执行，走 fitView 分支。
    // 这样 fitView 由 data watcher 内部完成，不会被后续 watcher 覆盖。
    logicFlowRef.value?.resetInitState()
    regenerateFlowData()
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
    if (keyDownHandler) {
      document.removeEventListener('keydown', keyDownHandler)
      keyDownHandler = null
    }

    // 移除 CustomEvent 监听器
    document.removeEventListener('lf-node-toggle-expand', handleToggleExpandEvent)
    document.removeEventListener('lf-node-update', handleNodeUpdateEvent)
    document.removeEventListener('lf-start-node-update', handleStartNodeUpdateEvent)
    document.removeEventListener('lf-node-select-approver', handleSelectApproverEvent)
    document.removeEventListener('logicflow-node-delete', handleNodeDeleteEvent)
  })
</script>

<style scoped lang="scss">
  /* 关键：ElDialog 的结构由 ElementPlus 渲染，.el-dialog__body 不带当前 scoped 标记。
     这里用全局选择器 + 给 ElDialog 加专用 class，确保样式一定命中。 */
  :global(.flow-edit-dialog) {
    /* 全屏模式下让 dialog 本身也是 flex 布局，内容区可以撑满 */
    &.is-fullscreen {
      display: flex;
      flex-direction: column;
    }
  }

  :global(.flow-edit-dialog .el-dialog__body) {
    display: flex;
    flex: 1;
    flex-direction: column;
    height: 100%;
    overflow: hidden;
  }

  .dialog-content {
    display: flex;
    flex: 1;
    flex-direction: column;
    height: 100%;
    min-height: 0;
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
      min-height: 0;
      overflow: hidden;
      background-color: var(--el-fill-color-light);
      border: 1px solid var(--el-border-color-light);
      border-radius: var(--el-border-radius-base);
    }
  }

  // 暗色主题适配
  :global(.dark) {
    .flow-canvas-wrapper {
      .canvas-container {
        background-image: none !important;
      }
    }
  }
</style>
