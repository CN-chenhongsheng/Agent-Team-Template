/**
 * Excel 级联下拉生成器
 *
 * 使用 ExcelJS 创建带有 INDIRECT 函数的级联下拉框
 * 支持组织架构（校区-院系-专业-班级）和住宿信息（校区-楼层-房间-床位）的级联选择
 *
 * @module utils/excel/cascadeGenerator
 * @author 陈鸿昇
 */
import ExcelJS from 'exceljs'
import type { OrgTreeResponse, DormTreeResponse } from '@/api/student-import'
import { sanitizeName, buildHierarchyName } from './importValidation/cascadeName'

/**
 * 级联配置
 */
export interface CascadeConfig {
  /** 工作簿 */
  workbook: ExcelJS.Workbook
  /** 主工作表 */
  worksheet: ExcelJS.Worksheet
  /** 组织架构树数据 */
  orgTree?: OrgTreeResponse
  /** 住宿结构树数据 */
  dormTree?: DormTreeResponse
  /** 数据验证的行数 */
  validationRowCount: number
  /** 是否包含示例行（会影响数据验证的起始行） */
  includeExample: boolean
}

// sanitizeName/buildHierarchyName 已抽离到 importValidation/cascadeName.ts 共享

/**
 * 设置组织架构级联下拉
 */
export function setupOrgCascade(
  workbook: ExcelJS.Workbook,
  orgSheet: ExcelJS.Worksheet,
  orgTree: OrgTreeResponse,
  columnConfig: {
    campusCol: string // 校区列字母
    deptCol: string // 院系列字母
    majorCol: string // 专业列字母
    classCol: string // 班级列字母
  },
  validationRowCount: number,
  startRow: number
): void {
  const { campusCol, deptCol, majorCol, classCol } = columnConfig
  const campuses = orgTree.campuses || []

  if (campuses.length === 0) {
    console.warn('[cascadeGenerator] 组织架构树为空')
    return
  }

  let currentCol = 1

  // 1. 写入校区列表（使用 sanitizeName 处理后的名称）
  const campusSanitizedNames = campuses.map((c) => sanitizeName(c.name))
  campusSanitizedNames.forEach((name, idx) => {
    orgSheet.getCell(idx + 1, currentCol).value = name
  })

  // 创建校区命名范围
  const campusRangeName = '校区列表'
  workbook.definedNames.add(`'_OrgData'!$A$1:$A$${campusSanitizedNames.length}`, campusRangeName)
  currentCol++

  // 2. 为每个校区创建院系列表
  for (const campus of campuses) {
    const departments = campus.children || []
    if (departments.length === 0) continue

    const sanitizedCampusName = sanitizeName(campus.name)
    // 院系使用 "校区_院系名" 格式，避免不同校区同名院系冲突
    const deptSanitizedNames = departments.map((d) =>
      buildHierarchyName(sanitizedCampusName, d.name, 'D')
    )
    deptSanitizedNames.forEach((name, idx) => {
      orgSheet.getCell(idx + 1, currentCol).value = name
    })

    const rangeName = sanitizedCampusName + '_院系'
    const colLetter = getColLetter(currentCol)
    workbook.definedNames.add(`'_OrgData'!$${colLetter}$1:$${colLetter}$${deptSanitizedNames.length}`, rangeName)
    currentCol++

    // 3. 为每个院系创建专业列表
    for (let deptIdx = 0; deptIdx < departments.length; deptIdx++) {
      const dept = departments[deptIdx]
      const majors = dept.children || []
      if (majors.length === 0) continue

      // 使用已构建的层级名称作为父级
      const sanitizedDeptName = deptSanitizedNames[deptIdx]
      // 专业使用 "院系层级名_专业名" 格式
      const majorSanitizedNames = majors.map((m) =>
        buildHierarchyName(sanitizedDeptName, m.name, 'M')
      )
      majorSanitizedNames.forEach((name, idx) => {
        orgSheet.getCell(idx + 1, currentCol).value = name
      })

      const majorRangeName = sanitizedDeptName + '_专业'
      const majorColLetter = getColLetter(currentCol)
      workbook.definedNames.add(`'_OrgData'!$${majorColLetter}$1:$${majorColLetter}$${majorSanitizedNames.length}`, majorRangeName)
      currentCol++

      // 4. 为每个专业创建班级列表
      for (let majorIdx = 0; majorIdx < majors.length; majorIdx++) {
        const major = majors[majorIdx]
        const classes = major.children || []
        if (classes.length === 0) continue

        // 使用已构建的层级名称作为父级
        const sanitizedMajorName = majorSanitizedNames[majorIdx]
        // 班级使用 "专业层级名_班级名" 格式
        const classSanitizedNames = classes.map((c) =>
          buildHierarchyName(sanitizedMajorName, c.name, 'C')
        )
        classSanitizedNames.forEach((name, idx) => {
          orgSheet.getCell(idx + 1, currentCol).value = name
        })

        const classRangeName = sanitizedMajorName + '_班级'
        const classColLetter = getColLetter(currentCol)
        workbook.definedNames.add(`'_OrgData'!$${classColLetter}$1:$${classColLetter}$${classSanitizedNames.length}`, classRangeName)
        currentCol++
      }
    }
  }

  // 5. 设置主工作表的数据验证
  const endRow = startRow + validationRowCount - 1

  // 校区列：直接引用校区列表
  for (let row = startRow; row <= endRow; row++) {
    workbook.worksheets[0].getCell(`${campusCol}${row}`).dataValidation = {
      type: 'list',
      allowBlank: true,
      formulae: [campusRangeName],
      showErrorMessage: true,
      errorTitle: '输入错误',
      error: '请从下拉列表中选择有效值'
    }
  }

  // 院系列：INDIRECT 引用校区单元格
  for (let row = startRow; row <= endRow; row++) {
    workbook.worksheets[0].getCell(`${deptCol}${row}`).dataValidation = {
      type: 'list',
      allowBlank: true,
      formulae: [`INDIRECT($${campusCol}${row}&"_院系")`],
      showErrorMessage: true,
      errorTitle: '输入错误',
      error: '请先选择校区，再选择院系'
    }
  }

  // 专业列：INDIRECT 引用院系单元格
  for (let row = startRow; row <= endRow; row++) {
    workbook.worksheets[0].getCell(`${majorCol}${row}`).dataValidation = {
      type: 'list',
      allowBlank: true,
      formulae: [`INDIRECT($${deptCol}${row}&"_专业")`],
      showErrorMessage: true,
      errorTitle: '输入错误',
      error: '请先选择院系，再选择专业'
    }
  }

  // 班级列：INDIRECT 引用专业单元格
  for (let row = startRow; row <= endRow; row++) {
    workbook.worksheets[0].getCell(`${classCol}${row}`).dataValidation = {
      type: 'list',
      allowBlank: true,
      formulae: [`INDIRECT($${majorCol}${row}&"_班级")`],
      showErrorMessage: true,
      errorTitle: '输入错误',
      error: '请先选择专业，再选择班级'
    }
  }
}

