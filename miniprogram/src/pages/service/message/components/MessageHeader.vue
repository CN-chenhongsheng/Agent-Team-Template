<template>
  <view class="header-section">
    <!-- 状态栏占位 -->
    <view class="status-bar" :style="{ height: `${statusBarHeight}px` }" />

    <!-- 头部内容 -->
    <view class="header-content">
      <view class="header-left">
        <view class="header-title-group">
          <text class="header-title">
            消息中心
          </text>
          <text class="header-subtitle">
            Message Center
          </text>
        </view>
      </view>
    </view>

    <!-- 统计卡片 -->
    <view class="stats-cards">
      <view class="glass-card stat-card stat-card-1">
        <view class="stat-icon">
          <u-icon name="bell" size="20" color="#fff" />
        </view>
        <view class="stat-info">
          <text class="stat-value">
            {{ unreadCount }}
          </text>
          <text class="stat-label">
            未读消息
          </text>
        </view>
      </view>
      <view class="glass-card stat-card stat-card-2">
        <view class="stat-icon">
          <u-icon name="clock" size="20" color="#fff" />
        </view>
        <view class="stat-info">
          <text class="stat-value">
            {{ todayCount }}
          </text>
          <text class="stat-label">
            今日消息
          </text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
defineProps<{
  statusBarHeight: number;
  unreadCount: number;
  todayCount: number;
}>();
</script>

<style lang="scss" scoped>
.header-section {
  padding: 0 $spacing-lg $spacing-md;

  .status-bar {
    width: 100%;
  }

  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 0 $spacing-md;

    .header-left {
      flex: 1;

      .header-title-group {
        display: flex;
        flex-direction: column;
        gap: 4rpx;

        .header-title {
          font-size: 40rpx;
          color: $text-main;
          font-weight: $font-bold;
          letter-spacing: 0.5rpx;
        }

        .header-subtitle {
          font-size: 22rpx;
          color: $text-sub;
          font-weight: $font-medium;
          letter-spacing: 1rpx;
          text-transform: uppercase;
        }
      }
    }
  }

  // 统计卡片
  .stats-cards {
    display: flex;
    gap: $spacing-sm;
    margin-top: $spacing-sm;

    .stat-card {
      display: flex;
      align-items: center;
      padding: 20rpx $spacing-md;
      border-radius: 20rpx;
      transition: $transition-normal;
      gap: $spacing-sm;
      flex: 1;

      &:active {
        transform: scale(0.98);
      }

      .stat-icon {
        display: flex;
        justify-content: center;
        align-items: center;
        width: 48rpx;
        height: 48rpx;
        border-radius: 12rpx;
        flex-shrink: 0;
      }

      .stat-info {
        display: flex;
        flex-direction: column;
        gap: 4rpx;
        flex: 1;

        .stat-value {
          font-size: $font-lg;
          color: $text-main;
          font-weight: $font-bold;
          line-height: 1;
        }

        .stat-label {
          font-size: 22rpx;
          color: $text-sub;
          font-weight: $font-medium;
        }
      }

      &.stat-card-1 .stat-icon {
        background: linear-gradient(135deg, $primary 0%, $primary-dark 100%);
        box-shadow: $shadow-lg;
      }

      &.stat-card-2 .stat-icon {
        background: linear-gradient(135deg, $accent 0%, $accent-dark 100%);
        box-shadow: 0 4rpx 12rpx rgb(255 159 67 / 30%);
      }
    }
  }
}
</style>
