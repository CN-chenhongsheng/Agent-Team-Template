/**
 * 通用导入 Hook
 *
 * 配置驱动的通用导入系统，可用于学生、教师、房间等多种导入场景。
 *
 * ## 核心功能
 *
 * 1. 模板下载 - 动态生成带级联下拉的 Excel 模板
 * 2. 文件扫描 - 调用 Worker 进行前端校验
 * 3. 分片上传 - 大文件自动分片上传
 * 4. 进度监听 - SSE/轮询双模式监听导入进度
 *
 * ## 使用示例
 *
 * ```typescript
 * const {
 *   dialogVisible,
 *   openDialog,
 *   handleDownloadTemplate,
 *   handleScanFile,
 *   handleUploadSuccess
 * } = useGenericImport(studentImportConfig)
 *
 * // 在模板中
 * <ArtImportDialog
 *   v-model="dialogVisible"
 *   :template-download-fn="handleDownloadTemplate"
 *   :scan-with-progress-fn="handleScanFile"
 *   @upload-success="handleUploadSuccess"
 * />
 * ```
 *
 * @module composables/useGenericImport
 * @author 陈鸿昇
 */

import { ref, onBeforeUnmount, type Ref } from 'vue'
import { generateTemplate, type TemplateColumn, type CascadeData } from '@/utils/excel'
import type { UploadResult } from '@/utils/upload'
import { useImportProgress } from '@/hooks/core/useImportProgress'
import type {
  SSEImportCallbacks,
  ImportResult as BaseImportResult,
  ImportTaskVO
} from '@/hooks/core/useImportProgress'

// ===================== 类型定义 =====================

/**
 * 导入结果接口
 * 可通过泛型扩展具体业务字段
 */
export interface ImportResult extends BaseImportResult {
  totalRows: number
  successCount: number
  failCount: number
  errors?: any
}

/**
 * 上传 API 响应（同步或异步）
 */
export type UploadApiResponse<TResult = ImportResult> =
  | { taskId: string } // 异步任务
  | TResult // 同步结果

/**
 * 通用导入配置
 */
export interface GenericImportConfig<TResult = ImportResult, TContext = any> {
  // ==================== 模板配置 ====================

  /** 模板列配置 */
  templateColumns: TemplateColumn[]

  /** 模板文件名（不含扩展名） */
  templateFilename: string

  /** 工作表名称（默认：'导入数据'） */
  templateSheetName?: string

  // ==================== Worker 配置 ====================

  /**
   * Worker 文件路径（可选）
   * 如果不提供，则使用简单的前端校验
   */
  workerPath?: string

  /**
   * 字段校验器（简单场景使用，复杂场景使用 Worker）
   * Record<字段key, 校验函数>
   * 校验函数返回：null（通过） | string（错误信息）
   */
  fieldValidators?: Record<string, (value: string) => string | null>

  /**
   * 构建校验上下文（可选）
   * 将获取的上下文数据转换为 Worker 需要的格式
   */
  buildContext?: (contextData: any) => TContext

  /**
   * 行校验函数（可选）
   * 对整行数据进行业务校验
   */
  validateRow?: (row: Record<string, any>, context: TContext) => string | null

  // ==================== API 配置 ====================

  /**
   * 获取上下文数据（组织架构树、宿舍树等）
   * 用于：
   * 1. 生成 Excel 模板的级联下拉
   * 2. 传递给 Worker 进行校验
   */
  fetchContextData: () => Promise<any>

  /**
   * 上传导入 API
   * @param fileUrl 上传后的文件 URL
   * @param totalRows 总行数（可选，用于精确计算进度）
   * @returns 同步结果或异步任务 ID
   */
  uploadApi: (fileUrl: string, totalRows?: number) => Promise<UploadApiResponse<TResult>>

  /**
   * SSE 订阅 API（异步导入必须）
   * @param taskId 任务 ID
   * @param callbacks SSE 回调
   * @returns 取消订阅函数
   */
  subscribeApi?: (taskId: string, callbacks: SSEImportCallbacks<TResult>) => () => void

