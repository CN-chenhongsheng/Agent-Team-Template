<template>
  <view class="recommend-page">
    <!-- 背景装饰 -->
    <view class="bg-decorations">
      <view class="blob blob-1" />
      <view class="blob blob-2" />
    </view>

    <!-- 顶部导航 -->
    <page-header title="床位推荐" />

    <view class="page-content">
      <!-- 推荐说明 -->
      <view class="glass-card intro-card">
        <view class="intro-icon">
          <u-icon name="star" size="32" color="#f59e0b" />
        </view>
        <view class="intro-content">
          <view class="intro-title">
            智能床位推荐
          </view>
          <view class="intro-desc">
            根据您的偏好和习惯，系统为您推荐了以下床位，匹配度从高到低排序
          </view>
        </view>
      </view>

      <!-- 推荐类型切换 -->
      <view class="type-switch">
        <view
          class="switch-item"
          :class="{ active: recommendType === 'new' }"
          @click="switchType('new')"
        >
          <text>床位推荐</text>
        </view>
        <view
          class="switch-item"
          :class="{ active: recommendType === 'transfer' }"
          @click="switchType('transfer')"
        >
          <text>调宿推荐</text>
        </view>
      </view>

      <!-- 加载中 -->
      <view v-if="loading" class="loading-container">
        <u-loading-icon mode="circle" size="40" />
        <text class="loading-text">
          正在为您匹配...
        </text>
      </view>

      <!-- 推荐列表 -->
      <view v-else-if="recommendList.length > 0" class="recommend-list">
        <view
          v-for="(item, index) in recommendList"
          :key="item.bedId"
          class="glass-card recommend-card"
          @click="handleSelectBed(item)"
        >
          <!-- 排名标签 -->
          <view class="rank-badge" :class="getRankClass(index)">
            {{ index + 1 }}
          </view>

          <view class="card-header">
            <view class="room-info">
              <view class="room-name">
                {{ item.floorCode || '楼层' }} - {{ item.roomCode || item.roomNumber }}
              </view>
              <view class="room-meta">
                床位号：{{ item.bedCode }} · {{ item.roommateCount || 0 }} 位室友
              </view>
            </view>
            <view class="match-score">
              <text class="score-value">
                {{ item.matchScore || 0 }}
              </text>
              <text class="score-label">
                匹配度
              </text>
            </view>
          </view>

          <view class="card-content">
            <!-- 匹配亮点 -->
            <view v-if="item.advantages && item.advantages.length" class="highlights">
              <view
                v-for="(highlight, hIndex) in item.advantages.slice(0, 3)"
                :key="hIndex"
                class="highlight-tag"
              >
                <u-icon name="checkmark" size="12" color="#10b981" />
                <text>{{ highlight }}</text>
              </view>
            </view>

            <!-- 潜在冲突 -->
            <view v-if="item.conflicts && item.conflicts.length" class="conflicts">
              <view
                v-for="(conflict, cIndex) in item.conflicts.slice(0, 2)"
                :key="cIndex"
                class="conflict-tag"
              >
                <u-icon name="info-circle" size="12" color="#f59e0b" />
                <text>{{ conflict }}</text>
              </view>
            </view>
          </view>

          <view class="card-footer">
            <view class="footer-info">
              <text class="score-range">
                匹配区间：{{ item.minMatchScore || 0 }} - {{ item.maxMatchScore || 0 }}
              </text>
            </view>
            <view class="select-btn">
              <text>查看详情</text>
              <u-icon name="arrow-right" size="14" color="#0adbc3" />
            </view>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-else class="empty-section">
        <view class="empty-icon">
          <u-icon name="search" size="80" color="#d1d5db" />
        </view>
        <view class="empty-text">
          {{ emptyText }}
        </view>
        <view class="empty-desc">
          {{ emptyDesc }}
        </view>
        <view v-if="!hasSurvey" class="empty-btn" @click="handleGoSurvey">
          <text>去填写问卷</text>
        </view>
      </view>

      <!-- 底部安全区域 -->
      <view class="safe-bottom" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { getRecommendBedsAPI, getTransferRecommendAPI } from '@/api/allocation';
