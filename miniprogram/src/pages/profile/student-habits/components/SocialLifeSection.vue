<!-- eslint-disable vue/no-mutating-props -->
<template>
  <view class="section-card glass-card">
    <view class="section-header">
      <view class="section-icon" style="background: rgb(99 102 241 / 10%);">
        <u-icon name="account" size="20" color="#6366f1" />
      </view>
      <view class="section-title">
        ??????
      </view>
    </view>

    <!-- ???? -->
    <view class="form-item">
      <view class="form-label">
        <text class="label-text">
          ????
        </text>
      </view>
      <u-radio-group v-model="formData.socialPreference" placement="column">
        <u-radio
          v-for="item in socialOptions"
          :key="item.value"
          :name="item.value"
          :label="item.label"
          active-color="#0adbc3"
          :custom-style="{ marginBottom: '16rpx' }"
        />
      </u-radio-group>
    </view>

    <!-- ???? -->
    <view class="form-item">
      <view class="form-label">
        <text class="label-text">
          ??????????
        </text>
      </view>
      <u-radio-group v-model="formData.allowVisitors" placement="column">
        <u-radio
          v-for="item in visitorOptions"
          :key="item.value"
          :name="item.value"
          :label="item.label"
          active-color="#0adbc3"
          :custom-style="{ marginBottom: '16rpx' }"
        />
      </u-radio-group>
    </view>

    <!-- ?????? -->
    <view class="form-item">
      <view class="form-label">
        <text class="label-text">
          ??????
        </text>
      </view>
      <u-radio-group v-model="formData.phoneCallTime" placement="column">
        <u-radio
          v-for="item in phoneOptions"
          :key="item.value"
          :name="item.value"
          :label="item.label"
          active-color="#0adbc3"
          :custom-style="{ marginBottom: '16rpx' }"
        />
      </u-radio-group>
    </view>

    <!-- ?????? -->
    <view class="form-item">
      <view class="form-label">
        <text class="label-text">
          ????????
        </text>
      </view>
      <u-radio-group v-model="formData.eatInRoom" placement="column">
        <u-radio
          v-for="item in frequencyOptions"
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
import {
  frequencyOptions,
  phoneOptions,
  socialOptions,
  visitorOptions,
} from '../composables/useStudentHabits';
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
