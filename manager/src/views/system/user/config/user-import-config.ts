/**
 * 用户导入配置文件
 */

import type { GenericImportConfig } from '@/composables/useGenericImport'
import type { TemplateColumn } from '@/utils/excel'
import { fetchImportUsers } from '@/api/system-manage'

/**
 * 用户导入模板列配置
 */
export const userTemplateColumns: TemplateColumn[] = [
  { title: '用户名', key: 'username', width: 20, required: true, example: 'zhangsan' },
  { title: '昵称', key: 'nickname', width: 20, required: true, example: '张三' },
  { title: '手机号', key: 'phone', width: 20, required: true, example: '13800138000' },
  { title: '邮箱', key: 'email', width: 25, required: false, example: 'zhangsan@example.com' },
  {
    title: '性别',
    key: 'gender',
    width: 10,
    required: false,
    example: '男',
    dropdown: { dictCode: 'sys_user_sex' }
  },
  { title: '角色', key: 'roles', width: 30, required: true, example: '普通用户' },
  {
    title: '状态',
    key: 'status',
    width: 10,
    required: false,
    example: '正常',
    dropdown: { dictCode: 'sys_user_status' }
  }
]

export const USER_TEMPLATE_FILENAME = '用户导入模板'
export const USER_TEMPLATE_SHEET_NAME = '用户数据'

/**
 * 用户导入配置
 */
export const userImportConfig: GenericImportConfig<Api.SystemManage.UserImportResult> = {
  // 模板配置
  templateColumns: userTemplateColumns,
  templateFilename: USER_TEMPLATE_FILENAME,
  templateSheetName: USER_TEMPLATE_SHEET_NAME,

  // 不需要上下文数据（无级联下拉）
  fetchContextData: async () => ({}),

  // 上传 API
  uploadApi: async (fileUrl: string, totalRows?: number) => {
    return await fetchImportUsers(fileUrl, totalRows)
  },

  // 进度监听（可选）
  subscribeApi: undefined,
  pollApi: undefined,

  // 分片上传配置
  enableChunkUpload: true,
  chunkSizeThreshold: 10 * 1024 * 1024, // 10MB
  skipScanThreshold: 50 * 1024 * 1024, // 50MB

  // 进度标题
  progressTitle: '用户导入'
}
