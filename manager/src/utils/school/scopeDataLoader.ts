/**
 * 层级数据加载工具
 *
 * @module utils/school/scopeDataLoader
 * @author 陈鸿昇
 */

import { fetchGetSchoolHierarchy } from '@/api/school-manage'
import { treeToLogicFlowData } from '@/components/core/charts/art-logic-flow/utils'
import type { LogicFlowData } from '@/components/core/charts/art-logic-flow/types'
import type { TreeNode } from '@/components/core/charts/art-logic-flow/types'

/**
 * 层级节点类型
 */
export interface HierarchyNode {
  id: number
  name: string
  type: 'campus' | 'department' | 'major' | 'class'
  code: string
  parentId?: number
  parentCode?: string
  children?: HierarchyNode[]
  [key: string]: any
}

/**
 * 完整的层级树结构
 */
export interface FullHierarchyTree {
  campuses: HierarchyNode[]
}

/**
 * 加载完整的层级数据
 * @returns 完整的层级树
 */
export async function loadFullHierarchy(): Promise<FullHierarchyTree> {
  try {
    // 调用新的单接口获取完整层级数据
    // request.get 已经提取了 res.data.data，所以 response 直接就是 SchoolHierarchyResponse
    const hierarchyData =
      (await fetchGetSchoolHierarchy()) as Api.SystemManage.SchoolHierarchyResponse

    console.log('[loadFullHierarchy] 接收到的数据:', hierarchyData)

    if (!hierarchyData || !hierarchyData.campuses) {
      console.warn('[loadFullHierarchy] 返回数据为空', hierarchyData)
      return { campuses: [] }
    }

    // 将后端返回的层级树转换为前端需要的格式
    const campuses: HierarchyNode[] = hierarchyData.campuses.map((campus) =>
      convertToHierarchyNode(campus)
    )

    return { campuses }
  } catch (error) {
    console.error('[loadFullHierarchy] 加载失败:', error)
    throw error
  }
}

/**
 * 将后端返回的层级节点转换为前端格式
 */
function convertToHierarchyNode(node: Api.SystemManage.SchoolHierarchyNode): HierarchyNode {
  const hierarchyNode: HierarchyNode = {
    id: node.id,
    name: node.name,
    type: node.type,
    code: node.code,
    parentCode: node.parentCode
  }

  if (node.children && node.children.length > 0) {
    hierarchyNode.children = node.children.map((child) => convertToHierarchyNode(child))
  }

  return hierarchyNode
}

/**
 * 将层级树转换为 LogicFlow 数据格式
 * @param hierarchyTree 层级树
 * @returns LogicFlow 数据
 */
export function hierarchyToLogicFlowData(hierarchyTree: FullHierarchyTree): LogicFlowData {
  // 将层级树转换为 TreeNode 格式
  const treeNodes: TreeNode[] = hierarchyTree.campuses.map((campus) => {
    return convertToTreeNode(campus)
  })

  // 使用工具函数转换为 LogicFlow 数据
  return treeToLogicFlowData(treeNodes, {
    nodeSpacingX: 250,
    nodeSpacingY: 120,
    startX: 100,
    startY: 100,
    direction: 'vertical'
  })
}

/**
 * 将 HierarchyNode 转换为 TreeNode
 */
function convertToTreeNode(node: HierarchyNode): TreeNode {
  const treeNode: TreeNode = {
    id: `${node.type}-${node.id}`,
    label: node.name,
    text: node.name,
    originalId: node.id,
    nodeType: node.type,
    nodeCode: node.code
  }

  if (node.children && node.children.length > 0) {
    treeNode.children = node.children.map((child) => convertToTreeNode(child))
  }

  return treeNode
}
