<!-- 角色管理页面 -->
<template>
  <div class="art-full-height">
    <RoleSearch
      v-show="showSearchBar"
      v-model="formFilters"
      @search="handleSearch"
      @reset="handleReset"
    ></RoleSearch>

    <ElCard
      class="art-table-card"
      shadow="never"
      :style="{ 'margin-top': showSearchBar ? '12px' : '0' }"
    >
      <ArtTableHeader
        v-model:columns="columnChecks"
        v-model:showSearchBar="showSearchBar"
        :loading="loading"
        @refresh="refreshData"
      >
        <template #left>
          <ElSpace wrap>
            <ElButton
              @click="showDialog('add')"
              v-ripple
              v-permission="PERMISSIONS.system.role.add"
            >
              {{ $t('system.role.add') }}
            </ElButton>
            <ElButton
              :disabled="selectedCount === 0"
              @click="handleBatchDelete"
              v-ripple
              v-permission="PERMISSIONS.system.role.delete"
            >
              {{ $t('system.role.batchDelete') }}{{ selectedCount > 0 ? `(${selectedCount})` : '' }}
            </ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <!-- 表格 -->
      <ArtTable
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        :contextMenuItems="contextMenuItems"
        :contextMenuWidth="contextMenuWidth"
        :onRowContextmenu="handleRowContextmenu as any"
        :onContextMenuSelect="handleContextMenuSelect"
        @selection-change="handleSelectionChange"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      >
      </ArtTable>
    </ElCard>

    <!-- 角色编辑弹窗 -->
    <RoleEditDialog
      v-model="dialogVisible"
      :dialog-type="dialogType"
      :role-data="editData"
      @success="handleEditDialogSuccess"
    />

    <!-- 菜单权限弹窗 -->
    <RolePermissionDialog
      v-model="permissionDialog"
      :role-data="editData"
      @success="refreshUpdate"
    />
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n'
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchGetRoleList,
    fetchDeleteRole,
    fetchBatchDeleteRole,
    fetchUpdateRoleStatus
  } from '@/api/system-manage'
  import RoleSearch from './modules/role-search.vue'
  import RoleEditDialog from './modules/role-edit-dialog.vue'
  import RolePermissionDialog from './modules/role-permission-dialog.vue'
  import ArtSwitch from '@/components/core/forms/art-switch/index.vue'
  import type { ActionButtonConfig } from '@/types/component'
  import type { DialogType } from '@/types'
  import { PERMISSIONS } from '@/utils/constants'

  defineOptions({ name: 'Role' })

  const { t } = useI18n()

  type RoleListItem = Api.SystemManage.RoleListItem & { _statusLoading?: boolean }

  // 搜索相关
  const formFilters = ref({
    pageNum: 1,
    roleName: undefined,
    roleCode: undefined,
    status: undefined
  })

  const showSearchBar = ref(true)
  const dialogVisible = ref(false)
  const permissionDialog = ref(false)
  const editData = ref<RoleListItem | undefined>(undefined)
  const selectedRows = ref<RoleListItem[]>([])
  const selectedCount = computed(() => selectedRows.value.length)

  /**
   * 获取角色操作配置
   */
  const getRoleActions = (row: RoleListItem): ActionButtonConfig[] => {
    const buttons: ActionButtonConfig[] = [
      {
        type: 'share',
        onClick: () => showPermissionDialog(row),
        auth: PERMISSIONS.system.role.assign,
        label: t('system.user.assignPermission')
      },
      {
        type: 'edit',
        onClick: () => showDialog('edit', row),
        auth: PERMISSIONS.system.role.edit,
        label: t('system.common.edit')
      }
    ]
    // 超级管理员角色不允许删除
    if (row.roleCode !== 'SUPER_ADMIN') {
      buttons.push({
        type: 'delete',
        onClick: () => deleteRole(row),
        auth: PERMISSIONS.system.role.delete,
        danger: true,
        label: t('system.common.delete')
      })
    }
    return buttons
  }

  const {
    columns,
    columnChecks,
    data,
    loading,
    pagination,
    getData,
    resetSearchParams,
    handleSizeChange,
    handleCurrentChange,
    refreshData,
    refreshCreate,
    refreshUpdate,
    refreshRemove,
    contextMenuItems,
    contextMenuWidth,
    handleRowContextmenu,
    handleContextMenuSelect
  } = useTable<typeof fetchGetRoleList>({
    // 核心配置
    core: {
      apiFn: fetchGetRoleList,
      apiParams: computed(() => {
        return {
          ...formFilters.value
        } as Api.SystemManage.RoleSearchParams
      }),
      // 自定义分页字段映射
      paginationKey: {
        current: 'pageNum',
        size: 'pageSize'
      },
      columnsFactory: () => [
        { type: 'selection' },
        {
          prop: 'roleName',
          label: t('system.role.roleName'),
          minWidth: 150,
          formatter: (row: RoleListItem) => {
            return h('div', { class: 'flex items-center gap-2' }, [
              h('span', { class: 'font-medium' }, `${row.roleName as string}#${row.id}`)
            ])
          }
        },
        {
          prop: 'roleCode',
          label: t('system.role.roleCode'),
          minWidth: 150,
          showOverflowTooltip: true,
          formatter: (row: RoleListItem) => {
            return row.roleCode
          }
        },
        {
          prop: 'sort',
          label: t('system.common.sort'),
          width: 80,
          sortable: true
        },
        {
          prop: 'status',
          label: t('system.common.status'),
          width: 100,
          formatter: (row: RoleListItem) => {
            const isSuperAdmin = row.roleCode === 'SUPER_ADMIN'
            return h(ArtSwitch, {
              modelValue: row.status === 1,
              loading: row._statusLoading || false,
              inlinePrompt: true,
              disabled: isSuperAdmin, // 超级管理员禁用开关
              onChange: (value: string | number | boolean) => {
                handleStatusChange(row, value === true || value === 1)
              }
            })
          }
        },
        {
          prop: 'remark',
          label: t('system.common.remark'),
          minWidth: 200,
          showOverflowTooltip: true
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
          fixed: 'right',
          formatter: getRoleActions
        }
      ]
    },
    adaptive: {
      enabled: true
    },
    contextMenu: {
      enabled: true
    }
  })

  const dialogType = ref<DialogType>('add')

  const showDialog = (type: 'add' | 'edit', row?: RoleListItem) => {
    dialogVisible.value = true
    dialogType.value = type
    editData.value = row ? { ...row } : undefined
  }

  /**
   * 搜索处理
   */
  const handleSearch = async (): Promise<void> => {
    formFilters.value.pageNum = 1
    await getData()
  }

  /**
   * 重置搜索
   */
  const handleReset = async (): Promise<void> => {
    formFilters.value = {
      pageNum: 1,
      roleName: undefined,
      roleCode: undefined,
      status: undefined
    }
    await resetSearchParams()
  }

  /**
   * 显示权限分配弹窗
   */
  const showPermissionDialog = (row?: RoleListItem) => {
    permissionDialog.value = true
    editData.value = row ? { ...row } : undefined
  }

  /**
   * 处理编辑弹窗成功事件
   */
  const handleEditDialogSuccess = async () => {
    // 根据 dialogType 判断是新增还是编辑
    if (dialogType.value === 'add') {
      await refreshCreate()
    } else {
      await refreshUpdate()
    }
  }

  /**
   * 删除角色
   */
  const deleteRole = async (row: RoleListItem) => {
    // 防止删除超级管理员角色
    if (row.roleCode === 'SUPER_ADMIN') {
      ElMessage.warning(t('system.role.superAdminDeleteForbidden'))
      return
    }

    try {
      await ElMessageBox.confirm(
        t('system.role.deleteConfirm', { name: row.roleName }),
        t('system.common.deleteConfirmTitle'),
        {
          type: 'warning',
          confirmButtonText: t('system.common.confirmDelete'),
          cancelButtonText: t('common.cancel'),
          dangerouslyUseHTMLString: true
        }
      )

      await fetchDeleteRole(row.id)
      await refreshRemove()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除角色失败:', error)
      }
    }
  }

  /**
   * 批量删除
   */
  const handleBatchDelete = async () => {
    if (selectedCount.value === 0) {
      ElMessage.warning(t('system.role.selectToDelete'))
      return
    }

    // 检查是否包含超级管理员
    const hasSuperAdmin = selectedRows.value.some((row) => row.roleCode === 'SUPER_ADMIN')
    if (hasSuperAdmin) {
      ElMessage.warning(t('system.role.batchContainsSuperAdmin'))
      return
    }

    try {
      await ElMessageBox.confirm(
        t('system.role.batchDeleteConfirm', { count: selectedCount.value }),
        t('system.role.batchDelete'),
        {
          type: 'warning',
          confirmButtonText: t('system.common.confirmDelete'),
          cancelButtonText: t('common.cancel'),
          dangerouslyUseHTMLString: true
        }
      )

      const ids = selectedRows.value.map((role) => role.id)
      await fetchBatchDeleteRole(ids)

      selectedRows.value = []
      await refreshRemove()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('批量删除失败:', error)
      }
    }
  }

  /**
   * 处理表格行选择变化
   */
  const handleSelectionChange = (selection: RoleListItem[]) => {
    selectedRows.value = selection
    console.log('选中行数据:', selectedRows.value)
  }

  /**
   * 更新角色状态
   */
  const handleStatusChange = async (row: RoleListItem, value: boolean): Promise<void> => {
    // 超级管理员不允许关闭
    if (row.roleCode === 'SUPER_ADMIN' && !value) {
      ElMessage.warning(t('system.role.superAdminDisableForbidden'))
      return
    }

    // 如果是关闭操作（从启用变为停用），需要提示用户级联影响
    if (!value && row.status === 1) {
      try {
        await ElMessageBox.confirm(
          t('system.role.disableConfirm', { name: row.roleName }),
          t('system.common.disableConfirmTitle'),
          {
            type: 'warning',
            confirmButtonText: t('system.common.confirmDisable'),
            cancelButtonText: t('common.cancel'),
            dangerouslyUseHTMLString: true
          }
        )
      } catch {
        // 用户取消操作，不执行任何更改
        return
      }
    }

    const originalStatus = row.status
    try {
      row._statusLoading = true
      row.status = value ? 1 : 0
      await fetchUpdateRoleStatus(row.id, value ? 1 : 0)
    } catch (error) {
      console.error('更新角色状态失败:', error)
      row.status = originalStatus
    } finally {
      row._statusLoading = false
    }
  }
</script>
