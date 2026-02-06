<template>
  <view class="form-section">
    <!-- 调宿日期 -->
    <DateRangePicker
      mode="start"
      :model-value="formData.transferDate"
      label="调宿日期"
      placeholder="请选择调宿日期"
      :required="true"
      @update:model-value="(val) => emit('update', 'transferDate', val)"
    />

    <!-- 目标校区 -->
    <OptionPicker
      :model-value="formData.targetCampusCode"
      :options="pickerOptions?.campusOptions?.value || []"
      label="目标校区"
      placeholder="请选择目标校区"
      icon="home"
      :required="true"
      @click="handleOpenCampusPicker"
    />

    <!-- 目标楼栋 -->
    <OptionPicker
      :model-value="formData.targetFloorCode"
      :options="pickerOptions?.floorOptions?.value || []"
      label="目标楼栋"
      :placeholder="formData.targetCampusCode ? '请选择目标楼栋' : '请先选择校区'"
      icon="coupon"
      :required="true"
      :can-modify="!!formData.targetCampusCode"
      @click="handleOpenFloorPicker"
    />

    <!-- 目标房间 -->
    <OptionPicker
      :model-value="formData.targetRoomId"
      :options="pickerOptions?.roomOptions?.value || []"
      label="目标房间"
      :placeholder="formData.targetFloorCode ? '请选择目标房间' : '请先选择楼栋'"
      icon="map"
      :required="true"
      :can-modify="!!formData.targetFloorCode"
      @click="handleOpenRoomPicker"
    />

    <!-- 目标床位 -->
    <OptionPicker
      :model-value="formData.targetBedId"
      :options="pickerOptions?.bedOptions?.value || []"
      label="目标床位"
      :placeholder="formData.targetRoomId ? '请选择目标床位' : '请先选择房间'"
      icon="grid"
      :required="true"
      :can-modify="!!formData.targetRoomId"
      @click="handleOpenBedPicker"
    />

    <!-- 调宿原因 -->
    <ReasonTextarea
      :model-value="formData.reason"
      placeholder="请详细描述调宿原因..."
      @update:model-value="(val) => emit('update', 'reason', val)"
    />
  </view>
</template>

<script setup lang="ts">
import { inject } from 'vue';
import type { ComputedRef } from 'vue';
import DateRangePicker from '../pickers/date-range-picker.vue';
import OptionPicker from '../fields/OptionPicker.vue';
import ReasonTextarea from '../fields/ReasonTextarea.vue';

interface PickerOption {
  label: string;
  value: string | number;
}

interface TransferPickerOptions {
  campusOptions: ComputedRef<PickerOption[]>;
  floorOptions: ComputedRef<PickerOption[]>;
  roomOptions: ComputedRef<PickerOption[]>;
  bedOptions: ComputedRef<PickerOption[]>;
}

interface Props {
  formData: {
    transferDate?: string;
    targetCampusCode?: string;
    targetFloorCode?: string;
    targetRoomId?: number;
    targetBedId?: number;
    reason?: string;
  };
}

const { formData } = defineProps<Props>();

const emit = defineEmits<{
  update: [field: string, value: any];
}>();

// 从父组件 inject 选项和打开函数
const pickerOptions = inject<TransferPickerOptions>('transferPickerOptions');
const openCampusPicker = inject<(onConfirm: (value: string) => void) => void>('openTransferCampusPicker');
const openFloorPicker = inject<(onConfirm: (value: string) => void) => void>('openTransferFloorPicker');
const openRoomPicker = inject<(onConfirm: (value: number) => void) => void>('openTransferRoomPicker');
const openBedPicker = inject<(onConfirm: (value: number) => void) => void>('openTransferBedPicker');

// 打开选择器的处理函数
function handleOpenCampusPicker() {
  openCampusPicker?.((value: string) => {
    emit('update', 'targetCampusCode', value);
    // 清空下级选择
    emit('update', 'targetFloorCode', '');
    emit('update', 'targetRoomId', undefined);
    emit('update', 'targetBedId', undefined);
  });
}

function handleOpenFloorPicker() {
  openFloorPicker?.((value: string) => {
    emit('update', 'targetFloorCode', value);
    // 清空下级选择
    emit('update', 'targetRoomId', undefined);
    emit('update', 'targetBedId', undefined);
  });
}

function handleOpenRoomPicker() {
  openRoomPicker?.((value: number) => {
    emit('update', 'targetRoomId', value);
    // 清空下级选择
    emit('update', 'targetBedId', undefined);
  });
}

function handleOpenBedPicker() {
  openBedPicker?.((value: number) => {
    emit('update', 'targetBedId', value);
  });
}
</script>

<style lang="scss" scoped>
.form-section {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}
</style>
