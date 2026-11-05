<template>
  <ElDialog
    v-model="dialogVisible"
    :title="
      dialogType === 'add' ? $t('system.user.form.addTitle') : $t('system.user.form.editTitle')
    "
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
        <ElButton @click="dialogVisible = false">{{ $t('common.cancel') }}</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ submitLoading ? $t('system.common.saving') : $t('common.confirm') }}
        </ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n'
  import { fetchAddUser, fetchUpdateUser, fetchGetRoleList } from '@/api/system-manage'
  import type { FormRules } from 'element-plus'
  import ArtForm from '@/components/core/forms/art-form/index.vue'
  import type { FormItem } from '@/components/core/forms/art-form/index.vue'
  import type { DialogType } from '@/types'

  const { t } = useI18n()

  interface Props {
    modelValue: boolean
    dialogType: DialogType
    userData?: Partial<Api.SystemManage.UserListItem>
  }

  interface Emits {
    (e: 'update:modelValue', value: boolean): void
    (e: 'success'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    modelValue: false,
    dialogType: 'add',
    userData: undefined
  })
  const emit = defineEmits<Emits>()

  // 角色列表数据
  const roleList = ref<Api.SystemManage.RoleListItem[]>([])
  const submitLoading = ref(false)

  // 对话框显示控制
  const dialogVisible = computed({
    get: () => props.modelValue,
    set: (value) => emit('update:modelValue', value)
  })

  /**
   * 是否为超级管理员
   */
  const isSuperAdmin = computed(() => {
    return props.dialogType === 'edit' && props.userData?.username === 'superAdmin'
  })

  // 表单实例
  const formRef = ref()

  /**
   * 表单配置项
   */
  const formItems = computed<FormItem[]>(() => [
    {
      key: 'username',
      label: t('system.user.form.username'),
      type: 'input',
      span: 12,
      props: {
        placeholder: t('system.user.form.usernamePlaceholder'),
        disabled: props.dialogType === 'edit'
      }
    },
    {
      key: 'nickname',
      label: t('system.user.form.nickname'),
      type: 'input',
      span: 12,
      props: {
        placeholder: t('system.user.form.nicknamePlaceholder')
      }
    },
    {
      key: 'phone',
      label: t('system.user.form.phone'),
      type: 'input',
      span: 12,
      props: {
        placeholder: t('system.user.form.phonePlaceholder'),
        maxlength: 11
      }
    },
    {
      key: 'email',
      label: t('system.user.form.email'),
      type: 'input',
      span: 12,
      props: {
        placeholder: t('system.user.form.emailPlaceholder')
      }
    },
    {
      key: 'roleIds',
      label: t('system.user.form.role'),
      type: 'select',
      span: 24,
      props: {
        multiple: true,
        placeholder: t('system.user.form.rolePlaceholder'),
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
  const rules = computed<FormRules>(() => ({
    username: [
      { required: true, message: t('system.user.form.usernameRequired'), trigger: 'blur' },
      {
        pattern: /^[a-zA-Z0-9_]{4,20}$/,
        message: t('system.user.form.usernamePattern'),
        trigger: 'blur'
      }
    ],
    nickname: [
      { required: true, message: t('system.user.form.nicknameRequired'), trigger: 'blur' }
    ],
    email: [{ type: 'email', message: t('system.user.form.emailInvalid'), trigger: 'blur' }],
    phone: [
      { pattern: /^1[3-9]\d{9}$/, message: t('system.user.form.phoneInvalid'), trigger: 'blur' }
    ]
  }))

  /**
   * 加载角色列表
   */
  const loadRoles = async () => {
    try {
      const res = await fetchGetRoleList({ pageNum: 1, pageSize: 1000, status: 1 })
      roleList.value = res.list || []
    } catch (error) {
      console.error('加载角色列表失败:', error)
    }
  }

  /**
   * 初始化表单数据
   */
  const initFormData = () => {
    if (props.dialogType === 'edit' && props.userData) {
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
    () => [props.modelValue, props.dialogType, props.userData],
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
        ElMessage.warning(t('system.user.superAdminStatusForbidden'))
        return
      }

      submitLoading.value = true

      // 准备提交数据
      const submitData: Api.SystemManage.UserSaveParams = {
        ...formData
      }

      // 调用API
      if (props.dialogType === 'add') {
        await fetchAddUser(submitData)
      } else {
        await fetchUpdateUser(formData.id!, submitData)
      }

      dialogVisible.value = false
      emit('success')
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
