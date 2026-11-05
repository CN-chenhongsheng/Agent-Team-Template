/**
 * 用户导入配置文件
 */

import type { GenericImportConfig } from '@/composables/useGenericImport'
import type { TemplateColumn } from '@/utils/excel'
import { fetchImportUsers } from '@/api/system-manage'
import i18n from '@/locales'

const t = (key: string): string => (i18n.global.t as (key: string) => string)(key)

const buildTemplateColumns = (): TemplateColumn[] => [
  {
    title: t('system.user.importConfig.columns.username'),
    key: 'username',
    width: 20,
    required: true,
    example: 'zhangsan'
  },
  {
    title: t('system.user.importConfig.columns.nickname'),
    key: 'nickname',
    width: 20,
    required: true,
    example: t('system.user.importConfig.examples.nickname')
  },
  {
    title: t('system.user.importConfig.columns.phone'),
    key: 'phone',
    width: 20,
    required: true,
    example: '13800138000'
  },
  {
    title: t('system.user.importConfig.columns.email'),
    key: 'email',
    width: 25,
    required: false,
    example: 'zhangsan@example.com'
  },
  {
    title: t('system.user.importConfig.columns.gender'),
    key: 'gender',
    width: 10,
    required: false,
    example: t('system.user.importConfig.examples.gender'),
    dropdown: { dictCode: 'sys_user_sex' }
  },
  {
    title: t('system.user.importConfig.columns.role'),
    key: 'roles',
    width: 30,
    required: true,
    example: t('system.user.importConfig.examples.role')
  },
  {
    title: t('system.user.importConfig.columns.status'),
    key: 'status',
    width: 10,
    required: false,
    example: t('system.user.importConfig.examples.status'),
    dropdown: { dictCode: 'sys_user_status' }
  }
]

/**
 * 用户导入配置
 */
export const userImportConfig: GenericImportConfig<Api.SystemManage.UserImportResult> = {
  // 模板配置
  get templateColumns() {
    return buildTemplateColumns()
  },
  get templateFilename() {
    return t('system.user.importConfig.filename')
  },
  get templateSheetName() {
    return t('system.user.importConfig.sheetName')
  },

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
  get progressTitle() {
    return t('system.user.importConfig.progressTitle')
  }
}
