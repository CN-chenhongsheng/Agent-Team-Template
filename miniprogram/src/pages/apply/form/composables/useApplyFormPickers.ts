import { ref } from 'vue';
import type { ComputedRef, Ref } from 'vue';
import type DateRangePicker from '../components/pickers/DateRangePicker.vue';
import type SignaturePicker from '../components/pickers/SignaturePicker.vue';
import { applyTypeOptions, repairTypeOptions } from './useApplyFormState';
import type { ApplyTypeOption } from './useApplyFormState';
import type { DateRangePickerMode } from '@/composables/useDateRangePicker';
import type { IApplyFormData } from '@/types';

interface DateRange {
  startDate?: string;
  endDate?: string;
}

interface UseApplyFormPickersOptions {
  formData: IApplyFormData;
  applyTypeIndex: Ref<number>;
  canModifyApplyType: ComputedRef<boolean>;
  filteredApplyTypeOptions: ComputedRef<ApplyTypeOption[]>;
  filteredApplyTypeIndex: ComputedRef<number>;
  resetFormFields: () => void;
}

export function useApplyFormPickers(options: UseApplyFormPickersOptions) {
  const {
    formData,
    applyTypeIndex,
    canModifyApplyType,
    filteredApplyTypeOptions,
    filteredApplyTypeIndex,
    resetFormFields,
  } = options;

  // 申请类型选择器状态
  const showApplyTypePicker = ref(false);
  const tempApplyTypeIndex = ref(0);

  // 报修类型选择器状态
  const showRepairTypePicker = ref(false);
  const tempRepairTypeIndex = ref(-1);

  // Picker 组件引用
  const dateRangePickerRef = ref<InstanceType<typeof DateRangePicker> | null>(null);
  const signaturePickerRef = ref<InstanceType<typeof SignaturePicker> | null>(null);

  // 打开申请类型选择器
  function openApplyTypePicker(): void {
    if (!canModifyApplyType.value) {
      return;
    }
    // 初始化临时选中索引为当前选中索引
    tempApplyTypeIndex.value = filteredApplyTypeIndex.value >= 0 ? filteredApplyTypeIndex.value : 0;
    showApplyTypePicker.value = true;
  }

  // 选择申请类型（在弹窗中）
  function handleSelectApplyType(index: number): void {
    if (!canModifyApplyType.value) {
      return;
    }
    tempApplyTypeIndex.value = index;
  }

  // 确认申请类型
  function confirmApplyType(onClose?: () => void): void {
    if (!canModifyApplyType.value) {
      if (onClose) {
        onClose();
      }
      else {
        showApplyTypePicker.value = false;
      }
      return;
    }
    const oldType = formData.applyType;
    const selectedOption = filteredApplyTypeOptions.value[tempApplyTypeIndex.value];
    if (selectedOption) {
      formData.applyType = selectedOption.value;
      const originalIndex = applyTypeOptions.findIndex(opt => opt.value === selectedOption.value);
      if (originalIndex >= 0) {
        applyTypeIndex.value = originalIndex;
      }
    }

    // 如果切换了申请类型，清空表单数据
    if (oldType && oldType !== formData.applyType) {
      resetFormFields();
    }

    if (onClose) {
      onClose();
    }
    else {
      showApplyTypePicker.value = false;
    }
  }

  // 打开报修类型选择器
  function openRepairTypePicker(): void {
    // 初始化临时选中索引为当前选中索引
    if (formData.repairType) {
      const index = repairTypeOptions.findIndex(opt => opt.value === formData.repairType);
      tempRepairTypeIndex.value = index >= 0 ? index : -1;
    }
    else {
      tempRepairTypeIndex.value = -1;
    }
    showRepairTypePicker.value = true;
  }

  // 选择报修类型（在弹窗中）
  function handleSelectRepairType(index: number): void {
    tempRepairTypeIndex.value = index;
  }

  // 确认报修类型
  function confirmRepairType(onClose?: () => void): void {
    if (tempRepairTypeIndex.value >= 0 && tempRepairTypeIndex.value < repairTypeOptions.length) {
      const selectedOption = repairTypeOptions[tempRepairTypeIndex.value];
      formData.repairType = selectedOption.value;
    }
    if (onClose) {
      onClose();
    }
    else {
      closeRepairTypePicker();
    }
  }

  // 关闭报修类型选择器
  function closeRepairTypePicker(): void {
    showRepairTypePicker.value = false;
  }

  // 打开日期范围选择器
  function openDateRangePicker(
    currentValue: DateRange,
    onConfirm: (value: DateRange) => void,
    mode: DateRangePickerMode = 'range',
  ): void {
    dateRangePickerRef.value?.open(currentValue, onConfirm, mode);
  }

  // 打开签名选择器
  function openSignaturePicker(currentValue: string, onConfirm: (value: string) => void): void {
    signaturePickerRef.value?.open(currentValue, onConfirm);
  }

  return {
    // 申请类型选择器
    showApplyTypePicker,
    tempApplyTypeIndex,
    openApplyTypePicker,
    handleSelectApplyType,
    confirmApplyType,
    // 报修类型选择器
    showRepairTypePicker,
    tempRepairTypeIndex,
    openRepairTypePicker,
    handleSelectRepairType,
    confirmRepairType,
    closeRepairTypePicker,
    // Picker 引用
    dateRangePickerRef,
    signaturePickerRef,
    openDateRangePicker,
    openSignaturePicker,
  };
}
