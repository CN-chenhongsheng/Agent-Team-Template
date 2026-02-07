<template>
  <view class="page-header" :style="{ paddingTop: `calc(var(--status-bar-height) + ${paddingTop})` }">
    <view v-if="showBack" class="page-header__back" @click="handleBack">
      <u-icon name="arrow-left" size="22" color="#111817" />
    </view>
    <view class="page-header__title">
      {{ title }}
    </view>
    <view class="page-header__right">
      <slot name="right" />
    </view>
  </view>
</template>

<script setup lang="ts">
interface Props {
  title?: string;
  showBack?: boolean;
  paddingTop?: string;
}

withDefaults(defineProps<Props>(), {
  title: '',
  showBack: true,
  paddingTop: '50rpx',
});

const emit = defineEmits<{
  back: [];
}>();

function handleBack() {
  emit('back');
  uni.navigateBack();
}
</script>

<style lang="scss" scoped>
.page-header {
  position: sticky;
  top: 0;
  z-index: $z-sticky;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 25rpx 32rpx;
  backdrop-filter: blur(32rpx);

  &__back {
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

  &__title {
    flex: 1;
    font-size: $font-xl;
    text-align: center;
    color: $text-main;
    font-weight: $font-bold;
  }

  &__right {
    display: flex;
    justify-content: flex-end;
    width: 80rpx;
  }
}
</style>
