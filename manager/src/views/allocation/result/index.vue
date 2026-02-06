<template>
  <div class="result-page art-full-height">
    <!-- 无任务提示 -->
    <ElEmpty v-if="!taskId" description="请从任务列表选择一个任务查看分配结果" class="h-full">
      <ElButton type="primary" @click="goToTaskList">前往任务列表</ElButton>
    </ElEmpty>

    <template v-else>
      <!-- 任务摘要 -->
      <ElCard shadow="never" class="mb-3" v-if="taskInfo.id">
      <div class="flex justify-between items-center">
        <div>
          <span class="text-lg font-bold">{{ taskInfo.taskName }}</span>
          <ElTag class="ml-2">{{ taskInfo.statusName }}</ElTag>
        </div>
        <div class="text-gray-500 text-sm">
          <span>总学生: {{ taskInfo.totalStudents }}</span>
          <span class="ml-4">已分配: {{ taskInfo.allocatedCount }}</span>
          <span class="ml-4">已确认: {{ taskInfo.confirmedCount }}</span>
          <span class="ml-4">平均分: {{ taskInfo.avgMatchScore }}</span>
        </div>
      </div>
    </ElCard>

    <!-- 搜索栏 -->
    <ResultSearch
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
        v-model:columns="columnChecks"
        v-model:showSearchBar="showSearchBar"
        :loading="loading"
        @refresh="refreshData"
      >
        <template #left>
          <ElSpace wrap>
            <ElButton
              :disabled="selectedCount === 0"
              @click="handleConfirmSelected"
            >
              确认选中 ({{ selectedCount }})
            </ElButton>
            <ElButton @click="handleConfirmAll">确认全部</ElButton>
            <ElButton
              :disabled="selectedCount === 0"
              @click="handleRejectSelected"
            >
              拒绝选中
            </ElButton>
            <ElButton @click="handleProblemList">问题清单</ElButton>
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
      />
    </ElCard>

    <!-- 详情抽屉 -->
    <ResultDrawer v-model:visible="drawerVisible" :result-data="currentResult" />
    </template>
  </div>
</template>

