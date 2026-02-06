<template>
  <view class="form-section">
    <!-- 家长姓名 -->
    <TextInput
      :model-value="formData.parentName"
      label="家长姓名"
      placeholder="请输入家长姓名"
      icon="account"
      @update:model-value="(val) => emit('update', 'parentName', val)"
    />

    <!-- 家长电话 -->
    <TextInput
      :model-value="formData.parentPhone"
      label="家长电话"
      placeholder="请输入家长电话"
      icon="phone"
      type="number"
      @update:model-value="(val) => emit('update', 'parentPhone', val)"
    />

    <!-- 家长是否同意 -->
    <view class="radio-group-wrapper">
      <view class="input-label">
        家长是否同意
        <text class="required-mark">
          *
        </text>
      </view>
      <view class="radio-group-container">
        <u-radio-group
          :value="formData.parentAgree"
          @change="handleRadioChange('parentAgree', $event)"
        >
          <view class="radio-item-wrapper">
            <u-radio
              name="agree"
              label="同意"
              active-color="#0adbc3"
            />
          </view>
          <view class="radio-item-wrapper">
            <u-radio
              name="disagree"
              label="不同意"
              active-color="#0adbc3"
            />
          </view>
        </u-radio-group>
      </view>
    </view>

    <!-- 日期时间：单范围 或 开始/结束分开 -->
    <template v-if="useSeparateDateFields">
      <DateRangePicker
        mode="start"
        :model-value="formData.stayStartDate"
        :label="startDateLabel"
        :placeholder="startDatePlaceholder"
        :required="true"
        @update:model-value="(val) => emit('update', 'stayStartDate', val)"
      />
      <DateRangePicker
        mode="end"
        :model-value="formData.stayEndDate"
        :label="endDateLabel"
        :placeholder="endDatePlaceholder"
        :required="true"
        @update:model-value="(val) => emit('update', 'stayEndDate', val)"
      />
    </template>
    <DateRangePicker
      v-else
      :model-value="dateRange"
      :label="dateLabel"
      :required="true"
      @update:model-value="handleDateRangeChange"
    />

    <!-- 申请原因 -->
    <ReasonTextarea
      :model-value="formData.reason"
      :placeholder="reasonPlaceholder"
      @update:model-value="(val) => emit('update', 'reason', val)"
    />

    <!-- 附件上传 + 签名 -->
    <AttachmentAndSignature
      :images="images"
      :signature="signature"
      @update:images="handleImagesChange"
      @update:signature="handleSignatureChange"
    />
  </view>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
// 小程序不支持 barrel export，必须直接导入 .vue 文件
import DateRangePicker from '../pickers/date-range-picker.vue';
import AttachmentAndSignature from '../fields/AttachmentAndSignature.vue';
import ReasonTextarea from '../fields/ReasonTextarea.vue';
import TextInput from '../fields/TextInput.vue';

interface Props {
  formData: {
    parentName?: string;
    parentPhone?: string;
    parentAgree?: string;
    stayStartDate?: string;
    stayEndDate?: string;
    reason?: string;
    images?: string[];
    signature?: string;
  };
  dateLabel?: string;
  reasonPlaceholder?: string;
  /** 是否拆成 开始时间 / 结束时间 两个字段 */
  useSeparateDateFields?: boolean;
  startDateLabel?: string;
  endDateLabel?: string;
  startDatePlaceholder?: string;
  endDatePlaceholder?: string;
}

const props = withDefaults(defineProps<Props>(), {
  dateLabel: '留宿时间',
  reasonPlaceholder: '请详细描述申请原因...',
  useSeparateDateFields: false,
  startDateLabel: '开始时间',
  endDateLabel: '结束时间',
  startDatePlaceholder: '请选择开始时间',
  endDatePlaceholder: '请选择结束时间',
});

const emit = defineEmits<{
  update: [field: string, value: any];
}>();

const dateRange = ref({
  startDate: props.formData.stayStartDate,
  endDate: props.formData.stayEndDate,
});

const images = ref(props.formData.images || []);
const signature = ref(props.formData.signature || '');

// 监听 formData 变化（但不要覆盖用户刚刚输入的签名值）
watch(() => props.formData, (newData) => {
  dateRange.value = {
    startDate: newData.stayStartDate,
    endDate: newData.stayEndDate,
  };
  images.value = newData.images || [];
  // 只有当新值不为空时才更新签名，避免覆盖用户刚刚输入的签名
  if (newData.signature && newData.signature.trim()) {
    signature.value = newData.signature;
  }
}, { deep: true });

function handleRadioChange(field: string, value: string) {
  emit('update', field, value);
}

function handleDateRangeChange(value: { startDate?: string; endDate?: string } | string) {
  if (typeof value === 'string') return;
  if (value.startDate && value.endDate) {
    dateRange.value = { startDate: value.startDate, endDate: value.endDate };
  }
  if (value.startDate) emit('update', 'stayStartDate', value.startDate);
  if (value.endDate) emit('update', 'stayEndDate', value.endDate);
}

function handleImagesChange(value: string[]) {
  emit('update', 'images', value);
}

function handleSignatureChange(value: string) {
  console.log('[StayLikeForm] 收到签名更新:', value, '长度:', value ? value.length : 0);
  // 更新本地 ref
  signature.value = value;
  // 发出更新事件
  emit('update', 'signature', value);
  console.log('[StayLikeForm] emit 完成，本地 signature.value:', signature.value);
}
</script>

<style lang="scss" scoped>
.form-section {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

.radio-group-wrapper {
  padding: 32rpx;
  background: rgb(255 255 255 / 60%);
  border: 1rpx solid rgb(255 255 255);
  border-radius: 32rpx;
  box-shadow: 0 2rpx 6rpx rgb(0 0 0 / 2%);

  .input-label {
    display: block;
    margin-bottom: 24rpx;
    font-size: 20rpx;
    color: #9ca3af;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 1rpx;

    .required-mark {
      color: #ff9f43;
    }
  }

  // 单选框容器
  .radio-group-container {
    :deep(.u-radio-group) {
      display: flex;
      flex-direction: row;
    }
  }

  // 单选框项包装器，添加间距
  .radio-item-wrapper {
    margin-right: 48rpx;

    &:last-child {
      margin-right: 0;
    }
  }

  // H5 和小程序通用样式
  :deep(.u-radio-group) {
    display: flex;
    flex-direction: row;
  }

  :deep(.u-radio) {
    margin-right: 0;
  }
}
</style>
