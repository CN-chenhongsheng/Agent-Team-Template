/**
 * 认证授权API
 * @module api/auth
 */

import { post } from '@/utils/request';
import type { ILoginParams } from '@/types';
import type {
  IBackendLoginResponse,
  IChangePasswordParams,
  IUpdateProfileParams,
} from '@/types/api';

/**
 * 用户登录
 */
export function loginAPI(data: ILoginParams) {
  return post<IBackendLoginResponse>({
    url: '/api/v1/auth/login',
    data,
  });
}

/**
 * 学生登录（学号+密码）
 */
export function studentLoginAPI(data: { studentNo: string; password: string }) {
  return post<IBackendLoginResponse>({
    url: '/api/v1/auth/student/login',
    data,
  });
}

/**
 * 用户登出
 */
export function logoutAPI() {
  return post({
    url: '/api/v1/auth/logout',
  });
}

/**
 * 刷新token
 * 注意：refresh token 通过 HttpOnly Cookie 自动发送，无需传参
 * 后端返回新的 access token（字符串）
 */
export function refreshTokenAPI() {
  return post<string>({
    url: '/api/v1/auth/refresh',
    data: {},
    custom: {
      auth: false, // 刷新接口不添加 Authorization 头（防止循环）
      skipErrorHandler: true, // 跳过统一错误处理（由拦截器处理）
    },
  });
}

/**
 * 微信小程序登录
 */
export function wxLoginAPI(code: string) {
  return post<IBackendLoginResponse>({
    url: '/api/v1/auth/wx-login',
    data: { code },
  });
}

/**
 * 获取当前用户详情
 */
export function getUserProfileAPI() {
  return post<IBackendLoginResponse>({
    url: '/api/v1/auth/user/profile',
  });
}

/**
 * 更新用户个人信息
 */
export function updateUserProfileAPI(data: IUpdateProfileParams) {
  return post<{ success: boolean }>({
    url: '/api/v1/auth/user/profile/update',
    data,
  });
}

/**
 * 修改密码
 */
export function changePasswordAPI(data: IChangePasswordParams) {
  return post<{ success: boolean }>({
    url: '/api/v1/auth/change-password',
    data,
  });
}
