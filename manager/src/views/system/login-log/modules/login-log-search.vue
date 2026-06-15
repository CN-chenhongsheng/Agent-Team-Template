<template>
  <ArtSearchBar
    ref="searchBarRef"
    v-model="formData"
    :items="formItems"
    :rules="rules"
    @reset="handleReset"
    @search="handleSearch"
  >
  </ArtSearchBar>
</template>

<script setup lang="ts">
  defineOptions({ name: 'LoginLogSearch' })

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

  /**
   * 表单数据双向绑定
   */
  const formData = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
  })

  /**
   * 表单校验规则
   */
  const rules = {}

  /**
   * 登录类型选项
   */
  const loginTypeOptions = ref([
    { label: '登录', value: 'login' },
    { label: '登出', value: 'logout' }
  ])

  /**
   * 登录状态选项
   */
  const loginStatusOptions = ref([
    { label: '成功', value: 0 },
    { label: '失败', value: 1 }
  ])

  /**
   * 搜索表单配置项
   */
  const formItems = computed(() => [
    {
      label: '用户账号',
      key: 'username',
      type: 'input',
      placeholder: '请输入用户账号',
      clearable: true
    },
    {
      label: '登录类型',
      key: 'loginType',
      type: 'select',
      props: {
        placeholder: '请选择登录类型',
        options: loginTypeOptions.value,
        clearable: true
      }
    },
    {
      label: '登录状态',
      key: 'loginStatus',
      type: 'select',
      props: {
        placeholder: '请选择登录状态',
        options: loginStatusOptions.value,
        clearable: true
      }
    },
    {
      label: '登录时间',
      key: 'loginTime',
      type: 'daterange',
      props: {
        startPlaceholder: '开始日期',
        endPlaceholder: '结束日期',
        valueFormat: 'YYYY-MM-DD'
      }
    }
  ])

  /**
   * 处理重置事件
   */
  const handleReset = () => {
    emit('reset')
  }

  /**
   * 处理搜索事件
   */
  const handleSearch = async () => {
    await searchBarRef.value.validate()
    const searchParams = { ...formData.value }
    if (
      searchParams.loginTime &&
      Array.isArray(searchParams.loginTime) &&
      searchParams.loginTime.length === 2
    ) {
      searchParams.startTime = searchParams.loginTime[0]
      searchParams.endTime = searchParams.loginTime[1]
    }
    delete searchParams.loginTime
    emit('search', searchParams)
  }
</script>
