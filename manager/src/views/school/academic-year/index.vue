<!-- 学年管理页面 -->
<template>
  <div class="academic-year-page art-full-height">
    <!-- 搜索栏 -->
    <AcademicYearSearch
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
            <ElButton @click="handleAdd" v-ripple v-permission="'system:academic-year:add'"
              >新增学年</ElButton
            >
            <ElButton
              :disabled="selectedCount === 0"
              @click="handleBatchDelete"
              v-ripple
              v-permission="'system:academic-year:delete'"
            >
              批量删除{{ selectedCount > 0 ? `(${selectedCount})` : '' }}
            </ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <ArtTable
        :contextMenuItems="contextMenuItems"
        :contextMenuWidth="contextMenuWidth"
        :onRowContextmenu="handleRowContextmenu as any"
        :onContextMenuSelect="handleContextMenuSelect"
        :loading="loading"
        :columns="columns"
        :data="data"
        :stripe="false"
        row-key="id"
        @selection-change="handleSelectionChange"
      />

      <!-- 学年弹窗 -->
      <AcademicYearDialog
        v-model:visible="dialogVisible"
        :type="dialogType"
        :edit-data="editData"
        @submit="handleSubmit"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { useTable } from '@/hooks/core/useTable'
  import AcademicYearDialog from './modules/academic-year-dialog.vue'
  import AcademicYearSearch from './modules/academic-year-search.vue'
  import {
    fetchGetAcademicYearPage,
    fetchBatchDeleteAcademicYear,
    fetchDeleteAcademicYear,
    fetchUpdateAcademicYearStatus
  } from '@/api/school-manage'
  import { ElMessageBox, ElMessage } from 'element-plus'
  import ArtSwitch from '@/components/core/forms/art-switch/index.vue'
  import { h } from 'vue'

  defineOptions({ name: 'AcademicYear' })

  type AcademicYearListItem = {
    id: number
    yearCode: string
    yearName: string
    startDate: string
    endDate: string
    status: number
    createTime?: string
    _statusLoading?: boolean
  }

  // 状态管理
  const showSearchBar = ref(false)
  const dialogVisible = ref(false)
  const dialogType = ref<'add' | 'edit'>('add')
  const editData = ref<AcademicYearListItem | null>(null)

  // 批量选择
  const selectedRows = ref<AcademicYearListItem[]>([])
  const selectedIds = computed(() => selectedRows.value.map((item) => item.id))
  const selectedCount = computed(() => selectedRows.value.length)

  // 搜索相关
  const formFilters = ref({
    yearCode: '',
    yearName: '',
    status: undefined
  })

  // 使用 useTable 管理表格数据
  const {
    columns,
    columnChecks,
    data,
    loading,
    getData,
    resetSearchParams,
    refreshData,
    refreshCreate,
    refreshUpdate,
    refreshRemove,
    contextMenuItems,
    contextMenuWidth,
    handleRowContextmenu,
    handleContextMenuSelect
  } = useTable<typeof fetchGetAcademicYearPage>({
    core: {
      apiFn: fetchGetAcademicYearPage,
      apiParams: computed(() => {
        return {
          yearCode: formFilters.value.yearCode || undefined,
          yearName: formFilters.value.yearName || undefined,
          status: formFilters.value.status
        }
      }),
      columnsFactory: () => [
        {
          type: 'selection',
          width: 50,
          reserveSelection: true
        },
        {
          prop: 'yearCode',
          label: '学年编码',
          minWidth: 120
        },
        {
          prop: 'yearName',
          label: '学年名称',
          minWidth: 150
        },
        {
          prop: 'startDate',
          label: '开始日期',
          width: 120
        },
        {
          prop: 'endDate',
          label: '结束日期',
          width: 120
        },
        {
          prop: 'status',
          label: '状态',
          width: 100,
          formatter: (row: AcademicYearListItem) => {
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
          formatter: (row: AcademicYearListItem) => [
            {
              type: 'edit',
              label: '编辑',
              onClick: () => handleEdit(row),
              auth: 'system:academic-year:edit'
            },
            {
              type: 'delete',
              label: '删除',
              onClick: () => handleDelete(row),
              auth: 'system:academic-year:delete',
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
      yearCode: '',
      yearName: '',
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
   * 新增学年
   */
  const handleAdd = (): void => {
    dialogType.value = 'add'
    editData.value = null
    dialogVisible.value = true
  }

  /**
   * 编辑学年
   */
  const handleEdit = (row: AcademicYearListItem): void => {
    dialogType.value = 'edit'
    editData.value = { ...row }
    dialogVisible.value = true
  }

  /**
   * 删除学年
   */
  const handleDelete = async (row: AcademicYearListItem): Promise<void> => {
    try {
      await ElMessageBox.confirm(
        `确定要删除学年"${row.yearName}"吗？此操作不可恢复！`,
        '删除确认',
        {
          type: 'warning',
          confirmButtonText: '确定删除',
          cancelButtonText: '取消'
        }
      )
      await fetchDeleteAcademicYear(row.id)
      await refreshRemove()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除学年失败:', error)
      }
    }
  }

  /**
   * 处理表格行选择变化
   */
  const handleSelectionChange = (selection: AcademicYearListItem[]): void => {
    selectedRows.value = selection
  }

  /**
   * 批量删除学年
   */
  const handleBatchDelete = async (): Promise<void> => {
    if (selectedCount.value === 0) {
      ElMessage.warning('请至少选择一条数据')
      return
    }

    try {
      await ElMessageBox.confirm(
        `确定要批量删除选中的 ${selectedCount.value} 条学年数据吗？此操作不可恢复！`,
        '批量删除确认',
        {
          type: 'warning',
          confirmButtonText: '确定删除',
          cancelButtonText: '取消'
        }
      )
      await fetchBatchDeleteAcademicYear(selectedIds.value as number[])
      selectedRows.value = []
      await getData()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('批量删除学年失败:', error)
      }
    }
  }

  /**
   * 弹窗提交
   */
  const handleSubmit = async (): Promise<void> => {
    dialogVisible.value = false
    // 根据 dialogType 判断是新增还是编辑
    if (dialogType.value === 'add') {
      await refreshCreate()
    } else {
      await refreshUpdate()
    }
  }

  /**
   * 更新学年状态
   */
  const handleStatusChange = async (row: AcademicYearListItem, value: boolean): Promise<void> => {
    const originalStatus = row.status
    try {
      row._statusLoading = true
      row.status = value ? 1 : 0
      await fetchUpdateAcademicYearStatus(row.id, value ? 1 : 0)
    } catch (error) {
      console.error('更新学年状态失败:', error)
      row.status = originalStatus
    } finally {
      row._statusLoading = false
    }
  }
</script>
