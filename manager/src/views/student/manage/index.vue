<!-- 学生管理页面 -->
<template>
  <div class="student-page art-full-height">
    <!-- 搜索栏 -->
    <StudentSearch
      v-show="showSearchBar"
      v-model="searchForm"
      @search="handleSearch"
      @reset="resetSearchParams"
    ></StudentSearch>

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
              v-permission="'student:delete'"
            >
              批量删除{{ selectedCount > 0 ? `(${selectedCount})` : '' }}
            </ElButton>
            <ElButton
              @click="
                () => {
                  importDialogVisible = true
                }
              "
              v-ripple
            >
              导入数据
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

    <!-- 学生详情抽屉（查看） -->
    <StudentDrawer
      v-model:visible="drawerVisible"
      :dialog-type="drawerDialogType"
      :student-id="editData?.id"
      :student-data="editData"
      @saved="handleRefresh"
    />

    <!-- 学生新增/编辑弹窗 -->
    <StudentDialog
      v-model:visible="dialogVisible"
      :dialog-type="dialogDialogType"
      :student-id="editData?.id"
      :student-data="editData"
      @saved="handleRefresh"
    />

    <!-- 导入学生数据弹窗 -->
    <ArtImportDialog
      v-model="importDialogVisible"
      title="导入学生数据"
      template-name="学生导入模板.xlsx"
      :template-download-fn="handleDownloadTemplate"
      :tips="importTips"
      :upload-disabled="false"
      :enable-chunk-upload="true"
      :chunk-upload-threshold="5 * 1024 * 1024"
      :scan-with-progress-fn="scanStudentImportFile"
      @upload-success="genericUploadSuccess"
    />

    <!-- 导入结果弹窗 -->
    <StudentImportResultDialog v-model:visible="importResultVisible" :result="importResultData" />
  </div>
</template>

