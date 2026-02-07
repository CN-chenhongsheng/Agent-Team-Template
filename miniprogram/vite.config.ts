import { resolve } from 'node:path';
import { defineConfig } from 'vite';
import type { UserConfig } from 'vite';
import createVitePlugins from './build/vite/plugins';
import proxy from './build/vite/proxy';

// https://vitejs.dev/config/
export default defineConfig((): UserConfig => {
  const isBuild = process.env.NODE_ENV === 'production';
  return {
    resolve: {
      // https://cn.vitejs.dev/config/#resolve-alias
      alias: {
        // 设置别名
        '@': resolve(__dirname, './src'),
      },
    },
    // vite 相关配置
    server: {
      port: 8080,
      host: true,
      open: true,
      proxy,
    },
    plugins: createVitePlugins(isBuild),
    // SCSS 全局变量和 mixins 自动注入，无需每个文件手动 @import
    css: {
      preprocessorOptions: {
        scss: {
          additionalData: `@import "@/styles/variables.scss"; @import "@/styles/mixins.scss";`,
        },
      },
    },
  };
});
