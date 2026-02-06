<!-- 专业管理页面 -->
<template>
  <div class="major-page art-full-height">
    <!-- 搜索栏 -->
    <MajorSearch
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
        :loading="loading"
        v-model:columns="columnChecks"
        v-model:showSearchBar="showSearchBar"
        @refresh="handleRefresh"
      >
        <template #left>
          <ElSpace wrap>
            <ElButton @click="handleAdd" v-ripple v-permission="'system:major:add'"
              >新增专业</ElButton
            >
            <ElButton
              :disabled="selectedCount === 0"
              @click="handleBatchDelete"
              v-ripple
              v-permission="'system:major:delete'"
            >
              批量删除{{ selectedCount > 0 ? `(${selectedCount})` : '' }}
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
        @selection-change="handleSelectionChange"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />

      <!-- 专业弹窗 -->
      <MajorDialog
        v-model:visible="dialogVisible"
        :type="dialogType"
        :edit-data="editData"
        @submit="handleDialogSubmit"
      />

      <!-- 下钻弹框 -->
      <DrillDownDialog
        v-model:visible="drillDownVisible"
        :drill-type="drillDownType"
        :parent-name="drillDownParentName"
        :filter-params="drillDownFilterParams"
        :key="`${drillDownType}-${drillDownVisible}`"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchGetMajorPage,
    fetchDeleteMajor,
    fetchBatchDeleteMajor,
    fetchUpdateMajorStatus
  } from '@/api/school-manage'
  import { useReferenceStore } from '@/store/modules/reference'
  import DrillDownDialog from '@/components/school/DrillDownDialog.vue'
  import ArtSwitch from '@/components/core/forms/art-switch/index.vue'
  import MajorDialog from './modules/major-dialog.vue'
  import MajorSearch from './modules/major-search.vue'
  import { ElMessageBox, ElMessage } from 'element-plus'
  import { DialogType } from '@/types'
  import { h } from 'vue'

  // 使用参考数据 store（用于缓存失效）
  const referenceStore = useReferenceStore()

  defineOptions({ name: 'OrganizationMajor' })

  type MajorListItem = Api.SystemManage.MajorListItem & { _statusLoading?: boolean }

  // 弹窗相关
  const dialogType = ref<DialogType>('add')
  const dialogVisible = ref(false)
  const editData = ref<Partial<MajorListItem>>({})
  const selectedRows = ref<MajorListItem[]>([])
  const selectedCount = computed(() => selectedRows.value.length)
  const showSearchBar = ref(false)

  // 下钻弹框相关
  const drillDownVisible = ref(false)
  const drillDownType = ref<'department' | 'major' | 'class'>('class')
  const drillDownParentName = ref('')
  const drillDownFilterParams = ref<Record<string, any>>({})

  // 搜索相关
  const formFilters = ref({
    pageNum: 1,
    majorCode: undefined,
    majorName: undefined,
    locationCascader: [] as string[], // 级联选择器值 [校区, 院系]
    campusCode: undefined,
    deptCode: undefined,
    status: undefined
  })

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
  } = useTable<typeof fetchGetMajorPage>({
    core: {
      apiFn: fetchGetMajorPage,
      apiParams: computed(() => {
        return {
          majorCode: formFilters.value.majorCode || undefined,
          majorName: formFilters.value.majorName || undefined,
          deptCode: formFilters.value.deptCode || undefined,
          status: formFilters.value.status
        } as Partial<Api.SystemManage.MajorSearchParams>
      }),
      paginationKey: {
        current: 'pageNum',
        size: 'pageSize'
      },
      columnsFactory: () => [
        {
          type: 'selection',
          width: 55
        },
        {
          prop: 'majorCode',
          label: '专业编码',
          minWidth: 120
        },
        {
          prop: 'majorName',
          label: '专业名称',
          minWidth: 150
        },
        {
          prop: 'deptName',
          label: '所属院系',
          minWidth: 150
        },
        {
          prop: 'director',
          label: '专业负责人',
          minWidth: 120
        },
        {
          prop: 'typeText',
          label: '类型',
          width: 100
        },
        {
          prop: 'duration',
          label: '学制',
          width: 100
        },
        {
          prop: 'status',
          label: '状态',
          width: 100,
          formatter: (row: MajorListItem) => {
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
          formatter: (row: MajorListItem) => [
            { type: 'view', label: '查看', onClick: () => handleViewClasses(row) },
            {
              type: 'edit',
              label: '编辑',
              onClick: () => handleEdit(row),
              auth: 'system:major:edit'
            },
            {
              type: 'delete',
              label: '删除',
              onClick: () => handleDelete(row),
              auth: 'system:major:delete',
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
  } as any)

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
      majorCode: undefined,
      majorName: undefined,
      locationCascader: [],
      campusCode: undefined,
      deptCode: undefined,
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
   * 新增专业
   */
  const handleAdd = (): void => {
    dialogType.value = 'add'
    editData.value = {}
    dialogVisible.value = true
  }

  /**
   * 编辑专业
   */
  const handleEdit = (row: MajorListItem): void => {
    dialogType.value = 'edit'
    editData.value = { ...row }
    dialogVisible.value = true
  }

  /**
   * 删除专业
   */
  const handleDelete = async (row: MajorListItem): Promise<void> => {
    try {
      await ElMessageBox.confirm(
        `确定要删除专业"${row.majorName}"吗？<br/>提示：删除专业后，该专业下的所有班级也会被删除。`,
        '删除确认',
        {
          type: 'warning',
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          dangerouslyUseHTMLString: true
        }
      )
      await fetchDeleteMajor(row.id)
      // 刷新专业列表缓存
      await referenceStore.refreshMajorList()
      await refreshRemove()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除专业失败:', error)
      }
    }
  }

  /**
   * 批量删除
   */
  const handleBatchDelete = async (): Promise<void> => {
    if (selectedCount.value === 0) {
      ElMessage.warning('请选择要删除的专业')
      return
    }

    try {
      await ElMessageBox.confirm(
        `确定要删除选中的 ${selectedCount.value} 个专业吗？<br/>提示：删除专业后，这些专业下的所有班级也会被删除。`,
        '批量删除确认',
        {
          type: 'warning',
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          dangerouslyUseHTMLString: true
        }
      )
      const ids = selectedRows.value.map((item) => item.id)
      await fetchBatchDeleteMajor(ids)
      // 刷新专业列表缓存
      await referenceStore.refreshMajorList()
      selectedRows.value = []
      await refreshRemove()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('批量删除失败:', error)
      }
    }
  }

  /**
   * 选择变化
   */
  const handleSelectionChange = (selection: MajorListItem[]): void => {
    selectedRows.value = selection
  }

  /**
   * 弹窗提交
   */
  const handleDialogSubmit = async (): Promise<void> => {
    dialogVisible.value = false
    // 刷新专业列表缓存
    await referenceStore.refreshMajorList()
    // 根据 dialogType 判断是新增还是编辑
    if (dialogType.value === 'add') {
      await refreshCreate()
    } else {
      await refreshUpdate()
    }
  }

  /**
   * 查看班级
   */
  const handleViewClasses = (row: MajorListItem): void => {
    drillDownType.value = 'class'
    drillDownParentName.value = row.majorName
    drillDownFilterParams.value = { majorCode: row.majorCode }
    drillDownVisible.value = true
  }

  /**
   * 更新专业状态
   */
  const handleStatusChange = async (row: MajorListItem, value: boolean): Promise<void> => {
    // 如果是关闭操作（从启用变为停用），需要提示用户级联影响
    if (!value && row.status === 1) {
      try {
        await ElMessageBox.confirm(
          `确定要停用专业"${row.majorName}"吗？<br/>提示：停用专业后，该专业下的所有班级也会被停用。`,
          '确认停用',
          {
            type: 'warning',
            confirmButtonText: '确认停用',
            cancelButtonText: '取消',
            dangerouslyUseHTMLString: true
          }
        )
      } catch {
        // 用户取消操作，不执行任何更改
        return
      }
    }

    const originalStatus = row.status
    try {
      row._statusLoading = true
      row.status = value ? 1 : 0
      await fetchUpdateMajorStatus(row.id, value ? 1 : 0)
    } catch (error) {
      console.error('更新专业状态失败:', error)
      row.status = originalStatus
    } finally {
      row._statusLoading = false
    }
  }
</script>
