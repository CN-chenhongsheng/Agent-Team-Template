import type { App } from 'vue';
import { createPinia } from 'pinia';
import { createPersistedState } from 'pinia-plugin-persistedstate';

// 导入子模块
import useAppStore from './modules/app';
import useUserStore from './modules/user';

// 安装pinia状态管理插件
function setupStore(app: App) {
  const store = createPinia();

  // 配置持久化插件
  store.use(
    createPersistedState({
      storage: {
        getItem: (key: string) => uni.getStorageSync(key),
        setItem: (key: string, value: string) => uni.setStorageSync(key, value),
      },
    }),
  );

  app.use(store);
}

// 导出模块
export { useAppStore, useUserStore };
export default setupStore;
