<template>
  <StayLikeForm
    :form-data="formData"
    :use-separate-date-fields="true"
    start-date-label="开始时间"
    end-date-label="结束时间"
    start-date-placeholder="请选择留宿开始时间"
    end-date-placeholder="请选择留宿结束时间"
    reason-placeholder="请详细描述留宿原因..."
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