<script setup lang="ts">
  import { useRoute } from 'vue-router'
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchGetResultPage,
    fetchGetTaskDetail,
    fetchConfirmResults,
    fetchConfirmAllResults,
    fetchRejectResults
  } from '@/api/allocation-manage'
  import ResultSearch from './modules/result-search.vue'
  import ResultDrawer from './modules/result-drawer.vue'
  import { ElMessageBox, ElTag, ElEmpty } from 'element-plus'
  import type { ActionButtonConfig } from '@/types/component'

  defineOptions({ name: 'AllocationResult' })

  type ResultListItem = Api.Allocation.ResultListItem

  const route = useRoute()
  const router = useRouter()
  const taskId = computed(() => {
    const id = Number(route.query.taskId)
    return isNaN(id) ? undefined : id
  })

  const showSearchBar = ref(false)
  const drawerVisible = ref(false)
  const currentResult = ref<ResultListItem | null>(null)

  // 任务信息
  const taskInfo = ref<Partial<Api.Allocation.TaskListItem>>({})

  // 选中行
  const selectedRows = ref<ResultListItem[]>([])
  const selectedCount = computed(() => selectedRows.value.length)

  // 状态映射
  const statusMap: Record<number, { label: string; type: 'warning' | 'success' | 'danger' | 'info' }> = {
    0: { label: '待确认', type: 'warning' },
    1: { label: '已确认', type: 'success' },
    2: { label: '已拒绝', type: 'danger' },
    3: { label: '已调整', type: 'info' }
  }

  // 搜索表单
  const searchForm = ref<Api.Allocation.ResultSearchParams>({
    pageNum: 1,
    taskId: undefined,
    studentNo: undefined,
    studentName: undefined,
    roomCode: undefined,
    status: undefined,
    problemOnly: false
  })

  // 前置声明函数
  let handleViewDetail: (row: ResultListItem) => void

  /**
   * 获取操作配置
   */
  const getRowActions = (row: ResultListItem): ActionButtonConfig[] => [
    {
      type: 'view',
      label: '详情',
      onClick: () => handleViewDetail(row),
      auth: 'allocation:result:view'
    }
  ]

  // 匹配分颜色
  const getScoreType = (score: number) => {
    if (score >= 80) return 'success'
    if (score >= 60) return 'warning'
    return 'danger'
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
    contextMenuItems,
    contextMenuWidth,
    handleRowContextmenu,
    handleContextMenuSelect
  } = useTable<typeof fetchGetResultPage>({
    core: {
      apiFn: fetchGetResultPage,
      apiParams: computed(() => ({
        pageNum: searchForm.value.pageNum,
        taskId: taskId.value,
        studentNo: searchForm.value.studentNo || undefined,
        studentName: searchForm.value.studentName || undefined,
        roomCode: searchForm.value.roomCode || undefined,
        status: searchForm.value.status,
        problemOnly: searchForm.value.problemOnly
      })),
      paginationKey: { current: 'pageNum', size: 'pageSize' },
      immediate: false,
      columnsFactory: () => [
        { type: 'selection', width: 50 },
        { prop: 'studentNo', label: '学号', width: 120 },
        { prop: 'studentName', label: '姓名', width: 100 },
        {
          prop: 'room',
          label: '分配房间',
          width: 150,
          formatter: (row: ResultListItem) =>
            `${row.allocatedFloorCode || ''} - ${row.allocatedRoomCode || ''}`
        },
        {
          prop: 'matchScore',
          label: '匹配分',
          width: 100,
          formatter: (row: ResultListItem) => {
            return h(ElTag, { type: getScoreType(row.matchScore) }, () => row.matchScore)
          }
        },
        { prop: 'matchScoreLevel', label: '等级', width: 80 },
        {
          prop: 'roommateNames',
          label: '室友',
          minWidth: 150,
          formatter: (row: ResultListItem) => {
            if (row.roommateNames && row.roommateNames.length) {
              return row.roommateNames.join('、')
            }
            return h('span', { class: 'text-gray-400' }, '无')
          }
        },
        {
          prop: 'conflictReasons',
          label: '冲突',
          minWidth: 150,
          formatter: (row: ResultListItem) => {
            if (row.conflictReasons && row.conflictReasons.length) {
              const tags = row.conflictReasons.slice(0, 2).map((c, i) =>
                h(ElTag, { type: 'danger', size: 'small', class: 'mr-1', key: i }, () => c)
              )
              if (row.conflictReasons.length > 2) {
                tags.push(
                  h('span', { class: 'text-gray-400' }, `+${row.conflictReasons.length - 2}`)
                )
              }
              return tags
            }
            return h('span', { class: 'text-gray-400' }, '无')
          }
        },
        {
          prop: 'status',
          label: '状态',
          width: 90,
          formatter: (row: ResultListItem) => {
            const statusInfo = statusMap[row.status] || { label: '未知', type: 'info' as const }
            return h(ElTag, { type: statusInfo.type }, () => statusInfo.label)
          }
        },
        {
          prop: 'action',
          label: '操作',
          width: 100,
          fixed: 'right',
          formatter: (row: ResultListItem) => getRowActions(row)
        }
      ]
    },
    adaptive: {
      enabled: true
    },
    contextMenu: {
      enabled: true,
      actionsGetter: getRowActions
    }
  })

  // 加载任务信息
  const loadTaskInfo = async () => {
    const id = taskId.value
    if (!id) return
    try {
      const data = await fetchGetTaskDetail(id)
      taskInfo.value = data
    } catch (error) {
      console.error('获取任务信息失败:', error)
    }
  }

  // 跳转到任务列表
  const goToTaskList = () => {
    router.push('/allocation/task')
  }

  // 搜索
  const handleSearch = async () => {
    searchForm.value.pageNum = 1
    await getData()
  }

  // 重置
  const handleReset = async () => {
    searchForm.value = {
      pageNum: 1,
      taskId: taskId.value,
      studentNo: undefined,
      studentName: undefined,
      roomCode: undefined,
      status: undefined,
      problemOnly: false
    }
    await resetSearchParams()
  }

  // 查看问题清单
  const handleProblemList = async () => {
    searchForm.value.problemOnly = true
    searchForm.value.pageNum = 1
    await getData()
  }

  // 查看详情
  handleViewDetail = (row: ResultListItem) => {
    currentResult.value = row
    drawerVisible.value = true
  }

  // 选择变化
  const handleSelectionChange = (rows: ResultListItem[]) => {
    selectedRows.value = rows
  }

  // 确认选中
  const handleConfirmSelected = async () => {
    const id = taskId.value
    if (!id) {
      ElMessage.error('任务ID不存在')
      return
    }
    if (selectedRows.value.length === 0) {
      ElMessage.warning('请选择要确认的记录')
      return
    }
    try {
      await ElMessageBox.confirm(
        `确定要确认选中的 ${selectedRows.value.length} 条记录吗？`,
        '提示'
      )
      const ids = selectedRows.value.map((r) => r.id)
      await fetchConfirmResults(id, ids)
      await refreshData()
      await loadTaskInfo()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('确认失败:', error)
      }
    }
  }

  // 确认全部
  const handleConfirmAll = async () => {
    const id = taskId.value
    if (!id) {
      ElMessage.error('任务ID不存在')
      return
    }
    try {
      await ElMessageBox.confirm('确定要确认全部待确认的记录吗？', '提示', { type: 'warning' })
      await fetchConfirmAllResults(id)
      await refreshData()
      await loadTaskInfo()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('确认全部失败:', error)
      }
    }
  }

  // 拒绝选中
  const handleRejectSelected = async () => {
    const id = taskId.value
    if (!id) {
      ElMessage.error('任务ID不存在')
      return
    }
    if (selectedRows.value.length === 0) {
      ElMessage.warning('请选择要拒绝的记录')
      return
    }
    try {
      const { value } = await ElMessageBox.prompt('请输入拒绝原因（可选）', '拒绝', {
        inputPlaceholder: '请输入原因'
      })
      const ids = selectedRows.value.map((r) => r.id)
      await fetchRejectResults(id, ids, value)
      await refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('拒绝失败:', error)
      }
    }
  }

  onMounted(() => {
    if (taskId.value) {
      searchForm.value.taskId = taskId.value
      loadTaskInfo()
      getData()
    }
  })
</script>
