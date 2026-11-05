<!-- 用户管理页面 -->
<template>
  <div class="user-page art-full-height">
    <!-- 搜索栏 -->
    <UserSearch
      v-show="showSearchBar"
      v-model="formFilters"
      @search="handleSearch"
      @reset="handleReset"
    />

    <ElCard
      class="art-table-card"
      shadow="never"
      :style="{ 'margin-top': showSearchBar ? '12px' : '0' }"
    >
      <!-- 表格头部 -->
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
              v-permission="PERMISSIONS.system.user.add"
            >
              {{ $t('system.user.add') }}
            </ElButton>
            <ElButton
              @click="showImportDialog"
              v-ripple
              v-permission="PERMISSIONS.system.user.import"
            >
              {{ $t('system.user.import') }}
            </ElButton>
            <ElButton
              :disabled="selectedCount === 0"
              @click="handleBatchDelete"
              v-ripple
              v-permission="PERMISSIONS.system.user.delete"
            >
              {{ $t('system.user.batchDelete') }}{{ selectedCount > 0 ? `(${selectedCount})` : '' }}
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

      <!-- 用户弹窗 -->
      <UserDialog
        v-model="dialogVisible"
        :dialog-type="dialogType"
        :user-data="editData"
        @success="handleDialogSubmit"
      />

      <!-- 权限分配弹窗 -->
      <UserPermissionDialog
        v-model="permissionDialogVisible"
        :user-data="currentPermissionUser"
        @success="handlePermissionSuccess"
      />

      <!-- 用户查看抽屉 -->
      <UserDrawer v-model="drawerVisible" :user-id="currentUserId" />

      <!-- 用户导入弹窗 -->
      <ArtImportDialog
        v-model="importDialogVisible"
        :title="$t('system.user.importTitle')"
        :template-download-fn="handleDownloadTemplate"
        :scan-with-progress-fn="handleScanFile"
        @upload-success="handleUploadSuccess"
        @upload="handleImportSuccess"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n'
  import { useTable } from '@/hooks/core/useTable'
  import defaultAvatar from '@/assets/images/user/avatar.webp'
  import {
    fetchGetUserPage,
    fetchDeleteUser,
    fetchBatchDeleteUser,
    fetchUpdateUserStatus,
    fetchResetUserPassword
  } from '@/api/system-manage'
  import UserSearch from './modules/user-search.vue'
  import UserDialog from './modules/user-dialog.vue'
  import UserPermissionDialog from './modules/user-permission-dialog.vue'
  import UserDrawer from './modules/user-drawer.vue'
  import ArtImportDialog from '@/components/core/forms/art-import-dialog/index.vue'
  import { useGenericImport } from '@/composables/useGenericImport'
  import { userImportConfig } from './config/user-import-config'
  import { ElImage, ElTooltip } from 'element-plus'
  import ArtSwitch from '@/components/core/forms/art-switch/index.vue'
  import { DialogType } from '@/types'
  import type { ActionButtonConfig } from '@/types/component'
  import { useUserOnlineStatus } from '@/hooks/core/useUserOnlineStatus'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import { PERMISSIONS } from '@/utils/constants'

  defineOptions({ name: 'User' })

  const { t } = useI18n()

  type UserListItem = Api.SystemManage.UserListItem

  const { onlineStatusMap } = useUserOnlineStatus()

  const showSearchBar = ref(false)
  const dialogType = ref<DialogType>('add')
  const dialogVisible = ref(false)
  const editData = ref<Partial<UserListItem>>({})
  const permissionDialogVisible = ref(false)
  const currentPermissionUser = ref<UserListItem | undefined>(undefined)
  const drawerVisible = ref(false)
  const currentUserId = ref<number>()

  // 导入功能
  const importDialogVisible = ref(false)
  const { handleDownloadTemplate, handleScanFile, handleUploadSuccess } =
    useGenericImport(userImportConfig)

  // 选中行
  const selectedRows = ref<UserListItem[]>([])
  const selectedCount = computed(() => selectedRows.value.length)

  // 搜索相关
  const formFilters = ref<Api.SystemManage.UserSearchParams>({
    pageNum: 1,
    username: undefined,
    nickname: undefined,
    phone: undefined,
    status: undefined
  })

  // 前置声明函数（后面会实际定义）
  let showDialog: (type: DialogType, row?: UserListItem) => void
  let deleteUser: (row: UserListItem) => Promise<void>
  let handleResetPassword: (row: UserListItem) => Promise<void>
  let showPermissionDialog: (row: UserListItem) => void
  let showDrawer: (row: UserListItem) => void

  /**
   * 判断是否为超级管理员用户
   */
  const isSuperAdminUser = (row: UserListItem): boolean =>
    row.username === 'superAdmin' || row.roleNames?.includes('超级管理员') === true

  /**
   * 获取用户操作配置（复用于操作列和右键菜单）
   */
  const getUserActions = (row: UserListItem): ActionButtonConfig[] => {
    const actions: ActionButtonConfig[] = [
      {
        type: 'view',
        label: t('system.common.view'),
        onClick: () => showDrawer(row),
        auth: PERMISSIONS.system.user.view
      },
      {
        type: 'edit',
        label: t('system.common.edit'),
        onClick: () => showDialog('edit', row),
        auth: PERMISSIONS.system.user.edit
      },
      {
        type: 'reset',
        label: t('system.user.resetPassword'),
        onClick: () => handleResetPassword(row),
        auth: PERMISSIONS.system.user.resetPwd
      },
      {
        type: 'share',
        label: t('system.user.assignPermission'),
        onClick: () => showPermissionDialog(row),
        auth: PERMISSIONS.system.user.assignPermission
      }
    ]

    if (!isSuperAdminUser(row)) {
      actions.push({
        type: 'delete',
        label: t('system.common.delete'),
        onClick: () => deleteUser(row),
        auth: PERMISSIONS.system.user.delete,
        danger: true
      })
    }

    return actions
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
  } = useTable<typeof fetchGetUserPage>({
    // 核心配置
    core: {
      apiFn: fetchGetUserPage,
      apiParams: computed(() => {
        return {
          ...formFilters.value
        } as Api.SystemManage.UserSearchParams
      }),
      // 自定义分页字段映射
      paginationKey: {
        current: 'pageNum',
        size: 'pageSize'
      },
      columnsFactory: () => [
        { type: 'selection' }, // 勾选列
        {
          prop: 'userInfo',
          label: t('system.user.userInfo'),
          minWidth: 250,
          formatter: (row) => {
            return h('div', { class: 'user flex-c' }, [
              h(ElImage, {
                class: 'size-9.5 rounded-full',
                src: row.avatar || '/default-avatar.png',
                previewSrcList: [row.avatar || '/default-avatar.png'],
                previewTeleported: true
              }),
              h('div', { class: 'ml-2' }, [
                h('p', { class: 'nickname text-sm text-gray-500' }, `${row.nickname}#${row.id}`),
                h(
                  'p',
                  { class: 'email text-xs text-gray-400' },
                  row.email || t('system.user.noEmail')
                )
              ])
            ])
          }
        },
        {
          prop: 'genderText',
          label: t('system.user.gender'),
          width: 70,
          formatter: (row) => {
            const genderIcon = {
              1: 'ri-men-line',
              2: 'ri-women-line'
            }
            return h('div', { class: 'flex items-center gap-1' }, [
              h(
                'span',
                { class: 'text-g-700 text-sm' },
                row.gender === 1
                  ? t('system.userCenter.genderMale')
                  : row.gender === 2
                    ? t('system.userCenter.genderFemale')
                    : t('system.userCenter.genderUnknown')
              ),
              h(ArtSvgIcon, {
                icon: genderIcon[row.gender as keyof typeof genderIcon] as string,
                class: `text-g-700 text-md ${row.gender === 1 ? 'text-primary' : 'text-pink-500'}`
              })
            ])
          }
        },
        {
          prop: 'phone',
          label: t('system.user.phone'),
          width: 125
        },
        {
          prop: 'roleNames',
          label: t('system.user.role'),
          minWidth: 150,
          formatter: (row) => {
            const roleList = Array.isArray(row.roleNames)
              ? row.roleNames
              : row.roleNames
                ? (row.roleNames as unknown as string).split(',')
                : []
            if (roleList.length === 0) {
              return h('span', { class: 'text-gray-400' }, t('system.user.noRole'))
            }

            // 如果只有一个角色，直接显示
            if (roleList.length === 1) {
              return h(ElTag, { size: 'small', type: 'primary' }, () => roleList[0])
            }

            // 多个角色：显示第一个 + [+N]，hover 显示全部
            const tooltipContent = roleList.join(t('system.common.listSeparator'))
            const displayTags = [
              h(ElTag, { size: 'small', type: 'primary' }, () => roleList[0]),
              h(ElTag, { size: 'small', type: 'primary' }, () => `+${roleList.length - 1}`)
            ]

            return h(
              ElTooltip,
              {
                content: tooltipContent,
                placement: 'top',
                effect: 'dark'
              },
              {
                default: () => h('div', { class: 'flex gap-1' }, displayTags)
              }
            )
          }
        },
        {
          prop: 'onlineStatus',
          label: t('system.user.onlineStatus'),
          width: 100,
          formatter: (row) => {
            const isOnline = onlineStatusMap.value[row.id] ?? (row as any).isOnline
            return h(ElTag, { type: isOnline ? 'success' : 'info', size: 'small' }, () =>
              isOnline ? t('system.user.online') : t('system.user.offline')
            )
          }
        },
        {
          prop: 'status',
          label: t('system.user.systemStatus'),
          width: 100,
          formatter: (row) => {
            const isSuperAdmin = isSuperAdminUser(row)
            return h(ArtSwitch, {
              modelValue: row.status === 1,
              inlinePrompt: true,
              disabled: isSuperAdmin, // 超级管理员禁用开关
              onChange: async (value: boolean | string | number) => {
                const newStatus = typeof value === 'boolean' ? (value ? 1 : 0) : Number(value)
                await handleStatusChange(row, newStatus)
              }
            })
          }
        },
        {
          prop: 'lastLoginTime',
          label: t('system.user.lastLogin'),
          width: 180,
          sortable: true,
          formatter: (row) => row.lastLoginTime || t('system.user.neverLogin')
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
          formatter: getUserActions
        }
      ]
    },
    // 数据处理
    transform: {
      // 数据转换器 - 替换头像
      dataTransformer: (records: UserListItem[]) => {
        if (!Array.isArray(records)) {
          console.warn('数据转换器: 期望数组类型，实际收到:', typeof records)
          return []
        }

        // 接口没有返回头像时使用本地默认头像
        return records.map((item) => {
          return {
            ...item,
            avatar: item.avatar || defaultAvatar
          }
        })
      }
    },
    adaptive: {
      enabled: true
    },
    contextMenu: {
      enabled: true
    }
  })

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
      username: undefined,
      nickname: undefined,
      phone: undefined,
      status: undefined
    }
    await resetSearchParams()
  }

  /**
   * 显示用户弹窗
   */
  showDialog = (type: DialogType, row?: UserListItem): void => {
    console.log('打开弹窗:', { type, row })
    dialogType.value = type
    editData.value = row ? { ...row } : {}
    nextTick(() => {
      dialogVisible.value = true
    })
  }

  /**
   * 删除用户
   */
  deleteUser = async (row: UserListItem): Promise<void> => {
    if (isSuperAdminUser(row)) {
      ElMessage.warning(t('system.user.superAdminDeleteForbidden'))
      return
    }

    try {
      await ElMessageBox.confirm(
        t('system.user.deleteConfirm', { name: row.username }),
        t('system.user.deleteTitle'),
        {
          confirmButtonText: t('common.confirm'),
          cancelButtonText: t('common.cancel'),
          type: 'warning'
        }
      )

      await fetchDeleteUser(row.id)
      await refreshRemove()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除用户失败:', error)
      }
    }
  }

  /**
   * 批量删除
   */
  const handleBatchDelete = async (): Promise<void> => {
    if (selectedCount.value === 0) {
      ElMessage.warning(t('system.user.selectToDelete'))
      return
    }

    const hasSuperAdmin = selectedRows.value.some((row) => isSuperAdminUser(row))
    if (hasSuperAdmin) {
      ElMessage.warning(t('system.user.batchContainsSuperAdmin'))
      return
    }

    try {
      await ElMessageBox.confirm(
        t('system.user.batchDeleteConfirm', { count: selectedCount.value }),
        t('system.user.batchDelete'),
        {
          confirmButtonText: t('common.confirm'),
          cancelButtonText: t('common.cancel'),
          type: 'warning'
        }
      )

      const ids = selectedRows.value.map((user) => user.id)
      await fetchBatchDeleteUser(ids)

      selectedRows.value = []
      await refreshRemove()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('批量删除失败:', error)
      }
    }
  }

  /**
   * 更改用户状态
   */
  const handleStatusChange = async (row: UserListItem, status: number): Promise<void> => {
    // 超级管理员不允许关闭
    if (isSuperAdminUser(row) && status === 0) {
      ElMessage.warning(t('system.user.superAdminStatusForbidden'))
      return
    }

    const originalStatus = row.status
    try {
      row.status = status
      await fetchUpdateUserStatus(row.id, status)
    } catch (error) {
      console.error('更改状态失败:', error)
      // 恢复原状态
      row.status = originalStatus
    }
  }

  /**
   * 重置密码
   */
  handleResetPassword = async (row: UserListItem): Promise<void> => {
    try {
      const { value: newPassword } = await ElMessageBox.prompt(
        t('system.user.resetPasswordPrompt'),
        t('system.user.resetPasswordMessage', { name: row.nickname }),
        {
          confirmButtonText: t('common.confirm'),
          cancelButtonText: t('common.cancel'),
          inputPattern: /.{6,}/,
          inputErrorMessage: t('system.user.resetPasswordInvalid'),
          inputType: 'password'
        }
      )

      if (newPassword) {
        await fetchResetUserPassword(row.id, { newPassword: newPassword as string })
      }
    } catch (error) {
      if (error !== 'cancel') {
        console.error('重置密码失败:', error)
      }
    }
  }

  /**
   * 处理弹窗提交事件
   */
  const handleDialogSubmit = async () => {
    // 根据 dialogType 判断是新增还是编辑
    if (dialogType.value === 'add') {
      await refreshCreate()
    } else {
      await refreshUpdate()
    }
  }

  /**
   * 显示权限分配弹窗
   */
  showPermissionDialog = (row: UserListItem): void => {
    currentPermissionUser.value = row
    nextTick(() => {
      permissionDialogVisible.value = true
    })
  }

  /**
   * 显示用户查看抽屉
   */
  showDrawer = (row: UserListItem): void => {
    currentUserId.value = row.id
    drawerVisible.value = true
  }

  /**
   * 下载用户导入模板
   */
  const showImportDialog = (): void => {
    importDialogVisible.value = true
  }

  const handleImportSuccess = (): void => {
    importDialogVisible.value = false
    refreshData()
  }

  /**
   * 处理权限分配成功
   */
  const handlePermissionSuccess = (): void => {
    refreshData()
  }

  /**
   * 处理表格行选择变化
   */
  const handleSelectionChange = (selection: UserListItem[]): void => {
    selectedRows.value = selection
    console.log('选中行数据:', selectedRows.value)
  }
</script>

<style scoped lang="scss">
  .user-page {
    :deep(.user) {
      .user-name {
        margin-bottom: 4px;
        font-size: 14px;
        color: var(--el-text-color-primary);
      }

      .nickname {
        margin-bottom: 2px;
        font-size: 13px;
        color: var(--el-text-color-regular);
      }

      .email {
        font-size: 12px;
        color: var(--el-text-color-secondary);
      }
    }
  }
</style>
