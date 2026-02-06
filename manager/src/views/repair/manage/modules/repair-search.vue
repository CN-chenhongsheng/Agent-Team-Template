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
   * 报修状态选项（从字典加载）
   */
  const statusOptions = ref<Array<{ label: string; value: number }>>([])

  /**
   * 维修类型选项（从字典加载）
   */
  const repairTypeOptions = ref<Array<{ label: string; value: number }>>([])

  /**
   * 紧急程度选项（从字典加载）
   */
  const urgentLevelOptions = ref<Array<{ label: string; value: number }>>([])

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
      label: '报修类型',
      key: 'repairType',
      type: 'select',
      props: {
        clearable: true,
        placeholder: '请选择报修类型',
        options: repairTypeOptions.value
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
      label: '紧急程度',
      key: 'urgentLevel',
      type: 'select',
      props: {
        clearable: true,
        placeholder: '请选择紧急程度',
        options: urgentLevelOptions.value
      }
    },
    {
      label: '报修开始',
      key: 'createDateStart',
      type: 'date',
      props: {
        class: 'w-full',
        clearable: true,
        placeholder: '请选择报修日期开始',
        valueFormat: 'YYYY-MM-DD'
      }
    },
    {
      label: '报修结束',
      key: 'createDateEnd',
      type: 'date',
      props: {
        class: 'w-full',
        clearable: true,
        placeholder: '请选择报修日期结束',
        valueFormat: 'YYYY-MM-DD'
      }
    }
  ])

  /**
   * 加载字典数据
   */
  const loadDictData = async () => {
    try {
      const dictRes = await dictStore.loadDictDataBatch([
        'repair_status',
        'repair_type',
        'repair_urgent_level'
      ])

      // 解析报修状态字典
      statusOptions.value = (dictRes.repair_status || []).map(
        (item: Api.SystemManage.DictDataListItem) => ({
          label: item.label,
          value: Number(item.value)
        })
      )

      // 解析维修类型字典
      repairTypeOptions.value = (dictRes.repair_type || []).map(
        (item: Api.SystemManage.DictDataListItem) => ({
          label: item.label,
          value: Number(item.value)
        })
      )

      // 解析紧急程度字典
      urgentLevelOptions.value = (dictRes.repair_urgent_level || []).map(
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
   * 处理重置事件
   */
  const handleReset = () => {
    emit('reset')
  }

  /**
   * 处理搜索事件
   */
  const handleSearch = () => {
    emit('search', formData.value)
  }

  /**
   * 组件挂载时加载数据
   */
  onMounted(async () => {
    await loadDictData()
  })
</script>
