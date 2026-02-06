<!-- 校区编辑弹窗 -->
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
      :span="24"
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
  import { fetchAddCampus, fetchUpdateCampus } from '@/api/school-manage'
  import type { FormRules } from 'element-plus'
  import ArtForm from '@/components/core/forms/art-form/index.vue'
  import type { FormItem } from '@/components/core/forms/art-form/index.vue'

  interface Props {
    visible: boolean
    type: 'add' | 'edit'
    editData?: Api.SystemManage.CampusListItem | null
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

  const dialogVisible = computed({
    get: () => props.visible,
    set: (val) => emit('update:visible', val)
  })

  const isEdit = computed(() => props.type === 'edit')

  const dialogTitle = computed(() => {
    if (isEdit.value) return '编辑校区'
    return '新增校区'
  })

  /**
   * 表单配置项
   */
  const formItems = computed<FormItem[]>(() => [
    {
      key: 'campusCode',
      label: '校区编码',
      type: 'input',
      span: 24,
      props: {
        placeholder: '请输入校区编码',
        disabled: isEdit.value
      }
    },
    {
      key: 'campusName',
      label: '校区名称',
      type: 'input',
      span: 24,
      props: {
        placeholder: '请输入校区名称'
      }
    },
    {
      key: 'address',
      label: '校区地址',
      type: 'input',
      span: 24,
      props: {
        type: 'textarea',
        rows: 3,
        placeholder: '请输入校区地址'
      }
    },
    {
      key: 'manager',
      label: '负责人',
      type: 'input',
      span: 24,
      props: {
        placeholder: '请输入负责人姓名'
      }
    },
    {
      key: 'sort',
      label: '排序序号',
      type: 'number',
      span: 24,
      props: {
        min: 0,
        max: 9999
      }
    }
  ])

  const form = reactive<Api.SystemManage.CampusSaveParams>({
    campusCode: '',
    campusName: '',
    address: '',
    manager: undefined,
    status: 1,
    sort: 0
  })

  const rules = reactive<FormRules>({
    campusCode: [{ required: true, message: '请输入校区编码', trigger: 'blur' }],
    campusName: [{ required: true, message: '请输入校区名称', trigger: 'blur' }],
    address: [{ required: true, message: '请输入校区地址', trigger: 'blur' }]
  })

  /**
   * 加载表单数据
   */
  const loadFormData = (): void => {
    if (isEdit.value && props.editData) {
      Object.assign(form, {
        id: props.editData.id,
        campusCode: props.editData.campusCode,
        campusName: props.editData.campusName,
        address: props.editData.address,
        manager: props.editData.manager || undefined,
        status: props.editData.status,
        sort: props.editData.sort || 0
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
      campusCode: '',
      campusName: '',
      address: '',
      manager: undefined,
      status: 1,
      sort: 0
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
        await fetchUpdateCampus(form.id!, form)
      } else {
        await fetchAddCampus(form)
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
        loadFormData()
      }
    },
    { immediate: true }
  )
</script>
