<template>
  <view class="glass-card section-card">
    <view class="section-header">
      <view class="section-title-wrapper">
        <view class="section-indicator section-indicator-primary" />
        <view class="section-title">
          基础信息
        </view>
      </view>
      <view class="readonly-badge">
        <u-icon name="lock" size="12" color="#6b7280" />
        <text>只读</text>
      </view>
    </view>

    <view class="user-info-card">
      <view class="user-icon-wrapper">
        <u-icon name="account" size="26" color="#fff" />
      </view>
      <view class="user-info-grid">
        <view class="user-info-item">
          <view class="info-label">
            姓名
          </view>
          <view class="info-value">
            {{ displayName }}
          </view>
        </view>
        <view class="user-info-item user-info-item-border">
          <view class="info-label">
            学号
          </view>
          <view class="info-value">
            {{ studentNo }}
          </view>
        </view>
      </view>
      <view class="user-info-decoration" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { IUser } from '@/types';

interface Props {
  userInfo?: IUser | null;
}

const props = defineProps<Props>();

const displayName = computed(() =>
  props.userInfo?.studentInfo?.studentName || props.userInfo?.nickname || '--',
);

const studentNo = computed(() =>
  props.userInfo?.studentInfo?.studentNo || '--',
);
</script>

<style lang="scss" scoped>
$primary: #0adbc3;
$primary-dark: #08bda8;
$text-main: #111817;
$text-sub: #6b7280;
$glass-bg: rgb(255 255 255 / 65%);
$glass-border: rgb(255 255 255 / 80%);

.glass-card {
  background: $glass-bg;
  border: 2rpx solid $glass-border;
  border-radius: 48rpx;
  box-shadow: 0 8rpx 32rpx rgb(31 38 135 / 7%);
  backdrop-filter: blur(32rpx);
}

.section-card {
  display: flex;
  padding: 40rpx;
  flex-direction: column;
  gap: 32rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;

  .section-title-wrapper {
    display: flex;
    align-items: center;
    gap: 20rpx;

    .section-indicator {
      width: 8rpx;
      height: 40rpx;
      border-radius: 4rpx;

      &.section-indicator-primary {
        background: $primary;
      }
    }

    .section-title {
      font-size: 32rpx;
      font-weight: 700;
      color: $text-main;
    }
  }

  .readonly-badge {
    display: flex;
    align-items: center;
    padding: 4rpx 16rpx;
    font-size: 20rpx;
    color: $text-sub;
    background: rgb(255 255 255 / 50%);
    border: 1rpx solid rgb(255 255 255 / 60%);
    border-radius: 9999rpx;
    gap: 4rpx;
    font-weight: 700;
    backdrop-filter: blur(8rpx);
  }
}

.user-info-card {
  position: relative;
  display: flex;
  align-items: center;
  overflow: hidden;
  padding: 32rpx;
  background: rgb(255 255 255 / 40%);
  border: 1rpx solid rgb(255 255 255);
  border-radius: 32rpx;
  box-shadow: 0 2rpx 6rpx rgb(0 0 0 / 2%);
  gap: 32rpx;

  .user-icon-wrapper {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 96rpx;
    height: 96rpx;
    background: linear-gradient(to bottom right, $primary, $primary-dark);
    border-radius: 24rpx;
    box-shadow: 0 8rpx 24rpx rgb(10 219 195 / 20%);
    flex-shrink: 0;
  }

  .user-info-grid {
    flex: 1;
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 32rpx;

    .user-info-item {
      display: flex;
      flex-direction: column;

      &.user-info-item-border {
        border-left: 1rpx solid rgb(229 231 235 / 50%);
        padding-left: 32rpx;
      }

      .info-label {
        margin-bottom: 8rpx;
        font-size: 20rpx;
        color: #9ca3af;
        font-weight: 700;
        text-transform: uppercase;
        letter-spacing: 1rpx;
      }

      .info-value {
        font-size: 32rpx;
        font-weight: 700;
        color: $text-main;
      }
    }
  }

  .user-info-decoration {
    position: absolute;
    top: -48rpx;
    right: -48rpx;
    width: 160rpx;
    height: 160rpx;
    background: rgb(10 219 195 / 10%);
    border-radius: 50%;
    filter: blur(40rpx);
    pointer-events: none;
  }
}
</style>