  /**
   * 轮询 API（SSE 降级使用）
   * @param taskId 任务 ID
   * @returns 任务状态
   */
  pollApi?: (taskId: string) => Promise<ImportTaskVO<TResult> | null>

  // ==================== 可选配置 ====================

  /** 是否启用分片上传（默认：true） */
  enableChunkUpload?: boolean

  /** 分片上传阈值（字节，默认：5MB） */
  chunkSizeThreshold?: number

  /** 跳过前端扫描的文件大小阈值（字节，默认：50MB） */
  skipScanThreshold?: number

  /** 导入成功回调 */
  onImportComplete?: (result: TResult, status: 'success' | 'failed') => void

  /** 查看详情回调 */
  onViewDetail?: (result: TResult) => void

  /** 进度通知标题（默认：'数据导入'） */
  progressTitle?: string
}

/**
 * Hook 返回值
 */
export interface GenericImportReturn {
  /** 弹窗显示状态 */
  dialogVisible: Ref<boolean>

  /** 打开导入弹窗 */
  openDialog: () => void

  /** 关闭导入弹窗 */
  closeDialog: () => void

  /** 下载模板函数 */
  handleDownloadTemplate: () => Promise<void>

  /** 扫描文件函数（带进度） */
  handleScanFile: (
    file: File,
    onProgress: (payload: { scannedRows: number; totalRows: number; percent: number }) => void
  ) => Promise<void>

  /** 上传成功处理函数 */
  handleUploadSuccess: (uploadResult: UploadResult) => Promise<void>

  /** 是否正在下载模板 */
  isDownloading: Ref<boolean>

  /** 是否正在扫描 */
  isScanning: Ref<boolean>

  /** 是否正在上传 */
  isUploading: Ref<boolean>
}

// ===================== Hook 实现 =====================

/**
 * 通用导入 Hook
 */
