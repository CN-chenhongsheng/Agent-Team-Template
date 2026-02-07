<template>
  <view class="form-section">
    <view class="section-title">
      <view class="title-icon" />
      <text>{{ title }}</text>
    </view>

    <view class="glass-card form-card">
      <view v-for="item in items" :key="item.field" class="form-item">
        <view class="item-label">
          {{ item.label }}
        </view>
        <view class="item-options">
          <view
            v-for="option in item.options"
            :key="option.value"
            class="option-tag"
            :class="{ active: formData[item.field] === option.value, disabled }"
            @click="handleSelect(item.field, option.value)"
          >
            {{ option.label }}
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import type { SurveyFormItemConfig } from '../composables/useSurveyForm';
import type { ISurveyData } from '@/types/api';

const props = defineProps<{
  /** 区块标题 */
  title: string;
  /** 表单项配置列�? */
  items: SurveyFormItemConfig[];
  /** 表单数据（响应式�? */
  formData: ISurveyData;
  /** 是否禁用 */
  disabled: boolean;
}>();

const emit = defineEmits<{
  select: [field: keyof ISurveyData, value: number];
}>();

function handleSelect(field: keyof ISurveyData, value: number): void {
  if (!props.disabled) {
    emit('select', field, value);
  }
}
</script>

<style lang="scss" scoped>
.form-section {
  margin-bottom: $spacing-lg;

  // .section-title ???? components.scss ??
}

.form-card {
  padding: $spacing-md;
}

.form-item {
  padding: $spacing-md 0;
  border-bottom: 1rpx solid rgb(0 0 0 / 5%);

  &:last-child {
    padding-bottom: 0;
    border-bottom: none;
  }

  .item-label {
    margin-bottom: $spacing-sm;
    font-size: $font-md;
    color: $text-main;
    font-weight: $font-semibold;
  }

  .item-options {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-sm;
  }

  .option-tag {
    padding: $spacing-sm $spacing-md;
    font-size: $font-sm;
    color: $text-sub;
    background: #f5f5f5;
    border: 2rpx solid transparent;
    border-radius: $radius-full;
    transition: all 0.2s;

    &.active {
      color: $primary;
      background: rgb(10 219 195 / 10%);
      border-color: $primary;
    }

    &.disabled {
      opacity: 0.6;
      pointer-events: none;
    }
  }
}
</style>
