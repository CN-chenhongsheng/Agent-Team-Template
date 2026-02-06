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
            <ElButton @click="handleAddMenu" v-ripple v-permission="'system:menu:add'">
              添加菜单
            </ElButton>
            <ElButton @click="toggleExpand" v-ripple>
              {{ isExpanded ? '收起全部' : '展开全部' }}
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
        v-model:visible="dialogVisible"
        :type="dialogType"
        :edit-data="editData"
        @submit="handleSubmit"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { useTable } from '@/hooks/core/useTable'
  import MenuDialog from './modules/menu-dialog.vue'
  import MenuSearch from './modules/menu-search.vue'
  import { fetchGetMenuList, fetchDeleteMenu, fetchUpdateMenuStatus } from '@/api/system-manage'
  import { useReferenceStore } from '@/store/modules/reference'
  import { ElTag, ElMessageBox } from 'element-plus'
  import ArtSwitch from '@/components/core/forms/art-switch/index.vue'
  import { h, nextTick } from 'vue'

  // 使用参考数据 store（用于缓存失效）
  const referenceStore = useReferenceStore()

  defineOptions({ name: 'Menus' })

  type MenuListItem = Api.SystemManage.MenuListItem & { _statusLoading?: boolean }

  // 状态管理
  const isExpanded = ref(false)
  const tableRef = ref()
  const showSearchBar = ref(false)

  // 弹窗相关
  const dialogVisible = ref(false)
  const dialogType = ref<'add' | 'edit'>('add')
  const editData = ref<MenuListItem | null>(null)

  // 搜索相关
  const formFilters = ref({
    menuName: '',
    status: undefined
  })

  /**
   * 获取菜单类型标签颜色
   */
  const getMenuTypeTag = (type: string): 'primary' | 'success' | 'info' => {
    const typeMap: Record<string, 'primary' | 'success' | 'info'> = {
      M: 'success', // 目录
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
      M: '目录',
      C: '菜单',
      F: '按钮'
    }
    return typeMap[type] || '未知'
  }

  /**
   * 获取菜单图标
   */
  const getMenuIcon = (icon?: string) => {
    return icon || 'i-carbon-document'
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
          menuName: formFilters.value.menuName || undefined,
          status: formFilters.value.status
        } as Partial<Api.SystemManage.MenuSearchParams>
      }),
      columnsFactory: () => [
        {
          prop: 'menuName',
          label: '菜单名称',
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
          label: '类型',
          width: 80,
          formatter: (row: MenuListItem) => {
            return h(ElTag, { type: getMenuTypeTag(row.menuType), size: 'small' }, () =>
              getMenuTypeText(row.menuType)
            )
          }
        },
        {
          prop: 'path',
          label: '路由路径',
          minWidth: 150,
          formatter: (row: MenuListItem) => {
            return row.path || '-'
          }
        },
        {
          prop: 'component',
          label: '组件路径',
          minWidth: 180,
          showOverflowTooltip: true,
          formatter: (row: MenuListItem) => {
            return row.component || '-'
          }
        },
        {
          prop: 'permission',
          label: '权限标识',
          minWidth: 150,
          formatter: (row: MenuListItem) => {
            if (!row.permission) return '-'
            return `${row.permission.split(',').length} 个权限标识`
          }
        },
        {
          prop: 'sort',
          label: '排序',
          width: 85,
          sortable: true
        },
        {
          prop: 'visible',
          label: '可见',
          width: 80,
          formatter: (row: MenuListItem) => {
            return h(
              ElTag,
              {
                type: row.visible === 1 ? 'success' : 'info',
                size: 'small'
              },
              () => (row.visible === 1 ? '是' : '否')
            )
          }
        },
        {
          prop: 'status',
          label: '状态',
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
          label: '创建时间',
          width: 180,
          sortable: true
        },
        {
          prop: 'action',
          label: '操作',
          width: 180,
          fixed: 'right' as const,
          formatter: (row: MenuListItem) => {
            const buttons = []
            // 只有目录和菜单可以添加子菜单/按钮
            if (row.menuType === 'M' || row.menuType === 'C') {
              buttons.push({
                type: 'add',
                label: '新增',
                onClick: () => handleAddSubMenu(row),
                auth: 'system:menu:add'
              })
            }
            buttons.push({
              type: 'edit',
              label: '编辑',
              onClick: () => handleEditMenu(row),
              auth: 'system:menu:edit'
            })
            buttons.push({
              type: 'delete',
              label: '删除',
              onClick: () => handleDeleteMenu(row),
              auth: 'system:menu:delete',
              danger: true
            })
            return buttons
          }
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
  const handleSubmit = async (): Promise<void> => {
    dialogVisible.value = false
    // 刷新菜单树缓存
    await referenceStore.refreshMenuTreeSelect()
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
      let message = `确定要删除菜单"${row.menuName}"吗？`
      if (hasChildren) {
        message += `<br/>提示：删除菜单后，该菜单下的所有 ${childrenCount} 个子菜单也会被删除，此操作不可恢复！`
      } else {
        message += '此操作不可恢复！'
      }

      await ElMessageBox.confirm(message, '删除确认', {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: true
      })

      await fetchDeleteMenu(row.id)
      // 刷新菜单树缓存
      await referenceStore.refreshMenuTreeSelect()
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
        let message = `确定要停用菜单"${row.menuName}"吗？`
        if (hasChildren) {
          message += `<br/>提示：停用菜单后，该菜单下的所有子菜单也会被停用，且关联的角色也将失去对此菜单及其子菜单的权限。`
        } else {
          message += `<br/>提示：关联的角色将失去对此菜单的权限。`
        }
        await ElMessageBox.confirm(message, '确认停用', {
          type: 'warning',
          confirmButtonText: '确认停用',
          cancelButtonText: '取消',
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
    }
  }
</style>
