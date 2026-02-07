<template>
  <view class="apply-list-page">
    <!-- 背景装饰 -->
    <view class="bg-decorations">
      <view class="blob blob-1" />
      <view class="blob blob-2" />
      <view class="blob blob-3" />
    </view>

    <view class="page-container">
      <!-- 精美头部区域 -->
      <view class="header-section">
        <!-- 状态栏占位 -->
        <view class="status-bar" :style="{ height: `${statusBarHeight}px` }" />

        <!-- 头部内容 -->
        <view class="header-content">
          <view class="header-left">
            <view class="header-title-group">
              <text class="header-title">
                我的申请
              </text>
              <text class="header-subtitle">
                My Applications
              </text>
            </view>
          </view>
        </view>
      </view>

      <!-- Tab切换栏 -->
      <view class="glass-card apply-tabs">
        <view
          v-for="tab in tabs"
          :key="tab.value"
          class="tab-item"
          :class="{ active: activeTab === tab.value }"
          @click="handleTabChange(tab.value)"
        >
          <view class="tab-content">
            <u-icon :name="tab.icon" size="16" />
            <text class="tab-label">
              {{ tab.label }}
            </text>
          </view>
        </view>
        <!-- 筋斗云指示线 -->
        <view
          class="tab-indicator"
          :style="indicatorStyle"
        />
      </view>

      <!-- 列表内容 -->
      <scroll-view
        class="apply-list"
        scroll-y
      >
        <!-- 加载状态 -->
        <view v-if="loading" class="loading">
          <view class="loading-spinner" />
          <view class="loading-text">
            加载中...
          </view>
        </view>

        <!-- 空状态 -->
        <view v-else-if="list.length === 0" class="empty">
          <u-empty mode="list" text="暂无申请记录" icon-size="120" />
        </view>

        <!-- 列表内容 -->
        <view v-else class="list-content">
          <view
            v-for="(item, index) in list"
            :key="item.id"
            class="glass-card apply-item animate-fade-in"
            :style="{ 'animation-delay': `${index * 0.05}s` }"
            @click="handleViewDetail(item)"
          >
            <!-- 列表项头部 -->
            <view class="item-header">
              <view class="item-left">
                <view class="item-icon">
                  <u-icon
                    :name="getApplyTypeIcon(item.type).icon"
                    size="20"
                    :color="getApplyTypeIcon(item.type).iconColor"
                  />
                </view>
                <view class="item-info">
                  <view class="item-title">
                    {{ getApplyTypeName(item.type) }}
                  </view>
                  <view class="item-date">
                    {{ item.applyDate || item.createTime }}
                  </view>
                </view>
              </view>
              <view
                class="item-status"
                :style="{ color: getStatusColor(item.status) }"
              >
                {{ getStatusText(item.status) }}
              </view>
            </view>

            <!-- 列表项内容 -->
            <view v-if="item.approveTime || item.reason" class="item-content">
              <view v-if="item.approveTime" class="info-row">
                <text class="info-label">
                  审批时间：
                </text>
                <text class="info-value">
                  {{ item.approveTime }}
                </text>
              </view>
              <view v-if="item.reason" class="info-row reason-row">
                <text class="info-label">
                  申请理由：
                </text>
                <text class="info-value reason-text">
                  {{ item.reason }}
                </text>
              </view>
            </view>

            <!-- 列表项底部 -->
            <view class="item-footer">
              <view class="item-meta">
                <text class="meta-text">
                  {{ formatTime(item.createTime) }}
                </text>
              </view>
              <u-icon name="arrow-right" size="16" color="#9ca3af" />
            </view>
          </view>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import type { IApplyListItem } from '@/types';
import { ApplyStatus } from '@/types';
import useUserStore from '@/store/modules/user';
import {
  formatTime,
  getApplyTypeIcon,
  getApplyTypeName,
  getStatusColor,
  getStatusText,
} from '@/utils/apply';
import { ROUTE_CONSTANTS } from '@/constants';
import { getMyAppliesAPI } from '@/api';
import { transformMyApplyToListItem } from '@/utils/apply-transform';

