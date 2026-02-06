/**
 * 通用字段校验器
 *
 * 提供常用的字段校验函数，可在多个导入场景中复用。
 *
 * ## 使用方式
 *
 * ```typescript
 * import { validatePhone, validateEmail, validateIdCard } from '@/utils/excel/validators/baseValidators'
 *
 * // 在 Worker 中使用
 * const phoneError = validatePhone(value)
 * if (phoneError) {
 *   errors.push({ row, column: 'phone', message: phoneError, value })
 * }
 * ```
 *
 * @module utils/excel/validators/baseValidators
 * @author 陈鸿昇
 */

/**
 * 手机号校验
 * @param value 待校验值
 * @returns null（通过） | string（错误信息）
 */
export const validatePhone = (value: string): string | null => {
  if (!value) return null

  // 移除空格和短横线
  const cleaned = value.replace(/[\s-]/g, '')

  // 11位数字，1开头
  const phoneRegex = /^1[3-9]\d{9}$/
  if (!phoneRegex.test(cleaned)) {
    return '手机号格式不正确（需11位数字，1开头）'
  }

  return null
}

/**
 * 邮箱校验
 * @param value 待校验值
 * @returns null（通过） | string（错误信息）
 */
export const validateEmail = (value: string): string | null => {
  if (!value) return null

  // 基础邮箱格式校验
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(value)) {
    return '邮箱格式不正确'
  }

  // 长度校验
  if (value.length > 100) {
    return '邮箱长度不能超过100个字符'
  }

  return null
}

/**
 * 身份证号校验
 * @param value 待校验值
 * @returns null（通过） | string（错误信息）
 */
export const validateIdCard = (value: string): string | null => {
  if (!value) return null

  // 移除空格
  const cleaned = value.replace(/\s/g, '')

  // 18位身份证校验
  if (cleaned.length !== 18) {
    return '身份证号应为18位'
  }

  // 前17位必须是数字
  if (!/^\d{17}[\dXx]$/.test(cleaned)) {
    return '身份证号格式不正确'
  }

  // 校验码验证
  const weights = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2]
  const checkCodes = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2']

  let sum = 0
  for (let i = 0; i < 17; i++) {
    sum += parseInt(cleaned[i]) * weights[i]
  }

  const checkCode = checkCodes[sum % 11]
  const lastChar = cleaned[17].toUpperCase()

  if (checkCode !== lastChar) {
    return '身份证号校验码不正确'
  }

  // 验证出生日期
  const birthYear = parseInt(cleaned.substring(6, 10))
  const birthMonth = parseInt(cleaned.substring(10, 12))
  const birthDay = parseInt(cleaned.substring(12, 14))

  if (birthYear < 1900 || birthYear > new Date().getFullYear()) {
    return '身份证号出生年份不合理'
  }

  if (birthMonth < 1 || birthMonth > 12) {
    return '身份证号出生月份不合理'
  }

  if (birthDay < 1 || birthDay > 31) {
    return '身份证号出生日期不合理'
  }

  return null
}

/**
 * 日期校验 (YYYY-MM-DD)
 * @param value 待校验值
 * @returns null（通过） | string（错误信息）
 */
export const validateDate = (value: string): string | null => {
  if (!value) return null

  // 格式校验
  const dateRegex = /^\d{4}-\d{2}-\d{2}$/
  if (!dateRegex.test(value)) {
    return '日期格式不正确（应为 YYYY-MM-DD）'
  }

  // 解析日期
  const [year, month, day] = value.split('-').map(Number)

  // 范围校验
  if (year < 1900 || year > 2100) {
    return '年份超出有效范围（1900-2100）'
  }

  if (month < 1 || month > 12) {
    return '月份超出有效范围（1-12）'
  }

  if (day < 1 || day > 31) {
    return '日期超出有效范围（1-31）'
  }

  // 验证日期有效性（闰年、月份天数等）
  const date = new Date(value)
  if (isNaN(date.getTime())) {
    return '日期无效'
  }

  // 验证解析后的日期是否与输入一致（避免 2月30日 被自动修正为 3月2日）
  if (
    date.getFullYear() !== year ||
    date.getMonth() + 1 !== month ||
    date.getDate() !== day
  ) {
    return '日期无效'
  }

  return null
}

