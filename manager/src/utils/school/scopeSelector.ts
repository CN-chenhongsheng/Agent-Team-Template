/**
 * 管理范围选择工具函数
 *
 * @module utils/school/scopeSelector
 * @author 陈鸿昇
 */

import type { FullHierarchyTree, HierarchyNode } from './scopeDataLoader'

/**
 * 选中的节点ID对象
 */
export interface SelectedNodes {
  campusIds: number[]
  departmentIds: number[]
  majorIds: number[]
  classIds: number[]
}

/**
 * 解析 manageScope JSON 字符串
 * @param manageScope JSON字符串
 * @returns 选中的节点ID对象
 */
export function parseManageScope(manageScope?: string | null): SelectedNodes {
  const defaultResult: SelectedNodes = {
    campusIds: [],
    departmentIds: [],
    majorIds: [],
    classIds: []
  }

  if (!manageScope || manageScope.trim() === '') {
    return defaultResult
  }

  try {
    const parsed = JSON.parse(manageScope)
    // 过滤掉 null 和无效值，只保留有效的数字ID
    const filterValidIds = (ids: any[]): number[] => {
      if (!Array.isArray(ids)) return []
      return ids.filter((id: any) => id != null && !Number.isNaN(Number(id))).map(Number)
    }

    return {
      campusIds: filterValidIds(parsed.campusIds),
      departmentIds: filterValidIds(parsed.departmentIds),
      majorIds: filterValidIds(parsed.majorIds),
      classIds: filterValidIds(parsed.classIds)
    }
  } catch (error) {
    console.warn('[parseManageScope] 解析失败:', error, manageScope)
    return defaultResult
  }
}

/**
 * 序列化 manageScope 为 JSON 字符串
 * @param selectedNodes 选中的节点ID对象
 * @returns JSON字符串
 */
export function serializeManageScope(selectedNodes: SelectedNodes): string {
  // 过滤掉 null 和无效值，只保留有效的数字ID
  const filtered: SelectedNodes = {
    campusIds: selectedNodes.campusIds.filter((id) => id != null && !Number.isNaN(id)),
    departmentIds: selectedNodes.departmentIds.filter((id) => id != null && !Number.isNaN(id)),
    majorIds: selectedNodes.majorIds.filter((id) => id != null && !Number.isNaN(id)),
    classIds: selectedNodes.classIds.filter((id) => id != null && !Number.isNaN(id))
  }

  // 如果所有数组都为空，返回空字符串
  if (
    filtered.campusIds.length === 0 &&
    filtered.departmentIds.length === 0 &&
    filtered.majorIds.length === 0 &&
    filtered.classIds.length === 0
  ) {
    return ''
  }

  return JSON.stringify(filtered)
}

/**
 * 检查节点是否已选中
 * @param nodeId 节点ID
 * @param nodeType 节点类型：'campus' | 'department' | 'major' | 'class'
 * @param selectedNodes 已选中的节点ID对象
 * @returns 是否选中
 */
export function isNodeSelected(
  nodeId: number | string,
  nodeType: 'campus' | 'department' | 'major' | 'class',
  selectedNodes: SelectedNodes
): boolean {
  const id = typeof nodeId === 'string' ? Number.parseInt(nodeId, 10) : nodeId
  const idKey = `${nodeType}Ids` as keyof SelectedNodes
  const ids = selectedNodes[idKey] as number[]
  return ids.includes(id)
}

/**
 * 切换节点选中状态
 * @param nodeId 节点ID
 * @param nodeType 节点类型
 * @param selectedNodes 已选中的节点ID对象
 * @param hierarchyTree 可选的层级树，用于自动回填父级节点
 * @returns 新的选中状态
 */
export function toggleNodeSelection(
  nodeId: number | string | null | undefined,
  nodeType: 'campus' | 'department' | 'major' | 'class',
  selectedNodes: SelectedNodes,
  hierarchyTree?: FullHierarchyTree
): boolean {
  // 验证 ID 是否有效
  if (nodeId == null || nodeId === '') {
    console.warn('[toggleNodeSelection] 无效的节点ID:', nodeId)
    return false
  }

  const id = typeof nodeId === 'string' ? Number.parseInt(nodeId, 10) : nodeId

  // 验证转换后的 ID 是否有效
  if (Number.isNaN(id) || id <= 0) {
    console.warn('[toggleNodeSelection] 无效的节点ID（转换后）:', id, '原始值:', nodeId)
    return false
  }

  const idKey = `${nodeType}Ids` as keyof SelectedNodes
  const ids = selectedNodes[idKey] as number[]

  // 先过滤掉 null 和无效值
  const validIds = ids.filter((existingId) => existingId != null && !Number.isNaN(existingId))
  selectedNodes[idKey] = validIds as any

  const index = validIds.indexOf(id)
  if (index > -1) {
    // 取消选中
    validIds.splice(index, 1)
    selectedNodes[idKey] = validIds as any

    // 如果提供了层级树，自动取消所有子级节点
    if (hierarchyTree) {
      const childNodes = findChildNodes(id, nodeType, hierarchyTree)
      childNodes.forEach((child) => {
        removeNodeSelection(child.id, child.type, selectedNodes)
      })
    }

    return false
  } else {
    // 选中节点
    validIds.push(id)
    selectedNodes[idKey] = validIds as any

    // 如果提供了层级树，自动回填父级节点
    if (hierarchyTree) {
      const parentNodes = findParentNodes(id, nodeType, hierarchyTree)
      parentNodes.forEach((parent) => {
        addNodeSelection(parent.id, parent.type, selectedNodes)
      })
    }

    return true
  }
}

