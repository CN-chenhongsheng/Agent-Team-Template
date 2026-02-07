/**
 * 流程编辑器核心逻辑 Hook
 * 从 flow-edit-dialog.vue 提取的核心编辑逻辑
 */

import { ref, computed, watch, nextTick } from 'vue'
import type { ApprovalNode } from '@/api/approval-manage'
import type { LogicFlowData } from '@/components/core/charts/art-logic-flow/types'
import {
  approvalNodesToLogicFlow,
  logicFlowToApprovalNodes,
  createNewApprovalNode,
  syncNodePositions,
  reorderNodes
} from '@/views/approval/flow-config/modules/flow-data-utils'
import { useBusinessType } from '@/hooks/core/useBusinessType'
import type { FlowSaveData } from '../types'

export interface UseFlowEditorOptions {
  /** 流程 ID */
  flowId?: number
  /** 流程名称 */
  flowName?: string
  /** 流程编码 */
  flowCode?: string
  /** 业务类型 */
  businessType?: string
  /** 流程描述/备注 */
  description?: string
  /** 初始流程数据 */
  initialData?: ApprovalNode[]
  /** 是否为新增模式 */
  isAdd?: boolean
  /** 保存回调（传递完整流程数据） */
  onSave?: (data: FlowSaveData) => Promise<void>
  /** 取消回调 */
  onCancel?: () => void
}

/**
 * 流程编辑器核心逻辑
 */
