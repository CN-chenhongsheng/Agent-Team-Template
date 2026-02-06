/**
 * 学生导入 Excel 前端校验器（严格模式）
 *
 * - 使用 xlsx 读取 Excel
 * - 依据模板列配置校验必填、格式、字典/枚举
 * - 依据组织/宿舍树校验级联父子关系（使用与模板生成相同的命名规则）
 *
 * @module utils/excel/importValidation/studentImportValidator
 * @author 陈鸿昇
 */

import { useDictStore } from '@/store/modules/dict'
import { fetchImportTrees } from '@/api/student-import'
import { validateChineseIDCard } from '@/utils/form/validator'
import type { TemplateColumn } from '../templateGenerator'
import { buildHierarchyName, sanitizeName } from './cascadeName'
import { ImportValidationError, type ImportValidationErrorItem } from './errors'
import {
  getExcelSheetContext,
  getColLetter,
  getSheetCellText,
  normalizeHeaderText
} from './readExcel'

export interface ValidateStudentImportOptions {
  sheetName?: string
  /** 预览错误条数（用于弹窗展示） */
  previewLimit?: number
  /** 发现错误超过该阈值后提前终止扫描（性能保护） */
  maxScanErrors?: number
  /** 允许连续空行多少行后停止扫描（防止 !ref 过大时空跑） */
  maxTrailingEmptyRows?: number
}

export interface StudentImportValidationResult {
  ok: boolean
  /** 总错误数（可能为 >=，见 truncated） */
  errorCount: number
  /** 是否提前终止扫描 */
  truncated: boolean
  /** 仅展示前 N 条 */
  preview: ImportValidationErrorItem[]
  /**
   * 实际参与导入的有效行数
   * - 已按空行规则过滤（与 prepareStudentImportValidation 保持一致）
   */
  totalRowsForImport: number
}

/**
 * 学生导入校验的行数据（纯数据结构，方便在 Web Worker 中传递）
 */
export interface StudentImportRowForValidation {
  /** Excel 行号（从 1 开始） */
  row: number
  /** key 为模板列标题（去掉 *），value 为单元格文本值 */
  values: Record<string, string>
}

/**
 * 学生导入校验上下文（不包含任何 Pinia/Vue/DOM 依赖，适合在 Worker 中传递）
 */
export interface StudentImportValidationContext {
  /** 模板列配置 */
  columns: TemplateColumn[]
  /**
   * 字典值集合：dictCode -> string[]
   * 注意：这里只保存 label 文本，真正的 Set 构建在纯计算函数内部完成
   */
  dictSets: Record<string, string[]>
  /** 组织架构树数据 */
  orgTree: import('@/api/student-import').OrgTreeResponse
  /** 宿舍结构树数据 */
  dormTree: import('@/api/student-import').DormTreeResponse
}

/**
 * 纯计算校验函数的参数
 */
export interface ValidateStudentRowsParams {
  rows: StudentImportRowForValidation[]
  context: StudentImportValidationContext
  options?: ValidateStudentImportOptions
  /**
   * 进度回调（用于 Web Worker 主动上报扫描进度）
   * - scannedRows：已处理的行数
   * - totalRows：总行数
   */
  onProgress?: (payload: { scannedRows: number; totalRows: number }) => void
  /**
   * 进度步长：每处理多少行触发一次 onProgress，默认 200
   */
  progressStep?: number
}

function isEmpty(value: string): boolean {
  return !value || value.trim() === ''
}

function toExcelA1(colIndex0: number, row1: number): string {
  return `${getColLetter(colIndex0)}${row1}`
}

