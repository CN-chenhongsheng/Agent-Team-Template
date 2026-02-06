/**
 * 统一的加载状态管理 Composable
 * 提供简洁的 API 来管理异步操作的加载状态
 */

import { ref } from 'vue';

/**
 * 使用加载状态
 * @returns 加载状态和相关方法
 */
export function useLoading() {
  const loading = ref(false);

  /**
   * 包装异步函数，自动管理加载状态
   * @param fn 异步函数
   * @returns 执行结果
   */
  const withLoading = async <T>(fn: () => Promise<T>): Promise<T> => {
    loading.value = true;
    try {
      return await fn();
    }
    finally {
      loading.value = false;
    }
  };

  /**
   * 手动设置加载状态
   * @param value 加载状态
   */
  const setLoading = (value: boolean): void => {
    loading.value = value;
  };

  return {
    loading,
    withLoading,
    setLoading,
  };
}
