<template>
  <view class="status-tag" :class="statusClass">
    {{ text }}
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue';

type StatusType = 'pending' | 'processing' | 'approved' | 'rejected' | 'info' | 'warning' | 'success' | 'danger';

interface Props {
  type?: StatusType;
  text: string;
}

const props = withDefaults(defineProps<Props>(), {
  type: 'info',
});

const statusClass = computed(() => `status-tag--${props.type}`);
</script>

<style lang="scss" scoped>
.status-tag {
  display: inline-block;
  padding: 4rpx 16rpx;
  font-size: 20rpx;
  border-radius: 8rpx;
  font-weight: 600;

  &--pending, &--warning {
    color: #f97316;
    background: rgb(249 115 22 / 10%);
  }

  &--processing, &--info {
    color: #3b82f6;
    background: rgb(59 130 246 / 10%);
  }

  &--approved, &--success {
    color: #22c55e;
    background: rgb(34 197 94 / 10%);
  }

  &--rejected, &--danger {
    color: #ef4444;
    background: rgb(239 68 68 / 10%);
  }
}
</style>
