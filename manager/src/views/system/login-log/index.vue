<!-- 登入日志管理页面 -->
<template>
  <div class="login-log-page art-full-height">
    <!-- 搜索栏 -->
    <LoginLogSearch
      v-show="showSearchBar"
      v-model="searchForm"
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
              v-permission="'system:loginlog:delete'"
            >
              批量删除{{ selectedCount > 0 ? `(${selectedCount})` : '' }}
            </ElButton>
            <ElButton @click="handleClean" v-ripple v-permission="'system:loginlog:clean'">
              清空日志
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
      <LoginLogDetail v-model:visible="detailDialogVisible" :log-data="currentLogData" />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchGetLoginLogList,
    fetchBatchDeleteLoginLog,
    fetchCleanLoginLog
  } from '@/api/system-manage'
  import LoginLogSearch from './modules/login-log-search.vue'
  import LoginLogDetail from './modules/login-log-detail.vue'
  import { h } from 'vue'

  defineOptions({ name: 'LoginLog' })

  type LoginLogListItem = Api.SystemManage.LoginLogListItem

  const showSearchBar = ref(false)
  const detailDialogVisible = ref(false)
  const currentLogData = ref<LoginLogListItem | null>(null)

  // 选中行
  const selectedRows = ref<LoginLogListItem[]>([])
  const selectedCount = computed(() => selectedRows.value.length)

  // 搜索表单
  const searchForm = ref<Api.SystemManage.LoginLogSearchParams>({
    pageNum: 1,
    username: undefined,
    loginType: undefined,
    loginStatus: undefined,
    startTime: undefined,
    endTime: undefined
  })

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
          ...searchForm.value
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
          label: '用户账号',
          minWidth: 120
        },
        {
          prop: 'loginTypeText',
          label: '登录类型',
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
          label: '登录状态',
          width: 100,
          formatter: (row: LoginLogListItem) => {
            const typeMap = {
              0: 'danger', // 失败
              1: 'success', // 成功
              2: 'info' // 登出
            }
            return h(
              ElTag,
              { type: typeMap[row.loginStatus as 0 | 1 | 2] || 'info', size: 'small' },
              () => row.loginStatusText || '-'
            )
          }
        },
        {
          prop: 'ipAddress',
          label: 'IP地址',
          width: 130
        },
        {
          prop: 'loginLocation',
          label: '登录地点',
          minWidth: 150
        },
        {
          prop: 'browser',
          label: '浏览器',
          minWidth: 120,
          showOverflowTooltip: true
        },
        {
          prop: 'os',
          label: '操作系统',
          minWidth: 120,
          showOverflowTooltip: true
        },
        {
          prop: 'loginTime',
          label: '登录时间',
          width: 180,
          sortable: true
        },
        {
          prop: 'action',
          label: '操作',
          width: 150,
          fixed: 'right',
          formatter: (row: LoginLogListItem) => [
            {
              type: 'view',
              label: '查看',
              onClick: () => handleViewDetail(row),
              auth: 'system:loginlog:detail'
            },
            {
              type: 'delete',
              label: '删除',
              onClick: () => handleDelete(row),
              auth: 'system:loginlog:delete',
              danger: true
            }
          ]
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
    searchForm.value = {
      ...searchForm.value,
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
    ElMessageBox.confirm('确定要删除这条登入日志吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
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
      ElMessage.warning('请选择要删除的日志')
      return
    }

    ElMessageBox.confirm(`确定要删除选中的 ${selectedCount.value} 条登入日志吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
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
    ElMessageBox.confirm('确定要清空所有登入日志吗？此操作不可恢复！', '危险操作', {
      type: 'warning',
      confirmButtonText: '确定清空',
      cancelButtonText: '取消',
      confirmButtonClass: 'el-button--danger'
    })
      .then(async () => {
        await fetchCleanLoginLog()
        refreshData()
      })
      .catch(() => {})
  }
</script>
