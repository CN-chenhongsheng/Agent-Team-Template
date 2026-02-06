/// <reference lib="webworker" />

/**
 * 学生导入扫描 Web Worker
 *
 * 支持两种模式：
 * 1. scan-file: 接收 File 对象，Worker 内完成 Excel 读取 + 行构造 + 校验（推荐，大文件更快）
 * 2. scan: 接收预处理好的行数据与上下文（兼容旧用法）
 *
 * 通过 postMessage 上报分阶段进度与最终结果
 *
 * @module workers/student-import.worker
 * @author 陈鸿昇
 */

import type * as XLSX from 'xlsx'
import type { TemplateColumn } from '../utils/excel/templateGenerator'
import type { ImportValidationErrorItem } from '../utils/excel/importValidation/errors'
import type {
  OrgTreeResponse,
  DormTreeResponse
} from '../api/student-import'

// ============================================================================
// 类型定义
// ============================================================================

/** 校验上下文（不含任何函数，可结构化克隆） */
interface StudentImportValidationContext {
  columns: TemplateColumn[]
  dictSets: Record<string, string[]>
  orgTree: OrgTreeResponse
  dormTree: DormTreeResponse
}

/** 校验配置 */
interface ValidateStudentImportOptions {
  sheetName?: string
  previewLimit?: number
  maxScanErrors?: number
  maxTrailingEmptyRows?: number
}

/** 行数据结构 */
interface StudentImportRowForValidation {
  row: number
  values: Record<string, string>
}

/** 校验结果 */
interface StudentImportValidationResult {
  ok: boolean
  errorCount: number
  truncated: boolean
  preview: ImportValidationErrorItem[]
  totalRowsForImport: number
}

// ============================================================================
// Worker 消息类型定义
// ============================================================================

/** 新模式：接收 File，Worker 内完成全部处理 */
interface ScanFilePayload {
  file: File
  context: StudentImportValidationContext
  options?: ValidateStudentImportOptions
  progressStep?: number
}

/** 兼容旧模式：接收预处理好的行数据 */
interface ScanRowsPayload {
  rows: StudentImportRowForValidation[]
  context: StudentImportValidationContext
  options?: ValidateStudentImportOptions
  progressStep?: number
}

type WorkerRequest =
  | { type: 'scan-file'; payload: ScanFilePayload }
  | { type: 'scan'; payload: ScanRowsPayload }

type WorkerResponse =
  | { type: 'progress'; stage: string; scannedRows: number; totalRows: number; percent: number }
  | { type: 'done'; result: StudentImportValidationResult }
  | { type: 'error'; message: string; stack?: string; meta?: any }

// ============================================================================
// 工具函数（从 readExcel.ts / studentImportValidator.ts 复制的纯函数）
// ============================================================================

function normalizeHeaderText(input: unknown): string {
  const raw = String(input ?? '').trim()
  if (!raw) return ''
  return raw.replace(/^[*\uFF0A]\s*/, '').trim()
}

function getColLetter(colIndex0: number): string {
  let n = colIndex0 + 1
  let s = ''
  while (n > 0) {
    const m = (n - 1) % 26
    s = String.fromCharCode(65 + m) + s
    n = Math.floor((n - 1) / 26)
  }
  return s
}

function getSheetCellText(worksheet: XLSX.WorkSheet, a1: string): string {
  const cell = worksheet[a1] as XLSX.CellObject | undefined
  const v = cell?.w ?? cell?.v
  if (v === undefined || v === null) return ''
  return String(v).trim()
}

function isEmpty(value: string): boolean {
  return !value || value.trim() === ''
}

function toExcelA1(colIndex0: number, row1: number): string {
  return `${getColLetter(colIndex0)}${row1}`
}

