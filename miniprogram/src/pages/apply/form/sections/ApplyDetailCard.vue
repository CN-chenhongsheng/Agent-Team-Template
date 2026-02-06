<template>
  <view class="apply-detail-card-root">
    <view class="glass-card section-card">
      <view class="section-header">
        <view class="section-title-wrapper">
          <view class="section-indicator section-indicator-accent" />
          <view class="section-title">
            业务详情
          </view>
        </view>
      </view>

      <!-- 申请类型（type 直达时隐藏，无二次选择） -->
      <ApplyTypePicker
        v-if="!hideTypePicker"
        :model-value="modelValue"
        :options="filteredApplyTypeOptions"
        :can-modify="canModifyApplyType"
        @update:model-value="(val: ApplyType) => emit('update:modelValue', val)"
      />

      <!-- 动态表单组件 -->
      <NormalCheckIn
        v-if="modelValue === 'normalCheckIn'"
        :form-data="formDataCopy"
        @update="handleFormUpdate"
      />
      <TempCheckIn
        v-else-if="modelValue === 'tempCheckIn'"
        :form-data="formDataCopy"
        @update="handleFormUpdate"
      />
      <Transfer
        v-else-if="modelValue === 'transfer'"
        :form-data="formDataCopy"
        @update="handleFormUpdate"
      />
      <CheckOut
        v-else-if="modelValue === 'checkOut'"
        :form-data="formDataCopy"
        @update="handleFormUpdate"
      />
      <Stay
        v-else-if="modelValue === 'stay'"
        :form-data="formDataCopy"
        @update="handleFormUpdate"
      />
      <template v-else-if="modelValue === 'repair'">
        <RepairTypePicker
          v-model="repairType"
          :options="repairTypeOptions"
        />
        <Repair
          :form-data="formDataCopy"
          @update="handleFormUpdate"
        />
      </template>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, toRefs } from 'vue';
// 小程序不支持 barrel export，必须直接导入 .vue 文件
import CheckOut from '../components/form-types/check-out.vue';
import NormalCheckIn from '../components/form-types/normal-check-in.vue';
import Repair from '../components/form-types/repair.vue';
import Stay from '../components/form-types/stay.vue';
import TempCheckIn from '../components/form-types/temp-check-in.vue';
import Transfer from '../components/form-types/transfer.vue';
import ApplyTypePicker from '../components/pickers/apply-type-picker.vue';
import RepairTypePicker from '../components/pickers/repair-type-picker.vue';
import type { ApplyTypeOption, RepairTypeOption } from '../composables/useApplyFormState';
import type { ApplyType, IApplyFormData } from '@/types';

interface Props {
  modelValue: ApplyType;
  formData: IApplyFormData;
  hideTypePicker?: boolean;
  filteredApplyTypeOptions: ApplyTypeOption[];
  canModifyApplyType: boolean;
  repairTypeOptions: RepairTypeOption[];
  repairType?: number;
}

const props = withDefaults(defineProps<Props>(), {
  hideTypePicker: false,
  repairType: undefined,
});

const emit = defineEmits<{
  'update:modelValue': [value: ApplyType];
  'update:repairType': [value: number | undefined];
  'formUpdate': [field: string, value: any];
}>();

// 使用 toRefs 保持响应性
const {
  modelValue,
  hideTypePicker,
  filteredApplyTypeOptions,
  canModifyApplyType,
  repairTypeOptions,
} = toRefs(props);

const formDataCopy = computed(() => ({ ...props.formData }));

const repairType = computed({
  get: () => props.repairType,
  set: value => emit('update:repairType', value),
});

function handleFormUpdate(field: string, value: any): void {
  emit('formUpdate', field, value);
}
</script>

<style lang="scss" scoped>
$accent: #ff9f43;
$text-main: #111817;
$glass-bg: rgb(255 255 255 / 65%);
$glass-border: rgb(255 255 255 / 80%);

.apply-detail-card-root {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.glass-card {
  background: $glass-bg;
  border: 2rpx solid $glass-border;
  border-radius: 48rpx;
  box-shadow: 0 8rpx 32rpx rgb(31 38 135 / 7%);
  backdrop-filter: blur(32rpx);
}

.section-card {
  display: flex;
  padding: 40rpx;
  flex-direction: column;
  gap: 32rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;

  .section-title-wrapper {
    display: flex;
    align-items: center;
    gap: 20rpx;

    .section-indicator {
      width: 8rpx;
      height: 40rpx;
      border-radius: 4rpx;

      &.section-indicator-accent {
        background: $accent;
      }
    }

    .section-title {
      font-size: 32rpx;
      font-weight: 700;
      color: $text-main;
    }
  }
}
</style>
