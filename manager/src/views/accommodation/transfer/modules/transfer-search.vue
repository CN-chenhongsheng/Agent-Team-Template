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
  import { useDictStore } from '@/store/modules/dict'
  import { useReferenceStore } from '@/store/modules/reference'

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
  const referenceStore = useReferenceStore()

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
   * 校区列表
   */
  const campusList = ref<Api.SystemManage.CampusListItem[]>([])

  /**
   * 调宿状态选项（从字典加载）
   */
  const statusOptions = ref<Array<{ label: string; value: number }>>([])

  /**
   * 校区选项
   */
  const campusOptions = computed(() =>
    campusList.value.map((item) => ({
      label: item.campusName,
      value: item.campusCode
    }))
  )

  /**
   * 搜索表单配置项
   */
  const formItems = computed(() => [
    {
      label: '学号',
      key: 'studentNo',
      type: 'input',
      props: { clearable: true, placeholder: '请输入学号' }
    },
    {
      label: '学生姓名',
      key: 'studentName',
      type: 'input',
      props: { clearable: true, placeholder: '请输入学生姓名' }
    },
    {
      label: '原校区',
      key: 'originalCampusCode',
      type: 'select',
      props: {
        clearable: true,
        placeholder: '请选择原校区',
        options: campusOptions.value
      }
    },
    {
      label: '目标校区',
      key: 'targetCampusCode',
      type: 'select',
      props: {
        clearable: true,
        placeholder: '请选择目标校区',
        options: campusOptions.value
      }
    },
    {
      label: '状态',
      key: 'status',
      type: 'select',
      props: {
        clearable: true,
        placeholder: '请选择状态',
        options: statusOptions.value
      }
    },
    {
      label: '申请开始',
      key: 'applyDateStart',
      type: 'date',
      props: {
        class: 'w-full',
        clearable: true,
        placeholder: '请选择申请日期开始',
        valueFormat: 'YYYY-MM-DD'
      }
    },
    {
      label: '申请结束',
      key: 'applyDateEnd',
      type: 'date',
      props: {
        class: 'w-full',
        clearable: true,
        placeholder: '请选择申请日期结束',
        valueFormat: 'YYYY-MM-DD'
      }
    }
  ])

  /**
   * 加载字典数据
   */
  const loadDictData = async () => {
    try {
      const dictRes = await dictStore.loadDictDataBatch(['transfer_status'])

      // 解析调宿状态字典
      statusOptions.value = (dictRes.transfer_status || []).map(
        (item: Api.SystemManage.DictDataListItem) => ({
          label: item.label,
          value: Number(item.value)
        })
      )
    } catch (error) {
      console.error('加载字典数据失败:', error)
    }
  }

  /**
   * 加载校区列表
   */
  const loadCampusList = async () => {
    try {
      campusList.value = await referenceStore.loadCampusTree()
    } catch (error) {
      console.error('加载校区列表失败:', error)
    }
  }

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
    emit('search', formData.value)
  }

  /**
   * 组件挂载时加载数据
   */
  onMounted(async () => {
    await Promise.all([loadDictData(), loadCampusList()])
  })
</script>
