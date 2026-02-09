<template>
  <div class="survey-page art-full-height">
    <!-- 搜索栏 -->
    <SurveySearch
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
            <ElButton @click="handleBatchReminder"> 批量提醒未填写 </ElButton>
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

    <!-- 详情抽屉 -->
    <SurveyDrawer
      v-model:visible="drawerVisible"
      :student-id="currentStudentId"
      :student-name="currentStudentName"
    />
  </div>
</template>

<script setup lang="ts">
  import { useTable } from '@/hooks/core/useTable'
  import { fetchGetSurveyPage } from '@/api/allocation-manage'
  import { ElPopover } from 'element-plus'
  import SurveySearch from './modules/survey-search.vue'
  import SurveyDrawer from './modules/survey-drawer.vue'
  import ArtStatusDot from '@/components/core/tables/art-status-dot/index.vue'
  import StudentInfoPopover from '@/components/core/cards/art-student-info-popover/index.vue'
  import { fetchGetStudentDetail } from '@/api/accommodation-manage'

  defineOptions({ name: 'AllocationSurvey' })

  type SurveyListItem = Api.Allocation.SurveyListItem

  const showSearchBar = ref(false)
  const drawerVisible = ref(false)

  // 学生详情懒加载缓存（hover 时才请求，避免 N+1）
  const studentDetailCache = ref<Map<number, Api.AccommodationManage.StudentListItem>>(new Map())

  /**
   * 懒加载学生详情（仅在 popover 显示时触发）
   */
  const lazyLoadStudent = async (studentId: number) => {
    if (studentDetailCache.value.has(studentId)) return
    try {
      const detail = await fetchGetStudentDetail(studentId)
      if (detail) {
        studentDetailCache.value.set(studentId, detail)
      }
    } catch {
      // 静默失败，popover 显示已有数据
    }
  }

  /**
   * 获取 popover 展示用的学生数据（合并列表数据 + 缓存详情）
   */
  const getPopoverStudent = (row: SurveyListItem) => {
    const cached = studentDetailCache.value.get(row.studentId)
    if (cached) return { ...row, ...cached }
    return row
  }
  const currentStudentId = ref<number | undefined>(undefined)
  const currentStudentName = ref<string | undefined>(undefined)

  // 搜索表单
  const searchForm = ref<Api.Allocation.SurveySearchParams>({
    pageNum: 1,
    studentName: undefined,
    studentNo: undefined,
    classCode: undefined,
    enrollmentYear: undefined,
    fillStatus: undefined
  })

  // 查看详情
  const handleViewDetail = (row: SurveyListItem) => {
    currentStudentId.value = row.studentId
    currentStudentName.value = row.studentName
    drawerVisible.value = true
  }

  // 发送提醒
  const handleSendReminder = async (row: SurveyListItem) => {
    try {
      await ElMessageBox.confirm(`确定要向 ${row.studentName} 发送问卷填写提醒吗？`, '发送提醒', {
        type: 'info'
      })
      // TODO: 调用发送提醒 API（需要对接消息系统）
      ElMessage.success('提醒发送成功')
    } catch {
      // 取消操作
    }
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
  } = useTable<typeof fetchGetSurveyPage>({
    core: {
      apiFn: fetchGetSurveyPage,
      apiParams: computed(() => ({
        pageNum: searchForm.value.pageNum,
        studentName: searchForm.value.studentName || undefined,
        studentNo: searchForm.value.studentNo || undefined,
        classCode: searchForm.value.classCode || undefined,
        enrollmentYear: searchForm.value.enrollmentYear,
        fillStatus: searchForm.value.fillStatus || undefined
      })),
      paginationKey: { current: 'pageNum', size: 'pageSize' },
      columnsFactory: () => [
        { prop: 'studentNo', label: '学号', width: 120 },
        {
          prop: 'studentName',
          label: '姓名',
          minWidth: 100,
          formatter: (row: SurveyListItem) => {
            if (!row.studentName) return h('span', '--')
            return h(
              ElPopover,
              {
                placement: 'bottom-start',
                trigger: 'hover',
                width: 320,
                popperClass: 'student-info-popover',
                onShow: () => lazyLoadStudent(row.studentId)
              },
              {
                default: () => h(StudentInfoPopover, { student: getPopoverStudent(row) }),
                reference: () =>
                  h(
                    'span',
                    {
                      class: 'cursor-pointer hover:underline',
                      style: { color: 'var(--el-color-primary)' }
                    },
                    row.studentName
                  )
              }
            )
          }
        },
        { prop: 'className', label: '班级', minWidth: 140 },
        {
          prop: 'enrollmentYear',
          label: '入学年份',
          width: 100,
          formatter: (row: SurveyListItem) => `${row.enrollmentYear}级`
        },
        {
          prop: 'fillStatus',
          label: '填写状态',
          width: 100,
          formatter: (row: SurveyListItem) => {
            const type = row.fillStatus === 'filled' ? 'success' : 'danger'
            const text = row.fillStatus === 'filled' ? '已填写' : '未填写'
            return h(ArtStatusDot, { type, text })
          }
        },
        {
          prop: 'fillTime',
          label: '填写时间',
          width: 180,
          sortable: true,
          formatter: (row: SurveyListItem) => row.fillTime || '-'
        },
        {
          prop: 'sleepSchedule',
          label: '作息时间',
          minWidth: 140,
          formatter: (row: SurveyListItem) => row.sleepSchedule || '-'
        },
        {
          prop: 'smokingStatus',
          label: '吸烟情况',
          width: 90,
          formatter: (row: SurveyListItem) => row.smokingStatus || '-'
        },
        {
          prop: 'action',
          label: '操作',
          width: 120,
          fixed: 'right',
          formatter: (row: SurveyListItem) => {
            const actions = []

            if (row.fillStatus === 'filled') {
              actions.push({
                type: 'view',
                label: '查看详情',
                onClick: () => handleViewDetail(row),
                auth: 'allocation:survey:detail'
              })
            }

            if (row.fillStatus === 'unfilled') {
              actions.push({
                type: 'notify',
                label: '发送提醒',
                onClick: () => handleSendReminder(row),
                auth: 'allocation:survey:statistics'
              })
            }

            return actions
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

  // 搜索
  const handleSearch = async () => {
    searchForm.value.pageNum = 1
    await getData()
  }

  // 重置
  const handleReset = async () => {
    searchForm.value = {
      pageNum: 1,
      studentName: undefined,
      studentNo: undefined,
      classCode: undefined,
      enrollmentYear: undefined,
      fillStatus: undefined
    }
    await resetSearchParams()
  }

  // 批量发送提醒
  const handleBatchReminder = async () => {
    try {
      await ElMessageBox.confirm('确定要向所有未填写问卷的学生发送提醒吗？', '批量发送提醒', {
        type: 'warning'
      })
      // TODO: 调用批量发送提醒 API（需要对接消息系统）
      ElMessage.success('批量提醒发送成功')
    } catch {
      // 取消操作
    }
  }
</script>
