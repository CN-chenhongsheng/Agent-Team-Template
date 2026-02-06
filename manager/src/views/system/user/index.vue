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
            <ElButton @click="showDialog('add')" v-ripple v-permission="'system:user:add'">
              新增用户
            </ElButton>
            <ElButton
              :disabled="selectedCount === 0"
              @click="handleBatchDelete"
              v-ripple
              v-permission="'system:user:delete'"
            >
              批量删除{{ selectedCount > 0 ? `(${selectedCount})` : '' }}
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
        v-model:visible="dialogVisible"
        :type="dialogType"
        :user-data="editData"
        @submit="handleDialogSubmit"
      />

      <!-- 管理范围分配弹窗 -->
      <UserScopeDialog
        v-model:visible="scopeDialogVisible"
        :user-data="currentScopeUser"
        @submit="handleScopeSubmit"
      />

      <!-- 权限分配弹窗 -->
      <UserPermissionDialog
        v-model="permissionDialogVisible"
        :user-data="currentPermissionUser"
        @success="handlePermissionSuccess"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { ACCOUNT_TABLE_DATA } from '@/mock/temp/formData'
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchGetUserList,
    fetchDeleteUser,
    fetchBatchDeleteUser,
    fetchUpdateUserStatus,
    fetchResetUserPassword,
    fetchUpdateUser
  } from '@/api/system-manage'
  import UserSearch from './modules/user-search.vue'
  import UserDialog from './modules/user-dialog.vue'
  import UserScopeDialog from './modules/user-scope-dialog.vue'
  import UserPermissionDialog from './modules/user-permission-dialog.vue'
  import { ElTag, ElMessageBox, ElImage, ElMessage, ElTooltip } from 'element-plus'
  import ArtSwitch from '@/components/core/forms/art-switch/index.vue'
  import { DialogType } from '@/types'
  import type { ActionButtonConfig } from '@/types/component'
  import { useUserOnlineStatus } from '@/hooks/core/useUserOnlineStatus'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import { formatManageScopeSync } from '@/utils/school/scopeFormatter'

  defineOptions({ name: 'User' })

  type UserListItem = Api.SystemManage.UserListItem

  const { onlineStatusMap } = useUserOnlineStatus()

  // 预加载节点名称缓存（在后台异步加载）
  import('@/utils/school/scopeFormatter').then((module) => {
    module.formatManageScope('').catch(() => {
      // 静默失败，不影响页面显示
    })
  })

  const showSearchBar = ref(false)
  const dialogType = ref<DialogType>('add')
  const dialogVisible = ref(false)
  const editData = ref<Partial<UserListItem>>({})
  const scopeDialogVisible = ref(false)
  const currentScopeUser = ref<Partial<UserListItem>>({})
  const permissionDialogVisible = ref(false)
  const currentPermissionUser = ref<UserListItem | undefined>(undefined)

  // 选中行
  const selectedRows = ref<UserListItem[]>([])
  const selectedCount = computed(() => selectedRows.value.length)

  // 搜索相关
  const formFilters = ref<Api.SystemManage.UserSearchParams>({
    pageNum: 1,
    username: undefined,
    nickname: undefined,
    phone: undefined,
    manageScope: undefined,
    status: undefined
  })

  // 前置声明函数（后面会实际定义）
  let showDialog: (type: DialogType, row?: UserListItem) => void
  let deleteUser: (row: UserListItem) => Promise<void>
  let handleResetPassword: (row: UserListItem) => Promise<void>
  let showScopeDialog: (row: UserListItem) => void
  let showPermissionDialog: (row: UserListItem) => void

  /**
   * 获取用户操作配置（复用于操作列和右键菜单）
   */
  const getUserActions = (row: UserListItem): ActionButtonConfig[] => [
    {
      type: 'edit',
      label: '编辑',
      onClick: () => showDialog('edit', row),
      auth: 'system:user:edit'
    },
    {
      type: 'reset',
      label: '重置密码',
      onClick: () => handleResetPassword(row),
      auth: 'system:user:reset-pwd'
    },
    {
      type: 'share',
      label: '分配范围',
      onClick: () => showScopeDialog(row),
      auth: 'system:user:assign-scope'
    },
    {
      type: 'share',
      label: '分配权限',
      onClick: () => showPermissionDialog(row),
      auth: 'system:user:assign-permission'
    },
    {
      type: 'delete',
      label: '删除',
      onClick: () => deleteUser(row),
      auth: 'system:user:delete',
      danger: true
    }
  ]

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
  } = useTable<typeof fetchGetUserList>({
    // 核心配置
    core: {
      apiFn: fetchGetUserList,
      apiParams: computed(() => {
        return {
          pageNum: formFilters.value.pageNum,
          username: formFilters.value.username || undefined,
          nickname: formFilters.value.nickname || undefined,
          phone: formFilters.value.phone || undefined,
          manageScope: formFilters.value.manageScope || undefined,
          status: formFilters.value.status
        } as Partial<Api.SystemManage.UserSearchParams>
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
          label: '用户信息',
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
                h('p', { class: 'email text-xs text-gray-400' }, row.email || '未设置邮箱')
              ])
            ])
          }
        },
        {
          prop: 'genderText',
          label: '性别',
          width: 70,
          formatter: (row) => {
            const genderIcon = {
              1: 'ri-men-line',
              2: 'ri-women-line'
            }
            return h('div', { class: 'flex items-center gap-1' }, [
              h('span', { class: 'text-g-700 text-sm' }, row.genderText),
              h(ArtSvgIcon, {
                icon: genderIcon[row.gender as keyof typeof genderIcon] as string,
                class: `text-g-700 text-md ${row.gender === 1 ? 'text-primary' : 'text-pink-500'}`
              })
            ])
          }
        },
        {
          prop: 'phone',
          label: '手机号',
          width: 125
        },
        {
          prop: 'manageScope',
          label: '管理范围',
          minWidth: 150,
          formatter: (row) => {
            const formatted = formatManageScopeSync(row.manageScope)
            // 将字符串按"、"拆分，每个部分作为一个 tag
            const parts = formatted.split('、').filter((part) => part.trim())

            if (parts.length === 0 || formatted === '未设置' || formatted === '格式错误') {
              return h(ElTag, { type: 'info', size: 'small' }, () => formatted || '未设置')
            }

            // 如果只有一个标签，直接显示
            if (parts.length === 1) {
              return h(ElTag, { size: 'small', type: 'primary' }, () => parts[0])
            }

            // 多个标签：显示第一个 + [+N]，hover 显示全部
            const tooltipContent = parts.join('、')
            const displayTags = [
              h(ElTag, { size: 'small', type: 'primary' }, () => parts[0]),
              h(ElTag, { size: 'small', type: 'primary' }, () => `+${parts.length - 1}`)
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
          prop: 'roleNames',
          label: '角色',
          minWidth: 150,
          formatter: (row) => {
            const roleList = Array.isArray(row.roleNames)
              ? row.roleNames
              : row.roleNames
                ? (row.roleNames as unknown as string).split(',')
                : []
            if (roleList.length === 0) {
              return h('span', { class: 'text-gray-400' }, '暂无角色')
            }

            // 如果只有一个角色，直接显示
            if (roleList.length === 1) {
              return h(ElTag, { size: 'small', type: 'primary' }, () => roleList[0])
            }

            // 多个角色：显示第一个 + [+N]，hover 显示全部
            const tooltipContent = roleList.join('、')
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
          label: '在线状态',
          width: 100,
          formatter: (row) => {
            const isOnline = onlineStatusMap.value[row.id] ?? (row as any).isOnline
            return h(ElTag, { type: isOnline ? 'success' : 'info', size: 'small' }, () =>
              isOnline ? '在线' : '离线'
            )
          }
        },
        {
          prop: 'status',
          label: '系统状态',
          width: 100,
          formatter: (row) => {
            const isSuperAdmin = row.username === 'superAdmin'
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
          label: '最后登录',
          width: 180,
          sortable: true,
          formatter: (row) => row.lastLoginTime || '未登录'
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
          fixed: 'right',
          formatter: (row) => getUserActions(row)
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

        // 使用本地头像替换接口返回的头像（如果接口没有返回头像）
        return records.map((item, index: number) => {
          return {
            ...item,
            avatar: item.avatar || ACCOUNT_TABLE_DATA[index % ACCOUNT_TABLE_DATA.length].avatar
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
      manageScope: undefined,
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
    try {
      await ElMessageBox.confirm(
        `确定要删除用户 "${row.username}" 吗？此操作不可恢复！`,
        '删除用户',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
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
      ElMessage.warning('请先选择要删除的用户')
      return
    }

    try {
      await ElMessageBox.confirm(
        `确定要删除选中的 ${selectedCount.value} 个用户吗？此操作不可恢复！`,
        '批量删除',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
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
    if (row.username === 'superAdmin' && status === 0) {
      ElMessage.warning('超级管理员不允许停用')
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
        '请输入新密码（至少6位）',
        `重置用户 "${row.nickname}" 的密码`,
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputPattern: /.{6,}/,
          inputErrorMessage: '密码长度不能少于6位',
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
   * 显示管理范围分配对话框
   */
  showScopeDialog = (row: UserListItem): void => {
    currentScopeUser.value = { ...row }
    nextTick(() => {
      scopeDialogVisible.value = true
    })
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
   * 处理权限分配成功
   */
  const handlePermissionSuccess = (): void => {
    refreshData()
  }

  /**
   * 处理管理范围分配提交
   */
  const handleScopeSubmit = async (manageScope: string): Promise<void> => {
    if (!currentScopeUser.value.id) {
      ElMessage.error('用户ID不存在')
      return
    }

    try {
      // 使用完整的用户数据，只更新 manageScope 字段
      const updateData: Api.SystemManage.UserSaveParams = {
        id: currentScopeUser.value.id,
        username: currentScopeUser.value.username || '',
        nickname: currentScopeUser.value.nickname || '',
        email: currentScopeUser.value.email,
        phone: currentScopeUser.value.phone,
        gender: currentScopeUser.value.gender,
        status: currentScopeUser.value.status,
        roleIds: currentScopeUser.value.roleIds,
        manageScope // 更新管理范围
      }

      await fetchUpdateUser(currentScopeUser.value.id, updateData)
      await refreshUpdate()
    } catch (error) {
      console.error('分配管理范围失败:', error)
    }
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
