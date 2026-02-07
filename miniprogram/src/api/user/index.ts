/**
 * 用户信息与用户 API
 * @module api/user
 */

import type { IUser, IUserPageResult, IUserQueryParams } from '@/types';
import { get, post } from '@/utils/request';

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
