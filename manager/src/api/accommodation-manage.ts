/**
 * 住宿管理模块 API
 * 包含人员管理、入住管理、调宿管理、退宿管理、留宿管理等接口
 *
 * @module api/accommodation-manage
 * @author 陈鸿昇
 * @date 2026-01-06
 */

import request from '@/utils/http'

/** ==================== 人员管理（学生） ==================== */

/**
 * 获取学生分页列表
 * @param params 查询参数
 */
export function fetchGetStudentPage(params: Api.AccommodationManage.StudentSearchParams) {
  return request.get<Api.AccommodationManage.StudentPageResponse>({
    url: '/api/v1/system/student/page',
    params
  })
}

/**
 * 获取学生详情
 * @param id 学生ID
 */
export function fetchGetStudentDetail(id: number) {
  return request.get<Api.AccommodationManage.StudentListItem>({
    url: `/api/v1/system/student/${id}`
  })
}

/**
 * 新增学生
 * @param data 学生数据
 */
export function fetchAddStudent(data: Api.AccommodationManage.StudentSaveParams) {
  return request.post({
    url: '/api/v1/system/student',
    data,
    showSuccessMessage: true
  })
}

/**
 * 更新学生
 * @param data 学生数据
 */
export function fetchUpdateStudent(data: Api.AccommodationManage.StudentSaveParams) {
  return request.put({
    url: '/api/v1/system/student',
    data,
    showSuccessMessage: true
  })
}

/**
 * 删除学生
 * @param id 学生ID
 */
export function fetchDeleteStudent(id: number) {
  return request.del({
    url: `/api/v1/system/student/${id}`,
    showSuccessMessage: true
  })
}

/**
 * 批量删除学生
 * @param ids 学生ID数组
 */
export function fetchBatchDeleteStudent(ids: number[]) {
  return request.del({
    url: '/api/v1/system/student/batch',
    data: ids,
    showSuccessMessage: true
  })
}

/**
 * 更新学生状态
 * @param id 学生ID
 * @param status 状态：1正常 0停用
 */
export function fetchUpdateStudentStatus(id: number, status: number) {
  return request.put({
    url: `/api/v1/system/student/${id}/status/${status}`,
    showSuccessMessage: true
  })
}

/** ==================== 入住管理 ==================== */

/**
 * 获取入住管理分页列表
 * @param params 查询参数
 */
export function fetchGetCheckInPage(params: Api.AccommodationManage.CheckInSearchParams) {
  return request.get<Api.AccommodationManage.CheckInPageResponse>({
    url: '/api/v1/system/check-in/page',
    params
  })
}

/**
 * 获取入住管理详情
 * @param id 入住记录ID
 */
export function fetchGetCheckInDetail(id: number) {
  return request.get<Api.AccommodationManage.CheckInListItem>({
    url: `/api/v1/system/check-in/${id}`
  })
}

/**
 * 新增入住管理
 * @param data 入住管理数据
 */
export function fetchAddCheckIn(data: Api.AccommodationManage.CheckInSaveParams) {
  return request.post({
    url: '/api/v1/system/check-in',
    data,
    showSuccessMessage: true
  })
}

/**
 * 管理员直接分配床位（跳过审批流程）
 * 用于可视化视图中管理员直接将学生分配到空床位
 * @param data 入住管理数据
 */
export function fetchAdminAssignBed(data: Api.AccommodationManage.CheckInSaveParams) {
  return request.post({
    url: '/api/v1/system/check-in/admin-assign',
    data,
    showSuccessMessage: true
  })
}

/**
 * 更新入住管理
 * @param data 入住管理数据
 */
export function fetchUpdateCheckIn(data: Api.AccommodationManage.CheckInSaveParams) {
  return request.put({
    url: '/api/v1/system/check-in',
    data,
    showSuccessMessage: true
  })
}

/**
 * 删除入住管理
 * @param id 入住记录ID
 */
export function fetchDeleteCheckIn(id: number) {
  return request.del({
    url: `/api/v1/system/check-in/${id}`,
    showSuccessMessage: true
  })
}

/**
 * 批量删除入住管理
 * @param ids 入住记录ID数组
 */
export function fetchBatchDeleteCheckIn(ids: number[]) {
  return request.del({
    url: '/api/v1/system/check-in/batch',
    data: ids,
    showSuccessMessage: true
  })
}

/** ==================== 调宿管理 ==================== */

/**
 * 获取调宿管理分页列表
 * @param params 查询参数
 */
export function fetchGetTransferPage(params: Api.AccommodationManage.TransferSearchParams) {
  return request.get<Api.AccommodationManage.TransferPageResponse>({
    url: '/api/v1/system/transfer/page',
    params
  })
}

/**
 * 获取调宿管理详情
 * @param id 调宿记录ID
 */
