<!-- 调宿管理页面 -->
<template>
  <div class="transfer-page art-full-height">
    <!-- 搜索栏 -->
    <TransferSearch
      v-show="showSearchBar"
      v-model="searchForm"
      @search="handleSearch"
      @reset="handleReset"
    ></TransferSearch>

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
            <ElButton @click="handleAdd" v-ripple v-permission="'system:transfer:add'"
              >新增调宿</ElButton
            >
            <ElButton
              :disabled="selectedCount === 0"
              @click="handleBatchDelete"
              v-ripple
              v-permission="'system:transfer:delete'"
            >
              批量删除{{ selectedCount > 0 ? `(${selectedCount})` : '' }}
            </ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <ArtTable
        :loading="loading"
        :columns="columns"
        :data="data"
        :pagination="pagination"
        :contextMenuItems="contextMenuItems"
        :contextMenuWidth="contextMenuWidth"
        :onRowContextmenu="handleRowContextmenu as any"
        :onContextMenuSelect="handleContextMenuSelect"
        :stripe="false"
        row-key="id"
        @selection-change="handleSelectionChange"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <!-- 申请详情抽屉 -->
    <TransferDrawer
      :visible="drawerVisible"
      :transfer-id="currentRow?.id || null"
      :transfer-data="currentRow"
      @update:visible="drawerVisible = $event"
      @approval-success="handleRefresh"
    />
  </div>
</template>

<script setup lang="ts">
  import { computed, ref, h } from 'vue'
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchGetTransferPage,
    fetchDeleteTransfer,
    fetchBatchDeleteTransfer
  } from '@/api/accommodation-manage'
  import { ElPopover } from 'element-plus'
  import TransferSearch from './modules/transfer-search.vue'
  import TransferDrawer from './modules/transfer-drawer.vue'
  import ApprovalProgressTag from '@/components/core/approval/approval-progress-tag/index.vue'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import StudentInfoPopover from '@/components/core/cards/art-student-info-popover/index.vue'
  import { enrichStudentInfo } from '@/utils/student/enrichStudentInfo'

  defineOptions({ name: 'AccommodationTransfer' })

  type TransferListItem = Api.AccommodationManage.TransferListItem

  // 状态管理
  const showSearchBar = ref(false)
  const selectedRows = ref<TransferListItem[]>([])
  const selectedIds = computed(() => selectedRows.value.map((item) => item.id))
  const selectedCount = computed(() => selectedRows.value.length)
  const drawerVisible = ref(false)
  const currentRow = ref<TransferListItem | null>(null)

  // 搜索表单
  const searchForm = ref<Api.AccommodationManage.TransferSearchParams>({
    pageNum: 1,
    studentNo: undefined,
    studentName: undefined,
    studentId: undefined,
    originalCampusCode: undefined,
    targetCampusCode: undefined,
    status: undefined,
    applyDateStart: undefined,
    applyDateEnd: undefined
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
  } = useTable<typeof fetchGetTransferPage>({
    core: {
      apiFn: fetchGetTransferPage,
      apiParams: computed(() => ({
        pageNum: searchForm.value.pageNum,
        studentNo: searchForm.value.studentNo || undefined,
        studentName: searchForm.value.studentName || undefined,
        studentId: searchForm.value.studentId || undefined,
        originalCampusCode: searchForm.value.originalCampusCode || undefined,
        targetCampusCode: searchForm.value.targetCampusCode || undefined,
        status: searchForm.value.status,
        applyDateStart: searchForm.value.applyDateStart || undefined,
        applyDateEnd: searchForm.value.applyDateEnd || undefined
      })),
      paginationKey: {
        current: 'pageNum',
        size: 'pageSize'
      },
      columnsFactory: () => [
        { type: 'selection', width: 50 },
        {
          prop: 'studentInfo.studentNo',
          label: '学号',
          width: 120,
          formatter: (row: TransferListItem) => row.studentInfo?.studentNo ?? '--'
        },
        {
          prop: 'studentInfo.studentName',
          label: '学生姓名',
          minWidth: 100,
          formatter: (row: TransferListItem) => {
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
        {
          prop: 'studentInfo.genderText',
          label: '性别',
          width: 70,
          formatter: (row: TransferListItem) => {
            const genderIcon = {
              1: 'ri-men-line',
              2: 'ri-women-line'
            }
            const genderText = row.studentInfo?.genderText
            const gender = row.studentInfo?.gender
            return h('div', { class: 'flex items-center gap-1' }, [
              h('span', { class: 'text-g-700 text-sm' }, genderText as string),
              h(ArtSvgIcon, {
                icon: genderIcon[gender as number as keyof typeof genderIcon] as string,
                class: `text-g-700 text-md ${gender === 1 ? 'text-primary' : 'text-pink-500'}`
              })
            ])
          }
        },
        { prop: 'phone', label: '手机号', width: 125 },
        { prop: 'originalCampusName', label: '原校区', width: 120 },
        { prop: 'originalFloorName', label: '原楼层', width: 120 },
        { prop: 'originalRoomCode', label: '原房间', width: 100 },
        { prop: 'originalBedCode', label: '原床位', width: 100 },
        { prop: 'targetCampusName', label: '目标校区', width: 120 },
        { prop: 'targetFloorName', label: '目标楼层', width: 120 },
        { prop: 'targetRoomCode', label: '目标房间', width: 100 },
        { prop: 'targetBedCode', label: '目标床位', width: 100 },
        { prop: 'applyDate', label: '申请日期', width: 120, sortable: true },
        { prop: 'transferDate', label: '调宿日期', width: 120, sortable: true },
        {
          prop: 'approvalProgress',
          label: '审批状态',
          width: 120,
          formatter: (row: TransferListItem) => {
            return h(ApprovalProgressTag, {
              approvalProgress: row.approvalProgress
            })
          }
        },
        {
          prop: 'action',
          label: '操作',
          width: 150,
          fixed: 'right',
          formatter: (row: TransferListItem) => {
            return [
              { type: 'view', label: '查看', onClick: () => handleView(row) },
              {
                type: 'delete',
                label: '删除',
                onClick: () => handleDelete(row),
                danger: true,
                auth: 'system:transfer:delete'
              }
            ]
          }
        }
      ]
    },
    hooks: {
      onSuccess: async (tableData) => {
        // 补齐学生信息
        const enrichedData = await enrichStudentInfo(tableData)
        // 更新数据
        data.value = enrichedData
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
  const handleSearch = (params: Record<string, any>) => {
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

  // 新增
  const handleAdd = () => {
    // TODO: 打开新增对话框
  }

  // 查看
  const handleView = (row: TransferListItem) => {
    currentRow.value = row
    drawerVisible.value = true
  }

  // 删除
  const handleDelete = async (row: TransferListItem) => {
    try {
      await ElMessageBox.confirm(`确定要删除调宿记录吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await fetchDeleteTransfer(row.id)
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
        `确定要批量删除选中的 ${selectedCount.value} 条调宿记录吗？`,
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      await fetchBatchDeleteTransfer(selectedIds.value)
      selectedRows.value = []
      await refreshRemove()
    } catch {
      // 用户取消或删除失败
    }
  }

  // 选择变化
  const handleSelectionChange = (rows: TransferListItem[]) => {
    selectedRows.value = rows
  }
</script>
