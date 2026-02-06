<template>
  <view>
    <!-- 弹窗组件移到最外层，确保全屏显示 -->
    <!-- 申请类型选择弹窗 -->
    <view
      v-if="showApplyTypePicker"
      class="picker-overlay"
      :class="{ closing: isClosingApplyTypePicker }"
      @click="closeApplyTypePickerWithAnimation"
    />
    <view
      v-if="showApplyTypePicker"
      class="custom-picker-popup"
      :class="{ closing: isClosingApplyTypePicker }"
    >
      <view class="picker-popup-header">
        <view class="cancel-btn picker-popup-btn" @click="closeApplyTypePickerWithAnimation">
          取消
        </view>
        <view class="picker-popup-title">
          申请类型
        </view>
        <view class="picker-popup-btn confirm-btn" @click="handleConfirmApplyType">
          完成
        </view>
      </view>
      <view class="picker-popup-content">
        <view
          v-for="(option, index) in filteredApplyTypeOptions"
          :key="index"
          class="picker-popup-item"
          :class="{ active: tempApplyTypeIndex === index }"
          @click="handleSelectApplyType(index)"
        >
          <view class="picker-item-icon">
            <u-icon
              v-if="tempApplyTypeIndex === index"
              name="checkmark"
              size="20"
              color="#0adbc3"
            />
          </view>
          <view class="picker-item-text">
            {{ option.label }}
          </view>
        </view>
      </view>
    </view>

    <DateRangePicker ref="dateRangePickerRef" />

    <!-- 报修类型选择弹窗 -->
    <view
      v-if="showRepairTypePicker"
      class="picker-overlay"
      :class="{ closing: isClosingRepairTypePicker }"
      @click="closeRepairTypePickerWithAnimation"
    />
    <view
      v-if="showRepairTypePicker"
      class="custom-picker-popup"
      :class="{ closing: isClosingRepairTypePicker }"
    >
      <view class="picker-popup-header">
        <view class="cancel-btn picker-popup-btn" @click="closeRepairTypePickerWithAnimation">
          取消
        </view>
        <view class="picker-popup-title">
          报修类型
        </view>
        <view class="picker-popup-btn confirm-btn" @click="handleConfirmRepairType">
          完成
        </view>
      </view>
      <view class="picker-popup-content">
        <view
          v-for="(option, index) in repairTypeOptions"
          :key="index"
          class="picker-popup-item"
          :class="{ active: tempRepairTypeIndex === index }"
          @click="handleSelectRepairType(index)"
        >
          <view class="picker-item-icon">
            <u-icon
              v-if="tempRepairTypeIndex === index"
              name="checkmark"
              size="20"
              color="#0adbc3"
            />
          </view>
          <view class="picker-item-text">
            {{ option.label }}
          </view>
        </view>
      </view>
    </view>

    <SignaturePicker ref="signaturePickerRef" />

    <!-- 调宿申请 - 校区选择器 -->
    <u-picker
      :show="showCampusPicker"
      :columns="[campusPickerOptions]"
      @confirm="handleCampusConfirm"
      @cancel="closeCampusPickerWithAnimation"
      @close="closeCampusPickerWithAnimation"
    />

    <!-- 调宿申请 - 楼栋选择器 -->
    <u-picker
      :show="showFloorPicker"
      :columns="[floorPickerOptions]"
      @confirm="handleFloorConfirm"
      @cancel="closeFloorPickerWithAnimation"
      @close="closeFloorPickerWithAnimation"
    />

    <!-- 调宿申请 - 房间选择器 -->
    <u-picker
      :show="showRoomPicker"
      :columns="[roomPickerOptions]"
      @confirm="handleRoomConfirm"
      @cancel="closeRoomPickerWithAnimation"
      @close="closeRoomPickerWithAnimation"
    />

    <!-- 调宿申请 - 床位选择器 -->
    <u-picker
      :show="showBedPicker"
      :columns="[bedPickerOptions]"
      @confirm="handleBedConfirm"
      @cancel="closeBedPickerWithAnimation"
      @close="closeBedPickerWithAnimation"
    />

    <FormLayout
      title="业务填报"
      submit-text="提交申请"
      @submit="handleSubmit"
    >
      <!-- 主内容插槽 -->
      <template #content>
        <!-- 微信小程序 slot 会创建包装层，需要额外容器来应用 gap -->
        <view class="content-wrapper">
          <!-- 标题区域 -->
          <TitleSection />

          <!-- 基础信息卡片 -->
          <BaseInfoCard :user-info="userInfo" />

          <!-- 业务详情卡片 -->
          <ApplyDetailCard
            v-model="formData.applyType"
            :form-data="formData"
            :hide-type-picker="hideTypePicker"
            :filtered-apply-type-options="filteredApplyTypeOptions"
            :can-modify-apply-type="canModifyApplyType"
            :repair-type-options="repairTypeOptions"
            :repair-type="formData.repairType"
            @update:repair-type="(val) => formData.repairType = val"
            @form-update="handleFormUpdate"
          />
        </view>
      </template>
    </FormLayout>
  </view>
