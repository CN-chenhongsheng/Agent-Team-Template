import type { IUser } from '@/types';

export type RoleType = '' | '*' | 'user';

export interface UserState {
  /** 用户信息 */
  userInfo: IUser | null;
  /** 访问令牌 */
  token: string;
  /** 刷新令牌 */
  refreshToken: string;
  /** 是否已登录 */
  isLoggedIn: boolean;
}

export type providerType =
  | 'weixin'
  | 'qq'
  | 'sinaweibo'
  | 'xiaomi'
  | 'apple'
  | 'univerify'
  | undefined;
