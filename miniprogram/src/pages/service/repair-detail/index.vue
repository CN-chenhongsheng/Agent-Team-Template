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
import { onLoad } from '@dcloudio/uni-app';
import { useRepairDetail } from './composables/useRepairDetail';

const {
  loading,
  repairDetail,
  ratingPopupVisible,
  ratingForm,
  canCancel,
  canRate,
  showActions,
  handleCancel,
  handleRate,
  submitRating,
  previewImage,
  getStatusIcon,
  getStatusText,
  getRepairTypeText,
  getUrgentLevelText,
  formatTime,
  initPage,
} = useRepairDetail();

onLoad((options: any) => {
  initPage(options);
});
</script>

<style lang="scss" scoped>
.repair-detail-page {
  padding: 20rpx;
  min-height: 100vh;
  background: linear-gradient(135deg, #fff5f7 0%, #ffe8ec 100%);
}

// .loading-container 使用全局 components.scss 定义

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  padding-bottom: 40rpx;
}

// 使用全局 .glass-card--solid 变体，仅覆盖 padding 和 border-radius
.glass-card {
  padding: 30rpx;
  border-radius: 16rpx;
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
