<!-- 学生新增/编辑 Dialog 弹窗 -->
<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogType === 'add' ? '新增学生' : '编辑学生'"
    width="800px"
    align-center
    @close="handleClose"
  >
    <ArtForm
      ref="formRef"
      v-model="formData"
      :items="formItems"
      :span="12"
      :gutter="20"
      label-width="100px"
      :rules="rules"
      :showSubmit="false"
      :showReset="false"
    />

    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ submitLoading ? '提交中...' : '确定' }}
        </ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { ref, watch, computed, reactive, nextTick } from 'vue'
  import { ElDialog, ElButton } from 'element-plus'
  import type { FormRules } from 'element-plus'
  import { fetchAddStudent, fetchUpdateStudent, fetchGetStudentDetail } from '@/api/student-manage'
  import { useDictStore } from '@/store/modules/dict'
  import ArtForm from '@/components/core/forms/art-form/index.vue'
  import type { FormItem } from '@/components/core/forms/art-form/index.vue'

  defineOptions({ name: 'StudentDialog' })

  interface Props {
    /** 是否显示 */
    visible: boolean
    /** 对话框类型 */
    dialogType: 'add' | 'edit'
    /** 学生ID */
    studentId?: number
    /** 学生数据 */
    studentData?: Api.StudentManage.StudentDetail | null
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'close'): void
    (e: 'saved'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const submitLoading = ref(false)
  const formRef = ref()

  // 字典 store
  const dictStore = useDictStore()

  // 字典选项
  const nationOptions = ref<Array<{ label: string; value: string }>>([])
  const politicalStatusOptions = ref<Array<{ label: string; value: string }>>([])
  const academicStatusOptions = ref<Array<{ label: string; value: string }>>([])

  // 对话框显示控制
  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  /**
   * 表单配置项
   */
  const formItems = computed<FormItem[]>(() => [
    {
      key: 'studentNo',
      label: '学号',
      type: 'input',
      span: 12,
      props: {
        placeholder: '请输入学号',
        disabled: props.dialogType === 'edit'
      }
    },
    {
      key: 'studentName',
      label: '姓名',
      type: 'input',
      span: 12,
      props: {
        placeholder: '请输入姓名'
      }
    },
    {
      key: 'gender',
      label: '性别',
      type: 'select',
      span: 12,
      props: {
        placeholder: '请选择性别',
        style: 'width: 100%',
        options: [
          { label: '男', value: 1 },
          { label: '女', value: 2 }
        ]
      }
    },
    {
      key: 'phone',
      label: '手机号',
      type: 'input',
      span: 12,
      props: {
        placeholder: '请输入手机号',
        maxlength: 11
      }
    },
    {
      key: 'email',
      label: '邮箱',
      type: 'input',
      span: 12,
      props: {
        placeholder: '请输入邮箱'
      }
    },
    {
      key: 'idCard',
      label: '身份证号',
      type: 'input',
      span: 12,
      props: {
        placeholder: '请输入身份证号',
        maxlength: 18
      }
    },
    {
      key: 'birthDate',
      label: '出生日期',
      type: 'date',
      span: 12,
      props: {
        placeholder: '请选择出生日期',
        style: 'width: 100%',
        valueFormat: 'YYYY-MM-DD'
      }
    },
    {
      key: 'nation',
      label: '民族',
      type: 'select',
      span: 12,
      props: {
        placeholder: '请选择或搜索民族',
        filterable: true,
        style: 'width: 100%',
        options: nationOptions.value
      }
    },
    {
      key: 'politicalStatus',
      label: '政治面貌',
      type: 'select',
      span: 12,
      props: {
        placeholder: '请选择政治面貌',
        style: 'width: 100%',
        options: politicalStatusOptions.value
      }
    },
    {
      key: 'enrollmentYear',
      label: '入学年份',
      type: 'date',
      span: 12,
      props: {
        type: 'year',
        placeholder: '请选择入学年份',
        style: 'width: 100%',
        valueFormat: 'YYYY'
      }
    },
    {
      key: 'schoolingLength',
      label: '学制',
      type: 'number',
      span: 12,
      props: {
        placeholder: '请输入学制（年）',
        min: 1,
        max: 10,
        style: 'width: 100%'
      }
    },
    {
      key: 'academicStatus',
      label: '学籍状态',
      type: 'select',
      span: 12,
      props: {
        placeholder: '请选择学籍状态',
        style: 'width: 100%',
        options: academicStatusOptions.value.map((item) => ({
          label: item.label,
          value: Number(item.value)
        }))
      }
    },
    {
      key: 'parentName',
      label: '家长姓名',
      type: 'input',
      span: 12,
      props: {
        placeholder: '请输入家长姓名'
      }
    },
    {
      key: 'parentPhone',
      label: '家长电话',
      type: 'input',
      span: 12,
      props: {
        placeholder: '请输入家长电话',
        maxlength: 11
      }
    },
    {
      key: 'emergencyContact',
      label: '紧急联系人',
      type: 'input',
      span: 12,
      props: {
        placeholder: '请输入紧急联系人'
      }
    },
    {
      key: 'emergencyPhone',
      label: '紧急联系电话',
      type: 'input',
      span: 12,
      props: {
        placeholder: '请输入紧急联系电话',
        maxlength: 11
      }
    },
    {
      key: 'homeAddress',
      label: '家庭地址',
      type: 'input',
      span: 24,
      props: {
        type: 'textarea',
        rows: 2,
        placeholder: '请输入家庭地址'
      }
    },
    {
      key: 'remark',
      label: '备注',
      type: 'input',
      span: 24,
      props: {
        type: 'textarea',
        rows: 3,
        placeholder: '请输入备注'
      }
    }
  ])

  // 表单数据
  const formData = reactive<Api.StudentManage.StudentSaveParams>({
    studentNo: '',
    studentName: '',
    gender: undefined,
    idCard: '',
    phone: '',
    email: '',
    birthDate: '',
    nation: '',
    politicalStatus: '',
    enrollmentYear: undefined,
    schoolingLength: undefined,
    academicStatus: undefined,
    homeAddress: '',
    emergencyContact: '',
    emergencyPhone: '',
    parentName: '',
    parentPhone: '',
    remark: ''
  })

  // 表单验证规则
  const rules: FormRules = {
    studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
    studentName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
    phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }],
    email: [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }],
    idCard: [
      {
        pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/,
        message: '请输入正确的身份证号格式',
        trigger: 'blur'
      }
    ],
    emergencyPhone: [
      { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
    ],
    parentPhone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }]
  }

  /**
   * 加载字典数据
   */
  const loadDictData = async (): Promise<void> => {
    try {
      const dictRes = await dictStore.loadDictDataBatch([
        'student_nation',
        'student_political_status',
        'academic_status'
      ])

      nationOptions.value = (dictRes.student_nation || []).map(
        (item: Api.SystemManage.DictDataListItem) => ({
          label: item.label,
          value: item.value
        })
      )

      politicalStatusOptions.value = (dictRes.student_political_status || []).map(
        (item: Api.SystemManage.DictDataListItem) => ({
          label: item.label,
          value: item.value
        })
      )

      academicStatusOptions.value = (dictRes.academic_status || []).map(
        (item: Api.SystemManage.DictDataListItem) => ({
          label: item.label,
          value: item.value
        })
      )
    } catch (error) {
      console.error('加载字典数据失败:', error)
    }
  }

  /**
   * 初始化表单数据
   */
  const initFormData = async () => {
    if (props.dialogType === 'edit') {
      if (props.studentData) {
        const row = props.studentData
        Object.assign(formData, {
          id: row.id,
          studentNo: row.studentNo || '',
          studentName: row.studentName || '',
          gender: row.gender,
          idCard: row.idCard || '',
          phone: row.phone || '',
          email: row.email || '',
          birthDate: row.birthDate || '',
          nation: row.nation || '',
          politicalStatus: row.politicalStatus || '',
          enrollmentYear: row.enrollmentYear,
          schoolingLength: row.schoolingLength,
          academicStatus: row.academicStatus,
          homeAddress: row.homeAddress || '',
          emergencyContact: row.emergencyContact || '',
          emergencyPhone: row.emergencyPhone || '',
          parentName: row.parentName || '',
          parentPhone: row.parentPhone || '',
          remark: row.remark || ''
        })
      } else if (props.studentId) {
        try {
          const res = await fetchGetStudentDetail(props.studentId)
          if (res) {
            Object.assign(formData, {
              id: res.id,
              studentNo: res.studentNo || '',
              studentName: res.studentName || '',
              gender: res.gender,
              idCard: res.idCard || '',
              phone: res.phone || '',
              email: res.email || '',
              birthDate: res.birthDate || '',
              nation: res.nation || '',
              politicalStatus: res.politicalStatus || '',
              enrollmentYear: res.enrollmentYear,
              schoolingLength: res.schoolingLength,
              academicStatus: res.academicStatus,
              homeAddress: res.homeAddress || '',
              emergencyContact: res.emergencyContact || '',
              emergencyPhone: res.emergencyPhone || '',
              parentName: res.parentName || '',
              parentPhone: res.parentPhone || '',
              remark: res.remark || ''
            })
          }
        } catch (error) {
          ElMessage.error('获取学生详情失败')
          console.error('获取学生详情失败:', error)
        }
      }
    } else {
      Object.assign(formData, {
        id: undefined,
        studentNo: '',
        studentName: '',
        gender: undefined,
        idCard: '',
        phone: '',
        email: '',
        birthDate: '',
        nation: '',
        politicalStatus: '',
        enrollmentYear: undefined,
        schoolingLength: undefined,
        academicStatus: undefined,
        homeAddress: '',
        emergencyContact: '',
        emergencyPhone: '',
        parentName: '',
        parentPhone: '',
        remark: ''
      })
    }
  }

  /**
   * 监听对话框状态变化
   */
  watch(
    () => [props.visible, props.dialogType, props.studentId, props.studentData],
    ([visible]) => {
      if (visible) {
        loadDictData()
        initFormData()
        nextTick(() => {
          formRef.value?.ref?.clearValidate()
        })
      }
    },
    { immediate: true }
  )

  /**
   * 提交表单
   */
  const handleSubmit = async () => {
    if (!formRef.value) return

    try {
      await formRef.value.validate()

      submitLoading.value = true

      const submitData: Api.StudentManage.StudentSaveParams = {
        ...formData
      }

      if (submitData.enrollmentYear && typeof submitData.enrollmentYear === 'string') {
        submitData.enrollmentYear = parseInt(submitData.enrollmentYear)
      }

      if (props.dialogType === 'add') {
        await fetchAddStudent(submitData)
      } else {
        await fetchUpdateStudent(submitData)
      }

      dialogVisible.value = false
      emit('saved')
    } catch (error) {
      console.error('提交失败:', error)
    } finally {
      submitLoading.value = false
    }
  }

  /**
   * 关闭对话框
   */
  const handleClose = () => {
    formRef.value?.reset()
  }
</script>

<style scoped lang="scss">
  .dialog-footer {
    display: flex;
    gap: 10px;
    justify-content: flex-end;
  }
</style>