/**
 * 设置住宿信息级联下拉
 */
export function setupDormCascade(
  workbook: ExcelJS.Workbook,
  dormSheet: ExcelJS.Worksheet,
  dormTree: DormTreeResponse,
  columnConfig: {
    floorCol: string  // 楼层列字母
    roomCol: string   // 房间列字母
    bedCol: string    // 床位列字母
  },
  campusCol: string,  // 校区列字母（复用组织架构的校区列）
  validationRowCount: number,
  startRow: number
): void {
  const { floorCol, roomCol, bedCol } = columnConfig
  const campuses = dormTree.campuses || []

  if (campuses.length === 0) {
    console.warn('[cascadeGenerator] 住宿结构树为空')
    return
  }

  let currentCol = 1

  // 1. 为每个校区创建楼层列表
  for (const campus of campuses) {
    const floors = campus.children || []
    if (floors.length === 0) continue

    const sanitizedCampusName = sanitizeName(campus.name)
    // 楼层使用 "校区_楼层" 格式，避免不同校区同名楼层冲突
    const floorSanitizedNames = floors.map((f) =>
      buildHierarchyName(sanitizedCampusName, f.name, 'F')
    )
    floorSanitizedNames.forEach((name, idx) => {
      dormSheet.getCell(idx + 1, currentCol).value = name
    })

    const rangeName = sanitizedCampusName + '_楼层'
    const colLetter = getColLetter(currentCol)
    workbook.definedNames.add(`'_DormData'!$${colLetter}$1:$${colLetter}$${floorSanitizedNames.length}`, rangeName)
    currentCol++

    // 2. 为每个楼层创建房间列表
    for (let floorIdx = 0; floorIdx < floors.length; floorIdx++) {
      const floor = floors[floorIdx]
      const rooms = floor.children || []
      if (rooms.length === 0) continue

      // 使用已构建的层级名称作为父级
      const sanitizedFloorName = floorSanitizedNames[floorIdx]
      // 房间使用 "楼层层级名_房间号" 格式
      const roomSanitizedNames = rooms.map((r) =>
        buildHierarchyName(sanitizedFloorName, r.name, 'R')
      )
      roomSanitizedNames.forEach((name, idx) => {
        dormSheet.getCell(idx + 1, currentCol).value = name
      })

      const roomRangeName = sanitizedFloorName + '_房间'
      const roomColLetter = getColLetter(currentCol)
      workbook.definedNames.add(`'_DormData'!$${roomColLetter}$1:$${roomColLetter}$${roomSanitizedNames.length}`, roomRangeName)
      currentCol++

      // 3. 为每个房间创建床位列表
      for (let roomIdx = 0; roomIdx < rooms.length; roomIdx++) {
        const room = rooms[roomIdx]
        const beds = room.children || []
        if (beds.length === 0) continue

        // 使用已构建的层级名称作为父级
        const sanitizedRoomName = roomSanitizedNames[roomIdx]
        // 床位使用 "房间层级名_床位号" 格式
        const bedSanitizedNames = beds.map((b) =>
          buildHierarchyName(sanitizedRoomName, b.name, 'B')
        )
        bedSanitizedNames.forEach((name, idx) => {
          dormSheet.getCell(idx + 1, currentCol).value = name
        })

        const bedRangeName = sanitizedRoomName + '_床位'
        const bedColLetter = getColLetter(currentCol)
        workbook.definedNames.add(`'_DormData'!$${bedColLetter}$1:$${bedColLetter}$${bedSanitizedNames.length}`, bedRangeName)
        currentCol++
      }
    }
  }

  // 4. 设置主工作表的数据验证
  const endRow = startRow + validationRowCount - 1

  // 楼层列：INDIRECT 引用校区单元格
  for (let row = startRow; row <= endRow; row++) {
    workbook.worksheets[0].getCell(`${floorCol}${row}`).dataValidation = {
      type: 'list',
      allowBlank: true,
      formulae: [`INDIRECT($${campusCol}${row}&"_楼层")`],
      showErrorMessage: true,
      errorTitle: '输入错误',
      error: '请先选择校区，再选择楼层'
    }
  }

  // 房间列：INDIRECT 引用楼层单元格
  for (let row = startRow; row <= endRow; row++) {
    workbook.worksheets[0].getCell(`${roomCol}${row}`).dataValidation = {
      type: 'list',
      allowBlank: true,
      formulae: [`INDIRECT($${floorCol}${row}&"_房间")`],
      showErrorMessage: true,
      errorTitle: '输入错误',
      error: '请先选择楼层，再选择房间'
    }
  }

  // 床位列：INDIRECT 引用房间单元格
  for (let row = startRow; row <= endRow; row++) {
    workbook.worksheets[0].getCell(`${bedCol}${row}`).dataValidation = {
      type: 'list',
      allowBlank: true,
      formulae: [`INDIRECT($${roomCol}${row}&"_床位")`],
      showErrorMessage: true,
      errorTitle: '输入错误',
      error: '请先选择房间，再选择床位'
    }
  }
}

