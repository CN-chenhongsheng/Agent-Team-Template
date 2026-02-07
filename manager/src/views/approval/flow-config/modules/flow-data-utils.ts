/**
 * 审批流程数据转换工具
 * 在 ApprovalNode[] 和 LogicFlowData 之间进行转换
 *
 * @module views/approval/flow-config/modules/flow-data-utils
 */

import type { ApprovalNode } from '@/api/approval-manage'
import type {
  LogicFlowData,
  LogicFlowNode,
  LogicFlowEdge
} from '@/components/core/charts/art-logic-flow/types'

/** 节点间距配置（水平布局：从左到右） */
const NODE_SPACING = {
  horizontal: 700, // 节点中心间距（节点宽320，间隙380）
  startX: 200, // 起始X坐标
  centerY: 300 // 中心Y坐标（所有节点在同一水平线上）
}

/**
 * 流程基本信息
 */
interface FlowInfo {
  flowName?: string
  businessType?: string
  status?: number
  flowCode?: string
  description?: string
  dialogType?: 'add' | 'edit'
  businessTypeOptions?: Array<{ label: string; value: string }>
}

// ==================== 通用工具函数 ====================

/**
 * 重新排序节点（设置 nodeOrder = index + 1）
 */
export function reorderNodes(nodes: ApprovalNode[]): ApprovalNode[] {
  return nodes.map((node, index) => ({
    ...node,
    nodeOrder: index + 1
  }))
}

/**
 * 从 LogicFlow 节点属性中解析出业务节点 ID
 * @returns originalId（数字）或 tempId（字符串）或 null
 */
export function resolveNodeId(props: Record<string, any>): number | string | null {
  if (props.originalId != null) {
    const id =
      typeof props.originalId === 'string'
        ? Number.parseInt(props.originalId, 10)
        : Number(props.originalId)
    if (!isNaN(id)) return id
  }
  if (props.tempId) return String(props.tempId)
  return null
}

/**
 * 从 LogicFlow 节点 ID 字符串解析出业务节点 ID
 * 格式：approval-{数字id} 或 approval-temp-{timestamp}-{random}
 */
export function parseApprovalNodeId(lfNodeId: string): number | string | null {
  if (typeof lfNodeId !== 'string' || !lfNodeId.startsWith('approval-')) return null
  const idPart = lfNodeId.replace('approval-', '')
  const numId = parseInt(idPart, 10)
  if (!isNaN(numId) && String(numId) === idPart) return numId
  return idPart || null
}

/**
 * 获取节点类型的显示文本
 */
export function getNodeTypeText(nodeType: number): string {
  const texts: Record<number, string> = { 1: '串行', 2: '会签', 3: '或签' }
  return texts[nodeType] || '串行'
}

/**
 * 获取驳回动作的显示文本
 */
export function getRejectActionText(rejectAction: number): string {
  const texts: Record<number, string> = { 1: '直接结束', 2: '退回申请人', 3: '退回上一节点' }
  return texts[rejectAction] || '直接结束'
}

// ==================== 核心转换函数 ====================

/**
 * 将 ApprovalNode[] 转换为 LogicFlowData
 */
