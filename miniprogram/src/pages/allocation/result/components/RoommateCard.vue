<template>
  <view class="glass-card roommates-card">
    <view v-if="roommates.length === 0" class="empty-roommates">
      <u-icon name="account" size="48" color="#d1d5db" />
      <text>暂无室友信息</text>
    </view>
    <view v-else class="roommate-list">
      <view
        v-for="mate in roommates"
        :key="mate.studentId"
        class="roommate-item"
      >
        <view class="roommate-avatar">
          <text class="avatar-text">
            {{ mate.studentName?.charAt(0) || '?' }}
          </text>
        </view>
        <view class="roommate-info">
          <view class="roommate-name">
            {{ mate.studentName }}
          </view>
          <view class="roommate-meta">
            {{ mate.className || '未知班级' }}
          </view>
        </view>
        <view class="roommate-habits">
          <view class="habit-tag">
            {{ mate.sleepScheduleText || '作息未知' }}
          </view>
          <view class="habit-tag">
            {{ mate.socialPreferenceText || '社交未知' }}
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import type { IRoommateInfo } from '@/types/api';

defineProps<{
  roommates: IRoommateInfo[];
}>();
</script>

<style lang="scss" scoped>
// 毛玻璃卡�?

// 室友卡片
.roommates-card {
  padding: $spacing-md;
  margin-bottom: $spacing-lg;

  .empty-roommates {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    padding: $spacing-xl 0;
    gap: $spacing-sm;

    text {
      font-size: $font-sm;
      color: $text-disabled;
    }
  }

  .roommate-list {
    display: flex;
    flex-direction: column;
  }

  .roommate-item {
    display: flex;
    align-items: center;
    padding: $spacing-md 0;
    border-bottom: 1rpx solid rgb(0 0 0 / 5%);
    gap: $spacing-md;

    &:last-child {
      padding-bottom: 0;
      border-bottom: none;
    }

    .roommate-avatar {
      display: flex;
      justify-content: center;
      align-items: center;
      width: 80rpx;
      height: 80rpx;
      background: linear-gradient(135deg, $primary 0%, #14b8a6 100%);
      border-radius: 50%;
      flex-shrink: 0;

      .avatar-text {
        font-size: $font-lg;
        color: #fff;
        font-weight: $font-bold;
      }
    }

    .roommate-info {
      flex: 1;
      min-width: 0;

      .roommate-name {
        margin-bottom: $spacing-xs;
        font-size: $font-md;
        color: $text-main;
        font-weight: $font-semibold;
      }

      .roommate-meta {
        font-size: $font-xs;
        color: $text-sub;
      }
    }

    .roommate-habits {
      display: flex;
      flex-direction: column;
      gap: 4rpx;
      flex-shrink: 0;

      .habit-tag {
        padding: 4rpx 12rpx;
        font-size: 20rpx;
        text-align: center;
        color: $text-sub;
        background: rgb(0 0 0 / 3%);
        border-radius: 6rpx;
      }
    }
  }
}
</style>