function sanitizeName(name: string, prefix?: string): string {
  if (!name) return prefix ? prefix + '_空' : '空'
  let sanitized = name
    .replace(/[\s/\\:;,.!@#$%^&*()+=\u005B\u005D{}|<>?'"-]/g, '_')
    .replace(/_+/g, '_')
    .replace(/^_+|_+$/g, '')
  if (!/^[a-zA-Z_\u4e00-\u9fa5]/.test(sanitized)) {
    sanitized = (prefix || 'N') + sanitized
  }
  if (sanitized.length > 200) {
    sanitized = sanitized.substring(0, 200)
  }
  return sanitized || (prefix ? prefix + '_空' : '空')
}

function buildHierarchyName(parentName: string, currentName: string, prefix: string): string {
  const sanitizedCurrent = sanitizeName(currentName, prefix)
  return parentName + '_' + sanitizedCurrent
}

function isLikelyExampleRow(
  columns: TemplateColumn[],
  getValue: (title: string) => string
): boolean {
  const cols = columns.filter((c) => c.example && c.example.trim() !== '')
  if (cols.length === 0) return false
  let hit = 0
  let total = 0
  for (const col of cols) {
    total++
    const v = getValue(col.title)
    if (!isEmpty(v) && v === col.example) hit++
  }
  return total >= 3 && hit >= Math.max(3, Math.ceil(total * 0.6))
}

// ============================================================================
// 级联校验集合构建
// ============================================================================

function buildOrgCascadeSets(orgTree: OrgTreeResponse) {
  const campusSet = new Set<string>()
  const campusToDept = new Map<string, Set<string>>()
  const deptToMajor = new Map<string, Set<string>>()
  const majorToClass = new Map<string, Set<string>>()

  const campuses = orgTree?.campuses || []
  for (const campus of campuses) {
    if (campus.status !== 1) continue
    const campusName = sanitizeName(campus.name)
    campusSet.add(campusName)

    const departments = campus.children || []
    for (const dept of departments) {
      if (dept.status !== 1) continue
      const deptName = buildHierarchyName(campusName, dept.name, 'D')
      if (!campusToDept.has(campusName)) campusToDept.set(campusName, new Set())
      campusToDept.get(campusName)!.add(deptName)

      const majors = dept.children || []
      for (const major of majors) {
        if (major.status !== 1) continue
        const majorName = buildHierarchyName(deptName, major.name, 'M')
        if (!deptToMajor.has(deptName)) deptToMajor.set(deptName, new Set())
        deptToMajor.get(deptName)!.add(majorName)

        const classes = major.children || []
        for (const cls of classes) {
          if (cls.status !== 1) continue
          const className = buildHierarchyName(majorName, cls.name, 'C')
          if (!majorToClass.has(majorName)) majorToClass.set(majorName, new Set())
          majorToClass.get(majorName)!.add(className)
        }
      }
    }
  }
  return { campusSet, campusToDept, deptToMajor, majorToClass }
}

function buildDormCascadeSets(dormTree: DormTreeResponse) {
  const campusToFloor = new Map<string, Set<string>>()
  const floorToRoom = new Map<string, Set<string>>()
  const roomToBed = new Map<string, Set<string>>()

  const campuses = dormTree?.campuses || []
  for (const campus of campuses) {
    if (campus.status !== 1) continue
    const campusName = sanitizeName(campus.name)

    const floors = campus.children || []
    for (const floor of floors) {
      if (floor.status !== 1) continue
      const floorName = buildHierarchyName(campusName, floor.name, 'F')
      if (!campusToFloor.has(campusName)) campusToFloor.set(campusName, new Set())
      campusToFloor.get(campusName)!.add(floorName)

      const rooms = floor.children || []
      for (const room of rooms) {
        if (room.status !== 1) continue
        const roomName = buildHierarchyName(floorName, room.name, 'R')
        if (!floorToRoom.has(floorName)) floorToRoom.set(floorName, new Set())
        floorToRoom.get(floorName)!.add(roomName)

        const beds = room.children || []
        for (const bed of beds) {
          if (bed.status !== 1) continue
          const bedName = buildHierarchyName(roomName, bed.name, 'B')
          if (!roomToBed.has(roomName)) roomToBed.set(roomName, new Set())
          roomToBed.get(roomName)!.add(bedName)
        }
      }
    }
  }
  return { campusToFloor, floorToRoom, roomToBed }
}

// ============================================================================
// 格式校验器
// ============================================================================

function buildBasicFormatValidators() {
  const phoneRe = /^1\d{10}$/
  const emailRe = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  const dateRe = /^\d{4}-\d{2}-\d{2}$/
  const yearRe = /^\d{4}$/

  // 身份证校验（包含校验码）
  const validateIdCard = (v: string): boolean => {
    if (!/^\d{17}[\dXx]$/.test(v)) return false
    const weights = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2]
    const checkCodes = '10X98765432'
    let sum = 0
    for (let i = 0; i < 17; i++) {
      sum += parseInt(v[i], 10) * weights[i]
    }
    return v[17].toUpperCase() === checkCodes[sum % 11]
  }

  return {
    phone: (v: string) => (isEmpty(v) || phoneRe.test(v) ? '' : '手机号格式不正确（需 11 位数字）'),
    idCard: (v: string) =>
      isEmpty(v) || validateIdCard(v) ? '' : '身份证号格式不正确（需 18 位且通过校验码验证）',
    email: (v: string) => (isEmpty(v) || emailRe.test(v) ? '' : '邮箱格式不正确'),
    birthDate: (v: string) =>
      isEmpty(v) || dateRe.test(v) ? '' : '出生日期格式不正确（YYYY-MM-DD）',
    enrollmentYear: (v: string) =>
      isEmpty(v) || yearRe.test(v) ? '' : '入学年份格式不正确（YYYY）',
    parentPhone: (v: string) =>
      isEmpty(v) || phoneRe.test(v) ? '' : '家长电话格式不正确（需 11 位数字）',
    emergencyPhone: (v: string) =>
      isEmpty(v) || phoneRe.test(v) ? '' : '紧急联系电话格式不正确（需 11 位数字）'
  }
}

function inferSheetKeyValidators(columns: TemplateColumn[]) {
  const v = buildBasicFormatValidators()
  const validatorsByKey: Record<string, (value: string) => string> = {
    phone: v.phone,
    idCard: v.idCard,
    email: v.email,
    birthDate: v.birthDate,
    enrollmentYear: v.enrollmentYear,
    parentPhone: v.parentPhone,
    emergencyPhone: v.emergencyPhone
  }
  const result = new Map<string, (value: string) => string>()
  for (const col of columns) {
    const fn = validatorsByKey[col.key]
    if (fn) result.set(col.key, fn)
  }
  return result
}

// ============================================================================
// Worker 主逻辑
// ============================================================================

const ctx: DedicatedWorkerGlobalScope = self as unknown as DedicatedWorkerGlobalScope

/**
 * 发送进度消息
 */
function postProgress(stage: string, scannedRows: number, totalRows: number, percent: number) {
  const msg: WorkerResponse = { type: 'progress', stage, scannedRows, totalRows, percent }
  ctx.postMessage(msg)
}

/**
 * 发送完成消息
 */
function postDone(result: StudentImportValidationResult) {
  const msg: WorkerResponse = { type: 'done', result }
  ctx.postMessage(msg)
}

/**
 * 发送错误消息
 */
function postError(message: string, stack?: string, meta?: any) {
  const msg: WorkerResponse = { type: 'error', message, stack, meta }
  ctx.postMessage(msg)
}

/**
 * 纯计算校验函数（与原 validateStudentRows 逻辑一致）
 */
function validateStudentRows(
  rows: StudentImportRowForValidation[],
  context: StudentImportValidationContext,
  options: ValidateStudentImportOptions = {},
  onProgress?: (scannedRows: number, totalRows: number) => void,
  progressStep = 200
): StudentImportValidationResult {
  const previewLimit = options.previewLimit ?? 50
  const maxScanErrors = options.maxScanErrors ?? 5000

  const errors: ImportValidationErrorItem[] = []
  let errorCount = 0
  let truncated = false

  const validatorsByKey = inferSheetKeyValidators(context.columns)
  const requiredTitles = context.columns.filter((c) => c.required).map((c) => c.title)

  const dictSetsMap = new Map<string, Set<string>>()
  for (const [code, list] of Object.entries(context.dictSets)) {
    dictSetsMap.set(code, new Set(list))
  }

  const orgSets = buildOrgCascadeSets(context.orgTree)
  const dormSets = buildDormCascadeSets(context.dormTree)

  const campusTitle = context.columns.find((c) => c.cascadeType === 'campus')?.title
  const deptTitle = context.columns.find((c) => c.cascadeType === 'department')?.title
  const majorTitle = context.columns.find((c) => c.cascadeType === 'major')?.title
  const classTitle = context.columns.find((c) => c.cascadeType === 'class')?.title
  const floorTitle = context.columns.find((c) => c.cascadeType === 'floor')?.title
  const roomTitle = context.columns.find((c) => c.cascadeType === 'room')?.title
  const bedTitle = context.columns.find((c) => c.cascadeType === 'bed')?.title

  const studentNoCol = context.columns.find((c) => c.key === 'studentNo')?.title
  const seenStudentNo = new Set<string>()

  const totalRows = rows.length

  const report = (row1: number, title: string, message: string, value?: string) => {
    errorCount++
    if (errors.length < previewLimit) {
      errors.push({
        row: row1,
        column: normalizeHeaderText(title),
        message,
        value
      })
    }
    if (errorCount >= maxScanErrors) {
      truncated = true
    }
  }

  rows.forEach((row, index) => {
    if (truncated) return

    const values = row.values

    // 必填
    for (const title of requiredTitles) {
      const v = values[title] ?? ''
      if (isEmpty(v)) {
        report(row.row, title, '必填项不能为空', v)
      }
    }

    // 学号重复
    if (studentNoCol) {
      const studentNo = values[studentNoCol] ?? ''
      if (!isEmpty(studentNo)) {
        if (seenStudentNo.has(studentNo)) {
          report(row.row, studentNoCol, '学号在文件内重复', studentNo)
        } else {
          seenStudentNo.add(studentNo)
        }
      }
    }

    // 基础格式
    for (const col of context.columns) {
      const fn = validatorsByKey.get(col.key)
      if (!fn) continue
      const v = values[col.title] ?? ''
      const msg = fn(v)
      if (msg) report(row.row, col.title, msg, v)
    }

    // 枚举/字典
    for (const col of context.columns) {
      if (!col.dropdown || col.cascadeType) continue
      const v = values[col.title] ?? ''
      if (isEmpty(v)) continue

      if (col.dropdown.options && col.dropdown.options.length > 0) {
        const set = new Set(col.dropdown.options)
        if (!set.has(v)) {
          report(row.row, col.title, '值不在下拉选项中', v)
        }
      } else if (col.dropdown.dictCode) {
        const set = dictSetsMap.get(col.dropdown.dictCode)
        if (set && !set.has(v)) {
          report(row.row, col.title, `值不在字典(${col.dropdown.dictCode})中`, v)
        }
      }
    }

    // 级联校验
    const campus = campusTitle ? values[campusTitle] ?? '' : ''
    const dept = deptTitle ? values[deptTitle] ?? '' : ''
    const major = majorTitle ? values[majorTitle] ?? '' : ''
    const cls = classTitle ? values[classTitle] ?? '' : ''
    const floor = floorTitle ? values[floorTitle] ?? '' : ''
    const room = roomTitle ? values[roomTitle] ?? '' : ''
    const bed = bedTitle ? values[bedTitle] ?? '' : ''

    if (campusTitle && !isEmpty(campus) && !orgSets.campusSet.has(campus)) {
      report(row.row, campusTitle, '校区不存在或已停用', campus)
    }

    if (deptTitle && !isEmpty(dept)) {
      if (isEmpty(campus)) {
        report(row.row, deptTitle, '请先填写校区', dept)
      } else {
        const set = orgSets.campusToDept.get(campus)
        if (!set || !set.has(dept)) {
          report(row.row, deptTitle, '院系不属于该校区或不存在', dept)
        }
      }
    }

    if (majorTitle && !isEmpty(major)) {
      if (isEmpty(dept)) {
        report(row.row, majorTitle, '请先填写院系', major)
      } else {
        const set = orgSets.deptToMajor.get(dept)
        if (!set || !set.has(major)) {
          report(row.row, majorTitle, '专业不属于该院系或不存在', major)
        }
      }
    }

    if (classTitle && !isEmpty(cls)) {
      if (isEmpty(major)) {
        report(row.row, classTitle, '请先填写专业', cls)
      } else {
        const set = orgSets.majorToClass.get(major)
        if (!set || !set.has(cls)) {
          report(row.row, classTitle, '班级不属于该专业或不存在', cls)
        }
      }
    }

    if (floorTitle && !isEmpty(floor)) {
      if (isEmpty(campus)) {
        report(row.row, floorTitle, '请先填写校区', floor)
      } else {
        const set = dormSets.campusToFloor.get(campus)
        if (!set || !set.has(floor)) {
          report(row.row, floorTitle, '楼层不属于该校区或不存在', floor)
        }
      }
    }

    if (roomTitle && !isEmpty(room)) {
      if (isEmpty(floor)) {
        report(row.row, roomTitle, '请先填写楼层', room)
      } else {
        const set = dormSets.floorToRoom.get(floor)
        if (!set || !set.has(room)) {
          report(row.row, roomTitle, '房间不属于该楼层或不存在', room)
        }
      }
    }

    if (bedTitle && !isEmpty(bed)) {
      if (isEmpty(room)) {
        report(row.row, bedTitle, '请先填写房间', bed)
      } else {
        const set = dormSets.roomToBed.get(room)
        if (!set || !set.has(bed)) {
          report(row.row, bedTitle, '床位不属于该房间或不存在', bed)
        }
      }
    }

    // 进度上报
    if (onProgress && progressStep > 0 && ((index + 1) % progressStep === 0 || index === totalRows - 1)) {
      onProgress(index + 1, totalRows)
    }
  })

  const ok = errorCount === 0
  return { ok, errorCount, truncated, preview: errors, totalRowsForImport: totalRows }
}

/**
 * 处理 scan-file 模式：Worker 内完成 Excel 读取 + 校验
 */
async function handleScanFile(payload: ScanFilePayload) {
  const { file, context, options = {}, progressStep = 200 } = payload
  const previewLimit = options.previewLimit ?? 50
  const maxTrailingEmptyRows = options.maxTrailingEmptyRows ?? 200

  try {
    // 阶段 1：读取文件 (0-10%)
    postProgress('reading', 0, 100, 0)
    const buffer = await file.arrayBuffer()
    postProgress('reading', 0, 100, 10)

    // 阶段 2：解析 Excel (10-30%)
    postProgress('parsing', 0, 100, 10)
    const XLSX = await import('xlsx')
    const workbook = XLSX.read(buffer, { type: 'array' })
    postProgress('parsing', 0, 100, 20)

    // 选择工作表
    const sheetName = options.sheetName && workbook.Sheets[options.sheetName]
      ? options.sheetName
      : workbook.SheetNames[0]
    const worksheet = workbook.Sheets[sheetName]

    const ref = worksheet['!ref']
    const range = ref ? XLSX.utils.decode_range(ref) : XLSX.utils.decode_range('A1:A1')
    postProgress('parsing', 0, 100, 25)

    // 检测表头
    const expectedHeaders = context.columns.map((c) => c.title)
    const expected = new Set(expectedHeaders.map((h) => normalizeHeaderText(h)))
    const headerScanRows = 10
    const scanTo = Math.min(range.e.r + 1, headerScanRows)

    let headerRow = 1
    let bestScore = -1
    const columnIndexByHeader = new Map<string, number>()

    for (let row = 1; row <= scanTo; row++) {
      const map = new Map<string, number>()
      let score = 0
      for (let c = range.s.c; c <= range.e.c; c++) {
        const a1 = `${getColLetter(c)}${row}`
        const h = normalizeHeaderText(getSheetCellText(worksheet, a1))
        if (h && expected.has(h) && !map.has(h)) {
          map.set(h, c)
          score++
        }
      }
      if (score > bestScore) {
        bestScore = score
        headerRow = row
        columnIndexByHeader.clear()
        map.forEach((v, k) => columnIndexByHeader.set(k, v))
      }
    }
    postProgress('parsing', 0, 100, 30)

    // 映射表头
    const titleToColIndex = new Map<string, number>()
    for (const col of context.columns) {
      const t = normalizeHeaderText(col.title)
      const idx = columnIndexByHeader.get(t)
      if (idx !== undefined) titleToColIndex.set(col.title, idx)
    }

    // 检查必填列
    const missingRequiredColumns = context.columns
      .filter((c) => c.required)
      .map((c) => c.title)
      .filter((t) => !titleToColIndex.has(t))

    if (missingRequiredColumns.length > 0) {
      postError('Excel 表头不完整，请使用系统下载的模板填写', undefined, {
        errorCount: missingRequiredColumns.length,
        truncated: false,
        preview: missingRequiredColumns.slice(0, previewLimit).map((t) => ({
          row: headerRow,
          column: t,
          message: '缺少必填列',
          value: ''
        }))
      })
      return
    }

    // 阶段 3：构造行数据 (30-50%)
    postProgress('preparing', 0, 100, 30)

    const getValueByTitle = (row1: number, title: string): string => {
      const colIndex0 = titleToColIndex.get(title)
      if (colIndex0 === undefined) return ''
      return getSheetCellText(worksheet, toExcelA1(colIndex0, row1))
    }

    let startRow = headerRow + 1
    if (startRow <= range.e.r + 1) {
      const isExample = isLikelyExampleRow(context.columns, (title) =>
        getValueByTitle(startRow, title)
      )
      if (isExample) startRow++
    }

    const endRow = range.e.r + 1

    // 大文件保护
    const MAX_SCAN_ROWS = 200_000
    const totalDataRows = endRow - startRow + 1
    if (totalDataRows > MAX_SCAN_ROWS) {
      postError(
        `当前 Excel 数据行数约为 ${totalDataRows} 行，已超过前端可安全扫描的上限（${MAX_SCAN_ROWS} 行）。建议拆分为多个文件，或直接上传由后台进行完整校验。`,
        undefined,
        {
          errorCount: 1,
          truncated: false,
          preview: [
            {
              row: headerRow,
              column: '系统提示',
              message: `数据行数过多（约 ${totalDataRows} 行），前端已停止扫描以保护浏览器性能。请拆分文件或改用后台批量导入流程。`,
              value: ''
            }
          ]
        }
      )
      return
    }

    let trailingEmpty = 0
    const rows: StudentImportRowForValidation[] = []
    const prepareProgressStep = Math.max(1, Math.floor(totalDataRows / 20)) // 每 5% 上报一次

    for (let row1 = startRow; row1 <= endRow; row1++) {
      // 判空行
      let hasAny = false
      for (const c of titleToColIndex.values()) {
        if (!isEmpty(getSheetCellText(worksheet, toExcelA1(c, row1)))) {
          hasAny = true
          break
        }
      }

      if (!hasAny) {
        trailingEmpty++
        if (trailingEmpty >= maxTrailingEmptyRows) break
        continue
      }
      trailingEmpty = 0

      // 构造行数据
      const values: Record<string, string> = {}
      for (const col of context.columns) {
        values[col.title] = getValueByTitle(row1, col.title)
      }
      rows.push({ row: row1, values })

      // 上报构造进度
      const scannedRows = row1 - startRow + 1
      if (scannedRows % prepareProgressStep === 0) {
        const percent = 30 + Math.floor((scannedRows / totalDataRows) * 20)
        postProgress('preparing', scannedRows, totalDataRows, Math.min(50, percent))
      }
    }

    postProgress('preparing', rows.length, rows.length, 50)

    // 阶段 4：校验 (50-100%)
    const result = validateStudentRows(
      rows,
      context,
      options,
      (scannedRows, totalRows) => {
        const percent = 50 + Math.floor((scannedRows / totalRows) * 50)
        postProgress('validating', scannedRows, totalRows, Math.min(99, percent))
      },
      progressStep
    )

    postProgress('validating', rows.length, rows.length, 100)
    postDone(result)
  } catch (error) {
    const err = error as Error
    postError(err?.message || String(error), err?.stack)
  }
}

/**
 * 处理 scan 模式：接收预处理好的行数据（兼容旧用法）
 */
function handleScan(payload: ScanRowsPayload) {
  const { rows, context, options, progressStep } = payload

  try {
    const result = validateStudentRows(
      rows,
      context,
      options,
      (scannedRows, totalRows) => {
        const percent = totalRows === 0 ? 100 : Math.floor((scannedRows / totalRows) * 100)
        postProgress('validating', scannedRows, totalRows, Math.min(99, percent))
      },
      progressStep ?? 200
    )
    postDone(result)
  } catch (error) {
    const err = error as Error
    postError(err?.message || String(error), err?.stack)
  }
}

// ============================================================================
// Worker 入口
// ============================================================================

ctx.onmessage = async (event: MessageEvent<WorkerRequest>) => {
  const data = event.data
  if (!data || typeof data !== 'object' || !data.type) {
    return
  }

  if (data.type === 'scan-file') {
    await handleScanFile(data.payload)
  } else if (data.type === 'scan') {
    handleScan(data.payload)
  }
}
