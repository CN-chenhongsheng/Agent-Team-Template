<template>
  <ElDialog
    v-model="visible"
    :title="
      dialogType === 'add' ? $t('system.role.form.addTitle') : $t('system.role.form.editTitle')
    "
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
      <ElButton @click="handleClose">{{ $t('common.cancel') }}</ElButton>
      <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">
        {{ submitLoading ? $t('system.common.saving') : $t('common.confirm') }}
      </ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n'
  import { fetchAddRole, fetchUpdateRole } from '@/api/system-manage'
  import type { FormRules } from 'element-plus'
  import ArtForm from '@/components/core/forms/art-form/index.vue'
  import type { FormItem } from '@/components/core/forms/art-form/index.vue'
  import type { DialogType } from '@/types'

  const { t } = useI18n()

  type RoleListItem = Api.SystemManage.RoleListItem

  interface Props {
    modelValue: boolean
    dialogType: DialogType
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
      label: t('system.role.form.roleCode'),
      type: 'input',
      span: 24,
      props: {
        placeholder: t('system.role.form.roleCodePlaceholder'),
        disabled: dialogType.value === 'edit'
      }
    },
    {
      key: 'roleName',
      label: t('system.role.form.roleName'),
      type: 'input',
      span: 24,
      props: {
        placeholder: t('system.role.form.roleNamePlaceholder')
      }
    },
    {
      key: 'sort',
      label: t('system.role.form.sort'),
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
      label: t('system.role.form.remark'),
      type: 'input',
      span: 24,
      props: {
        type: 'textarea',
        rows: 3,
        placeholder: t('system.role.form.remarkPlaceholder'),
        maxlength: 200,
        showWordLimit: true
      }
    }
  ])

  /**
   * 表单验证规则
   */
  const rules = computed<FormRules>(() => ({
    roleName: [
      { required: true, message: t('system.role.form.roleNameRequired'), trigger: 'blur' },
      { min: 2, max: 20, message: t('system.role.form.roleNameLength'), trigger: 'blur' }
    ],
    roleCode: [
      { required: true, message: t('system.role.form.roleCodeRequired'), trigger: 'blur' },
      { pattern: /^[A-Z_]+$/, message: t('system.role.form.roleCodePattern'), trigger: 'blur' }
    ],
    sort: [{ required: true, message: t('system.role.form.sortRequired'), trigger: 'blur' }]
  }))

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
        ElMessage.warning(t('system.role.superAdminDisableForbidden'))
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
