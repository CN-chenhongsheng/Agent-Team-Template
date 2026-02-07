<!-- 流程搜索组件 -->
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
      label: '流程名称',
      key: 'flowName',
      type: 'input',
      props: { clearable: true, placeholder: '请输入流程名称' }
    },
    {
      label: '流程编码',
      key: 'flowCode',
      type: 'input',
      props: { clearable: true, placeholder: '请输入流程编码' }
    },
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
      label: '状态',
      key: 'status',
      type: 'select',
      props: {
        clearable: true,
        placeholder: '请选择状态',
        options: [
          { label: '启用', value: 1 },
          { label: '停用', value: 0 }
        ]
      }
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
