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

  // 字典 store
  const dictStore = useDictStore()
  // 参考数据 store
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
   * 性别类型选项（从字典加载）
   */
  const genderTypeOptions = ref<Array<{ label: string; value: number }>>([])

  /**
   * 系统状态选项（从字典加载）
   */
  const statusOptions = ref<Array<{ label: string; value: number }>>([])

  /**
   * 校区选项（转换为 select 组件需要的格式）
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
      label: '楼层编码',
      key: 'floorCode',
      type: 'input',
      props: { clearable: true, placeholder: '请输入楼层编码' }
    },
    {
      label: '楼层名称',
      key: 'floorName',
      type: 'input',
      props: { clearable: true, placeholder: '请输入楼层名称' }
    },
    {
      label: '位置',
      key: 'campusCode',
      type: 'select',
      props: {
        clearable: true,
        placeholder: '请选择位置',
        options: campusOptions.value
      }
    },
    {
      label: '性别类型',
      key: 'genderType',
      type: 'select',
      props: {
        clearable: true,
        placeholder: '请选择性别类型',
        options: genderTypeOptions.value
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
      const dictRes = await dictStore.loadDictDataBatch([
        'dormitory_gender_type',
        'sys_common_status'
      ])

      // 解析性别类型字典
      genderTypeOptions.value = (dictRes.dormitory_gender_type || []).map(
        (item: Api.SystemManage.DictDataListItem) => ({
          label: item.label,
          value: Number(item.value)
        })
      )

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
