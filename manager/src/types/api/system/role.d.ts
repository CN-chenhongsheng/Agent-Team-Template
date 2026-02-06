/**
 * API 系统管理 - 角色管理类型定义
 *
 * @module types/api/system/role
 * @author 陈鸿昇
 */

declare namespace Api {
  namespace SystemManage {
    /** ==================== 角色管理 ==================== */
    /** 角色查询参数 */
    interface RoleSearchParams {
      roleCode?: string
      roleName?: string
      status?: number
      pageNum?: number
      pageSize?: number
    }

    /** 角色保存参数 */
    interface RoleSaveParams {
      id?: number
      roleCode: string
      roleName: string
      sort?: number
      status?: number
      remark?: string
      menuIds?: number[]
    }

    /** 角色分页响应 */
    interface RolePageResponse {
      list: RoleListItem[]
      total: number
      pageNum: number
      pageSize: number
      totalPages: number
    }

    /** 角色列表 */
    type RoleList = RolePageResponse

    /** 角色权限项（包含菜单ID和状态） */
    interface RolePermissionItem {
      menuId: number
      status: number
    }

    /** 用户权限列表项 */
    interface UserPermissionItem {
      menuId: number
      status: number
    }

    /** 角色列表项 */
    interface RoleListItem {
      id: number
      roleCode: string
      roleName: string
      sort?: number
      status: number
      statusText?: string
      remark?: string
      menuIds?: number[]
      createTime?: string
      updateTime?: string
    }
  }
}
