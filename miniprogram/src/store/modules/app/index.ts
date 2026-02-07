import { defineStore } from 'pinia';
import type { AppState } from './types';

const useAppStore = defineStore('app', {
  state: (): AppState => ({
    systemInfo: {} as UniApp.GetSystemInfoResult,
    statusBarHeight: 0,
    navBarHeight: 44,
    windowWidth: 375,
    windowHeight: 667,
    networkType: 'unknown',
    isConnected: true,
  }),

  getters: {
    getSystemInfo(): UniApp.GetSystemInfoResult {
      return this.systemInfo;
    },
    /** 自定义导航栏总高度 = 状态栏 + 导航栏 */
    totalNavHeight(): number {
      return this.statusBarHeight + this.navBarHeight;
    },
  },

  actions: {
    setSystemInfo(info: UniApp.GetSystemInfoResult) {
      this.systemInfo = info;
    },

    /**
     * 初始化系统信息（同步获取，确保可用）
     */
    initSystemInfo() {
      try {
        const info = uni.getSystemInfoSync();
        this.setSystemInfo(info);
        this.statusBarHeight = info.statusBarHeight || 0;
        this.windowWidth = info.windowWidth || 375;
        this.windowHeight = info.windowHeight || 667;

        // 计算导航栏高度（微信小程序使用胶囊按钮定位）
        // #ifdef MP-WEIXIN
        const menuButtonInfo = uni.getMenuButtonBoundingClientRect();
        this.navBarHeight = (menuButtonInfo.top - (info.statusBarHeight || 0)) * 2 + menuButtonInfo.height;
        // #endif
      }
      catch (err) {
        console.error('[AppStore] 获取系统信息失败:', err);
      }
    },

    /**
     * 监听网络状态变化
     */
    initNetworkListener() {
      // 获取当前网络状态
      uni.getNetworkType({
        success: (res) => {
          this.networkType = res.networkType;
          this.isConnected = res.networkType !== 'none';
        },
      });

      // 监听网络变化
      uni.onNetworkStatusChange((res) => {
        this.networkType = res.networkType;
        this.isConnected = res.isConnected;

        if (!res.isConnected) {
          uni.showToast({ title: '网络已断开', icon: 'none' });
        }
      });
    },

    /**
     * 检查小程序更新
     */
    checkUpdate() {
      // #ifdef MP-WEIXIN
      const updateManager = uni.getUpdateManager();
      updateManager.onCheckForUpdate((res: UniApp.OnCheckForUpdateResult) => {
        console.log('[AppStore] 是否有新版本:', res.hasUpdate);
      });
      updateManager.onUpdateReady(() => {
        uni.showModal({
          title: '更新提示',
          content: '新版本已经准备好，是否重启应用?',
          success(res) {
            if (res.confirm) {
              updateManager.applyUpdate();
            }
          },
        });
      });
      updateManager.onUpdateFailed((res: any) => {
        console.error('[AppStore] 更新失败:', res);
        uni.showToast({ title: '更新失败', icon: 'error' });
      });
      // #endif
    },
  },
});

export default useAppStore;
