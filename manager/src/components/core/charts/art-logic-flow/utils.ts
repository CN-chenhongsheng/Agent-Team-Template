/**
 * LogicFlow 工具函数
 *
 * @module components/core/charts/art-logic-flow/utils
 * @author 陈鸿昇
 */

import type { LogicFlowData, LogicFlowNode, LogicFlowEdge, TreeNode, LayoutConfig } from './types'

/**
 * 树形数据转换为 LogicFlow 数据
 * @param treeData 树形数据
 * @param config 布局配置
 * @returns LogicFlow 数据
 */
export function treeToLogicFlowData(
  treeData: TreeNode[],
  config: LayoutConfig = {}
): LogicFlowData {
  const {
    nodeSpacingX = 200,
    nodeSpacingY = 100,
    startX = 100,
    startY = 100,
    direction = 'vertical'
  } = config

  const nodes: LogicFlowNode[] = []
  const edges: LogicFlowEdge[] = []
  let nodeIdCounter = 0

  /**
   * 递归处理树节点
   */
  function processNode(
    node: TreeNode,
    parentId: string | null,
    level: number,
    index: number,
    siblingCount: number
  ): string {
    const nodeId = `node-${nodeIdCounter++}`
    const nodeText = node.label || node.text || String(node.id)

    // 计算节点位置
    let x: number, y: number
    if (direction === 'vertical') {
      // 垂直布局：从上到下
      x = startX + index * nodeSpacingX - (siblingCount - 1) * (nodeSpacingX / 2)
      y = startY + level * nodeSpacingY
    } else {
      // 水平布局：从左到右
      x = startX + level * nodeSpacingX
      y = startY + index * nodeSpacingY - (siblingCount - 1) * (nodeSpacingY / 2)
    }

    nodes.push({
      id: nodeId,
      type: 'glass-node',
      x,
      y,
      text: nodeText,
      properties: {
        ...(node.properties || {}),
        // 显式传递 label 到 properties，供自定义节点使用
        label: nodeText,
        // 优先使用 originalId（数字），如果不存在则尝试从 node.id 提取
        originalId:
          (node as any).originalId ??
          (typeof node.id === 'number'
            ? node.id
            : typeof node.id === 'string' && node.id.includes('-')
              ? Number.parseInt(node.id.split('-').pop() || '0', 10)
              : Number(node.id)),
        nodeType: (node as any).nodeType || node.properties?.nodeType
      }
    })

    // 如果有父节点，创建边
    if (parentId) {
      edges.push({
        id: `edge-${parentId}-${nodeId}`,
        sourceNodeId: parentId,
        targetNodeId: nodeId,
        type: 'bezier' // 使用贝塞尔曲线，带弧度
      })
    }

    // 处理子节点
    if (node.children && node.children.length > 0) {
      node.children.forEach((child, childIndex) => {
        processNode(child, nodeId, level + 1, childIndex, node.children!.length)
      })
    }

    return nodeId
  }

  // 处理所有根节点
  treeData.forEach((root, index) => {
    processNode(root, null, 0, index, treeData.length)
  })

  return { nodes, edges }
}

/**
 * LogicFlow 数据转换为树形数据
 * @param data LogicFlow 数据
 * @returns 树形数据
 */
export function logicFlowDataToTree(data: LogicFlowData): TreeNode[] {
  const { nodes, edges } = data
  const nodeMap = new Map<string, TreeNode>()
  const childrenMap = new Map<string, string[]>()
  const rootNodes: TreeNode[] = []

  // 创建节点映射
  nodes.forEach((node) => {
    const originalId = node.properties?.originalId || node.id
    nodeMap.set(node.id, {
      id: originalId,
      label: node.text || String(originalId),
      ...node.properties
    })
    childrenMap.set(node.id, [])
  })

  // 建立父子关系
  edges.forEach((edge) => {
    const children = childrenMap.get(edge.sourceNodeId) || []
    children.push(edge.targetNodeId)
    childrenMap.set(edge.sourceNodeId, children)
  })

  // 找出根节点（没有父边的节点）
  const hasParent = new Set(edges.map((edge) => edge.targetNodeId))
  nodes.forEach((node) => {
    if (!hasParent.has(node.id)) {
      rootNodes.push(nodeMap.get(node.id)!)
    }
  })

  /**
   * 递归构建树
   */
  function buildTree(nodeId: string): TreeNode {
    const node = nodeMap.get(nodeId)!
    const childIds = childrenMap.get(nodeId) || []
    if (childIds.length > 0) {
      node.children = childIds.map((childId) => buildTree(childId))
    }
    return node
  }

  return rootNodes.map((root) => {
    const rootNodeId =
      nodes.find((n) => n.properties?.originalId === root.id)?.id || String(root.id)
    return buildTree(rootNodeId)
  })
}

