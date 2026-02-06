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
  const formData = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
  })

  const rules = {}

  // 任务类型选项
  const taskTypeOptions = [
    { label: '批量分配', value: 1 },
    { label: '单个推荐', value: 2 },
    { label: '调宿优化', value: 3 }
  ]

  // 状态选项
  const statusOptions = [
    { label: '待执行', value: 0 },
    { label: '执行中', value: 1 },
    { label: '已完成', value: 2 },
    { label: '部分确认', value: 3 },
    { label: '全部确认', value: 4 },
    { label: '已取消', value: 5 }
  ]

  // 表单配置
  const formItems = computed(() => [
    {
      label: '任务名称',
      key: 'taskName',
      type: 'input',
      placeholder: '请输入任务名称',
      clearable: true
    },
    {
      label: '任务类型',
      key: 'taskType',
      type: 'select',
      props: {
        placeholder: '请选择任务类型',
        clearable: true,
        options: taskTypeOptions
      }
    },
    {
      label: '状态',
      key: 'status',
      type: 'select',
      props: {
        placeholder: '请选择状态',
        clearable: true,
        options: statusOptions
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
