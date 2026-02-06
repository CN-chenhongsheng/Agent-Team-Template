<!-- 专业编辑弹窗 -->
<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <ArtForm
      ref="formRef"
      v-model="form"
      :items="formItems"
      :span="12"
      :gutter="20"
      label-width="100px"
      :rules="rules"
      :showSubmit="false"
      :showReset="false"
    />

    <template #footer>
      <ElButton @click="handleClose">取消</ElButton>
      <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">
        {{ submitLoading ? '提交中...' : '确定' }}
      </ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import {
    fetchGetDepartmentTree,
    fetchGetMajorDetail,
    fetchAddMajor,
    fetchUpdateMajor
  } from '@/api/school-manage'
  import { useDictStore } from '@/store/modules/dict'
  import { ElInputNumber } from 'element-plus'
  import type { FormRules } from 'element-plus'
  import ArtForm from '@/components/core/forms/art-form/index.vue'
  import type { FormItem } from '@/components/core/forms/art-form/index.vue'

  interface Props {
    visible: boolean
    type: 'add' | 'edit'
    editData?: Partial<Api.SystemManage.MajorListItem> | null
  }

  interface Emits {
    (e: 'update:visible', visible: boolean): void
    (e: 'submit'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    editData: null
  })

  const emit = defineEmits<Emits>()

  const formRef = ref()
  const submitLoading = ref(false)
  const deptLoading = ref(false)
  const deptOptions = ref<Api.SystemManage.DepartmentListItem[]>([])
  const degreeTypeOptions = ref<Array<{ label: string; value: string }>>([])

  // 使用字典 store
  const dictStore = useDictStore()

  // 学制数字值（用于数字输入框）
  const durationNumber = ref<number | null>(null)

  const dialogVisible = computed({
    get: () => props.visible,
    set: (val) => emit('update:visible', val)
  })

  const isEdit = computed(() => props.type === 'edit')

  const dialogTitle = computed(() => (isEdit.value ? '编辑专业' : '新增专业'))

  /**
   * 表单配置项
   */
  const formItems = computed<FormItem[]>(() => [
    {
      key: 'majorCode',
      label: '专业编码',
      type: 'input',
      span: 24,
      props: {
        placeholder: '请输入专业编码',
        disabled: isEdit.value
      }
    },
    {
      key: 'majorName',
      label: '专业名称',
      type: 'input',
      span: 24,
      props: {
        placeholder: '请输入专业名称'
      }
    },
    {
      key: 'deptCode',
      label: '所属院系',
      type: 'select',
      span: 24,
      props: {
        placeholder: '请选择所属院系',
        filterable: true,
        clearable: true,
        loading: deptLoading.value,
        onFocus: handleDeptFocus,
        options: deptOptions.value.map((dept) => ({
          label: `${dept.deptName} (${dept.deptCode})`,
          value: dept.deptCode
        }))
      }
    },
    {
      key: 'director',
      label: '专业负责人',
      type: 'input',
      span: 12,
      props: {
        placeholder: '请输入专业负责人'
      }
    },
    {
      key: 'type',
      label: '学位类型',
      type: 'select',
      span: 12,
      props: {
        placeholder: '请选择学位类型',
        clearable: true,
        filterable: true,
        options: degreeTypeOptions.value
      }
    },
    {
      key: 'duration',
      label: '学制',
      type: 'number',
      span: 24,
      render: () =>
        h(ElInputNumber, {
          modelValue: durationNumber.value,
          'onUpdate:modelValue': (val: number | undefined) => {
            durationNumber.value = val ?? null
            form.duration = formatDuration(val ?? null)
          },
          min: 1,
          max: 10,
          precision: 0,
          placeholder: '请输入学制年限',
          controlsPosition: 'right',
          formatter: (value: string) => `${value}年制`,
          parser: (value: string) => value.replace('年制', ''),
          style: 'width: 100%'
        })
    },
    {
      key: 'goal',
      label: '培养目标',
      type: 'input',
      span: 24,
      props: {
        type: 'textarea',
        rows: 4,
        placeholder: '请输入培养目标'
      }
    }
  ])

  const form = reactive<Api.SystemManage.MajorSaveParams>({
    majorCode: '',
    majorName: '',
    deptCode: '',
    director: undefined,
    type: undefined,
    duration: '',
    goal: undefined
  })

  const rules = reactive<FormRules>({
    majorCode: [{ required: true, message: '请输入专业编码', trigger: 'blur' }],
    majorName: [{ required: true, message: '请输入专业名称', trigger: 'blur' }],
    deptCode: [{ required: true, message: '请选择所属院系', trigger: 'change' }],
    type: [{ required: true, message: '请选择学位类型', trigger: 'change' }],
    duration: [
      {
        required: true,
        validator: (_rule, _value, callback) => {
          if (durationNumber.value === null || durationNumber.value === undefined) {
            callback(new Error('请输入学制'))
          } else {
            callback()
          }
        },
        trigger: 'blur'
      }
    ]
  })

  /**
   * 加载学位类型字典
   */
  const loadDegreeTypeOptions = async (): Promise<void> => {
    try {
      const data = await dictStore.loadDictData('degree_type')
      degreeTypeOptions.value = data
        .filter((item) => item.status === 1)
        .map((item) => ({
          label: item.label,
          value: item.value
        }))
    } catch (error) {
      console.error('加载学位类型字典失败:', error)
    }
  }

  /**
   * 加载院系列表
   */
  const loadDepartmentOptions = async (): Promise<void> => {
    try {
      const list = await fetchGetDepartmentTree()
      deptOptions.value = flattenDepartmentTree(list)
    } catch (error) {
      console.error('加载院系列表失败:', error)
    }
  }

  /**
   * 扁平化院系树
   */
  const flattenDepartmentTree = (
    list: Api.SystemManage.DepartmentListItem[]
  ): Api.SystemManage.DepartmentListItem[] => {
    const result: Api.SystemManage.DepartmentListItem[] = []
    const flatten = (items: Api.SystemManage.DepartmentListItem[]) => {
      items.forEach((item) => {
        result.push(item)
        if (item.children && item.children.length > 0) {
          flatten(item.children)
        }
      })
    }
    flatten(list)
    return result
  }

  /**
   * 院系下拉框获取焦点时加载数据
   */
  const handleDeptFocus = async (): Promise<void> => {
    if (deptOptions.value.length === 0) {
      deptLoading.value = true
      try {
        await loadDepartmentOptions()
      } finally {
        deptLoading.value = false
      }
    }
  }

  /**
   * 从字符串中提取数字（用于从后端数据中解析学制）
   */
  const parseDuration = (durationStr: string | undefined): number | null => {
    if (!durationStr) return null
    const match = durationStr.match(/^(\d+)/)
    return match ? Number.parseInt(match[1], 10) : null
  }

  /**
   * 将数字转换为学制字符串（用于提交到后端）
   */
  const formatDuration = (num: number | null): string => {
    return num ? `${num}年制` : ''
  }

  /**
   * 加载表单数据
   */
  const loadFormData = async (): Promise<void> => {
    if (isEdit.value && props.editData?.id) {
      try {
        const detail = await fetchGetMajorDetail(props.editData.id)
        Object.assign(form, {
          id: detail.id,
          majorCode: detail.majorCode,
          majorName: detail.majorName,
          deptCode: detail.deptCode,
          director: detail.director || undefined,
          type: detail.type || undefined,
          duration: detail.duration,
          goal: detail.goal || undefined
        })
        durationNumber.value = parseDuration(detail.duration)
        if (detail.deptCode) {
          const list = await fetchGetDepartmentTree({ deptCode: detail.deptCode })
          deptOptions.value = flattenDepartmentTree(list)
        }
      } catch (error) {
        console.error('加载专业详情失败:', error)
      }
    } else {
      resetForm()
    }
  }

  /**
   * 重置表单
   */
  const resetForm = (): void => {
    Object.assign(form, {
      majorCode: '',
      majorName: '',
      deptCode: '',
      director: undefined,
      type: undefined,
      duration: '',
      goal: undefined
    })
    durationNumber.value = null
    formRef.value?.ref?.clearValidate()
  }

  /**
   * 提交表单
   */
  const handleSubmit = async (): Promise<void> => {
    if (!formRef.value) return

    try {
      await formRef.value.validate()

      form.duration = formatDuration(durationNumber.value)

      submitLoading.value = true
      if (isEdit.value) {
        await fetchUpdateMajor(form.id!, form)
      } else {
        await fetchAddMajor(form)
      }
      emit('submit')
      handleClose()
    } catch (error) {
      console.error('提交失败:', error)
    } finally {
      submitLoading.value = false
    }
  }

  /**
   * 关闭弹窗
   */
  const handleClose = (): void => {
    resetForm()
    emit('update:visible', false)
  }

  watch(
    () => props.visible,
    (val) => {
      if (val) {
        loadDegreeTypeOptions()
        loadDepartmentOptions()
        loadFormData()
      }
    },
    { immediate: true }
  )
</script>
