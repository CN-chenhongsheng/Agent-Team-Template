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
  import { useDictStore } from '@/store/modules/dict'

  const { t } = useI18n()

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
  const dictStore = useDictStore()

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
   * 业务类型选项（从字典获取）
   */
  const businessTypeOptions = computed(() => {
    const dictData = dictStore.getDictData('sys_oper_business_type')
    return dictData.map((item) => ({
      label: item.label,
      value: Number(item.value)
    }))
  })

  // 加载业务类型字典
  onMounted(async () => {
    await dictStore.loadDictData('sys_oper_business_type')
  })

  /**
   * 状态选项
   */
  const statusOptions = computed(() => [
    { label: t('system.operLog.normal'), value: 0 },
    { label: t('system.operLog.abnormal'), value: 1 }
  ])

  /**
   * 搜索表单配置项
   */
  const formItems = computed(() => [
    {
      label: t('system.operLog.search.module'),
      key: 'title',
      type: 'input',
      placeholder: t('system.operLog.search.modulePlaceholder'),
      clearable: true
    },
    {
      label: t('system.operLog.search.operator'),
      key: 'operName',
      type: 'input',
      placeholder: t('system.operLog.search.operatorPlaceholder'),
      clearable: true
    },
    {
      label: t('system.operLog.search.businessType'),
      key: 'businessType',
      type: 'select',
      props: {
        placeholder: t('system.operLog.search.businessTypePlaceholder'),
        options: businessTypeOptions.value,
        clearable: true
      }
    },
    {
      label: t('system.operLog.search.status'),
      key: 'status',
      type: 'select',
      props: {
        placeholder: t('system.operLog.search.statusPlaceholder'),
        options: statusOptions.value,
        clearable: true
      }
    },
    {
      label: t('system.operLog.search.time'),
      key: 'operTime',
      type: 'daterange',
      props: {
        startPlaceholder: t('system.operLog.search.startDate'),
        endPlaceholder: t('system.operLog.search.endDate'),
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
    // 处理时间范围，将数组转换为开始和结束时间
    const searchParams = { ...formData.value }
    if (
      searchParams.operTime &&
      Array.isArray(searchParams.operTime) &&
      searchParams.operTime.length === 2
    ) {
      searchParams.operTimeStart = searchParams.operTime[0]
      searchParams.operTimeEnd = searchParams.operTime[1]
    }
    delete searchParams.operTime
    emit('search', searchParams)
  }
</script>
