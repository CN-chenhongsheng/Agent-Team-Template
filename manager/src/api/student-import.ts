/**
 * 学生导入模块 API
 * 提供学生批量导入所需的树形结构数据
 *
 * @module api/student-import
 * @author 陈鸿昇
 * @date 2026-02-04
 */

import request from '@/utils/http'

/** ==================== 类型别名（规范类型定义在 types/api/student/） ==================== */

export type OrgTreeNode = Api.StudentImportTree.OrgTreeNode
export type OrgTreeResponse = Api.StudentImportTree.OrgTreeResponse
export type DormTreeNode = Api.StudentImportTree.DormTreeNode
export type DormTreeResponse = Api.StudentImportTree.DormTreeResponse

/**
 * 获取组织架构树
 * 返回校区-院系-专业-班级的层级结构
 * 用于生成Excel导入模板的级联下拉
 */
export function fetchOrgTree() {
  return request.get<OrgTreeResponse>({
    url: '/api/v1/system/student/import/org-tree'
  })
}

/**
 * 获取住宿结构树
 * 返回校区-楼层-房间-床位的层级结构
 * 用于生成Excel导入模板的级联下拉
 */
export function fetchDormTree() {
  return request.get<DormTreeResponse>({
    url: '/api/v1/system/student/import/dorm-tree'
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
