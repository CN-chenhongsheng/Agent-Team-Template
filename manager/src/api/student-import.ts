/**
 * 学生导入模块 API
 * 提供学生批量导入所需的树形结构数据
 *
 * @module api/student-import
 * @author 陈鸿昇
 * @date 2026-02-04
 */

import request from '@/utils/http'

/**
 * 组织架构树节点
 */
export interface OrgTreeNode {
  /** 节点ID */
  id: number
  /** 节点编码 */
  code: string
  /** 节点名称 */
  name: string
  /** 节点类型：campus-校区, department-院系, major-专业, class-班级 */
  type: 'campus' | 'department' | 'major' | 'class'
  /** 父节点编码 */
  parentCode?: string
  /** 状态：1启用 0停用 */
  status: number
  /** 子节点列表 */
  children?: OrgTreeNode[]
}

/**
 * 组织架构树响应
 */
export interface OrgTreeResponse {
  /** 校区列表（包含完整的层级结构） */
  campuses: OrgTreeNode[]
}

/**
 * 宿舍结构树节点
 */
export interface DormTreeNode {
  /** 节点ID */
  id: number
  /** 节点编码 */
  code: string
  /** 节点名称 */
  name: string
  /** 节点类型：campus-校区, floor-楼层, room-房间, bed-床位 */
  type: 'campus' | 'floor' | 'room' | 'bed'
  /** 父节点编码或ID */
  parentCode?: string
  /** 状态：1启用 0停用 */
  status: number
  /** 床位状态（仅床位节点有效）：1空闲 2已占用 3维修中 4已预订 */
  bedStatus?: number
  /** 子节点列表 */
  children?: DormTreeNode[]
}

/**
 * 宿舍结构树响应
 */
export interface DormTreeResponse {
  /** 校区列表（包含完整的宿舍层级结构） */
  campuses: DormTreeNode[]
}

/**
 * 获取组织架构树
 * 返回校区-院系-专业-班级的层级结构
 * 用于生成Excel导入模板的级联下拉
 */
export function fetchOrgTree() {
  return request.get<OrgTreeResponse>({
    url: '/api/v1/student/import/org-tree'
  })
}

/**
 * 获取住宿结构树
 * 返回校区-楼层-房间-床位的层级结构
 * 用于生成Excel导入模板的级联下拉
 */
export function fetchDormTree() {
  return request.get<DormTreeResponse>({
    url: '/api/v1/student/import/dorm-tree'
  })
}

/**
 * 同时获取组织架构树和住宿结构树
 * 用于生成完整的Excel导入模板
 */
export async function fetchImportTrees(): Promise<{
  orgTree: OrgTreeResponse
  dormTree: DormTreeResponse
}> {
  const [orgTree, dormTree] = await Promise.all([fetchOrgTree(), fetchDormTree()])
  return { orgTree, dormTree }
}
