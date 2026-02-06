<!-- 待办审批页面 -->
<template>
  <div class="art-full-height">
    <!-- 搜索栏 -->
    <PendingSearch
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
      <ArtTableHeader
        v-model:columns="columnChecks"
        v-model:showSearchBar="showSearchBar"
        :loading="loading"
        @refresh="refreshData"
      >
        <template #left>
          <ElSpace wrap>
            <ElButton :disabled="selectedCount === 0" @click="handleBatchApprove" v-ripple>
              批量审核{{ selectedCount > 0 ? `(${selectedCount})` : '' }}
            </ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <!-- 表格 -->
      <ArtTable
        :contextMenuItems="contextMenuItems"
        :contextMenuWidth="contextMenuWidth"
        :onRowContextmenu="handleRowContextmenu as any"
        :onContextMenuSelect="handleContextMenuSelect"
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        row-key="id"
        @selection-change="handleSelectionChange"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <!-- 审批抽屉 -->
    <ApprovalDrawer
      v-model:visible="approvalDialogVisible"
      :instance-data="currentInstance"
      @success="handleApprovalSuccess"
    />

    <!-- 批量审核对话框 -->
    <BatchApprovalDialog
      v-model:visible="batchApprovalDialogVisible"
      :selected-instances="selectedRows"
      @success="handleBatchApprovalSuccess"
    />
  </div>
</template>

<script setup lang="ts">
  import { useTable } from '@/hooks/core/useTable'
  import { fetchGetPendingList, type ApprovalInstance } from '@/api/approval-manage'
  import ApprovalDrawer from './modules/approval-drawer.vue'
  import BatchApprovalDialog from './modules/batch-approval-dialog.vue'
  import PendingSearch from './modules/pending-search.vue'
  import { ElTag, ElMessage, ElPopover } from 'element-plus'
  import StudentInfoPopover from '@/components/core/cards/art-student-info-popover/index.vue'
  import { enrichStudentInfo } from '@/utils/student/enrichStudentInfo'

  defineOptions({ name: 'ApprovalPending' })

  // 搜索相关
  const formFilters = ref({
    pageNum: 1,
    businessType: undefined,
    applicantName: undefined
  })

  const showSearchBar = ref(false)
  const approvalDialogVisible = ref(false)
  const currentInstance = ref<ApprovalInstance | null>(null)
  const batchApprovalDialogVisible = ref(false)

  // 选中行
  const selectedRows = ref<ApprovalInstance[]>([])
  const selectedCount = computed(() => selectedRows.value.length)

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
    contextMenuItems,
    contextMenuWidth,
    handleRowContextmenu,
    handleContextMenuSelect
  } = useTable<typeof fetchGetPendingList>({
    core: {
      apiFn: fetchGetPendingList,
      apiParams: computed(() => ({
        pageNum: formFilters.value.pageNum,
        businessType: formFilters.value.businessType || undefined,
        applicantName: formFilters.value.applicantName || undefined
      })),
      paginationKey: {
        current: 'pageNum',
        size: 'pageSize'
      },
      columnsFactory: () => [
        { type: 'selection' },
        {
          prop: 'businessType',
          label: '业务类型',
          width: 120,
          formatter: (row: ApprovalInstance) => {
            return h(ElTag, { type: 'primary', size: 'small' }, () => row.businessTypeText)
          }
        },
        {
          prop: 'applicantName',
          label: '申请人',
          width: 120,
          formatter: (row: ApprovalInstance) => {
            // 只有当有申请人姓名且是学生相关业务时才显示悬浮卡片
            if (!row.applicantName) {
              return h('span', row.applicantName || '--')
            }
            // 判断是否为学生相关业务
            const isStudentBusiness = ['check_in', 'check_out', 'transfer', 'stay'].includes(
              row.businessType
            )
            if (!isStudentBusiness || !row.studentInfo) {
              // 非学生业务或没有学生信息，直接显示姓名
              return h('span', row.applicantName)
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
                default: () =>
                  h(StudentInfoPopover, {
                    student: row.studentInfo ?? { studentName: row.applicantName }
                  }),
                reference: () =>
                  h(
                    'span',
                    {
                      class: 'cursor-pointer hover:underline',
                      style: { color: 'var(--el-color-primary)' }
                    },
                    row.applicantName
                  )
              }
            )
          }
        },
        {
          prop: 'flowName',
          label: '审批流程',
          minWidth: 150
        },
        {
          prop: 'currentNodeName',
          label: '当前节点',
          width: 120,
          formatter: (row: ApprovalInstance) => {
            return h(ElTag, { type: 'warning', size: 'small' }, () => row.currentNodeName)
          }
        },
        {
          prop: 'startTime',
          label: '提交时间',
          width: 180,
          sortable: true
        },
        {
          prop: 'action',
          label: '操作',
          width: 120,
          fixed: 'right',
          formatter: (row: ApprovalInstance) => {
            return [{ type: 'view', onClick: () => openApprovalDialog(row), label: '查看' }]
          }
        }
      ],
      immediate: true
    },
    hooks: {
      onSuccess: async (tableData) => {
        // 补齐学生信息（工具函数会自动判断是否需要补齐）
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

  const handleSearch = async (): Promise<void> => {
    formFilters.value.pageNum = 1
    await getData()
  }

  const handleReset = async (): Promise<void> => {
    formFilters.value = {
      pageNum: 1,
      businessType: undefined,
      applicantName: undefined
    }
    await resetSearchParams()
  }

  const openApprovalDialog = (row: ApprovalInstance) => {
    currentInstance.value = row
    approvalDialogVisible.value = true
  }

  const handleApprovalSuccess = async () => {
    await refreshData()
  }

  // 处理表格行选择变化
  const handleSelectionChange = (selection: ApprovalInstance[]): void => {
    selectedRows.value = selection
  }

  // 处理批量审核
  const handleBatchApprove = (): void => {
    if (selectedCount.value === 0) {
      ElMessage.warning('请先选择要审核的审批项')
      return
    }
    batchApprovalDialogVisible.value = true
  }

  // 处理批量审核成功
  const handleBatchApprovalSuccess = async () => {
    selectedRows.value = []
    await refreshData()
  }
</script>
