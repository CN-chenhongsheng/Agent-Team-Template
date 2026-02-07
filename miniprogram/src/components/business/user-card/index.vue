<template>
  <view class="glass-card user-card">
    <view class="user-card__content">
      <image class="user-card__avatar" :src="avatar || defaultAvatar" mode="aspectFill" />
      <view class="user-card__info">
        <view class="user-card__name">
          {{ name || '未登录' }}
        </view>
        <view v-if="studentNo" class="user-card__no">
          学号: {{ studentNo }}
        </view>
        <view v-if="dormInfo" class="user-card__dorm">
          <u-icon name="home" size="14" color="#009688" />
          <text>{{ dormInfo }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
interface Props {
  avatar?: string;
  name?: string;
  studentNo?: string;
  dormInfo?: string;
}

withDefaults(defineProps<Props>(), {
  avatar: '',
  name: '',
  studentNo: '',
  dormInfo: '',
});

const defaultAvatar = 'https://via.placeholder.com/150';
</script>

<style lang="scss" scoped>
.glass-card {
  padding: $spacing-lg;
  background: $glass-bg;
  border: 2rpx solid $glass-border;
  border-radius: $radius-xl;
  box-shadow: $shadow-md;
  backdrop-filter: blur(32rpx);
}

.user-card {
  &__content {
    display: flex;
    align-items: center;
    gap: 24rpx;
  }

  &__avatar {
    width: 120rpx;
    height: 120rpx;
    background: #e2e8f0;
    border: 4rpx solid rgb(255 255 255 / 80%);
    border-radius: 50%;
    flex-shrink: 0;
  }

  &__info {
    flex: 1;
  }

  &__name {
    font-size: $font-xl;
    font-weight: $font-bold;
    color: $text-main;
  }

  &__no {
    margin-top: 8rpx;
    font-size: $font-sm;
    color: $text-sub;
  }

  &__dorm {
    display: inline-flex;
    align-items: center;
    gap: 8rpx;
    padding: 8rpx 20rpx;
    margin-top: 16rpx;
    font-size: $font-sm;
    color: $primary-dark;
    background: rgb(10 219 195 / 10%);
    border-radius: 12rpx;
    font-weight: $font-semibold;
  }
}
</style>
