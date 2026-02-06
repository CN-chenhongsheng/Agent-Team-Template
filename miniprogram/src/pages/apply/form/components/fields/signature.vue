<template>
  <view class="signature-wrapper">
    <view v-if="label" class="signature-label">
      {{ label }}
      <text v-if="required" class="required-mark">
        *
      </text>
    </view>

    <view class="signature-canvas-wrapper" @click="openSignatureModal">
      <view v-if="!signatureValue" class="signature-placeholder">
        <u-icon name="edit-pen" size="32" color="#9ca3af" />
        <text class="placeholder-text">
          点击进行签名
        </text>
      </view>
      <image v-else class="signature-preview" :src="signatureValue" mode="aspectFit" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { inject, ref, watch } from 'vue';

interface Props {
  modelValue?: string;
  label?: string;
  required?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  label: '本人签名',
  required: false,
});

const emit = defineEmits<{
  'update:modelValue': [value: string];
}>();

const openSignaturePicker = inject<(currentValue: string, onConfirm: (value: string) => void) => void>('openSignaturePicker');

const signatureValue = ref(props.modelValue || '');

// 打开签名弹窗
function openSignatureModal() {
  if (openSignaturePicker) {
    openSignaturePicker(props.modelValue || '', (value) => {
      console.log('[signature.vue] 收到签名值:', value, '类型:', typeof value, '长度:', value ? value.length : 0);
      signatureValue.value = value;
      emit('update:modelValue', value);
      console.log('[signature.vue] emit 完成，当前 signatureValue:', signatureValue.value);
    });
  }
}

// 监听 modelValue 变化
watch(() => props.modelValue, (newVal) => {
  console.log('[signature.vue] watch 触发，新值:', newVal, '长度:', newVal ? newVal.length : 0);
  // 只有当新值不为空时才更新，避免覆盖用户刚刚输入的签名
  if (newVal && newVal.trim()) {
    signatureValue.value = newVal;
  }
}, { immediate: true });
</script>

<style lang="scss" scoped>
.signature-wrapper {
  width: 100%;
}

.signature-label {
  margin-bottom: 16rpx;
  font-size: 24rpx;
  color: #6b7280;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 1rpx;

  .required-mark {
    margin-left: 4rpx;
    color: #ff9f43;
  }
}

.signature-canvas-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  width: 100%;
  height: 200rpx;
  background: rgb(255 255 255 / 60%);
  border: 2rpx dashed rgb(10 219 195 / 30%);
  border-radius: 24rpx;
  transition: all 0.2s;

  &:active {
    background: rgb(255 255 255 / 80%);
  }

  .signature-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12rpx;

    .placeholder-text {
      font-size: 24rpx;
      color: #9ca3af;
    }
  }

  .signature-preview {
    width: 100%;
    height: 100%;
    object-fit: contain;
  }
}

// 遮罩层
.signature-overlay {
  position: fixed !important;
  inset: 0 !important;
  z-index: 99998 !important;
  background: rgb(0 0 0 / 50%);
  animation: fadeIn 0.3s;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
  }
}

.signature-modal {
  position: fixed !important;
  top: 50% !important;
  left: 50% !important;
  z-index: 99999 !important;
  display: flex;
  overflow: hidden;
  width: 680rpx;
  max-height: 90vh;
  background: #fff;
  border-radius: 32rpx;
  transform: translate(-50%, -50%) !important;
  flex-direction: column;
  animation: scaleIn 0.3s;
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

.signature-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 40rpx;
  border-bottom: 1rpx solid rgb(229 231 235 / 50%);

  .modal-title {
    font-size: 36rpx;
    font-weight: 700;
    color: #111817;
  }

  .modal-close {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 64rpx;
    height: 64rpx;
    border-radius: 50%;

    &:active {
      background: rgb(0 0 0 / 5%);
    }
  }
}

.signature-canvas-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40rpx;
  background: #f9fafb;
}

.signature-canvas {
  background: #fff;
  border: 1rpx solid #e5e7eb;
  border-radius: 16rpx;
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
    font-size: 32rpx;
    border-radius: 24rpx;
    transition: all 0.2s;
    flex: 1;
    font-weight: 700;

    &:active {
      transform: scale(0.95);
    }

    &.clear-btn {
      color: #6b7280;
      background: rgb(229 231 235);
    }

    &.confirm-btn {
      color: #fff;
      background: linear-gradient(to right, #0adbc3, #08bda8);
      box-shadow: 0 8rpx 24rpx rgb(10 219 195 / 30%);
    }
  }
}
</style>
