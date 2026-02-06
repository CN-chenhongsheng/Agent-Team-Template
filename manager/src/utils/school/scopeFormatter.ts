/**
 * 管理范围格式化工具函数
 *
 * @module utils/school/scopeFormatter
 * @author 陈鸿昇
 */

import { parseManageScope } from './scopeSelector'
import type { FullHierarchyTree, HierarchyNode } from './scopeDataLoader'
import { loadFullHierarchy } from './scopeDataLoader'

/**
 * 节点名称映射缓存
 */
let nodeNameCache: Map<string, string> | null = null
let hierarchyTreeCache: FullHierarchyTree | null = null

/**
 * 构建节点名称映射缓存
 */
async function buildNodeNameCache(): Promise<Map<string, string>> {
  if (nodeNameCache && hierarchyTreeCache) {
    return nodeNameCache
  }

  try {
    hierarchyTreeCache = await loadFullHierarchy()
    nodeNameCache = new Map()

    /**
     * 递归遍历节点树，构建 ID 到名称的映射
     */
    function traverseNodes(nodes: HierarchyNode[]): void {
      for (const node of nodes) {
        const key = `${node.type}-${node.id}`
        nodeNameCache!.set(key, node.name)
        if (node.children && node.children.length > 0) {
          traverseNodes(node.children)
        }
      }
    }

    traverseNodes(hierarchyTreeCache.campuses)
    return nodeNameCache
  } catch (error) {
    console.error('[scopeFormatter] 构建节点名称缓存失败:', error)
    return new Map()
  }
}

/**
 * 根据节点类型和ID获取节点名称
 */
function getNodeName(nodeType: string, nodeId: number): string {
  if (!nodeNameCache) return `未知${nodeType}`
  const key = `${nodeType}-${nodeId}`
  return nodeNameCache.get(key) || `未知${nodeType}(${nodeId})`
}

/**
 * 格式化管理范围 - 智能显示
 * @param manageScope JSON字符串
 * @returns 格式化后的中文文本
 */
export async function formatManageScope(manageScope?: string | null): Promise<string> {
  if (!manageScope || manageScope.trim() === '') {
    return '未设置'
  }

  try {
    // 解析管理范围
    const selectedNodes = parseManageScope(manageScope)

    // 检查是否有选中的节点
    const hasSelection =
      selectedNodes.campusIds.length > 0 ||
      selectedNodes.departmentIds.length > 0 ||
      selectedNodes.majorIds.length > 0 ||
      selectedNodes.classIds.length > 0

    if (!hasSelection) {
      return '未设置'
    }

    // 构建节点名称缓存
    await buildNodeNameCache()

    // 情况1：只选择了班级，显示完整路径
    if (
      selectedNodes.campusIds.length === 0 &&
      selectedNodes.departmentIds.length === 0 &&
      selectedNodes.majorIds.length === 0 &&
      selectedNodes.classIds.length > 0
    ) {
      // 查找每个班级的完整路径
      const classPaths: string[] = []
      for (const classId of selectedNodes.classIds) {
        const path = findNodePath(classId, 'class', hierarchyTreeCache!)
        if (path) {
          classPaths.push(path.join(' > '))
        } else {
          classPaths.push(getNodeName('class', classId))
        }
      }
      return classPaths.join('、')
    }

    // 情况2：选择了多个节点，显示统计信息
    const parts: string[] = []

    // 显示校区
    if (selectedNodes.campusIds.length > 0) {
      const campusNames = selectedNodes.campusIds.map((id) => getNodeName('campus', id)).join('、')
      parts.push(campusNames)

      // 统计下级节点数量
      const stats: string[] = []
      if (selectedNodes.departmentIds.length > 0) {
        stats.push(`${selectedNodes.departmentIds.length}个院系`)
      }
      if (selectedNodes.majorIds.length > 0) {
        stats.push(`${selectedNodes.majorIds.length}个专业`)
      }
      if (selectedNodes.classIds.length > 0) {
        stats.push(`${selectedNodes.classIds.length}个班级`)
      }

      if (stats.length > 0) {
        parts[0] = `${parts[0]}（含${stats.join('、')}）`
      }
    } else {
      // 如果没有选择校区，显示其他节点
      if (selectedNodes.departmentIds.length > 0) {
        const deptNames = selectedNodes.departmentIds
          .map((id) => getNodeName('department', id))
          .join('、')
        parts.push(deptNames)
      }
      if (selectedNodes.majorIds.length > 0) {
        const majorNames = selectedNodes.majorIds.map((id) => getNodeName('major', id)).join('、')
        parts.push(majorNames)
      }
      if (selectedNodes.classIds.length > 0) {
        const classNames = selectedNodes.classIds.map((id) => getNodeName('class', id)).join('、')
        parts.push(classNames)
      }
    }

    return parts.length > 0 ? parts.join('、') : '未设置'
  } catch (error) {
    console.error('[formatManageScope] 格式化失败:', error)
    return '格式错误'
  }
}

