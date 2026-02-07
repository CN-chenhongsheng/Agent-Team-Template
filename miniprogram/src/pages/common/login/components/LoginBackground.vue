<template>
  <view class="bg-decoration">
    <view class="bg-image" />
    <view class="bg-gradient-1" />
    <view class="bg-gradient-2" />
    <view class="bg-blob-1" />
    <view class="bg-blob-2" />
  </view>
</template>

<style lang="scss" scoped>
.bg-decoration {
  position: fixed;
  inset: 0;
  z-index: 0;
  overflow: hidden;
  pointer-events: none;

  .bg-image {
    position: absolute;
    background-position: center;
    background-size: cover;
    opacity: 0.03;
    filter: grayscale(100%);
    inset: 0;
    background-image: url('https://via.placeholder.com/800x600');
    mix-blend-mode: multiply;
  }

  .bg-gradient-1 {
    position: absolute;
    inset: 0;
    background: linear-gradient(to bottom right, rgb(207 250 254 / 90%), rgb(255 255 255 / 70%), rgb(255 237 213 / 50%));
  }

  .bg-gradient-2 {
    position: absolute;
    inset: 0;
    background: linear-gradient(to top, rgb(255 255 255 / 80%), transparent, transparent);
  }

  .bg-blob-1 {
    position: absolute;
    top: 0;
    right: 0;
    width: 800rpx;
    height: 800rpx;
    background: rgb(103 232 249 / 30%);
    border-radius: 50%;
    filter: blur(200rpx);
    transform: translate(50%, -33%);
  }

  .bg-blob-2 {
    position: absolute;
    bottom: 0;
    left: 0;
    width: 700rpx;
    height: 700rpx;
    background: rgb(249 115 22 / 20%);
    border-radius: 50%;
    filter: blur(180rpx);
    transform: translate(-33%, 33%);
  }
}
</style>
