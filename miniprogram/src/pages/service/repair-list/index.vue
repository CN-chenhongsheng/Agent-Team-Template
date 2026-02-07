<template>
  <view class="repair-list-page">
    <!-- 背景装饰 -->
    <view class="bg-decorations">
      <view class="blob blob-1" />
      <view class="blob blob-2" />
      <view class="blob blob-3" />
    </view>

    <view class="page-container">
      <!-- 筛选栏 -->
      <view class="glass-card filter-bar">
        <u-tabs
          :list="statusTabs"
          :current="currentStatusIndex"
          @change="handleStatusChange"
        />
      </view>

      <!-- 报修列表 -->
      <z-paging
        ref="pagingRef"
        v-model="repairList"
        :auto="true"
        :default-page-size="10"
        @query="loadRepairList"
      >
        <view class="repair-list">
          <view
            v-for="item in repairList"
            :key="item.id"
            class="glass-card repair-item"
            @click="handleItemClick(item)"
          >
            <view class="item-header">
              <view class="type-badge">
                {{ item.repairTypeText || getRepairTypeText(item.repairType) }}
              </view>
              <view class="urgent-badge" :class="`urgent-${item.urgentLevel}`">
                {{ item.urgentLevelText || getUrgentLevelText(item.urgentLevel) }}
              </view>
              <view class="status-badge" :class="`status-${item.status}`">
                {{ item.statusText || getStatusText(item.status) }}
              </view>
            </view>

            <view class="item-content">
              <view class="description">
                {{ item.faultDescription }}
              </view>
              <view class="info-row">
                <view class="room-info">
                  <u-icon name="home" size="14" color="#999" />
                  <text class="text">
                    {{ item.roomCode || '暂无房间' }}
                  </text>
                </view>
                <view class="time">
                  {{ formatTime(item.createTime) }}
                </view>
              </view>
              <view v-if="item.repairPersonName" class="handler-info">
                <u-icon name="account" size="14" color="#999" />
                <text class="text">
                  维修人：{{ item.repairPersonName }}
                </text>
              </view>
            </view>

            <!-- 操作按钮 -->
            <view v-if="showActions(item)" class="item-actions">
              <u-button
                v-if="canCancel(item.status)"
                type="error"
                size="small"
                @click.stop="handleCancel(item)"
              >
                取消报修
              </u-button>
              <u-button
                v-if="canRate(item.status)"
                type="primary"
                size="small"
                @click.stop="handleRate(item)"
              >
                评价
              </u-button>
            </view>
          </view>
        </view>

        <!-- 空状态 -->
        <template #empty>
          <view class="empty-state">
            <u-empty mode="list" text="暂无报修记录" />
          </view>
        </template>
      </z-paging>
    </view>

    <!-- 评价弹窗 -->
    <u-popup v-model:show="ratingPopupVisible" mode="center" :round="20">
      <view class="rating-popup">
        <view class="popup-title">
          评价报修服务
        </view>
        <view class="rating-content">
          <u-rate
            v-model="ratingForm.rating"
            :count="5"
            active-color="#FFB700"
            size="32"
          />
          <u-textarea
            v-model="ratingForm.ratingComment"
            placeholder="请输入评价内容（选填）"
            :maxlength="200"
            count
            class="rating-textarea"
          />
        </view>
        <view class="popup-actions">
          <u-button type="default" @click="ratingPopupVisible = false">
            取消
          </u-button>
          <u-button type="primary" @click="submitRating">
            提交评价
          </u-button>
        </view>
      </view>
    </u-popup>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import type { IRepair, IRepairQueryParams } from '@/types';
import { cancelRepairAPI, getRepairListAPI, rateRepairAPI } from '@/api';
import { RepairStatus, RepairTypeText, UrgentLevelText } from '@/types';

// 状态筛选标签
const statusTabs = [
  { name: '全部' },
  { name: '待接单' },
  { name: '处理中' },
  { name: '已完成' },
  { name: '已取消' },
];

