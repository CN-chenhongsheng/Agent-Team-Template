/**
 * Mock数据统一导出
 * @module mock
 *
 * 说明：此文件用于开发阶段的Mock数据
 * 生产环境请将 VITE_USE_MOCK 设置为 false
 */

export * from './user';

/**
 * 是否启用Mock数据
 * 注意：已禁用 Mock，使用真实 API 接口
 */
export const USE_MOCK = false; // 强制禁用 Mock，使用真实接口
// export const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'; // 原配置（已注释）

/**
 * Mock延迟时间（毫秒）
 */
export const MOCK_DELAY = 500;

/**
 * Mock响应包装器
 */
export function mockResponse<T>(data: T, delay = MOCK_DELAY): Promise<T> {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve(data);
    }, delay);
  });
}
