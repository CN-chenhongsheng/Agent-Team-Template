<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="700px"
    align-center
    class="menu-dialog"
    @closed="handleClosed"
  >
    <div class="menu-type-picker">
      <div class="menu-type-picker__header">
        <span class="menu-type-picker__title">{{ $t('system.menu.dialog.selectType') }}</span>
        <span v-if="isEdit" class="menu-type-picker__locked">
          <ArtSvgIcon icon="ri:lock-2-line" />
          {{ $t('system.menu.dialog.typeLocked') }}
        </span>
        <ElTooltip v-else-if="typeHintShort" :content="typeHint" placement="top">
          <span class="menu-type-picker__note">
            <ArtSvgIcon icon="ri:information-line" />
            {{ typeHintShort }}
          </span>
        </ElTooltip>
      </div>
      <div class="menu-type-picker__cards">
        <button
          v-for="opt in typeOptions"
          :key="opt.value"
          type="button"
          class="menu-type-card"
          :class="{ 'is-active': form.menuType === opt.value, 'is-disabled': opt.disabled }"
          :disabled="opt.disabled"
          @click="handleTypeSelect(opt.value)"
        >
          <span class="menu-type-card__head">
            <ArtSvgIcon :icon="opt.icon" class="menu-type-card__icon" />
            <span class="menu-type-card__title">{{ opt.label }}</span>
          </span>
          <span class="menu-type-card__desc">{{ opt.desc }}</span>
        </button>
      </div>
    </div>

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

      <template #visible>
        <div class="switch-field">
          <ElSwitch
            v-model="form.visible"
            :active-value="1"
            :inactive-value="0"
            inline-prompt
            active-text="✓"
            inactive-text="✗"
          />
          <span class="switch-field__text">
            {{ form.visible === 1 ? $t('system.menu.dialog.show') : $t('system.menu.dialog.hide') }}
          </span>
        </div>
      </template>

      <template #keepAlive>
        <div class="switch-field">
          <ElSwitch
            v-model="form.keepAlive"
            :active-value="1"
            :inactive-value="0"
            inline-prompt
            active-text="✓"
            inactive-text="✗"
          />
          <span class="switch-field__text">
            {{ form.keepAlive === 1 ? $t('system.menu.dialog.on') : $t('system.menu.dialog.off') }}
          </span>
        </div>
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
  const isEdit = computed(() => props.dialogType === 'edit')
  const typeTouched = ref(false)

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
    parentId: [
      {
        validator: (rule, value, callback) => {
          if (form.menuType !== 'M' && (value === 0 || value === undefined || value === null)) {
            callback(new Error(t('system.menu.dialog.parentRequired')))
          } else {
            callback()
          }
        },
        trigger: 'change'
      }
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
   * 类型卡片元数据
   */
  const MENU_TYPE_META = {
    M: { icon: 'ri:folders-line', desc: 'typeDirectoryDesc' },
    C: { icon: 'ri:file-list-3-line', desc: 'typeMenuDesc' },
    F: { icon: 'ri:checkbox-circle-line', desc: 'typeButtonDesc' }
  } as const

  const menuTypeLabel = (type: string): string => {
    if (type === 'M') return t('system.menu.typeDirectory')
    if (type === 'C') return t('system.menu.typeMenu')
    if (type === 'F') return t('system.menu.typeButton')
    return ''
  }

  const typeOptions = computed(() =>
    (Object.keys(MENU_TYPE_META) as Array<keyof typeof MENU_TYPE_META>).map((value) => ({
      value,
      label: menuTypeLabel(value),
      desc: t(`system.menu.dialog.${MENU_TYPE_META[value].desc}`),
      icon: MENU_TYPE_META[value].icon,
      disabled: isEdit.value || !isMenuTypeAvailable(value)
    }))
  )

  /**
   * 根据上级菜单推荐类型
   */
  const recommendedType = computed<string | null>(() => {
    const parentType = selectedParentMenu.value?.menuType
    if (parentType === 'ROOT') return 'M'
    if (parentType === 'M') return 'C'
    if (parentType === 'C') return 'F'
    return null
  })

  const typeHint = computed(() => {
    if (isEdit.value) return ''
    const parentType = selectedParentMenu.value?.menuType
    if (parentType === 'ROOT') return t('system.menu.dialog.parentHintRoot')
    if (parentType === 'M') return t('system.menu.dialog.parentHintDirectory')
    if (parentType === 'C') return t('system.menu.dialog.parentHintMenu')
    return t('system.menu.dialog.parentHintUnknown')
  })

  const typeHintShort = computed(() => {
    if (isEdit.value) return ''
    const parentType = selectedParentMenu.value?.menuType
    if (parentType === 'ROOT') return t('system.menu.dialog.parentShortRoot')
    if (parentType === 'M') return t('system.menu.dialog.parentShortDirectory')
    if (parentType === 'C') return t('system.menu.dialog.parentShortMenu')
    return t('system.menu.dialog.parentShortUnknown')
  })

  const findMenuById = (list: MenuListItem[], id: number): MenuListItem | null => {
    for (const item of list) {
      if (item.id === id) return item
      const found = item.children?.length ? findMenuById(item.children, id) : null
      if (found) return found
    }
    return null
  }

  /**
   * 编辑时禁止把自己或自己的子孙选为上级
   */
  const lockedParentIds = computed(() => {
    const ids = new Set<number>()
    if (!isEdit.value || !form.id) return ids
    const node = findMenuById(menuTreeOptions.value, form.id)
    if (!node) return ids
    const walk = (item: MenuListItem) => {
      ids.add(item.id)
      item.children?.forEach(walk)
    }
    walk(node)
    return ids
  })

  /**
   * 父级树节点：带类型标签；编辑时禁用自身及子孙，避免形成环
   */
  interface ParentTreeNode extends MenuListItem {
    label: string
    disabled: boolean
    children?: ParentTreeNode[]
  }

  const buildParentTree = (list: MenuListItem[]): ParentTreeNode[] =>
    list.map((node) => {
      const typeLabel = node.menuType ? menuTypeLabel(node.menuType) : ''
      return {
        ...node,
        label: typeLabel ? `${node.menuName} · ${typeLabel}` : node.menuName,
        disabled: lockedParentIds.value.has(node.id),
        children: node.children ? buildParentTree(node.children) : undefined
      }
    })

  const parentTreeOptions = computed(() => buildParentTree(menuTreeOptions.value))

  const handleTypeSelect = (type: string) => {
    if (isEdit.value) return
    typeTouched.value = true
    if (type !== form.menuType) {
      form.menuType = type
    }
  }

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
        span: 24,
        props: {
          data: parentTreeOptions.value,
          props: { label: 'label', disabled: 'disabled' },
          valueKey: 'id',
          checkStrictly: true,
          renderAfterExpand: false,
          defaultExpandedKeys: [0],
          placeholder: t('system.menu.dialog.parentPlaceholder'),
          clearable: true,
          filterable: true,
          style: 'width: 100%'
        }
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
        span: 12
      },
      {
        key: 'keepAlive',
        label: t('system.menu.dialog.keepAlive'),
        type: 'switch',
        span: 12,
        hidden: isButton
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
        { id: 0, menuName: t('system.menu.dialog.topMenu'), parentId: -1, children: list } as any
      ]
    } catch (error) {
      console.error('加载菜单树失败:', error)
    }
  }

  /**
   * 重置表单数据
   */
  const resetForm = (): void => {
    typeTouched.value = false
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
      typeTouched.value = true
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
   * 监听上级菜单变化，自动调整/推荐菜单类型
   */
  watch(
    () => form.parentId,
    () => {
      if (isEdit.value) return
      const available = availableMenuTypes.value
      const isValid = available.some((item) => item.value === form.menuType)
      if (!isValid) {
        const next = recommendedType.value || available[0]?.value
        if (next) form.menuType = next
        return
      }
      if (!typeTouched.value && recommendedType.value) {
        form.menuType = recommendedType.value
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

  .menu-type-picker {
    margin-bottom: 18px;

    &__header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 10px;
    }

    &__title {
      font-size: 13px;
      font-weight: 500;
      color: var(--el-text-color-secondary);
    }

    &__locked,
    &__note {
      display: inline-flex;
      gap: 4px;
      align-items: center;
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }

    &__cards {
      display: grid;
      grid-template-columns: repeat(3, minmax(0, 1fr));
      gap: 10px;
    }
  }

  .menu-type-card {
    display: flex;
    flex-direction: column;
    gap: 4px;
    padding: 10px 12px;
    text-align: left;
    background-color: transparent;
    border: 1px solid var(--el-border-color);
    border-radius: 6px;
    transition: border-color 0.2s ease;

    &:hover:not(:disabled) {
      border-color: var(--el-color-primary-light-5);
    }

    &.is-active {
      border-color: var(--el-color-primary);

      .menu-type-card__icon,
      .menu-type-card__title {
        color: var(--el-color-primary);
      }
    }

    &.is-disabled:not(.is-active) {
      opacity: 0.5;
    }

    &__head {
      display: flex;
      gap: 6px;
      align-items: center;
      min-width: 0;
    }

    &__icon {
      flex-shrink: 0;
      font-size: 14px;
      color: var(--el-text-color-secondary);
    }

    &__title {
      overflow: hidden;
      font-size: 14px;
      font-weight: 600;
      color: var(--el-text-color-primary);
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    &__desc {
      overflow: hidden;
      font-size: 12px;
      line-height: 1.4;
      color: var(--el-text-color-secondary);
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .switch-field {
    display: flex;
    gap: 8px;
    align-items: center;
    height: 36px;

    &__text {
      min-width: 28px;
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }
  }

  :deep(.el-form-item__content) {
    justify-content: flex-start;
  }

  :deep(.icon-link-btn) {
    height: 24px;
    padding: 0 6px;
  }

  :global(.menu-dialog .el-button.icon-link-btn) {
    color: var(--el-text-color-secondary);
  }

  :global(.menu-dialog .el-button.icon-link-btn:hover) {
    color: var(--el-color-primary) !important;
    background-color: transparent !important;
  }
</style>
