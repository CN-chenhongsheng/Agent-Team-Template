<!-- 院系管理页面 -->
<template>
  <div class="department-page art-full-height">
    <!-- 搜索栏 -->
    <DepartmentSearch
      v-show="showSearchBar"
      v-model="formFilters"
      @reset="handleReset"
      @search="handleSearch"
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
            <ElButton @click="handleAdd" v-ripple v-permission="'system:department:add'"
              >新增院系</ElButton
            >
            <ElButton
              :disabled="selectedCount === 0"
              @click="handleBatchDelete"
              v-ripple
              v-permission="'system:department:delete'"
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

      <!-- 院系弹窗 -->
      <DepartmentDialog
        v-model:visible="dialogVisible"
        :type="dialogType"
        :edit-data="editData"
        @submit="handleSubmit"
      />

      <!-- 下钻弹框 -->
      <DrillDownDialog
        v-model:visible="drillDownVisible"
        :drill-type="drillDownType"
        :parent-name="drillDownParentName"
        :filter-params="drillDownFilterParams"
        :key="`${drillDownType}-${drillDownVisible}`"
        @drill-down="handleDrillDown"
      />

      <!-- 嵌套下钻弹框 -->
      <DrillDownDialog
        v-model:visible="nestedDrillDownVisible"
        :drill-type="nestedDrillDownType"
        :parent-name="nestedDrillDownParentName"
        :filter-params="nestedDrillDownFilterParams"
        :key="`${nestedDrillDownType}-${nestedDrillDownVisible}`"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { useTable } from '@/hooks/core/useTable'
  import DepartmentDialog from './modules/department-dialog.vue'
  import {
    fetchGetDepartmentPage,
    fetchDeleteDepartment,
    fetchBatchDeleteDepartment,
    fetchUpdateDepartmentStatus
  } from '@/api/school-manage'
  import { useReferenceStore } from '@/store/modules/reference'
  import DrillDownDialog from '@/components/school/DrillDownDialog.vue'
  import DepartmentSearch from './modules/department-search.vue'
  import ArtSwitch from '@/components/core/forms/art-switch/index.vue'
  import { h } from 'vue'

  // 使用参考数据 store（用于缓存失效）
  const referenceStore = useReferenceStore()

  defineOptions({ name: 'OrganizationDepartment' })

  type DepartmentListItem = Api.SystemManage.DepartmentListItem & { _statusLoading?: boolean }

  // 状态管理
  const showSearchBar = ref(false)
  const dialogVisible = ref(false)
  const dialogType = ref<'add' | 'edit'>('add')
  const editData = ref<DepartmentListItem | null>(null)

  // 批量选择
  const selectedRows = ref<DepartmentListItem[]>([])
  const selectedIds = computed(() => selectedRows.value.map((item) => item.id))
  const selectedCount = computed(() => selectedRows.value.length)

  // 下钻弹框相关
  const drillDownVisible = ref(false)
  const drillDownType = ref<'department' | 'major' | 'class'>('major')
  const drillDownParentName = ref('')
  const drillDownFilterParams = ref<Record<string, any>>({})
  const nestedDrillDownVisible = ref(false)
  const nestedDrillDownType = ref<'department' | 'major' | 'class'>('class')
  const nestedDrillDownParentName = ref('')
  const nestedDrillDownFilterParams = ref<Record<string, any>>({})

  // 搜索相关
  const formFilters = ref({
    pageNum: 1,
    deptCode: '',
    deptName: '',
    campusCode: '',
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
    resetSearchParams,
    handleSizeChange,
    handleCurrentChange,
    refreshData,
    refreshCreate,
    refreshUpdate,
    refreshRemove,
    contextMenuItems,
    contextMenuWidth,
    handleRowContextmenu,
    handleContextMenuSelect
  } = useTable<typeof fetchGetDepartmentPage>({
    core: {
      apiFn: fetchGetDepartmentPage,
      apiParams: computed(() => {
        return {
          pageNum: formFilters.value.pageNum,
          deptCode: formFilters.value.deptCode || undefined,
          deptName: formFilters.value.deptName || undefined,
          campusCode: formFilters.value.campusCode || undefined,
          status: formFilters.value.status
        } as Partial<Api.SystemManage.DepartmentSearchParams>
      }),
      paginationKey: {
        current: 'pageNum',
        size: 'pageSize'
      },
      columnsFactory: () => [
        {
          type: 'selection',
          width: 50,
          reserveSelection: true
        },
        {
          prop: 'deptCode',
          label: '院系编码',
          minWidth: 120
        },
        {
          prop: 'deptName',
          label: '院系名称',
          minWidth: 150
        },
        {
          prop: 'campusName',
          label: '所属校区',
          minWidth: 120
        },
        {
          prop: 'leader',
          label: '院系领导',
          minWidth: 100
        },
        {
          prop: 'phone',
          label: '联系电话',
          width: 125
        },
        {
          prop: 'sort',
          label: '排序',
          width: 85,
          sortable: true
        },
        {
          prop: 'status',
          label: '状态',
          width: 100,
          formatter: (row: DepartmentListItem) => {
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
          prop: 'createTime',
          label: '创建时间',
          width: 180,
          sortable: true
        },
        {
          prop: 'action',
          label: '操作',
          width: 180,
          fixed: 'right' as const,
          formatter: (row: DepartmentListItem) => [
            { type: 'view', label: '查看', onClick: () => handleViewMajors(row) },
            {
              type: 'edit',
              label: '编辑',
              onClick: () => handleEdit(row),
              auth: 'system:department:edit'
            },
            {
              type: 'delete',
              label: '删除',
              onClick: () => handleDelete(row),
              auth: 'system:department:delete',
              danger: true
            }
          ]
        }
      ],
      immediate: true
    },
    adaptive: {
      enabled: true
    },
    contextMenu: {
      enabled: true
    }
  })

  /**
   * 搜索
   */
  const handleSearch = async (): Promise<void> => {
    await getData()
  }

  /**
   * 重置搜索
   */
  const handleReset = async (): Promise<void> => {
    formFilters.value = {
      pageNum: 1,
      deptCode: '',
      deptName: '',
      campusCode: '',
      status: undefined
    }
    await resetSearchParams()
  }

  /**
   * 刷新数据
   */
  const handleRefresh = (): void => {
    refreshData()
  }

  /**
   * 新增院系
   */
  const handleAdd = (): void => {
    dialogType.value = 'add'
    editData.value = null
    dialogVisible.value = true
  }

  /**
   * 编辑院系
   */
  const handleEdit = (row: DepartmentListItem): void => {
    dialogType.value = 'edit'
    editData.value = { ...row }
    dialogVisible.value = true
  }

  /**
   * 删除院系
   */
  const handleDelete = async (row: DepartmentListItem): Promise<void> => {
    try {
      await ElMessageBox.confirm(
        `确定要删除院系"${row.deptName}"吗？<br/>提示：删除院系后，该院系下的所有专业和班级也会被删除。`,
        '删除确认',
        {
          type: 'warning',
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          dangerouslyUseHTMLString: true
        }
      )
      await fetchDeleteDepartment(row.id)
      // 刷新院系树缓存
      await referenceStore.refreshDepartmentTree()
      await refreshRemove()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除院系失败:', error)
      }
    }
  }

  /**
   * 处理表格行选择变化
   */
  const handleSelectionChange = (selection: DepartmentListItem[]): void => {
    selectedRows.value = selection
  }

  /**
   * 批量删除院系
   */
  const handleBatchDelete = async (): Promise<void> => {
    if (selectedCount.value === 0) {
      ElMessage.warning('请选择要删除的院系')
      return
    }

    try {
      await ElMessageBox.confirm(
        `确定要批量删除选中的 ${selectedCount.value} 个院系吗？<br/>提示：删除院系后，该院系下的所有专业和班级也会被删除。`,
        '批量删除确认',
        {
          type: 'warning',
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          dangerouslyUseHTMLString: true
        }
      )
      await fetchBatchDeleteDepartment(selectedIds.value as number[])
      // 刷新院系树缓存
      await referenceStore.refreshDepartmentTree()
      selectedRows.value = []
      await refreshRemove()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('批量删除院系失败:', error)
      }
    }
  }

  /**
   * 弹窗提交
   */
  const handleSubmit = async (): Promise<void> => {
    dialogVisible.value = false
    // 刷新院系树缓存
    await referenceStore.refreshDepartmentTree()
    // 根据 dialogType 判断是新增还是编辑
    if (dialogType.value === 'add') {
      await refreshCreate()
    } else {
      await refreshUpdate()
    }
  }

  /**
   * 查看专业
   */
  const handleViewMajors = (row: DepartmentListItem): void => {
    drillDownType.value = 'major'
    drillDownParentName.value = row.deptName
    drillDownFilterParams.value = { deptCode: row.deptCode }
    drillDownVisible.value = true
  }

  /**
   * 处理下钻事件
   */
  const handleDrillDown = (
    type: 'department' | 'major' | 'class' | 'floor' | 'room' | 'bed',
    row: any
  ): void => {
    if (type === 'class') {
      // 从专业下钻到班级
      nestedDrillDownType.value = 'class'
      nestedDrillDownParentName.value = row.majorName
      nestedDrillDownFilterParams.value = { majorCode: row.majorCode }
      nestedDrillDownVisible.value = true
    }
  }

  /**
   * 更新院系状态
   */
  const handleStatusChange = async (row: DepartmentListItem, value: boolean): Promise<void> => {
    // 如果是关闭操作（从启用变为停用），需要提示用户级联影响
    if (!value && row.status === 1) {
      try {
        let message = `确定要停用院系"${row.deptName}"吗？<br/>提示：停用院系后，该院系下的所有专业和班级也会被停用。`
        await ElMessageBox.confirm(message, '确认停用', {
          type: 'warning',
          confirmButtonText: '确认停用',
          cancelButtonText: '取消',
          dangerouslyUseHTMLString: true
        })
      } catch {
        // 用户取消操作，不执行任何更改
        return
      }
    }

    const originalStatus = row.status
    try {
      row._statusLoading = true
      row.status = value ? 1 : 0
      await fetchUpdateDepartmentStatus(row.id, value ? 1 : 0)
    } catch (error) {
      console.error('更新院系状态失败:', error)
      row.status = originalStatus
    } finally {
      row._statusLoading = false
    }
  }
</script>
