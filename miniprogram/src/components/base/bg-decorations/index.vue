<template>
  <view class="bg-decorations" :class="`theme-${theme}`">
    <view
      v-for="(blob, i) in blobs"
      :key="i"
      class="blob"
      :class="blob.class"
      :style="blob.style"
    />
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue';

export interface BlobConfig {
  class?: string;
  style?: Record<string, string>;
}

const props = withDefaults(defineProps<{
  /** 预设主题: default(teal+orange+blue), warm(pink+orange), cool(blue+purple) */
  theme?: 'default' | 'warm' | 'cool';
  /** 自定义 blob 配置（覆盖预设主题） */
  customBlobs?: BlobConfig[];
}>(), {
  theme: 'default',
  customBlobs: undefined,
});

const presetBlobs: Record<string, BlobConfig[]> = {
  default: [
    { class: 'blob-primary' },
    { class: 'blob-accent' },
    { class: 'blob-blue' },
  ],
  warm: [
    { class: 'blob-pink' },
    { class: 'blob-orange' },
  ],
  cool: [
    { class: 'blob-blue-top' },
    { class: 'blob-purple' },
  ],
};

const blobs = computed(() => props.customBlobs || presetBlobs[props.theme] || presetBlobs.default);
</script>

<style lang="scss" scoped>
.bg-decorations {
  position: fixed;
  inset: 0;
  z-index: 0;
  overflow: hidden;
  pointer-events: none;
}

.blob {
  position: absolute;
  border-radius: 50%;
  opacity: 0.6;
  filter: blur(120rpx);
}

// === default 主题 ===
.theme-default {
  .blob-primary {
    top: -100rpx;
    right: -100rpx;
    width: 500rpx;
    height: 500rpx;
    background-color: rgb(10 219 195 / 30%);
  }

  .blob-accent {
    top: 400rpx;
    left: -200rpx;
    width: 560rpx;
    height: 560rpx;
    background-color: rgb(255 140 66 / 25%);
  }

  .blob-blue {
    right: -100rpx;
    bottom: 200rpx;
    width: 500rpx;
    height: 500rpx;
    background-color: rgb(59 130 246 / 25%);
  }
}

// === warm 主题 ===
.theme-warm {
  .blob-pink {
    top: -5%;
    right: -10%;
    width: 500rpx;
    height: 500rpx;
    background: rgb(255 105 117 / 20%);
    filter: blur(80rpx);
  }

  .blob-orange {
    bottom: 20%;
    left: -15%;
    width: 400rpx;
    height: 400rpx;
    background: rgb(255 152 0 / 15%);
    filter: blur(80rpx);
  }
}

// === cool 主题 ===
.theme-cool {
  .blob-blue-top {
    top: -5%;
    right: -10%;
    width: 500rpx;
    height: 500rpx;
    background: rgb(10 219 195 / 20%);
    filter: blur(80rpx);
  }

  .blob-purple {
    top: 30%;
    left: -15%;
    width: 400rpx;
    height: 400rpx;
    background: rgb(99 102 241 / 15%);
    filter: blur(80rpx);
  }
}
</style>
