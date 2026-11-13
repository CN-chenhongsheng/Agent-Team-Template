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
      :validate-on-rule-change="false"
    >
      <template #menuType>
        <div class="menu-type-field">
          <span v-if="isEdit" class="menu-type-field__locked">
            <ArtSvgIcon icon="ri:lock-2-line" />
            {{ $t('system.menu.dialog.typeLocked') }}
          </span>
          <div class="menu-type-field__cards">
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
    // 所有类型都必须显式选择上级；空值由自定义 validator 兜底（required 仅用于红星显示），菜单/按钮选顶级(0)同样拦截
    parentId: [
      {
        required: true,
        validator: (rule, value, callback) => {
          if (
            value === undefined ||
            value === null ||
            (form.menuType !== 'M' && value === 0)
          ) {
            callback(new Error(t('system.menu.dialog.parentRequired')))
          } else {
            callback()
          }
        },
        message: t('system.menu.dialog.parentRequired'),
        trigger: 'change'
      }
    ],
    sort: [{ required: true, message: t('system.menu.dialog.sortRequired'), trigger: 'blur' }],
    // 目录/菜单必填路由地址
    path:
      form.menuType !== 'F'
        ? [{ required: true, message: t('system.menu.dialog.pathRequired'), trigger: 'blur' }]
        : [],
    // 菜单必填组件路径
    component:
      form.menuType === 'C'
        ? [{ required: true, message: t('system.menu.dialog.componentRequired'), trigger: 'blur' }]
        : [],
    // 按钮必填权限标识
    permission:
      form.menuType === 'F'
        ? [{ required: true, message: t('system.menu.dialog.permissionRequired'), trigger: 'blur' }]
        : []
  }))

  const dialogTitle = computed(() => {
    const typeText =
      form.menuType === 'M'
        ? t('system.menu.typeDirectory')
        : form.menuType === 'C'
          ? t('system.menu.typeMenu')
          : form.menuType === 'F'
            ? t('system.menu.typeButton')
            : ''
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
      disabled: isEdit.value
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

  /**
   * 按当前表单类型过滤父级候选：按钮挂菜单下、菜单挂目录下、目录挂顶级或目录下
   */
  const isParentTypeAllowed = (node: MenuListItem): boolean => {
    if (node.menuType === 'F') return false
    if (node.id === 0) return form.menuType === 'M'
    const allowed: Record<string, string> = { M: 'M', C: 'M', F: 'C' }
    return allowed[form.menuType] === node.menuType
  }

  const buildParentTree = (list: MenuListItem[]): ParentTreeNode[] =>
    list.map((node) => {
      const typeLabel = node.menuType ? menuTypeLabel(node.menuType) : ''
      return {
        ...node,
        label: typeLabel ? `${node.menuName} · ${typeLabel}` : node.menuName,
        // 防环 + 类型不匹配的节点禁用但保留展示，便于用户查看整体层级
        disabled: lockedParentIds.value.has(node.id) || !isParentTypeAllowed(node),
        children: node.children ? buildParentTree(node.children) : undefined
      }
    })

  const parentTreeOptions = computed(() => buildParentTree(menuTreeOptions.value))

  const handleTypeSelect = (type: string) => {
    if (isEdit.value) return
    if (type !== form.menuType) {
      form.menuType = type
      // 当前父级与新类型不匹配时清空，引导重新选择上级
      const parentType = selectedParentMenu.value?.menuType
      const match: Record<string, string> = { M: 'M', C: 'M', F: 'C' }
      const parentMatch = form.parentId === 0 ? type === 'M' : parentType === match[type]
      if (!parentMatch) {
        form.parentId = undefined
      }
      // ElSelect 对 modelValue 变化会自动触发 change 校验（validateEvent），且其异步结果
      // 晚于微任务队列；用宏任务在它落定后清空校验状态，避免切类型时误报必填
      setTimeout(() => formRef.value?.ref?.clearValidate(), 0)
    }
  }

  /**
   * 表单项配置
   */
  const formItems = computed<FormItem[]>(() => {
    const isButton = form.menuType === 'F'
    return [
      {
        key: 'menuType',
        label: t('system.menu.dialog.selectType'),
        type: 'input',
        span: 24
      },
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
        key: 'icon',
        label: t('system.menu.dialog.icon'),
        type: 'input',
        span: 12,
        hidden: isButton
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
        // 目录不参与权限校验，该标识对目录无意义
        hidden: form.menuType === 'M',
        props: { placeholder: t('system.menu.dialog.permissionPlaceholder') }
      },
      {
        key: 'sort',
        label: t('system.menu.dialog.sort'),
        type: 'number',
        span: 12,
        props: { min: 0, max: 9999, controlsPosition: 'right', style: 'width: 100%' }
      },
      {
        key: 'visible',
        label: t('system.menu.dialog.visible'),
        type: 'switch',
        span: 12,
        // 按钮不参与页面显示，该开关对按钮无意义
        hidden: isButton
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
   * 获取选中的上级菜单信息
   */
  const selectedParentMenu = computed(() => {
    if (form.parentId === 0) {
      return { menuType: 'ROOT' } // 顶级菜单
    }
    const findMenu = (list: MenuListItem[], id: number | undefined): MenuListItem | null => {
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
      // 「新增子菜单」预填入口：按父级自动推荐类型
      const recommended = recommendedType.value
      if (recommended) form.menuType = recommended
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
</script>

<style scoped lang="scss">
  .menu-dialog {
    :deep(.el-form-item) {
      margin-bottom: 18px;
    }
  }

  .menu-type-field {
    display: flex;
    flex-direction: column;
    gap: 6px;
    width: 100%;

    &__cards {
      display: grid;
      grid-template-columns: repeat(3, minmax(0, 1fr));
      gap: 10px;
    }

    &__locked,
    &__note {
      display: inline-flex;
      gap: 4px;
      align-items: center;
      align-self: flex-end;
      font-size: 12px;
      color: var(--el-text-color-secondary);
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
