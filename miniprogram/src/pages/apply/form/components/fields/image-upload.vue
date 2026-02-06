<template>
  <view class="image-upload-wrapper">
    <view v-if="label" class="upload-label">
      {{ label }}
      <text v-if="required" class="required-mark">
        *
      </text>
    </view>

    <view class="upload-container">
      <view
        v-for="(image, index) in displayList"
        :key="index"
        class="upload-item upload-preview"
      >
        <image
          class="upload-image"
          :src="image"
          mode="aspectFill"
          @click="previewImage(index)"
        />
        <view class="upload-delete" @click.stop="handleDeleteImage(index)">
          <u-icon name="close" size="16" color="#fff" />
        </view>
      </view>
      <view
        v-if="displayList.length < maxLimit"
        class="upload-item upload-add"
        @click="handleAddImage"
      >
        <view class="upload-add-icon">
          <u-icon name="plus-circle" size="28" color="#0adbc3" />
        </view>
        <view class="upload-add-text">
          上传图片
        </view>
      </view>
    </view>

    <view v-if="tip" class="upload-tip">
      <u-icon name="info-circle" size="16" color="#60a5fa" />
      <text>{{ tip }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { uploadImageAPI } from '@/api/common';

interface Props {
  modelValue?: string[];
  label?: string;
  required?: boolean;
  maxCount?: number;
  tip?: string;
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: () => [],
  label: '附件上传',
  required: false,
  maxCount: 3,
  tip: '请上传相关证明材料，支持 jpg, png 格式，单张不超过 5MB。',
});

const emit = defineEmits<{
  'update:modelValue': [value: string[]];
}>();

const imageList = ref<string[]>(props.modelValue || []);
const maxLimit = computed(() => Math.min(props.maxCount, 3));
const displayList = computed(() => imageList.value.slice(0, maxLimit.value));

// 监听外部值变化
watch(() => props.modelValue, (newVal) => {
  const nextList = (newVal || []).slice(0, maxLimit.value);
  imageList.value = nextList;
  if ((newVal || []).length > nextList.length) {
    emit('update:modelValue', nextList);
  }
}, { immediate: true });

// 添加图片
function handleAddImage() {
  if (imageList.value.length >= maxLimit.value) {
    uni.showToast({
      title: `最多上传${maxLimit.value}张图片`,
      icon: 'none',
    });
    return;
  }

  uni.chooseImage({
    count: maxLimit.value - imageList.value.length,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      // 显示上传进度
      uni.showLoading({
        title: '上传中...',
        mask: true,
      });

      try {
        // 依次上传每张图片（每个返回 string[]，需要展平）
        const uploadPromises = (res.tempFilePaths as string[]).map((filePath: string) => uploadImageAPI(filePath));
        const uploadResults = await Promise.all(uploadPromises);
        // 展平数组：[[url1], [url2]] -> [url1, url2]
        const uploadedUrls = uploadResults.flat();

        // 将上传成功的 URL 添加到列表
        imageList.value.push(...uploadedUrls);
        emit('update:modelValue', imageList.value);

        uni.hideLoading();
        uni.showToast({
          title: '上传成功',
          icon: 'success',
        });
      }
      catch (error: any) {
        uni.hideLoading();
        console.error('上传图片失败:', error);
        uni.showToast({
          title: error?.message || '上传失败',
          icon: 'none',
        });
      }
    },
    fail: (err: any) => {
      console.error('选择图片失败:', err);
      uni.showToast({
        title: '选择图片失败',
        icon: 'none',
      });
    },
  });
}

// 删除图片
function handleDeleteImage(index: number) {
  imageList.value.splice(index, 1);
  emit('update:modelValue', imageList.value);
}

// 预览图片
function previewImage(index: number) {
  uni.previewImage({
    urls: imageList.value,
    current: index,
  });
}
</script>

<style lang="scss" scoped>
.image-upload-wrapper {
  width: 100%;
}

.upload-label {
  margin-bottom: 16rpx;
  font-size: 24rpx;
  color: #6b7280;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 1rpx;

  .required-mark {
    margin-left: 4rpx;
    color: #ff9f43;
  }
}

.upload-container {
  display: flex;
  width: 100%;
  flex-wrap: nowrap;
  gap: 8.5rpx;
}

.upload-item {
  position: relative;
  overflow: hidden;
  width: 192rpx;
  height: 192rpx;
  border-radius: 32rpx;
  flex-shrink: 0;

  &.upload-add {
    display: flex;
    justify-content: center;
    align-items: center;
    background: rgb(10 219 195 / 5%);
    border: 2rpx dashed rgb(10 219 195 / 30%);
    transition: all 0.2s;
    flex-direction: column;
    gap: 8rpx;

    &:active {
      transform: scale(0.95);
      background: rgb(10 219 195 / 10%);
    }

    .upload-add-icon {
      color: #0adbc3;
    }

    .upload-add-text {
      font-size: 20rpx;
      font-weight: 700;
      color: rgb(10 219 195 / 80%);
    }
  }

  &.upload-preview {
    border: 1rpx solid rgb(255 255 255);
    box-shadow: 0 2rpx 8rpx rgb(0 0 0 / 5%);

    .upload-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .upload-delete {
      position: absolute;
      top: 0;
      right: 0;
      display: flex;
      justify-content: center;
      align-items: center;
      width: 48rpx;
      height: 48rpx;
      background: #ef4444;
      border-radius: 0 0 0 24rpx;
      box-shadow: 0 2rpx 8rpx rgb(0 0 0 / 20%);
      transition: all 0.2s;

      &:active {
        transform: scale(0.9);
      }
    }
  }
}

.upload-tip {
  display: flex;
  align-items: flex-start;
  padding: 24rpx;
  margin-top: 16rpx;
  font-size: 24rpx;
  color: #6b7280;
  background: rgb(96 165 250 / 10%);
  border: 1rpx solid rgb(96 165 250 / 20%);
  border-radius: 24rpx;
  gap: 12rpx;
  line-height: 1.5;
}
</style>
