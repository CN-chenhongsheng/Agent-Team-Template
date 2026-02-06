<!-- 报修管理页面 -->
<template>
  <div class="repair-page art-full-height">
    <!-- 搜索栏 -->
    <RepairSearch
      v-show="showSearchBar"
      v-model="searchForm"
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
        :showZebra="false"
        :loading="loading"
        v-model:columns="columnChecks"
        v-model:showSearchBar="showSearchBar"
        @refresh="handleRefresh"
      >
        <template #left>
          <ElSpace wrap>
            <ElButton
              :disabled="selectedCount === 0"
              @click="handleBatchDelete"
              v-ripple
              v-permission="'system:repair:delete'"
            >
              批量删除{{ selectedCount > 0 ? `(${selectedCount})` : '' }}
            </ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <ArtTable
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        :contextMenuItems="contextMenuItems"
        :contextMenuWidth="contextMenuWidth"
        :onRowContextmenu="handleRowContextmenu as any"
        :onContextMenuSelect="handleContextMenuSelect"
        row-key="id"
        @selection-change="handleSelectionChange"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <!-- 报修详情抽屉 -->
    <RepairDrawer
      :visible="drawerVisible"
      :repair-id="currentRow?.id || null"
      :repair-data="currentRow"
      @update:visible="drawerVisible = $event"
      @refresh="handleRefresh"
    />
  </div>
</template>

