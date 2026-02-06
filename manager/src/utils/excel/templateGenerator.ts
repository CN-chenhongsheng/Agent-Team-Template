/**
 * Excel 模板生成器
 *
 * 使用 ExcelJS 动态生成带下拉框的 Excel 导入模板
 *
 * @module utils/excel/templateGenerator
 * @author 陈鸿昇
 */
import ExcelJS from 'exceljs'
import FileSaver from 'file-saver'
import { useDictStore } from '@/store/modules/dict'

/**
 * 模板列配置接口
 */
export interface TemplateColumn {
  /** 列标题 */
  title: string
  /** 字段key（用于数据映射） */
  key: string
  /** 列宽度（字符数） */
  width?: number
  /** 是否必填（标题会加 * 号） */
  required?: boolean
  /** 下拉选项配置 */
  dropdown?: {
    /** 固定选项 */
    options?: string[]
    /** 字典编码（动态加载） */
    dictCode?: string
    /** 自定义获取选项函数 */
    fetchOptions?: () => Promise<string[]>
  }
  /** 示例值（填在第二行） */
  example?: string
  /** 提示信息（批注） */
  comment?: string
  /**
   * 级联类型（标记该列参与级联下拉）
   * 级联列由 cascadeGenerator 单独处理，不走普通下拉逻辑
   */
  cascadeType?:
    | 'campus'     // 校区（第一级）
    | 'department' // 院系（第二级，依赖校区）
    | 'major'      // 专业（第三级，依赖院系）
    | 'class'      // 班级（第四级，依赖专业）
    | 'floor'      // 楼层（第二级，依赖校区）
    | 'room'       // 房间（第三级，依赖楼层）
    | 'bed'        // 床位（第四级，依赖房间）
}

/**
 * 级联数据配置
 * 直接使用 student-import.ts 中定义的类型
 */
export interface CascadeData {
  /** 组织架构树数据 */
  orgTree?: import('@/api/student-import').OrgTreeResponse
  /** 住宿结构树数据 */
  dormTree?: import('@/api/student-import').DormTreeResponse
}

/**
 * 模板生成配置接口
 */
export interface TemplateConfig {
  /** 列配置 */
  columns: TemplateColumn[]
  /** 文件名（不含扩展名） */
  filename: string
  /** 工作表名称 */
  sheetName?: string
  /** 是否包含示例行 */
  includeExample?: boolean
  /** 下拉框验证的行数（默认100行） */
  validationRowCount?: number
  /** 级联数据（如果有级联列，需要提供） */
  cascadeData?: CascadeData
}

/**
 * 加载字典数据
 * @param dictCodes 字典编码数组
 * @returns 字典数据 Map
 */
async function loadDictData(
  dictCodes: string[]
): Promise<Record<string, string[]>> {
  if (dictCodes.length === 0) {
    return {}
  }

  const dictStore = useDictStore()
  const dictData = await dictStore.loadDictDataBatch(dictCodes)

  const result: Record<string, string[]> = {}
  for (const code of dictCodes) {
    const items = dictData[code] || []
    result[code] = items.map((item) => item.label)
  }

  return result
}

/**
 * 获取列的下拉选项
 * @param column 列配置
 * @param dictData 字典数据
 * @returns 选项数组
 */
async function getColumnOptions(
  column: TemplateColumn,
  dictData: Record<string, string[]>
): Promise<string[] | null> {
  if (!column.dropdown) {
    return null
  }

  // 优先使用固定选项
  if (column.dropdown.options && column.dropdown.options.length > 0) {
    return column.dropdown.options
  }

  // 使用字典数据
  if (column.dropdown.dictCode) {
    return dictData[column.dropdown.dictCode] || null
  }

  // 使用自定义函数
  if (column.dropdown.fetchOptions) {
    try {
      return await column.dropdown.fetchOptions()
    } catch (error) {
      console.error(`获取列 ${column.title} 的选项失败:`, error)
      return null
    }
  }

  return null
}

/**
 * 主题色配置
 */
const THEME_COLORS = {
  // 表头背景色 - 深蓝色
  headerBg: '4472C4',
  // 表头文字色 - 白色
  headerText: 'FFFFFF',
  // 必填标记色 - 红色
  requiredMark: 'FF6B6B',
  // 示例行背景色 - 浅蓝色
  exampleBg: 'E8F4FD',
  // 示例行文字色 - 灰色
  exampleText: '7F8C8D',
  // 下拉框列标记色 - 浅绿色
  dropdownBg: 'E8F8F5',
  // 边框色
  border: 'BDC3C7'
}

/**
 * 生成智能批注内容
 * @param column 列配置
 * @param options 下拉选项
 * @returns 批注文本
 */
