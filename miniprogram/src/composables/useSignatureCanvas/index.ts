/**
 * 签名 Canvas 管理组合式函数
 */

import type { ComponentInternalInstance, Ref } from 'vue';
import { nextTick, ref } from 'vue';

interface SignatureCanvasConfig {
  canvasWidth: number;
  canvasHeight: number;
}

export function useSignatureCanvas(
  config: SignatureCanvasConfig,
  instance?: ComponentInternalInstance | null,
  h5CanvasRef?: Ref<HTMLCanvasElement | null>,
) {
  let signatureCtx: any = null;
  const isDrawing = ref(false);
  /** 绘图坐标系宽高（与 canvas 绘制空间一致），用于触摸/鼠标坐标换算 */
  let logicalWidth = 0;
  let logicalHeight = 0;
  /** H5 环境下缓存的 canvas 元素引用 */
  let h5Canvas: HTMLCanvasElement | null = null;

  /**
   * 初始化签名画布
   */
  function initSignatureCanvas() {
    // #ifdef MP-WEIXIN
    nextTick(() => {
      setTimeout(() => {
        const query = instance ? uni.createSelectorQuery().in(instance as any) : uni.createSelectorQuery();
        query.select('#signatureCanvas').node((res: any) => {
          if (res && res.node) {
            const canvas = res.node;
            if (canvas) {
              signatureCtx = canvas.getContext('2d');
              if (signatureCtx) {
                const canvasWidthPx = config.canvasWidth / 2;
                const canvasHeightPx = config.canvasHeight / 2;
                logicalWidth = canvasWidthPx;
                logicalHeight = canvasHeightPx;
                const dpr = uni.getSystemInfoSync().pixelRatio || 2;

                canvas.width = canvasWidthPx * dpr;
                canvas.height = canvasHeightPx * dpr;

                signatureCtx.scale(dpr, dpr);
                signatureCtx.strokeStyle = '#111817';
                signatureCtx.lineWidth = 3;
                signatureCtx.lineCap = 'round';
                signatureCtx.lineJoin = 'round';
              }
            }
          }
          else {
            console.warn('Canvas node not found, retrying...');
            // 重试一次
            setTimeout(() => {
              const retryQuery = instance ? uni.createSelectorQuery().in(instance as any) : uni.createSelectorQuery();
              retryQuery.select('#signatureCanvas').node((retryRes: any) => {
                if (retryRes && retryRes.node) {
                  const canvas = retryRes.node;
                  if (canvas) {
                    signatureCtx = canvas.getContext('2d');
                    if (signatureCtx) {
                      const canvasWidthPx = config.canvasWidth / 2;
                      const canvasHeightPx = config.canvasHeight / 2;
                      logicalWidth = canvasWidthPx;
                      logicalHeight = canvasHeightPx;
                      const dpr = uni.getSystemInfoSync().pixelRatio || 2;

                      canvas.width = canvasWidthPx * dpr;
                      canvas.height = canvasHeightPx * dpr;

                      signatureCtx.scale(dpr, dpr);
                      signatureCtx.strokeStyle = '#111817';
                      signatureCtx.lineWidth = 3;
                      signatureCtx.lineCap = 'round';
                      signatureCtx.lineJoin = 'round';
                    }
                  }
                }
              }).exec();
            }, 200);
          }
        }).exec();
      }, 100);
    });
    // #endif

    // #ifdef H5
    nextTick(() => {
      setTimeout(() => {
        // 优先使用传入的 ref，否则尝试 document.getElementById
        const canvas = h5CanvasRef?.value || document.getElementById('signatureCanvas') as HTMLCanvasElement;
        if (canvas && canvas instanceof HTMLCanvasElement) {
          h5Canvas = canvas;
          signatureCtx = canvas.getContext('2d');
          if (signatureCtx) {
            const systemInfo = uni.getSystemInfoSync();
            const screenWidth = systemInfo.windowWidth;
            const canvasWidthPx = Math.floor((config.canvasWidth / 750) * screenWidth);
            const canvasHeightPx = Math.floor((config.canvasHeight / 750) * screenWidth);

            logicalWidth = canvasWidthPx;
            logicalHeight = canvasHeightPx;
            canvas.width = canvasWidthPx;
            canvas.height = canvasHeightPx;
            signatureCtx.strokeStyle = '#111817';
            signatureCtx.lineWidth = 3;
            signatureCtx.lineCap = 'round';
            signatureCtx.lineJoin = 'round';
          }
        }
      }, 100);
    });
    // #endif
  }

  function scaleToCanvas(
    clientX: number,
    clientY: number,
    rect: { left: number; top: number; width?: number; height?: number; right?: number; bottom?: number },
  ): { x: number; y: number } {
    const w = (rect.width ?? (rect.right != null && rect.left != null ? rect.right - rect.left : 0)) || 1;
    const h = (rect.height ?? (rect.bottom != null && rect.top != null ? rect.bottom - rect.top : 0)) || 1;
    const x = ((clientX - rect.left) / w) * logicalWidth;
    const y = ((clientY - rect.top) / h) * logicalHeight;
    return { x, y };
  }

  /**
   * 处理触摸开始
   */
  function handleTouchStart(e: TouchEvent) {
    if (!e.touches?.length) return;
    isDrawing.value = true;
    const touch = e.touches[0];
    const clientX = (touch as any).clientX ?? (touch as any).x;
    const clientY = (touch as any).clientY ?? (touch as any).y;

    // #ifdef MP-WEIXIN
    const query = instance ? uni.createSelectorQuery().in(instance as any) : uni.createSelectorQuery();
    query.select('#signatureCanvas').boundingClientRect((rect: any) => {
      if (rect && signatureCtx && logicalWidth && logicalHeight) {
        const { x, y } = scaleToCanvas(clientX, clientY, rect);
        signatureCtx.beginPath();
        signatureCtx.moveTo(x, y);
      }
    }).exec();
    // #endif

    // #ifdef H5
    const canvas = h5Canvas || h5CanvasRef?.value || document.getElementById('signatureCanvas') as HTMLElement | null;
    const rect = canvas?.getBoundingClientRect?.();
    if (rect && signatureCtx && logicalWidth && logicalHeight) {
      const { x, y } = scaleToCanvas(clientX, clientY, rect);
      signatureCtx.beginPath();
      signatureCtx.moveTo(x, y);
    }
    // #endif
  }

  /**
   * 处理触摸移动
   */
  function handleTouchMove(e: TouchEvent) {
    if (!isDrawing.value || !signatureCtx || !e.touches?.length) return;
    const touch = e.touches[0];
    const clientX = (touch as any).clientX ?? (touch as any).x;
    const clientY = (touch as any).clientY ?? (touch as any).y;

    // #ifdef MP-WEIXIN
    const query = instance ? uni.createSelectorQuery().in(instance as any) : uni.createSelectorQuery();
    query.select('#signatureCanvas').boundingClientRect((data: any) => {
      if (data && signatureCtx && logicalWidth && logicalHeight) {
        const { x, y } = scaleToCanvas(clientX, clientY, data);
        signatureCtx.lineTo(x, y);
        signatureCtx.stroke();
      }
    }).exec();
    // #endif

    // #ifdef H5
    const canvas = h5Canvas || h5CanvasRef?.value || document.getElementById('signatureCanvas') as HTMLElement | null;
    const rect = canvas?.getBoundingClientRect?.();
    if (rect && logicalWidth && logicalHeight) {
      const { x, y } = scaleToCanvas(clientX, clientY, rect);
      signatureCtx.lineTo(x, y);
      signatureCtx.stroke();
    }
    // #endif
  }

  /**
   * 处理触摸结束
   */
  function handleTouchEnd() {
    isDrawing.value = false;
  }

  /**
   * H5 鼠标按下（桌面端绘制）
   */
  function handleMouseDown(e: MouseEvent) {
    if (!signatureCtx || !logicalWidth || !logicalHeight) return;
    isDrawing.value = true;
    const canvas = h5Canvas || h5CanvasRef?.value || document.getElementById('signatureCanvas') as HTMLElement | null;
    const rect = canvas?.getBoundingClientRect?.();
    if (!rect) return;
    const { x, y } = scaleToCanvas(e.clientX, e.clientY, rect);
    signatureCtx.beginPath();
    signatureCtx.moveTo(x, y);
  }

  /**
   * H5 鼠标移动
   */
  function handleMouseMove(e: MouseEvent) {
    if (!isDrawing.value || !signatureCtx) return;
    const canvas = h5Canvas || h5CanvasRef?.value || document.getElementById('signatureCanvas') as HTMLElement | null;
    const rect = canvas?.getBoundingClientRect?.();
    if (!rect) return;
    const { x, y } = scaleToCanvas(e.clientX, e.clientY, rect);
    signatureCtx.lineTo(x, y);
    signatureCtx.stroke();
  }

  /**
   * H5 鼠标释放 / 移出画布
   */
  function handleMouseUp() {
    isDrawing.value = false;
  }

  /**
   * 清除签名
   */
  function clearSignature() {
    if (!signatureCtx) return;

    let w: number;
    let h: number;

    // #ifdef MP-WEIXIN
    w = config.canvasWidth / 2;
    h = config.canvasHeight / 2;
    // #endif

    // #ifdef H5
    const systemInfo = uni.getSystemInfoSync();
    const screenWidth = systemInfo.windowWidth;
    w = Math.floor((config.canvasWidth / 750) * screenWidth);
    h = Math.floor((config.canvasHeight / 750) * screenWidth);
    // #endif

    signatureCtx.clearRect(0, 0, w, h);
  }

  /**
   * 导出签名
   */
  function exportSignature(): Promise<string> {
    return new Promise((resolve, reject) => {
      // #ifdef MP-WEIXIN
      nextTick(() => {
        const query = instance ? uni.createSelectorQuery().in(instance as any) : uni.createSelectorQuery();
        query.select('#signatureCanvas').node((res: any) => {
          if (!res || !res.node) {
            console.error('Canvas node not found in exportSignature');
            reject(new Error('Canvas not found'));
            return;
          }
          const canvas = res.node;
          if (canvas) {
            const canvasWidthPx = config.canvasWidth / 2;
            const canvasHeightPx = config.canvasHeight / 2;

            uni.canvasToTempFilePath({
              canvas: canvas as any,
              width: canvasWidthPx,
              height: canvasHeightPx,
              destWidth: canvasWidthPx,
              destHeight: canvasHeightPx,
              success: (res: any) => {
                resolve(res.tempFilePath);
              },
              fail: (err: any) => {
                console.error('导出签名失败:', err);
                reject(err);
              },
            } as any);
          }
          else {
            reject(new Error('Canvas not found'));
          }
        }).exec();
      });
      // #endif

      // #ifdef H5
      setTimeout(() => {
        const canvas = h5Canvas || h5CanvasRef?.value || document.getElementById('signatureCanvas') as HTMLCanvasElement;
        if (canvas && canvas instanceof HTMLCanvasElement) {
          try {
            const dataUrl = canvas.toDataURL('image/png');
            resolve(dataUrl);
          }
          catch (error) {
            console.error('导出签名失败:', error);
            reject(error);
          }
        }
        else {
          reject(new Error('Canvas not found'));
        }
      }, 100);
      // #endif
    });
  }

  /**
   * 检查 canvas 是否已初始化
   */
  function isCanvasReady(): boolean {
    return signatureCtx !== null;
  }

  return {
    isDrawing,
    initSignatureCanvas,
    handleTouchStart,
    handleTouchMove,
    handleTouchEnd,
    handleMouseDown,
    handleMouseMove,
    handleMouseUp,
    clearSignature,
    exportSignature,
    isCanvasReady,
  };
}
