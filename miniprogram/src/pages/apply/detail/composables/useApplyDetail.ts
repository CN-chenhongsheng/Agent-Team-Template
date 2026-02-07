/**
 * 申请详情数据逻辑
 */

import { computed, onMounted, ref } from 'vue';
import { getApplyTypeName, getStatusText } from '@/utils/apply';
import { cancelCheckInAPI } from '@/api/accommodation/check-in';
import { cancelCheckOutAPI } from '@/api/accommodation/check-out';
import { cancelTransferAPI } from '@/api/accommodation/transfer';
import { cancelStayAPI } from '@/api/accommodation/stay';
import { getApprovalInstanceByBusinessAPI } from '@/api/approval';
import { getApplyDetailAPI } from '@/api/statistics';
import type { IApplyDetail, IApprovalInstance } from '@/types/api';

export function useApplyDetail() {
  const defaultAvatar = 'https://via.placeholder.com/150';
  const detail = ref<IApplyDetail | null>(null);
  const applyType = ref('');
  const applyId = ref(0);
  const approvalInstance = ref<IApprovalInstance | null>(null);

  const studentInfo = computed(() => ({
    avatar: '',
    name: '学生',
    grade: '',
    department: '',
    level: '',
  }));

  const applyNo = computed(() => {
    return detail.value?.id ? String(detail.value.id).padStart(10, '0') : '0000000000';
  });

  const applyTypeName = computed(() => {
    return detail.value?.applyTypeText || getApplyTypeName(applyType.value);
  });

  const statusText = computed(() => {
    if (!detail.value) return '审核中';
    return detail.value.statusText || getStatusText(detail.value.status);
  });

  const statusDesc = computed(() => {
    if (statusText.value === '审核中') return '预计24小时内完成宿管确认';
    if (statusText.value === '已通过') return '申请已通过，请等待后续通知';
    if (statusText.value === '已拒绝') return '申请已被拒绝';
    if (statusText.value === '已完成') return '申请已完成';
    return '';
  });

  const progressSteps = computed(() => {
    const records = approvalInstance.value?.records || [];
    const nodes = approvalInstance.value?.nodes || [];

    const steps: any[] = [
      {
        title: '提交申请',
        time: detail.value?.applyDate || approvalInstance.value?.startTime || '--',
        desc: '申请已成功提交至系统',
        completed: true,
      },
    ];

    nodes.forEach((node) => {
      const record = records.find(r => r.nodeId === node.id);
      steps.push({
        nodeId: node.id,
        title: node.nodeName,
        time: record?.approveTime || '',
        desc: record ? (record.action === 1 ? '审批通过' : '审批拒绝') : '等待审批',
        completed: !!record,
        reviewer: record ? { avatar: 'https://via.placeholder.com/40', name: record.approverName } : null,
        reviewReason: record?.opinion || '',
        action: record?.action,
      });
    });

    steps.push({
      title: '完成',
      time: approvalInstance.value?.endTime || '--',
      desc: '流程结束',
      completed: approvalInstance.value?.status === 2,
    });

    return steps;
  });

  const currentStep = computed(() => {
    if (!approvalInstance.value) return 0;
    const records = approvalInstance.value.records || [];
    const nodes = approvalInstance.value.nodes || [];
    const status = approvalInstance.value.status;

    if (status === 2) return progressSteps.value.length - 1;

    for (let i = 0; i < nodes.length; i++) {
      const hasRecord = records.some(r => r.nodeId === nodes[i].id);
      if (!hasRecord) return i + 1;
    }
    return progressSteps.value.length - 1;
  });

  const canCancel = computed(() => {
    if (!detail.value) return false;
    return detail.value.status === 1;
  });

  const detailFields = computed(() => {
    if (!detail.value) return [];
    const fields: Array<{ label: string; value: string; icon: string }> = [];

    if (applyType.value === 'check-in' || applyType.value === 'check_in') {
      if (detail.value.checkInTypeText) fields.push({ label: '入住类型', value: detail.value.checkInTypeText, icon: 'home-fill' });
      if (detail.value.checkInDate) fields.push({ label: '入住日期', value: detail.value.checkInDate, icon: 'calendar-fill' });
      if (detail.value.expectedCheckOutDate) fields.push({ label: '预计退宿日期', value: detail.value.expectedCheckOutDate, icon: 'calendar' });
    }

    if (applyType.value === 'transfer') {
      if (detail.value.originalDormAddress) fields.push({ label: '原宿舍', value: detail.value.originalDormAddress, icon: 'home' });
      if (detail.value.targetDormAddress) fields.push({ label: '目标宿舍', value: detail.value.targetDormAddress, icon: 'home-fill' });
      if (detail.value.transferDate) fields.push({ label: '调宿日期', value: detail.value.transferDate, icon: 'calendar-fill' });
    }

    if (applyType.value === 'check-out' || applyType.value === 'check_out') {
      if (detail.value.checkOutDate) fields.push({ label: '退宿日期', value: detail.value.checkOutDate, icon: 'calendar-fill' });
    }

    if (applyType.value === 'stay') {
      if (detail.value.stayStartDate) fields.push({ label: '留宿开始日期', value: detail.value.stayStartDate, icon: 'calendar-fill' });
      if (detail.value.stayEndDate) fields.push({ label: '留宿结束日期', value: detail.value.stayEndDate, icon: 'calendar' });
      if (detail.value.parentName) fields.push({ label: '家长姓名', value: detail.value.parentName, icon: 'account-fill' });
      if (detail.value.parentPhone) fields.push({ label: '家长电话', value: detail.value.parentPhone, icon: 'phone-fill' });
      if (detail.value.parentAgreeText) fields.push({ label: '家长是否同意', value: detail.value.parentAgreeText, icon: 'checkmark-circle-fill' });
    }

    if (detail.value.reason) fields.push({ label: '申请理由', value: detail.value.reason, icon: 'edit-pen-fill' });
    return fields;
  });

  async function handleCancel() {
    if (!canCancel.value) {
      uni.showToast({ title: '当前状态不可撤回', icon: 'none' });
      return;
    }
    uni.showModal({
      title: '确认撤回',
      content: '确定要撤回此申请吗？撤回后需要重新提交。',
      success: async (res) => {
        if (res.confirm) {
          uni.showLoading({ title: '撤回中...' });
          try {
            const cancelAPIs: Record<string, (id: number) => Promise<any>> = {
              'check-in': cancelCheckInAPI,
              'check_in': cancelCheckInAPI,
              'check-out': cancelCheckOutAPI,
              'check_out': cancelCheckOutAPI,
              'transfer': cancelTransferAPI,
              'stay': cancelStayAPI,
            };
            const cancelFn = cancelAPIs[applyType.value];
            if (cancelFn) await cancelFn(applyId.value);
            uni.hideLoading();
            uni.showToast({ title: '撤回成功', icon: 'success' });
            setTimeout(() => uni.navigateBack(), 1500);
          }
          catch (error: any) {
            uni.hideLoading();
            uni.showToast({ title: error?.message || '撤回失败', icon: 'none' });
          }
        }
      },
    });
  }

  function handleUrge() {
    uni.showToast({ title: '催办消息已发送', icon: 'success' });
  }

  async function loadDetail() {
    try {
      uni.showLoading({ title: '加载中...' });
      const typeMap: Record<string, string> = {
        'check-in': 'check_in',
        'check-out': 'check_out',
        'transfer': 'transfer',
        'stay': 'stay',
      };
      const backendType = typeMap[applyType.value] || applyType.value;
      detail.value = await getApplyDetailAPI(applyId.value, backendType);
      try {
        approvalInstance.value = await getApprovalInstanceByBusinessAPI(backendType, applyId.value);
      }
      catch {
        approvalInstance.value = null;
      }
      uni.hideLoading();
    }
    catch (error: any) {
      uni.hideLoading();
      console.error('加载失败:', error);
      uni.showToast({ title: error?.message || '加载失败', icon: 'none' });
    }
  }

  onMounted(() => {
    const pages = getCurrentPages();
    const currentPage = pages[pages.length - 1] as any;
    const options = currentPage.options || currentPage.$route?.query;
    applyId.value = Number(options.id) || 1;
    applyType.value = options.type || 'transfer';
    loadDetail();
  });

  return {
    defaultAvatar,
    detail,
    studentInfo,
    applyNo,
    applyTypeName,
    statusText,
    statusDesc,
    progressSteps,
    currentStep,
    canCancel,
    detailFields,
    handleCancel,
    handleUrge,
  };
}