<script setup lang="ts">
  import { computed, h, ref } from 'vue'
  import { useTable } from '@/hooks/core/useTable'
  import { useGenericImport } from '@/composables/useGenericImport'
  import {
    fetchGetStudentPage,
    fetchDeleteStudent,
    fetchBatchDeleteStudent,
    fetchUpdateStudentStatus
  } from '@/api/student-manage'
  import { ElPopover } from 'element-plus'
  import ArtSwitch from '@/components/core/forms/art-switch/index.vue'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import StudentInfoPopover from '@/components/core/cards/art-student-info-popover/index.vue'
  import StudentSearch from './modules/student-search.vue'
  import StudentDrawer from './modules/student-drawer.vue'
  import StudentDialog from './modules/student-dialog.vue'
  import StudentImportResultDialog from './modules/student-import-result-dialog.vue'
  import ArtImportDialog from '@/components/core/forms/art-import-dialog/index.vue'
  import { studentImportConfig } from './config/student-import-config'
  import { scanStudentFileWithWorker } from '@/utils/excel/studentImportWorkerClient'
  import {
    studentTemplateColumns,
    STUDENT_TEMPLATE_SHEET_NAME
  } from './utils/studentTemplateFields'
  import { ImportValidationError } from '@/utils/excel/importValidation/errors'

  defineOptions({ name: 'StudentManage' })

  type StudentListItem = Api.StudentManage.StudentListItem & { _statusLoading?: boolean }

  // 状态管理
  const showSearchBar = ref(false)
  const drawerVisible = ref(false) // 查看抽屉的显示状态
  const dialogVisible = ref(false) // 编辑/新增弹窗的显示状态
  const dialogType = ref<'view' | 'edit' | 'add'>('view')
  const editData = ref<StudentListItem | null>(null)
  const importResultVisible = ref(false)
  const importResultData = ref<Api.StudentImport.ImportResult | null>(null)

  // 使用通用导入 Hook
  const {
    dialogVisible: importDialogVisible,
    handleDownloadTemplate,
    handleUploadSuccess: genericUploadSuccess
  } = useGenericImport({
    ...studentImportConfig,
    onImportComplete: (result) => {
      // 如果存在失败记录，自动弹出结果弹窗
      if (result.failCount > 0) {
        importResultData.value = result as Api.StudentImport.ImportResult
        importResultVisible.value = true
      }
      handleRefresh()
    },
    onViewDetail: (result) => {
      importResultData.value = result as Api.StudentImport.ImportResult
      importResultVisible.value = true
    }
  })

  // 自定义扫描函数（使用学生专用的 Worker 客户端）
  const scanStudentImportFile = async (
    file: File,
    onProgress: (payload: { scannedRows: number; totalRows: number; percent: number }) => void
  ): Promise<number | null> => {
    const result = await scanStudentFileWithWorker(file, studentTemplateColumns, {
      sheetName: STUDENT_TEMPLATE_SHEET_NAME,
      previewLimit: 50,
      maxScanErrors: 5000,
      maxTrailingEmptyRows: 200,
      onProgress
    })

    if (!result.ok) {
      throw new ImportValidationError('前端校验未通过，请修正后再上传', {
        errorCount: result.errorCount,
        truncated: result.truncated,
        preview: result.preview
      })
    }

    // 返回总行数，用于传给后端计算进度
    return result.totalRowsForImport || null
  }

  // 导入提示信息
  const importTips = [
    '请先下载模板，按模板格式填写学生信息',
    '学号为必填项，不可重复',
    '手机号、身份证号等信息需符合格式要求',
    '请选择 XLSX 文件，点击"扫描"通过后才能上传'
  ]

  // 批量选择
  const selectedRows = ref<StudentListItem[]>([])
  const selectedIds = computed(() => selectedRows.value.map((item) => item.id))
  const selectedCount = computed(() => selectedRows.value.length)

  // 计算属性：用于避免 ESLint 误报过滤器
  const drawerDialogType = computed<'view' | 'edit'>(() => {
    return dialogType.value === 'view' ? 'view' : 'edit'
  })
  const dialogDialogType = computed<'add' | 'edit'>(() => {
    return dialogType.value === 'add' ? 'add' : 'edit'
  })

  // 搜索表单
  const searchForm = ref<
    Api.StudentManage.StudentSearchParams & {
      orgCascader?: string[]
      dormCascader?: string[]
      dormCampusCode?: string
      floorCode?: string
      roomCode?: string
      gender?: number
      nation?: string
      politicalStatus?: string
    }
  >({
    pageNum: 1,
    studentNo: undefined,
    studentName: undefined,
    phone: undefined,
    orgCascader: [], // 组织级联选择器值 [校区, 院系, 专业]
    campusCode: undefined,
    deptCode: undefined,
    majorCode: undefined,
    dormCascader: [], // 宿舍级联选择器值 [校区, 楼层, 房间]
    dormCampusCode: undefined,
    floorCode: undefined,
    roomCode: undefined,
    classId: undefined,
    bedId: undefined,
    gender: undefined,
    nation: undefined,
    politicalStatus: undefined,
    academicStatus: undefined,
    status: undefined
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
  } = useTable<typeof fetchGetStudentPage>({
    core: {
      apiFn: fetchGetStudentPage,
      apiParams: computed(() => ({
        pageNum: searchForm.value.pageNum,
        studentNo: searchForm.value.studentNo || undefined,
        studentName: searchForm.value.studentName || undefined,
        phone: searchForm.value.phone || undefined,
        campusCode: searchForm.value.campusCode || undefined,
        deptCode: searchForm.value.deptCode || undefined,
        majorCode: searchForm.value.majorCode || undefined,
        classId: searchForm.value.classId || undefined,
        bedId: searchForm.value.bedId || undefined,
        academicStatus: searchForm.value.academicStatus,
        status: searchForm.value.status
      })),
      paginationKey: {
        current: 'pageNum',
        size: 'pageSize'
      },
      columnsFactory: () => [
        { type: 'selection', width: 50 },
        { prop: 'studentNo', label: '学号', width: 120 },
        {
          prop: 'studentName',
          label: '姓名',
          minWidth: 100,
          formatter: (row: StudentListItem) => {
            // 只有当有学生姓名时才显示悬浮卡片
            if (!row.studentName) {
              return h('span', row.studentName || '--')
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
                default: () => h(StudentInfoPopover, { student: row }),
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
        {
          prop: 'genderText',
          label: '性别',
          width: 70,
          formatter: (row: StudentListItem) => {
            const genderIcon = {
              1: 'ri-men-line',
              2: 'ri-women-line'
            }
            return h('div', { class: 'flex items-center gap-1' }, [
              h('span', { class: 'text-g-700 text-sm' }, row.genderText as string),
              h(ArtSvgIcon, {
                icon: genderIcon[row.gender as number as keyof typeof genderIcon] as string,
                class: `text-g-700 text-md ${row.gender === 1 ? 'text-primary' : 'text-pink-500'}`
              })
            ])
          }
        },
        { prop: 'phone', label: '手机号', width: 125 },
        { prop: 'nation', label: '民族', minWidth: 80 },
        { prop: 'politicalStatus', label: '政治面貌', minWidth: 100 },
        { prop: 'campusName', label: '校区', minWidth: 100, showOverflowTooltip: true },
        { prop: 'deptName', label: '院系', minWidth: 120, showOverflowTooltip: true },
        { prop: 'majorName', label: '专业', minWidth: 120, showOverflowTooltip: true },
        { prop: 'className', label: '班级', minWidth: 100, showOverflowTooltip: true },
        { prop: 'floorName', label: '楼层', minWidth: 100, showOverflowTooltip: true },
        { prop: 'roomName', label: '房间', minWidth: 100, showOverflowTooltip: true },
        { prop: 'bedName', label: '床位', minWidth: 100, showOverflowTooltip: true },
        { prop: 'academicStatusText', label: '学籍状态', width: 100 },
        { prop: 'enrollmentYear', label: '入学年份', width: 100 },
        { prop: 'currentGrade', label: '当前年级', width: 100 },
        {
          prop: 'status',
          label: '状态',
          width: 100,
          formatter: (row: StudentListItem) => {
            return h(ArtSwitch, {
              modelValue: row.status === 1,
              loading: row._statusLoading || false,
              inlinePrompt: true,
              onChange: (value: string | number | boolean) => {
                handleStatusChange(row, value === true || value === 1)
              }
            })
          }
        },
        {
          prop: 'action',
          label: '操作',
          width: 180,
          fixed: 'right',
          formatter: (row: StudentListItem) => [
            { type: 'view', label: '查看', onClick: () => handleView(row) },
            {
              type: 'edit',
              label: '编辑',
              onClick: () => handleEdit(row),
              auth: 'student:edit'
            },
            {
              type: 'delete',
              label: '删除',
              onClick: () => handleDelete(row),
              auth: 'student:delete',
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

  // 表格自动刷新逻辑由 ArtTableHeader 统一处理，这里不再单独维护

  /**
   * 搜索处理
   */
  const handleSearch = (params: Record<string, any>) => {
    Object.assign(searchParams, params, { pageNum: 1 })
    getData()
  }

  // 刷新数据
  const handleRefresh = () => {
    refreshData()
  }

  // 查看
  const handleView = async (row: StudentListItem) => {
    dialogType.value = 'view'
    editData.value = row
    drawerVisible.value = true
    dialogVisible.value = false // 确保编辑弹窗关闭
  }

  // 编辑
  const handleEdit = (row: StudentListItem) => {
    dialogType.value = 'edit'
    editData.value = row
    dialogVisible.value = true
    drawerVisible.value = false // 确保查看抽屉关闭
  }

  // 删除
  const handleDelete = async (row: StudentListItem) => {
    try {
      await ElMessageBox.confirm(`确定要删除学生"${row.studentName}"吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: true
      })
      await fetchDeleteStudent(row.id)
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
        `确定要批量删除选中的 ${selectedCount.value} 条学生数据吗？`,
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
          dangerouslyUseHTMLString: true
        }
      )
      await fetchBatchDeleteStudent(selectedIds.value)
      selectedRows.value = []
      await refreshRemove()
    } catch {
      // 用户取消或删除失败
    }
  }

  // 选择变化
  const handleSelectionChange = (rows: StudentListItem[]) => {
    selectedRows.value = rows
  }

  /**
   * 更新学生状态
   */
  const handleStatusChange = async (row: StudentListItem, value: boolean): Promise<void> => {
    const originalStatus = row.status
    try {
      row._statusLoading = true
      row.status = value ? 1 : 0
      await fetchUpdateStudentStatus(row.id, value ? 1 : 0)
    } catch (error) {
      console.error('更新学生状态失败:', error)
      row.status = originalStatus
      ElMessage.error('更新学生状态失败')
    } finally {
      row._statusLoading = false
    }
  }
</script>

<style scoped lang="scss">
  // 导入通知样式优化（缩小尺寸）
  :deep(.import-progress-notification) {
    width: 320px !important;
    padding: 12px 16px !important;

    .el-notification__title {
      margin-bottom: 6px !important;
      font-size: 14px !important;
    }

    .el-notification__content {
      margin-top: 4px !important;
      font-size: 12px !important;
    }

    .el-notification__icon {
      font-size: 20px !important;
    }
  }
</style>
