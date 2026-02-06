/**
 * 认证授权相关类型定义
 * @module types/api/auth
 */

import type { IStudent } from '@/types';

/**
 * 后端登录响应（统一格式，对应后端 LoginVO）
 */
export interface IBackendLoginResponse {
  token: string;
  userId: number;
  username: string;
  nickname?: string;
  avatar?: string;
  role: string;
  studentInfo?: IStudent;
}

/**
 * 更新用户个人信息参数
 */
export interface IUpdateProfileParams {
  phone?: string;
  email?: string;
  emergencyContact?: string;
  emergencyPhone?: string;
  avatar?: string;
}

/**
 * 修改密码参数
 */
export interface IChangePasswordParams {
  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
}
