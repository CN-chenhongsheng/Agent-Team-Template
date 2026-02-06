<!-- 审批记录搜索组件 -->
<template>
  <ElCard class="art-search-card" shadow="never">
    <ElForm :model="localModel" label-width="80px">
      <ElRow :gutter="16">
        <ElCol :xs="24" :sm="12" :md="8" :lg="6">
          <ElFormItem label="业务类型">
            <ElSelect
              v-model="localModel.businessType"
              placeholder="请选择业务类型"
              clearable
              style="width: 100%"
            >
              <ElOption
                v-for="item in businessTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :xs="24" :sm="12" :md="8" :lg="6">
          <ElFormItem label="申请人">
            <ElInput
              v-model="localModel.applicantName"
              placeholder="请输入申请人姓名"
              clearable
              @keyup.enter="handleSearch"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :xs="24" :sm="12" :md="8" :lg="6">
          <ElFormItem label="审批人">
            <ElInput
              v-model="localModel.approverName"
              placeholder="请输入审批人姓名"
              clearable
              @keyup.enter="handleSearch"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :xs="24" :sm="12" :md="8" :lg="6">
          <ElFormItem label="审批结果">
            <ElSelect
              v-model="localModel.action"
              placeholder="请选择审批结果"
              clearable
              style="width: 100%"
            >
              <ElOption label="通过" :value="1" />
              <ElOption label="拒绝" :value="2" />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :xs="24" :sm="12" :md="8" :lg="6">
          <ElFormItem label=" ">
            <ElSpace>
              <ElButton type="primary" @click="handleSearch" v-ripple>
                <i class="ri-search-line mr-1"></i>
                查询
              </ElButton>
              <ElButton @click="handleReset" v-ripple>
                <i class="ri-refresh-line mr-1"></i>
                重置
              </ElButton>
            </ElSpace>
          </ElFormItem>
        </ElCol>
      </ElRow>
    </ElForm>
  </ElCard>
</template>

<script setup lang="ts">
  import { computed, onMounted } from 'vue'
  import { useBusinessType } from '@/hooks'

  interface SearchParams {
    businessType?: string
    applicantName?: string
    approverName?: string
    action?: number
  }

  const props = defineProps<{
    modelValue: SearchParams
  }>()

  const emit = defineEmits<{
    'update:modelValue': [value: SearchParams]
    search: []
    reset: []
  }>()

  // 业务类型（从字典获取）
  const { businessTypeOptions, fetchBusinessTypes } = useBusinessType()

  onMounted(() => {
    fetchBusinessTypes()
  })

  const localModel = computed({
    get: () => props.modelValue,
    set: (value) => emit('update:modelValue', value)
  })

  const handleSearch = () => {
    emit('search')
  }

  const handleReset = () => {
    emit('reset')
  }
</script>
