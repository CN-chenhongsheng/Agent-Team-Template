/**
 * 分配管理模块 API
 * 包含配置管理、任务管理、结果管理、问卷管理等接口
 *
 * @module api/allocation
 * @author 陈鸿昇
 * @date 2025-02-02
 */

import request from '@/utils/http'

/** ==================== 配置管理 ==================== */

/**
 * 分页查询配置列表
 * @param params 查询参数
 */
export function fetchGetConfigPage(params: Api.Allocation.ConfigSearchParams) {
  return request.get<Api.Allocation.ConfigPageResponse>({
    url: '/api/v1/system/allocation/config/page',
    params
  })
}

/**
 * 获取配置详情
 * @param id 配置ID
 */
export function fetchGetConfigDetail(id: number) {
  return request.get<Api.Allocation.ConfigListItem>({
    url: `/api/v1/system/allocation/config/${id}`
  })
}

/**
 * 新增配置
 * @param data 配置数据
 */
export function fetchAddConfig(data: Api.Allocation.ConfigSaveParams) {
  return request.post<number>({
    url: '/api/v1/system/allocation/config',
    data,
    showSuccessMessage: true
  })
}

/**
 * 更新配置
 * @param id 配置ID
 * @param data 配置数据
 */
export function fetchUpdateConfig(id: number, data: Api.Allocation.ConfigSaveParams) {
  return request.put({
    url: `/api/v1/system/allocation/config/${id}`,
    data,
    showSuccessMessage: true
  })
}

/**
 * 删除配置
 * @param id 配置ID
 */
export function fetchDeleteConfig(id: number) {
  return request.del({
    url: `/api/v1/system/allocation/config/${id}`,
    showSuccessMessage: true
  })
}

/**
 * 复制配置
 * @param id 配置ID
 * @param newName 新配置名称
 */
export function fetchCopyConfig(id: number, newName: string) {
  return request.post<number>({
    url: `/api/v1/system/allocation/config/${id}/copy`,
    params: { newName },
    showSuccessMessage: true
  })
}

/**
 * 获取默认配置模板
 */
export function fetchGetConfigTemplate() {
  return request.get<Api.Allocation.ConfigSaveParams>({
    url: '/api/v1/system/allocation/config/default-template'
  })
}

/**
 * 更新配置状态
 * @param id 配置ID
 * @param status 状态值
 */
export function fetchUpdateConfigStatus(id: number, status: number) {
  return request.put({
    url: `/api/v1/system/allocation/config/${id}/status/${status}`,
    showSuccessMessage: true
  })
}

/**
 * 获取可用算法列表
 */
export function fetchGetAlgorithmList() {
  return request.get<Api.Allocation.AlgorithmOption[]>({
    url: '/api/v1/system/allocation/config/algorithms'
  })
}

/** ==================== 任务管理 ==================== */

/**
 * 分页查询任务列表
 * @param params 查询参数
 */
export function fetchGetTaskPage(params: Api.Allocation.TaskSearchParams) {
  return request.get<Api.Allocation.TaskPageResponse>({
    url: '/api/v1/system/allocation/task/page',
    params
  })
}

/**
 * 获取任务详情
 * @param id 任务ID
 */
export function fetchGetTaskDetail(id: number) {
  return request.get<Api.Allocation.TaskListItem>({
    url: `/api/v1/system/allocation/task/${id}`
  })
}

/**
 * 创建任务
 * @param data 任务数据
 */
export function fetchAddTask(data: Api.Allocation.TaskSaveParams) {
  return request.post<number>({
    url: '/api/v1/system/allocation/task',
    data,
    showSuccessMessage: true
  })
}

/**
 * 预览分配
 * @param data 预览参数
 */
export function fetchPreviewTask(data: Api.Allocation.TaskPreviewParams) {
  return request.post<Api.Allocation.TaskPreviewResult>({
    url: '/api/v1/system/allocation/task/preview',
    data
  })
}

/**
 * 执行任务
 * @param id 任务ID
 */
export function fetchExecuteTask(id: number) {
  return request.put({
    url: `/api/v1/system/allocation/task/${id}/execute`,
    showSuccessMessage: true
  })
}

/**
 * 获取任务进度
 * @param id 任务ID
 */
export function fetchGetTaskProgress(id: number) {
  return request.get<Api.Allocation.TaskProgress>({
    url: `/api/v1/system/allocation/task/${id}/progress`
  })
}

/**
 * 取消任务
 * @param id 任务ID
 */
export function fetchCancelTask(id: number) {
  return request.put({
    url: `/api/v1/system/allocation/task/${id}/cancel`,
    showSuccessMessage: true
  })
}

/** ==================== 结果管理 ==================== */

/**
 * 分页查询分配结果
 * @param params 查询参数
 */
export function fetchGetResultPage(params: Api.Allocation.ResultSearchParams) {
  return request.get<Api.Allocation.ResultPageResponse>({
    url: '/api/v1/system/allocation/result/page',
    params
  })
}

/**
 * 确认分配结果
 * @param taskId 任务ID
 * @param resultIds 结果ID列表
 */
export function fetchConfirmResults(taskId: number, resultIds: number[]) {
  return request.put({
    url: '/api/v1/system/allocation/result/confirm',
    params: { taskId },
    data: resultIds,
    showSuccessMessage: true
  })
}

/**
 * 确认全部结果
 * @param taskId 任务ID
 */
export function fetchConfirmAllResults(taskId: number) {
  return request.put({
    url: '/api/v1/system/allocation/result/confirm-all',
    params: { taskId },
    showSuccessMessage: true
  })
}

/**
 * 拒绝分配结果
 * @param taskId 任务ID
 * @param resultIds 结果ID列表
 * @param reason 拒绝原因
 */
export function fetchRejectResults(taskId: number, resultIds: number[], reason?: string) {
  return request.put({
    url: '/api/v1/system/allocation/result/reject',
    params: { taskId, reason },
    data: resultIds,
    showSuccessMessage: true
  })
}

/**
 * 调整分配
 * @param resultId 结果ID
 * @param newBedId 新床位ID
 * @param reason 调整原因
 */
export function fetchAdjustResult(resultId: number, newBedId: number, reason?: string) {
  return request.put({
    url: '/api/v1/system/allocation/result/adjust',
    data: { resultId, newBedId, reason },
    showSuccessMessage: true
  })
}

/**
 * 获取问题清单
 * @param taskId 任务ID
 * @param threshold 阈值，默认60
 */
export function fetchGetProblemList(taskId: number, threshold: number = 60) {
  return request.get<Api.Allocation.ResultListItem[]>({
    url: '/api/v1/system/allocation/result/problem-list',
    params: { taskId, threshold }
  })
}

/** ==================== 问卷管理 ==================== */

/**
 * 分页查询问卷列表
 * @param params 查询参数
 */
export function fetchGetSurveyPage(params: Api.Allocation.SurveySearchParams) {
  return request.get<Api.Allocation.SurveyPageResponse>({
    url: '/api/v1/system/allocation/survey/page',
    params
  })
}

/**
 * 获取问卷统计数据
 * @param params 统计参数
 */
export function fetchGetSurveyStatistics(params?: { enrollmentYear?: number; classCode?: string }) {
  return request.get<Api.Allocation.SurveyStatistics>({
    url: '/api/v1/system/allocation/survey/statistics',
    params
  })
}

/**
 * 获取学生问卷详情
 * @param studentId 学生ID
 */
export function fetchGetSurveyDetail(studentId: number) {
  return request.get<Api.Allocation.SurveyDetail>({
    url: `/api/v1/system/allocation/survey/${studentId}`
  })
}