/**
 * 验证 LogicFlow 数据
 * @param data LogicFlow 数据
 * @returns 验证结果
 */
export function validateLogicFlowData(data: LogicFlowData): {
  valid: boolean
  errors: string[]
} {
  const errors: string[] = []

  if (!data) {
    errors.push('数据不能为空')
    return { valid: false, errors }
  }

  if (!Array.isArray(data.nodes)) {
    errors.push('nodes 必须是数组')
  }

  if (!Array.isArray(data.edges)) {
    errors.push('edges 必须是数组')
  }

  // 验证节点
  const nodeIds = new Set<string>()
  data.nodes?.forEach((node, index) => {
    if (!node.id) {
      errors.push(`节点 ${index} 缺少 id`)
    } else if (nodeIds.has(node.id)) {
      errors.push(`节点 id "${node.id}" 重复`)
    } else {
      nodeIds.add(node.id)
    }

    if (typeof node.x !== 'number' || typeof node.y !== 'number') {
      errors.push(`节点 ${node.id} 的坐标必须是数字`)
    }
  })

  // 验证边
  data.edges?.forEach((edge, index) => {
    if (!edge.sourceNodeId || !edge.targetNodeId) {
      errors.push(`边 ${index} 缺少 sourceNodeId 或 targetNodeId`)
    } else if (!nodeIds.has(edge.sourceNodeId)) {
      errors.push(`边 ${index} 的 sourceNodeId "${edge.sourceNodeId}" 不存在`)
    } else if (!nodeIds.has(edge.targetNodeId)) {
      errors.push(`边 ${index} 的 targetNodeId "${edge.targetNodeId}" 不存在`)
    }
  })

  return {
    valid: errors.length === 0,
    errors
  }
}

/**
 * 自动布局算法（简单的层次布局）
 * @param data LogicFlow 数据
 * @param config 布局配置
 * @returns 布局后的数据
 */
export function autoLayout(data: LogicFlowData, config: LayoutConfig = {}): LogicFlowData {
  const {
    nodeSpacingX = 200,
    nodeSpacingY = 100,
    startX = 100,
    startY = 100,
    direction = 'vertical'
  } = config

  const { nodes, edges } = data
  const nodeMap = new Map(nodes.map((n) => [n.id, n]))
  const levelMap = new Map<string, number>()
  const childrenMap = new Map<string, string[]>()

  // 找出根节点
  const hasParent = new Set(edges.map((e) => e.targetNodeId))
  const rootNodes = nodes.filter((n) => !hasParent.has(n.id))

  // 建立父子关系
  edges.forEach((edge) => {
    const children = childrenMap.get(edge.sourceNodeId) || []
    children.push(edge.targetNodeId)
    childrenMap.set(edge.sourceNodeId, children)
  })

  /**
   * 计算节点层级
   */
  function calculateLevel(nodeId: string, level: number = 0): void {
    levelMap.set(nodeId, level)
    const children = childrenMap.get(nodeId) || []
    children.forEach((childId) => {
      calculateLevel(childId, level + 1)
    })
  }

  rootNodes.forEach((root) => {
    calculateLevel(root.id, 0)
  })

  // 按层级分组
  const levelGroups = new Map<number, string[]>()
  levelMap.forEach((level, nodeId) => {
    const group = levelGroups.get(level) || []
    group.push(nodeId)
    levelGroups.set(level, group)
  })

  // 计算位置
  levelGroups.forEach((nodeIds, level) => {
    const count = nodeIds.length
    nodeIds.forEach((nodeId, index) => {
      const node = nodeMap.get(nodeId)!
      if (direction === 'vertical') {
        node.x = startX + index * nodeSpacingX - (count - 1) * (nodeSpacingX / 2)
        node.y = startY + level * nodeSpacingY
      } else {
        node.x = startX + level * nodeSpacingX
        node.y = startY + index * nodeSpacingY - (count - 1) * (nodeSpacingY / 2)
      }
    })
  })

  return { nodes, edges }
}