import type { IBedRecommend } from '@/types/api';

// 推荐类型
const recommendType = ref<'new' | 'transfer'>('new');
const loading = ref(true);
const hasSurvey = ref(true);

// 推荐列表数据
const recommendList = ref<IBedRecommend[]>([]);

// 空状态文案
const emptyText = computed(() => {
  if (!hasSurvey.value) {
    return '请先完成问卷';
  }
  return recommendType.value === 'transfer' ? '暂无更好的推荐' : '暂无推荐床位';
});

const emptyDesc = computed(() => {
  if (!hasSurvey.value) {
    return '请先完成生活习惯问卷，系统才能为您推荐合适的床位';
  }
  return recommendType.value === 'transfer'
    ? '您当前的宿舍已经是最佳匹配了'
    : '暂时没有符合条件的空床位，请稍后再试';
});

// 获取排名样式类
function getRankClass(index: number): string {
  if (index === 0) return 'rank-gold';
  if (index === 1) return 'rank-silver';
  if (index === 2) return 'rank-bronze';
  return '';
}

// 切换推荐类型
function switchType(type: 'new' | 'transfer'): void {
  if (recommendType.value === type) return;
  recommendType.value = type;
  loadRecommendList();
}

// 加载推荐列表
async function loadRecommendList(): Promise<void> {
  loading.value = true;
  try {
    if (recommendType.value === 'transfer') {
      const data = await getTransferRecommendAPI(10);
      recommendList.value = data || [];
    }
    else {
      const data = await getRecommendBedsAPI(10);
      recommendList.value = data || [];
    }
    hasSurvey.value = true;
  }
  catch (error: any) {
    console.error('获取推荐列表失败:', error);
    // 检查是否是因为未填写问卷
    if (error?.message?.includes('问卷') || error?.code === 'SURVEY_REQUIRED') {
      hasSurvey.value = false;
    }
    recommendList.value = [];
  }
  finally {
    loading.value = false;
  }
}

// 选择床位
function handleSelectBed(item: IBedRecommend): void {
  uni.showModal({
    title: '床位详情',
    content: `房间：${item.floorCode} - ${item.roomCode}\n床位：${item.bedCode}\n匹配度：${item.matchScore}分\n室友数：${item.roommateCount || 0}人\n\n优势：${item.advantages?.join('、') || '无'}\n\n注意：${item.conflicts?.join('、') || '无'}`,
    confirmText: '知道了',
    showCancel: false,
  });
}

// 跳转填写问卷
function handleGoSurvey(): void {
  uni.navigateTo({ url: '/pages/allocation/survey/index' });
}

onMounted(() => {
  loadRecommendList();
});
</script>

<style lang="scss" scoped>
.recommend-page {
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
      background: rgb(245 158 11 / 20%);
    }

    &.blob-2 {
      bottom: 30%;
      left: -15%;
      width: 400rpx;
      height: 400rpx;
      background: rgb(10 219 195 / 15%);
    }
  }
}

.page-content {
  position: relative;
  z-index: 10;
  padding: 0 $spacing-lg $spacing-xl;
}

// 说明卡片
.intro-card {
  display: flex;
  align-items: center;
  padding: $spacing-lg;
  margin-bottom: $spacing-md;
  gap: $spacing-md;

  .intro-icon {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 80rpx;
    height: 80rpx;
    background: rgb(245 158 11 / 10%);
    border-radius: 20rpx;
    flex-shrink: 0;
  }

  .intro-content {
    flex: 1;

    .intro-title {
      margin-bottom: $spacing-xs;
      font-size: $font-lg;
      color: $text-main;
      font-weight: $font-bold;
    }

    .intro-desc {
      font-size: $font-sm;
      color: $text-sub;
      line-height: 1.5;
    }
  }
}