/**
 * 年份校验 (YYYY)
 * @param value 待校验值
 * @returns null（通过） | string（错误信息）
 */
export const validateYear = (value: string): string | null => {
  if (!value) return null

  // 格式校验
  const yearRegex = /^\d{4}$/
  if (!yearRegex.test(value)) {
    return '年份格式不正确（应为 YYYY）'
  }

  const year = parseInt(value)

  // 范围校验
  if (year < 1900 || year > 2100) {
    return '年份超出有效范围（1900-2100）'
  }

  return null
}

/**
 * 数字校验
 * @param value 待校验值
 * @param options 校验选项
 * @returns null（通过） | string（错误信息）
 */
export const validateNumber = (
  value: string,
  options?: {
    min?: number
    max?: number
    integer?: boolean
  }
): string | null => {
  if (!value) return null

  const num = Number(value)

  // 数字格式校验
  if (isNaN(num)) {
    return '必须是有效的数字'
  }

  // 整数校验
  if (options?.integer && !Number.isInteger(num)) {
    return '必须是整数'
  }

  // 范围校验
  if (options?.min !== undefined && num < options.min) {
    return `数字不能小于 ${options.min}`
  }

  if (options?.max !== undefined && num > options.max) {
    return `数字不能大于 ${options.max}`
  }

  return null
}

/**
 * 长度校验
 * @param value 待校验值
 * @param options 校验选项
 * @returns null（通过） | string（错误信息）
 */
export const validateLength = (
  value: string,
  options?: {
    min?: number
    max?: number
    exact?: number
  }
): string | null => {
  if (!value) return null

  const length = value.length

  // 精确长度校验
  if (options?.exact !== undefined && length !== options.exact) {
    return `长度必须为 ${options.exact} 个字符`
  }

  // 最小长度校验
  if (options?.min !== undefined && length < options.min) {
    return `长度不能少于 ${options.min} 个字符`
  }

  // 最大长度校验
  if (options?.max !== undefined && length > options.max) {
    return `长度不能超过 ${options.max} 个字符`
  }

  return null
}

/**
 * 正则校验
 * @param value 待校验值
 * @param pattern 正则表达式
 * @param errorMessage 错误信息
 * @returns null（通过） | string（错误信息）
 */
export const validatePattern = (
  value: string,
  pattern: RegExp,
  errorMessage: string
): string | null => {
  if (!value) return null

  if (!pattern.test(value)) {
    return errorMessage
  }

  return null
}

/**
 * 枚举值校验
 * @param value 待校验值
 * @param allowedValues 允许的值列表
 * @param fieldName 字段名称（用于错误信息）
 * @returns null（通过） | string（错误信息）
 */
export const validateEnum = (
  value: string,
  allowedValues: string[],
  fieldName = '该字段'
): string | null => {
  if (!value) return null

  if (!allowedValues.includes(value)) {
    return `${fieldName}的值必须是以下之一：${allowedValues.join('、')}`
  }

  return null
}

/**
 * URL 校验
 * @param value 待校验值
 * @returns null（通过） | string（错误信息）
 */
export const validateUrl = (value: string): string | null => {
  if (!value) return null

  try {
    const url = new URL(value)
    if (!['http:', 'https:'].includes(url.protocol)) {
      return 'URL 必须以 http:// 或 https:// 开头'
    }
    return null
  } catch {
    return 'URL 格式不正确'
  }
}

/**
 * 中文姓名校验
 * @param value 待校验值
 * @returns null（通过） | string（错误信息）
 */
export const validateChineseName = (value: string): string | null => {
  if (!value) return null

  // 2-20个中文字符（支持少数民族姓名中的·）
  const nameRegex = /^[\u4e00-\u9fa5·]{2,20}$/
  if (!nameRegex.test(value)) {
    return '姓名格式不正确（2-20个中文字符）'
  }

  return null
}
