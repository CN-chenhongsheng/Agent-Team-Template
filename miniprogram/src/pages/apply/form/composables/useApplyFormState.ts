import { computed, reactive, ref } from 'vue';
import type { ApplyType, IApplyFormData } from '@/types';
import useUserStore from '@/store/modules/user';

// 重新导出类型供外部使用
export type { ApplyType };

export interface ApplyTypeOption {
  label: string;
  value: ApplyType;
}

export interface RepairTypeOption {
  label: string;
  value: number;
}

// 申请类型选项
export const applyTypeOptions: ApplyTypeOption[] = [
  { label: '正常入住', value: 'normalCheckIn' },
  { label: '临时入住', value: 'tempCheckIn' },
  { label: '调宿申请', value: 'transfer' },
  { label: '退宿申请', value: 'checkOut' },
  { label: '留宿申请', value: 'stay' },
  { label: '故障报修', value: 'repair' },
];

// 报修类型选项
export const repairTypeOptions: RepairTypeOption[] = [
  { label: '水电', value: 1 },
  { label: '门窗', value: 2 },
  { label: '家具', value: 3 },
  { label: '网络', value: 4 },
  { label: '其他', value: 99 },
];

export function useApplyFormState() {
  const userStore = useUserStore();

  // 表单数据
  const formData = reactive<IApplyFormData>({
    applyType: '',
    // 通用字段
    reason: '',
    images: [] as string[],
    signature: '',
    // 临时入住/留宿申请字段
    parentName: '',
    parentPhone: '',
    parentAgree: '',
    stayStartDate: '',
    stayEndDate: '',
    // 报修申请字段
    repairType: undefined,
    phone: '',
    description: '',
    // 调宿申请字段
    transferDate: '',
    targetCampusCode: '',
    targetFloorCode: '',
    targetRoomId: undefined,
    targetBedId: undefined,
  });

  const applyTypeIndex = ref(0);
  // 是否通过 type 参数直达（无二次选择，隐藏类型选择器）
  const hideTypePicker = ref(false);

  // 用户信息
  const userInfo = computed(() => userStore.userInfo);

  // 是否允许修改申请类型（只有入住申请可以修改）
  const canModifyApplyType = computed(() => {
    // 如果没有设置申请类型，允许选择
    if (!formData.applyType) {
      return true;
    }
    // 如果是入住申请（正常入住或临时入住），允许修改
    return formData.applyType === 'normalCheckIn' || formData.applyType === 'tempCheckIn';
  });

  // 过滤后的申请类型选项（如果当前是入住申请，只显示正常入住和临时入住）
  const filteredApplyTypeOptions = computed(() => {
    // 如果当前申请类型是入住申请（正常入住或临时入住），只显示这两个选项
    if (formData.applyType === 'normalCheckIn' || formData.applyType === 'tempCheckIn') {
      return applyTypeOptions.filter(opt => opt.value === 'normalCheckIn' || opt.value === 'tempCheckIn');
    }
    // 否则显示所有选项（但会被禁用）
    return applyTypeOptions;
  });

  // 过滤后的选中索引
  const filteredApplyTypeIndex = computed(() => {
    const currentValue = formData.applyType;
    return filteredApplyTypeOptions.value.findIndex(opt => opt.value === currentValue);
  });

  // 获取组件所需的表单数据
  function getFormDataForComponent(): IApplyFormData {
    return { ...formData };
  }

  // 处理表单更新
  function handleFormUpdate(field: string, value: any): void {
    // 类型安全的字段更新
    if (field in formData) {
      if (field === 'signature') {
        console.log('[useApplyFormState] handleFormUpdate 收到签名:', value, '长度:', value ? String(value).length : 0);
      }
      (formData as any)[field] = value;
      if (field === 'signature') {
        console.log('[useApplyFormState] 更新后 formData.signature:', formData.signature, '长度:', formData.signature ? formData.signature.length : 0);
      }
    }
  }

  // 初始化申请类型（从路由参数）
  function initApplyType(type: string | undefined): void {
    if (type) {
      const typeMap: Record<string, ApplyType> = {
        checkIn: 'normalCheckIn', // 入住申请默认正常入住
        transfer: 'transfer',
        checkOut: 'checkOut',
        stay: 'stay',
        repair: 'repair',
      };
      const defaultType = typeMap[type];
      if (defaultType) {
        const typeIndex = applyTypeOptions.findIndex(opt => opt.value === defaultType);
        if (typeIndex >= 0) {
          applyTypeIndex.value = typeIndex;
          formData.applyType = defaultType;
          hideTypePicker.value = true;
        }
      }
    }
  }

  // 清空表单（切换类型时调用）
  function resetFormFields(): void {
    Object.keys(formData).forEach((key) => {
      if (key !== 'applyType') {
        const formKey = key as keyof IApplyFormData;
        if (Array.isArray(formData[formKey])) {
          (formData[formKey] as string[]) = [];
        }
        else {
          (formData[formKey] as string) = '';
        }
      }
    });
  }

  return {
    formData,
    applyTypeIndex,
    hideTypePicker,
    userInfo,
    canModifyApplyType,
    filteredApplyTypeOptions,
    filteredApplyTypeIndex,
    getFormDataForComponent,
    handleFormUpdate,
    initApplyType,
    resetFormFields,
  };
}
