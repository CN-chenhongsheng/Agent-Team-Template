/**
 * 审批管理模块 API
 * 包含审批流程配置、审批操作等接口
 *
 * @module api/approval-manage
 * @author 陈鸿昇
 * @date 2026-01-17
 */

import request from '@/utils/http'

/** ==================== 类型定义 ==================== */

/** 审批人 */
export interface ApprovalAssignee {
  id?: number
  nodeId?: number
  assigneeType: number // 1角色 2用户
  assigneeId: number
  assigneeName?: string
  assigneeTypeText?: string
}

/** 审批节点 */
export interface ApprovalNode {
  id?: number
  flowId?: number
  nodeName: string
  nodeOrder: number
  nodeType: number // 1串行 2会签 3或签
  nodeTypeText?: string
  rejectAction: number // 1直接结束 2退回申请人 3退回上一节点
  rejectActionText?: string
  remark?: string
  assignees: ApprovalAssignee[]
  createTime?: string
  // 节点位置坐标（用于保存用户自定义布局）
  x?: number
  y?: number
  // 临时唯一标识符（用于新节点追踪，不提交到后端）
  tempId?: string
}

/** 审批流程 */
export interface ApprovalFlow {
  id?: number
  flowName: string
  flowCode: string
  businessType: string
  businessTypeText?: string
  description?: string
  status: number
  statusText?: string
  remark?: string
  nodes?: ApprovalNode[]
  bound?: boolean
  createTime?: string
  updateTime?: string
}

/** 流程查询参数 */
export interface ApprovalFlowQueryParams {
  flowName?: string
  flowCode?: string
  businessType?: string
  status?: number
  pageNum?: number
  pageSize?: number
}

/** 流程绑定 */
export interface ApprovalFlowBinding {
  id?: number
  businessType: string
  businessTypeText?: string
  flowId: number
  flowName?: string
  flowCode?: string
  status: number
  statusText?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

/** 审批实例 */
export interface ApprovalInstance {
  id: number
  flowId: number
  flowName: string
  businessType: string
  businessTypeText?: string
  businessId: number
  applicantId: number
  applicantName: string
  /** 学生基本信息（当业务类型为学生相关业务时返回） */
  studentInfo?: Api.Common.StudentBasicInfo
  currentNodeId?: number
  currentNodeName?: string
  status: number
  statusText?: string
  startTime?: string
  endTime?: string
  remark?: string
  nodes?: ApprovalNode[]
  records?: ApprovalRecord[]
  canApprove?: boolean
  createTime?: string
}

/** 审批实例查询参数 */
export interface ApprovalInstanceQueryParams {
  businessType?: string
  applicantName?: string
  flowName?: string
  status?: number
  pageNum?: number
  pageSize?: number
}

/** 审批记录 */
export interface ApprovalRecord {
  id: number
  instanceId: number
  nodeId: number
  nodeName: string
  approverId: number
  approverName: string
  action: number // 1通过 2拒绝
  actionText?: string
  opinion?: string
  approveTime: string
  businessType?: string
  businessTypeText?: string
  applicantName?: string
  flowName?: string
}

/** 审批记录查询参数 */
export interface ApprovalRecordQueryParams {
  businessType?: string
  applicantName?: string
  approverName?: string
  action?: number
  pageNum?: number
  pageSize?: number
}

/** 审批操作参数 */
export interface ApprovalActionParams {
  instanceId: number
  action: number // 1通过 2拒绝
  opinion?: string
}

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
    url: '/api/v1/system/approval/flow/list',
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
  return request.post({
    url: '/api/v1/system/approval/approve',
    data,
    showSuccessMessage: true
  })
}

/**
 * 撤回审批
 */
export function fetchWithdrawApproval(instanceId: number) {
  return request.post({
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
    url: '/api/v1/system/approval/pending/list',
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
    url: '/api/v1/system/approval/record/list',
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
    url: '/api/v1/system/approval/record/my',
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
