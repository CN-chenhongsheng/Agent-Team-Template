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
      ref="progressDialogRef"
      v-model:visible="progressVisible"
      :task-id="currentTaskId"
      @completed="handleProgressCompleted"
      @minimize="handleMinimize"
    />

    <!-- 创建任务弹窗 -->
    <TaskCreateDialog
      v-model:visible="createDialogVisible"
      :task="editingTask"
      @submit="handleCreateSubmit"
    />
  </div>
</template>

<script setup lang="ts">
  import { useRouter } from 'vue-router'
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchGetTaskPage,
    fetchGetTaskDetail,
    fetchExecuteTask,
    fetchCancelTask
  } from '@/api/allocation-manage'
  import TaskSearch from './modules/task-search.vue'
  import TaskProgressDialog from './modules/task-progress-dialog.vue'
  import TaskCreateDialog from './modules/task-create-dialog.vue'
  import type { ActionButtonConfig } from '@/types/component'

  defineOptions({ name: 'AllocationTask' })

  type TaskListItem = Api.Allocation.TaskListItem

  const router = useRouter()

  const showSearchBar = ref(false)
  const progressVisible = ref(false)
  const createDialogVisible = ref(false)
  const currentTaskId = ref<number | undefined>(undefined)
  const editingTask = ref<TaskListItem | undefined>(undefined)
  const progressDialogRef = ref<InstanceType<typeof TaskProgressDialog>>()

  // ========== 进度通知栏 ==========
  let progressNotification: ReturnType<typeof ElNotification> | null = null
  let notificationTimer: ReturnType<typeof setInterval> | null = null

  /** 最小化进度弹窗 -> 显示通知栏 */
  const handleMinimize = () => {
    progressVisible.value = false
    showProgressNotification()
  }

  /** 显示进度通知（只创建一次，后续原地更新 DOM） */
  const showProgressNotification = () => {
    closeProgressNotification()

    const pct = progressDialogRef.value?.progressData?.progressPercent || 0
    const stage = progressDialogRef.value?.progressData?.currentStage || '处理中...'

    progressNotification = ElNotification({
      title: '分配任务执行中',
      dangerouslyUseHTMLString: true,
      customClass: 'task-progress-notification',
      message: `
        <div class="tpn-body">
          <div class="tpn-row">
            <span class="tpn-stage" id="__tpn_stage">${stage}</span>
            <span class="tpn-pct" id="__tpn_pct">${pct}%</span>
          </div>
          <div class="tpn-track">
            <div class="tpn-bar" id="__tpn_bar" style="width:${pct}%"></div>
          </div>
          <a id="__tpn_detail_btn" class="tpn-link">查看详情</a>
        </div>
      `,
      duration: 0,
      position: 'bottom-right',
      showClose: false,
      onClose: () => {
        progressNotification = null
      }
    })

    // 绑定"查看详情"点击
    nextTick(() => {
      const btn = document.getElementById('__tpn_detail_btn')
      if (btn) {
        btn.onclick = () => {
          closeProgressNotification()
          progressVisible.value = true
        }
      }
    })

    // 定时原地更新 DOM 内容（不重建通知）
    notificationTimer = setInterval(() => {
      const data = progressDialogRef.value?.progressData
      if (!data) return

      if (data.completed) {
        closeProgressNotification()
        handleProgressCompleted()
        return
      }

      const stageEl = document.getElementById('__tpn_stage')
      const pctEl = document.getElementById('__tpn_pct')
      const barEl = document.getElementById('__tpn_bar')
      if (stageEl) stageEl.textContent = data.currentStage || '处理中...'
      if (pctEl) pctEl.textContent = `${data.progressPercent || 0}%`
      if (barEl) barEl.style.width = `${data.progressPercent || 0}%`
    }, 2000)
  }

  /** 关闭进度通知 */
  const closeProgressNotification = () => {
    if (notificationTimer) {
      clearInterval(notificationTimer)
      notificationTimer = null
    }
    if (progressNotification) {
      progressNotification.close()
      progressNotification = null
    }
  }

  // 任务类型映射
  const taskTypeMap: Record<number, string> = {
    1: '批量分配',
    2: '单个推荐',
    3: '调宿优化'
  }

  // 状态映射
  const statusMap: Record<
    number,
    { label: string; type: 'info' | 'warning' | 'success' | 'primary' | 'danger' }
  > = {
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
  let handleEdit: (row: TaskListItem) => Promise<void>
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
        type: 'edit',
        label: '编辑',
        onClick: () => handleEdit(row),
        auth: 'allocation:task:edit'
      })
      actions.push({
        type: 'play',
        label: '执行',
        onClick: () => handleExecute(row),
        auth: 'allocation:task:execute'
      })
      actions.push({
        type: 'cancel',
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
            if (row.targetGender) {
              const genderMap: Record<string, string> = { '1': '男', '2': '女', '3': '混合', male: '男', female: '女' }
              parts.push(genderMap[row.targetGender] || row.targetGender)
            }
            if (row.targetCampusCode) parts.push(row.targetCampusCode)
            return parts.length > 0 ? parts.join(' · ') : '--'
          }
        },
        {
          prop: 'progress',
          label: '进度',
          width: 160,
          formatter: (row: TaskListItem) => {
            const total = row.totalStudents || 0
            const done = row.allocatedCount || 0

            // 未执行或无数据
            if (row.status < 2 || total === 0) {
              return h('span', { class: 'text-gray-400 text-sm' }, '--')
            }

            const percent = Math.round((done * 100) / total)
            const color = percent >= 100 ? '#10b981' : percent >= 50 ? '#3b82f6' : '#f59e0b'

            return h(
              'div',
              { style: 'display:flex;align-items:center;gap:8px' },
              [
                // 迷你进度条（竖向填充块）
                h(
                  'div',
                  {
                    style:
                      'position:relative;width:4px;height:28px;border-radius:2px;background:var(--el-fill-color-light);overflow:hidden;flex-shrink:0'
                  },
                  [
                    h('div', {
                      style: `position:absolute;bottom:0;width:100%;height:${percent}%;background:${color};border-radius:2px;transition:height 0.3s`
                    })
                  ]
                ),
                // 数字和标签
                h('div', { style: 'display:flex;flex-direction:column;gap:0' }, [
                  h(
                    'span',
                    { style: `font-size:13px;font-weight:600;color:${color};line-height:1.4` },
                    `${done} / ${total}`
                  ),
                  h(
                    'span',
                    { style: 'font-size:11px;color:var(--el-text-color-placeholder);line-height:1.3' },
                    `${percent}%${percent >= 100 ? ' 已完成' : ''}`
                  )
                ])
              ]
            )
          }
        },
        {
          prop: 'avgMatchScore',
          label: '平均匹配分',
          width: 100,
          formatter: (row: TaskListItem) => {
            if (!row.avgMatchScore) return h('span', { class: 'text-gray-400' }, '--')

            const type =
              row.avgMatchScore >= 70 ? 'success' : row.avgMatchScore >= 50 ? 'warning' : 'danger'
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
          width: 180,
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
    editingTask.value = undefined
    createDialogVisible.value = true
  }

  // 编辑任务
  handleEdit = async (row: TaskListItem) => {
    try {
      const detail = await fetchGetTaskDetail(row.id)
      editingTask.value = detail || row
      createDialogVisible.value = true
    } catch {
      editingTask.value = row
      createDialogVisible.value = true
    }
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
    closeProgressNotification()
    await refreshData()
  }

  // 页面卸载时清理
  onUnmounted(() => {
    closeProgressNotification()
  })
</script>

<!-- 通知栏样式必须全局，因为 ElNotification 挂载在 body -->
<style lang="scss">
  .task-progress-notification {
    // 使用系统圆角变量
    border-radius: var(--el-border-radius-base, 8px) !important;
    overflow: hidden;
    box-shadow: var(--el-box-shadow-light) !important;

    .tpn-body {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .tpn-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
    }

    .tpn-stage {
      font-size: 13px;
      color: var(--el-text-color-regular);
    }

    .tpn-pct {
      font-size: 13px;
      font-weight: 600;
      color: var(--el-color-primary);
    }

    .tpn-track {
      height: 4px;
      border-radius: 2px;
      background: var(--el-fill-color-light);
      overflow: hidden;
    }

    .tpn-bar {
      height: 100%;
      border-radius: 2px;
      background: var(--el-color-primary);
      transition: width 0.4s ease;
    }

    .tpn-link {
      font-size: 12px;
      color: var(--el-color-primary);
      cursor: pointer;
      text-align: right;
      text-decoration: none;

      &:hover {
        text-decoration: underline;
      }
    }
  }
</style>