</template>

<script setup lang="ts">
import { computed, provide, ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
// 小程序不支持 barrel export，必须直接导入 .vue 文件
import DateRangePicker from './components/pickers/DateRangePicker.vue';
import SignaturePicker from './components/pickers/SignaturePicker.vue';
import ApplyDetailCard from './sections/ApplyDetailCard.vue';
import BaseInfoCard from './sections/BaseInfoCard.vue';
import FormLayout from './sections/FormLayout.vue';
import TitleSection from './sections/TitleSection.vue';
// 小程序不支持 barrel export，必须直接导入
import { repairTypeOptions, useApplyFormState } from './composables/useApplyFormState';
import { useApplyFormPickers } from './composables/useApplyFormPickers';
import { useApplyFormActions } from './composables/useApplyFormActions';
import { useTransferPickers } from './composables/useTransferPickers';

// 关闭动画状态
const isClosingApplyTypePicker = ref(false);
const isClosingRepairTypePicker = ref(false);

// 表单状态
const {
  formData,
  applyTypeIndex,
  hideTypePicker,
  userInfo,
  canModifyApplyType,
  filteredApplyTypeOptions,
  filteredApplyTypeIndex,
  handleFormUpdate,
  initApplyType,
  resetFormFields,
} = useApplyFormState();

// Picker 状态与行为
const {
  showApplyTypePicker,
  tempApplyTypeIndex,
  openApplyTypePicker,
  handleSelectApplyType,
  confirmApplyType,
  showRepairTypePicker,
  tempRepairTypeIndex,
  openRepairTypePicker,
  handleSelectRepairType,
  confirmRepairType,
  closeRepairTypePicker,
  // Picker 组件引用（需要绑定到模板 ref）
  dateRangePickerRef,
  signaturePickerRef,
  openDateRangePicker,
  openSignaturePicker,
} = useApplyFormPickers({
  formData,
  applyTypeIndex,
  canModifyApplyType,
  filteredApplyTypeOptions,
  filteredApplyTypeIndex,
  resetFormFields,
});

// 表单提交
const { handleSubmit } = useApplyFormActions(formData);

// 调宿申请 Picker 状态与行为
const transferFormDataRef = computed(() => ({
  targetCampusCode: formData.targetCampusCode,
  targetFloorCode: formData.targetFloorCode,
  targetRoomId: formData.targetRoomId,
  targetBedId: formData.targetBedId,
}));

const {
  // OptionPicker 选项
  campusOptions,
  floorOptions,
  roomOptions,
  bedOptions,
  // u-picker 选项
  campusPickerOptions,
  floorPickerOptions,
  roomPickerOptions,
  bedPickerOptions,
  // 选择器显示状态
  showCampusPicker,
  showFloorPicker,
  showRoomPicker,
  showBedPicker,
  // 打开选择器
  openCampusPicker,
  openFloorPicker,
  openRoomPicker,
  openBedPicker,
  // 关闭选择器（带动画）
  closeCampusPickerWithAnimation,
  closeFloorPickerWithAnimation,
  closeRoomPickerWithAnimation,
  closeBedPickerWithAnimation,
  // 确认选择
  handleCampusConfirm,
  handleFloorConfirm,
  handleRoomConfirm,
  handleBedConfirm,
  // 初始化
  init: initTransferPickers,
} = useTransferPickers({ formData: transferFormDataRef });

// 带动画的关闭函数
function closeApplyTypePickerWithAnimation() {
  isClosingApplyTypePicker.value = true;
  setTimeout(() => {
    showApplyTypePicker.value = false;
    isClosingApplyTypePicker.value = false;
  }, 300);
}

function closeRepairTypePickerWithAnimation() {
  isClosingRepairTypePicker.value = true;
  setTimeout(() => {
    closeRepairTypePicker();
    isClosingRepairTypePicker.value = false;
  }, 300);
}

// 包装确认函数，添加关闭动画
function handleConfirmApplyType() {
  confirmApplyType(closeApplyTypePickerWithAnimation);
}

function handleConfirmRepairType() {
  confirmRepairType(closeRepairTypePickerWithAnimation);
}

// Provide 给子组件使用
provide('openDateRangePicker', openDateRangePicker);
provide('openSignaturePicker', openSignaturePicker);
provide('openApplyTypePicker', openApplyTypePicker);
provide('openRepairTypePicker', openRepairTypePicker);

// 调宿申请 Picker - provide 给子组件
provide('transferPickerOptions', {
  campusOptions,
  floorOptions,
  roomOptions,
  bedOptions,
});
provide('openTransferCampusPicker', openCampusPicker);
provide('openTransferFloorPicker', openFloorPicker);
provide('openTransferRoomPicker', openRoomPicker);
provide('openTransferBedPicker', openBedPicker);

// 页面加载时接收参数
onLoad((options: any) => {
  initApplyType(options?.type);
  // 初始化调宿申请选择器数据
  initTransferPickers();
});
</script>

<style lang="scss" scoped>
// slot 内容包装器 - 应用 gap 布局
.content-wrapper {
  display: flex;
  flex-direction: column;
  gap: 40rpx;
}

// 遮罩层 - 固定在屏幕，不受父容器影响
.picker-overlay {
  position: fixed !important;
  inset: 0 !important;
  z-index: 99998 !important;
  background: rgb(0 0 0 / 50%);
  animation: fadeIn 0.3s;

  &.closing {
    animation: fadeOut 0.3s;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
  }
}

@keyframes fadeOut {
  from {
    opacity: 1;
  }

  to {
    opacity: 0;
  }
}

// 自定义 Picker 弹窗样式 - 固定在屏幕底部，不受父容器影响
.custom-picker-popup {
  position: fixed !important;
  right: 0 !important;
  bottom: 0 !important;
  left: 0 !important;
  z-index: 99999 !important;
  overflow: hidden;
  padding-bottom: env(safe-area-inset-bottom);
  width: 100vw !important;
  max-height: 80vh;
  background: #fff;
  border-radius: 32rpx 32rpx 0 0;
  animation: slideUp 0.3s;

  &.closing {
    animation: slideDown 0.3s;
  }

  .picker-popup-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 32rpx 48rpx;
    border-bottom: 1rpx solid rgb(229 231 235 / 50%);
    background: #fff;

    .picker-popup-btn {
      padding: 8rpx 16rpx;
      font-size: 32rpx;
      transition: all 0.2s;
      font-weight: 500;

      &.cancel-btn {
        color: #6b7280;

        &:active {
          color: #111817;
        }
      }

      &.confirm-btn {
        color: #0adbc3;
        font-weight: 600;

        &:active {
          color: #08bda8;
        }
      }
    }

    .picker-popup-title {
      font-size: 36rpx;
      text-align: center;
      color: #111817;
      flex: 1;
      font-weight: 700;
    }
  }

  .picker-popup-content {
    overflow-y: auto;
    padding: 16rpx 0;
    max-height: calc(80vh - 120rpx);

    .picker-popup-item {
      position: relative;
      padding: 32rpx 48rpx;
      font-size: 32rpx;
      text-align: center;
      color: #111817;
      transition: all 0.2s;

      .picker-item-icon {
        position: absolute;
        top: 50%;
        right: 75rpx;
        display: flex;
        justify-content: center;
        align-items: center;
        transform: translateY(-50%);
      }

      .picker-item-text {
        display: block;
        width: 100%;
        text-align: center;
      }

      &.active {
        color: #0adbc3;
        font-weight: 700;
      }
    }
  }
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
  }

  to {
    transform: translateY(0);
  }
}

@keyframes slideDown {
  from {
    transform: translateY(0);
  }

  to {
    transform: translateY(100%);
  }
}
</style>
