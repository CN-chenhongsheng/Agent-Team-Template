/**
 * 学校管理模块 API
 * 包含校区管理、院系管理、专业管理、班级管理等接口
 *
 * @module api/school-manage
 * @author 陈鸿昇
 * @date 2025-12-31
 */

import request from '@/utils/http'

/** ==================== 校区管理 ==================== */

/**
 * 获取校区分页列表
 * @param params 查询参数
 */
export function fetchGetCampusPage(params: Api.SystemManage.CampusSearchParams) {
  return request.get<Api.SystemManage.CampusPageResponse>({
    url: '/api/v1/system/campus/page',
    params
  })
}

/**
 * 获取校区树形列表
 * @param params 查询参数
 */
export function fetchGetCampusTree(params?: Api.SystemManage.CampusSearchParams) {
  return request.get<Api.SystemManage.CampusTreeList>({
    url: '/api/v1/system/campus/tree',
    params
  })
}

/**
 * 获取校区详情
 * @param id 校区ID
 */
export function fetchGetCampusDetail(id: number) {
  return request.get<Api.SystemManage.CampusListItem>({
    url: `/api/v1/system/campus/${id}`
  })
}

/**
 * 新增校区
 * @param data 校区数据
 */
export function fetchAddCampus(data: Api.SystemManage.CampusSaveParams) {
  return request.post({
    url: '/api/v1/system/campus',
    data,
    showSuccessMessage: true
  })
}

/**
 * 编辑校区
 * @param id 校区ID
 * @param data 校区数据
 */
export function fetchUpdateCampus(id: number, data: Api.SystemManage.CampusSaveParams) {
  return request.put({
    url: `/api/v1/system/campus/${id}`,
    data,
    showSuccessMessage: true
  })
}

/**
 * 删除校区
 * @param id 校区ID
 */
export function fetchDeleteCampus(id: number) {
  return request.del({
    url: `/api/v1/system/campus/${id}`,
    showSuccessMessage: true
  })
}

/**
 * 批量删除校区
 * @param ids 校区ID数组
 */
export function fetchBatchDeleteCampus(ids: number[]) {
  return request.del({
    url: '/api/v1/system/campus/batch',
    data: ids,
    showSuccessMessage: true
  })
}

/**
 * 修改校区状态
 * @param id 校区ID
 * @param status 状态：1正常 0停用
 */
export function fetchUpdateCampusStatus(id: number, status: number) {
  return request.put({
    url: `/api/v1/system/campus/${id}/status/${status}`,
    showSuccessMessage: true
  })
}

/** ==================== 院系管理 ==================== */

/**
 * 获取院系分页列表
 * @param params 查询参数
 */
export function fetchGetDepartmentPage(params: Api.SystemManage.DepartmentSearchParams) {
  return request.get<Api.SystemManage.DepartmentPageResponse>({
    url: '/api/v1/system/department/page',
    params
  })
}

/**
 * 获取院系树形列表
 * @param params 查询参数
 */
export function fetchGetDepartmentTree(params?: Api.SystemManage.DepartmentSearchParams) {
  return request.get<Api.SystemManage.DepartmentTreeList>({
    url: '/api/v1/system/department/tree',
    params
  })
}

/**
 * 获取院系详情
 * @param id 院系ID
 */
export function fetchGetDepartmentDetail(id: number) {
  return request.get<Api.SystemManage.DepartmentListItem>({
    url: `/api/v1/system/department/${id}`
  })
}

/**
 * 新增院系
 * @param data 院系数据
 */
export function fetchAddDepartment(data: Api.SystemManage.DepartmentSaveParams) {
  return request.post({
    url: '/api/v1/system/department',
    data,
    showSuccessMessage: true
  })
}

/**
 * 编辑院系
 * @param id 院系ID
 * @param data 院系数据
 */
export function fetchUpdateDepartment(id: number, data: Api.SystemManage.DepartmentSaveParams) {
  return request.put({
    url: `/api/v1/system/department/${id}`,
    data,
    showSuccessMessage: true
  })
}

/**
 * 删除院系
 * @param id 院系ID
 */
export function fetchDeleteDepartment(id: number) {
  return request.del({
    url: `/api/v1/system/department/${id}`,
    showSuccessMessage: true
  })
}

/**
 * 批量删除院系
 * @param ids 院系ID数组
 */
export function fetchBatchDeleteDepartment(ids: number[]) {
  return request.del({
    url: '/api/v1/system/department/batch',
    data: ids,
    showSuccessMessage: true
  })
}

/**
 * 修改院系状态
 * @param id 院系ID
 * @param status 状态：1正常 0停用
 */
export function fetchUpdateDepartmentStatus(id: number, status: number) {
  return request.put({
    url: `/api/v1/system/department/${id}/status/${status}`,
    showSuccessMessage: true
  })
}

/** ==================== 专业管理 ==================== */

/**
 * 获取专业分页列表
 * @param params 查询参数
 */
export function fetchGetMajorPage(params: Api.SystemManage.MajorSearchParams) {
  return request.get<Api.SystemManage.MajorPageResponse>({
    url: '/api/v1/system/major/page',
    params
  })
}

/**
 * 获取专业详情
 * @param id 专业ID
 */
export function fetchGetMajorDetail(id: number) {
  return request.get<Api.SystemManage.MajorListItem>({
    url: `/api/v1/system/major/${id}`
  })
}

/**
 * 新增专业
 * @param data 专业数据
 */
