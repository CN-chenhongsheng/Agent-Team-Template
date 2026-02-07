import { ref } from 'vue';
import type { IRepair, IRepairQueryParams } from '@/types';
import { RepairStatus, RepairTypeText, UrgentLevelText } from '@/types';
import { cancelRepairAPI, getRepairListAPI, rateRepairAPI } from '@/api';

/**
 * 报修列表页逻辑
 */
export function useRepairList() {
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

  return {
    statusTabs,
    currentStatusIndex,
    repairList,
    pagingRef,
    ratingPopupVisible,
    ratingForm,
    loadRepairList,
    handleStatusChange,
    handleItemClick,
    handleCancel,
    handleRate,
    submitRating,
    getRepairTypeText,
    getUrgentLevelText,
    getStatusText,
    formatTime,
    showActions,
    canCancel,
    canRate,
  };
}
