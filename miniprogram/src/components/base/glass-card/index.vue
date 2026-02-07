<template>
  <view class="glass-card" :class="[sizeClass, { 'glass-card--clickable': clickable }]" :style="customStyle">
    <slot />
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue';

interface Props {
  size?: 'sm' | 'md' | 'lg';
  padding?: string;
  radius?: string;
  clickable?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  size: 'md',
  clickable: false,
});

const sizeClass = computed(() => `glass-card--${props.size}`);

const customStyle = computed(() => ({
  padding: props.padding,
  borderRadius: props.radius,
}));
</script>

<style lang="scss" scoped>
.glass-card {
  background: $glass-bg;
  border: 2rpx solid $glass-border;
  box-shadow: $shadow-md;
  backdrop-filter: blur(32rpx);

  &--sm {
    padding: $spacing-md;
    border-radius: $radius-lg;
  }

  &--md {
    padding: $spacing-lg;
    border-radius: $radius-xl;
  }

  &--lg {
    padding: $spacing-xl;
    border-radius: $radius-xl;
  }

  &--clickable {
    transition: $transition-fast;

    &:active {
      transform: scale(0.98);
      opacity: 0.9;
    }
  }
}
</style>
