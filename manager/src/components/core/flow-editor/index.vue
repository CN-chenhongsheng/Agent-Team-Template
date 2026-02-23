<template>
  <div class="flow-editor w-full h-full flex flex-col bg-[var(--el-fill-color-light)]">
    <!-- 工具栏 -->
    <div
      class="flow-editor-toolbar flex items-center justify-between px-6 py-4 bg-[var(--default-box-color)] border-b border-[var(--default-border)]"
    >
      <div class="flex items-center gap-2 text-sm text-[var(--el-text-color-regular)]">
        <span>{{ props.isAdd ? '新建流程' : '正在编辑：' }}</span>
        <el-tag v-if="flowName" type="primary">{{ flowName }}</el-tag>
      </div>
      <div class="flex items-center gap-3">
        <el-button v-ripple @click="handleCancel">取消</el-button>
        <el-button v-ripple type="primary" :loading="isSaving" @click="handleSave">
          保存
        </el-button>
      </div>
    </div>

    <!-- 画布区域 -->
    <div class="flow-editor-canvas flex-1 relative overflow-hidden">
      <ArtLogicFlow
        ref="logicFlowRef"
        :data="logicFlowData"
        :config="logicFlowConfig"
        :node-registrar="registerApprovalNodes"
        @node:click="handleNodeClick"
        @edge:click="handleEdgeClick"
      />
    </div>

    <!-- 审批人选择弹窗 -->
    <AssigneeSelectDialog
      v-model="showAssigneeDialog"
      :selected-ids="currentAssigneeIds"
      @confirm="handleAssigneeConfirm"
    />
  </div>
</template>

