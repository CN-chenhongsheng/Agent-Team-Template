/**
 * Excel 级联下拉生成器（最小化版本）
 *
 * 用户导入不需要级联功能，此文件仅为满足 templateGenerator.ts 的导入需求
 * 如需完整级联功能，请从 git 历史恢复
 */
import type ExcelJS from 'exceljs'

export interface CascadeConfig {
  workbook: ExcelJS.Workbook
  worksheet: ExcelJS.Worksheet
  orgTree?: any
  dormTree?: any
  validationRowCount: number
  includeExample: boolean
}

export async function setupCascadeDropdowns(
  _config: CascadeConfig,
  _columnMapping: Record<string, string>
): Promise<void> {
  // 用户导入不需要级联功能，空实现
  void _config
  void _columnMapping
  console.warn('级联下拉功能未实现，如需使用请从 git 恢复完整版本')
}
