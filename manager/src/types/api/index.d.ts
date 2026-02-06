/**
 * API 类型定义汇总文件
 *
 * 此文件作为所有 API 类型的入口，确保所有模块的类型定义都被正确加载
 * TypeScript 会自动加载所有 .d.ts 文件，但为了清晰的模块组织，我们在这里显式引用
 *
 * ## 模块结构
 *
 * - common.d.ts - 通用类型（分页、审批、学生基本信息等）
 * - auth.d.ts - 认证类型（登录、用户信息等）
 * - system/ - 系统管理类型
 *   - user.d.ts - 用户管理
 *   - role.d.ts - 角色管理
 *   - menu.d.ts - 菜单管理
 *   - dict.d.ts - 字典管理
 *   - notice.d.ts - 通知管理
 *   - school.d.ts - 学校管理（校区、院系、专业、班级、学年）
 *   - dormitory.d.ts - 宿舍管理（楼层、房间、床位）
 *   - log.d.ts - 日志管理
 * - accommodation/ - 住宿管理（学生、入住、调宿、退宿、留宿）
 *   - index.d.ts
 * - repair/ - 报修管理
 *   - index.d.ts
 * - allocation/ - 分配管理（配置、任务、结果、问卷）
 *   - index.d.ts
 * - approval/ - 审批管理（流程、实例、记录）
 *   - index.d.ts
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
 *
 * // 使用住宿管理类型
 * const student: Api.AccommodationManage.StudentListItem = { ... }
 *
 * // 使用报修管理类型
 * const repair: Api.RepairManage.RepairListItem = { ... }
 *
 * // 使用分配管理类型
 * const config: Api.Allocation.ConfigListItem = { ... }
 *
 * // 使用审批管理类型
 * const flow: Api.ApprovalManage.ApprovalFlow = { ... }
 * ```
 *
 * @module types/api
 * @author 陈鸿昇
 */

/// <reference path="./common.d.ts" />
/// <reference path="./auth.d.ts" />
/// <reference path="./system/user.d.ts" />
/// <reference path="./system/role.d.ts" />
/// <reference path="./system/menu.d.ts" />
/// <reference path="./system/dict.d.ts" />
/// <reference path="./system/notice.d.ts" />
/// <reference path="./system/school.d.ts" />
/// <reference path="./system/dormitory.d.ts" />
/// <reference path="./system/log.d.ts" />
/// <reference path="./accommodation/index.d.ts" />
/// <reference path="./repair/index.d.ts" />
/// <reference path="./allocation/index.d.ts" />
/// <reference path="./approval/index.d.ts" />

/**
 * 空的 export 语句，确保这个文件被视为模块
 * 这样可以防止全局命名空间污染
 */
export {}
