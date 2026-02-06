<!-- 院系编辑弹窗 -->
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
  import { fetchAddDepartment, fetchUpdateDepartment } from '@/api/school-manage'
  import { useReferenceStore } from '@/store/modules/reference'
  import type { FormRules } from 'element-plus'
  import ArtForm from '@/components/core/forms/art-form/index.vue'
  import type { FormItem } from '@/components/core/forms/art-form/index.vue'

  interface Props {
    visible: boolean
    type: 'add' | 'edit'
    editData?: Api.SystemManage.DepartmentListItem | null
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
  const campusOptions = ref<Api.SystemManage.CampusListItem[]>([])

  // 使用参考数据 store
  const referenceStore = useReferenceStore()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (val) => emit('update:visible', val)
  })

  const isEdit = computed(() => props.type === 'edit')

  const dialogTitle = computed(() => {
    if (isEdit.value) return '编辑院系'
    return '新增院系'
  })

  /**
   * 表单配置项
   */
  const formItems = computed<FormItem[]>(() => [
    {
      key: 'deptCode',
      label: '院系编码',
      type: 'input',
      span: 24,
      props: {
        placeholder: '请输入院系编码',
        disabled: isEdit.value
      }
    },
    {
      key: 'deptName',
      label: '院系名称',
      type: 'input',
      span: 24,
      props: {
        placeholder: '请输入院系名称'
      }
    },
    {
      key: 'campusCode',
      label: '所属校区',
      type: 'select',
      span: 24,
      props: {
        placeholder: '请选择所属校区',
        filterable: true,
        options: campusOptions.value.map((campus) => ({
          label: campus.campusName,
          value: campus.campusCode
        }))
      }
    },
    {
      key: 'leader',
      label: '院系领导',
      type: 'input',
      span: 12,
      props: {
        placeholder: '请输入院系领导'
      }
    },
    {
      key: 'phone',
      label: '联系电话',
      type: 'input',
      span: 12,
      props: {
        placeholder: '请输入联系电话'
      }
    },
    {
      key: 'sort',
      label: '排序',
      type: 'number',
      span: 12,
      props: {
        min: 0,
        max: 9999,
        placeholder: '请输入排序值'
      }
    }
  ])

  const form = reactive<Api.SystemManage.DepartmentSaveParams>({
    deptCode: '',
    deptName: '',
    campusCode: '',
    leader: undefined,
    phone: undefined,
    sort: 0,
    status: 1
  })

  const rules = reactive<FormRules>({
    deptCode: [{ required: true, message: '请输入院系编码', trigger: 'blur' }],
    deptName: [{ required: true, message: '请输入院系名称', trigger: 'blur' }],
    campusCode: [{ required: true, message: '请选择所属校区', trigger: 'change' }]
  })

  /**
   * 加载校区列表（使用 store 缓存）
   */
  const loadCampusOptions = async (): Promise<void> => {
    try {
      campusOptions.value = await referenceStore.loadCampusTree()
    } catch (error) {
      console.error('加载校区列表失败:', error)
    }
  }

  /**
   * 加载表单数据
   */
  const loadFormData = (): void => {
    if (isEdit.value && props.editData) {
      Object.assign(form, {
        id: props.editData.id,
        deptCode: props.editData.deptCode,
        deptName: props.editData.deptName,
        campusCode: props.editData.campusCode,
        leader: props.editData.leader || undefined,
        phone: props.editData.phone || undefined,
        sort: props.editData.sort || 0,
        status: props.editData.status
      })
    } else {
      resetForm()
    }
  }

  /**
   * 重置表单
   */
  const resetForm = (): void => {
    Object.assign(form, {
      deptCode: '',
      deptName: '',
      campusCode: '',
      leader: undefined,
      phone: undefined,
      sort: 0,
      status: 1
    })
    formRef.value?.ref?.clearValidate()
  }

  /**
   * 提交表单
   */
  const handleSubmit = async (): Promise<void> => {
    if (!formRef.value) return

    try {
      await formRef.value.validate()

      submitLoading.value = true
      if (isEdit.value) {
        await fetchUpdateDepartment(form.id!, form)
      } else {
        await fetchAddDepartment(form)
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
        loadCampusOptions()
        loadFormData()
      }
    },
    { immediate: true }
  )
</script>
