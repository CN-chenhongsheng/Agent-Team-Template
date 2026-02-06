/**
 * 用户信息与用户 API
 * @module api/user
 */

import type { LoginByCodeParams, LoginByCodeResult, LoginParams, LoginResult, ProfileParams } from './types';
import type { IUser, IUserPageResult, IUserQueryParams } from '@/types';
import type { UserState } from '@/store/modules/user/types';
import type { CommonResult } from '@/api/common/types';
import { get, post } from '@/utils/request';

enum URL {
  login = '/user/login',
  loginByCode = '/user/loginByCode',
  logout = '/user/logout',
  profile = '/user/profile',
}

// ----- 原 user/index：登录、档案 -----
export const getUserProfile = (params?: ProfileParams) => get<UserState>({ url: URL.profile, params });
export const login = (data: LoginParams) => post<LoginResult>({ url: URL.login, data });
export const loginByCode = (data: LoginByCodeParams) => post<LoginByCodeResult>({ url: URL.loginByCode, data });
export const logout = () => post<CommonResult>({ url: URL.logout });

// ----- 原 user.ts：用户 CRUD -----
/** 获取当前用户信息 */
export function getUserInfoAPI() {
  return get<IUser>({
    url: '/api/v1/user/info',
  });
}

/** 更新用户信息 */
export function updateUserInfoAPI(data: Partial<IUser>) {
  return post({
    url: '/api/v1/user/update',
    data,
  });
}

/** 获取用户分页列表 */
export function getUserPageAPI(params: IUserQueryParams) {
  return get<IUserPageResult>({
    url: '/api/v1/user/page',
    data: params,
  });
}

/** 修改密码（用户模块） */
export function changePasswordByUserAPI(data: { oldPassword: string; newPassword: string }) {
  return post({
    url: '/api/v1/user/change-password',
    data,
  });
}
