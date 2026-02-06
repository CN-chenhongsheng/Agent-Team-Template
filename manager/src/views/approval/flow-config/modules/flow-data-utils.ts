/**
 * 审批流程数据转换工具
 * 在 ApprovalNode[] 和 LogicFlowData 之间进行转换
 *
 * @module views/approval/flow-config/modules/flow-data-utils
 */

import type { ApprovalNode, ApprovalAssignee } from '@/api/approval-manage'
import type {
  LogicFlowData,
  LogicFlowNode,
  LogicFlowEdge
} from '@/components/core/charts/art-logic-flow/types'

/** 节点间距配置 */
const NODE_SPACING = {
  vertical: 120, // 垂直间距
  startY: 80, // 起始Y坐标
  centerX: 400 // 中心X坐标
}

/** 节点尺寸 */
// const NODE_SIZE = {
//   approval: { width: 220, height: 80 },
//   startEnd: { width: 220, height: 80 } // 现在也是卡片形式
// }

/**
 * 生成唯一ID
 */
const generateId = (): string => {
  return `node_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
}

/**
 * 流程基本信息
 */
interface FlowInfo {
  flowName?: string
  businessType?: string
  status?: number
}

/**
 * 将 ApprovalNode[] 转换为 LogicFlowData
 * @param nodes 审批节点数组
 * @param selectedNodeId 当前选中的节点ID（可选）
 * @param flowInfo 流程基本信息（可选）
 * @returns LogicFlow 数据格式
 */
export function approvalNodesToLogicFlow(
  nodes: ApprovalNode[],
  selectedNodeId?: string | number | null,
  flowInfo?: FlowInfo
): LogicFlowData {
  const lfNodes: LogicFlowNode[] = []
  const lfEdges: LogicFlowEdge[] = []

  // 按 nodeOrder 排序（使用稳定排序，如果 nodeOrder 相同则保持原顺序）
  const sortedNodes = [...nodes].sort((a, b) => {
    const diff = a.nodeOrder - b.nodeOrder
    if (diff !== 0) return diff
    // 如果 nodeOrder 相同，保持原顺序（使用数组索引作为稳定键）
    return 0
  })

  // 计算节点位置
  let currentY = NODE_SPACING.startY

  // 1. 添加开始节点
  const startNodeId = 'start-node'
  lfNodes.push({
    id: startNodeId,
    type: 'start-node',
    x: NODE_SPACING.centerX,
    y: currentY,
    properties: {
      isSelected: selectedNodeId === startNodeId,
      flowName: flowInfo?.flowName || '',
      businessType: flowInfo?.businessType || ''
    }
  })

  let prevNodeId = startNodeId
  currentY += NODE_SPACING.vertical

  // 2. 添加审批节点 - 优先使用保存的坐标
  sortedNodes.forEach((node) => {
    // 优先使用 tempId（新节点），其次使用 id（已保存节点）
    const nodeId = node.id
      ? `approval-${node.id}`
      : node.tempId
        ? `approval-${node.tempId}`
        : generateId()

    // 优先级1：使用保存的坐标（如果存在）
    // 优先级2：使用默认间距计算
    const nodeX = node.x !== undefined ? node.x : NODE_SPACING.centerX
    const nodeY = node.y !== undefined ? node.y : currentY

    lfNodes.push({
      id: nodeId,
      type: 'approval-node',
      x: nodeX, // 使用优先级坐标
      y: nodeY, // 使用优先级坐标
      properties: {
        // 原始数据 - 保存 originalId 和 tempId 到 properties
        originalId: node.id,
        tempId: node.tempId,
        nodeName: node.nodeName,
        nodeOrder: node.nodeOrder,
        nodeType: node.nodeType,
        rejectAction: node.rejectAction,
        assignees: node.assignees || [],
        // 显示数据
        assigneeCount: node.assignees?.length || 0,
        isSelected: selectedNodeId === nodeId || selectedNodeId === node.id
      }
    })

    // 添加边：从上一个节点到当前节点
    lfEdges.push({
      id: `edge-${prevNodeId}-${nodeId}`,
      sourceNodeId: prevNodeId,
      targetNodeId: nodeId,
      type: 'custom-bezier',
      properties: {}
    })

    prevNodeId = nodeId

    // 仅在使用默认坐标时递增 currentY
    if (node.y === undefined) {
      currentY += NODE_SPACING.vertical
    }
  })

  // 3. 添加结束节点
  // 计算结束节点位置：基于最后一个审批节点
  const lastApprovalNode = sortedNodes[sortedNodes.length - 1]
  const endY =
    lastApprovalNode?.y !== undefined ? lastApprovalNode.y + NODE_SPACING.vertical : currentY

  const endNodeId = 'end-node'
  lfNodes.push({
    id: endNodeId,
    type: 'end-node',
    x: NODE_SPACING.centerX,
    y: endY,
    properties: {
      isSelected: selectedNodeId === endNodeId
    }
  })

  // 添加边：从最后一个审批节点到结束节点
  lfEdges.push({
    id: `edge-${prevNodeId}-${endNodeId}`,
    sourceNodeId: prevNodeId,
    targetNodeId: endNodeId,
    type: 'custom-bezier',
    properties: {}
  })

  return { nodes: lfNodes, edges: lfEdges }
}

/**
 * 将 LogicFlowData 转换回 ApprovalNode[]
 * @param data LogicFlow 数据
 * @returns 审批节点数组
 */
export function logicFlowToApprovalNodes(data: LogicFlowData): ApprovalNode[] {
  const approvalNodes: ApprovalNode[] = []

  // 筛选出审批节点并按Y坐标排序
  const lfApprovalNodes = data.nodes
    .filter((node) => node.type === 'approval-node')
    .sort((a, b) => a.y - b.y)

  lfApprovalNodes.forEach((node, index) => {
    const props = node.properties || {}
    approvalNodes.push({
      id: props.originalId,
      nodeName: props.nodeName || '新节点',
      nodeOrder: index + 1,
      nodeType: props.nodeType || 1,
      rejectAction: props.rejectAction || 1,
      assignees: props.assignees || []
    })
  })

  return approvalNodes
}

/**
 * 创建一个新的审批节点数据
 * @param order 节点顺序
 * @param x X 坐标（可选）
 * @param y Y 坐标（可选）
 * @returns 新的审批节点
 */
export function createNewApprovalNode(order: number, x?: number, y?: number): ApprovalNode {
  return {
    nodeName: '',
    nodeOrder: order,
    nodeType: 1,
    rejectAction: 1,
    assignees: [],
    x,
    y,
    // 生成临时唯一 ID（使用时间戳 + 随机数）
    tempId: `temp-${Date.now()}-${Math.random().toString(36).slice(2, 9)}`
  }
}

/**
 * 在指定位置插入新节点
 * @param nodes 现有节点数组
 * @param afterOrder 在哪个顺序之后插入（0表示在开始位置）
 * @returns 更新后的节点数组
 */
export function insertNodeAfter(nodes: ApprovalNode[], afterOrder: number): ApprovalNode[] {
  const newNodes = [...nodes]
  const insertIndex = afterOrder // afterOrder 就是插入位置的索引

  // 创建新节点
  const newNode = createNewApprovalNode(afterOrder + 1)

  // 插入新节点
  newNodes.splice(insertIndex, 0, newNode)

  // 重新排序所有节点
  return reorderNodes(newNodes)
}

/**
 * 删除指定节点
 * @param nodes 现有节点数组
 * @param nodeId 要删除的节点ID
 * @returns 更新后的节点数组
 */
export function removeNode(nodes: ApprovalNode[], nodeId: number): ApprovalNode[] {
  const newNodes = nodes.filter((n) => n.id !== nodeId)
  return reorderNodes(newNodes)
}

/**
 * 重新排序节点
 * @param nodes 节点数组
 * @returns 排序后的节点数组
 */
export function reorderNodes(nodes: ApprovalNode[]): ApprovalNode[] {
  const result = nodes.map((node, index) => ({
    ...node,
    nodeOrder: index + 1
  }))

  return result
}

/**
 * 更新节点属性
 * @param nodes 节点数组
 * @param nodeId 节点ID
 * @param updates 要更新的属性
 * @returns 更新后的节点数组
 */
export function updateNodeProperties(
  nodes: ApprovalNode[],
  nodeId: number | undefined,
  updates: Partial<ApprovalNode>
): ApprovalNode[] {
  return nodes.map((node) => {
    if (node.id === nodeId) {
      return { ...node, ...updates }
    }
    return node
  })
}

/**
 * 更新节点的审批人
 * @param nodes 节点数组
 * @param nodeId 节点ID
 * @param assignees 新的审批人列表
 * @returns 更新后的节点数组
 */
export function updateNodeAssignees(
  nodes: ApprovalNode[],
  nodeId: number | undefined,
  assignees: ApprovalAssignee[]
): ApprovalNode[] {
  return nodes.map((node) => {
    if (node.id === nodeId) {
      return { ...node, assignees }
    }
    return node
  })
}

/**
 * 从 LogicFlow 节点ID 提取原始节点ID
 * @param lfNodeId LogicFlow 节点ID（如 "approval-123"）
 * @returns 原始节点ID 或 null
 */
export function extractOriginalNodeId(lfNodeId: string): number | null {
  if (lfNodeId.startsWith('approval-')) {
    const id = parseInt(lfNodeId.replace('approval-', ''), 10)
    return isNaN(id) ? null : id
  }
  return null
}

/**
 * 获取节点类型的显示文本
 */
export function getNodeTypeText(nodeType: number): string {
  const texts: Record<number, string> = {
    1: '串行',
    2: '会签',
    3: '或签'
  }
  return texts[nodeType] || '串行'
}

/**
 * 获取驳回动作的显示文本
 */
export function getRejectActionText(rejectAction: number): string {
  const texts: Record<number, string> = {
    1: '直接结束',
    2: '退回申请人',
    3: '退回上一节点'
  }
  return texts[rejectAction] || '直接结束'
}

/**
 * 将 LogicFlow 中的节点位置同步回 ApprovalNode 数组
 * @param nodes ApprovalNode 数组
 * @param lfData 当前 LogicFlow 数据（包含最新位置）
 * @returns 更新后的 ApprovalNode 数组
 */
export function syncNodePositions(nodes: ApprovalNode[], lfData: LogicFlowData): ApprovalNode[] {
  return nodes.map((node) => {
    // 查找匹配的 LogicFlow 节点
    const lfNode = lfData.nodes.find((lfn) => {
      // 优先级1：通过 tempId 匹配（新节点）
      if (node.tempId && lfn.properties?.tempId) {
        return lfn.properties.tempId === node.tempId
      }

      // 优先级2：通过 originalId 匹配（已保存节点）
      // 注意：LogicFlow 的 getGraphData() 会将 properties 中的数值序列化为字符串
      // 因此需要进行类型转换后再比较
      if (node.id && lfn.properties?.originalId != null) {
        const lfOriginalId =
          typeof lfn.properties.originalId === 'string'
            ? Number.parseInt(lfn.properties.originalId, 10)
            : Number(lfn.properties.originalId)
        return lfOriginalId === node.id
      }

      // 优先级3：通过 nodeOrder 匹配（备用，仅用于审批节点）
      // 同样需要类型转换
      if (lfn.properties?.nodeOrder != null && lfn.type === 'approval-node') {
        const lfNodeOrder =
          typeof lfn.properties.nodeOrder === 'string'
            ? Number.parseInt(lfn.properties.nodeOrder, 10)
            : Number(lfn.properties.nodeOrder)
        return lfNodeOrder === node.nodeOrder
      }

      return false
    })

    if (lfNode) {
      return {
        ...node,
        x: lfNode.x,
        y: lfNode.y
      }
    }

    return node
  })
}
