<template>
  <view class="repair-detail-page">
    <view v-if="loading" class="loading-container">
      <u-loading-icon mode="circle" size="40" color="#ff6975" />
    </view>

    <view v-else-if="repairDetail" class="detail-content">
      <!-- 状态卡片 -->
      <view class="glass-card status-card">
        <view class="status-icon" :class="`status-${repairDetail.status}`">
          <u-icon :name="getStatusIcon(repairDetail.status)" size="32" color="#fff" />
        </view>
        <view class="status-info">
          <view class="status-text">
            {{ repairDetail.statusText || getStatusText(repairDetail.status) }}
          </view>
          <view class="status-time">
            {{ formatTime(repairDetail.createTime) }}
          </view>
        </view>
        <view class="urgent-badge" :class="`urgent-${repairDetail.urgentLevel}`">
          {{ repairDetail.urgentLevelText || getUrgentLevelText(repairDetail.urgentLevel) }}
        </view>
      </view>

      <!-- 基本信息 -->
      <view class="glass-card info-card">
        <view class="card-title">
          基本信息
        </view>
        <view class="info-row">
          <view class="label">
            报修类型
          </view>
          <view class="value">
            {{ repairDetail.repairTypeText || getRepairTypeText(repairDetail.repairType) }}
          </view>
        </view>
        <view class="info-row">
          <view class="label">
            房间信息
          </view>
          <view class="value">
            {{ repairDetail.roomCode || '暂无' }}
            {{ repairDetail.bedCode ? `- ${repairDetail.bedCode}` : '' }}
          </view>
        </view>
        <view class="info-row">
          <view class="label">
            报修人
          </view>
          <view class="value">
            {{ repairDetail.studentInfo?.studentName }}
          </view>
        </view>
      </view>

      <!-- 故障描述 -->
      <view class="info-card glass-card">
        <view class="card-title">
          故障描述
        </view>
        <view class="description-text">
          {{ repairDetail.faultDescription }}
        </view>
        <view v-if="repairDetail.faultImages && repairDetail.faultImages.length > 0" class="image-list">
          <image
            v-for="(img, index) in repairDetail.faultImages"
            :key="index"
            :src="img"
            class="fault-image"
            mode="aspectFill"
            @click="previewImage(repairDetail.faultImages!, index)"
          />
        </view>
      </view>

      <!-- 维修人员信息 -->
      <view v-if="repairDetail.repairPersonName" class="info-card glass-card">
        <view class="card-title">
          维修人员
        </view>
        <view class="info-row">
          <view class="label">
            维修人员
          </view>
          <view class="value">
            {{ repairDetail.repairPersonName }}
          </view>
        </view>
        <view v-if="repairDetail.appointmentTime" class="info-row">
          <view class="label">
            预约时间
          </view>
          <view class="value">
            {{ formatTime(repairDetail.appointmentTime) }}
          </view>
        </view>
      </view>

      <!-- 维修结果 -->
      <view v-if="repairDetail.repairResult" class="info-card glass-card">
        <view class="card-title">
          维修结果
        </view>
        <view class="description-text">
          {{ repairDetail.repairResult }}
        </view>
        <view v-if="repairDetail.repairImages && repairDetail.repairImages.length > 0" class="image-list">
          <image
            v-for="(img, index) in repairDetail.repairImages"
            :key="index"
            :src="img"
            class="fault-image"
            mode="aspectFill"
            @click="previewImage(repairDetail.repairImages!, index)"
          />
        </view>
        <view v-if="repairDetail.completeTime" class="complete-time">
          完成时间：{{ formatTime(repairDetail.completeTime) }}
        </view>
      </view>

      <!-- 评价信息 -->
      <view v-if="repairDetail.rating" class="info-card glass-card">
        <view class="card-title">
          我的评价
        </view>
        <view class="rating-display">
          <u-rate :model-value="repairDetail.rating" readonly active-color="#FFB700" size="24" />
          <text class="rating-score">
            {{ repairDetail.rating }}.0分
          </text>
        </view>
        <view v-if="repairDetail.ratingComment" class="description-text">
          {{ repairDetail.ratingComment }}
        </view>
      </view>

      <!-- 操作按钮 -->
      <view v-if="showActions" class="action-buttons">
        <u-button
          v-if="canCancel"
          type="error"
          size="large"
          @click="handleCancel"
        >
          取消报修
        </u-button>
        <u-button
          v-if="canRate"
          type="primary"
          size="large"
          @click="handleRate"
        >
          评价
        </u-button>
      </view>
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
import { computed, ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import type { IRepair } from '@/types';
import { cancelRepairAPI, getRepairDetailAPI, rateRepairAPI } from '@/api';
import { RepairStatus, RepairTypeText, UrgentLevelText } from '@/types';

const loading = ref(true);
const repairDetail = ref<IRepair | null>(null);
const repairId = ref<number>(0);

// 评价弹窗
const ratingPopupVisible = ref(false);
const ratingForm = ref({
  rating: 5,
  ratingComment: '',
});

/**
 * 是否可以取消
 */
const canCancel = computed(() => {
  if (!repairDetail.value) {
    return false;
  }
  return (
    repairDetail.value.status === RepairStatus.PENDING
    || repairDetail.value.status === RepairStatus.ACCEPTED
  );
});

/**
 * 是否可以评价
 */
const canRate = computed(() => {
  if (!repairDetail.value) {
    return false;
  }
  return repairDetail.value.status === RepairStatus.COMPLETED && !repairDetail.value.rating;
});

/**
 * 是否显示操作按钮
 */
const showActions = computed(() => canCancel.value || canRate.value);

/**
 * 加载报修详情
 */
async function loadDetail() {
  if (!repairId.value) {
    return;
  }

  try {
    loading.value = true;
    repairDetail.value = await getRepairDetailAPI(repairId.value);
  }
  catch (error) {
    console.error('加载报修详情失败:', error);
    uni.showToast({
      title: '加载失败',
      icon: 'none',
    });
  }
  finally {
    loading.value = false;
  }
}

/**
 * 取消报修
 */
function handleCancel() {
  uni.showModal({
    title: '确认取消',
    content: '确定要取消这条报修吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await cancelRepairAPI(repairId.value);
          uni.showToast({
            title: '取消成功',
            icon: 'success',
          });
          // 重新加载详情
          await loadDetail();
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
function handleRate() {
  ratingForm.value = {
    rating: 5,
    ratingComment: '',
  };
  ratingPopupVisible.value = true;
}

/**
 * 提交评价
 */
async function submitRating() {
  if (ratingForm.value.rating < 1) {
    uni.showToast({
      title: '请选择评分',
      icon: 'none',
    });
    return;
  }

  try {
    await rateRepairAPI(repairId.value, ratingForm.value);
    uni.showToast({
      title: '评价成功',
      icon: 'success',
    });
    ratingPopupVisible.value = false;
    // 重新加载详情
    await loadDetail();
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
 * 预览图片
 */
function previewImage(images: string[], index: number) {
  uni.previewImage({
    urls: images,
    current: index,
  });
}

/**
 * 获取状态图标
 */
function getStatusIcon(status: number) {
  const map: Record<number, string> = {
    [RepairStatus.PENDING]: 'clock',
    [RepairStatus.ACCEPTED]: 'checkmark',
    [RepairStatus.IN_PROGRESS]: 'loading',
    [RepairStatus.COMPLETED]: 'checkmark-circle',
    [RepairStatus.CANCELLED]: 'close-circle',
  };
  return map[status] || 'info-circle';
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
 * 格式化时间
 */
function formatTime(time?: string) {
  if (!time) {
    return '';
  }
  return time.replace(/:\d{2}$/, ''); // 移除秒
}

onLoad((options: any) => {
  repairId.value = Number.parseInt(options?.id || '0');
  if (repairId.value) {
    loadDetail();
  }
  else {
    uni.showToast({
      title: '参数错误',
      icon: 'none',
    });
  }
});
</script>

<style lang="scss" scoped>
.repair-detail-page {
  padding: 20rpx;
  min-height: 100vh;
  background: linear-gradient(135deg, #fff5f7 0%, #ffe8ec 100%);
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 80vh;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  padding-bottom: 40rpx;
}

.glass-card {
  padding: 30rpx;
  background: rgb(255 255 255 / 90%);
  border-radius: 16rpx;
  box-shadow: 0 4rpx 12rpx rgb(255 105 135 / 10%);
  backdrop-filter: blur(10rpx);
}

.status-card {
  display: flex;
  align-items: center;
  gap: 20rpx;

  .status-icon {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 60rpx;
    height: 60rpx;
    border-radius: 50%;

    &.status-1 {
      background: #ff9800;
    }

    &.status-2 {
      background: #2196f3;
    }

    &.status-3 {
      background: #2196f3;
    }

    &.status-4 {
      background: #4caf50;
    }

    &.status-5 {
      background: #999;
    }
  }

  .status-info {
    flex: 1;

    .status-text {
      margin-bottom: 8rpx;
      font-size: 32rpx;
      color: #333;
      font-weight: 600;
    }

    .status-time {
      font-size: 24rpx;
      color: #999;
    }
  }

  .urgent-badge {
    padding: 8rpx 16rpx;
    font-size: 24rpx;
    border-radius: 8rpx;

    &.urgent-1 {
      color: #4caf50;
      background: rgb(76 175 80 / 10%);
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
}

.info-card {
  .card-title {
    padding-bottom: 16rpx;
    margin-bottom: 20rpx;
    font-size: 28rpx;
    color: #333;
    font-weight: 600;
    border-bottom: 1rpx solid rgb(0 0 0 / 5%);
  }

  .info-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16rpx 0;

    &:not(:last-child) {
      border-bottom: 1rpx solid rgb(0 0 0 / 3%);
    }

    .label {
      font-size: 26rpx;
      color: #666;
    }

    .value {
      font-size: 26rpx;
      color: #333;
      font-weight: 500;
    }
  }

  .description-text {
    font-size: 28rpx;
    color: #333;
    line-height: 1.8;
  }

  .image-list {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;
    margin-top: 20rpx;

    .fault-image {
      width: 200rpx;
      height: 200rpx;
      border-radius: 12rpx;
    }
  }

  .complete-time {
    margin-top: 16rpx;
    font-size: 24rpx;
    color: #999;
  }

  .rating-display {
    display: flex;
    align-items: center;
    gap: 16rpx;
    margin-bottom: 16rpx;

    .rating-score {
      font-size: 28rpx;
      font-weight: 600;
      color: #FFB700;
    }
  }
}

.action-buttons {
  display: flex;
  gap: 20rpx;
  margin-top: 20rpx;
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
