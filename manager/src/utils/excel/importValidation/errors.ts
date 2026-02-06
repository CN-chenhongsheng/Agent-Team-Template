/**
 * 导入校验错误类型
 *
 * @module utils/excel/importValidation/errors
 * @author 陈鸿昇
 */

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
      const valPart = it.value ? `，原值：<code>${val}</code>` : ''
      return `<li>第 <b>${row}</b> 行，<b>${col}</b>：${msg}${valPart}</li>`
    })
    .join('')

  const tail = truncated
    ? `<div class="mt-2 text-xs" style="color: var(--el-text-color-secondary);">错误过多，已提前终止扫描（当前错误数：≥${escapeHtml(
        String(errorCount)
      )}）。</div>`
    : ''

  const previewCount = escapeHtml(String(preview.length))

  return `
<div style="line-height: 1.6;">
  <div style="font-weight: 600; color: var(--el-text-color-primary); margin-bottom: 8px;">${title}</div>
  <div style="color: var(--el-text-color-secondary); margin-bottom: 8px;">
    共发现 <b>${escapeHtml(String(errorCount))}</b> 处问题，仅展示前 <b>${previewCount}</b> 条：
  </div>
  <ol style="padding-left: 18px; margin: 0; color: var(--el-text-color-regular);">
    ${items}
  </ol>
  ${tail}
</div>
  `.trim()
}
