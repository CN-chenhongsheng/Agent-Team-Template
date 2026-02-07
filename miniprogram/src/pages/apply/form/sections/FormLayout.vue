<template>
  <view class="page-root-wrapper">
    <view class="apply-form-page">
      <!-- 背景装饰 -->
      <view class="bg-decorations">
        <view class="blob blob-1" />
        <view class="blob blob-2" />
        <view class="blob blob-3" />
      </view>

      <view class="page-container">
        <!-- 顶部导航栏 -->
        <header class="top-header">
          <view class="header-back" @click="handleBack">
            <u-icon name="arrow-left" size="22" color="#111817" />
          </view>
          <view class="header-title">
            {{ title }}
          </view>
          <view class="header-placeholder" />
        </header>

        <!-- 主内容 -->
        <scroll-view class="content-scroll" scroll-y>
          <main class="main-content">
            <slot name="content" />
          </main>
        </scroll-view>

        <!-- 底部操作栏 -->
        <view class="bottom-actions">
          <slot name="footer">
            <view class="action-btn submit-btn" @click="emit('submit')">
              <text>{{ submitText }}</text>
              <u-icon name="arrow-right" size="18" color="#fff" />
            </view>
          </slot>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
interface Props {
  title?: string;
  submitText?: string;
}

withDefaults(defineProps<Props>(), {
  title: '业务填报',
  submitText: '提交申请',
});

const emit = defineEmits<{
  submit: [];
  back: [];
}>();

function handleBack(): void {
  emit('back');
  uni.navigateBack();
}
</script>

<style lang="scss" scoped>
// 主题变量
$primary: #0adbc3;
$text-main: #111817;
$glass-bg: rgb(255 255 255 / 65%);

.page-root-wrapper {
  position: relative;
  width: 100%;
  min-height: 100vh;
}

.apply-form-page {
  overflow-x: hidden;
  min-height: 100vh;
  background: linear-gradient(135deg, #f0fdfa 0%, #ecfccb 100%);
  background-attachment: fixed;
}

// .bg-decorations 容器使用全局定义，仅覆盖 blob 位置
.bg-decorations .blob {
  &.blob-1 {
    top: -200rpx;
    right: -100rpx;
    width: 600rpx;
    height: 600rpx;
    background: rgb(10 219 195 / 15%);
  }

  &.blob-2 {
    top: 40%;
    left: -200rpx;
    width: 500rpx;
    height: 500rpx;
    background: rgb(234 179 8 / 20%);
  }

  &.blob-3 {
    bottom: -100rpx;
    left: 40%;
    width: 700rpx;
    height: 700rpx;
    background: rgb(96 165 250 / 20%);
  }
}

.page-container {
  position: relative;
  z-index: 1;
  display: flex;
  margin: 0 auto;
  flex-direction: column;
}

// 内容滚动区域
.content-scroll {
  position: relative;
  z-index: 10;
  height: calc(100vh - var(--status-bar-height) - 84rpx - 160rpx);
}

// 顶部导航栏
.top-header {
  position: sticky;
  top: 0;
  z-index: 50;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: calc(var(--status-bar-height) + 45rpx) 32rpx 25rpx;

  .header-back {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;

    &:active {
      background: rgb(0 0 0 / 5%);
    }
  }

  .header-title {
    font-size: 36rpx;
    text-align: center;
    color: $text-main;
    flex: 1;
    font-weight: 700;
    letter-spacing: 0.5rpx;
  }

  .header-placeholder {
    width: 80rpx;
  }
}

// 主内容
// 注：gap 由 slot 内容的包装器自行控制（微信小程序 slot 会创建包装层）
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 40rpx 32rpx;
  padding-top: 0;
  padding-bottom: calc(70rpx + env(safe-area-inset-bottom));
}

// 底部操作栏
.bottom-actions {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 50;
  display: flex;
  padding: 20rpx 48rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  margin: 0 auto;
  max-width: 750rpx;
  background: $glass-bg;
  box-shadow: 0 -10rpx 40rpx rgb(0 0 0 / 3%);
  backdrop-filter: blur(32rpx);
  border-top: 1rpx solid rgb(255 255 255 / 60%);
  gap: 32rpx;

  .action-btn {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 96rpx;
    font-size: 28rpx;
    border-radius: 32rpx;
    transition: all 0.2s;
    font-weight: 700;

    &:active {
      transform: scale(0.95);
    }

    &.submit-btn {
      color: #fff;
      background: linear-gradient(to right, $primary, #08bda8);
      box-shadow: 0 8rpx 24rpx rgb(10 219 195 / 30%);
      flex: 1;
      gap: 16rpx;

      &:active {
        box-shadow: 0 4rpx 12rpx rgb(10 219 195 / 30%);
      }
    }
  }
}
</style>
