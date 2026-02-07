<template>
  <view class="home-page">
    <!-- 背景装饰球 -->
    <view class="ambient-blob blob-primary" />
    <view class="ambient-blob blob-accent" />
    <view class="ambient-blob blob-blue" />

    <!-- 顶部导航 -->
    <view class="header">
      <view class="header-left">
        <view class="date-text">
          {{ currentDate }}
        </view>
        <!-- 未登录状态 -->
        <view v-if="!isLoggedIn" class="header-unlogged">
          <view class="greeting-text">
            欢迎来到智慧宿舍
          </view>
          <view class="login-btn" @click="handleGoLogin">
            <text>点击登录</text>
            <u-icon name="arrow-right" size="14" color="#0adbc3" />
          </view>
        </view>
        <!-- 已登录状态 -->
        <view v-else class="greeting-text">
          {{ greeting }}，{{ userInfo?.nickname || '同学' }}
        </view>
      </view>
    </view>

    <!-- 学生端首页 -->
    <view v-if="isStudent || !isLoggedIn" class="student-home">
      <!-- 用户信息卡片（仅登录后显示） -->
      <view v-if="isLoggedIn" class="glass-card user-card">
        <view class="user-card-content">
          <image class="avatar" :src="userInfo?.avatar || defaultAvatar" mode="aspectFill" />
          <view class="user-info">
            <view class="user-name">
              {{ userInfo?.nickname || '未登录' }}
            </view>
            <view class="student-no">
              学号: {{ userInfo?.studentInfo?.studentNo || '-' }}
            </view>
            <view v-if="dormInfo !== '未分配'" class="dorm-tag">
              <u-icon name="home" size="14" color="#009688" />
              <text>{{ dormInfo }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 通知公告 -->
      <view class="section notice-section">
        <view class="section-header">
          <view class="section-title-wrapper">
            <view class="section-title">
              通知公告
            </view>
            <view v-if="unreadNoticeCount > 0" class="unread-badge">
              {{ unreadNoticeCount > 99 ? '99+' : unreadNoticeCount }}
            </view>
          </view>
          <view class="section-more" @click="handleGoNoticeList">
            查看全部
          </view>
        </view>
        <scroll-view class="notice-scroll" scroll-x enable-flex>
          <view
            v-for="notice in noticeList"
            :key="notice.id"
            class="glass-card notice-card"
            @click="handleViewNotice(notice)"
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

      <!-- 快捷服务 -->
      <view class="section">
        <view class="section-title">
          快捷服务
        </view>
        <view class="service-grid">
          <view
            v-for="item in quickServices"
            :key="item.id"
            class="glass-card service-item"
            @click="handleQuickEntry(item)"
          >
            <view class="service-gradient" :style="getGradientStyle(item.color)" />
            <view class="service-bg-icon" :style="{ color: item.color }">
              <u-icon :name="item.icon" size="100" :color="item.color" />
            </view>
            <view class="service-content">
              <u-icon :name="item.icon" :size="32" :color="item.color" />
              <view class="service-name">
                {{ item.name }}
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 水电统计 -->
      <view class="section">
        <view class="section-title">
          水电统计
        </view>
        <view class="utility-stats">
          <!-- 用电统计 -->
          <view class="glass-card utility-card">
            <view class="utility-bg-blob utility-bg-electric" />
            <view class="utility-header">
              <view class="utility-info">
                <view class="utility-title-row">
                  <view class="utility-icon-wrapper utility-icon-electric">
                    <u-icon name="grid" size="18" color="#0adbc3" />
                  </view>
                  <text class="utility-title">
                    本年用电
                  </text>
                </view>
                <view class="utility-value-row">
                  <text class="utility-value">
                    {{ electricityData.value }}
                  </text>
                  <text class="utility-unit">
                    kWh
                  </text>
                </view>
              </view>
              <view class="utility-trend" :class="electricityData.trendClass">
                <u-icon :name="electricityData.trendIcon" size="14" :color="electricityData.trendColor" />
                <text>
                  {{ electricityData.trendText }}
                </text>
              </view>
            </view>
            <view class="utility-chart">
              <view class="chart-container">
                <qiun-ucharts
                  type="line"
                  :opts="electricityChartOpts"
                  :chart-data="electricityChartData"
                  canvas-id="electricity-chart"
                />
              </view>
            </view>
          </view>

          <!-- 用水统计 -->
          <view class="glass-card utility-card">
            <view class="utility-bg-blob utility-bg-water" />
            <view class="utility-header">
              <view class="utility-info">
                <view class="utility-title-row">
                  <view class="utility-icon-wrapper utility-icon-water">
                    <u-icon name="grid" size="18" color="#60a5fa" />
                  </view>
                  <text class="utility-title">
                    本年用水
                  </text>
                </view>
                <view class="utility-value-row">
                  <text class="utility-value">
                    {{ waterData.value }}
                  </text>
                  <text class="utility-unit">
                    m³
                  </text>
                </view>
              </view>
              <view class="utility-trend" :class="waterData.trendClass">
                <u-icon :name="waterData.trendIcon" size="14" :color="waterData.trendColor" />
                <text>
                  {{ waterData.trendText }}
                </text>
              </view>
            </view>
            <view class="utility-chart">
              <view class="chart-container">
                <qiun-ucharts
                  type="line"
                  :opts="waterChartOpts"
                  :chart-data="waterChartData"
                  canvas-id="water-chart"
                />
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 我的申请进度（仅登录后显示） -->
      <view v-if="isLoggedIn" class="section">
        <view class="section-header">
          <view class="section-title">
            我的申请进度
          </view>
          <view class="filter-btn" @click="handleGoApplyList">
            <u-icon name="list" size="20" color="#94a3b8" />
          </view>
        </view>
        <view v-if="applyList.length === 0" class="empty-apply">
          <u-empty mode="list" text="暂无申请记录" icon-size="120" />
        </view>
        <view v-else class="apply-list">
          <view
            v-for="item in applyList.slice(0, 4)"
            :key="item.id"
            class="glass-card apply-item"
            @click="handleViewApply(item)"
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
    </view>

    <!-- 底部安全区域 -->
    <view class="safe-bottom" />

    <!-- 隐私协议组件 -->
    <!-- #ifdef MP-WEIXIN -->
    <agree-privacy v-model="showAgreePrivacy" :disable-check-privacy="false" @agree="handleAgree" />
    <!-- #endif -->
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { storeToRefs } from 'pinia';
import type { IApplyDisplay, INoticeDisplay, IQuickService } from '@/types';
import useUserStore from '@/store/modules/user';
import { ROUTE_CONSTANTS } from '@/constants';
import { USE_MOCK } from '@/mock';
import { getCheckInPageAPI } from '@/api/accommodation/check-in';
import { getTransferPageAPI } from '@/api/accommodation/transfer';
import { getCheckOutPageAPI } from '@/api/accommodation/check-out';
import { getStayPageAPI } from '@/api/accommodation/stay';
import { getMyAppliesAPI, getStudentHomeStatisticsAPI } from '@/api';
import {
  transformCheckInToDisplay,
  transformCheckOutToDisplay,
  transformMyApplyToDisplay,
  transformStayToDisplay,
  transformTransferToDisplay,
} from '@/utils/apply-transform';
import { useNotice } from '@/hooks/use-notice';

const userStore = useUserStore();
const { userInfo, isLoggedIn } = storeToRefs(userStore);
const { unreadCount: unreadNoticeCount, loadUnreadCount } = useNotice();

const defaultAvatar = 'https://via.placeholder.com/150';
const showAgreePrivacy = ref(false);

// 是否是学生
const isStudent = computed(() => userInfo.value?.role === 'student');

// 宿舍信息
const dormInfo = computed(() => {
  const student = userInfo.value?.studentInfo;
  if (!student || !student.floorCode || !student.roomCode) {
    return '未分配';
  }
  return `${student.floorCode} ${student.roomCode}室`;
});

// 当前日期
const currentDate = computed(() => {
  const now = new Date();
  const weekDays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'];
  return `${now.getMonth() + 1}月${now.getDate()}日 ${weekDays[now.getDay()]}`;
});

// 问候语
const greeting = computed((): string => {
  const hour = new Date().getHours();
  if (hour < 6) {
    return '凌晨好';
  }
  else if (hour < 9) {
    return '早上好';
  }
  else if (hour < 12) {
    return '上午好';
  }
  else if (hour < 14) {
    return '中午好';
  }
  else if (hour < 17) {
    return '下午好';
  }
  else if (hour < 19) {
    return '傍晚好';
  }
  else {
    return '晚上好';
  }
});

// 快捷服务（学生端）
const quickServices = ref<IQuickService[]>([
  { id: 1, name: '入住申请', icon: 'home', color: '#14b8a6', type: 'checkIn', path: '/pages/apply/form/index' },
  { id: 2, name: '调宿申请', icon: 'reload', color: '#6366f1', type: 'transfer', path: '/pages/apply/form/index' },
  { id: 3, name: '退宿申请', icon: 'arrow-left', color: '#f43f5e', type: 'checkOut', path: '/pages/apply/form/index' },
  { id: 4, name: '留宿申请', icon: 'calendar', color: '#3b82f6', type: 'stay', path: '/pages/apply/form/index' },
  { id: 5, name: '故障报修', icon: 'setting', color: '#f97316', type: 'repair', path: '/pages/apply/form/index' },
  { id: 6, name: '智能分配', icon: 'grid', color: '#8b5cf6', type: 'allocation', path: '/pages/allocation/survey/index' },
]);

// 通知列表
const noticeList = ref<INoticeDisplay[]>([]);

// 申请列表
const applyList = ref<IApplyDisplay[]>([]);

// 水电统计数据
const electricityData = ref({
  value: '1542.5',
  trend: -5,
  trendText: '-5% 同比',
  trendClass: 'trend-down',
  trendIcon: 'arrow-down',
  trendColor: '#22c55e',
});

const waterData = ref({
  value: '145.2',
  trend: 2,
  trendText: '+2% 同比',
  trendClass: 'trend-up',
  trendIcon: 'arrow-up',
  trendColor: '#ef4444',
});

// 用电图表配置
const electricityChartOpts = ref({
  color: ['#0adbc3'],
  padding: [15, 2, 0, 2], // 增加顶部 padding，为数据标签留出空间
  enableScroll: false,
  legend: {
    show: false,
  },
  xAxis: {
    disableGrid: true,
  },
  yAxis: {
    gridType: 'dash',
    dashLength: 2,
    // 手动设置 Y 轴范围，确保图表完整显示
    dataMin: 100, // 数据最小值
    dataMax: 200, // 数据最大值
    splitNumber: 5, // 分割数
  },
  extra: {
    line: {
      type: 'curve',
      width: 3,
      activeType: 'hollow',
      activeRadius: 6,
    },
  },
});

// 用电图表数据
const electricityChartData = computed(() => ({
  categories: ['1月', '4月', '8月', '12月'],
  series: [
    {
      name: '用电量',
      data: [120, 180, 140, 160],
    },
  ],
}));

// 用水图表配置
const waterChartOpts = ref({
  color: ['#60a5fa'],
  padding: [15, 2, 0, 2], // 增加顶部 padding，为数据标签留出空间
  enableScroll: false,
  legend: {
    show: false,
  },
  xAxis: {
    disableGrid: true,
  },
  yAxis: {
    gridType: 'dash',
    dashLength: 2,
  },
  extra: {
    line: {
      type: 'curve',
      width: 3,
      activeType: 'hollow',
    },
  },
});

// 用水图表数据
const waterChartData = computed(() => ({
  categories: ['1月', '4月', '8月', '12月'],
  series: [
    {
      name: '用水量',
      data: [110, 95, 85, 75],
    },
  ],
}));

// 获取渐变背景样式
function getGradientStyle(color: string): { background: string } {
  // 将十六进制颜色转换为 rgba，透明度为 0.05
  const hex = color.replace('#', '');
  const r = Number.parseInt(hex.substring(0, 2), 16);
  const g = Number.parseInt(hex.substring(2, 4), 16);
  const b = Number.parseInt(hex.substring(4, 6), 16);
  return {
    background: `linear-gradient(to bottom right, rgba(${r}, ${g}, ${b}, 0.05), transparent)`,
  };
}

// 快捷入口点击
function handleQuickEntry(item: IQuickService): void {
  // 检查登录状态（个人情况和智能分配需要登录）
  if ((item.type === 'habits' || item.type === 'allocation') && !isLoggedIn.value) {
    uni.showModal({
      title: '提示',
      content: item.type === 'allocation'
        ? '请先登录后再使用智能分配功能'
        : '请先登录后再查看和完善个人情况',
      confirmText: '去登录',
      cancelText: '取消',
      success: (res) => {
        if (res.confirm) {
          uni.navigateTo({ url: ROUTE_CONSTANTS.LOGIN });
        }
      },
    });
    return;
  }

  // 智能分配入口 - 直接跳转问卷页面
  if (item.type === 'allocation') {
    uni.navigateTo({ url: item.path });
    return;
  }

  // 其他业务类型跳转到form页面，传递type参数
  if (item.type && item.path === '/pages/apply/form/index') {
    uni.navigateTo({ url: `${item.path}?type=${item.type}` });
  }
  else {
    uni.navigateTo({ url: item.path });
  }
}

// 跳转通知列表
function handleGoNoticeList(): void {
  uni.navigateTo({ url: ROUTE_CONSTANTS.MESSAGE });
}

// 查看通知详情
function handleViewNotice(notice: INoticeDisplay): void {
  uni.navigateTo({ url: `${ROUTE_CONSTANTS.NOTICE_DETAIL}?id=${notice.id}` });
}

// 跳转申请列表
function handleGoApplyList(): void {
  uni.switchTab({ url: '/pages/tab/apply/index' });
}

// 查看申请详情
function handleViewApply(item: IApplyDisplay): void {
  uni.navigateTo({ url: `${ROUTE_CONSTANTS.STUDENT_APPLY_DETAIL}?id=${item.id}&type=${item.type}` });
}

// 同意隐私协议
function handleAgree(): void {
}

function handleGoLogin(): void {
  uni.navigateTo({ url: ROUTE_CONSTANTS.LOGIN });
}

// 加载数据
async function loadData(): Promise<void> {
  try {
    // 1. 通知列表 - 暂时使用写死的数据（阶段二再接入）
    noticeList.value = [
      {
        id: 1,
        title: '宿舍停水通知',
        desc: '因管道维修，明日9:00-17:00将暂停供水。',
        tag: '紧急',
        tagClass: 'tag-urgent',
        date: '10-23',
        icon: 'warning',
      },
      {
        id: 2,
        title: '宿舍文化节报名',
        desc: '本周五截止报名，欢迎各寝室踊跃参加！',
        tag: '活动',
        tagClass: 'tag-activity',
        date: '10-21',
        icon: 'bell',
      },
      {
        id: 3,
        title: '消防演练通知',
        desc: '本周六下午进行消防演练，请同学们积极配合。',
        tag: '通知',
        tagClass: 'tag-notice',
        date: '10-20',
        icon: 'info-circle',
      },
    ];

    // 2. 申请列表 - 根据 USE_MOCK 决定使用 Mock 数据或真实 API
    if (USE_MOCK) {
      applyList.value = [
        {
          id: 1,
          type: 'repair',
          typeName: '洗手台报修',
          icon: 'setting',
          iconColor: '#f97316',
          bgColor: 'rgba(249, 115, 22, 0.1)',
          statusText: '处理中',
          statusClass: 'status-processing',
          applyDate: '01-07 14:30',
        },
        {
          id: 2,
          type: 'transfer',
          typeName: '调宿申请',
          icon: 'reload',
          iconColor: '#6366f1',
          bgColor: 'rgba(99, 102, 241, 0.1)',
          statusText: '审核中',
          statusClass: 'status-pending',
          applyDate: '01-05 10:20',
        },
        {
          id: 3,
          type: 'check-in',
          typeName: '入住申请',
          icon: 'checkmark-circle',
          iconColor: '#22c55e',
          bgColor: 'rgba(34, 197, 94, 0.1)',
          statusText: '已通过',
          statusClass: 'status-approved',
          applyDate: '12-25',
        },
        {
          id: 4,
          type: 'stay',
          typeName: '留宿申请',
          icon: 'calendar',
          iconColor: '#3b82f6',
          bgColor: 'rgba(59, 130, 246, 0.1)',
          statusText: '已通过',
          statusClass: 'status-approved',
          applyDate: '12-20',
        },
        {
          id: 5,
          type: 'check-out',
          typeName: '退宿申请',
          icon: 'arrow-left',
          iconColor: '#f43f5e',
          bgColor: 'rgba(244, 63, 94, 0.1)',
          statusText: '审核中',
          statusClass: 'status-pending',
          applyDate: '01-03 16:45',
        },
        {
          id: 6,
          type: 'repair',
          typeName: '门锁维修',
          icon: 'setting',
          iconColor: '#f97316',
          bgColor: 'rgba(249, 115, 22, 0.1)',
          statusText: '已完成',
          statusClass: 'status-approved',
          applyDate: '12-28',
        },
        {
          id: 7,
          type: 'transfer',
          typeName: '调宿申请',
          icon: 'reload',
          iconColor: '#ef4444',
          bgColor: 'rgba(239, 68, 68, 0.1)',
          statusText: '已拒绝',
          statusClass: 'status-rejected',
          applyDate: '12-15',
        },
        {
          id: 8,
          type: 'repair',
          typeName: '空调故障',
          icon: 'setting',
          iconColor: '#f97316',
          bgColor: 'rgba(249, 115, 22, 0.1)',
          statusText: '处理中',
          statusClass: 'status-processing',
          applyDate: '01-06 09:15',
        },
      ];
    }
    else {
      // 调用真实API加载申请列表（使用统一的我的申请接口）
      await loadApplyListFromAPI();
    }
  }
  catch (error) {
    console.error('加载首页数据失败:', error);
  }
}

// 从API加载申请列表
async function loadApplyListFromAPI(): Promise<void> {
  try {
    // 使用统一的我的申请API，限制返回最近4条
    const result = await getMyAppliesAPI({ limit: 4 });

    // 转换为展示格式
    applyList.value = result.list.map(transformMyApplyToDisplay);

    console.log('首页申请列表加载成功:', applyList.value);
  }
  catch (error) {
    console.error('从API加载申请列表失败:', error);
    // 降级方案：使用空数组
    applyList.value = [];
  }
}

onMounted(async () => {
  // 检查登录状态（如果有token会自动获取用户信息）
  await userStore.checkLoginStatus();
  // 加载页面数据
  loadData();
  // 加载未读通知数量
  if (isLoggedIn.value) {
    loadUnreadCount();
  }
});
</script>

<style lang="scss" scoped>
// 导入公共样式变量

.home-page {
  position: relative;
  overflow: hidden;
  padding-bottom: env(safe-area-inset-bottom);
  min-height: 100vh;
  background-color: $bg-light;
}

// .ambient-blob 使用全局 components.scss 定义

// 顶部导航
.header {
  position: relative;
  z-index: 10;
  align-items: flex-start;
  padding: calc(var(--status-bar-height) + 45rpx) $spacing-lg 25rpx;
  margin-bottom: 10rpx;

  .header-left {
    .date-text {
      font-size: $font-sm;
      color: $text-sub;
      font-weight: $font-medium;
    }

    .greeting-text {
      margin-top: $spacing-xs;
      font-size: $font-2xl;
      color: $text-main;
      font-weight: $font-bold;
      line-height: 1.2;
    }

    .header-unlogged {
      margin-top: $spacing-xs;

      .greeting-text {
        margin-top: 0;
        margin-bottom: 12rpx;
      }

      .login-btn {
        display: inline-flex;
        align-items: center;
        padding: $spacing-xs $spacing-sm;
        font-size: $font-sm;
        color: $primary;
        background: rgb(10 219 195 / 10%);
        border-radius: $radius-full;
        transition: $transition-fast;
        gap: 4rpx;
        font-weight: $font-bold;

        &:active {
          background: rgb(10 219 195 / 20%);
        }
      }
    }
  }
}

// 学生端首页
.student-home {
  position: relative;
  z-index: 10;
  padding: 0 $spacing-lg;
}

// 用户卡片
.user-card {
  padding: $spacing-lg;
  margin-bottom: $spacing-lg;

  .user-card-content {
    display: flex;
    align-items: center;
    gap: $spacing-md;
  }

  .avatar {
    width: 120rpx;
    height: 120rpx;
    background: #e2e8f0;
    border: 4rpx solid $glass-border;
    border-radius: 50%;
    flex-shrink: 0;
  }

  .user-info {
    flex: 1;

    .user-name {
      font-size: $font-xl;
      font-weight: $font-bold;
      color: $text-main;
      line-height: 1.2;
    }

    .student-no {
      margin-top: $spacing-xs;
      font-size: $font-sm;
      color: $text-sub;
      font-weight: $font-medium;
    }

    .dorm-tag {
      display: inline-flex;
      align-items: center;
      padding: $spacing-xs 20rpx;
      margin-top: $spacing-sm;
      font-size: $font-sm;
      color: $primary-dark;
      background: rgb(10 219 195 / 10%);
      border-radius: 12rpx;
      gap: $spacing-xs;
      font-weight: $font-semibold;
    }
  }
}

// 区块样式
.section {
  margin-bottom: $spacing-lg;
}

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
  margin-bottom: 20rpx;
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

.filter-btn {
  padding: $spacing-xs;
}

// 通知公告区域
.notice-section {
  padding-right: $spacing-lg;
  padding-left: $spacing-lg;
  margin-right: -$spacing-lg;
  margin-bottom: 0;
  margin-left: -$spacing-lg;

  .section-header {
    padding-right: 0;
  }
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

// 快捷服务
.service-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
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

    :deep(.u-icon) {
      color: inherit !important;
    }

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
    font-size: $font-sm;
    font-weight: $font-bold;
    color: #334155;
  }
}

