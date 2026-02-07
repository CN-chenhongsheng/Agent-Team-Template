<template>
  <view class="page-layout" :class="pageClass">
    <!-- 背景装饰 -->
    <BgDecorations :theme="bgTheme" />

    <!-- 自定义导航栏 -->
    <view v-if="title" class="nav-header">
      <view v-if="showBack" class="nav-back" @click="handleBack">
        <u-icon name="arrow-left" size="22" color="#111817" />
      </view>
      <view class="nav-title" :class="{ 'nav-title-center': showBack }">
        {{ title }}
      </view>
      <view v-if="showBack" class="nav-placeholder" />
      <slot name="header-right" />
    </view>

    <!-- 内容区域 -->
    <view class="page-body">
      <slot />
    </view>

    <!-- 底部安全区域 -->
    <view class="safe-area-bottom" />

    <!-- 底部操作�?slot -->
    <slot name="footer" />
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import BgDecorations from '../bg-decorations/index.vue';

const props = withDefaults(defineProps<{
  /** 页面标题 */
  title?: string;
  /** 是否显示返回按钮 */
  showBack?: boolean;
  /** 背景主题 */
  bgTheme?: 'default' | 'warm' | 'cool';
  /** 自定义页�?class */
  customClass?: string;
}>(), {
  title: '',
  showBack: false,
  bgTheme: 'default',
  customClass: '',
});

const pageClass = computed(() => props.customClass);

function handleBack(): void {
  uni.navigateBack();
}
</script>

<style lang="scss" scoped>
.page-layout {
  position: relative;
  overflow: hidden;
  min-height: 100vh;
  background-color: $bg-light;
}

.nav-header {
  position: sticky;
  top: 0;
  z-index: 50;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: calc(var(--status-bar-height) + 45rpx) $spacing-lg 25rpx;

  .nav-back {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    flex-shrink: 0;

    &:active {
      background: rgb(0 0 0 / 5%);
    }
  }

  .nav-title {
    font-size: $font-xl;
    color: $text-main;
    font-weight: $font-bold;
    letter-spacing: 0.5rpx;

    &.nav-title-center {
      text-align: center;
      flex: 1;
    }
  }

  .nav-placeholder {
    width: 80rpx;
    flex-shrink: 0;
  }
}

.page-body {
  position: relative;
  z-index: 10;
  padding: 0 $spacing-lg;
}

.safe-area-bottom {
  height: calc(env(safe-area-inset-bottom) + 40rpx);
}
</style>