function generateSmartNote(
  column: TemplateColumn,
  options: string[] | null
): string {
  const notes: string[] = []

  // 必填提示
  if (column.required) {
    notes.push('⚠️ 此字段为必填项')
  }

  // 自定义批注
  if (column.comment) {
    notes.push(`📝 ${column.comment}`)
  }

  // 下拉选项提示
  if (options && options.length > 0) {
    if (options.length <= 10) {
      notes.push(`📋 可选值: ${options.join('、')}`)
    } else {
      notes.push(`📋 可选值: ${options.slice(0, 8).join('、')}... 等${options.length}项`)
    }
  }

  // 示例提示
  if (column.example) {
    notes.push(`💡 示例: ${column.example}`)
  }

  return notes.join('\n')
}

/**
 * 生成并下载 Excel 模板
 * @param config 模板配置
 */
export async function generateTemplate(config: TemplateConfig): Promise<void> {
  const {
    columns,
    filename,
    sheetName = 'Sheet1',
    includeExample = true,
    validationRowCount = 100,
    cascadeData
  } = config

  // 1. 收集需要加载的字典编码（排除级联列）
  const dictCodes = columns
    .filter((col) => col.dropdown?.dictCode && !col.cascadeType)
    .map((col) => col.dropdown!.dictCode!)

  // 2. 批量加载字典数据
  const dictData = await loadDictData([...new Set(dictCodes)])

  // 3. 预先获取所有列的下拉选项（排除级联列，级联列由 cascadeGenerator 处理）
  const columnOptionsMap: Map<number, string[]> = new Map()
  for (let i = 0; i < columns.length; i++) {
    // 跳过级联列
    if (columns[i].cascadeType) continue
    const options = await getColumnOptions(columns[i], dictData)
    if (options && options.length > 0) {
      columnOptionsMap.set(i, options)
    }
  }

  // 3.1 构建级联列映射（列索引 -> 列字母）
  const cascadeColumnMap: Record<string, string> = {}
  columns.forEach((col, index) => {
    if (col.cascadeType) {
      cascadeColumnMap[col.cascadeType] = getColumnLetter(index + 1)
    }
  })

  // 4. 创建工作簿和工作表
  const workbook = new ExcelJS.Workbook()
  workbook.creator = '宿舍管理系统'
  workbook.created = new Date()
  workbook.modified = new Date()

  const worksheet = workbook.addWorksheet(sheetName, {
    views: [{ state: 'frozen', ySplit: 1 }] // 冻结首行
  })

  // 5. 设置列配置（宽度）
  worksheet.columns = columns.map((col) => ({
    key: col.key,
    width: col.width || Math.max(col.title.length * 2 + 4, 14)
  }))

  // 6. 添加表头行
  const headerRow = worksheet.addRow(
    columns.map((col) => (col.required ? `*${col.title}` : col.title))
  )
  headerRow.height = 28 // 设置行高

  // 7. 设置表头样式 - 精美渐变效果
  headerRow.eachCell((cell, colNumber) => {
    const column = columns[colNumber - 1]
    const isRequired = column?.required

    // 字体样式
    cell.font = {
      bold: true,
      size: 11,
      color: { argb: THEME_COLORS.headerText },
      name: '微软雅黑'
    }

    // 背景填充
    cell.fill = {
      type: 'pattern',
      pattern: 'solid',
      fgColor: { argb: THEME_COLORS.headerBg }
    }

    // 对齐方式
    cell.alignment = {
      horizontal: 'center',
      vertical: 'middle',
      wrapText: true
    }

    // 边框
    cell.border = {
      top: { style: 'thin', color: { argb: THEME_COLORS.border } },
      left: { style: 'thin', color: { argb: THEME_COLORS.border } },
      bottom: { style: 'medium', color: { argb: THEME_COLORS.headerBg } },
      right: { style: 'thin', color: { argb: THEME_COLORS.border } }
    }

    // 生成智能批注
    const options = columnOptionsMap.get(colNumber - 1) || null
    const noteText = generateSmartNote(column, options)
    if (noteText) {
      cell.note = {
        texts: [
          {
            font: { size: 10, name: '微软雅黑', color: { argb: '333333' } },
            text: noteText
          }
        ],
        margins: {
          insetmode: 'custom',
          inset: [0.05, 0.05, 0.05, 0.05]
        }
      }
    }

    // 必填字段特殊标记 - 在单元格值中已有*号，这里通过颜色区分
    if (isRequired) {
      // 必填列使用稍深的背景色
      cell.fill = {
        type: 'pattern',
        pattern: 'solid',
        fgColor: { argb: '365F91' } // 更深的蓝色
      }
    }
  })

  // 8. 添加示例行（如果需要）
  if (includeExample) {
    const exampleData = columns.map((col) => col.example || '')
    const exampleRow = worksheet.addRow(exampleData)
    exampleRow.height = 22

    exampleRow.eachCell((cell, colNumber) => {
      const hasDropdown = columnOptionsMap.has(colNumber - 1)

      // 背景色 - 下拉框列用浅绿色，其他用浅蓝色
      cell.fill = {
        type: 'pattern',
        pattern: 'solid',
        fgColor: { argb: hasDropdown ? THEME_COLORS.dropdownBg : THEME_COLORS.exampleBg }
      }

      // 字体
      cell.font = {
        size: 10,
        color: { argb: THEME_COLORS.exampleText },
        italic: true,
        name: '微软雅黑'
      }

      // 对齐
      cell.alignment = {
        horizontal: 'center',
        vertical: 'middle'
      }

      // 边框
      cell.border = {
        top: { style: 'thin', color: { argb: THEME_COLORS.border } },
        left: { style: 'thin', color: { argb: THEME_COLORS.border } },
        bottom: { style: 'thin', color: { argb: THEME_COLORS.border } },
        right: { style: 'thin', color: { argb: THEME_COLORS.border } }
      }
    })
  }

  // 9. 为列添加下拉框数据验证
  // 创建隐藏的选项数据工作表（用于超长选项列表）
  let optionsSheet: ExcelJS.Worksheet | null = null
  let optionsSheetColIndex = 1

  for (const [colIndex, options] of columnOptionsMap) {
    const colNum = colIndex + 1
    const startRow = includeExample ? 3 : 2
    const endRow = startRow + validationRowCount - 1

    // 计算选项字符串长度
    const optionsString = options.join(',')

    let formulae: string

    // Excel 数据验证字符串限制约 255 字符，超过则使用隐藏工作表
    if (optionsString.length <= 250) {
      // 短选项列表：直接使用字符串
      formulae = `"${optionsString}"`
    } else {
      // 长选项列表：使用隐藏工作表存储
      if (!optionsSheet) {
        optionsSheet = workbook.addWorksheet('_Options', {
          state: 'veryHidden' // 隐藏工作表
        })
      }

      // 将选项写入隐藏工作表的一列
      const colLetter = String.fromCharCode(64 + optionsSheetColIndex)
      options.forEach((opt, idx) => {
        optionsSheet!.getCell(idx + 1, optionsSheetColIndex).value = opt
      })

      // 使用工作表范围引用
      formulae = `'_Options'!$${colLetter}$1:$${colLetter}$${options.length}`
      optionsSheetColIndex++
    }

    // 为该列的每一行设置数据验证
    for (let row = startRow; row <= endRow; row++) {
      const cell = worksheet.getCell(row, colNum)
      cell.dataValidation = {
        type: 'list',
        allowBlank: true,
        formulae: [formulae],
        showErrorMessage: true,
        errorTitle: '输入错误',
        error: '请从下拉列表中选择有效值'
      }
    }
  }

  // 10. 处理级联下拉（如果有级联列和级联数据）
  const hasCascadeColumns = Object.keys(cascadeColumnMap).length > 0
  if (hasCascadeColumns && cascadeData) {
    const { setupCascadeDropdowns } = await import('./cascadeGenerator')

    // 构建级联配置
    const cascadeConfig = {
      workbook,
      worksheet,
      orgTree: cascadeData.orgTree,
      dormTree: cascadeData.dormTree,
      validationRowCount,
      includeExample
    }

    // 构建列映射
    const columnMapping = {
      campus: cascadeColumnMap['campus'] || 'N',
      dept: cascadeColumnMap['department'] || 'O',
      major: cascadeColumnMap['major'] || 'P',
      class: cascadeColumnMap['class'] || 'Q',
      floor: cascadeColumnMap['floor'] || 'R',
      room: cascadeColumnMap['room'] || 'S',
      bed: cascadeColumnMap['bed'] || 'T'
    }

    await setupCascadeDropdowns(cascadeConfig, columnMapping)
  }

  // 11. 设置自动筛选
  if (columns.length > 0) {
    const lastColLetter = getColumnLetter(columns.length)
    worksheet.autoFilter = {
      from: 'A1',
      to: `${lastColLetter}1`
    }
  }

  // 12. 生成文件并下载
  const buffer = await workbook.xlsx.writeBuffer()
  const blob = new Blob([buffer], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  })

  FileSaver.saveAs(blob, `${filename}.xlsx`)
}

/**
 * 获取列字母（1 -> A, 2 -> B, ... 27 -> AA）
 */
function getColumnLetter(colNum: number): string {
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
 * 创建模板列配置的工厂函数
 * @param config 列配置
 * @returns 完整的列配置
 */
export function createColumn(
  config: Partial<TemplateColumn> & { title: string; key: string }
): TemplateColumn {
  return {
    width: 12,
    required: false,
    ...config
  }
}

/**
 * 创建带下拉框的模板列
 * @param title 列标题
 * @param key 字段key
 * @param options 下拉选项或字典编码
 * @param extra 额外配置
 * @returns 列配置
 */
export function createDropdownColumn(
  title: string,
  key: string,
  options: string[] | { dictCode: string },
  extra?: Partial<Omit<TemplateColumn, 'title' | 'key' | 'dropdown'>>
): TemplateColumn {
  const dropdown: TemplateColumn['dropdown'] = Array.isArray(options)
    ? { options }
    : { dictCode: options.dictCode }

  return {
    title,
    key,
    dropdown,
    width: 12,
    required: false,
    ...extra
  }
}
