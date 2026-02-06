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

  // 状态选项
  const statusOptions = [
    { label: '待确认', value: 0 },
    { label: '已确认', value: 1 },
    { label: '已拒绝', value: 2 },
    { label: '已调整', value: 3 }
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
      label: '房间',
      key: 'roomCode',
      type: 'input',
      placeholder: '请输入房间号',
      clearable: true
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
