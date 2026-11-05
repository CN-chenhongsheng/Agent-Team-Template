/**
 * 导入校验错误类型
 *
 * @module utils/excel/importValidation/errors
 * @author 陈鸿昇
 */

import i18n from '@/locales'

const t = (key: string, params?: Record<string, unknown>): string =>
  (i18n.global.t as any)(key, params)

export interface ImportValidationErrorItem {
  /** Excel 行号（从 1 开始） */
  row: number
  /** 列名（Excel 表头标题，去掉 *） */
  column: string
  /** 错误原因 */
  message: string
  /** 原始值（用于展示） */
  value?: string
}

export interface ImportValidationErrorMeta {
  /** 总错误数（可能为 >=，见 truncated） */
  errorCount: number
  /** 是否因错误过多提前终止扫描 */
  truncated: boolean
  /** 仅展示的前 N 条 */
  preview: ImportValidationErrorItem[]
}

export class ImportValidationError extends Error {
  public readonly meta: ImportValidationErrorMeta

  constructor(summary: string, meta: ImportValidationErrorMeta) {
    super(summary)
    this.name = 'ImportValidationError'
    this.meta = meta
  }
}

function escapeHtml(input: string): string {
  return input
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

/**
 * 将错误信息格式化为可安全展示的 HTML（用于 MessageBox 的 dangerouslyUseHTMLString）
 */
export function formatImportValidationErrorHtml(error: ImportValidationError): string {
  const { errorCount, truncated, preview } = error.meta
  const title = escapeHtml(error.message)

  const items = preview
    .map((it) => {
      const row = escapeHtml(String(it.row))
      const col = escapeHtml(it.column)
      const msg = escapeHtml(it.message)
      const val = it.value ? escapeHtml(it.value) : ''
      const valPart = it.value ? t('utils.excel.errors.valuePart', { value: val }) : ''
      return `<li>${t('utils.excel.errors.rowLine', {
        row,
        column: col,
        message: msg,
        valuePart: valPart
      })}</li>`
    })
    .join('')

  const tail = truncated
    ? `<div class="mt-2 text-xs" style="color: var(--el-text-color-secondary);">${t(
        'utils.excel.errors.truncated',
        { count: escapeHtml(String(errorCount)) }
      )}</div>`
    : ''

  const previewCount = escapeHtml(String(preview.length))

  return `
<div style="line-height: 1.6;">
  <div style="font-weight: 600; color: var(--el-text-color-primary); margin-bottom: 8px;">${title}</div>
  <div style="color: var(--el-text-color-secondary); margin-bottom: 8px;">
    ${t('utils.excel.errors.summary', {
      count: escapeHtml(String(errorCount)),
      preview: previewCount
    })}
  </div>
  <ol style="padding-left: 18px; margin: 0; color: var(--el-text-color-regular);">
    ${items}
  </ol>
  ${tail}
</div>
  `.trim()
}
