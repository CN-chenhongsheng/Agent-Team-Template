/**
 * 认证模块 API
 * 包含登录、登出、Token 刷新、用户信息等接口
 *
 * @module api/auth
 * @author 陈鸿昇
 * @date 2025-12-30
 */

import request from '@/utils/http'

/**
 * 用户登录
 * @param params 登录参数
 * @returns 登录响应
 */
export function fetchLogin(params: Api.Auth.LoginParams) {
  return request.post<Api.Auth.LoginResponse>({
    url: '/api/v1/auth/login',
    data: params
  })
}

/**
 * 刷新 Access Token
 * Refresh Token 自动从 HttpOnly Cookie 中发送
 * @returns 新的 Access Token
 */
export function fetchRefreshToken() {
  return request.post<string>({
    url: '/api/v1/auth/refresh'
    // 刷新接口不需要 Authorization 头，Refresh Token 在 Cookie 中
    // 但需要确保 withCredentials 为 true（已在 axios 实例中配置）
  })
}

/**
 * 用户登出
 */
export function fetchLogout() {
  return request.post<void>({
    url: '/api/v1/auth/logout'
  })
}

/**
 * 获取当前用户信息
 * @returns 用户信息
 */
export function fetchGetUserInfo() {
  return request.get<Api.Auth.UserInfo>({
    url: '/api/v1/auth/userinfo'
  })
}

/** ==================== 个人中心 ==================== */

/**
 * 获取当前用户个人信息（详细）
 * @returns 用户详细信息
 */
export function fetchGetUserProfile() {
  return request.get<Api.Auth.UserProfile>({
    url: '/api/v1/user/profile'
  })
}

/**
 * 更新当前用户个人信息
 * @param data 个人信息
 */
export function fetchUpdateUserProfile(data: Api.Auth.UserProfileUpdateParams) {
  return request.put<void>({
    url: '/api/v1/user/profile',
    data,
    showSuccessMessage: true
  })
}

/**
 * 修改当前用户密码
 * @param data 密码信息
 */
export function fetchChangePassword(data: Api.Auth.ChangePasswordParams) {
  return request.put<void>({
    url: '/api/v1/user/profile/password',
    data,
    showSuccessMessage: true
  })
}
