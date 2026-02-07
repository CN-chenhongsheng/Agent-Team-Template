export interface AppState {
  /** 系统信息 */
  systemInfo: UniApp.GetSystemInfoResult;
  /** 状态栏高度 (px) */
  statusBarHeight: number;
  /** 导航栏高度 (px) */
  navBarHeight: number;
  /** 屏幕宽度 (px) */
  windowWidth: number;
  /** 屏幕高度 (px) */
  windowHeight: number;
  /** 网络类型 */
  networkType: string;
  /** 是否有网络连接 */
  isConnected: boolean;
}
