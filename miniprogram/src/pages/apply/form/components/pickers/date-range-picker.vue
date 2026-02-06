<template>
  <view class="date-range-picker-wrapper">
    <view v-if="label" class="picker-label">
      {{ label }}
      <text v-if="required" class="required-mark">
        *
      </text>
    </view>

    <view class="input-group" @click="openDateRangePickerLocal">
      <view class="input-icon">
        <u-icon name="calendar" size="20" color="#0adbc3" />
      </view>
      <view class="input-content">
        <view class="input-value" :class="{ 'placeholder': !displayValue, 'has-value': displayValue }">
          {{ displayValue || placeholder }}
        </view>
      </view>
      <view class="input-arrow">
        <u-icon name="arrow-down-fill" size="20" color="#9ca3af" />
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { inject, nextTick, ref, watch } from 'vue';
import type { DateRangePickerMode } from '@/composables/useDateRangePicker';

interface DateRange { startDate?: string; endDate?: string }

interface Props {
  /** range: 双日期；start / end: 单日期 */
  mode?: DateRangePickerMode;
  modelValue?: DateRange | string;
  label?: string;
  placeholder?: string;
  required?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  mode: 'range',
  modelValue: () => ({}),
  label: '留宿时间',
  placeholder: '请选择日期范围',
  required: false,
});

const emit = defineEmits<{
  'update:modelValue': [value: DateRange | string];
}>();

type OpenFn = (
  currentValue: DateRange,
  onConfirm: (value: DateRange) => void,
  mode?: DateRangePickerMode,
) => void;
const openDateRangePicker = inject<OpenFn>('openDateRangePicker');

const displayValue = ref('');

function updateDisplayValue() {
  const m = props.modelValue;
  const mode = props.mode;
  if (mode === 'start' || mode === 'end') {
    displayValue.value = typeof m === 'string' ? m : '';
    return;
  }
  const range = (typeof m === 'object' && m ? m : {}) as DateRange;
  const { startDate, endDate } = range;
  if (startDate && endDate) {
    displayValue.value = `${startDate} 至 ${endDate}`;
  }
  else {
    displayValue.value = '';
  }
}

watch(
  () => [props.mode, props.modelValue],
  () => updateDisplayValue(),
  { immediate: true, deep: true },
);

function getCurrentValue(): DateRange {
  const m = props.modelValue;
  const mode = props.mode;
  if (mode === 'start') {
    const s = typeof m === 'string' ? m : '';
    return { startDate: s || undefined, endDate: undefined };
  }
  if (mode === 'end') {
    const s = typeof m === 'string' ? m : '';
    return { startDate: undefined, endDate: s || undefined };
  }
  return (typeof m === 'object' && m ? m : {}) as DateRange;
}

function openDateRangePickerLocal() {
  if (!openDateRangePicker) return;
  const mode = props.mode;
  openDateRangePicker(getCurrentValue(), (value) => {
    if (mode === 'start') {
      displayValue.value = value.startDate || '';
      emit('update:modelValue', value.startDate || '');
    }
    else if (mode === 'end') {
      displayValue.value = value.endDate || '';
      emit('update:modelValue', value.endDate || '');
    }
    else {
      if (value.startDate && value.endDate) {
        displayValue.value = `${value.startDate} 至 ${value.endDate}`;
      }
      emit('update:modelValue', value);
    }
    nextTick(updateDisplayValue);
  }, mode);
}
</script>

<style lang="scss" scoped>
.date-range-picker-wrapper {
  width: 100%;
}

.picker-label {
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

.input-group {
  position: relative;
  display: flex;
  align-items: center;
  padding: 8rpx;
  background: rgb(255 255 255 / 60%);
  border: 1rpx solid rgb(255 255 255);
  border-radius: 32rpx;
  box-shadow: 0 2rpx 6rpx rgb(0 0 0 / 2%);
  transition: all 0.3s;
  cursor: pointer;

  .input-icon {
    position: absolute;
    left: 24rpx;
    display: flex;
    align-items: center;
    pointer-events: none;
  }

  .input-content {
    flex: 1;
    padding: 16rpx 80rpx;

    .input-value {
      display: flex;
      align-items: center;
      min-height: 48rpx;
      font-size: 28rpx;
      color: #111817;
      font-weight: 600;

      &.placeholder {
        color: #9ca3af;
        font-weight: 400;
      }

      &.has-value {
        color: #111817;
        font-weight: 600;
      }
    }
  }

  .input-arrow {
    position: absolute;
    right: 24rpx;
    display: flex;
    align-items: center;
    pointer-events: none;
  }
}

// 遮罩层 - 固定在屏幕，不受父容器影响
.picker-overlay {
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

// 日期选择弹窗 - 固定在屏幕底部，不受父容器影响
.date-range-picker-popup {
  position: fixed !important;
  right: 0 !important;
  bottom: 0 !important;
  left: 0 !important;
  z-index: 99999 !important;
  overflow: hidden;
  padding-bottom: env(safe-area-inset-bottom);
  width: 100vw !important;
  max-height: 80vh;
  background: #fff;
  border-radius: 32rpx 32rpx 0 0;
  animation: slideUp 0.3s;
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
  }

  to {
    transform: translateY(0);
  }
}

.picker-popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 48rpx;
  border-bottom: 1rpx solid rgb(229 231 235 / 50%);
  background: #fff;

  .picker-popup-btn {
    padding: 8rpx 16rpx;
    font-size: 32rpx;
    transition: all 0.2s;
    font-weight: 500;

    &.cancel-btn {
      color: #6b7280;

      &:active {
        color: #111817;
      }
    }

    &.confirm-btn {
      color: #0adbc3;
      font-weight: 600;

      &:active {
        color: #08bda8;
      }
    }
  }

  .picker-popup-title {
    font-size: 36rpx;
    text-align: center;
    color: #111817;
    flex: 1;
    font-weight: 700;
  }
}

.picker-popup-content {
  overflow-y: auto;
  padding: 16rpx 0;
  max-height: calc(80vh - 200rpx);
}

.date-range-tabs {
  display: flex;
  padding: 0 48rpx;
  border-bottom: 1rpx solid rgb(229 231 235 / 50%);
  margin-bottom: 24rpx;

  .date-tab {
    padding: 24rpx 0;
    font-size: 28rpx;
    text-align: center;
    color: #6b7280;
    transition: all 0.2s;
    flex: 1;
    border-bottom: 2rpx solid transparent;

    &.active {
      color: #0adbc3;
      font-weight: 700;
      border-bottom-color: #0adbc3;
    }
  }
}

.date-picker-view {
  width: 100%;
  height: 400rpx;
}

// 全局 Picker View 样式（微信小程序）
picker-view {
  width: 100%;
  height: 100%;
  background: #fff;
}

picker-view-column {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
}

picker-view .picker-view-item {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 16rpx 0;
  width: 100%;
  font-size: 32rpx;
  color: #111817;
  transition: all 0.3s;
}

picker-view .picker-view-item-selected {
  font-size: 36rpx;
  color: #0adbc3;
  font-weight: 700;
}
</style>
