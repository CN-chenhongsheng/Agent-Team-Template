<template>
  <view class="glass-card glass-card--clickable apply-card" @click="handleClick">
    <view class="apply-card__icon" :style="{ background: bgColor }">
      <u-icon :name="icon" size="18" :color="iconColor" />
    </view>
    <view class="apply-card__info">
      <view class="apply-card__header">
        <view class="apply-card__title">
          {{ typeName }}
        </view>
        <view class="status-tag" :class="statusTagClass">
          {{ statusText }}
        </view>
      </view>
      <view class="apply-card__date">
        {{ applyDate }}
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue';

interface Props {
  id: number;
  type: string;
  typeName: string;
  icon: string;
  iconColor: string;
  bgColor: string;
  statusText: string;
  statusClass: string;
  applyDate: string;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  click: [id: number, type: string];
}>();

const statusTagClass = computed(() => {
  const map: Record<string, string> = {
    'status-processing': 'status-tag--processing',
    'status-pending': 'status-tag--pending',
    'status-approved': 'status-tag--approved',
    'status-rejected': 'status-tag--rejected',
  };
  return map[props.statusClass] || 'status-tag--info';
});

function handleClick() {
  emit('click', props.id, props.type);
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

.apply-card {
  display: flex;
  align-items: center;

  &__icon {
    display: flex;
    justify-content: center;
    align-items: center;
    margin-right: 24rpx;
    width: 72rpx;
    height: 72rpx;
    border-radius: 25rpx;
    flex-shrink: 0;
  }

  &__info {
    flex: 1;
    min-width: 0;
  }

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 6rpx;
  }

  &__title {
    font-size: 26rpx;
    font-weight: $font-bold;
    color: $text-main;
  }

  &__date {
    font-size: 22rpx;
    color: #94a3b8;
  }
}

.status-tag {
  display: inline-block;
  padding: 4rpx 16rpx;
  font-size: 20rpx;
  border-radius: 8rpx;
  font-weight: 600;

  &--pending {
    color: #f97316;
    background: rgb(249 115 22 / 10%);
  }

  &--processing {
    color: #3b82f6;
    background: rgb(59 130 246 / 10%);
  }

  &--approved {
    color: #22c55e;
    background: rgb(34 197 94 / 10%);
  }

  &--rejected {
    color: #ef4444;
    background: rgb(239 68 68 / 10%);
  }

  &--info {
    color: #6b7280;
    background: rgb(107 114 128 / 10%);
  }
}
</style>
