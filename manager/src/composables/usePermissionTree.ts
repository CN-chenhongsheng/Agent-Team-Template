/**
 * 权限分配树逻辑
 *
 * 统一角色/用户授权弹窗的菜单树加载、勾选、全选与保存流程。
 *
 * @module composables/usePermissionTree
 */

import { $t } from '@/locales'

type MenuListItem = Api.SystemManage.MenuListItem
type PermissionItem = { menuId: number; status: number }

export interface UsePermissionTreeOptions {
  /** 当前授权对象 ID，为空时不做任何操作 */
  getTargetId: () => number | undefined
  /** 加载可分配的菜单树 */
  fetchMenuTree: () => Promise<MenuListItem[]>
  /** 加载当前已分配的权限 */
  fetchCheckedPermissions: () => Promise<PermissionItem[]>
  /** 保存选中的菜单 ID（含半选父节点） */
  savePermissions: (menuIds: number[]) => Promise<unknown>
  /** 可选的前置校验，返回 false 表示当前对象不可分配权限 */
  beforeLoad?: () => Promise<boolean>
}

export function usePermissionTree(options: UsePermissionTreeOptions) {
  const treeRef = ref()
  const loading = ref(false)
  const submitLoading = ref(false)
  const isExpandAll = ref(true)
  const isSelectAll = ref(false)
  const menuTree = ref<MenuListItem[]>([])
  const canAssign = ref(!options.beforeLoad)
  const menuStatusMap = ref<Map<number, number>>(new Map())

  const defaultProps = {
    children: 'children',
    label: 'menuName',
    disabled: (data: any) => (data as MenuListItem).status === 0
  }

  const getMenuIcon = (menu: MenuListItem) => {
    if (menu.icon) return menu.icon
    if (menu.menuType === 'M') return 'i-carbon-folder'
    if (menu.menuType === 'C') return 'i-carbon-document'
    if (menu.menuType === 'F') return 'i-carbon-checkbox'
    return 'i-carbon-circle-dash'
  }

  const getMenuTypeTag = (type: string): 'primary' | 'success' | 'info' => {
    const typeMap: Record<string, 'primary' | 'success' | 'info'> = {
      M: 'primary',
      C: 'success',
      F: 'info'
    }
    return typeMap[type] || 'info'
  }

  const getMenuTypeText = (type: string) => {
    const typeMap: Record<string, string> = {
      M: $t('system.menu.typeDirectory'),
      C: $t('system.menu.typeMenu'),
      F: $t('system.menu.typeButton')
    }
    return typeMap[type] || $t('system.menu.unknownType')
  }

  const syncMenuStatus = (menus: MenuListItem[], statusMap: Map<number, number>): void => {
    menus.forEach((menu) => {
      if (statusMap.has(menu.id)) {
        menu.status = statusMap.get(menu.id) || 0
      }
      if (menu.children?.length) {
        syncMenuStatus(menu.children, statusMap)
      }
    })
  }

  const getLeafMenuIds = (menus: MenuListItem[], targetIds: number[]): number[] => {
    const leafIds: number[] = []

    const traverse = (nodes: MenuListItem[]) => {
      nodes.forEach((node) => {
        if (targetIds.includes(node.id) && !node.children?.length) {
          leafIds.push(node.id)
        }
        if (node.children?.length) {
          traverse(node.children)
        }
      })
    }

    traverse(menus)
    return leafIds
  }

  const getAllEnabledNodeKeys = (nodes: MenuListItem[]): number[] => {
    const keys: number[] = []
    const traverse = (nodeList: MenuListItem[]): void => {
      nodeList.forEach((node) => {
        if (node.status === 1) {
          keys.push(node.id)
        }
        if (node.children?.length) traverse(node.children)
      })
    }
    traverse(nodes)
    return keys
  }

  const updateSelectAllStatus = () => {
    const tree = treeRef.value
    if (!tree) return

    const checkedKeys = tree.getCheckedKeys() as number[]
    const enabledKeys = getAllEnabledNodeKeys(menuTree.value)
    isSelectAll.value = enabledKeys.length > 0 && checkedKeys.length === enabledKeys.length
  }

  const handleTreeCheck = () => {
    updateSelectAllStatus()
  }

  const toggleExpandAll = () => {
    const tree = treeRef.value
    if (!tree) return

    const nodes = tree.store.nodesMap
    Object.values(nodes).forEach((node: any) => {
      node.expanded = !isExpandAll.value
    })
    isExpandAll.value = !isExpandAll.value
  }

  const toggleSelectAll = () => {
    const tree = treeRef.value
    if (!tree) return

    if (!isSelectAll.value) {
      tree.setCheckedKeys(getAllEnabledNodeKeys(menuTree.value))
    } else {
      tree.setCheckedKeys([])
    }
    isSelectAll.value = !isSelectAll.value
  }

  const loadData = async () => {
    const targetId = options.getTargetId()
    if (!targetId) return

    try {
      loading.value = true

      canAssign.value = options.beforeLoad ? await options.beforeLoad() : true
      if (!canAssign.value) {
        menuTree.value = []
        return
      }

      menuTree.value = await options.fetchMenuTree()
      const permissions = await options.fetchCheckedPermissions()

      const statusMap = new Map<number, number>()
      permissions.forEach((item) => statusMap.set(item.menuId, item.status))
      menuStatusMap.value = statusMap
      syncMenuStatus(menuTree.value, statusMap)

      const menuIds = permissions.map((item) => item.menuId)
      nextTick(() => {
        const leafMenuIds = getLeafMenuIds(menuTree.value, menuIds)
        treeRef.value?.setCheckedKeys(leafMenuIds, false)
        updateSelectAllStatus()
      })
    } catch (error) {
      console.error('加载权限数据失败:', error)
    } finally {
      loading.value = false
    }
  }

  const reset = () => {
    treeRef.value?.setCheckedKeys([])
    menuStatusMap.value.clear()
    canAssign.value = true
  }

  const savePermission = async (): Promise<boolean> => {
    if (!options.getTargetId()) {
      ElMessage.warning($t('system.common.incompleteData'))
      return false
    }

    if (!canAssign.value) {
      ElMessage.warning($t('system.common.cannotAssign'))
      return false
    }

    try {
      submitLoading.value = true
      const checkedKeys = (treeRef.value?.getCheckedKeys() as number[]) ?? []
      const halfCheckedKeys = (treeRef.value?.getHalfCheckedKeys() as number[]) ?? []
      await options.savePermissions([...checkedKeys, ...halfCheckedKeys])
      return true
    } catch (error) {
      console.error('保存权限失败:', error)
      return false
    } finally {
      submitLoading.value = false
    }
  }

  return {
    treeRef,
    loading,
    submitLoading,
    isExpandAll,
    isSelectAll,
    menuTree,
    canAssign,
    defaultProps,
    getMenuIcon,
    getMenuTypeTag,
    getMenuTypeText,
    loadData,
    reset,
    savePermission,
    toggleExpandAll,
    toggleSelectAll,
    handleTreeCheck
  }
}