/** Tab 项类型 */
interface TabItem {
  label: string;
  value: 'all' | ApplyStatus;
  count: number;
  icon: string;
}

const userStore = useUserStore();

// 页面显示时检查登录状态
onShow(() => {
  if (!userStore.token) {
    uni.reLaunch({ url: '/pages/common/login/index' });
  }
});

const tabs = ref<TabItem[]>([
  { label: '全部', value: 'all', count: 0, icon: 'list' },
  { label: '待审核', value: ApplyStatus.PENDING, count: 0, icon: 'clock' },
  { label: '已通过', value: ApplyStatus.APPROVED, count: 0, icon: 'checkmark-circle' },
  { label: '已拒绝', value: ApplyStatus.REJECTED, count: 0, icon: 'close-circle' },
]);

const activeTab = ref<'all' | ApplyStatus>('all');
const loading = ref(false);
const list = ref<IApplyListItem[]>([]);
const statusBarHeight = ref(0);

const INDICATOR_WIDTH_RPX = 60;

const indicatorStyle = computed(() => {
  const activeIndex = tabs.value.findIndex(tab => tab.value === activeTab.value);
  if (activeIndex === -1) {
    return { width: '60rpx', left: '0rpx' };
  }

  const tabWidthPercent = 100 / tabs.value.length;
  const leftPercent = activeIndex * tabWidthPercent + tabWidthPercent / 2;

  return {
    width: `${INDICATOR_WIDTH_RPX}rpx`,
    left: `${leftPercent}%`,
    transform: 'translateX(-50%)',
  };
});

function handleTabChange(value: 'all' | ApplyStatus): void {
  activeTab.value = value;
  loadData();
}

function handleViewDetail(item: IApplyListItem): void {
  uni.navigateTo({ url: `${ROUTE_CONSTANTS.STUDENT_APPLY_DETAIL}?id=${item.id}&type=${item.type}` });
}

async function loadData(): Promise<void> {
  loading.value = true;
  try {
    // 调用统一的「我的申请」API
    // 注意：不要传 status=undefined，否则会被序列化为字符串导致后端报错
    const params: Record<string, any> = {
      pageNum: 1,
      pageSize: 100,
    };
    if (activeTab.value !== 'all') {
      params.status = activeTab.value as number;
    }

    const result = await getMyAppliesAPI(params);

    // 转换为列表展示类型
    const allApplies = result.list.map(transformMyApplyToListItem);

    // 根据当前tab过滤数据（如果API已经过滤了则直接使用）
    list.value = activeTab.value === 'all'
      ? allApplies
      : allApplies.filter(item => item.status === activeTab.value);

    // 更新tab计数（基于全量数据重新统计）
    // 注意：由于我们传了status参数，返回的数据已被过滤，所以需要重新获取全量数据来统计
    if (activeTab.value === 'all') {
      updateTabCounts(allApplies);
    }
    else {
      // 如果当前不是"全部"tab，则需要额外请求全量数据来更新计数
      const allResult = await getMyAppliesAPI({ pageNum: 1, pageSize: 100 });
      const allItems = allResult.list.map(transformMyApplyToListItem);
      updateTabCounts(allItems);
    }

    console.log('申请列表加载成功:', list.value);
  }
  catch (error) {
    console.error('加载失败:', error);
    uni.showToast({ title: '加载失败', icon: 'none' });
    list.value = [];
  }
  finally {
    loading.value = false;
  }
}

function updateTabCounts(data: IApplyListItem[]): void {
  tabs.value.forEach((tab) => {
    tab.count = tab.value === 'all'
      ? data.length
      : data.filter(item => item.status === tab.value).length;
  });
}

onMounted(() => {
  // 获取状态栏高度
  const systemInfo = uni.getSystemInfoSync();
  statusBarHeight.value = systemInfo.statusBarHeight || 0;

  loadData();
});
</script>

