/**
 * 认证业务逻辑封装
 * Mock 判断统一在此层处理，Store 只关注状态管理
 */

import {
  changePasswordAPI,
  getUserInfoAPI,
  getUserProfileAPI,
  loginAPI,
  logoutAPI,
  refreshTokenAPI,
  studentLoginAPI,
  updateUserProfileAPI,
  wxLoginAPI,
} from '@/api';
import type { ILoginParams, IUser, UserRole } from '@/types';
import type { IBackendLoginResponse, IChangePasswordParams, IUpdateProfileParams } from '@/types/api';
import { USE_MOCK, getMockUserByRole, mockLoginResponse, mockResponse } from '@/mock';
import { UserRole as UserRoleEnum } from '@/types';

export interface LoginResult {
  accessToken: string;
  refreshToken: string;
  userInfo: IUser;
}

/**
 * 将后端登录响应转换为前端标准格式
 */
function transformLoginResponse(res: IBackendLoginResponse): LoginResult {
  if (!res || !res.token) {
    throw new Error('登录失败：服务器返回数据无效');
  }
  return {
    accessToken: res.token,
    refreshToken: '',
    userInfo: {
      id: res.userId,
      username: res.username,
      nickname: res.nickname,
      avatar: res.avatar,
      role: res.role as UserRole,
      studentInfo: res.studentInfo,
    },
  };
}

export class AuthService {
  /**
   * 用户名密码登录
   */
  static async login(data: ILoginParams): Promise<LoginResult> {
    if (USE_MOCK) {
      return mockResponse(mockLoginResponse);
    }
    const res = await loginAPI(data);
    return transformLoginResponse(res);
  }

  /**
   * 学生登录（学号+密码）
   */
  static async studentLogin(data: { studentNo: string; password: string }): Promise<LoginResult> {
    if (USE_MOCK) {
      return mockResponse(mockLoginResponse);
    }
    const res = await studentLoginAPI(data);
    return transformLoginResponse(res);
  }

  /**
   * 微信登录
   */
  static async wxLogin(code: string): Promise<LoginResult> {
    if (USE_MOCK) {
      return mockResponse(mockLoginResponse);
    }
    const res = await wxLoginAPI(code);
    return transformLoginResponse(res);
  }

  /**
   * 获取用户信息
   */
  static async getUserInfo(): Promise<IUser> {
    if (USE_MOCK) {
      return mockResponse(getMockUserByRole(UserRoleEnum.STUDENT));
    }
    return getUserInfoAPI();
  }

  /**
   * 登出
   */
  static async logout(): Promise<void> {
    if (!USE_MOCK) {
      await logoutAPI();
    }
  }

  /**
   * 刷新 Token
   */
  static refreshToken(refreshToken: string) {
    return refreshTokenAPI(refreshToken);
  }

  /**
   * 获取用户资料
   */
  static getProfile() {
    return getUserProfileAPI();
  }

  /**
   * 更新用户资料
   */
  static updateProfile(data: IUpdateProfileParams) {
    return updateUserProfileAPI(data);
  }

  /**
   * 修改密码
   */
  static changePassword(data: IChangePasswordParams) {
    return changePasswordAPI(data);
  }
}
