<template>
  <ElDialog
    :title="dialogTitle"
    :model-value="visible"
    @update:model-value="handleCancel"
    width="700px"
    align-center
    class="menu-dialog"
    @closed="handleClosed"
  >
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="100px">
      <ElRow :gutter="20">
        <ElCol :span="12">
          <ElFormItem label="上级菜单" prop="parentId">
            <ElTreeSelect
              v-model="form.parentId"
              :data="menuTreeOptions"
              :props="{ label: 'menuName' }"
              value-key="id"
              check-strictly
              :render-after-expand="false"
              placeholder="请选择上级菜单（默认为顶级菜单）"
              clearable
              filterable
              style="width: 100%"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="当前类型" prop="menuType">
            <ElRadioGroup v-model="form.menuType">
              <ElRadioButton value="M" :disabled="!isMenuTypeAvailable('M')">目录</ElRadioButton>
              <ElRadioButton value="C" :disabled="!isMenuTypeAvailable('C')">菜单</ElRadioButton>
              <ElRadioButton value="F" :disabled="!isMenuTypeAvailable('F')">按钮</ElRadioButton>
            </ElRadioGroup>
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElRow :gutter="20">
        <ElCol :span="12">
          <ElFormItem :label="menuNameLabel" prop="menuName">
            <ElInput v-model="form.menuName" :placeholder="`请输入${menuNameLabel}`" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="显示排序" prop="sort">
            <ElInputNumber
              v-model="form.sort"
              :min="0"
              :max="9999"
              controls-position="right"
              style="width: 100%"
            />
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElRow :gutter="20" v-if="form.menuType !== 'F'">
        <ElCol :span="12">
          <ElFormItem label="路由地址" prop="path">
            <ElInput v-model="form.path" placeholder="请输入路由地址" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="组件路径" prop="component">
            <ElInput v-model="form.component" placeholder="如：system/user/index" />
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElRow :gutter="20">
        <ElCol :span="12">
          <ElFormItem label="权限标识" prop="permission">
            <ElInput v-model="form.permission" placeholder="如：system:user:list" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12" v-if="form.menuType !== 'F'">
          <ElFormItem label="菜单图标" prop="icon">
            <ElInput v-model="form.icon" placeholder="如：ri:calendar-line">
              <template #prefix>
                <i :class="form.icon" v-if="form.icon"></i>
              </template>
              <template #suffix>
                <ElButton text type="primary" class="icon-link-btn" @click="handleOpenRemixIcon">
                  去官网
                </ElButton>
              </template>
            </ElInput>
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElRow :gutter="20">
        <ElCol :span="12">
          <ElFormItem label="显示状态">
            <ArtSwitch
              v-model="form.visible"
              :active-value="1"
              :inactive-value="0"
              active-text="显示"
              inactive-text="隐藏"
              inline-prompt
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12" v-if="form.menuType !== 'F'">
          <ElFormItem label="页面缓存">
            <ArtSwitch
              v-model="form.keepAlive"
              :active-value="1"
              :inactive-value="0"
              active-text="开启"
              inactive-text="关闭"
              inline-prompt
            />
          </ElFormItem>
        </ElCol>
      </ElRow>
    </ElForm>

    <template #footer>
      <ElSpace>
        <ElButton @click="handleCancel">取消</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ submitLoading ? '提交中...' : '确定' }}
        </ElButton>
      </ElSpace>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { fetchGetMenuList, fetchAddMenu, fetchUpdateMenu } from '@/api/system-manage'
  import type { FormInstance, FormRules } from 'element-plus'
  import ArtSwitch from '@/components/core/forms/art-switch/index.vue'

  type MenuListItem = Api.SystemManage.MenuListItem

  interface Props {
    visible: boolean
    editData?: MenuListItem | null
    type?: 'add' | 'edit'
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'submit'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    visible: false,
    type: 'add',
    editData: null
  })

  const emit = defineEmits<Emits>()

  const formRef = ref<FormInstance>()
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

  const rules = reactive<FormRules>({
    menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
    menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
    sort: [{ required: true, message: '请输入排序值', trigger: 'blur' }],
    path: [
      {
        validator: (rule, value, callback) => {
          if (form.menuType !== 'F' && !value) {
            callback(new Error('请输入路由地址'))
          } else {
            callback()
          }
        },
        trigger: 'blur'
      }
    ]
  })

  const dialogTitle = computed(() => {
    const typeText = props.type === 'add' ? '新增' : '编辑'
    const menuTypeText = form.menuType === 'M' ? '目录' : form.menuType === 'C' ? '菜单' : '按钮'
    return `${typeText}${menuTypeText}`
  })

  /**
   * 菜单名称标签（根据类型动态变化）
   */
  const menuNameLabel = computed(() => {
    if (form.menuType === 'M') return '目录名称'
    if (form.menuType === 'C') return '菜单名称'
    if (form.menuType === 'F') return '按钮名称'
    return '菜单名称'
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
    // 递归查找选中的上级菜单
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
   * - 顶级菜单下：只能选择"目录"
   * - 目录下：可以选择"目录"或"菜单"
   * - 菜单下：只能选择"按钮"
   * - 按钮下：不能再有子级
   */
  const availableMenuTypes = computed(() => {
    const parentType = selectedParentMenu.value?.menuType
    if (parentType === 'ROOT') {
      // 顶级菜单下只能是目录
      return [{ value: 'M', label: '目录' }]
    } else if (parentType === 'M') {
      // 目录下可以是目录或菜单
      return [
        { value: 'M', label: '目录' },
        { value: 'C', label: '菜单' }
      ]
    } else if (parentType === 'C') {
      // 菜单下只能是按钮
      return [{ value: 'F', label: '按钮' }]
    }
    // 按钮下不能再有子级，默认返回所有类型（理论上不会到这里）
    return [
      { value: 'M', label: '目录' },
      { value: 'C', label: '菜单' },
      { value: 'F', label: '按钮' }
    ]
  })

  /**
   * 加载菜单树（用于上级菜单选择）
   */
  const loadMenuTree = async () => {
    try {
      const list = await fetchGetMenuList()
      // 添加顶级菜单选项
      menuTreeOptions.value = [
        { id: 0, menuName: '顶级菜单', parentId: -1, children: list } as any,
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
    formRef.value?.clearValidate()
  }

  /**
   * 加载表单数据（编辑模式）
   */
  const loadFormData = (): void => {
    if (props.type === 'edit' && props.editData) {
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
    } else if (props.type === 'add' && props.editData?.parentId) {
      // 添加子菜单时，设置父级菜单ID
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

      if (props.type === 'add') {
        await fetchAddMenu(submitData)
      } else {
        await fetchUpdateMenu(form.id!, submitData)
      }

      emit('submit')
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
    emit('update:visible', false)
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
    () => props.visible,
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
      // 检查当前菜单类型是否在可用选项中
      const isCurrentTypeAvailable = availableMenuTypes.value.some(
        (item) => item.value === form.menuType
      )
      // 如果不可用，自动切换为第一个可用的类型
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
    font-size: 12px;
  }
</style>