/**
 * 查找节点的完整路径
 */
function findNodePath(
  nodeId: number,
  nodeType: string,
  hierarchyTree: FullHierarchyTree
): string[] | null {
  /**
   * 递归查找节点及其路径
   */
  function findNode(
    nodes: HierarchyNode[],
    targetId: number,
    targetType: string,
    path: string[] = []
  ): string[] | null {
    for (const node of nodes) {
      const currentPath = [...path, node.name]
      if (node.id === targetId && node.type === targetType) {
        return currentPath
      }
      if (node.children && node.children.length > 0) {
        const found = findNode(node.children, targetId, targetType, currentPath)
        if (found) return found
      }
    }
    return null
  }

  for (const campus of hierarchyTree.campuses) {
    const path = findNode([campus], nodeId, nodeType)
    if (path) return path
  }

  return null
}

/**
 * 同步格式化（使用缓存，如果缓存不存在则返回简化信息）
 */
export function formatManageScopeSync(manageScope?: string | null): string {
  if (!manageScope || manageScope.trim() === '') {
    return '未设置'
  }

  try {
    const selectedNodes = parseManageScope(manageScope)

    const hasSelection =
      selectedNodes.campusIds.length > 0 ||
      selectedNodes.departmentIds.length > 0 ||
      selectedNodes.majorIds.length > 0 ||
      selectedNodes.classIds.length > 0

    if (!hasSelection) {
      return '未设置'
    }

    // 如果缓存不存在，返回简化信息
    if (!nodeNameCache) {
      const parts: string[] = []
      if (selectedNodes.campusIds.length > 0) {
        parts.push(`${selectedNodes.campusIds.length}个校区`)
      }
      if (selectedNodes.departmentIds.length > 0) {
        parts.push(`${selectedNodes.departmentIds.length}个院系`)
      }
      if (selectedNodes.majorIds.length > 0) {
        parts.push(`${selectedNodes.majorIds.length}个专业`)
      }
      if (selectedNodes.classIds.length > 0) {
        parts.push(`${selectedNodes.classIds.length}个班级`)
      }
      return parts.join('、')
    }

    // 使用缓存格式化
    const parts: string[] = []
    if (selectedNodes.campusIds.length > 0) {
      const campusNames = selectedNodes.campusIds.map((id) => getNodeName('campus', id)).join('、')
      parts.push(campusNames)

      const stats: string[] = []
      if (selectedNodes.departmentIds.length > 0) {
        stats.push(`${selectedNodes.departmentIds.length}个院系`)
      }
      if (selectedNodes.majorIds.length > 0) {
        stats.push(`${selectedNodes.majorIds.length}个专业`)
      }
      if (selectedNodes.classIds.length > 0) {
        stats.push(`${selectedNodes.classIds.length}个班级`)
      }

      if (stats.length > 0) {
        parts[0] = `${parts[0]}（含${stats.join('、')}）`
      }
    } else {
      if (selectedNodes.departmentIds.length > 0) {
        const deptNames = selectedNodes.departmentIds
          .map((id) => getNodeName('department', id))
          .join('、')
        parts.push(deptNames)
      }
      if (selectedNodes.majorIds.length > 0) {
        const majorNames = selectedNodes.majorIds.map((id) => getNodeName('major', id)).join('、')
        parts.push(majorNames)
      }
      if (selectedNodes.classIds.length > 0) {
        const classNames = selectedNodes.classIds.map((id) => getNodeName('class', id)).join('、')
        parts.push(classNames)
      }
    }

    return parts.length > 0 ? parts.join('、') : '未设置'
  } catch (error) {
    console.error('[formatManageScopeSync] 格式化失败:', error)
    return '格式错误'
  }
}
