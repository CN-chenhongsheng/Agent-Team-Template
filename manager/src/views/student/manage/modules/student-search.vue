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
   * 性别选项（从字典加载）
   */
  const genderOptions = ref<Array<{ label: string; value: number }>>([])

  /**
   * 民族选项（从字典加载）
   */
  const nationOptions = ref<Array<{ label: string; value: string }>>([])

  /**
   * 政治面貌选项（从字典加载）
   */
  const politicalStatusOptions = ref<Array<{ label: string; value: string }>>([])

  /**
   * 学籍状态选项（从字典加载）
   */
  const academicStatusOptions = ref<Array<{ label: string; value: number }>>([])

  /**
   * 系统状态选项（从字典加载）
   */
  const statusOptions = ref<Array<{ label: string; value: number }>>([])

  /**
   * 组织位置级联选择器配置（校区 → 院系 → 专业）
   */
  const orgCascaderProps = {
    lazy: true,
    checkStrictly: true,
    lazyLoad: (node: any, resolve: any) => {
      referenceStore.orgCascaderLazyLoad(node, resolve, 3)
    }
  }

  /**
   * 宿舍位置级联选择器配置（校区 → 楼层 → 房间）
   */
  const dormCascaderProps = {
    lazy: true,
    checkStrictly: true,
    lazyLoad: (node: any, resolve: any) => {
      referenceStore.cascaderLazyLoad(node, resolve, 3)
    }
  }

  /**
   * 组织位置级联选择器值变化处理
   */
  const handleOrgCascaderChange = (value: string[]) => {
    if (value && value.length > 0) {
      formData.value.campusCode = value[0] || ''
      formData.value.deptCode = value[1] || ''
      formData.value.majorCode = value[2] || ''
    } else {
      formData.value.campusCode = ''
      formData.value.deptCode = ''
      formData.value.majorCode = ''
    }
  }

  /**
   * 宿舍位置级联选择器值变化处理
   */
  const handleDormCascaderChange = (value: string[]) => {
    if (value && value.length > 0) {
      formData.value.dormCampusCode = value[0] || ''
      formData.value.floorCode = value[1] || ''
      formData.value.roomCode = value[2] || ''
    } else {
      formData.value.dormCampusCode = ''
      formData.value.floorCode = ''
      formData.value.roomCode = ''
    }
  }

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
      label: '姓名',
      key: 'studentName',
      type: 'input',
      props: { clearable: true, placeholder: '请输入姓名' }
    },
    {
      label: '手机号',
      key: 'phone',
      type: 'input',
      props: { clearable: true, placeholder: '请输入手机号', maxlength: 11 }
    },
    {
      label: '性别',
      key: 'gender',
      type: 'select',
      props: {
        clearable: true,
        placeholder: '请选择性别',
        options: genderOptions.value
      }
    },
    {
      label: '组织',
      key: 'orgCascader',
      type: 'cascader',
      props: {
        class: 'w-full',
        clearable: true,
        placeholder: '请选择组织',
        props: orgCascaderProps,
        onChange: handleOrgCascaderChange
      }
    },
    {
      label: '宿舍',
      key: 'dormCascader',
      type: 'cascader',
      props: {
        class: 'w-full',
        clearable: true,
        placeholder: '请选择宿舍',
        props: dormCascaderProps,
        onChange: handleDormCascaderChange
      }
    },
    {
      label: '民族',
      key: 'nation',
      type: 'select',
      props: {
        clearable: true,
        filterable: true,
        placeholder: '请选择民族',
        options: nationOptions.value
      }
    },
    {
      label: '政治面貌',
      key: 'politicalStatus',
      type: 'select',
      props: {
        clearable: true,
        placeholder: '请选择政治面貌',
        options: politicalStatusOptions.value
      }
    },
    {
      label: '学籍状态',
      key: 'academicStatus',
      type: 'select',
      props: {
        clearable: true,
        placeholder: '请选择学籍状态',
        options: academicStatusOptions.value
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
        'sys_user_sex',
        'student_nation',
        'student_political_status',
        'academic_status',
        'sys_common_status'
      ])

      // 解析性别字典
      genderOptions.value = (dictRes.sys_user_sex || []).map(
        (item: Api.SystemManage.DictDataListItem) => ({
          label: item.label,
          value: Number(item.value)
        })
      )

      // 解析民族字典
      nationOptions.value = (dictRes.student_nation || []).map(
        (item: Api.SystemManage.DictDataListItem) => ({
          label: item.label,
          value: item.value
        })
      )

      // 解析政治面貌字典
      politicalStatusOptions.value = (dictRes.student_political_status || []).map(
        (item: Api.SystemManage.DictDataListItem) => ({
          label: item.label,
          value: item.value
        })
      )

      // 解析学籍状态字典
      academicStatusOptions.value = (dictRes.academic_status || []).map(
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
    formData.value.deptCode = ''
    formData.value.majorCode = ''
    formData.value.dormCampusCode = ''
    formData.value.floorCode = ''
    formData.value.roomCode = ''
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