// 申请列表
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

// 水电统计
.utility-stats {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.utility-card {
  position: relative;
  overflow: visible;
  padding: $spacing-lg;
}

.utility-bg-blob {
  position: absolute;
  top: -96rpx;
  right: -96rpx;
  z-index: 0;
  width: 320rpx;
  height: 320rpx;
  border-radius: 50%;
  filter: blur(64rpx);
  pointer-events: none;

  &.utility-bg-electric {
    background: rgb(10 219 195 / 10%);
  }

  &.utility-bg-water {
    background: rgb(96 165 250 / 10%);
  }
}

.utility-header {
  position: relative;
  z-index: 10;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: $spacing-md;
}

.utility-info {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.utility-title-row {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.utility-icon-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 12rpx;
  border-radius: 50%;

  &.utility-icon-electric {
    background: rgb(10 219 195 / 10%);
  }

  &.utility-icon-water {
    background: rgb(96 165 250 / 10%);
  }
}

.utility-title {
  font-size: $font-lg;
  font-weight: $font-bold;
  color: $text-main;
  letter-spacing: 0.5rpx;
}

.utility-value-row {
  display: flex;
  align-items: baseline;
  gap: $spacing-xs;
  margin-top: $spacing-sm;
}

.utility-value {
  font-size: 90rpx;
  line-height: 1;
  font-weight: $font-bold;
  color: $text-main;
  letter-spacing: -2rpx;
}

.utility-unit {
  font-size: $font-md;
  color: $text-sub;
  font-weight: $font-medium;
  transform: translateY(-4rpx);
}

.utility-trend {
  display: flex;
  align-items: center;
  padding: 12rpx 20rpx;
  font-size: $font-sm;
  border-radius: $radius-sm;
  gap: $spacing-xs;
  font-weight: $font-semibold;

  &.trend-down {
    color: #22c55e;
    background: rgb(34 197 94 / 10%);
    border: 2rpx solid rgb(34 197 94 / 20%);
  }

  &.trend-up {
    color: #ef4444;
    background: rgb(239 68 68 / 10%);
    border: 2rpx solid rgb(239 68 68 / 20%);
  }
}

.utility-chart {
  position: relative;
  z-index: 10;
  overflow: visible;
  margin-top: $spacing-md;
  margin-bottom: $spacing-xs;
  width: 100%;
  height: 240rpx;
}

.chart-container {
  position: relative;
  overflow: visible;
  width: 100%;
  height: 100%;
}
</style>
