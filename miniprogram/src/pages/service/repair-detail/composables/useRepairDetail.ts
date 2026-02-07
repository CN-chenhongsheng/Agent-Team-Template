import { computed, ref } from 'vue';
import type { IRepair } from '@/types';
import { RepairStatus, RepairTypeText, UrgentLevelText } from '@/types';
import { cancelRepairAPI, getRepairDetailAPI, rateRepairAPI } from '@/api';

/**
 * 报修详情页逻辑
 */
export function useRepairDetail() {
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

  /**
   * 初始化页面
   */
  function initPage(options: any) {
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
  }

  return {
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
  };
}
