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
  import { useI18n } from 'vue-i18n'

  const { t } = useI18n()
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
  const loginTypeOptions = computed(() => [
    { label: t('system.loginLog.login'), value: 'login' },
    { label: t('system.loginLog.logout'), value: 'logout' }
  ])

  /**
   * 登录状态选项
   */
  const loginStatusOptions = computed(() => [
    { label: t('system.loginLog.success'), value: 0 },
    { label: t('system.loginLog.fail'), value: 1 }
  ])

  /**
   * 搜索表单配置项
   */
  const formItems = computed(() => [
    {
      label: t('system.loginLog.search.userAccount'),
      key: 'username',
      type: 'input',
      placeholder: t('system.loginLog.search.userAccountPlaceholder'),
      clearable: true
    },
    {
      label: t('system.loginLog.search.loginType'),
      key: 'loginType',
      type: 'select',
      props: {
        placeholder: t('system.loginLog.search.loginTypePlaceholder'),
        options: loginTypeOptions.value,
        clearable: true
      }
    },
    {
      label: t('system.loginLog.search.loginStatus'),
      key: 'loginStatus',
      type: 'select',
      props: {
        placeholder: t('system.loginLog.search.loginStatusPlaceholder'),
        options: loginStatusOptions.value,
        clearable: true
      }
    },
    {
      label: t('system.loginLog.search.loginTime'),
      key: 'loginTime',
      type: 'daterange',
      props: {
        startPlaceholder: t('system.loginLog.search.startDate'),
        endPlaceholder: t('system.loginLog.search.endDate'),
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
