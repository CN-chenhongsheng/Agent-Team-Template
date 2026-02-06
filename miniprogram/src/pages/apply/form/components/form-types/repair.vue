<template>
  <view class="form-section">
    <!-- 联系电话 -->
    <TextInput
      :model-value="formData.phone"
      label="联系电话"
      placeholder="请输入联系电话"
      icon="phone"
      type="number"
      @update:model-value="(val) => emit('update', 'phone', val)"
    />

    <!-- 紧急程度 -->
    <view class="urgent-level-field">
      <view class="field-label required">
        <u-icon name="error-circle" size="16" color="#ff6987" />
        <text>紧急程度</text>
      </view>
      <view class="urgent-options">
        <view
          v-for="option in urgentLevelOptions"
          :key="option.value"
          class="urgent-option"
          :class="{ active: formData.urgentLevel === option.value }"
          @click="handleUrgentLevelChange(option.value)"
        >
          <view class="option-icon" :class="`urgent-${option.value}`">
            <u-icon
              v-if="formData.urgentLevel === option.value"
              name="checkmark"
              size="14"
              color="#fff"
            />
          </view>
          <text class="option-label">
            {{ option.label }}
          </text>
        </view>
      </view>
    </view>

    <!-- 问题描述 -->
    <ReasonTextarea
      :model-value="formData.description"
      label="问题描述"
      placeholder="请详细描述故障问题..."
      @update:model-value="(val) => emit('update', 'description', val)"
    />

    <!-- 故障图片 -->
    <ImageUpload
      v-model="images"
      label="故障图片"
      :required="false"
      :max-count="3"
      tip="建议上传故障现场照片，最多3张"
      @update:model-value="handleImagesChange"
    />
  </view>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
// 小程序不支持 barrel export，必须直接导入 .vue 文件
import ImageUpload from '../fields/image-upload.vue';
import ReasonTextarea from '../fields/ReasonTextarea.vue';
import TextInput from '../fields/TextInput.vue';

interface Props {
  formData: {
    phone?: string;
    description?: string;
    images?: string[];
    urgentLevel?: number;
  };
}

const props = defineProps<Props>();

const emit = defineEmits<{
  update: [field: string, value: any];
}>();

// 紧急程度选项
const urgentLevelOptions = [
  { label: '一般', value: 1 },
  { label: '紧急', value: 2 },
  { label: '非常紧急', value: 3 },
];

const images = ref(props.formData.images || []);

// 监听 formData 变化
watch(() => props.formData, (newData) => {
  images.value = newData.images || [];
}, { deep: true, immediate: true });

function handleImagesChange(value: string[]) {
  emit('update', 'images', value);
}

function handleUrgentLevelChange(value: number) {
  emit('update', 'urgentLevel', value);
}
</script>

<style lang="scss" scoped>
.form-section {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

.urgent-level-field {
  .field-label {
    display: flex;
    align-items: center;
    gap: 8rpx;
    margin-bottom: 16rpx;
    font-size: 28rpx;
    font-weight: 500;
    color: #333;

    &.required::after {
      content: '*';
      margin-left: 4rpx;
      color: #ff6987;
    }
  }

  .urgent-options {
    display: flex;
    gap: 16rpx;

    .urgent-option {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 12rpx;
      padding: 20rpx;
      background: rgb(255 255 255 / 60%);
      border: 2rpx solid rgb(0 0 0 / 5%);
      border-radius: 12rpx;
      cursor: pointer;
      transition: all 0.3s ease;

      &.active {
        background: rgb(255 105 135 / 10%);
        border-color: #ff6987;
      }

      .option-icon {
        display: flex;
        justify-content: center;
        align-items: center;
        width: 40rpx;
        height: 40rpx;
        border-radius: 50%;

        &.urgent-1 {
          background: #4caf50;
        }

        &.urgent-2 {
          background: #ff9800;
        }

        &.urgent-3 {
          background: #f44336;
        }
      }

      .option-label {
        font-size: 24rpx;
        color: #666;
      }

      &.active .option-label {
        color: #333;
        font-weight: 600;
      }
    }
  }
}
</style>
