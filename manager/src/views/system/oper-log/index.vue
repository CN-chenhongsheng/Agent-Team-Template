<!-- 操作日志管理页面 -->
<template>
  <div class="oper-log-page art-full-height">
    <!-- 搜索栏 -->
    <OperLogSearch
      v-show="showSearchBar"
      v-model="formFilters"
      @search="handleSearch"
      @reset="resetSearchParams"
    ></OperLogSearch>

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
              v-permission="PERMISSIONS.system.operLog.delete"
            >
              {{ $t('system.operLog.batchDelete')
              }}{{ selectedCount > 0 ? `(${selectedCount})` : '' }}
            </ElButton>
            <ElButton @click="handleClean" v-ripple v-permission="PERMISSIONS.system.operLog.clean">
              {{ $t('system.operLog.clean') }}
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
      <OperLogDetail v-model="detailDialogVisible" :log-data="currentLogData" />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n'
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchGetOperLogList,
    fetchBatchDeleteOperLog,
    fetchCleanOperLog
  } from '@/api/system-manage'
  import OperLogSearch from './modules/oper-log-search.vue'
  import OperLogDetail from './modules/oper-log-detail.vue'
  import type { ActionButtonConfig } from '@/types/component'
  import { PERMISSIONS } from '@/utils/constants'
  import { h } from 'vue'

  defineOptions({ name: 'OperLog' })

  const { t } = useI18n()

  type OperLogListItem = Api.SystemManage.OperLogListItem

  const showSearchBar = ref(false)
  const detailDialogVisible = ref(false)
  const currentLogData = ref<OperLogListItem | null>(null)

  // 选中行
  const selectedRows = ref<OperLogListItem[]>([])
  const selectedCount = computed(() => selectedRows.value.length)

  // 搜索表单
  const formFilters = ref<Api.SystemManage.OperLogSearchParams>({
    pageNum: 1,
    title: undefined,
    operName: undefined,
    businessType: undefined,
    status: undefined,
    operTimeStart: undefined,
    operTimeEnd: undefined
  })

  /**
   * 获取操作日志操作配置
   */
  const getOperLogActions = (row: OperLogListItem): ActionButtonConfig[] => [
    {
      type: 'view',
      label: t('system.common.view'),
      onClick: () => handleViewDetail(row),
      auth: PERMISSIONS.system.operLog.detail
    },
    {
      type: 'delete',
      label: t('system.common.delete'),
      onClick: () => handleDelete(row),
      auth: PERMISSIONS.system.operLog.delete,
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
  } = useTable<typeof fetchGetOperLogList>({
    // 核心配置
    core: {
      apiFn: fetchGetOperLogList,
      apiParams: computed(() => {
        return {
          pageNum: 1,
          ...formFilters.value
        } as Api.SystemManage.OperLogSearchParams
      }),
      // 自定义分页字段映射
      paginationKey: {
        current: 'pageNum',
        size: 'pageSize'
      },
      columnsFactory: () => [
        { type: 'selection' }, // 勾选列
        {
          prop: 'title',
          label: t('system.operLog.module'),
          minWidth: 150
        },
        {
          prop: 'businessTypeText',
          label: t('system.operLog.businessType'),
          width: 100,
          formatter: (row: OperLogListItem) => {
            return h(
              ElTag,
              { type: getBusinessTypeTag(row.businessType), size: 'small' },
              () => row.businessTypeText || t('system.operLog.otherType')
            )
          }
        },
        {
          prop: 'operName',
          label: t('system.operLog.operator'),
          width: 120
        },
        {
          prop: 'operIp',
          label: t('system.operLog.ip'),
          width: 130
        },
        {
          prop: 'statusText',
          label: t('system.operLog.status'),
          width: 100,
          formatter: (row: OperLogListItem) => {
            return h(
              ElTag,
              { type: row.status === 0 ? 'success' : 'danger', size: 'small' },
              () => row.statusText || t('system.operLog.normal')
            )
          }
        },
        {
          prop: 'operTime',
          label: t('system.operLog.time'),
          width: 180,
          sortable: true
        },
        {
          prop: 'costTime',
          label: t('system.operLog.cost'),
          width: 100,
          formatter: (row: OperLogListItem) => {
            return row.costTime ? `${row.costTime} ms` : '-'
          }
        },
        {
          prop: 'action',
          label: t('system.operLog.action'),
          width: 150,
          fixed: 'right',
          formatter: getOperLogActions
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
   * 获取业务类型标签类型
   */
  const getBusinessTypeTag = (businessType?: number): 'info' | 'success' | 'warning' | 'danger' => {
    if (businessType === 1) return 'success' // 新增
    if (businessType === 2) return 'warning' // 修改
    if (businessType === 3) return 'danger' // 删除
    return 'info' // 其它
  }

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
  const handleSelectionChange = (rows: OperLogListItem[]) => {
    selectedRows.value = rows
  }

  /**
   * 查看详情
   */
  const handleViewDetail = (row: OperLogListItem) => {
    currentLogData.value = row
    detailDialogVisible.value = true
  }

  /**
   * 删除单条日志
   */
  const handleDelete = (row: OperLogListItem) => {
    ElMessageBox.confirm(t('system.operLog.deleteConfirm'), t('system.common.tip'), {
      type: 'warning',
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel')
    })
      .then(async () => {
        await fetchBatchDeleteOperLog([row.id])
        refreshRemove()
      })
      .catch(() => {})
  }

  /**
   * 批量删除
   */
  const handleBatchDelete = () => {
    if (selectedCount.value === 0) {
      ElMessage.warning(t('system.operLog.selectToDelete'))
      return
    }

    ElMessageBox.confirm(
      t('system.operLog.batchDeleteConfirm', { count: selectedCount.value }),
      t('system.common.tip'),
      {
        type: 'warning',
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel')
      }
    )
      .then(async () => {
        const ids = selectedRows.value.map((row) => row.id)
        await fetchBatchDeleteOperLog(ids)
        selectedRows.value = []
        refreshRemove()
      })
      .catch(() => {})
  }

  /**
   * 清空日志
   */
  const handleClean = () => {
    ElMessageBox.confirm(t('system.operLog.cleanConfirm'), t('system.common.dangerTitle'), {
      type: 'warning',
      confirmButtonText: t('system.common.confirmClean'),
      cancelButtonText: t('common.cancel'),
      confirmButtonClass: 'el-button--danger'
    })
      .then(async () => {
        await fetchCleanOperLog()
        refreshData()
      })
      .catch(() => {})
  }
</script>
