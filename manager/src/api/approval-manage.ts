/**
 * 审批管理模块 API
 * 包含审批流程配置、审批操作等接口
 *
 * @module api/approval-manage
 * @author 陈鸿昇
 * @date 2026-01-17
 */

import request from '@/utils/http'

/** ==================== 类型别名（规范类型定义在 types/api/approval/） ==================== */

export type ApprovalAssignee = Api.ApprovalManage.ApprovalAssignee
export type ApprovalNode = Api.ApprovalManage.ApprovalNode
export type ApprovalFlow = Api.ApprovalManage.ApprovalFlow
export type ApprovalFlowQueryParams = Api.ApprovalManage.ApprovalFlowQueryParams
export type ApprovalFlowBinding = Api.ApprovalManage.ApprovalFlowBinding
export type ApprovalInstance = Api.ApprovalManage.ApprovalInstance
export type ApprovalInstanceQueryParams = Api.ApprovalManage.ApprovalInstanceQueryParams
export type ApprovalRecord = Api.ApprovalManage.ApprovalRecord
export type ApprovalRecordQueryParams = Api.ApprovalManage.ApprovalRecordQueryParams
export type ApprovalActionParams = Api.ApprovalManage.ApprovalActionParams

/** ==================== 流程配置接口 ==================== */

/**
 * 获取流程列表（分页）
 */
export function fetchGetFlowList(params: ApprovalFlowQueryParams) {
  return request.get<{
    list: ApprovalFlow[]
    total: number
    pageNum: number
    pageSize: number
  }>({
    url: '/api/v1/system/approval/flow/page',
    params
  })
}

/**
 * 获取所有流程（不分页）
 */
export function fetchGetAllFlows(businessType?: string) {
  return request.get<ApprovalFlow[]>({
    url: '/api/v1/system/approval/flow/all',
    params: { businessType }
  })
}

/**
 * 获取流程详情
 */
export function fetchGetFlowDetail(id: number) {
  return request.get<ApprovalFlow>({
    url: `/api/v1/system/approval/flow/${id}`
  })
}

/**
 * 新增流程
 */
export function fetchAddFlow(data: ApprovalFlow) {
  return request.post({
    url: '/api/v1/system/approval/flow',
    data,
    showSuccessMessage: true
  })
}

/**
 * 编辑流程
 */
export function fetchUpdateFlow(id: number, data: ApprovalFlow) {
  return request.put({
    url: `/api/v1/system/approval/flow/${id}`,
    data,
    showSuccessMessage: true
  })
}

/**
 * 删除流程
 */
export function fetchDeleteFlow(id: number) {
  return request.del({
    url: `/api/v1/system/approval/flow/${id}`,
    showSuccessMessage: true
  })
}

/**
 * 修改流程状态
 */
export function fetchUpdateFlowStatus(id: number, status: number) {
  return request.put({
    url: `/api/v1/system/approval/flow/${id}/status/${status}`,
    showSuccessMessage: true
  })
}

/** ==================== 流程绑定接口 ==================== */

/**
 * 获取所有流程绑定
 */
export function fetchGetAllBindings() {
  return request.get<ApprovalFlowBinding[]>({
    url: '/api/v1/system/approval/binding/list'
  })
}

/**
 * 获取业务类型绑定
 */
export function fetchGetBinding(businessType: string) {
  return request.get<ApprovalFlowBinding>({
    url: `/api/v1/system/approval/binding/${businessType}`
  })
}

/**
 * 绑定流程
 */
export function fetchBindFlow(data: { businessType: string; flowId: number; status?: number }) {
  return request.post({
    url: '/api/v1/system/approval/binding',
    data,
    showSuccessMessage: true
  })
}

/**
 * 解绑流程
 */
export function fetchUnbindFlow(businessType: string) {
  return request.del({
    url: `/api/v1/system/approval/binding/${businessType}`,
    showSuccessMessage: true
  })
}

/** ==================== 审批操作接口 ==================== */

/**
 * 获取审批实例详情
 */
export function fetchGetInstanceDetail(id: number) {
  return request.get<ApprovalInstance>({
    url: `/api/v1/system/approval/instance/${id}`
  })
}

/**
 * 根据业务查询审批实例
 */
export function fetchGetInstanceByBusiness(businessType: string, businessId: number) {
  return request.get<ApprovalInstance>({
    url: `/api/v1/system/approval/instance/business/${businessType}/${businessId}`
  })
}

/**
 * 执行审批
 */
export function fetchDoApprove(data: ApprovalActionParams) {
  return request.put({
    url: '/api/v1/system/approval/approve',
    data,
    showSuccessMessage: true
  })
}

/**
 * 撤回审批
 */
export function fetchWithdrawApproval(instanceId: number) {
  return request.put({
    url: `/api/v1/system/approval/withdraw/${instanceId}`,
    showSuccessMessage: true
  })
}

/** ==================== 待办/已办查询接口 ==================== */

/**
 * 获取待办审批列表（分页）
 */
export function fetchGetPendingList(params: ApprovalInstanceQueryParams) {
  return request.get<{
    list: ApprovalInstance[]
    total: number
    pageNum: number
    pageSize: number
  }>({
    url: '/api/v1/system/approval/pending/page',
    params
  })
}

/**
 * 获取待办审批数量统计
 */
export function fetchGetPendingCount() {
  return request.get<Record<string, number>>({
    url: '/api/v1/system/approval/pending/count'
  })
}

/**
 * 获取审批记录列表（分页）
 */
export function fetchGetRecordList(params: ApprovalRecordQueryParams) {
  return request.get<{
    list: ApprovalRecord[]
    total: number
    pageNum: number
    pageSize: number
  }>({
    url: '/api/v1/system/approval/record/page',
    params
  })
}

/**
 * 获取我的审批记录（分页）
 */
export function fetchGetMyRecordList(params: ApprovalRecordQueryParams) {
  return request.get<{
    list: ApprovalRecord[]
    total: number
    pageNum: number
    pageSize: number
  }>({
    url: '/api/v1/system/approval/record/my-page',
    params
  })
}

/**
 * 获取审批实例的审批记录
 */
export function fetchGetRecordsByInstance(instanceId: number) {
  return request.get<ApprovalRecord[]>({
    url: `/api/v1/system/approval/record/instance/${instanceId}`
  })
}