function buildOrgCascadeSets(orgTree: import('@/api/student-import').OrgTreeResponse) {
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

function buildDormCascadeSets(dormTree: import('@/api/student-import').DormTreeResponse) {
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

function buildBasicFormatValidators() {
  const phoneRe = /^1\d{10}$/
  const emailRe = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  const dateRe = /^\d{4}-\d{2}-\d{2}$/
  const yearRe = /^\d{4}$/

  return {
    phone: (v: string) => (isEmpty(v) || phoneRe.test(v) ? '' : '手机号格式不正确（需 11 位数字）'),
    // 身份证：复用全局表单校验工具，包含出生日期 + 校验码算法校验
    idCard: (v: string) =>
      isEmpty(v) || validateChineseIDCard(v) ? '' : '身份证号格式不正确（需 18 位且通过校验码验证）',
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

  // 只返回模板中出现的 key 的校验器
  const result = new Map<string, (value: string) => string>()
  for (const col of columns) {
    const fn = validatorsByKey[col.key]
    if (fn) result.set(col.key, fn)
  }
  return result
}

/**
 * 判断是否为示例行（供 Worker 使用）
 */
export function isLikelyExampleRow(
  row1: number,
  columns: TemplateColumn[],
  getValue: (title: string) => string
): boolean {
  // 只看有 example 的列，命中率足够高则认为是示例行
  const cols = columns.filter((c) => c.example && c.example.trim() !== '')
  if (cols.length === 0) return false

  let hit = 0
  let total = 0
  for (const col of cols) {
    total++
    const v = getValue(col.title)
    if (!isEmpty(v) && v === col.example) hit++
  }

  // 命中 >= 60% 且至少命中 3 列，判定为示例行
  return total >= 3 && hit >= Math.max(3, Math.ceil(total * 0.6))
}

/**
 * 仅预加载校验所需的上下文数据（字典 + 树数据）
 *
 * 注意：
 * - 该函数运行在主线程（依赖 Pinia、HTTP 请求）
 * - 不读取 Excel 文件，Excel 读取由 Worker 完成
 * - 返回的 context 结构只包含可结构化克隆的数据，适合传递给 Web Worker
 */
export async function prepareValidationContextOnly(
  columns: TemplateColumn[]
): Promise<StudentImportValidationContext> {
  // 1) 加载字典（严格模式：字典为空视为不可校验，直接中断避免误杀）
  const dictCodes = columns
    .filter((c) => c.dropdown?.dictCode && !c.cascadeType)
    .map((c) => c.dropdown!.dictCode!)
  const dictStore = useDictStore()
  const uniqueDictCodes = [...new Set(dictCodes)]
  const dictData = await dictStore.loadDictDataBatch(uniqueDictCodes)

  const plainDictSets: Record<string, string[]> = {}
  for (const code of uniqueDictCodes) {
    const items = dictData?.[code]
    if (!items || items.length === 0) {
      throw new ImportValidationError(`无法加载字典数据：${code}（请刷新后重试）`, {
        errorCount: 1,
        truncated: false,
        preview: [
          {
            row: 1,
            column: '字典',
            message: `字典 ${code} 加载为空`,
            value: ''
          }
        ]
      })
    }
    plainDictSets[code] = items.map((it) => it.label)
  }

  // 2) 拉取树数据
  const { orgTree, dormTree } = await fetchImportTrees()

  return {
    columns,
    dictSets: plainDictSets,
    orgTree,
    dormTree
  }
}

/**
 * 读取 Excel + 加载字典/树数据，准备纯计算校验所需的上下文与行数据
 *
 * 注意：
 * - 该函数运行在主线程（依赖 xlsx、Pinia、HTTP 请求）
 * - 返回的 rows/context 结构只包含可结构化克隆的数据，适合传递给 Web Worker
 * - 【已废弃】建议使用 prepareValidationContextOnly + Worker 内读取 Excel 的方式
 */
export async function prepareStudentImportValidation(
  file: File,
  columns: TemplateColumn[],
  options: ValidateStudentImportOptions = {}
): Promise<{
  rows: StudentImportRowForValidation[]
  context: StudentImportValidationContext
}> {
  const previewLimit = options.previewLimit ?? 50
  const maxTrailingEmptyRows = options.maxTrailingEmptyRows ?? 200

  const expectedHeaders = columns.map((c) => c.title)
  const ctx = await getExcelSheetContext(file, {
    sheetName: options.sheetName,
    expectedHeaders,
    headerScanRows: 10
  })

  const { worksheet, range } = ctx

  // 1) 映射表头：TemplateColumn.title -> columnIndex
  const titleToColIndex = new Map<string, number>()
  for (const col of columns) {
    const t = normalizeHeaderText(col.title)
    const idx = ctx.columnIndexByHeader.get(t)
    if (idx !== undefined) titleToColIndex.set(col.title, idx)
  }

  // 必填列必须存在（与原校验逻辑保持一致）
  const missingRequiredColumns = columns
    .filter((c) => c.required)
    .map((c) => c.title)
    .filter((t) => !titleToColIndex.has(t))

  if (missingRequiredColumns.length > 0) {
    throw new ImportValidationError('Excel 表头不完整，请使用系统下载的模板填写', {
      errorCount: missingRequiredColumns.length,
      truncated: false,
      preview: missingRequiredColumns.slice(0, previewLimit).map((t) => ({
        row: ctx.headerRow,
        column: t,
        message: '缺少必填列',
        value: ''
      }))
    })
  }

  // 2) 加载字典（严格模式：字典为空视为不可校验，直接中断避免误杀）
  const dictCodes = columns
    .filter((c) => c.dropdown?.dictCode && !c.cascadeType)
    .map((c) => c.dropdown!.dictCode!)
  const dictStore = useDictStore()
  const uniqueDictCodes = [...new Set(dictCodes)]
  const dictData = await dictStore.loadDictDataBatch(uniqueDictCodes)

  const plainDictSets: Record<string, string[]> = {}
  for (const code of uniqueDictCodes) {
    const items = dictData?.[code]
    if (!items || items.length === 0) {
      throw new ImportValidationError(`无法加载字典数据：${code}（请刷新后重试）`, {
        errorCount: 1,
        truncated: false,
        preview: [
          {
            row: ctx.headerRow,
            column: '字典',
            message: `字典 ${code} 加载为空`,
            value: ''
          }
        ]
      })
    }
    plainDictSets[code] = items.map((it) => it.label)
  }

  // 3) 拉取树数据（仅构造基础数据，具体级联校验在纯计算函数中完成）
  const { orgTree, dormTree } = await fetchImportTrees()

  // 4) 扫描数据行 -> 构造纯数据行结构（只读取单元格值，不做任何业务校验）
  const getValueByTitle = (row1: number, title: string): string => {
    const colIndex0 = titleToColIndex.get(title)
    if (colIndex0 === undefined) return ''
    return getSheetCellText(worksheet, toExcelA1(colIndex0, row1))
  }

  // 数据起始行：表头下一行；若识别到示例行则跳过
  let startRow = ctx.headerRow + 1
  if (startRow <= range.e.r + 1) {
    const isExample = isLikelyExampleRow(startRow, columns, (title) =>
      getValueByTitle(startRow, title)
    )
    if (isExample) startRow++
  }

  const endRow = range.e.r + 1

  // 为避免极端大文件在前端扫描导致浏览器内存/性能问题，这里对可扫描的最大行数做保护
  const MAX_SCAN_ROWS = 200_000
  const totalDataRows = endRow - startRow + 1
  if (totalDataRows > MAX_SCAN_ROWS) {
    throw new ImportValidationError(
      `当前 Excel 数据行数约为 ${totalDataRows} 行，已超过前端可安全扫描的上限（${MAX_SCAN_ROWS} 行）。` +
        '建议拆分为多个文件，或直接上传由后台进行完整校验。',
      {
        errorCount: 1,
        truncated: false,
        preview: [
          {
            row: ctx.headerRow,
            column: '系统提示',
            message:
              `数据行数过多（约 ${totalDataRows} 行），前端已停止扫描以保护浏览器性能。` +
              '请拆分文件或改用后台批量导入流程。',
            value: ''
          }
        ]
      }
    )
  }

  let trailingEmpty = 0

  const rows: StudentImportRowForValidation[] = []

  for (let row1 = startRow; row1 <= endRow; row1++) {
    // 判空行：任何一个已映射列有值即可
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

    // 构造该行的 values 映射：列标题 -> 文本值
    const values: Record<string, string> = {}
    for (const col of columns) {
      values[col.title] = getValueByTitle(row1, col.title)
    }
    rows.push({ row: row1, values })
  }

  return {
    rows,
    context: {
      columns,
      dictSets: plainDictSets,
      orgTree,
      dormTree
    }
  }
}

/**
 * 纯计算学生导入校验：
 * - 仅依赖 rows + context，不依赖 Pinia/Vue/DOM
 * - 适合在 Web Worker 或主线程中调用
 */
export function validateStudentRows(params: ValidateStudentRowsParams): StudentImportValidationResult {
  const { rows, context, options, onProgress, progressStep = 200 } = params
  const previewLimit = options?.previewLimit ?? 50
  const maxScanErrors = options?.maxScanErrors ?? 5000

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
    if (truncated) {
      return
    }

    const values = row.values

    // 必填
    for (const title of requiredTitles) {
      const v = values[title] ?? ''
      if (isEmpty(v)) {
        report(row.row, title, '必填项不能为空', v)
      }
    }

    // 学号重复（按 key=studentNo）
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

    // 基础格式（按 key）
    for (const col of context.columns) {
      const fn = validatorsByKey.get(col.key)
      if (!fn) continue
      const v = values[col.title] ?? ''
      const msg = fn(v)
      if (msg) report(row.row, col.title, msg, v)
    }

    // 枚举/字典（非级联列）
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

    // 级联严格校验（path 格式）
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

    // 宿舍信息（可选，但填写了就必须级联正确）
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

    // 进度上报（Worker 用）
    if (onProgress && progressStep > 0 && ((index + 1) % progressStep === 0 || index === totalRows - 1)) {
      onProgress({ scannedRows: index + 1, totalRows })
    }
  })

  const ok = errorCount === 0
  return { ok, errorCount, truncated, preview: errors, totalRowsForImport: totalRows }
}

export async function validateStudentImportExcel(
  file: File,
  columns: TemplateColumn[],
  options: ValidateStudentImportOptions = {}
): Promise<StudentImportValidationResult> {
  const { rows, context } = await prepareStudentImportValidation(file, columns, options)
  return validateStudentRows({ rows, context, options })
}

/**
 * 校验并在失败时抛出 ImportValidationError（方便上层统一处理）
 */
export async function assertStudentImportExcelValid(
  file: File,
  columns: TemplateColumn[],
  options: ValidateStudentImportOptions = {}
): Promise<void> {
  const res = await validateStudentImportExcel(file, columns, options)
  if (res.ok) return

  throw new ImportValidationError('前端校验未通过，请修正后再上传', {
    errorCount: res.errorCount,
    truncated: res.truncated,
    preview: res.preview
  })
}
