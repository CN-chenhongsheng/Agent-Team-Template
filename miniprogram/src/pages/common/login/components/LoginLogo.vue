<template>
  <view class="logo-section">
    <view class="logo-container">
      <view class="logo-bg-blur" />
      <view class="logo-deco-icon deco-1">
        <u-icon name="wifi" size="48" color="rgba(103, 232, 249, 0.4)" />
      </view>
      <view class="logo-deco-icon deco-2">
        <u-icon name="home" size="48" color="rgba(20, 184, 166, 0.4)" />
      </view>
      <view class="logo-deco-icon deco-3">
        <u-icon name="flash" size="48" color="rgba(249, 115, 22, 0.3)" />
      </view>
      <view class="logo-main-circle">
        <view class="logo-circle-gradient" />
        <view class="logo-icon-box">
          <u-icon name="home" size="32" color="#fff" />
        </view>
      </view>
      <view class="logo-shine" />
    </view>
    <view class="title-wrapper">
      <view class="title-main">
        <text class="title-gradient">
          智慧
        </text>
        <text>宿管</text>
      </view>
      <view class="title-sub-card">
        <view class="title-dot dot-1" />
        <text>Smart Dormitory System</text>
        <view class="title-dot dot-2" />
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 80rpx;
  width: 100%;
}

.logo-container {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: $spacing-lg;
  width: 256rpx;
  height: 256rpx;

  .logo-bg-blur {
    position: absolute;
    background: linear-gradient(to top right, rgb(207 250 254 / 50%), rgb(20 184 166 / 30%));
    border-radius: 50%;
    opacity: 0.7;
    filter: blur(60rpx);
    inset: 0;
    transform: scale(1.25);
  }

  .logo-deco-icon {
    position: absolute;
    z-index: 1;

    &.deco-1 {
      top: -16rpx;
      left: -64rpx;
      transform: rotate(12deg) scale(0.75);
    }

    &.deco-2 {
      top: 80rpx;
      right: -96rpx;
      transform: rotate(-12deg) scale(0.75);
    }

    &.deco-3 {
      bottom: -16rpx;
      left: -32rpx;
      transform: rotate(45deg) scale(0.5);
    }
  }

  .logo-main-circle {
    position: relative;
    z-index: 10;
    display: flex;
    justify-content: center;
    align-items: center;
    overflow: hidden;
    width: 192rpx;
    height: 192rpx;
    background: #fff;
    border: 2rpx solid rgb(255 255 255 / 60%);
    border-radius: 50%;
    box-shadow: 0 20rpx 40rpx rgb(13 148 136 / 5%);

    .logo-circle-gradient {
      position: absolute;
      inset: 0;
      background: linear-gradient(to bottom, #fff, rgb(207 250 254 / 50%));
    }

    .logo-icon-box {
      position: relative;
      z-index: 20;
      display: flex;
      justify-content: center;
      align-items: center;
      width: 96rpx;
      height: 96rpx;
      background: linear-gradient(135deg, #06b6d4, #0d9488);
      border-radius: 16rpx;
      box-shadow: 0 8rpx 24rpx rgb(6 182 212 / 30%);
      transition: transform 0.3s;
      transform: rotate(3deg);

      :deep(.u-icon) {
        filter: drop-shadow(0 2rpx 4rpx rgb(0 0 0 / 20%));
      }
    }
  }

  .logo-shine {
    position: absolute;
    z-index: 30;
    background: linear-gradient(to top right, transparent, rgb(255 255 255 / 40%), transparent);
    border-radius: 50%;
    inset: 0;
    pointer-events: none;
  }
}

.title-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-sm;
}

.title-main {
  display: flex;
  align-items: center;
  font-size: 64rpx;
  color: #1e293b;
  filter: drop-shadow(0 2rpx 4rpx rgb(0 0 0 / 5%));
  font-weight: $font-bold;
  letter-spacing: 4rpx;
  gap: $spacing-sm;

  .title-gradient {
    background: linear-gradient(to right, #0d9488, #06b6d4);
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
}

.title-sub-card {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 12rpx $spacing-lg;
  background: rgb(255 255 255 / 40%);
  backdrop-filter: blur(20rpx);
  border: 2rpx solid rgb(255 255 255 / 50%);
  border-radius: 9999rpx;
  box-shadow: 0 2rpx 8rpx rgb(0 0 0 / 5%);

  .title-dot {
    width: 12rpx;
    height: 12rpx;
    border-radius: 50%;
    animation: pulse-dot 2s ease-in-out infinite;

    &.dot-1 {
      background: #06b6d4;
    }

    &.dot-2 {
      background: #0d9488;
      animation-delay: 0.75s;
    }
  }

  text {
    font-size: $font-xs;
    color: $text-sub;
    font-weight: $font-bold;
    letter-spacing: 4rpx;
    text-transform: uppercase;
  }
}

@keyframes pulse-dot {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }

  50% {
    opacity: 0.5;
    transform: scale(1.2);
  }
}
</style>
