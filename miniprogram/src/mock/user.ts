/**
 * 用户Mock数据
 * @module mock/user
 */

import type { ILoginResponse, IUser } from '@/types';
import { UserRole } from '@/types';

/**
 * Mock学生用户
 */
export const mockStudentUser: IUser = {
  id: 1,
  username: '2021001',
  nickname: '张三',
  avatar: 'https://via.placeholder.com/150',
  phone: '13800138000',
  email: 'zhangsan@example.com',
  gender: 1,
  role: UserRole.STUDENT,
  roleName: '学生',
  status: 1,
};

/**
 * Mock宿管员用户
 */
export const mockDormManagerUser: IUser = {
  id: 2,
  username: 'manager01',
  nickname: '李管理',
  avatar: 'https://via.placeholder.com/150',
  phone: '13800138001',
  email: 'limanager@example.com',
  gender: 2,
  role: UserRole.DORM_MANAGER,
  roleName: '管理员',
  status: 1,
};

/**
 * Mock管理员用户
 */
export const mockAdminUser: IUser = {
  id: 3,
  username: 'admin',
  nickname: '系统管理员',
  avatar: 'https://via.placeholder.com/150',
  phone: '13800138002',
  email: 'admin@example.com',
  gender: 1,
  role: UserRole.ADMIN,
  roleName: '管理员',
  status: 1,
};

/**
 * Mock登录响应
 */
export const mockLoginResponse: ILoginResponse = {
  accessToken: `mock_access_token_${Date.now()}`,
  refreshToken: `mock_refresh_token_${Date.now()}`,
  userInfo: mockStudentUser,
  expiresIn: 7200,
};

/**
 * 根据角色获取Mock用户
 */
export function getMockUserByRole(role: UserRole): IUser {
  switch (role) {
    case UserRole.STUDENT:
      return mockStudentUser;
    case UserRole.DORM_MANAGER:
      return mockDormManagerUser;
    case UserRole.ADMIN:
      return mockAdminUser;
    default:
      return mockStudentUser;
  }
}