/**
 * 获取列字母（1 -> A, 2 -> B, ... 27 -> AA）
 */
function getColLetter(colNum: number): string {
  let letter = ''
  let num = colNum
  while (num > 0) {
    const remainder = (num - 1) % 26
    letter = String.fromCharCode(65 + remainder) + letter
    num = Math.floor((num - 1) / 26)
  }
  return letter
}

/**
 * 设置完整的级联下拉
 * 同时处理组织架构和住宿信息的级联
 */
export async function setupCascadeDropdowns(
  config: CascadeConfig,
  columnMapping: {
    campus: string    // 校区列字母
    dept: string      // 院系列字母
    major: string     // 专业列字母
    class: string     // 班级列字母
    floor: string     // 楼层列字母
    room: string      // 房间列字母
    bed: string       // 床位列字母
  }
): Promise<void> {
  const { workbook, orgTree, dormTree, validationRowCount, includeExample } = config
  const startRow = includeExample ? 3 : 2

  // 创建组织架构数据工作表
  if (orgTree && orgTree.campuses && orgTree.campuses.length > 0) {
    const orgSheet = workbook.addWorksheet('_OrgData', { state: 'veryHidden' })
    setupOrgCascade(
      workbook,
      orgSheet,
      orgTree,
      {
        campusCol: columnMapping.campus,
        deptCol: columnMapping.dept,
        majorCol: columnMapping.major,
        classCol: columnMapping.class
      },
      validationRowCount,
      startRow
    )
  }

  // 创建住宿数据工作表
  if (dormTree && dormTree.campuses && dormTree.campuses.length > 0) {
    const dormSheet = workbook.addWorksheet('_DormData', { state: 'veryHidden' })
    setupDormCascade(
      workbook,
      dormSheet,
      dormTree,
      {
        floorCol: columnMapping.floor,
        roomCol: columnMapping.room,
        bedCol: columnMapping.bed
      },
      columnMapping.campus, // 复用校区列
      validationRowCount,
      startRow
    )
  }
}
