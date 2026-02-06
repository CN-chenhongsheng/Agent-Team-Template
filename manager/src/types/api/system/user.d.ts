/**
 * API 系统管理 - 用户管理类型定义
 *
 * @module types/api/system/user
 * @author 陈鸿昇
 */

declare namespace Api {
  namespace SystemManage {
    /** ==================== 用户管理 ==================== */
    /** 用户查询参数 */
    interface UserSearchParams {
      username?: string
      nickname?: string
      phone?: string
      manageScope?: string
      status?: number
      pageNum?: number
      pageSize?: number
    }

    /** 用户保存参数 */
    interface UserSaveParams {
      id?: number
      username: string
      password?: string
      nickname: string
      avatar?: string
      email?: string
      phone?: string
      gender?: number
      manageScope?: string
      status?: number
      roleIds?: number[]
    }

    /** 用户重置密码参数 */
    interface UserResetPasswordParams {
      newPassword: string
    }

    /** 用户分页响应 */
    interface UserPageResponse extends Api.Common.PaginatedResponse<UserListItem> {
      records: UserListItem[]
      current: number
      size: number
      total: number
    }

    /** 用户列表 */
    type UserList = UserPageResponse

    /** 用户列表项 */
    interface UserListItem {
      id: number
      username: string
      nickname: string
      avatar?: string
      email?: string
      phone?: string
      gender?: number
      genderText?: string
      manageScope?: string
      status: number
      statusText?: string
      gender?: number
      genderText?: string
      address?: string
      introduction?: string
      cpUserId?: string
      openid?: string
      roleIds?: number[]
      roleNames?: string[]
      createTime?: string
      updateTime?: string
      lastLoginTime?: string
      isOnline?: boolean
      // 兼容旧字段
      userName?: string
      userGender?: string
      userEmail?: string
      userPhone?: string
    }

    /** 用户简单信息 */
    interface UserSimpleItem {
      id: number
      username: string
      nickname: string
      phone?: string
      email?: string
    }

    /** 角色用户查询参数 */
    interface RoleUserQueryParams {
      roleCodes: string[]
    }

    /** 角色用户信息（Map格式，key为角色代码，value为用户列表） */
    type RoleUserMap = Record<string, UserSimpleItem[]>
  }
}
