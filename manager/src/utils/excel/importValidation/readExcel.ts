/**
 * Excel 读取工具（导入校验专用）
 *
 * 注意：为了支持 10 万行大文件，避免 `sheet_to_json` 生成超大对象数组。
 *
 * @module utils/excel/importValidation/readExcel
 * @author 陈鸿昇
 */

import type * as XLSX from 'xlsx'

export interface ReadExcelOptions {
  /** 优先读取的工作表名称（不存在则降级到第一个 sheet） */
  sheetName?: string
  /** 期望表头标题集合（用于自动识别表头行） */
  expectedHeaders: string[]
  /** 允许扫描的最大表头行数（从第一行开始） */
  headerScanRows?: number
}

export interface ExcelSheetContext {
  workbook: XLSX.WorkBook
  worksheet: XLSX.WorkSheet
  sheetName: string
  range: XLSX.Range
  headerRow: number
  /** title -> 0-based column index */
  columnIndexByHeader: Map<string, number>
  /** 实际读取到的表头（normalized） */
  normalizedHeaders: string[]
}

export function normalizeHeaderText(input: unknown): string {
  const raw = String(input ?? '').trim()
  if (!raw) return ''

  // 兼容 *学号 / ＊学号
  return raw.replace(/^[*\uFF0A]\s*/, '').trim()
}

export function getSheetCellText(worksheet: XLSX.WorkSheet, a1: string): string {
  const cell = worksheet[a1] as XLSX.CellObject | undefined
  const v = cell?.w ?? cell?.v
  if (v === undefined || v === null) return ''
  return String(v).trim()
}

export function getColLetter(colIndex0: number): string {
  let n = colIndex0 + 1
  let s = ''
  while (n > 0) {
    const m = (n - 1) % 26
    s = String.fromCharCode(65 + m) + s
    n = Math.floor((n - 1) / 26)
  }
  return s
}

export async function readExcelFile(file: File): Promise<ArrayBuffer> {
  return await file.arrayBuffer()
}

export async function parseWorkbook(buffer: ArrayBuffer): Promise<XLSX.WorkBook> {
  const XLSX = await import('xlsx')
  return XLSX.read(buffer, { type: 'array' })
}

export function pickWorksheet(
  workbook: XLSX.WorkBook,
  preferredSheetName?: string
): { sheetName: string; worksheet: XLSX.WorkSheet } {
  if (preferredSheetName && workbook.Sheets[preferredSheetName]) {
    return { sheetName: preferredSheetName, worksheet: workbook.Sheets[preferredSheetName] }
  }
  const firstName = workbook.SheetNames[0]
  return { sheetName: firstName, worksheet: workbook.Sheets[firstName] }
}

export function detectHeaderRow(
  worksheet: XLSX.WorkSheet,
  range: XLSX.Range,
  expectedHeaders: string[],
  headerScanRows: number
): {
  headerRow: number
  normalizedHeaders: string[]
  columnIndexByHeader: Map<string, number>
} {
  const expected = new Set(expectedHeaders.map((h) => normalizeHeaderText(h)))
  const scanTo = Math.min(range.e.r + 1, headerScanRows)

  let bestRow = 1
  let bestScore = -1
  let bestHeaders: string[] = []
  let bestMap = new Map<string, number>()

  // 逐行扫描，选择“匹配 expectedHeaders 最多”的行作为表头
  for (let row = 1; row <= scanTo; row++) {
    const normalizedHeaders: string[] = []
    const map = new Map<string, number>()
    let score = 0

    for (let c = range.s.c; c <= range.e.c; c++) {
      const a1 = `${getColLetter(c)}${row}`
      const h = normalizeHeaderText(getSheetCellText(worksheet, a1))
      normalizedHeaders.push(h)
      if (h && expected.has(h) && !map.has(h)) {
        map.set(h, c)
        score++
      }
    }

    if (score > bestScore) {
      bestScore = score
      bestRow = row
      bestHeaders = normalizedHeaders
      bestMap = map
    }
  }

  return { headerRow: bestRow, normalizedHeaders: bestHeaders, columnIndexByHeader: bestMap }
}

export async function getExcelSheetContext(
  file: File,
  options: ReadExcelOptions
): Promise<ExcelSheetContext> {
  const buffer = await readExcelFile(file)
  const workbook = await parseWorkbook(buffer)
  const { sheetName, worksheet } = pickWorksheet(workbook, options.sheetName)

  const XLSX = await import('xlsx')
  const ref = worksheet['!ref']
  const range = ref ? XLSX.utils.decode_range(ref) : XLSX.utils.decode_range('A1:A1')

  const { headerRow, normalizedHeaders, columnIndexByHeader } = detectHeaderRow(
    worksheet,
    range,
    options.expectedHeaders,
    options.headerScanRows ?? 10
  )

  return {
    workbook,
    worksheet,
    sheetName,
    range,
    headerRow,
    normalizedHeaders,
    columnIndexByHeader
  }
}
