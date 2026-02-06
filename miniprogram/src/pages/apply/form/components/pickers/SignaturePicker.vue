<template>
  <view
    v-if="show"
    class="picker-overlay"
    :class="{ closing: isClosing }"
    @click="close"
  />
  <view
    v-if="show"
    class="signature-modal"
    :class="{ closing: isClosing }"
  >
    <view class="signature-modal-header">
      <view class="modal-title">
        手写签名
      </view>
      <view class="modal-close" @click="close">
        <u-icon name="close" size="24" color="#6b7280" />
      </view>
    </view>

    <view class="signature-canvas-container">
      <!-- #ifdef MP-WEIXIN -->
      <canvas
        id="signatureCanvas"
        type="2d"
        class="signature-canvas"
        :style="{ width: `${canvasConfig.canvasWidth}rpx`, height: `${canvasConfig.canvasHeight}rpx` }"
        @touchstart="onTouchStart"
        @touchmove="onTouchMove"
        @touchend="handleTouchEnd"
      />
      <!-- #endif -->

      <!-- #ifdef H5 -->
      <canvas
        id="signatureCanvas"
        ref="h5CanvasRef"
        class="signature-canvas"
        :style="{ width: `100%`, height: `100%` }"
        :width="canvasWidthPx"
        :height="canvasHeightPx"
        @touchstart.prevent="onTouchStart"
        @touchmove.prevent="onTouchMove"
        @touchend="handleTouchEnd"
        @mousedown.prevent="onMouseDown"
        @mousemove="onMouseMove"
        @mouseup="onMouseUp"
        @mouseleave="onMouseUp"
      />
      <!-- #endif -->
    </view>

    <view class="signature-modal-actions">
      <view class="action-btn clear-btn" @click="handleClear">
        清除
      </view>
      <view class="action-btn confirm-btn" @click="handleConfirm">
        确认
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, getCurrentInstance, nextTick, onMounted, reactive, ref } from 'vue';
import { useSignatureCanvas } from '@/composables/useSignatureCanvas';
import { uploadSignatureAPI } from '@/api/common';

const show = ref(false);
const isClosing = ref(false);
const currentValue = ref('');
const onConfirm = ref<((value: string) => void) | null>(null);

// H5 canvas ref
const h5CanvasRef = ref<HTMLCanvasElement | null>(null);

const canvasConfig = reactive({
  canvasWidth: 600,
  canvasHeight: 400,
});

// H5: 将 rpx 转换为 px（用于 canvas 的 width/height 属性）
// #ifdef H5
const canvasWidthPx = computed(() => {
  const systemInfo = uni.getSystemInfoSync();
  const screenWidth = systemInfo.windowWidth;
  // rpx 转 px: 1rpx = screenWidth / 750
  return Math.floor((canvasConfig.canvasWidth / 750) * screenWidth);
});

const canvasHeightPx = computed(() => {
  const systemInfo = uni.getSystemInfoSync();
  const screenWidth = systemInfo.windowWidth;
  // rpx 转 px: 1rpx = screenWidth / 750
  return Math.floor((canvasConfig.canvasHeight / 750) * screenWidth);
});
// #endif

const instance = getCurrentInstance();
const {
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
} = useSignatureCanvas(canvasConfig, instance, h5CanvasRef);

function onTouchStart(event: any) {
  handleTouchStart(event as TouchEvent);
}

function onTouchMove(event: any) {
  handleTouchMove(event as TouchEvent);
}

function onMouseDown(event: MouseEvent) {
  handleMouseDown(event);
}

function onMouseMove(event: MouseEvent) {
  handleMouseMove(event);
}

function onMouseUp() {
  handleMouseUp();
}

onMounted(() => {
  const systemInfo = uni.getSystemInfoSync();
  const screenWidth = systemInfo.windowWidth;
  const screenHeight = systemInfo.windowHeight;

  // #ifdef H5
  // H5：画布固定较小尺寸，避免过大（约 300×200 逻辑像素）
  const w = Math.min(320, Math.floor(screenWidth * 0.85));
  const h = Math.min(220, Math.floor(screenHeight * 0.32));
  canvasConfig.canvasWidth = Math.floor((w / screenWidth) * 750);
  canvasConfig.canvasHeight = Math.floor((h / screenWidth) * 750);
  // #endif

  // #ifdef MP-WEIXIN
  const modalWidthPx = screenWidth * 0.95;
  const modalHeightPx = screenHeight * 0.85;
  const modalWidthRpx = Math.floor(modalWidthPx * 2);
  const modalHeightRpx = Math.floor(modalHeightPx * 2);
  const canvasWidthRpx = Math.floor(modalWidthRpx - 64);
  const canvasHeightRpx = Math.floor(modalHeightRpx - 120 - 150 - 80);
  canvasConfig.canvasWidth = canvasWidthRpx;
  canvasConfig.canvasHeight = canvasHeightRpx;
  // #endif
});

