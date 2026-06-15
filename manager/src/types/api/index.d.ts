/**
 * API 类型定义汇总文件
 *
 * 此文件作为所有 API 类型的入口，确保所有模块的类型定义都被正确加载
 * TypeScript 会自动加载所有 .d.ts 文件，但为了清晰的模块组织，我们在这里显式引用
 *
 * ## 模块结构
 *
 * - common.d.ts - 通用类型（分页等）
 * - auth.d.ts - 认证类型（登录、用户信息等）
 * - system/ - 系统管理类型
 *   - user.d.ts - 用户管理
 *   - role.d.ts - 角色管理
 *   - menu.d.ts - 菜单管理
 *   - dict.d.ts - 字典管理
 *   - log.d.ts - 日志管理
 *
 * ## 使用方式
 *
 * 所有类型都在全局 Api 命名空间下，无需导入即可使用：
 *
 * ```typescript
 * // 使用通用类型
 * const params: Api.Common.PaginationParams = { current: 1, size: 10, total: 0 }
 *
 * // 使用认证类型
 * const loginData: Api.Auth.LoginParams = { username: 'admin', password: '123456' }
 *
 * // 使用系统管理类型
 * const user: Api.SystemManage.UserListItem = { ... }
 * ```
 *
 * @module types/api
 * @author 陈鸿昇
 */

/**
 * 空的 export 语句，确保这个文件被视为模块
 * 这样可以防止全局命名空间污染
 */
export {}
