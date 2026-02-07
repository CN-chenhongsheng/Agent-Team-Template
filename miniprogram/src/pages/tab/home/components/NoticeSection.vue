<template>
  <view class="section notice-section">
    <view class="section-header">
      <view class="section-title-wrapper">
        <view class="section-title">
          通知公告
        </view>
        <view v-if="unreadCount > 0" class="unread-badge">
          {{ unreadCount > 99 ? '99+' : unreadCount }}
        </view>
      </view>
      <view class="section-more" @click="$emit('viewAll')">
        查看全部
      </view>
    </view>
    <scroll-view class="notice-scroll" scroll-x enable-flex>
      <view
        v-for="notice in list"
        :key="notice.id"
        class="glass-card notice-card"
        @click="$emit('viewNotice', notice)"
      >
        <view class="notice-bg-icon">
          <u-icon :name="notice.icon || 'volume'" size="80" color="rgba(0,0,0,0.03)" />
        </view>
        <view class="notice-content">
          <view class="notice-meta">
            <view class="notice-tag" :class="notice.tagClass">
              {{ notice.tag }}
            </view>
            <view class="notice-date">
              {{ notice.date }}
            </view>
          </view>
          <view class="notice-title">
            {{ notice.title }}
          </view>
          <view class="notice-desc">
            {{ notice.desc }}
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import type { INoticeDisplay } from '@/types';

defineProps<{
  list: INoticeDisplay[];
  unreadCount: number;
}>();

defineEmits<{
  viewAll: [];
  viewNotice: [notice: INoticeDisplay];
}>();
</script>

<style lang="scss" scoped>
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;

  .section-title-wrapper {
    display: flex;
    align-items: center;
    gap: 12rpx;
  }
}

.section-title {
  font-size: $font-xl;
  color: $text-main;
  font-weight: $font-bold;

  .section-header & {
    margin-bottom: 0;
  }
}

.unread-badge {
  display: inline-flex;
  justify-content: center;
  align-items: center;
  padding: 0 8rpx;
  min-width: 32rpx;
  height: 32rpx;
  font-size: 20rpx;
  color: #fff;
  background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
  border-radius: 16rpx;
  box-shadow: 0 4rpx 12rpx rgb(255 107 107 / 30%);
  font-weight: $font-bold;
}

.section-more {
  font-size: $font-sm;
  color: $text-sub;
  font-weight: $font-medium;
}

.notice-section {
  padding-right: $spacing-lg;
  padding-left: $spacing-lg;
  margin-right: -$spacing-lg;
  margin-bottom: 0;
  margin-left: -$spacing-lg;
}

.notice-scroll {
  display: flex;
  margin-right: -$spacing-lg;
  margin-bottom: $spacing-lg;
  height: 190rpx;
  white-space: nowrap;
  background: transparent;

  :deep(.uni-scroll-view-content) {
    display: flex;
  }
}

.notice-card {
  position: relative;
  display: flex;
  overflow: hidden;
  padding: $spacing-md;
  margin-right: $spacing-md;
  width: 480rpx;
  min-width: 480rpx;
  height: 140rpx;
  max-height: 140rpx;
  white-space: normal;

  &:last-child {
    margin-right: 0;
  }

  .notice-bg-icon {
    position: absolute;
    right: -20rpx;
    bottom: -20rpx;
    transform: rotate(12deg);
    opacity: 0.5;
  }

  .notice-content {
    position: relative;
    z-index: 1;
    margin: auto 0;
  }

  .notice-meta {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    margin-bottom: 12rpx;
  }

  .notice-tag {
    display: inline-block;
    padding: 4rpx 12rpx;
    font-size: $font-xs;
    border-radius: $radius-sm;
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

  .notice-date {
    font-size: 22rpx;
    color: $text-disabled;
  }

  .notice-title {
    overflow: hidden;
    margin-bottom: $spacing-xs;
    font-size: $font-md;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: $text-main;
    font-weight: $font-bold;
    line-height: 1.3;
  }

  .notice-desc {
    overflow: hidden;
    font-size: $font-sm;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: $text-sub;
    line-height: 1.5;
  }
}
</style>