const currentStatusIndex = ref(0);
const repairList = ref<IRepair[]>([]);
const pagingRef = ref();

// 评价弹窗
const ratingPopupVisible = ref(false);
const currentRatingItem = ref<IRepair | null>(null);
const ratingForm = ref({
  rating: 5,
  ratingComment: '',
});

/**
 * 加载报修列表
 */
async function loadRepairList(pageNo: number, pageSize: number) {
  try {
    const params: IRepairQueryParams = {
      pageNum: pageNo,
      pageSize,
    };

    // 根据筛选条件添加状态
    if (currentStatusIndex.value > 0) {
      params.status = currentStatusIndex.value;
    }

    const result = await getRepairListAPI(params);
    pagingRef.value?.complete(result.list);
  }
  catch (error) {
    console.error('加载报修列表失败:', error);
    pagingRef.value?.complete(false);
    uni.showToast({
      title: '加载失败',
      icon: 'none',
    });
  }
}

/**
 * 状态切换
 */
function handleStatusChange(index: number) {
  currentStatusIndex.value = index;
  pagingRef.value?.reload();
}

/**
 * 点击报修项
 */
function handleItemClick(item: IRepair) {
  uni.navigateTo({
    url: `/pages/service/repair-detail/index?id=${item.id}`,
  });
}

/**
 * 取消报修
 */
async function handleCancel(item: IRepair) {
  uni.showModal({
    title: '确认取消',
    content: '确定要取消这条报修吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await cancelRepairAPI(item.id!);
          uni.showToast({
            title: '取消成功',
            icon: 'success',
          });
          pagingRef.value?.reload();
        }
        catch (error) {
          console.error('取消报修失败:', error);
          uni.showToast({
            title: '取消失败',
            icon: 'none',
          });
        }
      }
    },
  });
}

/**
 * 评价报修
 */
function handleRate(item: IRepair) {
  currentRatingItem.value = item;
  ratingForm.value = {
    rating: item.rating || 5,
    ratingComment: item.ratingComment || '',
  };
  ratingPopupVisible.value = true;
}

/**
 * 提交评价
 */
async function submitRating() {
  if (!currentRatingItem.value) {
    return;
  }

  if (ratingForm.value.rating < 1) {
    uni.showToast({
      title: '请选择评分',
      icon: 'none',
    });
    return;
  }

  try {
    await rateRepairAPI(currentRatingItem.value.id!, ratingForm.value);
    uni.showToast({
      title: '评价成功',
      icon: 'success',
    });
    ratingPopupVisible.value = false;
    pagingRef.value?.reload();
  }
  catch (error) {
    console.error('评价失败:', error);
    uni.showToast({
      title: '评价失败',
      icon: 'none',
    });
  }
}

/**
 * 获取报修类型文本
 */
function getRepairTypeText(type: number) {
  return RepairTypeText[type as keyof typeof RepairTypeText] || '未知';
}

/**
 * 获取紧急程度文本
 */
function getUrgentLevelText(level: number) {
  return UrgentLevelText[level as keyof typeof UrgentLevelText] || '一般';
}

/**
 * 获取状态文本
 */
function getStatusText(status: number) {
  const map: Record<number, string> = {
    [RepairStatus.PENDING]: '待接单',
    [RepairStatus.ACCEPTED]: '已接单',
    [RepairStatus.IN_PROGRESS]: '维修中',
    [RepairStatus.COMPLETED]: '已完成',
    [RepairStatus.CANCELLED]: '已取消',
  };
  return map[status] || '未知';
}

/**
 * 格式化时间
 */
function formatTime(time?: string) {
  if (!time) {
    return '';
  }
  return time.replace(/:\d{2}$/, ''); // 移除秒
}

/**
 * 是否显示操作按钮
 */
function showActions(item: IRepair) {
  return canCancel(item.status) || canRate(item.status);
}

