/**
 * Excel 工具模块
 *
 * 统一导出 Excel 相关的工具函数和类型
 *
 * @module utils/excel
 * @author 陈鸿昇
 */

// 导出通用校验器
export {
  validatePhone,
  validateEmail,
  validateIdCard,
  validateDate,
  validateYear,
  validateNumber,
  validateLength,
  validatePattern,
  validateEnum,
  validateUrl,
  validateChineseName
} from './validators/baseValidators'

// 导出模板生成器
export { generateTemplate, type TemplateColumn, type CascadeData } from './templateGenerator'
