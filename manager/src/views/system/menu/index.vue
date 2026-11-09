<!-- 菜单管理页面 -->
<template>
  <div class="menu-page art-full-height">
    <!-- 搜索栏 -->
    <MenuSearch
      v-show="showSearchBar"
      v-model="formFilters"
      @reset="handleReset"
      @search="handleSearch"
    />

    <ElCard
      class="art-table-card"
      shadow="never"
      :style="{ 'margin-top': showSearchBar ? '12px' : '0' }"
    >
      <!-- 表格头部 -->
      <ArtTableHeader
        :showZebra="false"
        :loading="loading"
        v-model:columns="columnChecks"
        v-model:showSearchBar="showSearchBar"
        @refresh="handleRefresh"
      >
        <template #left>
          <ElSpace wrap>
            <ElButton @click="handleAddMenu" v-ripple v-permission="PERMISSIONS.system.menu.add">
              {{ $t('system.menu.add') }}
            </ElButton>
            <ElButton @click="toggleExpand" v-ripple>
              {{ isExpanded ? $t('system.menu.collapseAll') : $t('system.menu.expandAll') }}
            </ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <ArtTable
        :contextMenuItems="contextMenuItems"
        :contextMenuWidth="contextMenuWidth"
        :onRowContextmenu="handleRowContextmenu as any"
        :onContextMenuSelect="handleContextMenuSelect"
        ref="tableRef"
        row-key="id"
        :loading="loading"
        :columns="columns"
        :data="data"
        :stripe="false"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :default-expand-all="false"
      />

      <!-- 菜单弹窗 -->
      <MenuDialog
        v-model="dialogVisible"
        :dialog-type="dialogType"
        :edit-data="editData"
        @success="handleDialogSubmit"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n'
  import { useTable } from '@/hooks/core/useTable'
  import MenuDialog from './modules/menu-dialog.vue'
  import MenuSearch from './modules/menu-search.vue'
  import { fetchGetMenuList, fetchDeleteMenu, fetchUpdateMenuStatus } from '@/api/system-manage'
  import ArtSwitch from '@/components/core/forms/art-switch/index.vue'
  import type { ActionButtonConfig } from '@/types/component'
  import type { DialogType } from '@/types'
  import { PERMISSIONS } from '@/utils/constants'
  import { h, nextTick } from 'vue'

  defineOptions({ name: 'Menus' })

  const { t } = useI18n()

  type MenuListItem = Api.SystemManage.MenuListItem & { _statusLoading?: boolean }

  // 状态管理
  const isExpanded = ref(false)
  const tableRef = ref()
  const showSearchBar = ref(true)

  // 弹窗相关
  const dialogVisible = ref(false)
  const dialogType = ref<DialogType>('add')
  const editData = ref<MenuListItem | null>(null)

  // 搜索相关
  const formFilters = ref({
    menuName: '',
    status: undefined
  })

  /**
   * 获取菜单类型标签颜色
   */
  const getMenuTypeTag = (type: string): 'primary' | 'warning' | 'info' => {
    const typeMap: Record<string, 'primary' | 'warning' | 'info'> = {
      M: 'warning', // 目录
      C: 'primary', // 菜单
      F: 'info' // 按钮
    }
    return typeMap[type] || 'info'
  }

  /**
   * 获取菜单类型文本
   */
  const getMenuTypeText = (type: string): string => {
    const typeMap: Record<string, string> = {
      M: t('system.menu.typeDirectory'),
      C: t('system.menu.typeMenu'),
      F: t('system.menu.typeButton')
    }
    return typeMap[type] || t('system.menu.unknownType')
  }

  /**
   * 获取菜单图标
   */
  const getMenuIcon = (icon?: string) => {
    return icon || 'i-carbon-document'
  }

  /**
   * 获取菜单操作配置
   */
  const getMenuActions = (row: MenuListItem): ActionButtonConfig[] => {
    const buttons: ActionButtonConfig[] = []
    // 只有目录和菜单可以添加子菜单/按钮
    if (row.menuType === 'M' || row.menuType === 'C') {
      buttons.push({
        type: 'add',
        label: t('system.common.add'),
        onClick: () => handleAddSubMenu(row),
        auth: PERMISSIONS.system.menu.add
      })
    }
    buttons.push({
      type: 'edit',
      label: t('system.common.edit'),
      onClick: () => handleEditMenu(row),
      auth: PERMISSIONS.system.menu.edit
    })
    buttons.push({
      type: 'delete',
      label: t('system.common.delete'),
      onClick: () => handleDeleteMenu(row),
      auth: PERMISSIONS.system.menu.delete,
      danger: true
    })
    return buttons
  }

  // 使用 useTable 管理表格数据
  const {
    columns,
    columnChecks,
    data,
    loading,
    getData,
    resetSearchParams,
    refreshData,
    refreshCreate,
    refreshUpdate,
    refreshRemove,
    contextMenuItems,
    contextMenuWidth,
    handleRowContextmenu,
    handleContextMenuSelect
  } = useTable<typeof fetchGetMenuList>({
    core: {
      apiFn: fetchGetMenuList,
      apiParams: computed(() => {
        return {
          ...formFilters.value
        } as Api.SystemManage.MenuSearchParams
      }),
      columnsFactory: () => [
        {
          prop: 'menuName',
          label: t('system.menu.menuName'),
          minWidth: 200,
          formatter: (row: MenuListItem) => {
            return h('div', { class: 'flex items-center gap-2' }, [
              h('i', { class: `${getMenuIcon(row.icon)} text-lg` }),
              h('span', row.menuName)
            ])
          }
        },
        {
          prop: 'menuType',
          label: t('system.menu.type'),
          width: 80,
          formatter: (row: MenuListItem) => {
            return h(ElTag, { type: getMenuTypeTag(row.menuType), size: 'small' }, () =>
              getMenuTypeText(row.menuType)
            )
          }
        },
        {
          prop: 'path',
          label: t('system.menu.routePath'),
          minWidth: 150,
          formatter: (row: MenuListItem) => {
            return row.path || '-'
          }
        },
        {
          prop: 'component',
          label: t('system.menu.componentPath'),
          minWidth: 180,
          showOverflowTooltip: true,
          formatter: (row: MenuListItem) => {
            return row.component || '-'
          }
        },
        {
          prop: 'permission',
          label: t('system.menu.permission'),
          minWidth: 150,
          formatter: (row: MenuListItem) => {
            if (!row.permission) return '-'
            return t('system.menu.permissionCount', {
              count: row.permission.split(',').length
            })
          }
        },
        {
          prop: 'sort',
          label: t('system.common.sort'),
          width: 85,
          sortable: true
        },
        {
          prop: 'visible',
          label: t('system.menu.visible'),
          width: 100,
          formatter: (row: MenuListItem) => {
            return h(
              ElTag,
              {
                type: row.visible === 1 ? 'success' : 'info',
                size: 'small'
              },
              () =>
                row.visible === 1 ? t('system.menu.dialog.show') : t('system.menu.dialog.hide')
            )
          }
        },
        {
          prop: 'status',
          label: t('system.common.status'),
          width: 100,
          formatter: (row: MenuListItem) => {
            return h(ArtSwitch, {
              modelValue: row.status === 1,
              loading: row._statusLoading || false,
              inlinePrompt: true,
              onChange: (value: string | number | boolean) => {
                handleStatusChange(row, value === true || value === 1)
              }
            })
          }
        },
        {
          prop: 'createTime',
          label: t('system.common.createTime'),
          width: 180,
          sortable: true
        },
        {
          prop: 'action',
          label: t('system.common.actions'),
          width: 180,
          fixed: 'right' as const,
          formatter: getMenuActions
        }
      ],
      immediate: true
    },
    adaptive: {
      enabled: true
    },
    contextMenu: {
      enabled: true
    }
  })

  /**
   * 重置搜索条件
   */
  const handleReset = async (): Promise<void> => {
    formFilters.value = {
      menuName: '',
      status: undefined
    }
    await resetSearchParams()
  }

  /**
   * 执行搜索
   */
  const handleSearch = async (): Promise<void> => {
    await getData()
  }

  /**
   * 刷新菜单列表
   */
  const handleRefresh = (): void => {
    refreshData()
  }

  /**
   * 添加顶级菜单
   */
  const handleAddMenu = (): void => {
    dialogType.value = 'add'
    editData.value = null
    dialogVisible.value = true
  }

  /**
   * 添加子菜单
   */
  const handleAddSubMenu = (row: MenuListItem): void => {
    dialogType.value = 'add'
    editData.value = { parentId: row.id } as MenuListItem
    dialogVisible.value = true
  }

  /**
   * 编辑菜单
   */
  const handleEditMenu = (row: MenuListItem): void => {
    dialogType.value = 'edit'
    editData.value = { ...row }
    dialogVisible.value = true
  }

  /**
   * 提交表单数据
   */
  const handleDialogSubmit = async (): Promise<void> => {
    dialogVisible.value = false
    // 根据 dialogType 判断是新增还是编辑
    if (dialogType.value === 'add') {
      await refreshCreate()
    } else {
      await refreshUpdate()
    }
  }

  /**
   * 递归计算子菜单数量
   */
  const getChildrenCount = (menu: MenuListItem): number => {
    let count = 0
    if (menu.children && menu.children.length > 0) {
      count += menu.children.length
      menu.children.forEach((child) => {
        count += getChildrenCount(child)
      })
    }
    return count
  }

  /**
   * 删除菜单
   */
  const handleDeleteMenu = async (row: MenuListItem): Promise<void> => {
    // 检查是否有子菜单
    const hasChildren = row.children && row.children.length > 0
    const childrenCount = hasChildren ? getChildrenCount(row) : 0

    try {
      let message = t('system.menu.deleteConfirm', { name: row.menuName })
      if (hasChildren) {
        message += t('system.menu.deleteChildrenTip', { count: childrenCount })
      } else {
        message += t('system.menu.deleteNoChildrenTip')
      }

      await ElMessageBox.confirm(message, t('system.common.deleteConfirmTitle'), {
        confirmButtonText: t('system.common.confirmDelete'),
        cancelButtonText: t('common.cancel'),
        type: 'warning',
        dangerouslyUseHTMLString: true
      })

      await fetchDeleteMenu(row.id)
      await refreshRemove()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除菜单失败:', error)
      }
    }
  }

  /**
   * 切换展开/收起所有菜单
   */
  const toggleExpand = (): void => {
    isExpanded.value = !isExpanded.value
    nextTick(() => {
      if (tableRef.value?.elTableRef && data.value) {
        toggleAllExpansion(data.value, isExpanded.value)
      }
    })
  }

  /**
   * 递归展开/收起所有节点
   */
  const toggleAllExpansion = (treeData: MenuListItem[], expand: boolean): void => {
    treeData.forEach((item) => {
      if (tableRef.value?.elTableRef) {
        tableRef.value.elTableRef.toggleRowExpansion(item, expand)
      }
      if (item.children && item.children.length > 0) {
        toggleAllExpansion(item.children, expand)
      }
    })
  }

  /**
   * 更新菜单状态
   */
  const handleStatusChange = async (row: MenuListItem, value: boolean): Promise<void> => {
    // 检查是否有子节点
    const hasChildren = row.children && row.children.length > 0

    // 如果是关闭操作（从启用变为停用），需要提示用户级联影响
    if (!value && row.status === 1) {
      try {
        let message = t('system.menu.disableConfirm', { name: row.menuName })
        if (hasChildren) {
          message += t('system.menu.disableChildrenTip')
        } else {
          message += t('system.menu.disableNoChildrenTip')
        }
        await ElMessageBox.confirm(message, t('system.common.disableConfirmTitle'), {
          type: 'warning',
          confirmButtonText: t('system.common.confirmDisable'),
          cancelButtonText: t('common.cancel'),
          dangerouslyUseHTMLString: true
        })
      } catch {
        // 用户取消操作，不执行任何更改
        return
      }
    }

    const originalStatus = row.status
    try {
      row._statusLoading = true
      row.status = value ? 1 : 0
      await fetchUpdateMenuStatus(row.id, value ? 1 : 0)
      // 如果有子节点，需要刷新表格以同步子菜单状态
      // 如果是叶子节点，只更新当前行状态即可，不需要刷新表格
      if (hasChildren) {
        await refreshData()
      }
    } catch (error) {
      console.error('更新菜单状态失败:', error)
      row.status = originalStatus
    } finally {
      row._statusLoading = false
    }
  }
</script>

<style scoped lang="scss">
  .menu-page {
    :deep(.el-table) {
      .el-table__row {
        .cell {
          display: flex;
          align-items: center;
        }
      }

      // 配合列设置弹窗中的对齐：EP 将 align 渲染为单元格的 is-center/is-right 类
      .el-table__cell.is-center .cell {
        justify-content: center;
      }

      .el-table__cell.is-right .cell {
        justify-content: flex-end;
      }
    }
  }
</style>
