/**
 * Excel 工具模块
 *
 * 统一导出 Excel 相关的工具函数和类型
 *
 * @module utils/excel
 * @author 陈鸿昇
 */

// 导出模板生成器
export {
  generateTemplate,
  createColumn,
  createDropdownColumn,
  type TemplateColumn,
  type TemplateConfig,
  type CascadeData
} from './templateGenerator'

// 导出通用字段配置
export {
  COMMON_FIELDS,
  getCommonField,
  getCommonFields,
  extendCommonField
} from './commonFields'

// 导出级联下拉生成器
export {
  setupCascadeDropdowns,
  setupOrgCascade,
  setupDormCascade,
  type CascadeConfig
} from './cascadeGenerator'

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
