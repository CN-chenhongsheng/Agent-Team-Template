<template>
  <ArtSearchBar
    ref="searchBarRef"
    v-model="formData"
    :items="formItems"
    @reset="handleReset"
    @search="handleSearch"
  >
  </ArtSearchBar>
</template>

<script setup lang="ts">
  import { useDictStore } from '@/store/modules/dict'

  interface Props {
    modelValue: Record<string, any> & { publishTimeRange?: string[] }
  }
  interface Emits {
    (e: 'update:modelValue', value: Record<string, any>): void
    (e: 'search', params: Api.SystemManage.NoticeSearchParams): void
    (e: 'reset'): void
  }
  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  // 表单数据双向绑定
  const searchBarRef = ref()
  const formData = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
  })

  // 字典store
  const dictStore = useDictStore()

  // 通知类型选项
  const noticeTypeOptions = ref<Array<{ label: string; value: number }>>([])

  // 通知状态选项
  const statusOptions = ref<Array<{ label: string; value: number }>>([])

  // 加载字典数据
  const loadDictData = async () => {
    try {
      const dictRes = await dictStore.loadDictDataBatch(['notice_type', 'notice_status'])

      // 解析通知类型字典
      noticeTypeOptions.value = (dictRes.notice_type || []).map(
        (item: Api.SystemManage.DictDataListItem) => ({
          label: item.label,
          value: Number(item.value)
        })
      )

      // 解析通知状态字典
      statusOptions.value = (dictRes.notice_status || []).map(
        (item: Api.SystemManage.DictDataListItem) => ({
          label: item.label,
          value: Number(item.value)
        })
      )
    } catch (error) {
      console.error('加载字典数据失败:', error)
    }
  }

  // 初始化加载字典
  onMounted(() => {
    loadDictData()
  })

  // 表单配置
  const formItems = computed(() => [
    {
      label: '通知标题',
      key: 'title',
      type: 'input',
      placeholder: '请输入通知标题',
      clearable: true
    },
    {
      label: '通知类型',
      key: 'noticeType',
      type: 'select',
      props: {
        placeholder: '请选择通知类型',
        clearable: true,
        options: noticeTypeOptions.value
      }
    },
    {
      label: '发布人',
      key: 'publisherName',
      type: 'input',
      placeholder: '请输入发布人姓名',
      clearable: true
    },
    {
      label: '状态',
      key: 'status',
      type: 'select',
      props: {
        placeholder: '请选择状态',
        clearable: true,
        options: statusOptions.value
      }
    },
    {
      label: '发布时间',
      key: 'publishTimeRange',
      type: 'date-range-picker',
      props: {
        type: 'daterange',
        startPlaceholder: '开始日期',
        endPlaceholder: '结束日期',
        clearable: true,
        valueFormat: 'YYYY-MM-DD'
      }
    }
  ])

  // 事件
  function handleReset() {
    emit('reset')
  }

  async function handleSearch() {
    await searchBarRef.value.validate()
    const params: any = { ...formData.value }

    // 转换时间范围为开始时间和结束时间
    if (params.publishTimeRange && Array.isArray(params.publishTimeRange)) {
      const [start, end] = params.publishTimeRange
      params.publishTimeStart = start
      params.publishTimeEnd = end
      delete params.publishTimeRange
    }

    emit('search', params as Api.SystemManage.NoticeSearchParams)
  }
</script>
