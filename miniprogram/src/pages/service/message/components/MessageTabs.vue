<template>
  <view class="glass-card message-tabs">
    <view
      v-for="tab in tabs"
      :key="tab.value"
      class="tab-item"
      :class="{ active: activeTab === tab.value }"
      @click="emit('tabChange', tab.value)"
    >
      <view class="tab-content">
        <u-icon :name="tab.icon" size="16" />
        <text class="tab-label">
          {{ tab.label }}
        </text>
      </view>
    </view>
    <view
      class="tab-indicator"
      :style="indicatorStyle"
    />
  </view>
</template>

<script setup lang="ts">
import type { TabItem } from '../composables/useMessageList';
import type { NoticeType } from '@/types';

defineProps<{
  tabs: TabItem[];
  activeTab: 'all' | NoticeType;
  indicatorStyle: Record<string, string>;
}>();

const emit = defineEmits<{
  tabChange: [value: 'all' | NoticeType];
}>();
</script>

<style lang="scss" scoped>
.message-tabs {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 0 $spacing-lg $spacing-md;

  .tab-item {
    position: relative;
    padding: 20rpx 0;
    text-align: center;
    transition: $transition-normal;
    flex: 1;

    .tab-content {
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 6rpx;
      color: $text-sub;
      transition: $transition-normal;

      .tab-label {
        font-size: 26rpx;
        font-weight: $font-medium;
      }
    }

    &.active .tab-content {
      color: $primary;
      font-weight: $font-bold;
    }
  }

  .tab-indicator {
    position: absolute;
    bottom: 0;
    width: 50rpx;
    height: 4rpx;
    background: linear-gradient(90deg, $primary 0%, $primary-dark 100%);
    border-radius: 2rpx;
    box-shadow: 0 2rpx 8rpx rgb(10 219 195 / 40%);
    transition:
      left 0.3s cubic-bezier(0.4, 0, 0.2, 1),
      transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    pointer-events: none;
  }
}
</style>
