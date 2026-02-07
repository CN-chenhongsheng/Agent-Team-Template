<!-- 待办审批搜索组件 -->
<template>
  <ArtSearchBar
    ref="searchBarRef"
    v-model="formData"
    :items="formItems"
    :rules="{}"
    @reset="handleReset"
    @search="handleSearch"
  />
</template>

<script setup lang="ts">
  import { onMounted } from 'vue'
  import { useBusinessType } from '@/hooks'

  interface Props {
    modelValue: Record<string, any>
  }

  interface Emits {
    (e: 'update:modelValue', value: Record<string, any>): void
    (e: 'search', params: Record<string, any>): void
    (e: 'reset'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const searchBarRef = ref()
  const { businessTypeOptions, fetchBusinessTypes } = useBusinessType()

  const formData = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
  })

  const formItems = computed(() => [
    {
      label: '业务类型',
      key: 'businessType',
      type: 'select',
      props: {
        clearable: true,
        placeholder: '请选择业务类型',
        options: businessTypeOptions.value
      }
    },
    {
      label: '申请人',
      key: 'applicantName',
      type: 'input',
      props: { clearable: true, placeholder: '请输入申请人姓名' }
    }
  ])

  const handleSearch = async () => {
    await searchBarRef.value.validate()
    emit('search', formData.value)
  }

  const handleReset = () => {
    emit('reset')
  }

  onMounted(() => {
    fetchBusinessTypes()
  })
</script>
