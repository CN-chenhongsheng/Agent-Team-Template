/**
 * 级联命名规则工具
 *
 * 与 `cascadeGenerator.ts` 保持一致的 sanitize/buildHierarchyName 逻辑，
 * 用于模板生成与导入校验共享，避免规则漂移。
 *
 * @module utils/excel/importValidation/cascadeName
 * @author 陈鸿昇
 */

/**
 * 将名称转换为合法的 Excel 命名范围名称/级联单元格值
 *
 * 规则（与 `cascadeGenerator.ts` 对齐）：
 * - 只能包含：字母、数字、下划线、中文
 * - 空格/特殊字符替换为下划线
 * - 若以数字开头，则追加指定前缀（如 F/R/B/C）
 * - 长度截断（避免 Excel 255 限制边界问题）
 */
export function sanitizeName(name: string, prefix?: string): string {
  if (!name) return prefix ? prefix + '_空' : '空'

  // 替换特殊字符为下划线
  let sanitized = name
    .replace(/[\s/\\:;,.!@#$%^&*()+=\u005B\u005D{}|<>?'"-]/g, '_')
    .replace(/_+/g, '_')
    .replace(/^_+|_+$/g, '')

  // 如果以数字开头，添加指定前缀
  if (!/^[a-zA-Z_\u4e00-\u9fa5]/.test(sanitized)) {
    sanitized = (prefix || 'N') + sanitized
  }

  // 限制长度
  if (sanitized.length > 200) {
    sanitized = sanitized.substring(0, 200)
  }

  return sanitized || (prefix ? prefix + '_空' : '空')
}

/**
 * 构建层级名称，用于确保父子关系唯一性
 * 格式：父级名称_当前名称（当前名称会按 prefix 规则 sanitize）
 */
export function buildHierarchyName(
  parentName: string,
  currentName: string,
  prefix: string
): string {
  const sanitizedCurrent = sanitizeName(currentName, prefix)
  return parentName + '_' + sanitizedCurrent
}
