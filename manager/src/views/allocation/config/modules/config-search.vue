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
    algorithmOptions?: Api.Allocation.AlgorithmOption[]
  }

  interface Emits {
    (e: 'update:modelValue', value: Record<string, any>): void
    (e: 'search', params: Record<string, any>): void
    (e: 'reset'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    algorithmOptions: () => []
  })
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
   * 状态选项（从字典加载）
   */
  const statusOptions = ref<Array<{ label: string; value: number }>>([])

  /**
   * 算法选项（从props获取）
   */
  const algorithmSelectOptions = computed(() =>
    props.algorithmOptions.map((item) => ({
      label: item.name,
      value: item.type
    }))
  )

  /**
   * 搜索表单配置项
   */
  const formItems = computed(() => [
    {
      label: '配置名称',
      key: 'configName',
      type: 'input',
      props: { clearable: true, placeholder: '请输入配置名称' }
    },
    {
      label: '算法类型',
      key: 'algorithmType',
      type: 'select',
      props: {
        clearable: true,
        placeholder: '请选择算法类型',
        options: algorithmSelectOptions.value
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
    }
  ])

  /**
   * 加载字典数据
   */
  const loadDictData = async () => {
    try {
      const dictRes = await dictStore.loadDictDataBatch(['sys_common_status'])

      // 解析系统状态字典
      statusOptions.value = (dictRes.sys_common_status || []).map(
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
  const handleSearch = async () => {
    await searchBarRef.value.validate()
    emit('search', formData.value)
  }

  /**
   * 组件挂载时加载数据
   */
  onMounted(async () => {
    await loadDictData()
  })
</script>
