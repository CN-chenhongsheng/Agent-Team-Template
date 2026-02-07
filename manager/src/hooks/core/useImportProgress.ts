/**
 * useImportProgress - 通用导入进度 Hook
 *
 * 提供导入任务的进度监听、SSE 订阅、轮询降级、Notification 显示等功能。
 * 可用于学生导入、教师导入、房间导入等多种导入场景。
 *
 * ## 主要功能
 *
 * 1. 状态管理 - 响应式的导入进度状态
 * 2. SSE 订阅 - 实时接收后端推送的进度
 * 3. 轮询降级 - SSE 失败时自动降级到轮询
 * 4. Notification - 统一的进度通知组件
 *
 * ## 使用示例
 *
 * ```typescript
 * const {
 *   state,
 *   startImport,
 *   stopImport
 * } = useImportProgress({
 *   title: '学生导入',
 *   subscribeApi: subscribeImportProgress,
 *   pollApi: fetchImportTaskResult,
 *   onComplete: (result) => {
 *     handleRefresh()
 *   },
 *   onViewDetail: (result) => {
 *     showResultDialog(result)
 *   }
 * })
 * ```
 *
 * @module useImportProgress
 * @author 陈鸿昇
 */

import { reactive, h, defineComponent } from 'vue'

// ===================== 类型定义 =====================

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
export interface SSECompleteData<TResult = ImportResult> {
  status: 'success' | 'failed'
  result?: TResult
}

/**
 * SSE 订阅回调
 */
export interface SSEImportCallbacks<TResult = ImportResult> {
  onStage: (data: SSEStageData) => void
  onProgress: (data: SSEProgressData) => void
  onComplete: (data: SSECompleteData<TResult>) => void
  onError: (error: string) => void
}

/**
 * 导入结果（通用基础接口）
 * 实际使用时可以通过泛型传入更具体的类型
 */
export interface ImportResult {
  totalRows: number
  successCount: number
  failCount: number
  errors?: unknown // 允许任意错误格式
}

/**
 * 导入任务 VO（通用）
 */
export interface ImportTaskVO<TResult = ImportResult> {
  taskId: string
  status: 'pending' | 'processing' | 'success' | 'failed'
  progressPercent?: number
  result?: TResult
}

/**
 * 导入进度状态
 */
export interface ImportProgressState {
  stage: 'parsing' | 'importing' | 'complete'
  stageMessage: string
  status: 'pending' | 'processing' | 'success' | 'failed'
  percent: number
  processed: number // 已处理行数（SSE 推送的 processed）
  totalRows: number
  successCount: number
  failCount: number
}

/**
 * Hook 配置选项
 */
export interface UseImportProgressOptions<TResult = ImportResult> {
  /** Notification 标题（如"学生导入"） */
  title: string
  /** SSE 订阅 API */
  subscribeApi: (taskId: string, callbacks: SSEImportCallbacks<TResult>) => () => void
  /** 轮询 API（降级用） */
  pollApi: (taskId: string) => Promise<ImportTaskVO<TResult> | null>
  /** 导入完成回调 */
  onComplete?: (result: TResult, status: 'success' | 'failed') => void
  /** 查看详情回调 */
  onViewDetail?: (result: TResult) => void
  /** 轮询间隔（毫秒），默认 5000 */
  pollingInterval?: number
  /** 轮询超时（毫秒），默认 30 分钟 */
  pollingTimeout?: number
  /** 最大轮询次数，默认 360 */
  maxPollingCount?: number
}

/**
 * Hook 返回值
 */
export interface UseImportProgressReturn<TResult = ImportResult> {
  /** 响应式进度状态 */
  state: ImportProgressState
  /** 开始监听导入进度 */
  startImport: (taskId: string) => void
  /** 停止监听（清理资源） */
  stopImport: () => void
  /** 最后一次导入结果 */
  lastResult: TResult | null
}

// ===================== Hook 实现 =====================

/**
 * 通用导入进度 Hook
 */