// 类型切换
.type-switch {
  display: flex;
  padding: 8rpx;
  margin-bottom: $spacing-lg;
  background: rgb(0 0 0 / 3%);
  border-radius: $radius-md;
  gap: 8rpx;

  .switch-item {
    display: flex;
    justify-content: center;
    align-items: center;
    flex: 1;
    padding: $spacing-sm 0;
    font-size: $font-sm;
    color: $text-sub;
    border-radius: $radius-sm;
    transition: all 0.2s;

    &.active {
      color: $primary;
      background: #fff;
      font-weight: $font-semibold;
      box-shadow: 0 2rpx 8rpx rgb(0 0 0 / 5%);
    }
  }
}

// 加载中
// .loading-container 使用全局 components.scss 定义

// 推荐列表
.recommend-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.recommend-card {
  position: relative;
  overflow: hidden;
  padding: $spacing-lg;

  .rank-badge {
    position: absolute;
    top: 0;
    left: 0;
    display: flex;
    justify-content: center;
    align-items: center;
    width: 48rpx;
    height: 48rpx;
    font-size: $font-sm;
    color: #fff;
    background: #9ca3af;
    border-radius: 0 0 16rpx;
    font-weight: $font-bold;

    &.rank-gold {
      background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
    }

    &.rank-silver {
      background: linear-gradient(135deg, #d1d5db 0%, #9ca3af 100%);
    }

    &.rank-bronze {
      background: linear-gradient(135deg, #d97706 0%, #b45309 100%);
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    padding-left: 36rpx;
    margin-bottom: $spacing-md;

    .room-info {
      .room-name {
        margin-bottom: $spacing-xs;
        font-size: $font-lg;
        color: $text-main;
        font-weight: $font-bold;
      }

      .room-meta {
        font-size: $font-sm;
        color: $text-sub;
      }
    }

    .match-score {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: $spacing-sm $spacing-md;
      background: rgb(10 219 195 / 10%);
      border-radius: $radius-md;

      .score-value {
        font-size: 40rpx;
        color: $primary;
        font-weight: $font-bold;
        line-height: 1;
      }

      .score-label {
        margin-top: 4rpx;
        font-size: 20rpx;
        color: $text-sub;
      }
    }
  }

  .card-content {
    padding-bottom: $spacing-md;
    margin-bottom: $spacing-md;
    border-bottom: 1rpx solid rgb(0 0 0 / 5%);
  }

  .highlights {
    display: flex;
    flex-wrap: wrap;
    margin-bottom: $spacing-sm;
    gap: $spacing-sm;

    .highlight-tag {
      display: flex;
      align-items: center;
      padding: 6rpx 16rpx;
      background: rgb(16 185 129 / 8%);
      border-radius: 8rpx;
      gap: 6rpx;

      text {
        font-size: $font-xs;
        color: #059669;
      }
    }
  }

  .conflicts {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-sm;

    .conflict-tag {
      display: flex;
      align-items: center;
      padding: 6rpx 16rpx;
      background: rgb(245 158 11 / 8%);
      border-radius: 8rpx;
      gap: 6rpx;

      text {
        font-size: $font-xs;
        color: #d97706;
      }
    }
  }

  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .footer-info {
      .score-range {
        font-size: $font-xs;
        color: $text-disabled;
      }
    }

    .select-btn {
      display: flex;
      align-items: center;
      padding: $spacing-sm $spacing-md;
      background: rgb(10 219 195 / 10%);
      border-radius: $radius-sm;
      gap: 6rpx;
      transition: all 0.2s;

      text {
        font-size: $font-sm;
        color: $primary;
        font-weight: $font-semibold;
      }

      &:active {
        background: rgb(10 219 195 / 20%);
      }
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
    text-align: center;
    color: $text-sub;
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

// .safe-bottom 使用全局 components.scss 定义
</style>
