<template>
  <view class="service-grid">
    <!-- 我的申请 -->
    <view class="glass-card service-item" @click="$emit('action', 'apply')">
      <view class="service-gradient" :style="{ background: 'linear-gradient(to bottom right, rgba(20, 184, 166, 0.05), transparent)' }" />
      <view class="service-bg-icon">
        <u-icon name="list" size="100" color="#14b8a6" />
      </view>
      <view class="service-content">
        <u-icon name="list" size="32" color="#14b8a6" />
        <view class="service-name">
          我的申请
        </view>
      </view>
    </view>

    <!-- 报修记录 -->
    <view class="glass-card service-item" @click="$emit('action', 'repair')">
      <view class="service-gradient" :style="{ background: 'linear-gradient(to bottom right, rgba(59, 130, 246, 0.05), transparent)' }" />
      <view class="service-bg-icon">
        <u-icon name="setting" size="100" color="#3b82f6" />
      </view>
      <view class="service-content">
        <u-icon name="setting" size="32" color="#3b82f6" />
        <view class="service-name">
          报修记录
        </view>
      </view>
    </view>

    <!-- 宿舍成员 -->
    <view class="glass-card service-item" @click="$emit('action', 'roommates')">
      <view class="service-gradient" :style="{ background: 'linear-gradient(to bottom right, rgba(139, 92, 246, 0.05), transparent)' }" />
      <view class="service-bg-icon">
        <u-icon name="account" size="100" color="#8b5cf6" />
      </view>
      <view class="service-content">
        <u-icon name="account" size="32" color="#8b5cf6" />
        <view class="service-name">
          宿舍成员
        </view>
      </view>
    </view>

    <!-- 通知公告 -->
    <view class="glass-card service-item" @click="$emit('action', 'notice')">
      <view class="service-gradient" :style="{ background: 'linear-gradient(to bottom right, rgba(249, 115, 22, 0.05), transparent)' }" />
      <view class="service-bg-icon">
        <u-icon name="bell" size="100" color="#f97316" />
      </view>
      <view class="service-content">
        <u-icon name="bell" size="32" color="#f97316" />
        <view class="service-name">
          通知公告
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
defineEmits<{
  (e: 'action', type: string): void;
}>();
</script>

<style lang="scss" scoped>
// 毛玻璃卡�?

// 快捷服务网格
.service-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
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
    margin-top: $spacing-xs;
    font-size: $font-sm;
    font-weight: $font-bold;
    color: #334155;
  }
}
</style>
