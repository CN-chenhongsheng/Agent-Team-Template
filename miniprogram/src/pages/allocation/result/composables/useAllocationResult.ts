import { computed, onMounted, ref } from 'vue';
import { getMyAllocationResultAPI, getRoommatesInfoAPI } from '@/api/allocation';
import type { IAllocationResult, IRoommateInfo } from '@/types/api';

type AllocationStatus = 'pending' | 'allocated' | 'confirmed';

export function useAllocationResult() {
  // 加载状态
  const loading = ref(true);

  // 分配结果
  const allocationResult = ref<IAllocationResult | null>(null);

  // 室友列表
  const roommates = ref<IRoommateInfo[]>([]);

  // 分配状态
  const allocationStatus = computed<AllocationStatus>(() => {
    if (!allocationResult.value) return 'pending';
    if (allocationResult.value.status === 1) return 'confirmed';
    return 'allocated';
  });

  // 状态相关计算属性
  const statusClass = computed(() => {
    const classMap: Record<AllocationStatus, string> = {
      pending: 'status-pending',
      allocated: 'status-allocated',
      confirmed: 'status-confirmed',
    };
    return classMap[allocationStatus.value];
  });

  const statusIcon = computed(() => {
    const iconMap: Record<AllocationStatus, string> = {
      pending: 'clock',
      allocated: 'checkmark-circle',
      confirmed: 'checkmark-circle-fill',
    };
    return iconMap[allocationStatus.value];
  });

  const statusColor = computed(() => {
    const colorMap: Record<AllocationStatus, string> = {
      pending: '#f59e0b',
      allocated: '#0adbc3',
      confirmed: '#10b981',
    };
    return colorMap[allocationStatus.value];
  });

  const statusTitle = computed(() => {
    const titleMap: Record<AllocationStatus, string> = {
      pending: '等待分配中',
      allocated: '已完成分配',
      confirmed: '分配已确认',
    };
    return titleMap[allocationStatus.value];
  });

  const statusDesc = computed(() => {
    const descMap: Record<AllocationStatus, string> = {
      pending: '系统正在为您智能匹配最合适的室友，请耐心等待',
      allocated: '您的宿舍已分配完成，请查看详细信息',
      confirmed: '您已确认分配结果，祝您入住愉快！',
    };
    return descMap[allocationStatus.value];
  });

  // 加载数据
  async function loadData(): Promise<void> {
    loading.value = true;
    try {
      // 并行请求分配结果和室友信息
      const [resultData, roommatesData] = await Promise.all([
        getMyAllocationResultAPI(),
        getRoommatesInfoAPI(),
      ]);

      allocationResult.value = resultData || null;
      roommates.value = roommatesData || [];
    }
    catch (error) {
      console.error('加载分配结果失败:', error);
      allocationResult.value = null;
      roommates.value = [];
    }
    finally {
      loading.value = false;
    }
  }

  // 跳转填写问卷
  function handleGoSurvey(): void {
    uni.navigateTo({ url: '/pages/allocation/survey/index' });
  }

  onMounted(() => {
    loadData();
  });

  return {
    loading,
    allocationResult,
    roommates,
    statusClass,
    statusIcon,
    statusColor,
    statusTitle,
    statusDesc,
    handleGoSurvey,
  };
}
