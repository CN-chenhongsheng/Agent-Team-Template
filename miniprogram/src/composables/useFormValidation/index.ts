/**
 * 表单验证组合式函数
 */

import type { IApplyFormData } from '@/types';

const validators = {
  required: (value: unknown, fieldName: string): boolean => {
    if (!value || (typeof value === 'string' && !value.trim())) {
      uni.showToast({
        title: `请输入${fieldName}`,
        icon: 'none',
      });
      return false;
    }
    return true;
  },
  phone: (value: string): boolean => {
    if (!/^1[3-9]\d{9}$/.test(value?.trim() || '')) {
      uni.showToast({
        title: '请输入正确的手机号',
        icon: 'none',
      });
      return false;
    }
    return true;
  },
  dateRange: (startDate: string, endDate: string): boolean => {
    if (new Date(endDate) < new Date(startDate)) {
      uni.showToast({
        title: '结束日期不能早于开始日期',
        icon: 'none',
      });
      return false;
    }
    return true;
  },
  signature: (value: string): boolean => {
    // 调试：打印签名值
    console.log('[FormValidation] 签名校验，值:', value, '类型:', typeof value, '长度:', value ? String(value).length : 0);

    // 检查签名值是否存在（可以是 URL 或 base64 或临时路径）
    if (!value || (typeof value === 'string' && !value.trim())) {
      uni.showToast({
        title: '请完成本人签名',
        icon: 'none',
      });
      return false;
    }
    return true;
  },
};

type ValidatorKey = keyof typeof validators;
interface ValidationRule { field: keyof IApplyFormData; validator: ValidatorKey; label?: string }

const formRules: Record<string, ValidationRule[]> = {
  normalCheckIn: [
    { field: 'stayStartDate', validator: 'required', label: '入住日期' },
    { field: 'reason', validator: 'required', label: '申请原因' },
  ],
  tempCheckIn: [
    { field: 'parentName', validator: 'required', label: '家长姓名' },
    { field: 'parentPhone', validator: 'required', label: '家长电话' },
    { field: 'parentPhone', validator: 'phone' },
    { field: 'parentAgree', validator: 'required', label: '家长是否同意' },
    { field: 'stayStartDate', validator: 'required', label: '开始日期' },
    { field: 'stayEndDate', validator: 'required', label: '结束日期' },
    { field: 'reason', validator: 'required', label: '申请原因' },
    { field: 'signature', validator: 'signature' },
  ],
  transfer: [
    { field: 'reason', validator: 'required', label: '申请原因' },
  ],
  checkOut: [
    { field: 'stayStartDate', validator: 'required', label: '退宿日期' },
    { field: 'reason', validator: 'required', label: '退宿理由' },
    { field: 'signature', validator: 'signature' },
  ],
  stay: [
    { field: 'parentName', validator: 'required', label: '家长姓名' },
    { field: 'parentPhone', validator: 'required', label: '家长电话' },
    { field: 'parentPhone', validator: 'phone' },
    { field: 'parentAgree', validator: 'required', label: '家长是否同意' },
    { field: 'stayStartDate', validator: 'required', label: '开始日期' },
    { field: 'stayEndDate', validator: 'required', label: '结束日期' },
    { field: 'reason', validator: 'required', label: '申请原因' },
    { field: 'signature', validator: 'signature' },
  ],
};

/**
 * 验证表单
 */
export function useFormValidation() {
  const validateForm = (formData: IApplyFormData): boolean => {
    if (!formData.applyType) {
      uni.showToast({
        title: '请选择申请类型',
        icon: 'none',
      });
      return false;
    }

    const rules = formRules[formData.applyType] || [];
    for (const rule of rules) {
      const value = formData[rule.field];
      if (rule.validator === 'required' && rule.label) {
        if (!validators.required(value, rule.label)) return false;
      }
      else if (rule.validator === 'phone') {
        if (!validators.phone(value as string)) return false;
      }
      else if (rule.validator === 'signature') {
        if (!validators.signature(value as string)) return false;
      }
    }

    if (['tempCheckIn', 'stay'].includes(formData.applyType)) {
      if (!validators.dateRange(formData.stayStartDate || '', formData.stayEndDate || '')) return false;
    }

    if (!rules.length) {
      uni.showToast({
        title: '未知的申请类型',
        icon: 'none',
      });
      return false;
    }

    return true;
  };

  return {
    validateForm,
    validators,
  };
}
