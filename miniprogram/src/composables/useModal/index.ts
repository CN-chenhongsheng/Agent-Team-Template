/**
 * Dialog 提示框
 */
export function useModal() {
  const showModal = (content: string, options: UniApp.ShowModalOptions) => {
    // @ts-expect-error - UniNamespace.ShowModalSuccessCallbackResult 类型定义不完整，使用 UniApp 类型
    return new Promise<UniApp.ShowModalSuccessCallbackResult>((resolve, reject) => {
      uni.showModal({
        title: '温馨提示',
        content,
        showCancel: false,
        confirmColor: '#1677FF',
        success: res => resolve(res),
        fail: () => reject(new Error('Alert 调用失败 !')),
        ...options,
      });
    });
  };
  return {
    showModal,
  };
}
