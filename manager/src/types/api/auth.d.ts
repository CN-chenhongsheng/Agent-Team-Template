/**
 * API 认证类型定义
 *
 * 提供登录、用户信息、角色用户、密码修改等认证相关的类型定义
 *
 * @module types/api/auth
 * @author 陈鸿昇
 */

declare namespace Api {
  /** 认证类型 */
  namespace Auth {
    /** 登录参数 */
    interface LoginParams {
      username: string
      password: string
    }

    /** 登录响应 */
    interface LoginResponse {
      token: string
      userId: number
      username: string
      nickname: string
      avatar?: string
    }

    /** 用户信息 */
    interface UserInfo {
      userId: number
      username: string
      nickname: string
      avatar?: string
      email?: string
      phone?: string
      manageScope?: string
      roles: string[]
      permissions: string[]
    }

    /** 用户个人信息（详细） */
    interface UserProfile {
      id: number
      username: string
      nickname: string
      avatar?: string
      email?: string
      phone?: string
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

    /** 更新个人信息参数 */
    interface UserProfileUpdateParams {
      nickname?: string
      avatar?: string
      email?: string
      phone?: string
      gender?: number
      address?: string
      introduction?: string
    }

    /** 修改密码参数 */
    interface ChangePasswordParams {
      oldPassword: string
      newPassword: string
      confirmPassword: string
    }
  }
}
