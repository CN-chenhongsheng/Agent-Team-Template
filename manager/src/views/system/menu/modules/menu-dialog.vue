<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="700px"
    align-center
    class="menu-dialog"
    @closed="handleClosed"
  >
    <ArtForm
      ref="formRef"
      v-model="form"
      :items="formItems"
      :span="12"
      :gutter="20"
      label-width="100px"
      :rules="rules"
      :show-submit="false"
      :show-reset="false"
    >
      <template #menuType>
        <ElRadioGroup v-model="form.menuType">
          <ElRadioButton value="M" :disabled="!isMenuTypeAvailable('M')">
            {{ $t('system.menu.typeDirectory') }}
          </ElRadioButton>
          <ElRadioButton value="C" :disabled="!isMenuTypeAvailable('C')">
            {{ $t('system.menu.typeMenu') }}
          </ElRadioButton>
          <ElRadioButton value="F" :disabled="!isMenuTypeAvailable('F')">
            {{ $t('system.menu.typeButton') }}
          </ElRadioButton>
        </ElRadioGroup>
      </template>

      <template #icon>
        <ElInput v-model="form.icon" :placeholder="$t('system.menu.dialog.iconPlaceholder')">
          <template #prefix>
            <i :class="form.icon" v-if="form.icon"></i>
          </template>
          <template #suffix>
            <ElButton text type="primary" class="icon-link-btn" @click="handleOpenRemixIcon">
              {{ $t('system.menu.dialog.goOfficial') }}
            </ElButton>
          </template>
        </ElInput>
      </template>
    </ArtForm>

    <template #footer>
      <ElSpace>
        <ElButton @click="handleCancel">{{ $t('common.cancel') }}</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ submitLoading ? $t('system.common.saving') : $t('common.confirm') }}
        </ElButton>
      </ElSpace>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n'
  import { fetchGetMenuList, fetchAddMenu, fetchUpdateMenu } from '@/api/system-manage'
  import type { FormRules } from 'element-plus'
  import ArtForm from '@/components/core/forms/art-form/index.vue'
  import type { FormItem } from '@/components/core/forms/art-form/index.vue'
  import type { DialogType } from '@/types'

  const { t } = useI18n()

  type MenuListItem = Api.SystemManage.MenuListItem

  interface Props {
    modelValue: boolean
    editData?: MenuListItem | null
    dialogType?: DialogType
  }

  interface Emits {
    (e: 'update:modelValue', value: boolean): void
    (e: 'success'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    modelValue: false,
    dialogType: 'add',
    editData: null
  })

  const emit = defineEmits<Emits>()

  const dialogVisible = computed({
    get: () => props.modelValue,
    set: (value) => emit('update:modelValue', value)
  })

  const formRef = ref()
  const submitLoading = ref(false)
  const menuTreeOptions = ref<MenuListItem[]>([])

  const form = reactive<Api.SystemManage.MenuSaveParams>({
    parentId: 0,
    menuName: '',
    menuType: 'M',
    path: '',
    component: '',
    permission: '',
    icon: '',
    sort: 0,
    visible: 1,
    status: 1,
    keepAlive: 1
  })

  const rules = computed<FormRules>(() => ({
    menuName: [
      { required: true, message: t('system.menu.dialog.menuNameRequired'), trigger: 'blur' }
    ],
    menuType: [
      { required: true, message: t('system.menu.dialog.menuTypeRequired'), trigger: 'change' }
    ],
    sort: [{ required: true, message: t('system.menu.dialog.sortRequired'), trigger: 'blur' }],
    path: [
      {
        validator: (rule, value, callback) => {
          if (form.menuType !== 'F' && !value) {
            callback(new Error(t('system.menu.dialog.pathRequired')))
          } else {
            callback()
          }
        },
        trigger: 'blur'
      }
    ]
  }))

  const dialogTitle = computed(() => {
    const typeText =
      form.menuType === 'M'
        ? t('system.menu.typeDirectory')
        : form.menuType === 'C'
          ? t('system.menu.typeMenu')
          : t('system.menu.typeButton')
    return t(
      props.dialogType === 'add' ? 'system.menu.dialog.addTitle' : 'system.menu.dialog.editTitle',
      {
        type: typeText
      }
    )
  })

  /**
   * 菜单名称标签（根据类型动态变化）
   */
  const menuNameLabel = computed(() => {
    if (form.menuType === 'M') return t('system.menu.dialog.nameDirectory')
    if (form.menuType === 'C') return t('system.menu.dialog.nameMenu')
    if (form.menuType === 'F') return t('system.menu.dialog.nameButton')
    return t('system.menu.dialog.nameMenu')
  })

  /**
   * 表单项配置
   */
  const formItems = computed<FormItem[]>(() => {
    const isButton = form.menuType === 'F'
    return [
      {
        key: 'parentId',
        label: t('system.menu.dialog.parent'),
        type: 'treeselect',
        span: 12,
        props: {
          data: menuTreeOptions.value,
          props: { label: 'menuName' },
          valueKey: 'id',
          checkStrictly: true,
          renderAfterExpand: false,
          placeholder: t('system.menu.dialog.parentPlaceholder'),
          clearable: true,
          filterable: true,
          style: 'width: 100%'
        }
      },
      {
        key: 'menuType',
        label: t('system.menu.dialog.currentType'),
        type: 'radiogroup',
        span: 12
      },
      {
        key: 'menuName',
        label: menuNameLabel.value,
        type: 'input',
        span: 12,
        props: {
          placeholder: t('system.menu.dialog.namePlaceholder', { label: menuNameLabel.value })
        }
      },
      {
        key: 'sort',
        label: t('system.menu.dialog.sort'),
        type: 'number',
        span: 12,
        props: { min: 0, max: 9999, controlsPosition: 'right', style: 'width: 100%' }
      },
      {
        key: 'path',
        label: t('system.menu.dialog.path'),
        type: 'input',
        span: 12,
        hidden: isButton,
        props: { placeholder: t('system.menu.dialog.pathPlaceholder') }
      },
      {
        key: 'component',
        label: t('system.menu.dialog.component'),
        type: 'input',
        span: 12,
        hidden: isButton,
        props: { placeholder: t('system.menu.dialog.componentPlaceholder') }
      },
      {
        key: 'permission',
        label: t('system.menu.dialog.permission'),
        type: 'input',
        span: 12,
        props: { placeholder: t('system.menu.dialog.permissionPlaceholder') }
      },
      {
        key: 'icon',
        label: t('system.menu.dialog.icon'),
        type: 'input',
        span: 12,
        hidden: isButton
      },
      {
        key: 'visible',
        label: t('system.menu.dialog.visible'),
        type: 'switch',
        span: 12,
        props: {
          activeValue: 1,
          inactiveValue: 0,
          activeText: t('system.menu.dialog.show'),
          inactiveText: t('system.menu.dialog.hide'),
          inlinePrompt: true
        }
      },
      {
        key: 'keepAlive',
        label: t('system.menu.dialog.keepAlive'),
        type: 'switch',
        span: 12,
        hidden: isButton,
        props: {
          activeValue: 1,
          inactiveValue: 0,
          activeText: t('system.menu.dialog.on'),
          inactiveText: t('system.menu.dialog.off'),
          inlinePrompt: true
        }
      }
    ]
  })

  /**
   * 判断菜单类型是否可用
   */
  const isMenuTypeAvailable = (type: string): boolean => {
    return availableMenuTypes.value.some((item) => item.value === type)
  }

  /**
   * 获取选中的上级菜单信息
   */
  const selectedParentMenu = computed(() => {
    if (form.parentId === 0) {
      return { menuType: 'ROOT' } // 顶级菜单
    }
    const findMenu = (list: MenuListItem[], id: number): MenuListItem | null => {
      for (const item of list) {
        if (item.id === id) return item
        if (item.children?.length) {
          const found = findMenu(item.children, id)
          if (found) return found
        }
      }
      return null
    }
    return findMenu(menuTreeOptions.value, form.parentId)
  })

  /**
   * 可选的菜单类型选项
   */
  const availableMenuTypes = computed(() => {
    const parentType = selectedParentMenu.value?.menuType
    if (parentType === 'ROOT') {
      return [{ value: 'M', label: t('system.menu.typeDirectory') }]
    } else if (parentType === 'M') {
      return [
        { value: 'M', label: t('system.menu.typeDirectory') },
        { value: 'C', label: t('system.menu.typeMenu') }
      ]
    } else if (parentType === 'C') {
      return [{ value: 'F', label: t('system.menu.typeButton') }]
    }
    return [
      { value: 'M', label: t('system.menu.typeDirectory') },
      { value: 'C', label: t('system.menu.typeMenu') },
      { value: 'F', label: t('system.menu.typeButton') }
    ]
  })

  /**
   * 加载菜单树（用于上级菜单选择）
   */
  const loadMenuTree = async () => {
    try {
      const list = await fetchGetMenuList()
      menuTreeOptions.value = [
        { id: 0, menuName: t('system.menu.dialog.topMenu'), parentId: -1, children: list } as any,
        ...list
      ]
    } catch (error) {
      console.error('加载菜单树失败:', error)
    }
  }

  /**
   * 重置表单数据
   */
  const resetForm = (): void => {
    Object.assign(form, {
      id: undefined,
      parentId: 0,
      menuName: '',
      menuType: 'M',
      path: '',
      component: '',
      permission: '',
      icon: '',
      sort: 0,
      visible: 1,
      status: 1,
      keepAlive: 1
    })
    formRef.value?.ref?.clearValidate()
  }

  /**
   * 加载表单数据（编辑模式）
   */
  const loadFormData = (): void => {
    if (props.dialogType === 'edit' && props.editData) {
      Object.assign(form, {
        id: props.editData.id,
        parentId: props.editData.parentId ?? 0,
        menuName: props.editData.menuName,
        menuType: props.editData.menuType,
        path: props.editData.path || '',
        component: props.editData.component || '',
        permission: props.editData.permission || '',
        icon: props.editData.icon || '',
        sort: props.editData.sort ?? 0,
        visible: props.editData.visible ?? 1,
        status: props.editData.status ?? 1,
        keepAlive: props.editData.keepAlive ?? 1
      })
    } else if (props.dialogType === 'add' && props.editData?.parentId) {
      form.parentId = props.editData.parentId
    }
  }

  /**
   * 打开 Remix Icon 官网
   */
  const handleOpenRemixIcon = (): void => {
    window.open('https://remixicon.com/', '_blank')
  }

  /**
   * 提交表单
   */
  const handleSubmit = async (): Promise<void> => {
    if (!formRef.value) return

    try {
      await formRef.value.validate()
      submitLoading.value = true

      const submitData: Api.SystemManage.MenuSaveParams = {
        ...form,
        parentId: form.parentId || 0
      }

      if (props.dialogType === 'add') {
        await fetchAddMenu(submitData)
      } else {
        await fetchUpdateMenu(form.id!, submitData)
      }

      emit('success')
      handleCancel()
    } catch (error) {
      console.error('提交失败:', error)
    } finally {
      submitLoading.value = false
    }
  }

  /**
   * 取消操作
   */
  const handleCancel = (): void => {
    dialogVisible.value = false
  }

  /**
   * 对话框关闭后的回调
   */
  const handleClosed = (): void => {
    resetForm()
  }

  /**
   * 监听对话框显示状态
   */
  watch(
    () => props.modelValue,
    async (newVal) => {
      if (newVal) {
        await loadMenuTree()
        nextTick(() => {
          resetForm()
          loadFormData()
        })
      }
    }
  )

  /**
   * 监听上级菜单变化，自动调整菜单类型
   */
  watch(
    () => form.parentId,
    () => {
      const isCurrentTypeAvailable = availableMenuTypes.value.some(
        (item) => item.value === form.menuType
      )
      if (!isCurrentTypeAvailable && availableMenuTypes.value.length > 0) {
        form.menuType = availableMenuTypes.value[0].value
      }
    }
  )
</script>

<style scoped lang="scss">
  .menu-dialog {
    :deep(.el-form-item) {
      margin-bottom: 18px;
    }
  }

  :deep(.el-form-item__content) {
    justify-content: flex-start;
  }

  :deep(.icon-link-btn) {
    height: 24px;
    padding: 0 6px;
  }
</style>
