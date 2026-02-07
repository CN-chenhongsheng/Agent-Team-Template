<!-- 留宿管理页面 -->
<template>
  <div class="stay-page art-full-height">
    <!-- 搜索栏 -->
    <StaySearch
      v-show="showSearchBar"
      v-model="searchForm"
      @search="handleSearch"
      @reset="handleReset"
    ></StaySearch>

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
            <ElButton @click="handleAdd" v-ripple v-permission="'system:stay:add'"
              >新增留宿</ElButton
            >
            <ElButton
              :disabled="selectedCount === 0"
              @click="handleBatchDelete"
              v-ripple
              v-permission="'system:stay:delete'"
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
    <StayDrawer
      :visible="drawerVisible"
      :stay-id="currentRow?.id || null"
      :stay-data="currentRow"
      @update:visible="drawerVisible = $event"
      @approval-success="handleRefresh"
    />
  </div>
</template>

<script setup lang="ts">
  import { computed, ref, h } from 'vue'
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchGetStayPage,
    fetchDeleteStay,
    fetchBatchDeleteStay
  } from '@/api/accommodation-manage'
  import { ElPopover } from 'element-plus'
  import StaySearch from './modules/stay-search.vue'
  import StayDrawer from './modules/stay-drawer.vue'
  import ApprovalProgressTag from '@/components/core/approval/approval-progress-tag/index.vue'
  import StudentInfoPopover from '@/components/core/cards/art-student-info-popover/index.vue'
  import { enrichStudentInfo } from '@/utils/student/enrichStudentInfo'

  defineOptions({ name: 'AccommodationStay' })

  type StayListItem = Api.AccommodationManage.StayListItem

  // 状态管理
  const showSearchBar = ref(false)
  const selectedRows = ref<StayListItem[]>([])
  const selectedIds = computed(() => selectedRows.value.map((item) => item.id))
  const selectedCount = computed(() => selectedRows.value.length)
  const drawerVisible = ref(false)
  const currentRow = ref<StayListItem | null>(null)

  // 搜索表单
  const searchForm = ref<Api.AccommodationManage.StaySearchParams>({
    pageNum: 1,
    studentNo: undefined,
    studentName: undefined,
    studentId: undefined,
    campusCode: undefined,
    bedId: undefined,
    status: undefined,
    applyDateStart: undefined,
    applyDateEnd: undefined,
    stayStartDateStart: undefined,
    stayStartDateEnd: undefined
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
  } = useTable<typeof fetchGetStayPage>({
    core: {
      apiFn: fetchGetStayPage,
      apiParams: computed(() => ({
        pageNum: searchForm.value.pageNum,
        studentNo: searchForm.value.studentNo || undefined,
        studentName: searchForm.value.studentName || undefined,
        studentId: searchForm.value.studentId || undefined,
        campusCode: searchForm.value.campusCode || undefined,
        bedId: searchForm.value.bedId || undefined,
        status: searchForm.value.status,
        applyDateStart: searchForm.value.applyDateStart || undefined,
        applyDateEnd: searchForm.value.applyDateEnd || undefined,
        stayStartDateStart: searchForm.value.stayStartDateStart || undefined,
        stayStartDateEnd: searchForm.value.stayStartDateEnd || undefined
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
          formatter: (row: StayListItem) => row.studentInfo?.studentNo ?? '--'
        },
        {
          prop: 'studentInfo.studentName',
          label: '学生姓名',
          minWidth: 100,
          formatter: (row: StayListItem) => {
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
          prop: 'studentInfo.campusName',
          label: '校区',
          minWidth: 100,
          showOverflowTooltip: true
        },
        {
          prop: 'studentInfo.deptName',
          label: '院系',
          minWidth: 120,
          showOverflowTooltip: true
        },
        {
          prop: 'studentInfo.majorName',
          label: '专业',
          minWidth: 120,
          showOverflowTooltip: true
        },
        {
          prop: 'studentInfo.className',
          label: '班级',
          minWidth: 100,
          showOverflowTooltip: true
        },
        {
          prop: 'studentInfo.roomName',
          label: '房间',
          width: 120,
          showOverflowTooltip: true
        },
        {
          prop: 'studentInfo.bedName',
          label: '床位',
          width: 120,
          showOverflowTooltip: true
        },
        { prop: 'applyDate', label: '申请日期', width: 120, sortable: true },
        { prop: 'stayStartDate', label: '留宿开始日期', width: 120, sortable: true },
        { prop: 'stayEndDate', label: '留宿结束日期', width: 120, sortable: true },
        { prop: 'stayReason', label: '留宿理由', showOverflowTooltip: true },
        {
          prop: 'approvalProgress',
          label: '审批状态',
          width: 120,
          formatter: (row: StayListItem) => {
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
          formatter: (row: StayListItem) => {
            return [
              { type: 'view', label: '查看', onClick: () => handleView(row) },
              {
                type: 'delete',
                label: '删除',
                onClick: () => handleDelete(row),
                danger: true,
                auth: 'system:stay:delete'
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
  const handleView = (row: StayListItem) => {
    currentRow.value = row
    drawerVisible.value = true
  }

  // 删除
  const handleDelete = async (row: StayListItem) => {
    try {
      await ElMessageBox.confirm(`确定要删除留宿记录吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await fetchDeleteStay(row.id)
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
        `确定要批量删除选中的 ${selectedCount.value} 条留宿记录吗？`,
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      await fetchBatchDeleteStay(selectedIds.value)
      selectedRows.value = []
      await refreshRemove()
    } catch {
      // 用户取消或删除失败
    }
  }

  // 选择变化
  const handleSelectionChange = (rows: StayListItem[]) => {
    selectedRows.value = rows
  }
</script>