<style lang="scss" scoped>
// 导入公共样式变量

// ========================================
// 页面布局
// ========================================

.apply-list-page {
  overflow-x: hidden;
  min-height: 100vh;
  background: linear-gradient(135deg, $bg-gradient-start 0%, $bg-gradient-end 100%);
  background-attachment: fixed;
}

// ========================================
// 头部区域
// ========================================

.header-section {
  padding: 0 $spacing-lg;

  .status-bar {
    width: 100%;
  }

  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 0 $spacing-md;

    .header-left {
      flex: 1;

      .header-title-group {
        display: flex;
        flex-direction: column;
        gap: 4rpx;

        .header-title {
          font-size: 40rpx;
          color: $text-main;
          font-weight: $font-bold;
          letter-spacing: 0.5rpx;
        }

        .header-subtitle {
          font-size: 22rpx;
          color: $text-sub;
          font-weight: $font-medium;
          letter-spacing: 1rpx;
          text-transform: uppercase;
        }
      }
    }
  }
}

// ========================================
// Tab切换栏
// ========================================

.apply-tabs {
  display: flex;
  margin-top: $spacing-md;

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
    width: 60rpx;
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

// ========================================
// 列表容器
// ========================================

.apply-list {
  padding: $spacing-md $spacing-lg;
  padding-bottom: calc($spacing-md + env(safe-area-inset-bottom));
  width: auto;
  flex: 1;
}

// ========================================
// 加载和空状态
// ========================================

.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 120rpx 0;
  flex-direction: column;

  .loading-spinner {
    width: 60rpx;
    height: 60rpx;
    border: 4rpx solid rgb(243 243 243);
    border-top-color: $primary;
    border-radius: 50%;
    animation: loading-spin 1s linear infinite;
  }

  .loading-text {
    margin-top: $spacing-md;
    font-size: $font-md;
    color: $text-sub;
  }
}

.empty {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 120rpx 0;
}

// ========================================
// 列表内容
// ========================================

.list-content {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

// ========================================
// 列表项
// ========================================

.apply-item {
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

  .item-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    padding: 20rpx $spacing-md 0;

    .item-left {
      flex: 1;
      display: flex;
      align-items: center;
      gap: $spacing-md;

      .item-icon {
        display: flex;
        justify-content: center;
        align-items: center;
        width: 40rpx;
        height: 40rpx;
        border-radius: 20rpx;
        flex-shrink: 0;
      }

      .item-info {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: $spacing-xs;

        .item-title {
          font-size: $font-lg;
          font-weight: $font-bold;
          color: $text-main;
        }

        .item-date {
          font-size: $font-sm;
          color: $text-sub;
        }
      }
    }

    .item-status {
      padding: $spacing-xs $spacing-sm;
      font-size: $font-sm;
      background: rgb(255 255 255 / 60%);
      border-radius: $radius-sm;
      flex-shrink: 0;
      font-weight: $font-semibold;
    }
  }

  .item-content {
    padding: 12rpx $spacing-md 0;

    .info-row {
      display: flex;
      margin-bottom: 12rpx;
      font-size: 26rpx;
      line-height: 1.6;

      &:last-child {
        margin-bottom: 0;
      }

      &.reason-row {
        flex-direction: row;
        align-items: flex-start;
      }

      .info-label {
        flex-shrink: 0;
        color: $text-sub;
        font-weight: $font-medium;
      }

      .info-value {
        flex: 1;
        color: $text-main;

        &.reason-text {
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          color: $text-sub;
        }
      }
    }
  }

  .item-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12rpx $spacing-md 20rpx;
    margin-top: 12rpx;
    border-top: 1rpx solid rgb(229 231 235 / 50%);

    .item-meta {
      display: flex;
      align-items: center;
      gap: $spacing-sm;

      .meta-text {
        font-size: $font-sm;
        color: $text-sub;
      }
    }
  }
}

// ========================================
// 动画
// ========================================

// @keyframes loading-spin 使用全局 components.scss 定义
</style>
