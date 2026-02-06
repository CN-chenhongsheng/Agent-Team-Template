/**
 * 申请业务逻辑封装
 */

import {
  submitCheckInAPI,
  submitCheckOutAPI,
  submitStayAPI,
  submitTransferAPI,
} from '@/api';
import type { IApplyFormData } from '@/types';
import { CheckInType } from '@/types';
import { formatDate } from '@/utils/date';

export class ApplyService {
  static async submit(applyType: string, formData: IApplyFormData) {
    const submitMap: Record<string, () => Promise<unknown>> = {
      normalCheckIn: () => this.submitCheckIn(formData, CheckInType.LONG_TERM),
      tempCheckIn: () => this.submitCheckIn(formData, CheckInType.TEMPORARY),
      transfer: () => this.submitTransfer(formData),
      checkOut: () => this.submitCheckOut(formData),
      stay: () => this.submitStay(formData),
    };

    const submitFn = submitMap[applyType];
    if (!submitFn) throw new Error('未知的申请类型');
    return submitFn();
  }

  private static async submitCheckIn(formData: IApplyFormData, checkInType: CheckInType) {
    return submitCheckInAPI({
      checkInType,
      checkInDate: formData.stayStartDate || formatDate(),
      expectedCheckOutDate: checkInType === CheckInType.TEMPORARY
        ? formData.stayEndDate
        : undefined,
      applyReason: formData.reason,
    });
  }

  private static async submitTransfer(formData: IApplyFormData) {
    return submitTransferAPI({
      transferDate: formatDate(),
      transferReason: formData.reason,
    });
  }

  private static async submitCheckOut(formData: IApplyFormData) {
    return submitCheckOutAPI({
      checkOutDate: formData.stayStartDate || formatDate(),
      checkOutReason: formData.reason,
    });
  }

  private static async submitStay(formData: IApplyFormData) {
    return submitStayAPI({
      stayStartDate: formData.stayStartDate || '',
      stayEndDate: formData.stayEndDate || '',
      stayReason: formData.reason,
      parentName: formData.parentName || '',
      parentPhone: formData.parentPhone || '',
      parentAgree: formData.parentAgree || '',
      signature: formData.signature ? [formData.signature] : [],
      images: formData.images || [],
    });
  }
}
