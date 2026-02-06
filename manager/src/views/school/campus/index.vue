<!-- 校区管理页面 -->
<template>
  <div class="campus-page art-full-height">
    <!-- 搜索栏 -->
    <CampusSearch
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
            <ElButton @click="handleAdd" v-ripple v-permission="'system:campus:add'"
              >新增校区</ElButton
            >
            <ElButton
              :disabled="selectedCount === 0"
              @click="handleBatchDelete"
              v-ripple
              v-permission="'system:campus:delete'"
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

      <!-- 校区弹窗 -->
      <CampusDialog
        v-model:visible="dialogVisible"
        :type="dialogType"
        :edit-data="editData"
        @submit="handleSubmit"
      />

      <!-- 下钻弹框（院系） -->
      <DrillDownDialog
        v-model:visible="drillDownVisible"
        :drill-type="drillDownType"
        :parent-name="drillDownParentName"
        :filter-params="drillDownFilterParams"
        :key="`dept-${drillDownType}-${drillDownVisible}`"
        @drill-down="handleDrillDown"
      />

      <!-- 嵌套下钻弹框（专业） -->
      <DrillDownDialog
        v-model:visible="nestedDrillDownVisible"
        :drill-type="nestedDrillDownType"
        :parent-name="nestedDrillDownParentName"
        :filter-params="nestedDrillDownFilterParams"
        :key="`${nestedDrillDownType}-${nestedDrillDownVisible}`"
        @drill-down="handleNestedDrillDown"
      />

      <!-- 第三层下钻弹框（班级） -->
      <DrillDownDialog
        v-model:visible="thirdLevelDrillDownVisible"
        :drill-type="thirdLevelDrillDownType"
        :parent-name="thirdLevelDrillDownParentName"
        :filter-params="thirdLevelDrillDownFilterParams"
        :key="`${thirdLevelDrillDownType}-${thirdLevelDrillDownVisible}`"
      />

      <!-- 楼层下钻弹框（楼层） -->
      <DrillDownDialog
        v-model:visible="floorDrillDownVisible"
        :drill-type="floorDrillDownType"
        :parent-name="floorDrillDownParentName"
        :filter-params="floorDrillDownFilterParams"
        :key="`floor-${floorDrillDownType}-${floorDrillDownVisible}`"
        @drill-down="handleFloorDrillDown"
      />

      <!-- 楼层嵌套下钻弹框（房间） -->
      <DrillDownDialog
        v-model:visible="floorNestedDrillDownVisible"
        :drill-type="floorNestedDrillDownType"
        :parent-name="floorNestedDrillDownParentName"
        :filter-params="floorNestedDrillDownFilterParams"
        :key="`${floorNestedDrillDownType}-${floorNestedDrillDownVisible}`"
        @drill-down="handleFloorNestedDrillDown"
      />

      <!-- 楼层第三层下钻弹框（床位） -->
      <DrillDownDialog
        v-model:visible="floorThirdLevelDrillDownVisible"
        :drill-type="floorThirdLevelDrillDownType"
        :parent-name="floorThirdLevelDrillDownParentName"
        :filter-params="floorThirdLevelDrillDownFilterParams"
        :key="`${floorThirdLevelDrillDownType}-${floorThirdLevelDrillDownVisible}`"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { useTable } from '@/hooks/core/useTable'
  import CampusDialog from './modules/campus-dialog.vue'
  import CampusSearch from './modules/campus-search.vue'
  import {
    fetchGetCampusTree,
    fetchDeleteCampus,
    fetchBatchDeleteCampus,
    fetchUpdateCampusStatus
  } from '@/api/school-manage'
  import { useReferenceStore } from '@/store/modules/reference'
  import DrillDownDialog from '@/components/school/DrillDownDialog.vue'
  import { ElMessageBox, ElMessage } from 'element-plus'
  import ArtSwitch from '@/components/core/forms/art-switch/index.vue'
  import { h, nextTick } from 'vue'

  // 使用参考数据 store（用于缓存失效）
  const referenceStore = useReferenceStore()

  defineOptions({ name: 'Campus' })

  type CampusListItem = Api.SystemManage.CampusListItem & { _statusLoading?: boolean }

  // 状态管理
  const showSearchBar = ref(false)

  // 弹窗相关
  const dialogVisible = ref(false)
  const dialogType = ref<'add' | 'edit'>('add')
  const editData = ref<CampusListItem | null>(null)

  // 批量选择
  const selectedRows = ref<CampusListItem[]>([])
  const selectedIds = computed(() => selectedRows.value.map((item) => item.id))
  const selectedCount = computed(() => selectedRows.value.length)

  // 下钻弹框相关（院系->专业->班级）
  const drillDownVisible = ref(false)
  const drillDownType = ref<'department' | 'major' | 'class'>('department')
  const drillDownParentName = ref('')
  const drillDownFilterParams = ref<Record<string, any>>({})
  const nestedDrillDownVisible = ref(false)
  const nestedDrillDownType = ref<'department' | 'major' | 'class'>('major')
  const nestedDrillDownParentName = ref('')
  const nestedDrillDownFilterParams = ref<Record<string, any>>({})
  const thirdLevelDrillDownVisible = ref(false)
  const thirdLevelDrillDownType = ref<'department' | 'major' | 'class'>('class')
  const thirdLevelDrillDownParentName = ref('')
  const thirdLevelDrillDownFilterParams = ref<Record<string, any>>({})

  // 下钻弹框相关（楼层->房间->床位）
  const floorDrillDownVisible = ref(false)
  const floorDrillDownType = ref<'floor' | 'room' | 'bed'>('floor')
  const floorDrillDownParentName = ref('')
  const floorDrillDownFilterParams = ref<Record<string, any>>({})
  const floorNestedDrillDownVisible = ref(false)
  const floorNestedDrillDownType = ref<'floor' | 'room' | 'bed'>('room')
  const floorNestedDrillDownParentName = ref('')
  const floorNestedDrillDownFilterParams = ref<Record<string, any>>({})
  const floorThirdLevelDrillDownVisible = ref(false)
  const floorThirdLevelDrillDownType = ref<'floor' | 'room' | 'bed'>('bed')
  const floorThirdLevelDrillDownParentName = ref('')
  const floorThirdLevelDrillDownFilterParams = ref<Record<string, any>>({})

  // 搜索相关
  const formFilters = ref({
    pageNum: 1,
    campusCode: '',
    campusName: '',
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
  } = useTable<typeof fetchGetCampusTree>({
    core: {
      apiFn: fetchGetCampusTree,
      apiParams: computed(() => {
        return {
          campusCode: formFilters.value.campusCode || undefined,
          campusName: formFilters.value.campusName || undefined,
          status: formFilters.value.status
        } as Partial<Api.SystemManage.CampusSearchParams>
      }),
      columnsFactory: () => [
        {
          type: 'selection',
          width: 50,
          reserveSelection: true
        },
        {
          prop: 'campusCode',
          label: '校区编码',
          minWidth: 120
        },
        {
          prop: 'campusName',
          label: '校区名称',
          minWidth: 150
        },
        {
          prop: 'address',
          label: '校区地址',
          minWidth: 200
        },
        {
          prop: 'manager',
          label: '负责人',
          minWidth: 100
        },
        {
          prop: 'status',
          label: '状态',
          width: 100,
          formatter: (row: CampusListItem) => {
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
          prop: 'sort',
          label: '排序',
          width: 85,
          sortable: true
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
          formatter: (row: CampusListItem) => [
            {
              type: 'edit',
              label: '编辑',
              onClick: () => handleEdit(row),
              auth: 'system:campus:edit'
            },
            {
              type: 'delete',
              label: '删除',
              onClick: () => handleDelete(row),
              auth: 'system:campus:delete',
              danger: true
            },
            { type: 'view', label: '查看院系', onClick: () => handleViewDepartments(row) },
            { type: 'view', label: '查看楼层', onClick: () => handleViewFloors(row) }
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
      campusCode: '',
      campusName: '',
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
   * 新增校区
   */
  const handleAdd = (): void => {
    dialogType.value = 'add'
    editData.value = null
    dialogVisible.value = true
  }

  /**
   * 编辑校区
   */
  const handleEdit = (row: CampusListItem): void => {
    dialogType.value = 'edit'
    editData.value = { ...row }
    dialogVisible.value = true
  }

  /**
   * 删除校区
   */
  const handleDelete = async (row: CampusListItem): Promise<void> => {
    try {
      await ElMessageBox.confirm(
        `确定要删除校区"${row.campusName}"吗？<br/>提示：删除校区后，该校区下的所有院系、专业、班级、楼栋、房间和床位也会被删除。`,
        '删除确认',
        {
          type: 'warning',
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          dangerouslyUseHTMLString: true
        }
      )
      await fetchDeleteCampus(row.id)
      // 刷新校区树缓存
      await referenceStore.refreshCampusTree()
      await refreshRemove()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除校区失败:', error)
      }
    }
  }

  /**
   * 批量删除校区
   */
  const handleBatchDelete = async (): Promise<void> => {
    if (selectedCount.value === 0) {
      ElMessage.warning('请至少选择一条数据')
      return
    }

    try {
      await ElMessageBox.confirm(
        `确定要批量删除选中的 ${selectedCount.value} 条校区数据吗？<br/>提示：删除校区后，这些校区下的所有院系、专业、班级、楼栋、房间和床位也会被删除。`,
        '批量删除确认',
        {
          type: 'warning',
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          dangerouslyUseHTMLString: true
        }
      )
      await fetchBatchDeleteCampus(selectedIds.value as number[])
      // 刷新校区树缓存
      await referenceStore.refreshCampusTree()
      selectedRows.value = []
      await refreshRemove()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('批量删除校区失败:', error)
      }
    }
  }

  /**
   * 弹窗提交
   */
  const handleSubmit = async (): Promise<void> => {
    dialogVisible.value = false
    // 刷新校区树缓存
    await referenceStore.refreshCampusTree()
    // 根据 dialogType 判断是新增还是编辑
    if (dialogType.value === 'add') {
      await refreshCreate()
    } else {
      await refreshUpdate()
    }
  }

  /**
   * 查看院系
   */
  const handleViewDepartments = (row: CampusListItem): void => {
    // 先关闭楼层下钻弹框（如果打开）
    floorDrillDownVisible.value = false
    floorNestedDrillDownVisible.value = false
    floorThirdLevelDrillDownVisible.value = false

    // 打开院系下钻弹框
    drillDownType.value = 'department'
    drillDownParentName.value = row.campusName
    drillDownFilterParams.value = { campusCode: row.campusCode }
    drillDownVisible.value = true
  }

  /**
   * 查看楼层
   */
  const handleViewFloors = (row: CampusListItem): void => {
    // 先关闭院系下钻弹框（如果打开）
    drillDownVisible.value = false
    nestedDrillDownVisible.value = false
    thirdLevelDrillDownVisible.value = false

    // 打开楼层下钻弹框 - 先设置所有状态，再打开弹框
    floorDrillDownType.value = 'floor'
    floorDrillDownParentName.value = row.campusName
    floorDrillDownFilterParams.value = { campusCode: row.campusCode, pageNum: 1 }

    // 使用 nextTick 确保状态更新后再打开弹框
    nextTick(() => {
      floorDrillDownVisible.value = true
    })
  }

  /**
   * 处理表格行选择变化
   */
  const handleSelectionChange = (selection: CampusListItem[]): void => {
    selectedRows.value = selection
  }

  /**
   * 处理下钻事件
   */
  const handleDrillDown = (
    type: 'department' | 'major' | 'class' | 'floor' | 'room' | 'bed',
    row: any
  ): void => {
    if (type === 'major') {
      // 从院系下钻到专业
      nestedDrillDownType.value = 'major'
      nestedDrillDownParentName.value = row.deptName
      nestedDrillDownFilterParams.value = { deptCode: row.deptCode }
      nestedDrillDownVisible.value = true
    } else if (type === 'class') {
      // 从专业下钻到班级
      thirdLevelDrillDownType.value = 'class'
      thirdLevelDrillDownParentName.value = row.majorName
      thirdLevelDrillDownFilterParams.value = { majorCode: row.majorCode }
      thirdLevelDrillDownVisible.value = true
    }
  }

  /**
   * 处理嵌套下钻事件（从专业下钻到班级）
   */
  const handleNestedDrillDown = (
    type: 'department' | 'major' | 'class' | 'floor' | 'room' | 'bed',
    row: any
  ): void => {
    if (type === 'class') {
      // 从专业下钻到班级
      thirdLevelDrillDownType.value = 'class'
      thirdLevelDrillDownParentName.value = row.majorName
      thirdLevelDrillDownFilterParams.value = { majorCode: row.majorCode }
      thirdLevelDrillDownVisible.value = true
    }
  }

  /**
   * 处理楼层下钻事件（从楼层下钻到房间）
   */
  const handleFloorDrillDown = (
    type: 'department' | 'major' | 'class' | 'floor' | 'room' | 'bed',
    row: any
  ): void => {
    if (type === 'room') {
      // 从楼层下钻到房间
      floorNestedDrillDownType.value = 'room'
      floorNestedDrillDownParentName.value = row.floorName || row.floorCode
      floorNestedDrillDownFilterParams.value = {
        floorCode: row.floorCode,
        pageNum: 1
      }
      floorNestedDrillDownVisible.value = true
    }
  }

  /**
   * 处理楼层嵌套下钻事件（从房间下钻到床位）
   */
  const handleFloorNestedDrillDown = (
    type: 'department' | 'major' | 'class' | 'floor' | 'room' | 'bed',
    row: any
  ): void => {
    if (type === 'bed') {
      // 从房间下钻到床位
      floorThirdLevelDrillDownType.value = 'bed'
      floorThirdLevelDrillDownParentName.value = row.roomNumber || row.roomCode
      floorThirdLevelDrillDownFilterParams.value = {
        roomCode: row.roomCode,
        pageNum: 1
      }
      floorThirdLevelDrillDownVisible.value = true
    }
  }

  /**
   * 更新校区状态
   */
  const handleStatusChange = async (row: CampusListItem, value: boolean): Promise<void> => {
    // 如果是关闭操作（从启用变为停用），需要提示用户级联影响
    if (!value && row.status === 1) {
      try {
        let message = `确定要停用校区"${row.campusName}"吗？<br/>提示：停用校区后，该校区下的所有院系、专业、班级、楼栋、房间和床位也会被停用。`
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
      await fetchUpdateCampusStatus(row.id, value ? 1 : 0)
    } catch (error) {
      console.error('更新校区状态失败:', error)
      row.status = originalStatus
    } finally {
      row._statusLoading = false
    }
  }
</script>
