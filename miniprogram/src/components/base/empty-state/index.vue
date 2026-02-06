<template>
  <view class="empty-state">
    <u-empty :mode="mode" :text="text" :icon-size="iconSize" />
    <slot name="action" />
  </view>
</template>

<script setup lang="ts">
interface Props {
  mode?: 'data' | 'list' | 'search' | 'message' | 'network' | 'order';
  text?: string;
  iconSize?: number;
}

withDefaults(defineProps<Props>(), {
  mode: 'list',
  text: '暂无数据',
  iconSize: 120,
});
</script>

<style lang="scss" scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 0;
}
</style>
