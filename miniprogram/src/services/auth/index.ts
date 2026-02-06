/**
 * 认证业务逻辑封装
 */

import {
  changePasswordAPI,
  getUserProfileAPI,
  loginAPI,
  logoutAPI,
  refreshTokenAPI,
  studentLoginAPI,
  updateUserProfileAPI,
  wxLoginAPI,
} from '@/api';
import type { ILoginParams } from '@/types';
import type { IChangePasswordParams, IUpdateProfileParams } from '@/types/api';

export class AuthService {
  static login(data: ILoginParams) {
    return loginAPI(data);
  }

  static studentLogin(data: { studentNo: string; password: string }) {
    return studentLoginAPI(data);
  }

  static wxLogin(code: string) {
    return wxLoginAPI(code);
  }

  static logout() {
    return logoutAPI();
  }

  static refreshToken(refreshToken: string) {
    return refreshTokenAPI(refreshToken);
  }

  static getProfile() {
    return getUserProfileAPI();
  }

  static updateProfile(data: IUpdateProfileParams) {
    return updateUserProfileAPI(data);
  }

  static changePassword(data: IChangePasswordParams) {
    return changePasswordAPI(data);
  }
}
