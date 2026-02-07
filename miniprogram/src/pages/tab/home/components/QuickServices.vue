<template>
  <view class="section">
    <view class="section-title">
      快捷服务
    </view>
    <view class="service-grid">
      <view
        v-for="item in list"
        :key="item.id"
        class="glass-card service-item"
        @click="$emit('select', item)"
      >
        <view class="service-gradient" :style="getGradientStyle(item.color)" />
        <view class="service-bg-icon" :style="{ color: item.color }">
          <u-icon :name="item.icon" size="100" :color="item.color" />
        </view>
        <view class="service-content">
          <u-icon :name="item.icon" :size="32" :color="item.color" />
          <view class="service-name">
            {{ item.name }}
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import type { IQuickService } from '@/types';

defineProps<{
  list: IQuickService[];
}>();

defineEmits<{
  select: [item: IQuickService];
}>();

function getGradientStyle(color: string): { background: string } {
  const hex = color.replace('#', '');
  const r = Number.parseInt(hex.substring(0, 2), 16);
  const g = Number.parseInt(hex.substring(2, 4), 16);
  const b = Number.parseInt(hex.substring(4, 6), 16);
  return {
    background: `linear-gradient(to bottom right, rgba(${r}, ${g}, ${b}, 0.05), transparent)`,
  };
}
</script>

<style lang="scss" scoped>
.section {
  margin-bottom: $spacing-lg;
}

.section-title {
  margin-bottom: 20rpx;
  font-size: $font-xl;
  color: $text-main;
  font-weight: $font-bold;
}

.service-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-md;
}

.service-item {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  width: 100%;
  height: 160rpx;
  transition: $transition-normal;
  flex-direction: column;

  &:active {
    transform: scale(0.95);
  }

  .service-gradient {
    position: absolute;
    inset: 0;
    pointer-events: none;
  }

  .service-bg-icon {
    position: absolute;
    right: -50rpx;
    bottom: -50rpx;
    z-index: 0;
    opacity: 0.08;
    transition: $transition-slow;
    transform: rotate(12deg);
    pointer-events: none;

    :deep(.u-icon) {
      color: inherit !important;
    }

    .service-item:active & {
      transform: rotate(0deg) scale(1.25);
    }
  }

  .service-content {
    position: relative;
    z-index: 10;
    display: flex;
    flex-direction: column;
    align-items: center;
    transition: transform 0.3s;

    .service-item:active & {
      transform: translateY(-4rpx);
    }
  }

  .service-name {
    font-size: $font-sm;
    font-weight: $font-bold;
    color: #334155;
  }
}
</style>
