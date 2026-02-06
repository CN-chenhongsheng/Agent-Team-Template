<!-- 班级管理页面 -->
<template>
  <div class="class-page art-full-height">
    <!-- 搜索栏 -->
    <ClassSearch
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
            <ElButton @click="handleAdd" v-ripple v-permission="'system:class:add'"
              >新增班级</ElButton
            >
            <ElButton
              :disabled="selectedCount === 0"
              @click="handleBatchDelete"
              v-ripple
              v-permission="'system:class:delete'"
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

      <!-- 班级弹窗 -->
      <ClassDialog
        v-model:visible="dialogVisible"
        :type="dialogType"
        :edit-data="editData"
        @submit="handleDialogSubmit"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchGetClassPage,
    fetchDeleteClass,
    fetchBatchDeleteClass,
    fetchUpdateClassStatus
  } from '@/api/school-manage'
  import ArtSwitch from '@/components/core/forms/art-switch/index.vue'
  import ClassDialog from './modules/class-dialog.vue'
  import ClassSearch from './modules/class-search.vue'
  import { ElMessageBox, ElMessage } from 'element-plus'
  import { DialogType } from '@/types'
  import { h } from 'vue'

  defineOptions({ name: 'OrganizationClass' })

  type ClassListItem = Api.SystemManage.ClassListItem & { _statusLoading?: boolean }

  // 弹窗相关
  const dialogType = ref<DialogType>('add')
  const dialogVisible = ref(false)
  const editData = ref<Partial<ClassListItem>>({})
  const selectedRows = ref<ClassListItem[]>([])
  const selectedCount = computed(() => selectedRows.value.length)
  const showSearchBar = ref(false)

  // 搜索相关
  const formFilters = ref({
    pageNum: 1,
    classCode: undefined,
    className: undefined,
    locationCascader: [] as string[], // 级联选择器值 [校区, 院系, 专业]
    campusCode: undefined,
    deptCode: undefined,
    majorCode: undefined,
    grade: undefined,
    enrollmentYear: undefined,
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
  } = useTable<typeof fetchGetClassPage>({
    core: {
      apiFn: fetchGetClassPage,
      apiParams: computed(() => {
        return {
          classCode: formFilters.value.classCode || undefined,
          className: formFilters.value.className || undefined,
          majorCode: formFilters.value.majorCode || undefined,
          grade: formFilters.value.grade || undefined,
          enrollmentYear: formFilters.value.enrollmentYear,
          status: formFilters.value.status
        } as Partial<Api.SystemManage.ClassSearchParams>
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
          prop: 'classCode',
          label: '班级编码',
          minWidth: 120
        },
        {
          prop: 'className',
          label: '班级名称',
          minWidth: 150
        },
        {
          prop: 'majorName',
          label: '所属专业',
          minWidth: 150
        },
        {
          prop: 'grade',
          label: '年级',
          width: 100
        },
        {
          prop: 'enrollmentYear',
          label: '入学年份',
          width: 100
        },
        {
          prop: 'teacherName',
          label: '负责人',
          minWidth: 120
        },
        {
          prop: 'currentCount',
          label: '当前人数',
          width: 100
        },
        {
          prop: 'status',
          label: '状态',
          width: 100,
          formatter: (row: ClassListItem) => {
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
          width: 150,
          fixed: 'right' as const,
          formatter: (row: ClassListItem) => [
            {
              type: 'edit',
              label: '编辑',
              onClick: () => handleEdit(row),
              auth: 'system:class:edit'
            },
            {
              type: 'delete',
              label: '删除',
              onClick: () => handleDelete(row),
              auth: 'system:class:delete',
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
      classCode: undefined,
      className: undefined,
      locationCascader: [],
      campusCode: undefined,
      deptCode: undefined,
      majorCode: undefined,
      grade: undefined,
      enrollmentYear: undefined,
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
   * 新增班级
   */
  const handleAdd = (): void => {
    dialogType.value = 'add'
    editData.value = {}
    dialogVisible.value = true
  }

  /**
   * 编辑班级
   */
  const handleEdit = (row: ClassListItem): void => {
    dialogType.value = 'edit'
    editData.value = { ...row }
    dialogVisible.value = true
  }

  /**
   * 删除班级
   */
  const handleDelete = async (row: ClassListItem): Promise<void> => {
    try {
      await ElMessageBox.confirm(
        `确定要删除班级"${row.className}"吗？此操作不可恢复！`,
        '删除确认',
        {
          type: 'warning',
          confirmButtonText: '确定删除',
          cancelButtonText: '取消'
        }
      )
      await fetchDeleteClass(row.id)
      await refreshRemove()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除班级失败:', error)
      }
    }
  }

  /**
   * 批量删除
   */
  const handleBatchDelete = async (): Promise<void> => {
    if (selectedCount.value === 0) {
      ElMessage.warning('请选择要删除的班级')
      return
    }

    try {
      await ElMessageBox.confirm(
        `确定要删除选中的 ${selectedCount.value} 个班级吗？此操作不可恢复！`,
        '批量删除确认',
        {
          type: 'warning',
          confirmButtonText: '确定删除',
          cancelButtonText: '取消'
        }
      )
      const ids = selectedRows.value.map((item) => item.id)
      await fetchBatchDeleteClass(ids)
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
  const handleSelectionChange = (selection: ClassListItem[]): void => {
    selectedRows.value = selection
  }

  /**
   * 弹窗提交
   */
  const handleDialogSubmit = async (): Promise<void> => {
    dialogVisible.value = false
    // 根据 dialogType 判断是新增还是编辑
    if (dialogType.value === 'add') {
      await refreshCreate()
    } else {
      await refreshUpdate()
    }
  }

  /**
   * 更新班级状态
   */
  const handleStatusChange = async (row: ClassListItem, value: boolean): Promise<void> => {
    const originalStatus = row.status
    try {
      row._statusLoading = true
      row.status = value ? 1 : 0
      await fetchUpdateClassStatus(row.id, value ? 1 : 0)
    } catch (error) {
      console.error('更新班级状态失败:', error)
      row.status = originalStatus
    } finally {
      row._statusLoading = false
    }
  }
</script>
