<template>
  <view class="form-section">
    <!-- 申请原因 -->
    <ReasonTextarea
      :model-value="formData.reason"
      placeholder="请详细描述退宿原因..."
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
import AttachmentAndSignature from '../fields/AttachmentAndSignature.vue';
import ReasonTextarea from '../fields/ReasonTextarea.vue';

interface Props {
  formData: {
    reason?: string;
    images?: string[];
    signature?: string;
  };
}

const props = defineProps<Props>();

const emit = defineEmits<{
  update: [field: string, value: any];
}>();

const images = ref(props.formData.images || []);
const signature = ref(props.formData.signature || '');

// 监听 formData 变化（但不要覆盖用户刚刚输入的签名值）
watch(() => props.formData, (newData) => {
  images.value = newData.images || [];
  // 只有当新值不为空时才更新签名，避免覆盖用户刚刚输入的签名
  if (newData.signature && newData.signature.trim()) {
    signature.value = newData.signature;
  }
}, { deep: true });

function handleImagesChange(value: string[]) {
  emit('update', 'images', value);
}

function handleSignatureChange(value: string) {
  console.log('[check-out.vue] 收到签名更新:', value, '长度:', value ? value.length : 0);
  // 更新本地 ref
  signature.value = value;
  // 发出更新事件
  emit('update', 'signature', value);
  console.log('[check-out.vue] emit 完成，本地 signature.value:', signature.value);
}
</script>

<style lang="scss" scoped>
.form-section {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}
</style>
