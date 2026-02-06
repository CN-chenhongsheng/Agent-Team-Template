<template>
  <view class="textarea-group">
    <view class="textarea-header">
      <u-icon name="edit-pen" size="18" color="#0adbc3" />
      <view class="textarea-label">
        {{ label }}
        <text v-if="required" class="required-mark">
          *
        </text>
      </view>
    </view>
    <textarea
      :value="modelValue"
      class="textarea-input"
      :placeholder="placeholder"
      :maxlength="maxlength"
      @input="handleInput"
    />
  </view>
</template>

<script setup lang="ts">
interface Props {
  modelValue?: string;
  label?: string;
  placeholder?: string;
  required?: boolean;
  maxlength?: number;
}

withDefaults(defineProps<Props>(), {
  modelValue: '',
  label: '申请原因',
  placeholder: '请详细描述申请原因...',
  required: true,
  maxlength: 500,
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
.textarea-group {
  padding: 32rpx;
  background: rgb(255 255 255 / 60%);
  border: 1rpx solid rgb(255 255 255);
  border-radius: 32rpx;
  box-shadow: 0 2rpx 6rpx rgb(0 0 0 / 2%);

  .textarea-header {
    display: flex;
    align-items: center;
    gap: 16rpx;
    margin-bottom: 16rpx;

    .textarea-label {
      font-size: 24rpx;
      font-weight: 700;
      color: #6b7280;
      text-transform: uppercase;
      letter-spacing: 1rpx;

      .required-mark {
        color: #ff9f43;
      }
    }
  }

  .textarea-input {
    padding: 24rpx;
    width: 100%;
    min-height: 200rpx;
    font-size: 28rpx;
    color: #111817;
    background: rgb(255 255 255 / 40%);
    border: none;
    border-radius: 24rpx;
    line-height: 1.6;

    &::placeholder {
      color: #9ca3af;
    }
  }
}
</style>
