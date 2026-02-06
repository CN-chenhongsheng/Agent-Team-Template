/**
 * 学生管理模块 API
 *
 * @module api/student-manage
 * @author 陈鸿昇
 * @date 2026-02-03
 */

import request from '@/utils/http'
import { useUserStore } from '@/store/modules/user'

/**
 * 获取学生分页列表
 * @param params 查询参数
 */
export function fetchGetStudentPage(params: Api.StudentManage.StudentSearchParams) {
  return request.get<Api.StudentManage.StudentPageResponse>({
    url: '/api/v1/student/page',
    params
  })
}

/**
 * 获取学生详情
 * @param id 学生ID
 */
export function fetchGetStudentDetail(id: number) {
  return request.get<Api.StudentManage.StudentListItem>({
    url: `/api/v1/student/${id}`
  })
}

/**
 * 新增学生
 * @param data 学生数据
 */
export function fetchAddStudent(data: Api.StudentManage.StudentSaveParams) {
  return request.post({
    url: '/api/v1/student',
    data,
    showSuccessMessage: true
  })
}

/**
 * 更新学生
 * @param data 学生数据
 */
export function fetchUpdateStudent(data: Api.StudentManage.StudentSaveParams) {
  return request.put({
    url: '/api/v1/student',
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
    url: `/api/v1/student/${id}`,
    showSuccessMessage: true
  })
}

/**
 * 批量删除学生
 * @param ids 学生ID数组
 */
export function fetchBatchDeleteStudent(ids: number[]) {
  return request.del({
    url: '/api/v1/student/batch',
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
    url: `/api/v1/student/${id}/status/${status}`,
    showSuccessMessage: true
  })
}

/**
 * 学生导入（根据分片上传 merge 返回的 fileUrl）
 * 同步时返回 ImportResult，异步时返回 { taskId }，需轮询 fetchImportTaskResult
 * 可选携带 totalRows（前端扫描得到的有效行数），用于后端计算精确进度
 */
export function fetchImportStudents(
  fileUrl: string,
  totalRows?: number
): Promise<Api.StudentImport.ImportResponse> {
  const data: Api.StudentImport.ImportRequest = {
    fileUrl,
    totalRows
  }

  return request.post<Api.StudentImport.ImportResponse>({
    url: '/api/v1/student/import',
    data,
    showErrorMessage: true
  })
}

/**
 * 查询异步导入任务结果（轮询）
 */
export function fetchImportTaskResult(taskId: string) {
  return request.get<Api.StudentImport.ImportTaskVO>({
    url: `/api/v1/student/import/task/${taskId}`,
    showErrorMessage: true
  })
}

/**
 * SSE 阶段事件数据
 */
export interface SSEStageData {
  stage: 'parsing' | 'importing' | 'complete'
  message: string
  totalRows?: number
}

/**
 * SSE 进度事件数据
 */
export interface SSEProgressData {
  percent: number
  processed: number
  total: number
  successCount?: number
  failCount?: number
}

/**
 * SSE 完成事件数据
 */
export interface SSECompleteData {
  status: 'success' | 'failed'
  result?: Api.StudentImport.ImportResult
}

/**
 * SSE 订阅回调
 */
export interface SSEImportCallbacks {
  onStage: (data: SSEStageData) => void
  onProgress: (data: SSEProgressData) => void
  onComplete: (data: SSECompleteData) => void
  onError: (error: string) => void
}

/**
 * SSE 订阅导入进度
 * 实时接收后端推送的导入进度，无需轮询
 *
 * @param taskId 任务 ID
 * @param callbacks 事件回调
 * @returns 取消订阅函数
 */
export function subscribeImportProgress(taskId: string, callbacks: SSEImportCallbacks): () => void {
  // 获取 API 基础 URL
  const baseUrl = import.meta.env.VITE_API_PROXY_URL || ''

  // 获取当前用户的 accessToken（EventSource 不支持自定义 headers，需要通过 URL 参数传递）
  const userStore = useUserStore()
  const token = userStore.accessToken

  // 构建带 token 的 SSE URL
  const sseUrl = `${baseUrl}/v1/student/import/progress/${taskId}?token=${encodeURIComponent(token)}`

  console.log('[SSE] 创建连接:', sseUrl.replace(token, '***'))

  const eventSource = new EventSource(sseUrl)

  // 监听阶段变化事件
  eventSource.addEventListener('stage', (event: MessageEvent) => {
    try {
      const data: SSEStageData = JSON.parse(event.data)
      console.log('[SSE] 收到 stage 事件:', data)
      callbacks.onStage(data)
    } catch (e) {
      console.error('[SSE] 解析 stage 事件失败:', e)
    }
  })

  // 监听进度更新事件
  eventSource.addEventListener('progress', (event: MessageEvent) => {
    try {
      const data: SSEProgressData = JSON.parse(event.data)
      // 进度事件较频繁，只在关键节点打印日志
      if (data.percent % 10 === 0 || data.percent >= 95) {
        console.log('[SSE] 收到 progress 事件:', data)
      }
      callbacks.onProgress(data)
    } catch (e) {
      console.error('[SSE] 解析 progress 事件失败:', e)
    }
  })

  // 监听完成事件
  eventSource.addEventListener('complete', (event: MessageEvent) => {
    try {
      const data: SSECompleteData = JSON.parse(event.data)
      console.log('[SSE] 收到 complete 事件:', data)
      callbacks.onComplete(data)
      // 完成后关闭连接
      eventSource.close()
    } catch (e) {
      console.error('[SSE] 解析 complete 事件失败:', e)
    }
  })

  // 监听错误事件
  eventSource.addEventListener('error', (event: MessageEvent) => {
    try {
      const data = JSON.parse(event.data)
      console.error('[SSE] 收到 error 事件:', data)
      callbacks.onError(data.message || '导入失败')
      eventSource.close()
    } catch (e) {
      // SSE 连接错误（非服务端推送的错误）
      console.error('[SSE] 连接错误:', e)
    }
  })

  // 监听 SSE 连接错误
  eventSource.onerror = (event) => {
    console.error('[SSE] 连接错误:', event)
    // 检查连接状态
    if (eventSource.readyState === EventSource.CLOSED) {
      console.log('[SSE] 连接已关闭')
    } else if (eventSource.readyState === EventSource.CONNECTING) {
      console.log('[SSE] 正在重连...')
    } else {
      // 连接失败，通知回调
      callbacks.onError('SSE 连接失败，将自动降级到轮询模式')
      eventSource.close()
    }
  }

  // 返回取消订阅函数
  return () => {
    console.log('[SSE] 手动关闭连接')
    eventSource.close()
  }
}
