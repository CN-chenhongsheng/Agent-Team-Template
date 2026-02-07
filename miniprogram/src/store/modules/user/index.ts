/**
 * 用户状态管理
 * @module store/modules/user
 *
 * 职责：纯状态管理，不包含 mock 判断
 * Mock 逻辑统一在 AuthService 层处理
 */

import { defineStore } from 'pinia';
import type { UserState, providerType } from './types';
import type { ILoginParams, IUser, UserRole } from '@/types';
import { AuthService } from '@/services';
import { clearToken, getToken, setToken } from '@/utils/auth';

const useUserStore = defineStore('user', {
  state: (): UserState => ({
    userInfo: null,
    token: getToken() || '',
    refreshToken: '',
    isLoggedIn: false,
  }),

  getters: {
    /** 用户角色 */
    userRole(state): UserRole | undefined {
      return state.userInfo?.role;
    },
    /** 是否是学生 */
    isStudent(state): boolean {
      return state.userInfo?.role === 'student';
    },
  },

  actions: {
    /** 设置用户信息 */
    setUserInfo(userInfo: IUser) {
      this.userInfo = userInfo;
      this.isLoggedIn = true;
    },

    /** 设置 Token */
    setToken(token: string, refreshToken?: string) {
      this.token = token;
      if (refreshToken) {
        this.refreshToken = refreshToken;
      }
      setToken(token);
    },

    /** 重置用户信息 */
    resetInfo() {
      this.userInfo = null;
      this.token = '';
      this.refreshToken = '';
      this.isLoggedIn = false;
      clearToken();
    },

    /**
     * 用户名密码登录
     */
    async login(loginForm: ILoginParams) {
      try {
        const result = await AuthService.login(loginForm);
        this.setToken(result.accessToken, result.refreshToken);
        this.setUserInfo(result.userInfo);
        return result;
      }
      catch (error) {
        console.error('登录失败:', error);
        throw error;
      }
    },

    /**
     * 学生登录（学号+密码）
     */
    async studentLogin(studentNo: string, password: string) {
      try {
        const result = await AuthService.studentLogin({ studentNo, password });
        this.setToken(result.accessToken, result.refreshToken);
        this.setUserInfo(result.userInfo);
        return result;
      }
      catch (error) {
        console.error('[Store] 学生登录失败:', error);
        throw error;
      }
    },

    /**
     * 微信小程序授权登录
     */
    async wxLogin(provider: providerType = 'weixin') {
      return new Promise((resolve, reject) => {
        uni.login({
          provider,
          success: async (res: UniApp.LoginRes) => {
            try {
              if (res.code) {
                const result = await AuthService.wxLogin(res.code);
                this.setToken(result.accessToken, result.refreshToken);
                this.setUserInfo(result.userInfo);
                resolve(result);
              }
              else {
                reject(new Error(res.errMsg));
              }
            }
            catch (error) {
              console.error('微信登录失败:', error);
              reject(error);
            }
          },
          fail: (err: any) => {
            console.error('微信登录失败:', err);
            reject(err);
          },
        });
      });
    },

    /**
     * 获取用户信息
     */
    async getUserInfo() {
      try {
        const userInfo = await AuthService.getUserInfo();
        this.setUserInfo(userInfo);
        return userInfo;
      }
      catch (error) {
        console.error('获取用户信息失败:', error);
        throw error;
      }
    },

    /**
     * 登出
     */
    async logout() {
      try {
        await AuthService.logout();
        this.resetInfo();
      }
      catch (error) {
        console.error('登出失败:', error);
        this.resetInfo();
        throw error;
      }
    },

    /**
     * 检查登录状态
     */
    async checkLoginStatus() {
      if (this.token && !this.userInfo) {
        try {
          await this.getUserInfo();
        }
        catch {
          this.resetInfo();
        }
      }
      return this.isLoggedIn;
    },
  },

  // 持久化配置
  persist: {
    key: 'user-store',
    paths: ['userInfo', 'token', 'refreshToken', 'isLoggedIn'],
  } as any,
});

export default useUserStore;