/**
 * 添加节点到选中列表
 * @param nodeId 节点ID
 * @param nodeType 节点类型
 * @param selectedNodes 已选中的节点ID对象
 */
export function addNodeSelection(
  nodeId: number | string,
  nodeType: 'campus' | 'department' | 'major' | 'class',
  selectedNodes: SelectedNodes
): void {
  const id = typeof nodeId === 'string' ? Number.parseInt(nodeId, 10) : nodeId
  const idKey = `${nodeType}Ids` as keyof SelectedNodes
  const ids = selectedNodes[idKey] as number[]

  if (!ids.includes(id)) {
    ids.push(id)
  }
}

/**
 * 从选中列表移除节点
 * @param nodeId 节点ID
 * @param nodeType 节点类型
 * @param selectedNodes 已选中的节点ID对象
 */
export function removeNodeSelection(
  nodeId: number | string,
  nodeType: 'campus' | 'department' | 'major' | 'class',
  selectedNodes: SelectedNodes
): void {
  const id = typeof nodeId === 'string' ? Number.parseInt(nodeId, 10) : nodeId
  const idKey = `${nodeType}Ids` as keyof SelectedNodes
  const ids = selectedNodes[idKey] as number[]

  const index = ids.indexOf(id)
  if (index > -1) {
    ids.splice(index, 1)
  }
}

/**
 * 父级节点信息
 */
export interface ParentNodeInfo {
  type: 'campus' | 'department' | 'major' | 'class'
  id: number
}

/**
 * 在层级树中查找节点的所有父级节点
 * @param nodeId 节点ID
 * @param nodeType 节点类型
 * @param hierarchyTree 完整的层级树
 * @returns 父级节点数组（从直接父级到根节点）
 */
export function findParentNodes(
  nodeId: number,
  nodeType: 'campus' | 'department' | 'major' | 'class',
  hierarchyTree: FullHierarchyTree
): ParentNodeInfo[] {
  const parents: ParentNodeInfo[] = []

  // 如果节点类型是 campus，没有父级
  if (nodeType === 'campus') {
    return parents
  }

  /**
   * 递归查找节点及其父级路径
   */
  function findNodeWithPath(
    nodes: HierarchyNode[],
    targetId: number,
    targetType: string,
    path: HierarchyNode[] = []
  ): { node: HierarchyNode; path: HierarchyNode[] } | null {
    for (const node of nodes) {
      const currentPath = [...path, node]
      if (node.id === targetId && node.type === targetType) {
        return { node, path }
      }
      if (node.children && node.children.length > 0) {
        const found = findNodeWithPath(node.children, targetId, targetType, currentPath)
        if (found) return found
      }
    }
    return null
  }

  // 查找节点及其父级路径
  let result: { node: HierarchyNode; path: HierarchyNode[] } | null = null
  for (const campus of hierarchyTree.campuses) {
    result = findNodeWithPath([campus], nodeId, nodeType)
    if (result) break
  }

  if (!result) {
    console.warn('[findParentNodes] 未找到节点:', nodeId, nodeType)
    return parents
  }

  // 从路径中提取父级节点（从直接父级到根节点）
  const { path } = result
  for (let i = path.length - 1; i >= 0; i--) {
    const parent = path[i]
    if (parent.type !== nodeType) {
      parents.push({ type: parent.type as ParentNodeInfo['type'], id: parent.id })
    }
  }

  return parents
}

/**
 * 在层级树中查找节点的所有子级节点
 * @param nodeId 节点ID
 * @param nodeType 节点类型
 * @param hierarchyTree 完整的层级树
 * @returns 子级节点数组（所有层级的子节点）
 */
export function findChildNodes(
  nodeId: number,
  nodeType: 'campus' | 'department' | 'major' | 'class',
  hierarchyTree: FullHierarchyTree
): ParentNodeInfo[] {
  const children: ParentNodeInfo[] = []

  // 如果节点类型是 class，没有子级
  if (nodeType === 'class') {
    return children
  }

  /**
   * 递归查找节点及其所有子级
   */
  function findNodeAndCollectChildren(
    nodes: HierarchyNode[],
    targetId: number,
    targetType: string
  ): HierarchyNode | null {
    for (const node of nodes) {
      if (node.id === targetId && node.type === targetType) {
        return node
      }
      if (node.children && node.children.length > 0) {
        const found = findNodeAndCollectChildren(node.children, targetId, targetType)
        if (found) return found
      }
    }
    return null
  }

  /**
   * 递归收集节点的所有子级节点
   */
  function collectAllChildren(node: HierarchyNode): void {
    if (node.children && node.children.length > 0) {
      for (const child of node.children) {
        children.push({ type: child.type as ParentNodeInfo['type'], id: child.id })
        // 递归收集子级的子级
        collectAllChildren(child)
      }
    }
  }

  // 查找节点
  let targetNode: HierarchyNode | null = null
  for (const campus of hierarchyTree.campuses) {
    targetNode = findNodeAndCollectChildren([campus], nodeId, nodeType)
    if (targetNode) break
  }

  if (!targetNode) {
    console.warn('[findChildNodes] 未找到节点:', nodeId, nodeType)
    return children
  }

  // 收集所有子级节点
  collectAllChildren(targetNode)

  return children
}
