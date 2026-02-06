<template>
  <view class="input-group">
    <view class="input-icon">
      <u-icon :name="icon" size="20" color="#0adbc3" />
    </view>
    <view class="input-content">
      <view class="input-label">
        {{ label }}
        <text v-if="required" class="required-mark">
          *
        </text>
      </view>
      <input
        :value="modelValue"
        class="input-value"
        :type="type"
        :placeholder="placeholder"
        :maxlength="maxlength"
        @input="handleInput"
      >
    </view>
  </view>
</template>

<script setup lang="ts">
interface Props {
  modelValue?: string;
  label?: string;
  placeholder?: string;
  icon?: string;
  type?: 'text' | 'number' | 'digit' | 'idcard';
  required?: boolean;
  maxlength?: number;
}

withDefaults(defineProps<Props>(), {
  modelValue: '',
  label: '输入框',
  placeholder: '请输入',
  icon: 'edit-pen',
  type: 'text',
  required: true,
  maxlength: -1,
});

const emit = defineEmits<{
  'update:modelValue': [value: string];
}>();

function handleInput(event: any) {
  const value = event.detail?.value || '';
  emit('update:modelValue', value);
}
</script>

<style lang="scss" scoped>
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

    .input-label {
      display: block;
      margin-bottom: 8rpx;
      font-size: 20rpx;
      color: #9ca3af;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 1rpx;

      .required-mark {
        color: #ff9f43;
      }
    }

    .input-value {
      display: flex;
      align-items: center;
      width: 100%;
      min-height: 48rpx;
      font-size: 28rpx;
      color: #111817;
      font-weight: 600;

      &::placeholder {
        color: #9ca3af;
        font-weight: 400;
      }
    }
  }
}
</style>
