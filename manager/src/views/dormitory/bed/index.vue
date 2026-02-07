<!-- 床位管理页面 -->
<template>
  <div class="bed-page art-full-height">
    <!-- 搜索栏（仅表格视图显示） -->
    <BedSearch
      v-show="showSearchBar && currentView === 'table'"
      v-model="formFilters"
      @reset="handleReset"
      @search="handleSearch"
    />

    <ElCard
      class="art-table-card"
      shadow="never"
      :style="{ 'margin-top': showSearchBar && currentView === 'table' ? '12px' : '0' }"
    >
      <!-- 表格头部 -->
      <ArtTableHeader
        :showZebra="false"
        :loading="currentView === 'table' ? loading : visualLoading"
        :layout="
          currentView === 'table'
            ? 'search,refresh,size,fullscreen,columns,settings'
            : 'refresh,fullscreen'
        "
        v-model:columns="columnChecks"
        v-model:showSearchBar="showSearchBar"
        @refresh="handleRefresh"
      >
        <template #left>
          <ElSpace wrap>
            <!-- 视图切换器 -->
            <ViewSwitcher v-model="currentView" />

            <template v-if="currentView === 'table'">
              <ElButton @click="handleAdd" v-ripple v-permission="'system:bed:add'"
                >新增床位</ElButton
              >
              <ElButton
                :disabled="selectedCount === 0"
                @click="handleBatchDelete"
                v-ripple
                v-permission="'system:bed:delete'"
              >
                批量删除{{ selectedCount > 0 ? `(${selectedCount})` : '' }}
              </ElButton>
            </template>

            <!-- 可视化视图：无需额外按钮，楼层导航在左侧 -->
          </ElSpace>
        </template>
      </ArtTableHeader>

      <!-- 表格视图 -->
      <div v-show="currentView === 'table'" class="h-full">
        <ArtTable
          :loading="loading"
          :columns="columns"
          :data="data"
          :stripe="false"
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
      </div>

      <!-- 可视化视图（左右布局） -->
      <div v-show="currentView === 'visual'" class="visual-layout">
        <!-- 左侧：楼栋楼层导航 -->
        <BuildingFloorNav
          v-model="selectedFloorValue"
          :options="buildingOptions"
          @change="handleFloorChange"
        />

        <!-- 右侧：房间卡片区域 -->
        <div class="visual-layout__content">
          <DormVisualView
            ref="visualViewRef"
            :building-options="buildingOptions"
            :rooms="visualRooms"
            :loading="visualLoading"
            :show-stats="true"
            :show-view-switcher="false"
            :show-floor-selector="false"
            @room-click="handleVisualRoomClick"
            @bed-click="handleVisualBedClick"
            @empty-bed-click="handleEmptyBedClick"
          >
            <template #table>
              <!-- 空插槽，表格在外部显示 -->
            </template>
          </DormVisualView>
        </div>
      </div>

      <!-- 床位弹窗 -->
      <BedDialog
        v-model:visible="dialogVisible"
        :type="dialogType"
        :edit-data="editData"
        @submit="handleSubmit"
      />

      <!-- 学生选择弹窗（空床位分配） -->
      <StudentSelectDialog
        v-model:visible="studentSelectVisible"
        :bed-info="studentSelectBedInfo"
        :room-info="studentSelectRoomInfo"
        @success="handleStudentSelectSuccess"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { useTable } from '@/hooks/core/useTable'
  import BedDialog from './modules/bed-dialog.vue'
  import BedSearch from './modules/bed-search.vue'
  import {
    fetchGetBedPage,
    fetchDeleteBed,
    fetchBatchDeleteBed,
    fetchUpdateBedStatus,
    fetchGetRoomListWithBeds,
    fetchGetFloorList
  } from '@/api/dormitory-manage'
  import { ElPopover } from 'element-plus'
  import ArtSwitch from '@/components/core/forms/art-switch/index.vue'
  import StudentInfoPopover from '@/components/core/cards/art-student-info-popover/index.vue'
  import { enrichStudentInfo } from '@/utils/student/enrichStudentInfo'
  import { h, onMounted, watch } from 'vue'
  import {
    DormVisualView,
    ViewSwitcher,
    BuildingFloorNav,
    StudentSelectDialog,
    type ViewType,
    type RoomWithBeds,
    type BedWithStudent,
    type BuildingOption,
    type FloorSelectValue
  } from '@/components/core/dormitory'

  defineOptions({ name: 'Bed' })

  type BedListItem = Api.SystemManage.BedListItem & { _statusLoading?: boolean }

  // 状态管理
  const showSearchBar = ref(false)

  // ==================== 视图切换相关 ====================
  const currentView = ref<ViewType>('table')
  const visualViewRef = ref<InstanceType<typeof DormVisualView> | null>(null)

  // 可视化视图数据
  const selectedFloorValue = ref<FloorSelectValue>(null)
  const visualRooms = ref<RoomWithBeds[]>([])
  const visualLoading = ref(false)
  const buildingOptions = ref<BuildingOption[]>([])

  // 弹窗相关
  const dialogVisible = ref(false)
  const dialogType = ref<'add' | 'edit'>('add')
  const editData = ref<BedListItem | null>(null)

  // 学生选择弹窗相关（空床位分配）
  const studentSelectVisible = ref(false)
  const studentSelectBedInfo = ref<BedWithStudent | null>(null)
  const studentSelectRoomInfo = ref<RoomWithBeds | null>(null)

  // 批量选择
  const selectedRows = ref<BedListItem[]>([])
  const selectedIds = computed(() => selectedRows.value.map((item) => item.id))
  const selectedCount = computed(() => selectedRows.value.length)

  // 搜索相关
  const formFilters = ref({
    bedCode: '',
    bedNumber: '',
    locationCascader: [] as string[], // 级联选择器值 [校区, 楼层, 房间]
    roomCode: '',
    floorCode: '',
    campusCode: '',
    bedPosition: '',
    bedStatus: undefined,
    status: undefined,
    pageNum: 1
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
    refreshData,
    refreshCreate,
    refreshUpdate,
    refreshRemove,
    handleSizeChange,
    handleCurrentChange,
    contextMenuItems,
    contextMenuWidth,
    handleRowContextmenu,
    handleContextMenuSelect
  } = useTable<typeof fetchGetBedPage>({
    core: {
      apiFn: fetchGetBedPage,
      apiParams: computed(() => {
        return {
          bedCode: formFilters.value.bedCode || undefined,
          bedNumber: formFilters.value.bedNumber || undefined,
          roomCode: formFilters.value.roomCode || undefined,
          floorCode: formFilters.value.floorCode || undefined,
          campusCode: formFilters.value.campusCode || undefined,
          bedPosition: formFilters.value.bedPosition || undefined,
          bedStatus: formFilters.value.bedStatus,
          status: formFilters.value.status,
          pageNum: formFilters.value.pageNum
        } as Partial<Api.SystemManage.BedSearchParams>
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
          prop: 'bedCode',
          label: '床位编码',
          width: 120
        },
        {
          prop: 'bedNumber',
          label: '床位号',
          width: 100
        },
        {
          prop: 'roomNumber',
          label: '所属房间',
          minWidth: 120
        },
        {
          prop: 'floorName',
          label: '所属楼层',
          minWidth: 120
        },
        {
          prop: 'campusName',
          label: '所属校区',
          minWidth: 120
        },
        {
          prop: 'bedPositionText',
          label: '床位位置',
          width: 100
        },
        {
          prop: 'bedStatusText',
          label: '床位状态',
          width: 100
        },
        {
          prop: 'studentInfo.studentName',
          label: '入住学生',
          minWidth: 120,
          formatter: (row: BedListItem) => {
            const name = row.studentInfo?.studentName
            if (!name) {
              return h('span', name ?? '--')
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
                default: () => h(StudentInfoPopover, { student: row.studentInfo ?? {} }),
                reference: () =>
                  h(
                    'span',
                    {
                      class: 'cursor-pointer hover:underline',
                      style: { color: 'var(--el-color-primary)' }
                    },
                    name
                  )
              }
            )
          }
        },
        {
          prop: 'checkInDate',
          label: '入住日期',
          width: 120
        },
        {
          prop: 'status',
          label: '状态',
          width: 100,
          formatter: (row: BedListItem) => {
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
          width: 150,
          fixed: 'right' as const,
          formatter: (row: BedListItem) => [
            {
              type: 'edit',
              label: '编辑',
              onClick: () => handleEdit(row),
              auth: 'system:bed:edit'
            },
            {
              type: 'delete',
              label: '删除',
              onClick: () => handleDelete(row),
              auth: 'system:bed:delete',
              danger: true
            }
          ]
        }
      ],
      immediate: true
    },
    hooks: {
      onSuccess: async (tableData) => {
        // 补齐学生信息
        const enrichedData = await enrichStudentInfo(tableData)
        // 更新数据
        data.value = enrichedData
      }
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
    formFilters.value.pageNum = 1
    await getData()
  }

  /**
   * 重置搜索
   */
  const handleReset = async (): Promise<void> => {
    formFilters.value = {
      bedCode: '',
      bedNumber: '',
      locationCascader: [],
      roomCode: '',
      floorCode: '',
      campusCode: '',
      bedPosition: '',
      bedStatus: undefined,
      status: undefined,
      pageNum: 1
    }
    await resetSearchParams()
  }

  /**
   * 刷新数据
   */
  const handleRefresh = (): void => {
    if (currentView.value === 'table') {
      refreshData()
    } else if (selectedFloorValue.value) {
      loadVisualRooms(selectedFloorValue.value)
    }
  }

  /**
   * 新增床位
   */
  const handleAdd = (): void => {
    dialogType.value = 'add'
    editData.value = null
    dialogVisible.value = true
  }

  /**
   * 编辑床位
   */
  const handleEdit = (row: BedListItem): void => {
    dialogType.value = 'edit'
    editData.value = { ...row }
    dialogVisible.value = true
  }

  /**
   * 删除床位
   */
  const handleDelete = async (row: BedListItem): Promise<void> => {
    try {
      await ElMessageBox.confirm(
        `确定要删除床位"${row.bedNumber || row.bedCode}"吗？`,
        '删除确认',
        {
          type: 'warning',
          confirmButtonText: '确定删除',
          cancelButtonText: '取消'
        }
      )
      await fetchDeleteBed(row.id)
      await refreshRemove()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除床位失败:', error)
      }
    }
  }

  /**
   * 处理表格行选择变化
   */
  const handleSelectionChange = (selection: BedListItem[]): void => {
    selectedRows.value = selection
  }

  /**
   * 批量删除床位
   */
  const handleBatchDelete = async (): Promise<void> => {
    if (selectedCount.value === 0) {
      ElMessage.warning('请至少选择一条数据')
      return
    }

    try {
      await ElMessageBox.confirm(
        `确定要批量删除选中的 ${selectedCount.value} 条床位数据吗？`,
        '批量删除确认',
        {
          type: 'warning',
          confirmButtonText: '确定删除',
          cancelButtonText: '取消'
        }
      )
      await fetchBatchDeleteBed(selectedIds.value as number[])
      selectedRows.value = []
      await refreshRemove()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('批量删除床位失败:', error)
      }
    }
  }

  /**
   * 状态切换
   */
  const handleStatusChange = async (row: BedListItem, enabled: boolean): Promise<void> => {
    const originalStatus = row.status
    try {
      row._statusLoading = true
      const status = enabled ? 1 : 0
      await ElMessageBox.confirm(
        `确定要${enabled ? '启用' : '停用'}床位"${row.bedNumber || row.bedCode}"吗？`,
        '状态确认',
        {
          type: 'warning',
          confirmButtonText: '确定',
          cancelButtonText: '取消'
        }
      )
      row.status = status
      await fetchUpdateBedStatus(row.id, status)
    } catch (error) {
      if (error !== 'cancel') {
        console.error('状态切换失败:', error)
        row.status = originalStatus
      } else {
        row.status = originalStatus
      }
    } finally {
      row._statusLoading = false
    }
  }

  /**
   * 弹窗提交
   */
  const handleSubmit = async (): Promise<void> => {
    dialogVisible.value = false
    if (dialogType.value === 'add') {
      await refreshCreate()
    } else {
      await refreshUpdate()
    }
  }

  // ==================== 可视化视图相关方法 ====================

  /**
   * 加载楼栋楼层选项
   */
  const loadBuildingOptions = async (): Promise<void> => {
    try {
      const res = await fetchGetFloorList({ status: 1 })
      // 按楼栋编码分组转换为楼栋选项格式（楼栋 -> 楼层）
      const floorList = res?.list || []
      const buildingMap = new Map<string, BuildingOption>()

      floorList.forEach((floor: any) => {
        const buildingKey = floor.floorCode
        if (!buildingKey) return

        if (!buildingMap.has(buildingKey)) {
          buildingMap.set(buildingKey, {
            id: buildingKey,
            name: floor.floorName || floor.floorCode,
            code: floor.floorCode,
            campusCode: floor.campusCode,
            floors: []
          })
        }
        const building = buildingMap.get(buildingKey)!

        const totalFloors = Number(floor.floorNumber) || 1

        // 注入“全部层数”（仅一次，放在最前面）
        if (
          !building.floors.some((f) => typeof f.id === 'string' && String(f.id).startsWith('all:'))
        ) {
          building.floors.push({
            id: `all:${buildingKey}`,
            name: '全部层数',
            code: buildingKey
          })
        }

        // 追加每一层：第 n 层（从高到低）
        for (let level = totalFloors; level >= 1; level--) {
          const id = `floor:${floor.id}:${level}`
          if (!building.floors.some((f) => f.id === id)) {
            building.floors.push({
              id,
              name: `第 ${level} 层`,
              code: buildingKey,
              floorNumber: level
            })
          }
        }
      })

      // 排序：楼栋按名称
      buildingOptions.value = Array.from(buildingMap.values()).sort((a, b) =>
        (a.name || a.id).localeCompare(b.name || b.id)
      )
    } catch (error) {
      console.error('加载楼栋选项失败:', error)
    }
  }

  /**
   * 楼层选择变更
   */
  const handleFloorChange = async (value: FloorSelectValue): Promise<void> => {
    selectedFloorValue.value = value
    if (value) {
      await loadVisualRooms(value)
    } else {
      visualRooms.value = []
    }
  }

  /**
   * 加载可视化房间数据
   */
  const loadVisualRooms = async (value: FloorSelectValue): Promise<void> => {
    visualLoading.value = true
    try {
      if (!value) {
        visualRooms.value = []
        return
      }

      const normalizeRooms = (rooms: any[]) =>
        (rooms || []).map((room) => ({
          ...room,
          roomStatus: room.roomStatus ?? 1,
          bedCount: room.bedCount ?? 0,
          currentOccupancy: room.currentOccupancy ?? 0,
          beds: (room.beds || []).map((bed: any) => ({
            ...bed,
            bedStatus: bed.bedStatus ?? 1
          }))
        })) as RoomWithBeds[]

      // 兼容旧逻辑：直接按楼层ID查询（不区分楼层数）
      if (typeof value === 'number') {
        const res = await fetchGetRoomListWithBeds(value)
        visualRooms.value = normalizeRooms(res || [])
        return
      }

      // 选择具体“第 n 层”
      if (typeof value === 'string' && value.startsWith('floor:')) {
        const [, floorIdStr, levelStr] = value.split(':')
        const floorId = Number(floorIdStr)
        const level = Number(levelStr)
        const res = await fetchGetRoomListWithBeds(floorId)
        visualRooms.value = normalizeRooms(res || []).filter((room) => room.floorNumber === level)
        return
      }

      // 某楼栋的“全部层数”
      if (typeof value === 'string' && value.startsWith('all:')) {
        const buildingCode = value.slice(4)
        const building = buildingOptions.value.find((b) => (b.code || b.id) === buildingCode)
        const anyFloor = (building?.floors || []).find(
          (f) => typeof f.id === 'string' && String(f.id).startsWith('floor:')
        )
        if (!anyFloor) {
          visualRooms.value = []
          return
        }
        const [, floorIdStr] = String(anyFloor.id).split(':')
        const floorId = Number(floorIdStr)
        const res = await fetchGetRoomListWithBeds(floorId)
        visualRooms.value = normalizeRooms(res || [])
        return
      }
    } catch (error) {
      console.error('加载房间数据失败:', error)
      visualRooms.value = []
    } finally {
      visualLoading.value = false
    }
  }

  /**
   * 可视化视图：点击房间
   */
  const handleVisualRoomClick = (room: RoomWithBeds): void => {
    console.log('点击房间:', room)
    // 可以在这里打开房间详情弹窗
  }

  /**
   * 可视化视图：点击床位（非空床位）
   */
  const handleVisualBedClick = (bed: BedWithStudent, room: RoomWithBeds): void => {
    // 打开床位编辑弹窗
    dialogType.value = 'edit'
    editData.value = {
      ...bed,
      roomId: room.id,
      roomCode: room.roomCode,
      roomNumber: room.roomNumber
    } as BedListItem
    dialogVisible.value = true
  }

  /**
   * 可视化视图：点击空床位（分配学生）
   */
  const handleEmptyBedClick = (bed: BedWithStudent, room: RoomWithBeds): void => {
    studentSelectBedInfo.value = bed
    studentSelectRoomInfo.value = room
    studentSelectVisible.value = true
  }

  /**
   * 学生选择成功回调
   */
  const handleStudentSelectSuccess = async (): Promise<void> => {
    // 刷新可视化视图数据
    if (selectedFloorValue.value) {
      await loadVisualRooms(selectedFloorValue.value)
    }
    // 同时刷新表格数据
    await refreshData()
  }

  // ==================== 生命周期 ====================

  onMounted(async () => {
    // 加载楼栋选项（用于可视化视图）
    await loadBuildingOptions()

    // 切换到平面图时：默认选择“全部楼层”并加载（若用户已选择则不覆盖）
    if (
      currentView.value === 'visual' &&
      !selectedFloorValue.value &&
      buildingOptions.value.length > 0
    ) {
      const code = buildingOptions.value[0].code || buildingOptions.value[0].id
      selectedFloorValue.value = `all:${code}`
      await loadVisualRooms(selectedFloorValue.value)
    }
  })

  watch(
    () => currentView.value,
    async (view) => {
      if (view !== 'visual') return
      if (selectedFloorValue.value) return
      if (buildingOptions.value.length === 0) return
      const code = buildingOptions.value[0].code || buildingOptions.value[0].id
      selectedFloorValue.value = `all:${code}`
      await loadVisualRooms(selectedFloorValue.value)
    }
  )
</script>

<style lang="scss" scoped>
  // 可视化视图左右布局
  .visual-layout {
    display: flex;
    height: calc(100vh - 200px);
    min-height: 400px;
    margin: -20px;
    margin-top: 0;

    &__content {
      display: flex;
      flex: 1;
      flex-direction: column;
      min-width: 0;
      padding: 16px;
      padding-top: 8px;
      overflow: hidden; // 由子组件 DormVisualView 处理滚动
    }
  }
</style>
