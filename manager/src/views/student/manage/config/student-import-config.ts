/**
 * 学生导入配置文件
 *
 * 将学生导入的所有配置抽取到此文件，实现配置驱动的导入功能。
 *
 * ## 使用方式
 *
 * ```typescript
 * import { useGenericImport } from '@/composables/useGenericImport'
 * import { studentImportConfig } from './config/student-import-config'
 *
 * const {
 *   dialogVisible,
 *   handleDownloadTemplate,
 *   handleScanFile,
 *   handleUploadSuccess
 * } = useGenericImport(studentImportConfig)
 * ```
 *
 * @module views/student/manage/config/student-import-config
 * @author 陈鸿昇
 */

import type { GenericImportConfig } from '@/composables/useGenericImport'
import { fetchImportTrees } from '@/api/student-import'
import {
  fetchImportStudents,
  fetchImportTaskResult,
  subscribeImportProgress
} from '@/api/student-manage'
import {
  studentTemplateColumns,
  STUDENT_TEMPLATE_FILENAME,
  STUDENT_TEMPLATE_SHEET_NAME
} from '../utils/studentTemplateFields'

/**
 * 学生导入结果类型
 */
export interface StudentImportResult {
  totalRows: number
  successCount: number
  failCount: number
  errors?: Api.StudentImport.ImportError[]
}

/**
 * 学生导入配置
 *
 * 完整配置学生导入的所有环节：
 * - 模板生成（45个字段，带级联下拉）
 * - 前端校验（Worker 严格校验）
 * - 分片上传（大文件自动分片）
 * - 进度监听（SSE + 轮询降级）
 */
export const studentImportConfig: GenericImportConfig<StudentImportResult> = {
  // ==================== 模板配置 ====================

  templateColumns: studentTemplateColumns,
  templateFilename: STUDENT_TEMPLATE_FILENAME,
  templateSheetName: STUDENT_TEMPLATE_SHEET_NAME,

  // ==================== Worker 配置 ====================

  /**
   * 使用专门的学生导入 Worker
   * Worker 内完成：
   * 1. Excel 解析（xlsx 库）
   * 2. 字段映射与清洗
   * 3. 必填校验
   * 4. 格式校验（手机号、身份证、邮箱等）
   * 5. 级联字段校验（校区→院系→专业→班级）
   * 6. 进度上报
   */
  workerPath: '/src/workers/student-import.worker.ts',

  // ==================== API 配置 ====================

  /**
   * 获取上下文数据
   * - orgTree: 组织架构树（校区→院系→专业→班级）
   * - dormTree: 宿舍树（校区→楼层→房间→床位）
   *
   * 用于：
   * 1. 生成 Excel 模板的级联下拉
   * 2. 传递给 Worker 进行级联字段校验
   */
  fetchContextData: fetchImportTrees,

  /**
   * 上传导入 API
   * @param fileUrl 上传后的文件 URL
   * @param totalRows 总行数（用于精确计算进度）
   * @returns 异步任务 ID 或同步结果
   */
  uploadApi: (fileUrl: string, totalRows?: number) => {
    return fetchImportStudents(fileUrl, totalRows)
  },

  /**
   * SSE 订阅 API
   * 实时接收后端推送的导入进度
   */
  subscribeApi: subscribeImportProgress as any,

  /**
   * 轮询 API（SSE 降级）
   * 当 SSE 连接失败时，自动降级到轮询模式
   */
  pollApi: fetchImportTaskResult as any,

  // ==================== 可选配置 ====================

  /**
   * 启用分片上传
   * 超过 5MB 的文件会自动使用分片上传
   */
  enableChunkUpload: true,

  /**
   * 分片上传阈值：5MB
   */
  chunkSizeThreshold: 5 * 1024 * 1024,

  /**
   * 跳过前端扫描的文件大小阈值：50MB
   * 超过此大小的文件会跳过前端 Worker 校验，直接由服务器处理
   */
  skipScanThreshold: 50 * 1024 * 1024,

  /**
   * 进度通知标题
   */
  progressTitle: '学生导入'
}
