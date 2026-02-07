<template>
  <view class="glass-card glass-card--clickable notice-card" @click="handleClick">
    <view class="notice-card__bg-icon">
      <u-icon :name="icon || 'volume'" size="80" color="rgba(0,0,0,0.03)" />
    </view>
    <view class="notice-card__content">
      <view class="notice-card__meta">
        <view class="notice-card__tag" :class="tagClass">
          {{ tag }}
        </view>
        <view class="notice-card__date">
          {{ date }}
        </view>
      </view>
      <view class="notice-card__title">
        {{ title }}
      </view>
      <view class="notice-card__desc">
        {{ desc }}
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
interface Props {
  id: number;
  title: string;
  desc: string;
  tag: string;
  tagClass: string;
  date: string;
  icon?: string;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  click: [id: number];
}>();

function handleClick() {
  emit('click', props.id);
}
</script>

<style lang="scss" scoped>
.glass-card {
  padding: 24rpx;
  background: $glass-bg;
  border: 2rpx solid $glass-border;
  border-radius: $radius-xl;
  box-shadow: $shadow-md;
  backdrop-filter: blur(32rpx);

  &--clickable {
    transition: $transition-fast;

    &:active {
      transform: scale(0.98);
      opacity: 0.9;
    }
  }
}

.notice-card {
  position: relative;
  overflow: hidden;
  min-width: 480rpx;
  height: 140rpx;

  &__bg-icon {
    position: absolute;
    right: -20rpx;
    bottom: -20rpx;
    transform: rotate(12deg);
    opacity: 0.5;
  }

  &__content {
    position: relative;
    z-index: 1;
  }

  &__meta {
    display: flex;
    align-items: center;
    gap: 16rpx;
    margin-bottom: 12rpx;
  }

  &__tag {
    display: inline-block;
    padding: 4rpx 12rpx;
    font-size: 20rpx;
    border-radius: 16rpx;
    font-weight: $font-bold;

    &.tag-urgent {
      color: $accent;
      background: rgb(255 140 66 / 10%);
      border: 2rpx solid rgb(255 140 66 / 20%);
    }

    &.tag-activity {
      color: #3b82f6;
      background: rgb(59 130 246 / 10%);
      border: 2rpx solid rgb(59 130 246 / 20%);
    }

    &.tag-notice {
      color: #22c55e;
      background: rgb(34 197 94 / 10%);
      border: 2rpx solid rgb(34 197 94 / 20%);
    }
  }

  &__date {
    font-size: 22rpx;
    color: #94a3b8;
  }

  &__title {
    overflow: hidden;
    margin-bottom: 8rpx;
    font-size: 28rpx;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: $text-main;
    font-weight: $font-bold;
  }

  &__desc {
    overflow: hidden;
    font-size: $font-sm;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: $text-sub;
  }
}
</style>
