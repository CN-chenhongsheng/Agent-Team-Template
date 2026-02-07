import { computed, onMounted, ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import type { IApplyListItem } from '@/types';
import { ApplyStatus } from '@/types';
import useUserStore from '@/store/modules/user';
import {
  getApplyTypeIcon,
  getApplyTypeName,
  getStatusColor,
  getStatusText,
} from '@/utils/apply';
import { extractDate } from '@/utils/date';
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

/**
 * 我的申请列表页逻辑
 */
export function useApplyList() {
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

  return {
    tabs,
    activeTab,
    loading,
    list,
    statusBarHeight,
    indicatorStyle,
    handleTabChange,
    handleViewDetail,
    getApplyTypeIcon,
    getApplyTypeName,
    getStatusColor,
    getStatusText,
    extractDate,
  };
}
