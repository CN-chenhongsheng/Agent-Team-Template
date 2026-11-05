<!-- 登入日志管理页面 -->
<template>
  <div class="login-log-page art-full-height">
    <!-- 搜索栏 -->
    <LoginLogSearch
      v-show="showSearchBar"
      v-model="formFilters"
      @search="handleSearch"
      @reset="resetSearchParams"
    ></LoginLogSearch>

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
              :disabled="selectedCount === 0"
              @click="handleBatchDelete"
              v-ripple
              v-permission="PERMISSIONS.system.loginLog.delete"
            >
              {{ $t('system.loginLog.batchDelete')
              }}{{ selectedCount > 0 ? `(${selectedCount})` : '' }}
            </ElButton>
            <ElButton
              @click="handleClean"
              v-ripple
              v-permission="PERMISSIONS.system.loginLog.clean"
            >
              {{ $t('system.loginLog.clean') }}
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

      <!-- 详情弹窗 -->
      <LoginLogDetail v-model="detailDialogVisible" :log-data="currentLogData" />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n'
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchGetLoginLogList,
    fetchBatchDeleteLoginLog,
    fetchCleanLoginLog
  } from '@/api/system-manage'
  import LoginLogSearch from './modules/login-log-search.vue'
  import LoginLogDetail from './modules/login-log-detail.vue'
  import type { ActionButtonConfig } from '@/types/component'
  import { PERMISSIONS } from '@/utils/constants'
  import { h } from 'vue'

  defineOptions({ name: 'LoginLog' })

  const { t } = useI18n()

  type LoginLogListItem = Api.SystemManage.LoginLogListItem

  const showSearchBar = ref(false)
  const detailDialogVisible = ref(false)
  const currentLogData = ref<LoginLogListItem | null>(null)

  // 选中行
  const selectedRows = ref<LoginLogListItem[]>([])
  const selectedCount = computed(() => selectedRows.value.length)

  // 搜索表单
  const formFilters = ref<Api.SystemManage.LoginLogSearchParams>({
    pageNum: 1,
    username: undefined,
    loginType: undefined,
    loginStatus: undefined,
    startTime: undefined,
    endTime: undefined
  })

  /**
   * 获取登入日志操作配置
   */
  const getLoginLogActions = (row: LoginLogListItem): ActionButtonConfig[] => [
    {
      type: 'view',
      label: t('system.common.view'),
      onClick: () => handleViewDetail(row),
      auth: PERMISSIONS.system.loginLog.detail
    },
    {
      type: 'delete',
      label: t('system.common.delete'),
      onClick: () => handleDelete(row),
      auth: PERMISSIONS.system.loginLog.delete,
      danger: true
    }
  ]

  const {
    columns,
    columnChecks,
    data,
    loading,
    pagination,
    resetSearchParams,
    handleSizeChange,
    handleCurrentChange,
    refreshData,
    refreshRemove,
    contextMenuItems,
    contextMenuWidth,
    handleRowContextmenu,
    handleContextMenuSelect
  } = useTable<typeof fetchGetLoginLogList>({
    // 核心配置
    core: {
      apiFn: fetchGetLoginLogList,
      apiParams: computed(() => {
        return {
          ...formFilters.value
        } as Api.SystemManage.LoginLogSearchParams
      }),
      // 自定义分页字段映射
      paginationKey: {
        current: 'pageNum',
        size: 'pageSize'
      },
      columnsFactory: () => [
        { type: 'selection' }, // 勾选列
        {
          prop: 'username',
          label: t('system.loginLog.userAccount'),
          minWidth: 120
        },
        {
          prop: 'loginTypeText',
          label: t('system.loginLog.loginType'),
          width: 100,
          formatter: (row: LoginLogListItem) => {
            return h(
              ElTag,
              { type: row.loginType === 'login' ? 'primary' : 'info', size: 'small' },
              () => row.loginTypeText || '-'
            )
          }
        },
        {
          prop: 'loginStatusText',
          label: t('system.loginLog.loginStatus'),
          width: 100,
          formatter: (row: LoginLogListItem) => {
            const typeMap: Record<0 | 1 | 2, 'danger' | 'success' | 'info'> = {
              0: 'danger',
              1: 'success',
              2: 'info'
            }
            const status = row.loginStatus as 0 | 1 | 2
            return h(
              ElTag,
              { type: typeMap[status] ?? 'info', size: 'small' },
              () => row.loginStatusText || '-'
            )
          }
        },
        {
          prop: 'ipAddress',
          label: t('system.loginLog.ip'),
          width: 130
        },
        {
          prop: 'loginLocation',
          label: t('system.loginLog.location'),
          minWidth: 150
        },
        {
          prop: 'browser',
          label: t('system.loginLog.browser'),
          minWidth: 120,
          showOverflowTooltip: true
        },
        {
          prop: 'os',
          label: t('system.loginLog.os'),
          minWidth: 120,
          showOverflowTooltip: true
        },
        {
          prop: 'loginTime',
          label: t('system.loginLog.loginTime'),
          width: 180,
          sortable: true
        },
        {
          prop: 'action',
          label: t('system.loginLog.action'),
          width: 150,
          fixed: 'right',
          formatter: getLoginLogActions
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

  /**
   * 处理搜索
   */
  const handleSearch = (params: Record<string, any>) => {
    formFilters.value = {
      ...formFilters.value,
      ...params,
      pageNum: 1
    }
    refreshData()
  }

  /**
   * 处理选择变化
   */
  const handleSelectionChange = (rows: LoginLogListItem[]) => {
    selectedRows.value = rows
  }

  /**
   * 查看详情
   */
  const handleViewDetail = (row: LoginLogListItem) => {
    currentLogData.value = row
    detailDialogVisible.value = true
  }

  /**
   * 删除单条日志
   */
  const handleDelete = (row: LoginLogListItem) => {
    ElMessageBox.confirm(t('system.loginLog.deleteConfirm'), t('system.common.tip'), {
      type: 'warning',
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel')
    })
      .then(async () => {
        await fetchBatchDeleteLoginLog([row.id])
        refreshRemove()
      })
      .catch(() => {})
  }

  /**
   * 批量删除
   */
  const handleBatchDelete = () => {
    if (selectedCount.value === 0) {
      ElMessage.warning(t('system.loginLog.selectToDelete'))
      return
    }

    ElMessageBox.confirm(
      t('system.loginLog.batchDeleteConfirm', { count: selectedCount.value }),
      t('system.common.tip'),
      {
        type: 'warning',
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel')
      }
    )
      .then(async () => {
        const ids = selectedRows.value.map((row) => row.id)
        await fetchBatchDeleteLoginLog(ids)
        selectedRows.value = []
        refreshRemove()
      })
      .catch(() => {})
  }

  /**
   * 清空日志
   */
  const handleClean = () => {
    ElMessageBox.confirm(t('system.loginLog.cleanConfirm'), t('system.common.dangerTitle'), {
      type: 'warning',
      confirmButtonText: t('system.common.confirmClean'),
      cancelButtonText: t('common.cancel'),
      confirmButtonClass: 'el-button--danger'
    })
      .then(async () => {
        await fetchCleanLoginLog()
        refreshData()
      })
      .catch(() => {})
  }
</script>