export function fetchGetTransferDetail(id: number) {
  return request.get<Api.AccommodationManage.TransferListItem>({
    url: `/api/v1/system/transfer/${id}`
  })
}

/**
 * 新增调宿管理
 * @param data 调宿管理数据
 */
export function fetchAddTransfer(data: Api.AccommodationManage.TransferSaveParams) {
  return request.post({
    url: '/api/v1/system/transfer',
    data,
    showSuccessMessage: true
  })
}

/**
 * 更新调宿管理
 * @param data 调宿管理数据
 */
export function fetchUpdateTransfer(data: Api.AccommodationManage.TransferSaveParams) {
  return request.put({
    url: '/api/v1/system/transfer',
    data,
    showSuccessMessage: true
  })
}

/**
 * 删除调宿管理
 * @param id 调宿记录ID
 */
export function fetchDeleteTransfer(id: number) {
  return request.del({
    url: `/api/v1/system/transfer/${id}`,
    showSuccessMessage: true
  })
}

/**
 * 批量删除调宿管理
 * @param ids 调宿记录ID数组
 */
export function fetchBatchDeleteTransfer(ids: number[]) {
  return request.del({
    url: '/api/v1/system/transfer/batch',
    data: ids,
    showSuccessMessage: true
  })
}

/** ==================== 退宿管理 ==================== */

/**
 * 获取退宿管理分页列表
 * @param params 查询参数
 */
export function fetchGetCheckOutPage(params: Api.AccommodationManage.CheckOutSearchParams) {
  return request.get<Api.AccommodationManage.CheckOutPageResponse>({
    url: '/api/v1/system/check-out/page',
    params
  })
}

/**
 * 获取退宿管理详情
 * @param id 退宿记录ID
 */
export function fetchGetCheckOutDetail(id: number) {
  return request.get<Api.AccommodationManage.CheckOutListItem>({
    url: `/api/v1/system/check-out/${id}`
  })
}

/**
 * 新增退宿管理
 * @param data 退宿管理数据
 */
export function fetchAddCheckOut(data: Api.AccommodationManage.CheckOutSaveParams) {
  return request.post({
    url: '/api/v1/system/check-out',
    data,
    showSuccessMessage: true
  })
}

/**
 * 更新退宿管理
 * @param data 退宿管理数据
 */
export function fetchUpdateCheckOut(data: Api.AccommodationManage.CheckOutSaveParams) {
  return request.put({
    url: '/api/v1/system/check-out',
    data,
    showSuccessMessage: true
  })
}

/**
 * 删除退宿管理
 * @param id 退宿记录ID
 */
export function fetchDeleteCheckOut(id: number) {
  return request.del({
    url: `/api/v1/system/check-out/${id}`,
    showSuccessMessage: true
  })
}

/**
 * 批量删除退宿管理
 * @param ids 退宿记录ID数组
 */
export function fetchBatchDeleteCheckOut(ids: number[]) {
  return request.del({
    url: '/api/v1/system/check-out/batch',
    data: ids,
    showSuccessMessage: true
  })
}

/** ==================== 留宿管理 ==================== */

/**
 * 获取留宿管理分页列表
 * @param params 查询参数
 */
export function fetchGetStayPage(params: Api.AccommodationManage.StaySearchParams) {
  return request.get<Api.AccommodationManage.StayPageResponse>({
    url: '/api/v1/system/stay/page',
    params
  })
}

/**
 * 获取留宿管理详情
 * @param id 留宿记录ID
 */
export function fetchGetStayDetail(id: number) {
  return request.get<Api.AccommodationManage.StayListItem>({
    url: `/api/v1/system/stay/${id}`
  })
}

/**
 * 新增留宿管理
 * @param data 留宿管理数据
 */
export function fetchAddStay(data: Api.AccommodationManage.StaySaveParams) {
  return request.post({
    url: '/api/v1/system/stay',
    data,
    showSuccessMessage: true
  })
}

/**
 * 更新留宿管理
 * @param data 留宿管理数据
 */
export function fetchUpdateStay(data: Api.AccommodationManage.StaySaveParams) {
  return request.put({
    url: '/api/v1/system/stay',
    data,
    showSuccessMessage: true
  })
}

/**
 * 删除留宿管理
 * @param id 留宿记录ID
 */
export function fetchDeleteStay(id: number) {
  return request.del({
    url: `/api/v1/system/stay/${id}`,
    showSuccessMessage: true
  })
}

/**
 * 批量删除留宿管理
 * @param ids 留宿记录ID数组
 */
export function fetchBatchDeleteStay(ids: number[]) {
  return request.del({
    url: '/api/v1/system/stay/batch',
    data: ids,
    showSuccessMessage: true
  })
}
