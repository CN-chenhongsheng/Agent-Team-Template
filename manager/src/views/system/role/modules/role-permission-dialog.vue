<template>
  <ElDialog
    v-model="visible"
    :title="`分配权限 - ${props.roleData?.roleName || ''}`"
    width="520px"
    align-center
    class="el-dialog-border"
    @close="handleClose"
  >
    <ElScrollbar height="60vh" v-loading="loading">
      <ElTree
        ref="treeRef"
        :data="menuTree"
        show-checkbox
        node-key="id"
        :default-expand-all="isExpandAll"
        :props="defaultProps"
        @check="handleTreeCheck"
      >
        <template #default="{ data }">
          <div
            style="display: flex; align-items: center"
            :style="
              data.status === 0 ? { opacity: 0.5, color: 'var(--el-text-color-disabled)' } : {}
            "
          >
            <i :class="getMenuIcon(data)" style="margin-right: 8px"></i>
            <span>{{ data.menuName }}</span>
            <ElTag
              v-if="data.menuType"
              :type="getMenuTypeTag(data.menuType)"
              size="small"
              style="margin-left: 8px"
            >
              {{ getMenuTypeText(data.menuType) }}
            </ElTag>
            <ElTag v-if="data.status === 0" type="info" size="small" style="margin-left: 8px">
              已停用
            </ElTag>
          </div>
        </template>
      </ElTree>
    </ElScrollbar>

    <template #footer>
      <ElSpace>
        <ElButton @click="toggleExpandAll">
          <template #icon>
            <ArtSvgIcon :icon="isExpandAll ? 'ri-skip-down-line' : 'ri-skip-up-line'" />
          </template>
          {{ isExpandAll ? '全部收起' : '全部展开' }}
        </ElButton>
        <ElButton @click="toggleSelectAll">
          <template #icon>
            <ArtSvgIcon
              :icon="isSelectAll ? 'ri-indeterminate-circle-line' : 'ri-checkbox-circle-line'"
            />
          </template>
          {{ isSelectAll ? '取消全选' : '全部选择' }}
        </ElButton>
        <ElButton @click="handleClose">取消</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="savePermission">
          {{ submitLoading ? '保存中...' : '保存' }}
        </ElButton>
      </ElSpace>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import {
    fetchGetRolePermissions,
    fetchAssignRolePermissions,
    fetchGetMenuTreeForPermission
  } from '@/api/system-manage'

  type RoleListItem = Api.SystemManage.RoleListItem
  type MenuListItem = Api.SystemManage.MenuListItem

  interface Props {
    modelValue: boolean
    roleData?: RoleListItem
  }

  interface Emits {
    (e: 'update:modelValue', value: boolean): void
    (e: 'success'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    modelValue: false,
    roleData: undefined
  })

  const emit = defineEmits<Emits>()

  const treeRef = ref()
  const loading = ref(false)
  const submitLoading = ref(false)
  const isExpandAll = ref(true)
  const isSelectAll = ref(false)
  const menuTree = ref<MenuListItem[]>([])
  const checkedMenuIds = ref<number[]>([])
  // 菜单状态映射：menuId -> status（用于禁用判断）
  const menuStatusMap = ref<Map<number, number>>(new Map())

  /**
   * 弹窗显示状态双向绑定
   */
  const visible = computed({
    get: () => props.modelValue,
    set: (value) => emit('update:modelValue', value)
  })

  /**
   * 树形组件配置
   */
  const defaultProps = {
    children: 'children',
    label: 'menuName',
    disabled: (data: any) => {
      // 如果菜单状态为关闭（0），则禁用
      return (data as MenuListItem).status === 0
    }
  }

  /**
   * 获取菜单图标
   */
  const getMenuIcon = (menu: MenuListItem) => {
    if (menu.icon) return menu.icon
    if (menu.menuType === 'M') return 'i-carbon-folder'
    if (menu.menuType === 'C') return 'i-carbon-document'
    if (menu.menuType === 'F') return 'i-carbon-checkbox'
    return 'i-carbon-circle-dash'
  }

  /**
   * 获取菜单类型标签
   */
  const getMenuTypeTag = (type: string) => {
    const typeMap: Record<string, any> = {
      M: 'primary',
      C: 'success',
      F: 'info'
    }
    return typeMap[type] || 'info'
  }

  /**
   * 获取菜单类型文本
   */
  const getMenuTypeText = (type: string) => {
    const typeMap: Record<string, string> = {
      M: '目录',
      C: '菜单',
      F: '按钮'
    }
    return typeMap[type] || '未知'
  }

  /**
   * 加载菜单树（用于权限分配，包含所有类型，不包含顶级菜单）
   */
  const loadMenuTree = async () => {
    try {
      loading.value = true
      const tree = await fetchGetMenuTreeForPermission()
      // 过滤掉 id=0 的"顶级菜单"节点（虽然后端已不返回，但为了安全还是过滤）
      menuTree.value = filterTopMenu(tree)
    } catch (error) {
      console.error('加载菜单树失败:', error)
    } finally {
      loading.value = false
    }
  }

  /**
   * 过滤掉顶级菜单节点（id=0）
   */
  const filterTopMenu = (menus: MenuListItem[]): MenuListItem[] => {
    return menus
      .filter((menu) => menu.id !== 0)
      .map((menu) => {
        if (menu.children && menu.children.length > 0) {
          return {
            ...menu,
            children: filterTopMenu(menu.children)
          }
        }
        return menu
      })
  }

  /**
   * 加载角色已分配的菜单
   */
  const loadRoleMenus = async () => {
    if (!props.roleData?.id) return

    try {
      loading.value = true
      const permissions = await fetchGetRolePermissions(props.roleData.id)

      // 构建菜单状态映射
      const statusMap = new Map<number, number>()
      permissions.forEach((item) => {
        statusMap.set(item.menuId, item.status)
      })
      menuStatusMap.value = statusMap

      // 同步菜单树中的状态信息
      syncMenuStatus(menuTree.value, statusMap)

      // 提取菜单ID列表
      const menuIds = permissions.map((item) => item.menuId)
      checkedMenuIds.value = menuIds

      // 设置选中的节点（只设置叶子节点，避免父节点被自动选中）
      nextTick(() => {
        const leafMenuIds = getLeafMenuIds(menuTree.value, menuIds)
        treeRef.value?.setCheckedKeys(leafMenuIds, false)
        // 重新计算全选状态
        updateSelectAllStatus()
      })
    } catch (error) {
      console.error('加载角色权限失败:', error)
    } finally {
      loading.value = false
    }
  }

  /**
   * 同步菜单树中的状态信息
   */
  const syncMenuStatus = (menus: MenuListItem[], statusMap: Map<number, number>): void => {
    menus.forEach((menu) => {
      if (statusMap.has(menu.id)) {
        menu.status = statusMap.get(menu.id) || 0
      }
      if (menu.children && menu.children.length > 0) {
        syncMenuStatus(menu.children, statusMap)
      }
    })
  }

  /**
   * 获取叶子节点ID（用于设置选中状态）
   */
  const getLeafMenuIds = (menus: MenuListItem[], targetIds: number[]): number[] => {
    const leafIds: number[] = []

    const traverse = (nodes: MenuListItem[]) => {
      nodes.forEach((node) => {
        if (targetIds.includes(node.id)) {
          // 如果没有子节点或子节点为空，则是叶子节点
          if (!node.children || node.children.length === 0) {
            leafIds.push(node.id)
          }
        }
        if (node.children && node.children.length > 0) {
          traverse(node.children)
        }
      })
    }

    traverse(menus)
    return leafIds
  }

  /**
   * 监听弹窗打开，加载数据
   */
  watch(
    () => props.modelValue,
    async (newVal) => {
      if (newVal) {
        await loadMenuTree()
        await loadRoleMenus()
      }
    }
  )

  /**
   * 关闭弹窗并清空选中状态
   */
  const handleClose = () => {
    visible.value = false
    treeRef.value?.setCheckedKeys([])
    checkedMenuIds.value = []
    menuStatusMap.value.clear()
  }

  /**
   * 保存权限配置
   */
  const savePermission = async () => {
    if (!props.roleData?.id) {
      ElMessage.warning('角色信息不完整')
      return
    }

    try {
      submitLoading.value = true

      // 获取选中的菜单ID（包括半选中的父节点）
      const checkedKeys = treeRef.value.getCheckedKeys() as number[]
      const halfCheckedKeys = treeRef.value.getHalfCheckedKeys() as number[]
      const allMenuIds = [...checkedKeys, ...halfCheckedKeys]

      await fetchAssignRolePermissions(props.roleData.id, allMenuIds)

      emit('success')
      handleClose()
    } catch (error) {
      console.error('保存权限失败:', error)
    } finally {
      submitLoading.value = false
    }
  }

  /**
   * 切换全部展开/收起状态
   */
  const toggleExpandAll = () => {
    const tree = treeRef.value
    if (!tree) return

    const nodes = tree.store.nodesMap
    Object.values(nodes).forEach((node: any) => {
      node.expanded = !isExpandAll.value
    })

    isExpandAll.value = !isExpandAll.value
  }

  /**
   * 切换全选/取消全选状态
   */
  const toggleSelectAll = () => {
    const tree = treeRef.value
    if (!tree) return

    if (!isSelectAll.value) {
      // 只选中启用状态的菜单
      const enabledKeys = getAllEnabledNodeKeys(menuTree.value)
      tree.setCheckedKeys(enabledKeys)
    } else {
      tree.setCheckedKeys([])
    }

    isSelectAll.value = !isSelectAll.value
  }

  /**
   * 递归获取所有启用状态的节点 key（排除禁用状态）
   */
  const getAllEnabledNodeKeys = (nodes: MenuListItem[]): number[] => {
    const keys: number[] = []
    const traverse = (nodeList: MenuListItem[]): void => {
      nodeList.forEach((node) => {
        // 只包含启用状态的菜单（status === 1）
        if (node.status === 1) {
          keys.push(node.id)
        }
        if (node.children?.length) traverse(node.children)
      })
    }
    traverse(nodes)
    return keys
  }

  /**
   * 更新全选状态
   */
  const updateSelectAllStatus = () => {
    const tree = treeRef.value
    if (!tree) return

    const checkedKeys = tree.getCheckedKeys() as number[]
    const enabledKeys = getAllEnabledNodeKeys(menuTree.value)

    // 只有当所有启用状态的菜单都被选中时，才显示为全选
    isSelectAll.value = enabledKeys.length > 0 && checkedKeys.length === enabledKeys.length
  }

  /**
   * 处理树节点选中状态变化
   */
  const handleTreeCheck = () => {
    updateSelectAllStatus()
  }
</script>
