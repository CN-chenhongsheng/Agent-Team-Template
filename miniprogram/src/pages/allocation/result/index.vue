<template>
  <view class="result-page">
    <!-- 背景装饰 -->
    <view class="bg-decorations">
      <view class="blob blob-1" />
      <view class="blob blob-2" />
    </view>

    <!-- 顶部导航 -->
    <page-header title="分配结果" />

    <view class="page-content" v-if="!loading">
      <!-- 分配状态卡片 -->
      <view class="glass-card status-card" :class="statusClass">
        <view class="status-icon">
          <u-icon :name="statusIcon" size="48" :color="statusColor" />
        </view>
        <view class="status-info">
          <view class="status-title">
            {{ statusTitle }}
          </view>
          <view class="status-desc">
            {{ statusDesc }}
          </view>
        </view>
      </view>

      <!-- 分配结果详情 -->
      <view v-if="allocationResult" class="result-section">
        <!-- 宿舍信息 -->
        <view class="section-title">
          <view class="title-icon" />
          <text>分配宿舍</text>
        </view>
        <view class="glass-card dorm-card">
          <view class="dorm-header">
            <view class="dorm-icon">
              <u-icon name="home" size="28" color="#0adbc3" />
            </view>
            <view class="dorm-name">
              {{ allocationResult.allocatedFloorCode }} - {{ allocationResult.allocatedRoomCode }}
            </view>
          </view>
          <view class="dorm-details">
            <view class="detail-item">
              <text class="detail-label">床位号</text>
              <text class="detail-value">{{ allocationResult.allocatedBedId ? `${allocationResult.allocatedBedId}号床` : '--' }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">匹配分数</text>
              <text class="detail-value score">{{ allocationResult.matchScore || '--' }}分</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">匹配等级</text>
              <text class="detail-value">{{ allocationResult.matchScoreLevel || '--' }}</text>
            </view>
          </view>
        </view>

        <!-- 室友信息 -->
        <view class="section-title">
          <view class="title-icon" />
          <text>室友信息</text>
        </view>
        <view class="glass-card roommates-card">
          <view v-if="roommates.length === 0" class="empty-roommates">
            <u-icon name="account" size="48" color="#d1d5db" />
            <text>暂无室友信息</text>
          </view>
          <view v-else class="roommate-list">
            <view
              v-for="(mate, index) in roommates"
              :key="mate.studentId"
              class="roommate-item"
            >
              <view class="roommate-avatar">
                <text class="avatar-text">{{ mate.studentName?.charAt(0) || '?' }}</text>
              </view>
              <view class="roommate-info">
                <view class="roommate-name">
                  {{ mate.studentName }}
                </view>
                <view class="roommate-meta">
                  {{ mate.className || '未知班级' }}
                </view>
              </view>
              <view class="roommate-habits">
                <view class="habit-tag">{{ mate.sleepScheduleText || '作息未知' }}</view>
                <view class="habit-tag">{{ mate.socialPreferenceText || '社交未知' }}</view>
              </view>
            </view>
          </view>
        </view>

        <!-- 匹配分析 -->
        <view class="section-title">
          <view class="title-icon" />
          <text>匹配分析</text>
        </view>
        <view class="glass-card analysis-card">
          <!-- 优势 -->
          <view v-if="allocationResult.advantages && allocationResult.advantages.length" class="analysis-section">
            <view class="analysis-label">
              <u-icon name="checkmark-circle" size="16" color="#10b981" />
              <text>匹配优势</text>
            </view>
            <view class="analysis-tags">
              <view
                v-for="(item, index) in allocationResult.advantages"
                :key="index"
                class="tag success"
              >
                {{ item }}
              </view>
            </view>
          </view>

          <!-- 潜在问题 -->
          <view v-if="allocationResult.conflictReasons && allocationResult.conflictReasons.length" class="analysis-section">
            <view class="analysis-label">
              <u-icon name="info-circle" size="16" color="#f59e0b" />
              <text>需注意</text>
            </view>
            <view class="analysis-tags">
              <view
                v-for="(item, index) in allocationResult.conflictReasons"
                :key="index"
                class="tag warning"
              >
                {{ item }}
              </view>
            </view>
          </view>

          <!-- 无分析数据 -->
          <view v-if="!allocationResult.advantages?.length && !allocationResult.conflictReasons?.length" class="empty-analysis">
            <text>暂无详细匹配分析</text>
          </view>
        </view>
      </view>

      <!-- 未分配状态 -->
      <view v-else class="empty-section">
        <view class="empty-icon">
          <u-icon name="info-circle" size="80" color="#d1d5db" />
        </view>
        <view class="empty-text">
          暂无分配结果
        </view>
        <view class="empty-desc">
          请先完成生活习惯问卷，等待系统为您智能匹配室友
        </view>
        <view class="empty-btn" @click="handleGoSurvey">
          <text>去填写问卷</text>
        </view>
      </view>

      <!-- 底部安全区域 -->
      <view class="safe-bottom" />
    </view>

    <!-- 加载中 -->
    <view v-else class="loading-container">
      <u-loading-icon mode="circle" size="48" />
      <text class="loading-text">加载中...</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { getMyAllocationResult, getRoommatesInfo } from '@/api/allocation'
import type { IAllocationResult, IRoommateInfo } from '@/types/api'

// 加载状态
const loading = ref(true)

// 分配结果
const allocationResult = ref<IAllocationResult | null>(null)

// 室友列表
const roommates = ref<IRoommateInfo[]>([])

// 分配状态
const allocationStatus = computed(() => {
  if (!allocationResult.value) return 'pending'
  if (allocationResult.value.status === 1) return 'confirmed'
  return 'allocated'
})

// 状态相关计算属性
const statusClass = computed(() => {
  const classMap = {
    pending: 'status-pending',
    allocated: 'status-allocated',
    confirmed: 'status-confirmed',
  }
  return classMap[allocationStatus.value]
})

const statusIcon = computed(() => {
  const iconMap = {
    pending: 'clock',
    allocated: 'checkmark-circle',
    confirmed: 'checkmark-circle-fill',
  }
  return iconMap[allocationStatus.value]
})

const statusColor = computed(() => {
  const colorMap = {
    pending: '#f59e0b',
    allocated: '#0adbc3',
    confirmed: '#10b981',
  }
  return colorMap[allocationStatus.value]
})

const statusTitle = computed(() => {
  const titleMap = {
    pending: '等待分配中',
    allocated: '已完成分配',
    confirmed: '分配已确认',
  }
  return titleMap[allocationStatus.value]
})

const statusDesc = computed(() => {
  const descMap = {
    pending: '系统正在为您智能匹配最合适的室友，请耐心等待',
    allocated: '您的宿舍已分配完成，请查看详细信息',
    confirmed: '您已确认分配结果，祝您入住愉快！',
  }
  return descMap[allocationStatus.value]
})

// 加载数据
async function loadData(): Promise<void> {
  loading.value = true
  try {
    // 并行请求分配结果和室友信息
    const [resultData, roommatesData] = await Promise.all([
      getMyAllocationResult(),
      getRoommatesInfo(),
    ])

    allocationResult.value = resultData || null
    roommates.value = roommatesData || []
  } catch (error) {
    console.error('加载分配结果失败:', error)
    allocationResult.value = null
    roommates.value = []
  } finally {
    loading.value = false
  }
}

// 跳转填写问卷
function handleGoSurvey(): void {
  uni.navigateTo({ url: '/pages/allocation/survey/index' })
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.result-page {
  position: relative;
  min-height: 100vh;
  background: $bg-light;
}

// 背景装饰
.bg-decorations {
  position: fixed;
  inset: 0;
  z-index: 0;
  overflow: hidden;
  pointer-events: none;

  .blob {
    position: absolute;
    border-radius: 50%;
    filter: blur(80rpx);

    &.blob-1 {
      top: -5%;
      right: -10%;
      width: 500rpx;
      height: 500rpx;
      background: rgb(10 219 195 / 20%);
    }

    &.blob-2 {
      bottom: 20%;
      left: -15%;
      width: 400rpx;
      height: 400rpx;
      background: rgb(99 102 241 / 15%);
    }
  }
}

.page-content {
  position: relative;
  z-index: 10;
  padding: 0 $spacing-lg $spacing-xl;
}

// 加载中
.loading-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
  gap: $spacing-md;

  .loading-text {
    font-size: $font-sm;
    color: $text-sub;
  }
}

// 状态卡片
.status-card {
  display: flex;
  align-items: center;
  padding: $spacing-lg;
  margin-bottom: $spacing-lg;
  gap: $spacing-md;

  .status-icon {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 96rpx;
    height: 96rpx;
    background: rgb(10 219 195 / 10%);
    border-radius: 24rpx;
    flex-shrink: 0;
  }

  .status-info {
    flex: 1;

    .status-title {
      margin-bottom: $spacing-xs;
      font-size: $font-xl;
      color: $text-main;
      font-weight: $font-bold;
    }

    .status-desc {
      font-size: $font-sm;
      color: $text-sub;
      line-height: 1.5;
    }
  }

  &.status-pending .status-icon {
    background: rgb(245 158 11 / 10%);
  }

  &.status-confirmed .status-icon {
    background: rgb(16 185 129 / 10%);
  }
}

// 区块标题
.section-title {
  display: flex;
  align-items: center;
  padding-left: 4rpx;
  gap: 12rpx;

  .title-icon {
    width: 8rpx;
    height: 32rpx;
    background: linear-gradient(180deg, $primary 0%, #6366f1 100%);
    border-radius: 4rpx;
  }

  text {
    font-size: $font-lg;
    color: $text-main;
    font-weight: $font-bold;
  }
}

// 宿舍卡片
.dorm-card {
  padding: $spacing-lg;
  margin-bottom: $spacing-lg;

  .dorm-header {
    display: flex;
    align-items: center;
    padding-bottom: $spacing-md;
    margin-bottom: $spacing-md;
    border-bottom: 1rpx solid rgb(0 0 0 / 5%);
    gap: $spacing-sm;

    .dorm-icon {
      display: flex;
      justify-content: center;
      align-items: center;
      width: 64rpx;
      height: 64rpx;
      background: rgb(10 219 195 / 10%);
      border-radius: 16rpx;
    }

    .dorm-name {
      font-size: $font-xl;
      color: $text-main;
      font-weight: $font-bold;
    }
  }

  .dorm-details {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-md;

    .detail-item {
      display: flex;
      flex-direction: column;
      min-width: 140rpx;
      gap: $spacing-xs;

      .detail-label {
        font-size: $font-xs;
        color: $text-disabled;
      }

      .detail-value {
        font-size: $font-md;
        color: $text-main;
        font-weight: $font-semibold;

        &.score {
          color: $primary;
        }
      }
    }
  }
}

// 室友卡片
.roommates-card {
  padding: $spacing-md;
  margin-bottom: $spacing-lg;

  .empty-roommates {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    padding: $spacing-xl 0;
    gap: $spacing-sm;

    text {
      font-size: $font-sm;
      color: $text-disabled;
    }
  }

  .roommate-list {
    display: flex;
    flex-direction: column;
  }

  .roommate-item {
    display: flex;
    align-items: center;
    padding: $spacing-md 0;
    border-bottom: 1rpx solid rgb(0 0 0 / 5%);
    gap: $spacing-md;

    &:last-child {
      padding-bottom: 0;
      border-bottom: none;
    }

    .roommate-avatar {
      display: flex;
      justify-content: center;
      align-items: center;
      width: 80rpx;
      height: 80rpx;
      background: linear-gradient(135deg, $primary 0%, #14b8a6 100%);
      border-radius: 50%;
      flex-shrink: 0;

      .avatar-text {
        font-size: $font-lg;
        color: #fff;
        font-weight: $font-bold;
      }
    }

    .roommate-info {
      flex: 1;
      min-width: 0;

      .roommate-name {
        margin-bottom: $spacing-xs;
        font-size: $font-md;
        color: $text-main;
        font-weight: $font-semibold;
      }

      .roommate-meta {
        font-size: $font-xs;
        color: $text-sub;
      }
    }

    .roommate-habits {
      display: flex;
      flex-direction: column;
      gap: 4rpx;
      flex-shrink: 0;

      .habit-tag {
        padding: 4rpx 12rpx;
        font-size: 20rpx;
        color: $text-sub;
        background: rgb(0 0 0 / 3%);
        border-radius: 6rpx;
        text-align: center;
      }
    }
  }
}

// 匹配分析卡片
.analysis-card {
  padding: $spacing-md;

  .analysis-section {
    margin-bottom: $spacing-md;

    &:last-child {
      margin-bottom: 0;
    }

    .analysis-label {
      display: flex;
      align-items: center;
      margin-bottom: $spacing-sm;
      gap: 8rpx;

      text {
        font-size: $font-sm;
        color: $text-main;
        font-weight: $font-semibold;
      }
    }

    .analysis-tags {
      display: flex;
      flex-wrap: wrap;
      gap: $spacing-sm;

      .tag {
        padding: 8rpx 16rpx;
        font-size: $font-xs;
        border-radius: 8rpx;

        &.success {
          color: #059669;
          background: rgb(16 185 129 / 10%);
        }

        &.warning {
          color: #d97706;
          background: rgb(245 158 11 / 10%);
        }
      }
    }
  }

  .empty-analysis {
    padding: $spacing-lg 0;
    text-align: center;

    text {
      font-size: $font-sm;
      color: $text-disabled;
    }
  }
}

// 空状态
.empty-section {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 120rpx $spacing-lg;

  .empty-icon {
    margin-bottom: $spacing-lg;
  }

  .empty-text {
    margin-bottom: $spacing-sm;
    font-size: $font-lg;
    color: $text-main;
    font-weight: $font-semibold;
  }

  .empty-desc {
    margin-bottom: $spacing-xl;
    font-size: $font-sm;
    color: $text-sub;
    text-align: center;
    line-height: 1.5;
  }

  .empty-btn {
    display: flex;
    justify-content: center;
    align-items: center;
    padding: $spacing-md $spacing-xl;
    color: #fff;
    background: linear-gradient(135deg, $primary 0%, #14b8a6 100%);
    border-radius: $radius-md;
    font-weight: $font-semibold;
    box-shadow: 0 8rpx 24rpx rgb(10 219 195 / 30%);

    &:active {
      transform: scale(0.98);
    }
  }
}

.safe-bottom {
  height: calc(env(safe-area-inset-bottom) + $spacing-lg);
}
</style>
