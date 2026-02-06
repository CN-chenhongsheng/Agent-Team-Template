<template>
  <StayLikeForm
    :form-data="formData"
    date-label="入住时间"
    reason-placeholder="请详细描述申请原因..."
    @update="handleUpdate"
  />
</template>

<script setup lang="ts">
import StayLikeForm from './StayLikeForm.vue';

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
}

defineProps<Props>();

const emit = defineEmits<{
  update: [field: string, value: any];
}>();

function handleUpdate(field: string, value: any) {
  emit('update', field, value);
}
</script>
