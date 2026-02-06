<template>
  <view class="title-section">
    <view class="title-row">
      <view class="title-text">
        {{ title }}
      </view>
      <view class="status-badge">
        {{ status }}
      </view>
    </view>
    <view class="title-desc">
      请如实填写以下信息，带
      <text class="required-mark">
        *
      </text>
      为必填项。
      <br>
      提交后将由宿管员进行审核。
    </view>
  </view>
</template>

<script setup lang="ts">
interface Props {
  title?: string;
  status?: string;
}

withDefaults(defineProps<Props>(), {
  title: '申请信息填写',
  status: '进行中',
});
</script>

<style lang="scss" scoped>
$primary-dark: #08bda8;
$accent: #ff9f43;
$text-main: #111817;
$text-sub: #6b7280;

.title-section {
  .title-row {
    display: flex;
    align-items: center;
    gap: 16rpx;
    margin-bottom: 8rpx;

    .title-text {
      font-size: 48rpx;
      font-weight: 700;
      color: $text-main;
      letter-spacing: 0.5rpx;
    }

    .status-badge {
      padding: 4rpx 16rpx;
      font-size: 20rpx;
      color: $primary-dark;
      background: rgb(10 219 195 / 10%);
      border: 1rpx solid rgb(10 219 195 / 20%);
      border-radius: 9999rpx;
      font-weight: 700;
    }
  }

  .title-desc {
    margin-top: 8rpx;
    font-size: 28rpx;
    color: $text-sub;
    line-height: 1.6;

    .required-mark {
      color: $accent;
    }
  }
}
</style>