<script setup lang="ts">
  import { computed, ref, h } from 'vue'
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchGetRepairPage,
    fetchDeleteRepair,
    fetchBatchDeleteRepair
  } from '@/api/repair-manage'
  import { ElMessageBox, ElTag, ElPopover, ElRate } from 'element-plus'
  import RepairSearch from './modules/repair-search.vue'
  import RepairDrawer from './modules/repair-drawer.vue'
  import StudentInfoPopover from '@/components/core/cards/art-student-info-popover/index.vue'

  defineOptions({ name: 'RepairList' })

  type RepairListItem = Api.RepairManage.RepairListItem

  // 状态管理
  const showSearchBar = ref(false)
  const selectedRows = ref<RepairListItem[]>([])
  const selectedIds = computed(() => selectedRows.value.map((item) => item.id))
  const selectedCount = computed(() => selectedRows.value.length)
  const drawerVisible = ref(false)
  const currentRow = ref<RepairListItem | null>(null)

  // 搜索表单
  const searchForm = ref({
    pageNum: 1,
    studentNo: undefined as string | undefined,
    studentName: undefined as string | undefined,
    repairType: undefined as number | undefined,
    status: undefined as number | undefined,
    urgentLevel: undefined as number | undefined,
    createDateStart: undefined as string | undefined,
    createDateEnd: undefined as string | undefined
  })

  // 使用 useTable 管理表格数据
  const {
    columns,
    columnChecks,
    data,
    loading,
    pagination,
    getData,
    searchParams,
    resetSearchParams,
    handleSizeChange,
    handleCurrentChange,
    refreshData,
    refreshRemove,
    contextMenuItems,
    contextMenuWidth,
    handleRowContextmenu,
    handleContextMenuSelect
  } = useTable<typeof fetchGetRepairPage>({
    core: {
      apiFn: fetchGetRepairPage,
      apiParams: computed(() => ({
        pageNum: searchForm.value.pageNum,
        studentNo: searchForm.value.studentNo || undefined,
        studentName: searchForm.value.studentName || undefined,
        repairType: searchForm.value.repairType,
        status: searchForm.value.status,
        urgentLevel: searchForm.value.urgentLevel,
        startTime: searchForm.value.createDateStart || undefined,
        endTime: searchForm.value.createDateEnd || undefined
      })),
      paginationKey: {
        current: 'pageNum',
        size: 'pageSize'
      },
      columnsFactory: () => [
        { type: 'selection', width: 50 },
        { prop: 'id', label: '编号', width: 80 },
        {
          prop: 'studentInfo.studentNo',
          label: '学号',
          width: 120,
          formatter: (row: RepairListItem) => row.studentInfo?.studentNo ?? '--'
        },
        {
          prop: 'studentInfo.studentName',
          label: '学生姓名',
          minWidth: 100,
          formatter: (row: RepairListItem) => {
            const name = row.studentInfo?.studentName
            if (!name) {
              return h('span', name ?? '--')
            }
            return h(
              ElPopover,
              {
                placement: 'bottom-start',
                trigger: 'hover',
                width: 320,
                popperClass: 'student-info-popover'
              },
              {
                default: () => h(StudentInfoPopover, { student: row.studentInfo ?? {} }),
                reference: () =>
                  h(
                    'span',
                    {
                      class: 'cursor-pointer hover:underline',
                      style: { color: 'var(--el-color-primary)' }
                    },
                    name
                  )
              }
            )
          }
        },
        { prop: 'roomCode', label: '房间号', minWidth: 100 },
        { prop: 'bedCode', label: '床位号', minWidth: 100 },
        { prop: 'repairTypeText', label: '报修类型', width: 100 },
        {
          prop: 'urgentLevelText',
          label: '紧急程度',
          width: 120,
          formatter: (row: RepairListItem) => {
            return h(ElRate, {
              modelValue: row.urgentLevel || 0,
              disabled: true,
              showText: false,
              max: 3,
              size: 'small'
            })
          }
        },
        {
          prop: 'statusText',
          label: '状态',
          width: 100,
          formatter: (row: RepairListItem) => {
            const status = row.status
            const colors: Record<number, 'info' | 'primary' | 'success' | 'warning' | 'danger'> = {
              1: 'info',
              2: 'primary',
              3: 'primary',
              4: 'success',
              5: 'info'
            }
            const color = colors[status] || 'info'
            return h(ElTag, { type: color, size: 'small' }, () => row.statusText || '--')
          }
        },
        { prop: 'createTime', label: '报修时间', width: 180, sortable: true },
        {
          prop: 'action',
          label: '操作',
          width: 120,
          fixed: 'right',
          formatter: (row: RepairListItem) => {
            return [
              { type: 'view', label: '查看', onClick: () => handleView(row) },
              {
                type: 'delete',
                label: '删除',
                onClick: () => handleDelete(row),
                danger: true,
                auth: 'system:repair:delete'
              }
            ]
          }
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
   * 搜索处理
   */
  const handleSearch = (params: Record<string, unknown>) => {
    Object.assign(searchParams, params, { pageNum: 1 })
    getData()
  }

  /**
   * 重置搜索
   */
  const handleReset = () => {
    resetSearchParams()
  }

  // 刷新数据
  const handleRefresh = () => {
    refreshData()
  }

  // 查看
  const handleView = (row: RepairListItem) => {
    currentRow.value = row
    drawerVisible.value = true
  }

  // 删除
  const handleDelete = async (row: RepairListItem) => {
    try {
      await ElMessageBox.confirm('确定要删除这条报修记录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await fetchDeleteRepair(row.id)
      await refreshRemove()
    } catch {
      // 用户取消或删除失败
    }
  }

  // 批量删除
  const handleBatchDelete = async () => {
    if (selectedCount.value === 0) {
      return
    }
    try {
      await ElMessageBox.confirm(
        `确定要批量删除选中的 ${selectedCount.value} 条报修记录吗？`,
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      await fetchBatchDeleteRepair(selectedIds.value)
      selectedRows.value = []
      await refreshRemove()
    } catch {
      // 用户取消或删除失败
    }
  }

  // 选择变化
  const handleSelectionChange = (rows: RepairListItem[]) => {
    selectedRows.value = rows
  }
</script>

<style lang="scss" scoped>
  .repair-page {
    padding: 0;
  }
</style>
