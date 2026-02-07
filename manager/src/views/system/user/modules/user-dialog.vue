<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogType === 'add' ? '添加用户' : '编辑用户'"
    width="600px"
    align-center
    @close="handleClose"
  >
    <ArtForm
      ref="formRef"
      v-model="formData"
      :items="formItems"
      :span="12"
      :gutter="20"
      label-width="90px"
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
  import { fetchAddUser, fetchUpdateUser } from '@/api/system-manage'
  import { useReferenceStore } from '@/store/modules/reference'
  import type { FormRules } from 'element-plus'
  import ArtForm from '@/components/core/forms/art-form/index.vue'
  import type { FormItem } from '@/components/core/forms/art-form/index.vue'

  interface Props {
    visible: boolean
    type: string
    userData?: Partial<Api.SystemManage.UserListItem>
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'submit'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  // 角色列表数据
  const roleList = ref<Api.SystemManage.RoleListItem[]>([])
  const submitLoading = ref(false)

  // 使用参考数据 store
  const referenceStore = useReferenceStore()

  // 对话框显示控制
  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const dialogType = computed(() => props.type)

  /**
   * 是否为超级管理员
   */
  const isSuperAdmin = computed(() => {
    return props.type === 'edit' && props.userData?.username === 'superAdmin'
  })

  // 表单实例
  const formRef = ref()

  /**
   * 表单配置项
   */
  const formItems = computed<FormItem[]>(() => [
    {
      key: 'username',
      label: '用户名',
      type: 'input',
      span: 12,
      props: {
        placeholder: '请输入用户名',
        disabled: dialogType.value === 'edit'
      }
    },
    {
      key: 'nickname',
      label: '昵称',
      type: 'input',
      span: 12,
      props: {
        placeholder: '请输入昵称'
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
      key: 'roleIds',
      label: '角色',
      type: 'select',
      span: 24,
      props: {
        multiple: true,
        placeholder: '请选择角色',
        style: 'width: 100%',
        options: roleList.value.map((role) => ({
          value: role.id,
          label: role.roleName
        }))
      }
    }
  ])

  // 表单数据
  const formData = reactive<Api.SystemManage.UserSaveParams>({
    username: '',
    nickname: '',
    email: '',
    phone: '',
    status: 1,
    roleIds: []
  })

  // 表单验证规则
  const rules: FormRules = {
    username: [
      { required: true, message: '请输入用户名', trigger: 'blur' },
      {
        pattern: /^[a-zA-Z0-9_]{4,20}$/,
        message: '用户名只能包含字母、数字、下划线，长度4-20位',
        trigger: 'blur'
      }
    ],
    nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
    email: [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }],
    phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }]
  }

  /**
   * 加载角色列表（使用 store 缓存）
   */
  const loadRoles = async () => {
    try {
      roleList.value = await referenceStore.loadAllRoles()
    } catch (error) {
      console.error('加载角色列表失败:', error)
    }
  }

  /**
   * 初始化表单数据
   */
  const initFormData = () => {
    if (props.type === 'edit' && props.userData) {
      const row = props.userData
      Object.assign(formData, {
        id: row.id,
        username: row.username || '',
        nickname: row.nickname || '',
        email: row.email || '',
        phone: row.phone || '',
        status: row.status ?? 1,
        roleIds: row.roleIds || []
      })
    } else {
      // 重置表单
      Object.assign(formData, {
        id: undefined,
        username: '',
        nickname: '',
        email: '',
        phone: '',
        status: 1,
        roleIds: []
      })
    }
  }

  /**
   * 监听对话框状态变化
   */
  watch(
    () => [props.visible, props.type, props.userData],
    ([visible]) => {
      if (visible) {
        initFormData()
        loadRoles()
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
      // 使用 ArtForm 暴露的 validate 方法
      await formRef.value.validate()

      // 超级管理员不允许设置为停用
      if (isSuperAdmin.value && formData.status === 0) {
        ElMessage.warning('超级管理员不允许停用')
        return
      }

      submitLoading.value = true

      // 准备提交数据
      const submitData: Api.SystemManage.UserSaveParams = {
        ...formData
      }

      // 调用API
      if (props.type === 'add') {
        await fetchAddUser(submitData)
      } else {
        await fetchUpdateUser(formData.id!, submitData)
      }

      dialogVisible.value = false
      emit('submit')
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