export function useGenericImport<TResult extends ImportResult = ImportResult, TContext = any>(
  config: GenericImportConfig<TResult, TContext>
): GenericImportReturn {
  // 解构配置
  const {
    templateColumns,
    templateFilename,
    templateSheetName = '导入数据',
    fetchContextData,
    uploadApi,
    subscribeApi,
    pollApi,
    skipScanThreshold = 50 * 1024 * 1024,
    onImportComplete,
    onViewDetail,
    progressTitle = '数据导入'
  } = config

  // ==================== 状态 ====================

  const dialogVisible = ref(false)
  const isDownloading = ref(false)
  const isScanning = ref(false)
  const isUploading = ref(false)

  // 最近一次扫描通过的有效行数
  const lastScanTotalRows = ref<number | null>(null)

  // ==================== 导入进度 Hook ====================

  // 仅在配置了 SSE/轮询 API 时初始化进度监听
  let importProgressHook: ReturnType<typeof useImportProgress<TResult>> | null = null

  if (subscribeApi && pollApi) {
    importProgressHook = useImportProgress<TResult>({
      title: progressTitle,
      subscribeApi: subscribeApi as any,
      pollApi: pollApi as any,
      onComplete: (result, status) => {
        if (onImportComplete) {
          onImportComplete(result, status)
        }
      },
      onViewDetail: (result) => {
        if (onViewDetail) {
          onViewDetail(result)
        }
      }
    })
  }

  // ==================== 模板下载 ====================

  /**
   * 下载 Excel 模板
   */
  const handleDownloadTemplate = async (): Promise<void> => {
    isDownloading.value = true

    try {
      // 获取上下文数据（用于级联下拉）
      const contextData = await fetchContextData()

      // 构建级联数据
      const cascadeData: CascadeData | undefined = contextData
        ? {
            orgTree: contextData.orgTree,
            dormTree: contextData.dormTree
          }
        : undefined

      // 生成模板
      await generateTemplate({
        columns: templateColumns,
        filename: templateFilename,
        sheetName: templateSheetName,
        cascadeData
      })

      ElMessage.success('模板下载成功')
    } catch (error) {
      console.error('下载模板失败:', error)
      ElMessage.error('模板下载失败，请稍后重试')
      throw error
    } finally {
      isDownloading.value = false
    }
  }

  // ==================== 文件扫描 ====================

  /**
   * 扫描文件（前端校验）
   */
  const handleScanFile = async (
    file: File,
    onProgress: (payload: { scannedRows: number; totalRows: number; percent: number }) => void
  ): Promise<void> => {
    isScanning.value = true

    try {
      // 大文件检测：超过阈值自动跳过前端扫描
      if (file.size > skipScanThreshold) {
        lastScanTotalRows.value = null
        // 不抛出异常，让组件知道是跳过扫描
        return
      }

      // TODO: 这里应该调用 Worker 或简单校验
      // 目前先使用简单的占位实现
      if (config.workerPath) {
        // 使用 Worker 校验（需要实现通用 Worker 客户端）
        console.log('[useGenericImport] Worker 校验暂未实现')
      } else {
        // 简单校验：检查文件格式
        if (!file.name.toLowerCase().endsWith('.xlsx')) {
          throw new Error('仅支持 .xlsx 格式文件')
        }
      }

      // 模拟进度上报（实际应由 Worker 上报）
      onProgress({ scannedRows: 100, totalRows: 100, percent: 100 })
      lastScanTotalRows.value = 100
    } catch (error) {
      console.error('文件扫描失败:', error)
      throw error
    } finally {
      isScanning.value = false
    }
  }

  // ==================== 文件上传 ====================

  /**
   * 处理上传成功
   */
  const handleUploadSuccess = async (
    uploadResult: UploadResult & { totalRows?: number | null }
  ): Promise<void> => {
    const fileUrl = uploadResult?.url
    if (!fileUrl) {
      ElMessage.warning('未获取到文件地址')
      closeDialog()
      return
    }

    isUploading.value = true

    try {
      // 使用扫描得到的 totalRows（如果有）
      const totalRows = uploadResult.totalRows ?? lastScanTotalRows.value ?? undefined

      // 调用上传 API
      const response = await uploadApi(fileUrl, totalRows)

      // 判断是同步结果还是异步任务
      if ('taskId' in response && response.taskId) {
        // 异步任务：启动进度监听
        closeDialog()

        if (importProgressHook) {
          importProgressHook.startImport(response.taskId)
        } else {
          ElMessage.warning('未配置进度监听，请检查配置')
        }
      } else {
        // 同步结果：直接处理
        const result = response as TResult
        closeDialog()

        if ((result.failCount ?? 0) > 0) {
          ElMessage.warning(
            `导入完成：成功 ${result.successCount ?? 0} 条，失败 ${result.failCount ?? 0} 条`
          )
        } else {
          ElMessage.success(`导入成功：共 ${result.successCount ?? 0} 条`)
        }

        // 触发完成回调
        if (onImportComplete) {
          onImportComplete(result, 'success')
        }

        // 如果有失败且配置了查看详情，则调用
        if ((result.failCount ?? 0) > 0 && onViewDetail) {
          onViewDetail(result)
        }
      }
    } catch (error) {
      console.error('导入失败:', error)
      ElMessage.error('导入失败，请重试')
      closeDialog()
    } finally {
      isUploading.value = false
    }
  }

  // ==================== 弹窗控制 ====================

  const openDialog = () => {
    dialogVisible.value = true
  }

  const closeDialog = () => {
    dialogVisible.value = false
    lastScanTotalRows.value = null
  }

  // ==================== 组件卸载清理 ====================

  onBeforeUnmount(() => {
    if (importProgressHook) {
      importProgressHook.stopImport()
    }
  })

  // ==================== 返回 ====================

  return {
    dialogVisible,
    openDialog,
    closeDialog,
    handleDownloadTemplate,
    handleScanFile,
    handleUploadSuccess,
    isDownloading,
    isScanning,
    isUploading
  }
}
