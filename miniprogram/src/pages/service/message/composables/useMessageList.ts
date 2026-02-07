/**
 * 消息列表数据管理逻辑
 */

import { computed, onMounted, ref } from 'vue';
import { ROUTE_CONSTANTS } from '@/constants';
import type { INotice, INoticeQueryParams } from '@/types';
import { NoticeType } from '@/types';
import { getNoticePageAPI, getUnreadNoticeCountAPI, markNoticeReadAPI } from '@/api';

export interface TabItem {
  label: string;
  value: 'all' | NoticeType;
  icon: string;
}

export function useMessageList() {
  const loading = ref(false);
  const refreshing = ref(false);
  const finished = ref(false);
  const list = ref<INotice[]>([]);
  const activeTab = ref<'all' | NoticeType>('all');
  const statusBarHeight = ref(0);
  const totalUnreadCount = ref(0);

  // 分页参数
  const pageNum = ref(1);
  const pageSize = 20;
  const total = ref(0);

  const tabs = ref<TabItem[]>([
    { label: '全部', value: 'all', icon: 'list' },
    { label: '系统', value: NoticeType.SYSTEM, icon: 'bell' },
    { label: '宿舍', value: NoticeType.DORM, icon: 'home' },
    { label: '安全', value: NoticeType.SAFETY, icon: 'warning' },
  ]);

  const INDICATOR_WIDTH_RPX = 50;

  // 计算未读消息数量
  const unreadCount = computed(() => {
    return totalUnreadCount.value;
  });

  // 计算今日消息数量
  const todayCount = computed(() => {
    const today = new Date().toISOString().split('T')[0];
    return list.value.filter((item) => {
      const itemDate = item.publishTime || item.createTime;
      return itemDate && itemDate.startsWith(today);
    }).length;
  });

  // 标签指示器样式
  const indicatorStyle = computed(() => {
    const activeIndex = tabs.value.findIndex(tab => tab.value === activeTab.value);
    if (activeIndex === -1) {
      return { width: '50rpx', left: '0rpx' };
    }

    const tabWidthPercent = 100 / tabs.value.length;
    const leftPercent = activeIndex * tabWidthPercent + tabWidthPercent / 2;

    return {
      width: `${INDICATOR_WIDTH_RPX}rpx`,
      left: `${leftPercent}%`,
      transform: 'translateX(-50%)',
    };
  });

  // 加载未读数量
  async function loadUnreadCount() {
    try {
      const count = await getUnreadNoticeCountAPI();
      totalUnreadCount.value = count;
    }
    catch (error) {
      console.error('获取未读数量失败', error);
      // 如果 API 不可用，回退到本地计算
      totalUnreadCount.value = list.value.filter(item => (item.readCount || 0) === 0).length;
    }
  }

  // 加载数据
  async function loadData(isRefresh = false): Promise<void> {
    if (loading.value) return;

    loading.value = true;
    try {
      const params: INoticeQueryParams = {
        pageNum: pageNum.value,
        pageSize,
        status: 1, // 只查询已发布的通知
      };

      // 根据选中的标签过滤
      if (activeTab.value !== 'all') {
        params.noticeType = activeTab.value as NoticeType;
      }

      const result = await getNoticePageAPI(params);

      if (isRefresh) {
        list.value = result.list || [];
      }
      else {
        list.value = [...list.value, ...(result.list || [])];
      }

      total.value = result.total || 0;

      // 判断是否加载完毕
      finished.value = list.value.length >= total.value;
    }
    catch (error) {
      console.error('加载通知列表失败', error);

      // 如果是首次加载失败，清空列表
      if (isRefresh || list.value.length === 0) {
        list.value = [];
        total.value = 0;
      }

      uni.showToast({
        title: '加载失败，请稍后重试',
        icon: 'none',
        duration: 2000,
      });
    }
    finally {
      loading.value = false;
      refreshing.value = false;
    }
  }

  // 处理标签切换
  function handleTabChange(value: 'all' | NoticeType): void {
    activeTab.value = value;
    pageNum.value = 1;
    list.value = [];
    finished.value = false;
    loadData();
  }

  // 查看详情
  async function handleViewDetail(item: INotice) {
    // 标记为已读
    if (item.readCount === 0) {
      try {
        await markNoticeReadAPI(item.id!);
        // 更新本地已读状态
        item.readCount = 1;
        // 刷新未读数量
        await loadUnreadCount();
      }
      catch (error) {
        console.error('标记已读失败', error);
      }
    }

    // 跳转详情页
    uni.navigateTo({
      url: `${ROUTE_CONSTANTS.NOTICE_DETAIL}?id=${item.id}`,
    });
  }

  // 下拉刷新
  async function onRefresh() {
    refreshing.value = true;
    pageNum.value = 1;
    finished.value = false;
    await Promise.all([
      loadData(true),
      loadUnreadCount(),
    ]);
  }

  // 上拉加载
  async function onLoadMore() {
    if (finished.value || loading.value) return;

    pageNum.value += 1;
    await loadData();
  }

  onMounted(() => {
    // 获取状态栏高度
    const systemInfo = uni.getSystemInfoSync();
    statusBarHeight.value = systemInfo.statusBarHeight || 0;

    // 加载数据
    loadData(true);
    loadUnreadCount();
  });

  return {
    // State
    loading,
    refreshing,
    finished,
    list,
    activeTab,
    statusBarHeight,
    tabs,
    // Computed
    unreadCount,
    todayCount,
    indicatorStyle,
    // Methods
    handleTabChange,
    handleViewDetail,
    onRefresh,
    onLoadMore,
  };
}
