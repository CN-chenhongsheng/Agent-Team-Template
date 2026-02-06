import type { IApplyFormData } from '@/types';
import { useFormValidation } from '@/composables/useFormValidation';
import { submitCheckInAPI } from '@/api/accommodation/check-in';
import { submitTransferAPI } from '@/api/accommodation/transfer';
import { submitCheckOutAPI } from '@/api/accommodation/check-out';
import { submitStayAPI } from '@/api/accommodation/stay';
import { submitRepairAPI } from '@/api';

export function useApplyFormActions(formData: IApplyFormData) {
  const { validateForm } = useFormValidation();

  /**
   * 提交申请表单
   */
  async function handleSubmit(): Promise<void> {
    // 表单验证
    if (!validateForm(formData)) {
      return;
    }

    // 显示加载状态
    uni.showLoading({
      title: '提交中...',
      mask: true,
    });

    try {
      const applyType = formData.applyType;

      // 根据申请类型调用对应的 API
      if (applyType === 'normalCheckIn' || applyType === 'tempCheckIn') {
        // 入住申请（正常入住或临时入住）
        await submitCheckInAPI({
          checkInType: applyType === 'normalCheckIn' ? 1 : 2,
          checkInDate: formData.stayStartDate || getCurrentDate(),
          expectedCheckOutDate: applyType === 'tempCheckIn' ? formData.stayEndDate : undefined,
          applyReason: formData.reason,
        });
      }
      else if (applyType === 'transfer') {
        // 调宿申请
        // 验证调宿日期
        if (!formData.transferDate) {
          uni.hideLoading();
          uni.showToast({
            title: '请选择调宿日期',
            icon: 'none',
            duration: 2000,
          });
          return;
        }

        // 验证调宿日期不早于今天
        const today = getCurrentDate();
        if (formData.transferDate < today) {
          uni.hideLoading();
          uni.showToast({
            title: '调宿日期不能早于今天',
            icon: 'none',
            duration: 2000,
          });
          return;
        }

        // 验证目标校区
        if (!formData.targetCampusCode) {
          uni.hideLoading();
          uni.showToast({
            title: '请选择目标校区',
            icon: 'none',
            duration: 2000,
          });
          return;
        }

        // 验证目标楼栋
        if (!formData.targetFloorCode) {
          uni.hideLoading();
          uni.showToast({
            title: '请选择目标楼栋',
            icon: 'none',
            duration: 2000,
          });
          return;
        }

        // 验证目标房间
        if (!formData.targetRoomId) {
          uni.hideLoading();
          uni.showToast({
            title: '请选择目标房间',
            icon: 'none',
            duration: 2000,
          });
          return;
        }

        // 验证目标床位
        if (!formData.targetBedId) {
          uni.hideLoading();
          uni.showToast({
            title: '请选择目标床位',
            icon: 'none',
            duration: 2000,
          });
          return;
        }

        // 验证调宿原因
        if (!formData.reason) {
          uni.hideLoading();
          uni.showToast({
            title: '请输入调宿原因',
            icon: 'none',
            duration: 2000,
          });
          return;
        }

        await submitTransferAPI({
          transferDate: formData.transferDate,
          targetCampusCode: formData.targetCampusCode,
          targetFloorCode: formData.targetFloorCode,
          targetRoomId: formData.targetRoomId,
          targetBedId: formData.targetBedId,
          transferReason: formData.reason,
        });
      }
      else if (applyType === 'checkOut') {
        // 退宿申请
        await submitCheckOutAPI({
          checkOutDate: formData.stayStartDate || getCurrentDate(),
          checkOutReason: formData.reason,
        });
      }
      else if (applyType === 'stay') {
        // 留宿申请
        await submitStayAPI({
          stayStartDate: formData.stayStartDate || '',
          stayEndDate: formData.stayEndDate || '',
          stayReason: formData.reason,
          parentName: formData.parentName || '',
          parentPhone: formData.parentPhone || '',
          parentAgree: formData.parentAgree || 'disagree',
          signature: formData.signature ? [formData.signature] : [],
          images: formData.images || [],
        });
      }
      else if (applyType === 'repair') {
        // 报修申请
        // 验证报修必填字段
        if (!formData.repairType) {
          uni.hideLoading();
          uni.showToast({
            title: '请选择报修类型',
            icon: 'none',
            duration: 2000,
          });
          return;
        }
        if (!formData.description) {
          uni.hideLoading();
          uni.showToast({
            title: '请输入问题描述',
            icon: 'none',
            duration: 2000,
          });
          return;
        }
        if (!formData.urgentLevel) {
          uni.hideLoading();
          uni.showToast({
            title: '请选择紧急程度',
            icon: 'none',
            duration: 2000,
          });
          return;
        }

        await submitRepairAPI({
          repairType: formData.repairType,
          faultDescription: formData.description,
          faultImages: formData.images || [],
          urgentLevel: formData.urgentLevel,
        });
      }
      else {
        // 未知的申请类型
        throw new Error('未知的申请类型');
      }

      // 提交成功
      uni.hideLoading();
      uni.showToast({
        title: '提交成功',
        icon: 'success',
        duration: 1500,
      });

      // 延迟返回上一页或跳转到申请列表页
      setTimeout(() => {
        // 返回到申请中心
        uni.navigateBack({
          delta: 1,
        });
      }, 1500);
    }
    catch (error: any) {
      // 提交失败
      console.error('[useApplyFormActions] 提交失败:', error);
      uni.hideLoading();

      // 显示错误提示
      const errorMessage = getErrorMessage(error);
      uni.showToast({
        title: errorMessage,
        icon: 'none',
        duration: 2000,
      });
    }
  }

  /**
   * 获取当前日期字符串 (YYYY-MM-DD)
   */
  function getCurrentDate(): string {
    return new Date().toISOString().slice(0, 10);
  }

  /**
   * 获取错误提示信息
   */
  function getErrorMessage(error: any): string {
    // 优先使用后端返回的错误信息
    if (error?.message) {
      return error.message;
    }

    // 其次使用 HTTP 错误信息
    if (error?.errMsg) {
      return error.errMsg;
    }

    // 最后使用默认错误信息
    return '提交失败，请稍后重试';
  }

  return {
    handleSubmit,
  };
}
