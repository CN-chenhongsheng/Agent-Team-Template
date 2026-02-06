/**
 * 统一提交流程组合式函数
 * 封装 loading -> 提交 -> toast -> 返回 的通用逻辑
 */

import { ref } from 'vue';
import { USE_MOCK } from '@/mock';

export interface SubmitOptions<T = unknown> {
  /** 提交前的校验函数，返回 false 则中止提交 */
  validate?: () => boolean | Promise<boolean>;
  /** 实际提交的 API 函数 */
  submitFn: () => Promise<T>;
  /** 提交成功后的回调 */
  onSuccess?: (result: T) => void;
  /** 提交失败后的回调 */
  onError?: (error: Error) => void;
  /** 加载提示文字 */
  loadingText?: string;
  /** 成功提示文字 */
  successText?: string;
  /** 失败提示文字 */
  errorText?: string;
  /** 成功后是否自动返回上一页 */
  autoBack?: boolean;
  /** 返回延迟时间（毫秒） */
  backDelay?: number;
  /** Mock 模式下的延迟时间（毫秒） */
  mockDelay?: number;
}

export function useSubmit() {
  const submitting = ref(false);

  /**
   * 执行提交流程
   */
  async function doSubmit<T = unknown>(options: SubmitOptions<T>): Promise<boolean> {
    const {
      validate,
      submitFn,
      onSuccess,
      onError,
      loadingText = '提交中...',
      successText = '提交成功',
      errorText = '提交失败',
      autoBack = true,
      backDelay = 1500,
      mockDelay = 1000,
    } = options;

    // 防止重复提交
    if (submitting.value) {
      return false;
    }

    try {
      // 校验
      if (validate) {
        const isValid = await validate();
        if (!isValid) {
          return false;
        }
      }

      submitting.value = true;
      uni.showLoading({ title: loadingText });

      let result: T;

      if (USE_MOCK) {
        // Mock 模式：模拟延迟
        await new Promise(resolve => setTimeout(resolve, mockDelay));
        result = undefined as T;
      }
      else {
        // 真实提交
        result = await submitFn();
      }

      uni.hideLoading();
      uni.showToast({ title: successText, icon: 'success' });

      // 成功回调
      onSuccess?.(result);

      // 自动返回
      if (autoBack) {
        setTimeout(() => {
          uni.navigateBack();
        }, backDelay);
      }

      return true;
    }
    catch (error: any) {
      uni.hideLoading();
      console.error('提交失败:', error);

      const message = error?.message || errorText;
      uni.showToast({ title: message, icon: 'none' });

      // 失败回调
      onError?.(error);

      return false;
    }
    finally {
      submitting.value = false;
    }
  }

  /**
   * 简化版提交（适用于表单 ref 校验场景）
   */
  async function submitWithFormRef<T = unknown>(
    formRef: { value?: { validate: () => Promise<void> } },
    submitFn: () => Promise<T>,
    options?: Partial<Omit<SubmitOptions<T>, 'validate' | 'submitFn'>>,
  ): Promise<boolean> {
    return doSubmit({
      validate: async () => {
        if (formRef.value?.validate) {
          await formRef.value.validate();
        }
        return true;
      },
      submitFn,
      ...options,
    });
  }

  return {
    submitting,
    doSubmit,
    submitWithFormRef,
  };
}