export function fetchAddMajor(data: Api.SystemManage.MajorSaveParams) {
  return request.post({
    url: '/api/v1/system/major',
    data,
    showSuccessMessage: true
  })
}

/**
 * 编辑专业
 * @param id 专业ID
 * @param data 专业数据
 */
export function fetchUpdateMajor(id: number, data: Api.SystemManage.MajorSaveParams) {
  return request.put({
    url: `/api/v1/system/major/${id}`,
    data,
    showSuccessMessage: true
  })
}

/**
 * 删除专业
 * @param id 专业ID
 */
export function fetchDeleteMajor(id: number) {
  return request.del({
    url: `/api/v1/system/major/${id}`,
    showSuccessMessage: true
  })
}

/**
 * 批量删除专业
 * @param ids 专业ID数组
 */
export function fetchBatchDeleteMajor(ids: number[]) {
  return request.del({
    url: '/api/v1/system/major/batch',
    data: ids,
    showSuccessMessage: true
  })
}

/**
 * 修改专业状态
 * @param id 专业ID
 * @param status 状态：1启用 0停用
 */
export function fetchUpdateMajorStatus(id: number, status: number) {
  return request.put({
    url: `/api/v1/system/major/${id}/status/${status}`,
    showSuccessMessage: true
  })
}

/** ==================== 班级管理 ==================== */

/**
 * 获取班级分页列表
 * @param params 查询参数
 */
export function fetchGetClassPage(params: Api.SystemManage.ClassSearchParams) {
  return request.get<Api.SystemManage.ClassPageResponse>({
    url: '/api/v1/system/class/page',
    params
  })
}

/**
 * 获取班级详情
 * @param id 班级ID
 */
export function fetchGetClassDetail(id: number) {
  return request.get<Api.SystemManage.ClassListItem>({
    url: `/api/v1/system/class/${id}`
  })
}

/**
 * 新增班级
 * @param data 班级数据
 */
export function fetchAddClass(data: Api.SystemManage.ClassSaveParams) {
  return request.post({
    url: '/api/v1/system/class',
    data,
    showSuccessMessage: true
  })
}

/**
 * 编辑班级
 * @param id 班级ID
 * @param data 班级数据
 */
export function fetchUpdateClass(id: number, data: Api.SystemManage.ClassSaveParams) {
  return request.put({
    url: `/api/v1/system/class/${id}`,
    data,
    showSuccessMessage: true
  })
}

/**
 * 删除班级
 * @param id 班级ID
 */
export function fetchDeleteClass(id: number) {
  return request.del({
    url: `/api/v1/system/class/${id}`,
    showSuccessMessage: true
  })
}

/**
 * 批量删除班级
 * @param ids 班级ID数组
 */
export function fetchBatchDeleteClass(ids: number[]) {
  return request.del({
    url: '/api/v1/system/class/batch',
    data: ids,
    showSuccessMessage: true
  })
}

/**
 * 修改班级状态
 * @param id 班级ID
 * @param status 状态：1启用 0停用
 */
export function fetchUpdateClassStatus(id: number, status: number) {
  return request.put({
    url: `/api/v1/system/class/${id}/status/${status}`,
    showSuccessMessage: true
  })
}

/** ==================== 学校层级管理 ==================== */

/**
 * 获取完整的学校层级树
 * 包含校区、院系、专业、班级的完整层级结构
 */
export function fetchGetSchoolHierarchy() {
  return request.get<Api.SystemManage.SchoolHierarchyResponse>({
    url: '/api/v1/system/school/hierarchy'
  })
}

/** ==================== 学年管理 ==================== */

/**
 * 获取学年分页列表
 * @param params 查询参数
 */
export function fetchGetAcademicYearPage(params: Api.SystemManage.AcademicYearSearchParams) {
  return request.get<Api.SystemManage.AcademicYearPageResponse>({
    url: '/api/v1/system/academic-year/page',
    params
  })
}

/**
 * 获取学年详情
 * @param id 学年ID
 */
export function fetchGetAcademicYearDetail(id: number) {
  return request.get<Api.SystemManage.AcademicYearListItem>({
    url: `/api/v1/system/academic-year/${id}`
  })
}

/**
 * 新增学年
 * @param data 学年数据
 */
export function fetchAddAcademicYear(data: Api.SystemManage.AcademicYearSaveParams) {
  return request.post({
    url: '/api/v1/system/academic-year',
    data,
    showSuccessMessage: true
  })
}

/**
 * 编辑学年
 * @param id 学年ID
 * @param data 学年数据
 */
export function fetchUpdateAcademicYear(id: number, data: Api.SystemManage.AcademicYearSaveParams) {
  return request.put({
    url: `/api/v1/system/academic-year/${id}`,
    data,
    showSuccessMessage: true
  })
}

/**
 * 删除学年
 * @param id 学年ID
 */
export function fetchDeleteAcademicYear(id: number) {
  return request.del({
    url: `/api/v1/system/academic-year/${id}`,
    showSuccessMessage: true
  })
}

/**
 * 批量删除学年
 * @param ids 学年ID数组
 */
export function fetchBatchDeleteAcademicYear(ids: number[]) {
  return request.del({
    url: '/api/v1/system/academic-year/batch',
    data: ids,
    showSuccessMessage: true
  })
}

/**
 * 修改学年状态
 * @param id 学年ID
 * @param status 状态：1正常 0停用
 */
export function fetchUpdateAcademicYearStatus(id: number, status: number) {
  return request.put({
    url: `/api/v1/system/academic-year/${id}/status/${status}`,
    showSuccessMessage: true
  })
}