export function approvalNodesToLogicFlow(
  nodes: ApprovalNode[],
  selectedNodeId?: string | number | null,
  flowInfo?: FlowInfo
): LogicFlowData {
  const lfNodes: LogicFlowNode[] = []
  const lfEdges: LogicFlowEdge[] = []

  // 按 nodeOrder 稳定排序
  const sortedNodes = [...nodes].sort((a, b) => a.nodeOrder - b.nodeOrder)

  let currentX = NODE_SPACING.startX

  // 1. 开始节点
  const startNodeId = 'start-node'
  lfNodes.push({
    id: startNodeId,
    type: 'start-node',
    x: currentX,
    y: NODE_SPACING.centerY,
    properties: {
      isSelected: selectedNodeId === startNodeId,
      isExpanded: true,
      flowName: flowInfo?.flowName || '',
      flowCode: flowInfo?.flowCode || '',
      businessType: flowInfo?.businessType || '',
      description: flowInfo?.description || '',
      dialogType: flowInfo?.dialogType || 'add',
      businessTypeOptions: flowInfo?.businessTypeOptions || []
    }
  })

  let prevNodeId = startNodeId
  currentX += NODE_SPACING.horizontal

  // 2. 审批节点
  sortedNodes.forEach((node) => {
    const nodeId = node.id
      ? `approval-${node.id}`
      : node.tempId
        ? `approval-${node.tempId}`
        : `approval-${Date.now()}-${Math.random().toString(36).slice(2, 9)}`

    const nodeX = node.x !== undefined ? node.x : currentX
    const nodeY = node.y !== undefined ? node.y : NODE_SPACING.centerY

    lfNodes.push({
      id: nodeId,
      type: 'approval-node',
      x: nodeX,
      y: nodeY,
      properties: {
        originalId: node.id,
        tempId: node.tempId,
        nodeName: node.nodeName,
        nodeOrder: node.nodeOrder,
        nodeType: node.nodeType,
        rejectAction: node.rejectAction,
        assignees: node.assignees || [],
        assigneeCount: node.assignees?.length || 0,
        isSelected: selectedNodeId === nodeId || selectedNodeId === node.id,
        isExpanded: true
      }
    })

    lfEdges.push({
      id: `edge-${prevNodeId}-${nodeId}`,
      sourceNodeId: prevNodeId,
      targetNodeId: nodeId,
      type: 'custom-bezier',
      sourceAnchorId: `${prevNodeId}_right`,
      targetAnchorId: `${nodeId}_left`,
      properties: {}
    })

    prevNodeId = nodeId
    currentX = nodeX + NODE_SPACING.horizontal
  })

  // 3. 结束节点
  const endNodeId = 'end-node'
  lfNodes.push({
    id: endNodeId,
    type: 'end-node',
    x: currentX,
    y: NODE_SPACING.centerY,
    properties: { isSelected: selectedNodeId === endNodeId }
  })

  lfEdges.push({
    id: `edge-${prevNodeId}-${endNodeId}`,
    sourceNodeId: prevNodeId,
    targetNodeId: endNodeId,
    type: 'custom-bezier',
    sourceAnchorId: `${prevNodeId}_right`,
    targetAnchorId: `${endNodeId}_left`,
    properties: {}
  })

  return { nodes: lfNodes, edges: lfEdges }
}

/**
 * 将 LogicFlowData 转换回 ApprovalNode[]
 */
export function logicFlowToApprovalNodes(data: LogicFlowData): ApprovalNode[] {
  return data.nodes
    .filter((node) => node.type === 'approval-node')
    .sort((a, b) => a.x - b.x)
    .map((node, index) => {
      const props = node.properties || {}
      return {
        id: props.originalId,
        tempId: props.tempId,
        nodeName: props.nodeName || '新节点',
        nodeOrder: index + 1,
        nodeType: props.nodeType || 1,
        rejectAction: props.rejectAction || 1,
        assignees: props.assignees || []
      }
    })
}

/**
 * 创建一个新的审批节点数据
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
    tempId: `temp-${Date.now()}-${Math.random().toString(36).slice(2, 9)}`
  }
}

/**
 * 将 LogicFlow 中的节点位置同步回 ApprovalNode 数组
 * 使用 Map 索引实现 O(n) 查找
 */
export function syncNodePositions(nodes: ApprovalNode[], lfData: LogicFlowData): ApprovalNode[] {
  // 构建索引 Map
  const byTempId = new Map<string, LogicFlowNode>()
  const byOriginalId = new Map<number, LogicFlowNode>()
  const byOrder = new Map<number, LogicFlowNode>()

  for (const lfn of lfData.nodes) {
    if (lfn.type !== 'approval-node') continue
    const p = lfn.properties || {}

    if (p.tempId) byTempId.set(String(p.tempId), lfn)

    if (p.originalId != null) {
      const id =
        typeof p.originalId === 'string' ? Number.parseInt(p.originalId, 10) : Number(p.originalId)
      if (!isNaN(id)) byOriginalId.set(id, lfn)
    }

    if (p.nodeOrder != null) {
      const order =
        typeof p.nodeOrder === 'string' ? Number.parseInt(p.nodeOrder, 10) : Number(p.nodeOrder)
      if (!isNaN(order)) byOrder.set(order, lfn)
    }
  }

  return nodes.map((node) => {
    const lfNode =
      (node.tempId ? byTempId.get(node.tempId) : undefined) ??
      (node.id ? byOriginalId.get(node.id) : undefined) ??
      byOrder.get(node.nodeOrder)

    return lfNode ? { ...node, x: lfNode.x, y: lfNode.y } : node
  })
}
