<template>
  <ElDialog
    v-model="visible"
    :title="dialogType === 'add' ? '新增角色' : '编辑角色'"
    width="600px"
    align-center
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
  import { fetchAddRole, fetchUpdateRole } from '@/api/system-manage'
  import type { FormRules } from 'element-plus'
  import ArtForm from '@/components/core/forms/art-form/index.vue'
  import type { FormItem } from '@/components/core/forms/art-form/index.vue'

  type RoleListItem = Api.SystemManage.RoleListItem

  interface Props {
    modelValue: boolean
    dialogType: 'add' | 'edit'
    roleData?: RoleListItem
  }

  interface Emits {
    (e: 'update:modelValue', value: boolean): void
    (e: 'success'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    modelValue: false,
    dialogType: 'add',
    roleData: undefined
  })

  const emit = defineEmits<Emits>()

  const formRef = ref()
  const submitLoading = ref(false)

  /**
   * 弹窗显示状态双向绑定
   */
  const visible = computed({
    get: () => props.modelValue,
    set: (value) => emit('update:modelValue', value)
  })

  const dialogType = computed(() => props.dialogType)

  /**
   * 表单配置项
   */
  const formItems = computed<FormItem[]>(() => [
    {
      key: 'roleCode',
      label: '角色编码',
      type: 'input',
      span: 24,
      props: {
        placeholder: '请输入角色编码',
        disabled: dialogType.value === 'edit'
      }
    },
    {
      key: 'roleName',
      label: '角色名称',
      type: 'input',
      span: 24,
      props: {
        placeholder: '请输入角色名称'
      }
    },
    {
      key: 'sort',
      label: '排序',
      type: 'number',
      span: 24,
      props: {
        min: 0,
        max: 999,
        controlsPosition: 'right'
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
        placeholder: '请输入角色备注',
        maxlength: 200,
        showWordLimit: true
      }
    }
  ])

  /**
   * 表单验证规则
   */
  const rules = reactive<FormRules>({
    roleName: [
      { required: true, message: '请输入角色名称', trigger: 'blur' },
      { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
    ],
    roleCode: [
      { required: true, message: '请输入角色编码', trigger: 'blur' },
      { pattern: /^[A-Z_]+$/, message: '角色编码只能包含大写字母和下划线', trigger: 'blur' }
    ],
    sort: [{ required: true, message: '请输入排序值', trigger: 'blur' }]
  })

  /**
   * 表单数据
   */
  const form = reactive<Api.SystemManage.RoleSaveParams>({
    roleName: '',
    roleCode: '',
    sort: 0,
    status: 1,
    remark: ''
  })

  /**
   * 是否为超级管理员
   */
  const isSuperAdmin = computed(() => {
    return props.dialogType === 'edit' && props.roleData?.roleCode === 'SUPER_ADMIN'
  })

  /**
   * 监听弹窗打开，初始化表单数据
   */
  watch(
    () => props.modelValue,
    (newVal) => {
      if (newVal) initForm()
    }
  )

  /**
   * 监听角色数据变化，更新表单
   */
  watch(
    () => props.roleData,
    (newData) => {
      if (newData && props.modelValue) initForm()
    },
    { deep: true }
  )

  /**
   * 初始化表单数据
   */
  const initForm = () => {
    if (props.dialogType === 'edit' && props.roleData) {
      Object.assign(form, {
        id: props.roleData.id,
        roleName: props.roleData.roleName,
        roleCode: props.roleData.roleCode,
        sort: props.roleData.sort ?? 0,
        status: props.roleData.status ?? 1,
        remark: props.roleData.remark || ''
      })
    } else {
      Object.assign(form, {
        id: undefined,
        roleName: '',
        roleCode: '',
        sort: 0,
        status: 1,
        remark: ''
      })
    }
  }

  /**
   * 关闭弹窗并重置表单
   */
  const handleClose = () => {
    visible.value = false
    formRef.value?.reset()
  }

  /**
   * 提交表单
   */
  const handleSubmit = async () => {
    if (!formRef.value) return

    try {
      await formRef.value.validate()

      // 超级管理员不允许设置为停用
      if (isSuperAdmin.value && form.status === 0) {
        ElMessage.warning('超级管理员角色不允许停用')
        return
      }

      submitLoading.value = true

      const submitData: Api.SystemManage.RoleSaveParams = {
        ...form
      }

      if (props.dialogType === 'add') {
        await fetchAddRole(submitData)
      } else {
        await fetchUpdateRole(form.id!, submitData)
      }

      emit('success')
      handleClose()
    } catch (error) {
      console.error('提交失败:', error)
    } finally {
      submitLoading.value = false
    }
  }
</script>