export function useFlowEditor(options: UseFlowEditorOptions) {
  const {
    flowId,
    flowName: initialFlowName = '',
    flowCode: initialFlowCode = '',
    businessType: initialBusinessType = '',
    description: initialDescription = '',
    initialData = [],
    isAdd = false,
    onSave,
    onCancel
  } = options

  // ========== 状态管理 ==========
  const flowName = ref(initialFlowName)
  const flowCode = ref(initialFlowCode)
  const businessType = ref(initialBusinessType)
  const description = ref(initialDescription)
  const approvalNodes = ref<ApprovalNode[]>(initialData.length > 0 ? initialData : [])
  const logicFlowData = ref<LogicFlowData>({ nodes: [], edges: [] })
  const selectedNodeId = ref<string | null>(null)
  const isLoading = ref(false)
  const isSaving = ref(false)

  // LogicFlow 实例引用
  const logicFlowRef = ref<any>(null)

  // 业务类型数据
  const { businessTypeOptions, fetchBusinessTypes } = useBusinessType()

  // ========== 计算属性 ==========
  const hasNodes = computed(() => approvalNodes.value.length > 0)
  const canSave = computed(() => {
    return flowName.value.trim() !== '' && hasNodes.value
  })

  /**
   * 保存前校验，返回错误信息列表（空表示通过）
   */
  const validateBeforeSave = (): string[] => {
    const errors: string[] = []

    // 1. 校验流程基本信息
    if (!flowName.value.trim()) {
      errors.push('请填写流程名称')
    }
    if (!flowCode.value.trim()) {
      errors.push('请填写流程编码')
    }
    if (!businessType.value) {
      errors.push('请选择业务类型')
    }

    // 2. 校验审批节点
    if (approvalNodes.value.length === 0) {
      errors.push('请至少添加一个审批节点')
      return errors
    }

    approvalNodes.value.forEach((node, index) => {
      const prefix = `第${index + 1}个审批节点`
      if (!node.nodeName || !node.nodeName.trim()) {
        errors.push(`${prefix}：请填写节点名称`)
      }
      if (!node.assignees || node.assignees.length === 0) {
        errors.push(`${prefix}「${node.nodeName || '未命名'}」：请选择审批人`)
      }
    })

    return errors
  }

  // ========== 数据转换 ==========
  /**
   * 将 ApprovalNode[] 转换为 LogicFlowData
   */
  const convertToLogicFlow = () => {
    logicFlowData.value = approvalNodesToLogicFlow(approvalNodes.value, selectedNodeId.value, {
      flowName: flowName.value,
      flowCode: flowCode.value,
      businessType: businessType.value,
      description: description.value,
      dialogType: isAdd ? 'add' : 'edit',
      businessTypeOptions: businessTypeOptions.value
    })
  }

  /**
   * 从 LogicFlow 同步数据回 ApprovalNode[]
   */
  const syncFromLogicFlow = () => {
    if (!logicFlowRef.value) return

    // 获取 LogicFlow 实例
    const lfInstance = (logicFlowRef.value as any).getInstance?.()
    if (!lfInstance) return

    const currentData = lfInstance.getGraphData()

    // 同步节点位置
    approvalNodes.value = syncNodePositions(approvalNodes.value, currentData)

    // 转换回 ApprovalNode[]
    const nodes = logicFlowToApprovalNodes(currentData)
    approvalNodes.value = nodes
  }

  // ========== 节点操作 ==========

  /**
   * 从 LogicFlow 同步当前节点位置到 approvalNodes（轻量级，仅同步坐标）
   */
  const syncPositionsFromLf = () => {
    const lfInstance = logicFlowRef.value?.getInstance?.()
    if (!lfInstance) return
    const graphData = lfInstance.getGraphData()
    approvalNodes.value = syncNodePositions(approvalNodes.value, graphData)
  }

  /**
   * 添加新节点
   * @param afterOrder 在哪个顺序之后插入
   * @param x X 坐标（可选）
   * @param y Y 坐标（可选）
   */
  const addNode = (afterOrder: number = approvalNodes.value.length, x?: number, y?: number) => {
    // 先同步当前拖拽后的位置，避免重新生成时丢失
    syncPositionsFromLf()

    const newNode = createNewApprovalNode(afterOrder + 1, x, y)
    approvalNodes.value.splice(afterOrder, 0, newNode)
    approvalNodes.value = reorderNodes(approvalNodes.value)

    convertToLogicFlow()

    // 选中新节点
    nextTick(() => {
      if (newNode.tempId) {
        selectedNodeId.value = `approval-${newNode.tempId}`
      }
    })
  }

  /**
   * 删除节点
   */
  const deleteNode = (nodeId: number | string) => {
    syncPositionsFromLf()

    approvalNodes.value = approvalNodes.value.filter((node) => {
      if (typeof nodeId === 'number') {
        return node.id !== nodeId
      } else {
        return node.tempId !== nodeId
      }
    })
    approvalNodes.value = reorderNodes(approvalNodes.value)

    convertToLogicFlow()
    selectedNodeId.value = null
  }

  /**
   * 更新节点数据
   */
  const updateNode = (nodeId: number | string, updates: Partial<ApprovalNode>) => {
    syncPositionsFromLf()

    approvalNodes.value = approvalNodes.value.map((node) => {
      const matches = typeof nodeId === 'number' ? node.id === nodeId : node.tempId === nodeId

      if (matches) {
        return { ...node, ...updates }
      }
      return node
    })

    convertToLogicFlow()
  }

  /**
   * 选中节点（直接更新 isSelected 属性，不重新生成图数据，避免重置拖拽后的位置）
   */
  const selectNode = (nodeId: string | null) => {
    const prevSelectedId = selectedNodeId.value
    selectedNodeId.value = nodeId

    const lfInstance = logicFlowRef.value?.getInstance?.()
    if (lfInstance) {
      if (prevSelectedId) {
        try {
          lfInstance.setProperties(prevSelectedId, { isSelected: false })
        } catch {
          /* ignore */
        }
      }
      if (nodeId) {
        try {
          lfInstance.setProperties(nodeId, { isSelected: true })
        } catch {
          /* ignore */
        }
      }
      return
    }

    convertToLogicFlow()
  }

  // ========== 保存和取消 ==========
  /**
   * 保存流程
   */
  const save = async () => {
    // 先同步 LogicFlow 数据（确保拿到最新的节点信息）
    syncFromLogicFlow()

    // 执行校验
    const errors = validateBeforeSave()
    if (errors.length > 0) {
      // 拼接全部错误，用换行分隔，方便 UI 层展示
      const err = new Error(errors[0])
      ;(err as any).errors = errors
      throw err
    }

    try {
      isSaving.value = true

      // 调用保存回调（传递完整流程数据）
      if (onSave) {
        await onSave({
          flowId,
          flowName: flowName.value,
          flowCode: flowCode.value,
          businessType: businessType.value,
          description: description.value,
          nodes: approvalNodes.value
        })
      }
    } finally {
      isSaving.value = false
    }
  }

  /**
   * 取消编辑
   */
  const cancel = () => {
    if (onCancel) {
      onCancel()
    }
  }

  // ========== 初始化 ==========
  /**
   * 初始化编辑器
   */
  const init = (data?: ApprovalNode[]) => {
    if (data && data.length > 0) {
      approvalNodes.value = data
    }
    convertToLogicFlow()
  }

  /**
   * 重置编辑器
   */
  const reset = () => {
    flowName.value = initialFlowName
    businessType.value = ''
    description.value = ''
    approvalNodes.value = initialData.length > 0 ? [...initialData] : []
    selectedNodeId.value = null
    convertToLogicFlow()
  }

  // ========== 监听数据变化 ==========
  // 监听业务类型选项变化
  watch(
    () => businessTypeOptions.value,
    () => {
      convertToLogicFlow()
    }
  )

  // 初始化 - 即使没有审批节点，也要调用 convertToLogicFlow() 以创建开始和结束节点
  // 同时获取业务类型数据
  convertToLogicFlow()
  fetchBusinessTypes()

  return {
    // 状态
    flowName,
    flowCode,
    businessType,
    description,
    approvalNodes,
    logicFlowData,
    selectedNodeId,
    isLoading,
    isSaving,
    logicFlowRef,

    // 计算属性
    hasNodes,
    canSave,

    // 方法
    convertToLogicFlow,
    syncFromLogicFlow,
    addNode,
    deleteNode,
    updateNode,
    selectNode,
    save,
    cancel,
    init,
    reset
  }
}