export function useImportProgress<TResult extends ImportResult = ImportResult>(
  options: UseImportProgressOptions<TResult>
): UseImportProgressReturn<TResult> {
  const {
    title,
    subscribeApi,
    pollApi,
    onComplete,
    onViewDetail,
    pollingInterval = 5000,
    pollingTimeout = 30 * 60 * 1000,
    maxPollingCount = 360
  } = options

  // ==================== 内部状态 ====================

  // 响应式进度状态
  const state = reactive<ImportProgressState>({
    stage: 'parsing',
    stageMessage: '',
    status: 'processing',
    percent: 0,
    processed: 0,
    totalRows: 0,
    successCount: 0,
    failCount: 0
  })

  // 内部变量
  let sseUnsubscribe: (() => void) | null = null
  let pollingTimer: ReturnType<typeof setInterval> | null = null
  let pollingStartTime = 0
  let pollingCount = 0
  let notificationInstance: ReturnType<typeof ElNotification> | null = null
  let notificationClose: (() => void) | null = null
  let lastImportResult: TResult | null = null
  let currentTaskId: string | null = null

  // ==================== 状态更新 ====================

  const updateState = (updates: Partial<ImportProgressState>) => {
    if (updates.stage !== undefined) state.stage = updates.stage
    if (updates.stageMessage !== undefined) state.stageMessage = updates.stageMessage
    if (updates.status !== undefined) state.status = updates.status
    if (updates.percent !== undefined) state.percent = updates.percent
    if (updates.processed !== undefined) state.processed = updates.processed
    if (updates.totalRows !== undefined) state.totalRows = updates.totalRows
    if (updates.successCount !== undefined) state.successCount = updates.successCount
    if (updates.failCount !== undefined) state.failCount = updates.failCount
  }

  const resetState = () => {
    updateState({
      stage: 'parsing',
      stageMessage: '',
      status: 'processing',
      percent: 0,
      processed: 0,
      totalRows: 0,
      successCount: 0,
      failCount: 0
    })
  }

  // ==================== Notification 组件 ====================

  const NotificationContent = defineComponent({
    name: 'ImportNotificationContent',
    setup() {
      const handleViewDetail = () => {
        if (lastImportResult && onViewDetail) {
          onViewDetail(lastImportResult)
        }
        closeNotification()
      }

      return () => {
        // 根据阶段和状态生成进度文本
        const getProgressText = () => {
          if (state.status === 'success') {
            return `导入完成（共 ${state.totalRows || 0} 行）`
          }
          if (state.status === 'failed') {
            return `导入失败（已处理 ${state.totalRows || 0} 行）`
          }
          if (state.stage === 'parsing') {
            if (state.totalRows === 0 || state.percent === 0) {
              // 大文件：不知道总行数，只显示已解析行数
              return `解析中：已解析 ${state.processed.toLocaleString()} 行`
            }
            // 小文件：有总行数，显示百分比进度
            return `解析中：${state.percent}% · 共 ${state.totalRows.toLocaleString()} 行`
          }
          // 导入阶段显示已成功处理的行数（successCount），而不是 totalRows
          const processed = state.successCount || 0
          return `导入中：${state.percent}%${processed > 0 ? ` · 已处理 ${processed.toLocaleString()} 行` : ''}`
        }

        // 根据阶段生成描述文字
        const getStageDescription = () => {
          if (state.status !== 'processing') return null
          if (state.stage === 'parsing') {
            return (
              state.stageMessage || '服务器正在解析导入文件，请耐心等待，不要刷新或关闭当前页面。'
            )
          }
          return '正在导入数据，请耐心等待，不要刷新或关闭当前页面。'
        }

        const progressText = getProgressText()
        const stageDescription = getStageDescription()
        const isCompleted = state.status === 'success' || state.status === 'failed'
        const showIndeterminate = state.status === 'processing' && state.percent === 0

        // 完成状态的简洁布局
        if (isCompleted) {
          const { successCount, failCount, totalRows } = state
          return h(
            'div',
            { style: 'display:flex;flex-direction:column;' },
            [
              // 统计信息
              h('div', { style: 'display:flex;align-items:center;gap:16px;font-size:13px;' }, [
                h('span', { style: 'color:var(--el-color-success);font-weight:500;' }, [
                  h('span', { style: 'margin-right:4px;' }, '✓'),
                  `成功 ${successCount.toLocaleString()} 行`
                ]),
                failCount > 0
                  ? h('span', { style: 'color:var(--el-color-danger);font-weight:500;' }, [
                      h('span', { style: 'margin-right:4px;' }, '✗'),
                      `失败 ${failCount.toLocaleString()} 行`
                    ])
                  : h('span', { style: 'color:var(--el-text-color-secondary);' }, '失败 0 行')
              ]),
              // 总计信息
              h(
                'div',
                {
                  style: 'font-size:12px;color:var(--el-text-color-regular);'
                },
                `共处理 ${totalRows.toLocaleString()} 行数据`
              ),
              // 查看详情按钮
              onViewDetail &&
                h('div', { style: 'margin-top:2px;' }, [
                  h(
                    'span',
                    {
                      style:
                        'padding:0px 12px;color:var(--el-color-primary);font-size:12px;cursor:pointer;border-radius:4px;background-color:var(--el-color-primary-light-9);transition:background-color 0.2s;user-select:none;display:inline-block;',
                      onMouseenter: (e: MouseEvent) => {
                        const target = e.target as HTMLElement
                        if (target) target.style.backgroundColor = 'var(--el-color-primary-light-8)'
                      },
                      onMouseleave: (e: MouseEvent) => {
                        const target = e.target as HTMLElement
                        if (target) target.style.backgroundColor = 'var(--el-color-primary-light-9)'
                      },
                      onClick: handleViewDetail
                    },
                    '查看详情'
                  )
                ])
            ].filter(Boolean)
          )
        }

        // 处理中状态的布局
        // 大文件解析阶段（totalRows === 0）：不显示进度条，只显示已解析行数
        const isLargeFileParsing = state.stage === 'parsing' && state.totalRows === 0

        return h('div', { style: 'display:flex;flex-direction:column;' }, [
          h(
            'div',
            { style: 'line-height:1.4;' },
            [
              stageDescription &&
                h(
                  'div',
                  {
                    style: 'font-size:12px;color:var(--el-text-color-regular);margin-bottom:4px;'
                  },
                  stageDescription
                ),
              state.stage === 'importing' &&
                state.totalRows > 0 &&
                h(
                  'div',
                  {
                    style: 'font-size:12px;color:var(--el-text-color-regular);line-height:1.4;'
                  },
                  `成功 ${state.successCount} · 失败 ${state.failCount}`
                )
            ].filter(Boolean)
          ),
          // 大文件解析阶段：只显示文字，不显示进度条
          isLargeFileParsing
            ? h(
                'div',
                {
                  style:
                    'font-size:14px;color:var(--el-text-color-primary);margin-top:4px;font-weight:500;'
                },
                progressText
              )
            : h('div', { style: 'display:flex;align-items:center;gap:8px;margin-top:4px;' }, [
                h(ElProgress, {
                  percentage: showIndeterminate ? 100 : state.percent,
                  'stroke-width': 6,
                  showText: false,
                  indeterminate: showIndeterminate,
                  style: 'flex:1;'
                }),
                h(
                  'span',
                  {
                    style: 'font-size:12px;color:var(--el-text-color-regular);white-space:nowrap;'
                  },
                  progressText
                )
              ])
        ])
      }
    }
  })

  // ==================== Notification 管理 ====================

  const showNotification = (task?: ImportTaskVO<TResult>) => {
    const isCompleted = state.status === 'success' || state.status === 'failed'

    // 如果传入了 task，先更新状态
    if (task) {
      updateState({
        status: task.status,
        percent: task.progressPercent ?? 0,
        totalRows: task.result?.totalRows ?? 0,
        successCount: task.result?.successCount ?? 0,
        failCount: task.result?.failCount ?? 0
      })
    }

    // 只在第一次创建 Notification
    if (!notificationInstance) {
      const n = ElNotification({
        title,
        message: h(NotificationContent),
        type: isCompleted ? (state.status === 'success' ? 'success' : 'warning') : 'info',
        duration: 0,
        position: 'top-right',
        customClass: 'import-progress-notification'
      })
      notificationInstance = n
      notificationClose = n.close
    } else {
      // 更新类型
      ;(notificationInstance as any).type = isCompleted
        ? state.status === 'success'
          ? 'success'
          : 'warning'
        : 'info'
    }
  }

  const closeNotification = () => {
    if (notificationClose) {
      notificationClose()
      notificationClose = null
    }
    notificationInstance = null
  }

  // ==================== SSE 事件处理 ====================

  const handleSSEStage = (data: SSEStageData) => {
    // 切换阶段时重置进度相关状态
    updateState({
      stage: data.stage,
      stageMessage: data.message,
      percent: 0,
      processed: 0, // 切换阶段时重置已处理行数
      totalRows: data.totalRows ?? state.totalRows,
      // 切换到导入阶段时重置计数器（解析阶段不需要这些）
      successCount: data.stage === 'importing' ? 0 : state.successCount,
      failCount: data.stage === 'importing' ? 0 : state.failCount
    })
  }

  const handleSSEProgress = (data: SSEProgressData) => {
    const updates: Partial<ImportProgressState> = {
      percent: data.percent,
      processed: data.processed,
      totalRows: data.total
    }
    if (data.successCount !== undefined) updates.successCount = data.successCount
    if (data.failCount !== undefined) updates.failCount = data.failCount
    updateState(updates)
  }

  const handleSSEComplete = (data: SSECompleteData<TResult>) => {
    updateState({
      stage: 'complete',
      status: data.status,
      percent: 100,
      totalRows: data.result?.totalRows ?? state.totalRows,
      successCount: data.result?.successCount ?? 0,
      failCount: data.result?.failCount ?? 0
    })

    if (data.result) {
      lastImportResult = data.result
    }

    // 更新 Notification 类型
    if (notificationInstance) {
      ;(notificationInstance as any).type = data.status === 'success' ? 'success' : 'warning'
    }

    // 触发完成回调
    if (data.result && onComplete) {
      onComplete(data.result, data.status)
    }

    // 提示消息
    if (data.result && (data.result.failCount ?? 0) > 0) {
      ElMessage.warning('导入结束，存在失败记录')
    } else if (data.status === 'success') {
      ElMessage.success('导入完成')
    }
  }

  const handleSSEError = (errorMsg: string) => {
    console.warn('[SSE] 错误，降级到轮询:', errorMsg)
    if (sseUnsubscribe) {
      sseUnsubscribe()
      sseUnsubscribe = null
    }
    startPolling()
  }

  // ==================== 轮询逻辑 ====================

  const startPolling = () => {
    if (!currentTaskId) return
    console.log('[Polling] 开始轮询，taskId:', currentTaskId)
    updateState({ stage: 'importing' })
    poll()
    if (!pollingTimer) {
      pollingTimer = setInterval(poll, pollingInterval)
    }
  }

  const poll = async () => {
    if (!currentTaskId) return

    try {
      // 检查超时
      const elapsed = Date.now() - pollingStartTime
      if (elapsed > pollingTimeout) {
        stopPolling()
        closeNotification()
        ElMessage.error('导入超时，请稍后查看导入结果')
        return
      }

      // 检查最大轮询次数
      pollingCount++
      if (pollingCount > maxPollingCount) {
        stopPolling()
        closeNotification()
        ElMessage.error('导入超时，请稍后查看导入结果')
        return
      }

      const task = await pollApi(currentTaskId)
      if (!task) return

      if (task.status === 'processing') {
        showNotification(task)
      }

      if (task.status === 'success' && task.result) {
        stopPolling()
        lastImportResult = task.result
        updateState({
          status: 'success',
          percent: 100,
          totalRows: task.result.totalRows,
          successCount: task.result.successCount,
          failCount: task.result.failCount
        })
        showNotification(task)
        if (onComplete) onComplete(task.result, 'success')
        if ((task.result.failCount ?? 0) > 0) {
          ElMessage.warning('导入结束，存在失败记录')
        } else {
          ElMessage.success('导入完成')
        }
      } else if (task.status === 'failed' && task.result) {
        stopPolling()
        lastImportResult = task.result
        updateState({
          status: 'failed',
          percent: 100,
          totalRows: task.result.totalRows,
          successCount: task.result.successCount,
          failCount: task.result.failCount
        })
        showNotification(task)
        if (onComplete) onComplete(task.result, 'failed')
        ElMessage.error('导入失败')
      }
    } catch (e) {
      console.error('[Polling] 轮询出错:', e)
    }
  }

  const stopPolling = () => {
    if (pollingTimer) {
      clearInterval(pollingTimer)
      pollingTimer = null
    }
  }

  // ==================== 公开方法 ====================

  const startImport = (taskId: string) => {
    // 清理之前的状态
    stopImport()
    resetState()

    currentTaskId = taskId
    pollingStartTime = Date.now()
    pollingCount = 0
    lastImportResult = null

    // 显示初始 Notification
    showNotification()

    // 尝试 SSE 订阅
    try {
      sseUnsubscribe = subscribeApi(taskId, {
        onStage: handleSSEStage,
        onProgress: handleSSEProgress,
        onComplete: handleSSEComplete,
        onError: handleSSEError
      })
      console.log('[SSE] 订阅成功，taskId:', taskId)
    } catch (e) {
      console.warn('[SSE] 订阅失败，降级到轮询:', e)
      startPolling()
    }
  }

  const stopImport = () => {
    // 清理 SSE
    if (sseUnsubscribe) {
      sseUnsubscribe()
      sseUnsubscribe = null
    }
    // 清理轮询
    stopPolling()
    // 清理任务 ID
    currentTaskId = null
  }

  return {
    state,
    startImport,
    stopImport,
    get lastResult() {
      return lastImportResult
    }
  }
}
