<!-- 班级编辑弹窗 -->
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
  import { fetchGetClassDetail, fetchAddClass, fetchUpdateClass } from '@/api/school-manage'
  import { useReferenceStore } from '@/store/modules/reference'
  import type { FormRules } from 'element-plus'
  import ArtForm from '@/components/core/forms/art-form/index.vue'
  import type { FormItem } from '@/components/core/forms/art-form/index.vue'

  interface Props {
    visible: boolean
    type: 'add' | 'edit'
    editData?: Partial<Api.SystemManage.ClassListItem> | null
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
  const majorLoading = ref(false)
  const majorOptions = ref<Api.SystemManage.MajorListItem[]>([])
  const teacherLoading = ref(false)
  const teacherOptions = ref<Api.SystemManage.UserSimpleItem[]>([])

  const referenceStore = useReferenceStore()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (val) => emit('update:visible', val)
  })

  const isEdit = computed(() => props.type === 'edit')
  const dialogTitle = computed(() => (isEdit.value ? '编辑班级' : '新增班级'))

  const formItems = computed<FormItem[]>(() => [
    {
      key: 'classCode',
      label: '班级编码',
      type: 'input',
      span: 24,
      props: {
        placeholder: '请输入班级编码',
        disabled: isEdit.value
      }
    },
    {
      key: 'className',
      label: '班级名称',
      type: 'input',
      span: 24,
      props: {
        placeholder: '请输入班级名称'
      }
    },
    {
      key: 'majorCode',
      label: '所属专业',
      type: 'select',
      span: 24,
      props: {
        placeholder: '请选择所属专业',
        filterable: true,
        clearable: true,
        loading: majorLoading.value,
        onFocus: handleMajorFocus,
        options: majorOptions.value.map((major) => ({
          label: `${major.majorName} (${major.majorCode})`,
          value: major.majorCode
        }))
      }
    },
    {
      key: 'grade',
      label: '年级',
      type: 'input',
      span: 12,
      props: {
        placeholder: '如：2023'
      }
    },
    {
      key: 'enrollmentYear',
      label: '入学年份',
      type: 'number',
      span: 12,
      props: {
        min: 2000,
        max: 2100,
        placeholder: '如：2023'
      }
    },
    {
      key: 'teacherId',
      label: '负责人',
      type: 'select',
      span: 12,
      props: {
        placeholder: '请选择负责人',
        filterable: true,
        clearable: true,
        loading: teacherLoading.value,
        onFocus: handleTeacherFocus,
        options: teacherOptions.value.map((user) => ({
          label:
            user.nickname && user.phone
              ? `${user.nickname} (${user.phone})`
              : user.nickname || user.username,
          value: user.id
        }))
      }
    },
    {
      key: 'currentCount',
      label: '当前人数',
      type: 'number',
      span: 12,
      props: {
        min: 0,
        max: 9999
      }
    }
  ])

  const form = reactive<Api.SystemManage.ClassSaveParams>({
    classCode: '',
    className: '',
    majorCode: '',
    grade: '',
    teacherId: undefined,
    enrollmentYear: new Date().getFullYear(),
    currentCount: 0
  })

  const rules = reactive<FormRules>({
    classCode: [{ required: true, message: '请输入班级编码', trigger: 'blur' }],
    className: [{ required: true, message: '请输入班级名称', trigger: 'blur' }],
    majorCode: [{ required: true, message: '请选择所属专业', trigger: 'change' }],
    grade: [{ required: true, message: '请输入年级', trigger: 'blur' }],
    enrollmentYear: [{ required: true, message: '请输入入学年份', trigger: 'blur' }]
  })

  const loadMajorOptions = async (): Promise<void> => {
    majorLoading.value = true
    try {
      majorOptions.value = await referenceStore.loadMajorList()
    } catch (error) {
      console.error('加载专业列表失败:', error)
    } finally {
      majorLoading.value = false
    }
  }

  const handleMajorFocus = async (): Promise<void> => {
    if (majorOptions.value.length === 0) {
      await loadMajorOptions()
    }
  }

  const loadTeacherOptions = async (): Promise<void> => {
    if (teacherOptions.value.length > 0) return

    teacherLoading.value = true
    try {
      teacherOptions.value = await referenceStore.loadUsersByRoleCodes(['COUNSELOR'])
    } catch (error) {
      console.error('加载负责人列表失败:', error)
    } finally {
      teacherLoading.value = false
    }
  }

  const handleTeacherFocus = async (): Promise<void> => {
    await loadTeacherOptions()
  }

  const loadFormData = async (): Promise<void> => {
    if (isEdit.value && props.editData?.id) {
      try {
        const detail = await fetchGetClassDetail(props.editData.id)
        Object.assign(form, {
          id: detail.id,
          classCode: detail.classCode,
          className: detail.className,
          majorCode: detail.majorCode,
          grade: detail.grade,
          teacherId: detail.teacherId || undefined,
          enrollmentYear: detail.enrollmentYear,
          currentCount: detail.currentCount || 0
        })
        if (detail.majorCode) {
          majorOptions.value = await referenceStore.loadMajorList()
        }
      } catch (error) {
        console.error('加载班级详情失败:', error)
      }
    } else {
      resetForm()
    }
  }

  const resetForm = (): void => {
    Object.assign(form, {
      classCode: '',
      className: '',
      majorCode: '',
      grade: '',
      teacherId: undefined,
      enrollmentYear: new Date().getFullYear(),
      currentCount: 0
    })
    formRef.value?.ref?.clearValidate()
  }

  const handleSubmit = async (): Promise<void> => {
    if (!formRef.value) return

    try {
      await formRef.value.validate()

      submitLoading.value = true
      if (isEdit.value) {
        await fetchUpdateClass(form.id!, form)
      } else {
        await fetchAddClass(form)
      }
      emit('submit')
      handleClose()
    } catch (error) {
      console.error('提交失败:', error)
    } finally {
      submitLoading.value = false
    }
  }

  const handleClose = (): void => {
    resetForm()
    emit('update:visible', false)
  }

  watch(
    () => props.visible,
    (val) => {
      if (val) {
        loadMajorOptions()
        loadTeacherOptions()
        loadFormData()
      }
    },
    { immediate: true }
  )
</script>
