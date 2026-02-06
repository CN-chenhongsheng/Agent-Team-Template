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
   * 房间类型选项（从字典加载）
   */
  const roomTypeOptions = ref<Array<{ label: string; value: string }>>([])

  /**
   * 房间状态选项（从字典加载）
   */
  const roomStatusOptions = ref<Array<{ label: string; value: number }>>([])

  /**
   * 系统状态选项（从字典加载）
   */
  const statusOptions = ref<Array<{ label: string; value: number }>>([])

  /**
   * 级联选择器配置
   */
  const cascaderProps = {
    lazy: true,
    checkStrictly: true, // 允许选择任意一级
    lazyLoad: (node: any, resolve: any) => {
      referenceStore.cascaderLazyLoad(node, resolve, 2) // 校区 → 楼层（2级）
    }
  }

  /**
   * 级联选择器值变化处理
   */
  const handleCascaderChange = (value: string[]) => {
    if (value && value.length > 0) {
      formData.value.campusCode = value[0] || ''
      formData.value.floorCode = value[1] || ''
    } else {
      formData.value.campusCode = ''
      formData.value.floorCode = ''
    }
  }

  /**
   * 搜索表单配置项
   */
  const formItems = computed(() => [
    {
      label: '房间编码',
      key: 'roomCode',
      type: 'input',
      props: { clearable: true, placeholder: '请输入房间编码' }
    },
    {
      label: '房间号',
      key: 'roomNumber',
      type: 'input',
      props: { clearable: true, placeholder: '请输入房间号' }
    },
    {
      label: '位置',
      key: 'locationCascader',
      type: 'cascader',
      props: {
        class: 'w-full',
        clearable: true,
        placeholder: '请选择位置',
        props: cascaderProps,
        onChange: handleCascaderChange
      }
    },
    {
      label: '房间类型',
      key: 'roomType',
      type: 'select',
      props: {
        clearable: true,
        placeholder: '请选择房间类型',
        options: roomTypeOptions.value
      }
    },
    {
      label: '房间状态',
      key: 'roomStatus',
      type: 'select',
      props: {
        clearable: true,
        placeholder: '请选择房间状态',
        options: roomStatusOptions.value
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
        'dormitory_room_type',
        'dormitory_room_status',
        'sys_common_status'
      ])

      // 解析房间类型字典
      roomTypeOptions.value = (dictRes.dormitory_room_type || []).map(
        (item: Api.SystemManage.DictDataListItem) => ({
          label: item.label,
          value: item.value
        })
      )

      // 解析房间状态字典
      roomStatusOptions.value = (dictRes.dormitory_room_status || []).map(
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
   * 处理重置事件
   */
  const handleReset = () => {
    // 清空级联选择器关联的字段
    formData.value.campusCode = ''
    formData.value.floorCode = ''
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
