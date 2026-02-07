<template>
  <view
    class="message-item-wrapper animate-fade-in"
    :style="{ 'animation-delay': `${index * 0.05}s` }"
  >
    <view
      class="glass-card message-item"
      @click="emit('viewDetail', item)"
    >
      <!-- 消息内容 -->
      <view class="item-main">
        <!-- 消息头部 -->
        <view class="item-header">
          <view class="icon-wrapper" :class="`icon-type-${item.noticeType}`">
            <u-icon :name="getIconName(item.noticeType)" size="18" color="#fff" />
          </view>
          <view class="header-info">
            <view class="title-row">
              <text class="title">
                {{ item.title }}
              </text>
              <view v-if="!isRead(item)" class="new-badge">
                <view class="badge-dot" />
              </view>
            </view>
            <text class="time">
              {{ formatTime(item.publishTime || item.createTime || '') }}
            </text>
          </view>
          <u-icon name="arrow-right" size="16" color="#9ca3af" />
        </view>

        <!-- 消息内容 -->
        <view class="item-body">
          <text class="content">
            {{ item.content }}
          </text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import type { INotice } from '@/types';
import { NoticeType } from '@/types';

defineProps<{
  item: INotice;
  index: number;
}>();

const emit = defineEmits<{
  viewDetail: [item: INotice];
}>();

// 获取图标名称
function getIconName(type: NoticeType) {
  const iconMap: Record<NoticeType, string> = {
    [NoticeType.SYSTEM]: 'bell',
    [NoticeType.DORM]: 'home',
    [NoticeType.SAFETY]: 'warning',
    [NoticeType.MAINTENANCE]: 'setting',
    [NoticeType.OTHER]: 'info-circle',
  };
  return iconMap[type] || 'info-circle';
}

// 格式化时�?
function formatTime(time: string): string {
  if (!time) return '';
  const date = new Date(time);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);

  if (minutes < 1) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;
  if (hours < 24) return `${hours}小时前`;
  if (days < 7) return `${days}天前`;

  return time.split(' ')[0];
}

// 判断是否已读
function isRead(item: INotice): boolean {
  return (item.readCount || 0) > 0;
}
</script>

<style lang="scss" scoped>
.message-item-wrapper {
  opacity: 0;
}

.message-item {
  position: relative;
  display: flex;
  overflow: hidden;
  padding: 0;
  background: $glass-bg;
  border: 2rpx solid $glass-border-light;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 16rpx rgb(31 38 135 / 5%);
  transition: $transition-normal;
  backdrop-filter: blur(32rpx);

  &:active {
    transform: scale(0.98);
    box-shadow: 0 2rpx 8rpx rgb(31 38 135 / 8%);
  }

  .item-main {
    display: flex;
    padding: 20rpx $spacing-md;
    flex-direction: column;
    gap: 12rpx;
    flex: 1;
    min-width: 0;

    .item-header {
      display: flex;
      align-items: center;
      gap: 12rpx;

      .icon-wrapper {
        display: flex;
        justify-content: center;
        align-items: center;
        width: 44rpx;
        height: 44rpx;
        border-radius: 12rpx;
        flex-shrink: 0;

        // 图标类型颜色
        &.icon-type-1 {
          background: linear-gradient(135deg, $primary 0%, $primary-dark 100%);
          box-shadow: 0 2rpx 8rpx rgb(10 219 195 / 25%);
        }

        &.icon-type-2 {
          background: linear-gradient(135deg, $accent 0%, $accent-dark 100%);
          box-shadow: 0 2rpx 8rpx rgb(255 159 67 / 25%);
        }

        &.icon-type-3 {
          background: linear-gradient(135deg, #f43f5e 0%, #e11d48 100%);
          box-shadow: 0 2rpx 8rpx rgb(244 63 94 / 25%);
        }

        &.icon-type-4 {
          background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
          box-shadow: 0 2rpx 8rpx rgb(59 130 246 / 25%);
        }

        &.icon-type-99 {
          background: linear-gradient(135deg, #8b5cf6 0%, #6d28d9 100%);
          box-shadow: 0 2rpx 8rpx rgb(139 92 246 / 25%);
        }
      }

      .header-info {
        display: flex;
        flex-direction: column;
        gap: 4rpx;
        flex: 1;
        min-width: 0;

        .title-row {
          display: flex;
          align-items: center;
          gap: $spacing-xs;

          .title {
            overflow: hidden;
            font-size: $font-md;
            text-overflow: ellipsis;
            white-space: nowrap;
            color: $text-main;
            font-weight: $font-semibold;
            flex: 1;
          }

          .new-badge {
            position: relative;
            display: flex;
            justify-content: center;
            align-items: center;
            margin-left: $spacing-xs;
            flex-shrink: 0;

            .badge-dot {
              width: 12rpx;
              height: 12rpx;
              background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
              border-radius: 50%;
              box-shadow: 0 0 0 0 rgb(255 107 107 / 70%);
              animation: badge-pulse 2s infinite;
            }
          }
        }

        .time {
          font-size: 22rpx;
          color: $text-disabled;
        }
      }
    }

    .item-body {
      overflow: hidden;
      padding-left: 56rpx;
      text-overflow: ellipsis;
      white-space: nowrap;

      .content {
        font-size: $font-sm;
        color: $text-sub;
        line-height: 1.6;
      }
    }
  }
}

@keyframes badge-pulse {
  0% {
    box-shadow: 0 0 0 0 rgb(255 107 107 / 70%);
  }

  70% {
    box-shadow: 0 0 0 10rpx rgb(255 107 107 / 0%);
  }

  100% {
    box-shadow: 0 0 0 0 rgb(255 107 107 / 0%);
  }
}
</style>