/**
 * 是否可以取消
 */
function canCancel(status: number) {
  return status === RepairStatus.PENDING || status === RepairStatus.ACCEPTED;
}

/**
 * 是否可以评价
 */
function canRate(status: number) {
  return status === RepairStatus.COMPLETED;
}
</script>

<style lang="scss" scoped>
.repair-list-page {
  position: relative;
  min-height: 100vh;
  background: linear-gradient(135deg, #fff5f7 0%, #ffe8ec 100%);
}

.page-container {
  position: relative;
  z-index: 1;
  padding: 0 24rpx 32rpx;
  margin: 0 auto;
  max-width: 750rpx;
}

.filter-bar {
  padding: 20rpx 24rpx;
  margin-bottom: 24rpx;
}

.repair-list {
  .repair-item {
    padding: 30rpx;
    margin-bottom: 20rpx;
    transition: $transition-normal;

    &:active {
      transform: scale(0.98);
      box-shadow: 0 2rpx 8rpx rgb(31 38 135 / 8%);
    }

    .item-header {
      display: flex;
      align-items: center;
      gap: 12rpx;
      margin-bottom: 20rpx;
      flex-wrap: wrap;

      .type-badge {
        padding: 4rpx 16rpx;
        font-size: 24rpx;
        font-weight: 600;
        color: #ff6987;
        background: rgb(255 105 135 / 10%);
        border-radius: 8rpx;
      }

      .urgent-badge {
        padding: 4rpx 12rpx;
        font-size: 22rpx;
        border-radius: 8rpx;

        &.urgent-1 {
          color: #999;
          background: rgb(153 153 153 / 10%);
        }

        &.urgent-2 {
          color: #ff9800;
          background: rgb(255 152 0 / 10%);
        }

        &.urgent-3 {
          color: #f44336;
          background: rgb(244 67 54 / 10%);
        }
      }

      .status-badge {
        padding: 4rpx 12rpx;
        margin-left: auto;
        font-size: 22rpx;
        border-radius: 8rpx;

        &.status-1 {
          color: #ff9800;
          background: rgb(255 152 0 / 10%);
        }

        &.status-2 {
          color: #2196f3;
          background: rgb(33 150 243 / 10%);
        }

        &.status-3 {
          color: #2196f3;
          background: rgb(33 150 243 / 10%);
        }

        &.status-4 {
          color: #4caf50;
          background: rgb(76 175 80 / 10%);
        }

        &.status-5 {
          color: #999;
          background: rgb(153 153 153 / 10%);
        }
      }
    }

    .item-content {
      .description {
        margin-bottom: 16rpx;
        font-size: 28rpx;
        color: #333;
        line-height: 1.6;
      }

      .info-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8rpx;

        .room-info {
          display: flex;
          align-items: center;
          gap: 8rpx;

          .text {
            font-size: 24rpx;
            color: #666;
          }
        }

        .time {
          font-size: 24rpx;
          color: #999;
        }
      }

      .handler-info {
        display: flex;
        align-items: center;
        gap: 8rpx;

        .text {
          font-size: 24rpx;
          color: #666;
        }
      }
    }

    .item-actions {
      display: flex;
      padding-top: 20rpx;
      margin-top: 20rpx;
      gap: 16rpx;
      border-top: 1rpx solid rgb(0 0 0 / 5%);
    }
  }
}

.empty-state {
  padding: 100rpx 0;
}

.rating-popup {
  padding: 40rpx;
  width: 600rpx;
  background: #fff;
  border-radius: 20rpx;

  .popup-title {
    margin-bottom: 30rpx;
    font-size: 32rpx;
    text-align: center;
    color: #333;
    font-weight: 600;
  }

  .rating-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 30rpx;
    margin-bottom: 30rpx;

    .rating-textarea {
      width: 100%;
    }
  }

  .popup-actions {
    display: flex;
    gap: 20rpx;
  }
}
</style>
