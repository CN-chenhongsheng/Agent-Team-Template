<!-- eslint-disable vue/no-mutating-props -->
<template>
  <view class="section-card glass-card">
    <view class="section-header">
      <view class="section-icon" style="background: rgb(255 140 66 / 10%);">
        <u-icon name="reload" size="20" color="#ff8c42" />
      </view>
      <view class="section-title">
        卫生习惯
      </view>
    </view>

    <!-- 整洁程度 -->
    <view class="form-item">
      <view class="form-label">
        <text class="label-text">
          整洁程度
        </text>
      </view>
      <u-radio-group v-model="formData.cleanlinessLevel" placement="column">
        <u-radio
          v-for="item in cleanlinessOptions"
          :key="item.value"
          :name="item.value"
          :label="item.label"
          active-color="#0adbc3"
          :custom-style="{ marginBottom: '16rpx' }"
        />
      </u-radio-group>
    </view>

    <!-- 睡前整理 -->
    <view class="form-item">
      <view class="form-label">
        <text class="label-text">
          睡前是否整理
        </text>
      </view>
      <u-radio-group v-model="formData.bedtimeCleanup" placement="column">
        <u-radio
          v-for="item in cleanupOptions"
          :key="item.value"
          :name="item.value"
          :label="item.label"
          active-color="#0adbc3"
          :custom-style="{ marginBottom: '16rpx' }"
        />
      </u-radio-group>
    </view>
  </view>
</template>

<script setup lang="ts">
import { cleanlinessOptions, cleanupOptions } from '../composables/useStudentHabits';
import type { IStudentHabitsForm } from '@/types/api/student-habits';

defineProps<{ formData: IStudentHabitsForm }>();
</script>

<style lang="scss" scoped>
.section-card {
  padding: $spacing-lg;
  margin-bottom: $spacing-md;
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: $spacing-lg;
  gap: $spacing-sm;
}

.section-icon {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
}

.section-title {
  font-size: $font-lg;
  font-weight: $font-bold;
  color: #0f172a;
}

.form-item {
  margin-bottom: 40rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.form-label {
  display: flex;
  align-items: baseline;
  margin-bottom: 20rpx;

  .label-text {
    font-size: $font-md;
    font-weight: $font-semibold;
    color: #334155;
  }
}
</style>
