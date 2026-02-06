<template>
  <div class="task-page art-full-height">
    <!-- 搜索栏 -->
    <TaskSearch
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
            <ElButton @click="handleCreate" v-ripple>创建任务</ElButton>
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
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <!-- 进度弹窗 -->
    <TaskProgressDialog
      v-model:visible="progressVisible"
      :task-id="currentTaskId"
      @completed="handleProgressCompleted"
    />

    <!-- 创建任务弹窗 -->
    <TaskCreateDialog
      v-model:visible="createDialogVisible"
      @submit="handleCreateSubmit"
    />
  </div>
</template>

<script setup lang="ts">
  import { useRouter } from 'vue-router'
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchGetTaskPage,
    fetchExecuteTask,
    fetchCancelTask
  } from '@/api/allocation-manage'
  import TaskSearch from './modules/task-search.vue'
  import TaskProgressDialog from './modules/task-progress-dialog.vue'
  import TaskCreateDialog from './modules/task-create-dialog.vue'
  import { ElMessageBox, ElTag, ElProgress } from 'element-plus'
  import type { ActionButtonConfig } from '@/types/component'

  defineOptions({ name: 'AllocationTask' })

  type TaskListItem = Api.Allocation.TaskListItem

  const router = useRouter()

  const showSearchBar = ref(false)
  const progressVisible = ref(false)
  const createDialogVisible = ref(false)
  const currentTaskId = ref<number | undefined>(undefined)

  // 任务类型映射
  const taskTypeMap: Record<number, string> = {
    1: '批量分配',
    2: '单个推荐',
    3: '调宿优化'
  }

  // 状态映射
  const statusMap: Record<number, { label: string; type: 'info' | 'warning' | 'success' | 'primary' | 'danger' }> = {
    0: { label: '待执行', type: 'info' },
    1: { label: '执行中', type: 'warning' },
    2: { label: '已完成', type: 'success' },
    3: { label: '部分确认', type: 'primary' },
    4: { label: '全部确认', type: 'success' },
    5: { label: '已取消', type: 'danger' }
  }

  // 搜索表单
  const searchForm = ref<Api.Allocation.TaskSearchParams>({
    pageNum: 1,
    taskName: undefined,
    taskType: undefined,
    status: undefined
  })

  // 前置声明函数
  let handleExecute: (row: TaskListItem) => Promise<void>
  let handleCancel: (row: TaskListItem) => Promise<void>
  let handleViewProgress: (row: TaskListItem) => void
  let handleViewResult: (row: TaskListItem) => void

  /**
   * 获取操作配置
   */
  const getRowActions = (row: TaskListItem): ActionButtonConfig[] => {
    const actions: ActionButtonConfig[] = []

    if (row.status === 0) {
      // 待执行
      actions.push({
        type: 'play',
        label: '执行',
        onClick: () => handleExecute(row),
        auth: 'allocation:task:execute'
      })
      actions.push({
        type: 'delete',
        label: '取消',
        onClick: () => handleCancel(row),
        danger: true,
        auth: 'allocation:task:cancel'
      })
    } else if (row.status === 1) {
      // 执行中
      actions.push({
        type: 'view',
        label: '查看进度',
        onClick: () => handleViewProgress(row),
        auth: 'allocation:task:view'
      })
    } else {
      // 已完成/部分确认/全部确认
      actions.push({
        type: 'view',
        label: '查看结果',
        onClick: () => handleViewResult(row),
        auth: 'allocation:result:view'
      })
    }

    return actions
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
  } = useTable<typeof fetchGetTaskPage>({
    core: {
      apiFn: fetchGetTaskPage,
      apiParams: computed(() => ({
        pageNum: searchForm.value.pageNum,
        taskName: searchForm.value.taskName || undefined,
        taskType: searchForm.value.taskType,
        status: searchForm.value.status
      })),
      paginationKey: { current: 'pageNum', size: 'pageSize' },
      columnsFactory: () => [
        { prop: 'taskName', label: '任务名称', minWidth: 150 },
        {
          prop: 'taskType',
          label: '类型',
          width: 100,
          formatter: (row: TaskListItem) => taskTypeMap[row.taskType] || '未知'
        },
        { prop: 'configName', label: '使用配置', width: 120 },
        {
          prop: 'scope',
          label: '分配范围',
          minWidth: 150,
          formatter: (row: TaskListItem) => {
            const parts: string[] = []
            if (row.targetEnrollmentYear) parts.push(`${row.targetEnrollmentYear}级`)
            if (row.targetGender) parts.push(row.targetGender === 'male' ? '男' : '女')
            if (row.targetCampusName) parts.push(row.targetCampusName)
            return parts.length > 0 ? parts.join(' · ') : '--'
          }
        },
        {
          prop: 'progress',
          label: '进度',
          width: 180,
          formatter: (row: TaskListItem) => {
            if (row.status < 2) return h('span', { class: 'text-gray-400' }, '--')

            const percent = row.totalStudents ? Math.round((row.allocatedCount * 100) / row.totalStudents) : 0
            return h('div', [
              h('span', { class: 'text-sm' }, `${row.allocatedCount}/${row.totalStudents}`),
              h(ElProgress, {
                percentage: percent,
                strokeWidth: 6,
                class: 'mt-1'
              })
            ])
          }
        },
        {
          prop: 'avgMatchScore',
          label: '平均匹配分',
          width: 100,
          formatter: (row: TaskListItem) => {
            if (!row.avgMatchScore) return h('span', { class: 'text-gray-400' }, '--')

            const type = row.avgMatchScore >= 70 ? 'success' : row.avgMatchScore >= 50 ? 'warning' : 'danger'
            return h(ElTag, { type }, () => row.avgMatchScore)
          }
        },
        {
          prop: 'status',
          label: '状态',
          width: 100,
          formatter: (row: TaskListItem) => {
            const statusInfo = statusMap[row.status] || { label: '未知', type: 'info' as const }
            return h(ElTag, { type: statusInfo.type }, () => statusInfo.label)
          }
        },
        { prop: 'createTime', label: '创建时间', width: 170, sortable: true },
        {
          prop: 'action',
          label: '操作',
          width: 150,
          fixed: 'right',
          formatter: (row: TaskListItem) => getRowActions(row)
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

  // 搜索
  const handleSearch = async () => {
    searchForm.value.pageNum = 1
    await getData()
  }

  // 重置
  const handleReset = async () => {
    searchForm.value = {
      pageNum: 1,
      taskName: undefined,
      taskType: undefined,
      status: undefined
    }
    await resetSearchParams()
  }

  // 创建任务
  const handleCreate = () => {
    createDialogVisible.value = true
  }

  // 创建任务提交回调
  const handleCreateSubmit = async () => {
    await refreshData()
  }

  // 执行任务
  handleExecute = async (row: TaskListItem) => {
    try {
      await ElMessageBox.confirm('确定要执行该任务吗？', '提示', { type: 'warning' })
      await fetchExecuteTask(row.id)

      // 打开进度弹窗
      currentTaskId.value = row.id
      progressVisible.value = true
    } catch (error) {
      if (error !== 'cancel') {
        console.error('执行任务失败:', error)
      }
    }
  }

  // 查看进度
  handleViewProgress = (row: TaskListItem) => {
    currentTaskId.value = row.id
    progressVisible.value = true
  }

  // 查看结果
  handleViewResult = (row: TaskListItem) => {
    router.push(`/allocation/result?taskId=${row.id}`)
  }

  // 取消任务
  handleCancel = async (row: TaskListItem) => {
    try {
      await ElMessageBox.confirm('确定要取消该任务吗？', '提示', { type: 'warning' })
      await fetchCancelTask(row.id)
      await refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('取消任务失败:', error)
      }
    }
  }

  // 进度完成回调
  const handleProgressCompleted = async () => {
    await refreshData()
  }
</script>
