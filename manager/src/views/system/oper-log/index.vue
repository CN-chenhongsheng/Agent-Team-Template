<!-- 操作日志管理页面 -->
<template>
  <div class="oper-log-page art-full-height">
    <!-- 搜索栏 -->
    <OperLogSearch
      v-show="showSearchBar"
      v-model="searchForm"
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
              v-permission="'system:operlog:delete'"
            >
              批量删除{{ selectedCount > 0 ? `(${selectedCount})` : '' }}
            </ElButton>
            <ElButton @click="handleClean" v-ripple v-permission="'system:operlog:clean'">
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
      <OperLogDetail v-model:visible="detailDialogVisible" :log-data="currentLogData" />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchGetOperLogList,
    fetchBatchDeleteOperLog,
    fetchCleanOperLog
  } from '@/api/system-manage'
  import OperLogSearch from './modules/oper-log-search.vue'
  import OperLogDetail from './modules/oper-log-detail.vue'
  import { ElTag, ElMessageBox, ElMessage } from 'element-plus'
  import { h } from 'vue'

  defineOptions({ name: 'OperLog' })

  type OperLogListItem = Api.SystemManage.OperLogListItem

  const showSearchBar = ref(false)
  const detailDialogVisible = ref(false)
  const currentLogData = ref<OperLogListItem | null>(null)

  // 选中行
  const selectedRows = ref<OperLogListItem[]>([])
  const selectedCount = computed(() => selectedRows.value.length)

  // 搜索表单
  const searchForm = ref<Api.SystemManage.OperLogSearchParams>({
    pageNum: 1,
    title: undefined,
    operName: undefined,
    businessType: undefined,
    status: undefined,
    operTimeStart: undefined,
    operTimeEnd: undefined
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
  } = useTable<typeof fetchGetOperLogList>({
    // 核心配置
    core: {
      apiFn: fetchGetOperLogList,
      apiParams: computed(() => {
        return {
          pageNum: 1,
          ...searchForm.value
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
          label: '操作模块',
          minWidth: 150
        },
        {
          prop: 'businessTypeText',
          label: '业务类型',
          width: 100,
          formatter: (row: OperLogListItem) => {
            return h(
              ElTag,
              { type: getBusinessTypeTag(row.businessType), size: 'small' },
              () => row.businessTypeText || '其它'
            )
          }
        },
        {
          prop: 'operName',
          label: '操作人员',
          width: 120
        },
        {
          prop: 'operIp',
          label: '操作IP',
          width: 130
        },
        {
          prop: 'statusText',
          label: '操作状态',
          width: 100,
          formatter: (row: OperLogListItem) => {
            return h(
              ElTag,
              { type: row.status === 0 ? 'success' : 'danger', size: 'small' },
              () => row.statusText || '正常'
            )
          }
        },
        {
          prop: 'operTime',
          label: '操作时间',
          width: 180,
          sortable: true
        },
        {
          prop: 'costTime',
          label: '耗时',
          width: 100,
          formatter: (row: OperLogListItem) => {
            return row.costTime ? `${row.costTime} ms` : '-'
          }
        },
        {
          prop: 'action',
          label: '操作',
          width: 150,
          fixed: 'right',
          formatter: (row: OperLogListItem) => [
            {
              type: 'view',
              label: '查看',
              onClick: () => handleViewDetail(row),
              auth: 'system:operlog:detail'
            },
            {
              type: 'delete',
              label: '删除',
              onClick: () => handleDelete(row),
              auth: 'system:operlog:delete',
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
    ElMessageBox.confirm('确定要删除这条操作日志吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
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
      ElMessage.warning('请选择要删除的日志')
      return
    }

    ElMessageBox.confirm(`确定要删除选中的 ${selectedCount.value} 条操作日志吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
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
    ElMessageBox.confirm('确定要清空所有操作日志吗？此操作不可恢复！', '危险操作', {
      type: 'warning',
      confirmButtonText: '确定清空',
      cancelButtonText: '取消',
      confirmButtonClass: 'el-button--danger'
    })
      .then(async () => {
        await fetchCleanOperLog()
        refreshData()
      })
      .catch(() => {})
  }
</script>
