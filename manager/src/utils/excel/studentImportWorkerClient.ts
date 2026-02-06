/**
 * 学生导入 Web Worker 扫描客户端
 *
 * 优化版：
 * - 主线程只负责加载字典和树数据（需要 Pinia 和 HTTP 请求）
 * - Worker 负责完整的 Excel 读取 + 行构造 + 校验
 * - 大文件扫描时主线程不阻塞，用户体验更好
 *
 * @module utils/excel/studentImportWorkerClient
 * @author 陈鸿昇
 */

import type { TemplateColumn } from './templateGenerator'
import {
  prepareValidationContextOnly,
  prepareStudentImportValidation,
  validateStudentRows,
  type StudentImportValidationResult,
  type ValidateStudentImportOptions
} from './importValidation/studentImportValidator'

export interface WorkerScanOptions extends ValidateStudentImportOptions {
  /**
   * Worker 内部进度步长：每处理多少行上报一次进度
   * 默认 200
   */
  progressStep?: number
  /**
   * 主线程进度回调
   * - stage: 当前阶段 ('loading' | 'reading' | 'parsing' | 'preparing' | 'validating')
   * - scannedRows: 已处理的行数
   * - totalRows: 总行数
   * - percent: 百分比 (0-100)
   */
  onProgress?: (payload: {
    stage?: string
    scannedRows: number
    totalRows: number
    percent: number
  }) => void
}

/**
 * 让出主线程，确保 UI 有机会渲染
 * 使用 requestAnimationFrame + setTimeout 双重保证
 */
function yieldToMain(): Promise<void> {
  return new Promise((resolve) => {
    // requestAnimationFrame 确保在下一帧渲染前执行
    // setTimeout 0 确保让出事件循环
    requestAnimationFrame(() => {
      setTimeout(resolve, 0)
    })
  })
}

/**
 * 使用 Web Worker 扫描学生导入 Excel（优化版）
 *
 * 新模式（推荐）：
 * - 主线程：只预加载字典/树数据
 * - Worker：完整执行 Excel 读取 + 行构造 + 校验
 *
 * 回退模式（不支持 Worker）：
 * - 主线程完成全部工作，但会在关键点让出主线程确保 UI 更新
 */
export async function scanStudentFileWithWorker(
  file: File,
  columns: TemplateColumn[],
  options: WorkerScanOptions = {}
): Promise<StudentImportValidationResult> {
  const { onProgress } = options

  // 阶段 0：预加载字典/树数据（主线程，需要 Pinia 和 API）
  onProgress?.({ stage: 'loading', scannedRows: 0, totalRows: 100, percent: 0 })

  // 让出主线程，确保进度条 UI 渲染
  await yieldToMain()

  const context = await prepareValidationContextOnly(columns)

  onProgress?.({ stage: 'loading', scannedRows: 0, totalRows: 100, percent: 5 })

  // 让出主线程，确保进度更新渲染
  await yieldToMain()

  // 检测 Worker 支持
  const WorkerConstructor: typeof Worker | undefined =
    typeof Worker !== 'undefined' ? Worker : undefined

  // 不支持 Worker 时，回退到主线程版本（开发环境也尝试使用 Worker）
  if (!WorkerConstructor) {
    // 回退模式：主线程读取 Excel + 校验
    onProgress?.({ stage: 'reading', scannedRows: 0, totalRows: 100, percent: 10 })
    await yieldToMain()

    const { rows } = await prepareStudentImportValidation(file, columns, options)

    onProgress?.({ stage: 'validating', scannedRows: 0, totalRows: rows.length, percent: 50 })
    await yieldToMain()

    const result = validateStudentRows({
      rows,
      context,
      options,
      onProgress: ({ scannedRows, totalRows }) => {
        const percent = 50 + Math.floor((scannedRows / totalRows) * 50)
        onProgress?.({ stage: 'validating', scannedRows, totalRows, percent })
      },
      progressStep: options.progressStep ?? 200
    })

    onProgress?.({
      stage: 'validating',
      scannedRows: rows.length,
      totalRows: rows.length,
      percent: 100
    })

    return result
  }

  // 新模式：Worker 内完成 Excel 读取 + 校验
  const worker = new WorkerConstructor(
    new URL('../../workers/student-import.worker.ts', import.meta.url),
    { type: 'module' }
  )

  return await new Promise<StudentImportValidationResult>((resolve, reject) => {
    const handleMessage = (event: MessageEvent) => {
      const data = event.data as
        | { type: 'progress'; stage: string; scannedRows: number; totalRows: number; percent: number }
        | { type: 'done'; result: StudentImportValidationResult }
        | { type: 'error'; message: string; stack?: string; meta?: any }

      if (!data || typeof data !== 'object' || typeof (data as any).type !== 'string') {
        return
      }

      if (data.type === 'progress') {
        // Worker 进度从 5% 开始（前 5% 是预加载阶段）
        const adjustedPercent = 5 + Math.floor(data.percent * 0.95)
        onProgress?.({
          stage: data.stage,
          scannedRows: data.scannedRows,
          totalRows: data.totalRows,
          percent: Math.min(99, adjustedPercent)
        })
        return
      }

      if (data.type === 'done') {
        cleanup()
        onProgress?.({
          stage: 'done',
          scannedRows: data.result.totalRowsForImport,
          totalRows: data.result.totalRowsForImport,
          percent: 100
        })
        resolve(data.result)
        return
      }

      if (data.type === 'error') {
        cleanup()
        // 如果有 meta，构造带结构化错误信息的 Error
        const error = new Error(data.message) as any
        if (data.stack) {
          error.stack = data.stack
        }
        if (data.meta) {
          error.meta = data.meta
        }
        reject(error)
      }
    }

    const handleError = (event: ErrorEvent) => {
      cleanup()
      reject(new Error(event.message || '学生导入扫描 Worker 执行失败'))
    }

    const cleanup = () => {
      worker.removeEventListener('message', handleMessage)
      worker.removeEventListener('error', handleError)
      worker.terminate()
    }

    worker.addEventListener('message', handleMessage)
    worker.addEventListener('error', handleError)

    // 发送 scan-file 消息，让 Worker 完成 Excel 读取和校验
    const { onProgress: _onProgress, progressStep, ...plainOptions } = options

    worker.postMessage({
      type: 'scan-file',
      payload: {
        file, // File 对象可以通过结构化克隆传递
        context,
        options: plainOptions as ValidateStudentImportOptions,
        progressStep: progressStep ?? 200
      }
    })
  })
}