function open(value: string, onConfirmCallback: (value: string) => void) {
  currentValue.value = value;
  onConfirm.value = onConfirmCallback;
  show.value = true;
  isClosing.value = false;
  // 延迟初始化，确保 DOM 已渲染
  nextTick(() => {
    setTimeout(() => {
      initSignatureCanvas();
    }, 150);
  });
}

function close() {
  isClosing.value = true;
  setTimeout(() => {
    show.value = false;
    isClosing.value = false;
  }, 300); // 等待动画完成
}

function handleClear() {
  clearSignature();
}

async function handleConfirm() {
  try {
    // 确保 canvas 已初始化
    if (!isCanvasReady()) {
      uni.showToast({ title: '签名画布未初始化，请稍候再试', icon: 'none' });
      // 尝试重新初始化
      setTimeout(() => {
        initSignatureCanvas();
      }, 100);
      return;
    }

    // 1. 导出签名（返回临时文件路径或 dataURL）
    const signatureValue = await exportSignature();

    // 2. 判断是否启用上传（开发环境可关闭，直接使用本地预览）
    const enableUpload = import.meta.env.VITE_ENABLE_SIGNATURE_UPLOAD === 'true'
      || import.meta.env.VITE_ENABLE_SIGNATURE_UPLOAD === true;

    if (!enableUpload) {
      // 开发模式：直接使用导出的 dataURL 或临时路径（不上传到服务器）
      console.log('[SignaturePicker] 开发模式：直接使用本地签名，不上传到服务器');
      console.log('[SignaturePicker] 签名值类型:', typeof signatureValue, '长度:', signatureValue ? signatureValue.length : 0);
      console.log('[SignaturePicker] 签名值预览:', signatureValue ? signatureValue.substring(0, 100) : 'null');

      if (onConfirm.value) {
        onConfirm.value(signatureValue);
      }
      close();
      return;
    }

    // 3. 生产模式：上传签名到服务器
    uni.showLoading({ title: '正在上传签名...' });
    try {
      const signatureUrls = await uploadSignatureAPI(signatureValue);
      uni.hideLoading();

      // 上传成功，将 URL 传给回调（取数组第一个元素）
      if (onConfirm.value && signatureUrls.length > 0) {
        onConfirm.value(signatureUrls[0]);
      }
      close();
    }
    catch (uploadError: any) {
      uni.hideLoading();
      console.error('上传签名失败:', uploadError);
      uni.showToast({
        title: uploadError?.message || '上传签名失败，请重试',
        icon: 'none',
        duration: 2000,
      });
      // 不关闭弹窗，让用户可以重试
    }
  }
  catch (error) {
    uni.hideLoading();
    console.error('导出签名失败:', error);
    uni.showToast({ title: '导出签名失败，请重试', icon: 'none' });
  }
}

defineExpose({
  open,
});
</script>

<style lang="scss" scoped>
// 遮罩层 - 确保全屏覆盖
.picker-overlay {
  position: fixed !important;
  inset: 0 !important;
  z-index: 99998 !important;
  background: rgb(0 0 0 / 50%);
  animation: fadeIn 0.3s;

  &.closing {
    animation: fadeOut 0.3s;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
  }
}

@keyframes fadeOut {
  from {
    opacity: 1;
  }

  to {
    opacity: 0;
  }
}

.signature-modal {
  position: fixed !important;
  top: 50% !important;
  left: 50% !important;
  z-index: 99999 !important;
  display: flex;
  overflow: hidden;
  width: 95vw;
  max-width: 1000rpx;
  height: 85vh;
  max-height: 85vh;
  background: #fff;
  border-radius: 32rpx;
  transform: translate(-50%, -50%) !important;
  flex-direction: column;
  animation: scaleIn 0.3s;
  box-sizing: border-box;

  &.closing {
    animation: scaleOut 0.3s;
  }
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0.9);
  }

  to {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
}

@keyframes scaleOut {
  from {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }

  to {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0.9);
  }
}

.signature-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 40rpx;
  border-bottom: 1rpx solid rgb(229 231 235 / 50%);

  .modal-title {
    font-size: 32rpx;
    font-weight: 700;
    color: #111817;
  }

  .modal-close {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 64rpx;
    height: 64rpx;
    color: #6b7280;
    border-radius: 50%;
    transition: all 0.2s;

    &:active {
      background: rgb(0 0 0 / 5%);
    }
  }
}

.signature-canvas-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40rpx 32rpx;
  min-height: 0;
  background: #f9fafb;
  flex: 1;
}

.signature-canvas {
  background: #fff;
  border: 1rpx solid #e5e7eb;
  border-radius: 16rpx;
  touch-action: none;
}

.signature-modal-actions {
  display: flex;
  gap: 24rpx;
  padding: 32rpx 40rpx;
  border-top: 1rpx solid rgb(229 231 235 / 50%);

  .action-btn {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 88rpx;
    font-size: 28rpx;
    border-radius: 16rpx;
    flex: 1;
    font-weight: 600;

    &.clear-btn {
      color: #6b7280;
      background: #e5e7eb;
    }

    &.confirm-btn {
      color: #fff;
      background: linear-gradient(to right, #0adbc3, #08bda8);
    }
  }
}
</style>