<script setup lang="ts">
  import { ref, watch, onMounted, onUnmounted } from 'vue'
  import ArtLogicFlow from '@/components/core/charts/art-logic-flow/index.vue'
  import AssigneeSelectDialog from '@/views/approval/flow-config/modules/assignee-select-dialog.vue'
  import { registerApprovalNodes } from '@/components/core/charts/art-logic-flow/approval-node'
  import {
    resolveNodeId,
    parseApprovalNodeId
  } from '@/views/approval/flow-config/modules/flow-data-utils'
  import { useFlowEditor } from './composables/useFlowEditor'
  import type { FlowEditorProps, FlowEditorEmits, FlowSaveData } from './types'

  // ========== Props & Emits ==========
  const props = withDefaults(defineProps<FlowEditorProps>(), {
    isAdd: false
  })

  const emit = defineEmits<FlowEditorEmits>()

  // ========== 核心逻辑 ==========
  const {
    flowName,
    businessType,
    description,
    approvalNodes,
    logicFlowData,
    isSaving,
    logicFlowRef,
    addNode,
    deleteNode,
    updateNode,
    selectNode,
    save,
    cancel,
    init,
    syncFromLogicFlow
  } = useFlowEditor({
    flowId: props.flowId,
    flowName: props.flowName,
    flowCode: props.flowCode,
    businessType: props.businessType,
    description: props.description,
    initialData: props.flowData,
    isAdd: props.isAdd,
    onSave: async (data: FlowSaveData) => {
      emit('success', data)
    },
    onCancel: () => {
      emit('cancel')
    }
  })

  // ========== LogicFlow 配置（使用 CSS 变量适配暗色模式） ==========
  const logicFlowConfig = {
    grid: { size: 20, visible: true, type: 'dot' as const },
    style: {
      rect: { rx: 8, ry: 8, strokeWidth: 2 },
      circle: { r: 40, strokeWidth: 2 }
    }
  }

  // ========== 审批人选择 ==========
  const showAssigneeDialog = ref(false)
  const currentAssigneeIds = ref<number[]>([])
  const currentEditingNodeId = ref<number | string | null>(null)
  const currentEditingLfNodeId = ref<string | null>(null)

  /**
   * 如果当前处于全屏模式，先退出全屏
   * 因为 ElMessageBox / ElDialog 挂载在 document.body，全屏元素外部不可见
   */
  const ensureExitFullscreen = async () => {
    const lfComponent = logicFlowRef.value
    if (lfComponent?.isFullscreen) {
      await lfComponent.exitFullscreen()
      // 等待浏览器完成全屏退出过渡
      await new Promise((r) => setTimeout(r, 100))
    }
  }

  // ========== 事件处理 ==========
  /**
   * 节点点击
   */
  const handleNodeClick = (event: { node: any; e: MouseEvent }) => {
    selectNode(event.node.id)
  }

  /**
   * 边点击 - 在两个节点之间插入新节点
   */
  const handleEdgeClick = (event: { edge: any; e: MouseEvent }) => {
    console.log('[FlowEditor] 在边上添加节点')

    const edgeData = event.edge
    const lfInstance = logicFlowRef.value?.getInstance()

    if (!lfInstance || !approvalNodes.value) {
      console.warn('[FlowEditor] 缺少必要的实例或数据')
      return
    }

    const { sourceNodeId, targetNodeId } = edgeData

    // 同步当前所有节点的位置
    syncFromLogicFlow()

    // 获取源节点和目标节点的 nodeOrder
    let sourceOrder = 0 // 开始节点的 order 视为 0

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
        console.error('[FlowEditor] 无法获取源节点属性:', error)
      }
    }

    // 获取源节点和目标节点的数据，计算新节点的位置（在边的中点）
    let sourceNode, targetNode
    try {
      sourceNode = lfInstance.getNodeModelById(sourceNodeId)
      targetNode = lfInstance.getNodeModelById(targetNodeId)
    } catch (error) {
      console.error('[FlowEditor] 无法获取节点模型:', error)
      return
    }

    // 获取节点数据
    const sourceData = (sourceNode as any).getData()
    const targetData = (targetNode as any).getData()

    // 计算中点位置
    const newX = (sourceData.x + targetData.x) / 2
    const newY = (sourceData.y + targetData.y) / 2

    // 调用 addNode 添加节点（传入位置和坐标）
    const insertIndex = approvalNodes.value.findIndex((n) => n.nodeOrder > sourceOrder)
    const afterOrder = insertIndex === -1 ? approvalNodes.value.length : insertIndex

    addNode(afterOrder, newX, newY)

    ElMessage.success('节点已添加，请点击节点进行编辑')
  }

  /**
   * 审批人确认
   */
  const handleAssigneeConfirm = (selected: Array<{ id: number; name: string }>) => {
    if (currentEditingNodeId.value) {
      const assignees = selected.map((item) => ({
        assigneeType: 2,
        assigneeId: item.id,
        assigneeName: item.name
      }))

      // 直接更新 LogicFlow 节点属性，触发 MobX 响应式 → setHtml() → Vue 组件重新渲染
      const lfInstance = logicFlowRef.value?.getInstance()
      if (lfInstance && currentEditingLfNodeId.value) {
        try {
          lfInstance.setProperties(currentEditingLfNodeId.value, {
            assignees,
            assigneeCount: assignees.length
          })
        } catch (error) {
          console.error('[FlowEditor] 更新节点审批人属性失败:', error)
        }
      }

      // 同步到 approvalNodes 数据源
      updateNode(currentEditingNodeId.value, { assignees })
    }
    showAssigneeDialog.value = false
    currentEditingNodeId.value = null
    currentEditingLfNodeId.value = null
  }

  /**
   * 保存
   */
  const handleSave = async () => {
    try {
      await save()
    } catch (error: any) {
      const errors: string[] = error.errors
      if (errors && errors.length > 1) {
        // 多条校验错误：用 HTML 列表展示
        ElMessage({
          type: 'warning',
          dangerouslyUseHTMLString: true,
          duration: 5000,
          message: `<div style="line-height:1.8">${errors.map((e: string) => `· ${e}`).join('<br>')}</div>`
        })
      } else {
        ElMessage.error(error.message || '保存失败')
      }
    }
  }

  /**
   * 取消
   */
  const handleCancel = () => {
    // 只触发 cancel，让父组件决定如何处理（是否弹出确认框）
    cancel()
  }

  // ========== 自定义事件监听 ==========
  /**
   * 处理边的+号按钮点击（通过自定义事件）
   */
  const handleEdgeAddNodeClick = (event: CustomEvent) => {
    const { edgeData } = event.detail
    handleEdgeClick({ edge: edgeData, e: event as any })
  }

  // ========== 自定义事件处理 ==========
  /**
   * 切换节点展开/折叠状态
   */
  const handleToggleExpandEvent = (e: Event) => {
    const { nodeId } = (e as CustomEvent).detail
    const lfInstance = logicFlowRef.value?.getInstance()
    if (!lfInstance) return

    try {
      const model = lfInstance.getNodeModelById(nodeId)
      if (!model) return
      const nodeProps = (model as any).properties || {}
      const newExpanded = !nodeProps.isExpanded

      // 切换当前节点的展开状态
      lfInstance.setProperties(nodeId, { isExpanded: newExpanded })

      // 通过 setAttributes 重新计算节点高度
      // LogicFlow 的 MobX 响应式会自动触发 view 的 setHtml() 更新 Vue 组件
      if (typeof (model as any).setAttributes === 'function') {
        ;(model as any).setAttributes()
      }
    } catch (error) {
      console.error('[FlowEditor] 切换展开状态失败:', error)
    }
  }

  /**
   * 更新审批节点数据
   */
  const handleNodeUpdateEvent = (e: Event) => {
    const { nodeId, data } = (e as CustomEvent).detail
    const lfInstance = logicFlowRef.value?.getInstance()
    if (!lfInstance || !approvalNodes.value) return

    // 更新 LogicFlow 节点属性
    lfInstance.setProperties(nodeId, {
      nodeName: data.nodeName,
      nodeType: data.nodeType,
      rejectAction: data.rejectAction,
      assignees: data.assignees,
      assigneeCount: data.assignees?.length || 0
    })

    // 同步到 approvalNodes
    try {
      const model = lfInstance.getNodeModelById(nodeId)
      if (model) {
        const targetId = resolveNodeId((model as any).properties || {})
        if (targetId) updateNode(targetId, data)
      }
    } catch (error) {
      console.error('[FlowEditor] 更新节点失败:', error)
    }
  }

  /**
   * 更新开始节点数据
   */
  const handleStartNodeUpdateEvent = (e: Event) => {
    const { data } = (e as CustomEvent).detail
    const lfInstance = logicFlowRef.value?.getInstance()

    // 更新流程基础信息
    if (data.flowName !== undefined) flowName.value = data.flowName
    if (data.businessType !== undefined) businessType.value = data.businessType
    if (data.description !== undefined) description.value = data.description

    // 更新开始节点显示
    if (lfInstance) {
      lfInstance.setProperties('start-node', {
        flowName: data.flowName,
        flowCode: data.flowCode,
        businessType: data.businessType,
        description: data.description
      })
    }
  }

  /**
   * 选择审批人
   */
  const handleSelectApproverEvent = async (e: Event) => {
    const { nodeId } = (e as CustomEvent).detail
    const lfInstance = logicFlowRef.value?.getInstance()

    // 获取当前节点的已选审批人
    if (lfInstance) {
      try {
        const model = lfInstance.getNodeModelById(nodeId)
        if (model) {
          const nodeProps = (model as any).properties || {}
          const assignees = nodeProps.assignees || []
          currentAssigneeIds.value = assignees
            .filter((a: any) => a.assigneeType === 2)
            .map((a: any) => a.assigneeId)

          // 保存当前编辑的节点ID（用于更新 approvalNodes）和 LogicFlow 节点ID（用于 setProperties）
          currentEditingLfNodeId.value = nodeId
          currentEditingNodeId.value = resolveNodeId(nodeProps)
        }
      } catch {
        currentAssigneeIds.value = []
      }
    }

    // 全屏模式下 ElDialog 挂载在 body，需要先退出全屏
    await ensureExitFullscreen()
    showAssigneeDialog.value = true
  }

  /**
   * 删除节点
   */
  const handleNodeDeleteEvent = async (e: Event) => {
    const { nodeId, properties } = (e as CustomEvent).detail
    if (!approvalNodes.value) return

    const nodeName = properties?.nodeName || '节点'
    const targetNodeId = parseApprovalNodeId(nodeId)

    if (!targetNodeId) {
      console.warn('[FlowEditor] 无法解析要删除的节点ID:', nodeId)
      return
    }

    // 全屏模式下 ElMessageBox 挂载在 body，需要先退出全屏
    await ensureExitFullscreen()

    try {
      await ElMessageBox.confirm(`确定要删除节点 "${nodeName}" 吗？`, '删除节点', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      })

      deleteNode(targetNodeId)
      ElMessage.success('节点已删除')
    } catch {
      // 用户取消
    }
  }

  // 组件挂载时添加事件监听
  onMounted(() => {
    document.addEventListener('lf-edge-add-node-click', handleEdgeAddNodeClick as any)
    document.addEventListener('lf-node-toggle-expand', handleToggleExpandEvent)
    document.addEventListener('lf-node-update', handleNodeUpdateEvent)
    document.addEventListener('lf-start-node-update', handleStartNodeUpdateEvent)
    document.addEventListener('lf-node-select-approver', handleSelectApproverEvent)
    document.addEventListener('logicflow-node-delete', handleNodeDeleteEvent)
  })

  // 组件卸载时移除事件监听
  onUnmounted(() => {
    document.removeEventListener('lf-edge-add-node-click', handleEdgeAddNodeClick as any)
    document.removeEventListener('lf-node-toggle-expand', handleToggleExpandEvent)
    document.removeEventListener('lf-node-update', handleNodeUpdateEvent)
    document.removeEventListener('lf-start-node-update', handleStartNodeUpdateEvent)
    document.removeEventListener('lf-node-select-approver', handleSelectApproverEvent)
    document.removeEventListener('logicflow-node-delete', handleNodeDeleteEvent)
  })

  // ========== 初始化 ==========
  // 即使 flowData 为空（新增模式），也需要初始化以创建开始和结束节点
  init(props.flowData || [])

  // ========== 监听外部数据变化 ==========
  // 当父组件的 flowData 在 FlowEditor 创建后发生变化时（如 API 延迟返回），重新初始化
  watch(
    () => props.flowData,
    (newData) => {
      if (newData && newData.length > 0) {
        init(newData)
      }
    },
    { deep: true }
  )

  // ========== 暴露方法 ==========
  defineExpose({
    save,
    cancel,
    init
  })
</script>

<style scoped lang="scss">
  .flow-editor {
    @apply w-full h-full;

    &-toolbar {
      // Tailwind 类已在模板中定义，此处不需要重复
    }

    &-canvas {
      @apply flex-1 relative overflow-hidden;
    }
  }
</style>
