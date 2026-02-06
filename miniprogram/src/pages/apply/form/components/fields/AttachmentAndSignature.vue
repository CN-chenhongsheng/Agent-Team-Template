<template>
  <view class="attachment-signature-section">
    <!-- 附件上传 -->
    <ImageUpload
      v-model="localImages"
      :label="imageLabel"
      :required="imageRequired"
      :max-count="maxImageCount"
    />

    <!-- 本人签名 -->
    <Signature
      v-model="localSignature"
      :label="signatureLabel"
      :required="signatureRequired"
      @update:model-value="handleSignatureChange"
    />
  </view>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import ImageUpload from './image-upload.vue';
import Signature from './signature.vue';

interface Props {
  images?: string[];
  signature?: string;
  imageLabel?: string;
  signatureLabel?: string;
  imageRequired?: boolean;
  signatureRequired?: boolean;
  maxImageCount?: number;
}

const props = withDefaults(defineProps<Props>(), {
  images: () => [],
  signature: '',
  imageLabel: '附件上传',
  signatureLabel: '本人签名',
  imageRequired: false,
  signatureRequired: true,
  maxImageCount: 3,
});

const emit = defineEmits<{
  'update:images': [value: string[]];
  'update:signature': [value: string];
}>();

const localImages = ref<string[]>(props.images);
const localSignature = ref(props.signature);

// 监听 props 变化
watch(() => props.images, (newImages) => {
  localImages.value = newImages || [];
}, { deep: true });

watch(() => props.signature, (newSignature) => {
  console.log('[AttachmentAndSignature] props.signature watch 触发，新签名值:', newSignature, '长度:', newSignature ? newSignature.length : 0);
  // 只有当新值不为空时才更新，避免覆盖用户刚刚输入的签名
  if (newSignature && newSignature.trim()) {
    localSignature.value = newSignature;
  }
});

// 监听 localImages 变化（v-model 会自动更新它，需要手动发出事件）
watch(localImages, (newImages) => {
  emit('update:images', newImages || []);
}, { deep: true });

// 监听 localSignature 变化（v-model 会自动更新它，但不会触发 @update:model-value）
watch(localSignature, (newValue) => {
  console.log('[AttachmentAndSignature] localSignature watch 触发，新签名值:', newValue, '长度:', newValue ? newValue.length : 0);
  if (newValue && newValue.trim()) {
    emit('update:signature', newValue);
    console.log('[AttachmentAndSignature] emit update:signature 完成');
  }
});

function handleSignatureChange(value: string) {
  // Signature 组件会直接 emit update:modelValue，这里同步更新 localSignature 并发出事件
  localSignature.value = value;
  emit('update:signature', value);
}
</script>

<style lang="scss" scoped>
.attachment-signature-section {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}
</style>
