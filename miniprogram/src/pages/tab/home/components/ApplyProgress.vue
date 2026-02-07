<template>
  <view v-if="isLoggedIn" class="section">
    <view class="section-header">
      <view class="section-title">
        我的申请进度
      </view>
      <view class="filter-btn" @click="$emit('viewAll')">
        <u-icon name="list" size="20" color="#94a3b8" />
      </view>
    </view>
    <view v-if="list.length === 0" class="empty-apply">
      <u-empty mode="list" text="暂无申请记录" icon-size="120" />
    </view>
    <view v-else class="apply-list">
      <view
        v-for="item in list.slice(0, 4)"
        :key="item.id"
        class="glass-card apply-item"
        @click="$emit('viewApply', item)"
      >
        <view class="apply-icon" :style="{ background: item.bgColor }">
          <u-icon :name="item.icon" size="18" :color="item.iconColor" />
        </view>
        <view class="apply-info">
          <view class="apply-header">
            <view class="apply-title">
              {{ item.typeName }}
            </view>
            <view class="apply-status" :class="item.statusClass">
              {{ item.statusText }}
            </view>
          </view>
          <view class="apply-date">
            {{ item.applyDate }}
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import type { IApplyDisplay } from '@/types';

defineProps<{
  list: IApplyDisplay[];
  isLoggedIn: boolean;
}>();

defineEmits<{
  viewAll: [];
  viewApply: [item: IApplyDisplay];
}>();
</script>

<style lang="scss" scoped>
.section {
  margin-bottom: $spacing-lg;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: $font-xl;
  color: $text-main;
  font-weight: $font-bold;
}

.filter-btn {
  padding: $spacing-xs;
}

.empty-apply {
  padding: 60rpx 0;
}

.apply-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.apply-item {
  display: flex;
  align-items: center;
  padding: $spacing-md;

  .apply-icon {
    display: flex;
    justify-content: center;
    align-items: center;
    margin-right: $spacing-md;
    width: 72rpx;
    height: 72rpx;
    border-radius: 25rpx;
    flex-shrink: 0;
  }

  .apply-info {
    flex: 1;
    min-width: 0;

    .apply-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 6rpx;
    }

    .apply-title {
      font-size: 26rpx;
      font-weight: $font-bold;
      color: $text-main;
    }

    .apply-status {
      padding: 4rpx 12rpx;
      font-size: $font-xs;
      border-radius: $spacing-xs;
      font-weight: $font-medium;

      &.status-processing {
        color: $accent;
        background: rgb(255 140 66 / 10%);
      }

      &.status-pending {
        color: #3b82f6;
        background: rgb(59 130 246 / 10%);
      }

      &.status-approved {
        color: #22c55e;
        background: rgb(34 197 94 / 10%);
      }

      &.status-rejected {
        color: #ef4444;
        background: rgb(239 68 68 / 10%);
      }
    }

    .apply-date {
      font-size: 22rpx;
      color: $text-disabled;
    }
  }
}
</style>
