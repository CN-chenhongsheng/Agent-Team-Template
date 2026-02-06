<template>
  <ArtSearchBar
    ref="searchBarRef"
    v-model="formData"
    :items="formItems"
    :rules="rules"
    @reset="handleReset"
    @search="handleSearch"
  />
</template>

<script setup lang="ts">
  import { useDictStore } from '@/store/modules/dict'

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

  const dictStore = useDictStore()
  const searchBarRef = ref()
  const formData = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
  })

  const rules = {}

  // 年份选项（最近5年）
  const yearOptions = computed(() => {
    const currentYear = new Date().getFullYear()
    return Array.from({ length: 6 }, (_, i) => ({
      label: `${currentYear - i}级`,
      value: currentYear - i
    }))
  })

  // 填写状态选项
  const fillStatusOptions = [
    { label: '已填写', value: 'filled' },
    { label: '未填写', value: 'unfilled' }
  ]

  // 表单配置
  const formItems = computed(() => [
    {
      label: '学号',
      key: 'studentNo',
      type: 'input',
      placeholder: '请输入学号',
      clearable: true
    },
    {
      label: '姓名',
      key: 'studentName',
      type: 'input',
      placeholder: '请输入姓名',
      clearable: true
    },
    {
      label: '入学年份',
      key: 'enrollmentYear',
      type: 'select',
      props: {
        placeholder: '请选择年份',
        clearable: true,
        options: yearOptions.value
      }
    },
    {
      label: '填写状态',
      key: 'fillStatus',
      type: 'select',
      props: {
        placeholder: '请选择状态',
        clearable: true,
        options: fillStatusOptions
      }
    }
  ])

  function handleReset() {
    emit('reset')
  }

  async function handleSearch() {
    await searchBarRef.value.validate()
    emit('search', formData.value)
  }
</script>
