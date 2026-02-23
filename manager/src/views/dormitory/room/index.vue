<!-- 房间管理页面 -->
<template>
  <div class="room-page art-full-height">
    <!-- 搜索栏（仅表格视图显示） -->
    <RoomSearch
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
              <ElButton @click="handleAdd" v-ripple v-permission="'system:room:add'"
                >新增房间</ElButton
              >
              <ElButton
                :disabled="selectedCount === 0"
                @click="handleBatchDelete"
                v-ripple
                v-permission="'system:room:delete'"
              >
                批量删除{{ selectedCount > 0 ? `(${selectedCount})` : '' }}
              </ElButton>
            </template>
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

      <!-- 房间弹窗 -->
      <RoomDialog
        v-model:visible="dialogVisible"
        :type="dialogType"
        :edit-data="editData"
        @submit="handleSubmit"
      />

      <!-- 下钻弹框（床位） -->
      <DrillDownDialog
        v-model:visible="drillDownVisible"
        :drill-type="drillDownType"
        :parent-name="drillDownParentName"
        :filter-params="drillDownFilterParams"
        :key="`${drillDownType}-${drillDownVisible}`"
      />

      <!-- 批量增加床位弹框 -->
      <BatchBedDialog
        v-model:visible="batchBedDialogVisible"
        :room-data="batchBedRoomData"
        @submit="handleBatchBedSubmit"
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
  import RoomDialog from './modules/room-dialog.vue'
  import RoomSearch from './modules/room-search.vue'
  import BatchBedDialog from './modules/batch-bed-dialog.vue'
  import {
    fetchGetRoomPage,
    fetchDeleteRoom,
    fetchBatchDeleteRoom,
    fetchUpdateRoomStatus,
    fetchGetRoomListWithBeds,
    fetchGetFloorList
  } from '@/api/dormitory-manage'
  import { useReferenceStore } from '@/store/modules/reference'
  import DrillDownDialog from '@/components/school/DrillDownDialog.vue'
  import ArtSwitch from '@/components/core/forms/art-switch/index.vue'
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

  // 使用参考数据 store（用于缓存失效）
  const referenceStore = useReferenceStore()

  defineOptions({ name: 'Room' })

  type RoomListItem = Api.SystemManage.RoomListItem & { _statusLoading?: boolean }

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
  const editData = ref<RoomListItem | null>(null)

  // 下钻弹框相关
  const drillDownVisible = ref(false)
  const drillDownType = ref<'bed'>('bed')
  const drillDownParentName = ref('')
  const drillDownFilterParams = ref<Record<string, any>>({})

  // 批量增加床位弹框相关
  const batchBedDialogVisible = ref(false)
  const batchBedRoomData = ref<RoomListItem | null>(null)

  // 学生选择弹窗相关（空床位分配）
  const studentSelectVisible = ref(false)
  const studentSelectBedInfo = ref<BedWithStudent | null>(null)
  const studentSelectRoomInfo = ref<RoomWithBeds | null>(null)

  // 批量选择
  const selectedRows = ref<RoomListItem[]>([])
  const selectedIds = computed(() => selectedRows.value.map((item) => item.id))
  const selectedCount = computed(() => selectedRows.value.length)

  // 搜索相关
  const formFilters = ref({
    roomCode: '',
    roomNumber: '',
    locationCascader: [] as string[], // 级联选择器值 [校区, 楼层]
    floorCode: '',
    campusCode: '',
    roomType: '',
    roomStatus: undefined,
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
  } = useTable<typeof fetchGetRoomPage>({
    core: {
      apiFn: fetchGetRoomPage,
      apiParams: computed(() => {
        return {
          roomCode: formFilters.value.roomCode || undefined,
          roomNumber: formFilters.value.roomNumber || undefined,
          floorCode: formFilters.value.floorCode || undefined,
          campusCode: formFilters.value.campusCode || undefined,
          roomType: formFilters.value.roomType || undefined,
          roomStatus: formFilters.value.roomStatus,
          status: formFilters.value.status,
          pageNum: formFilters.value.pageNum
        } as Partial<Api.SystemManage.RoomSearchParams>
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
          prop: 'roomCode',
          label: '房间编码',
          width: 120
        },
        {
          prop: 'roomNumber',
          label: '房间号',
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
          prop: 'floorNumber',
          label: '楼层数',
          width: 100,
          formatter: (row: RoomListItem) => {
            return row.floorNumber ? `${row.floorNumber}层` : '-'
          }
        },
        {
          prop: 'roomTypeText',
          label: '房间类型',
          width: 100
        },
        {
          prop: 'bedCount',
          label: '床位数',
          width: 100
        },
        {
          prop: 'currentOccupancy',
          label: '入住人数',
          width: 100
        },
        {
          prop: 'roomStatusText',
          label: '房间状态',
          width: 100
        },
        {
          prop: 'facilities',
          label: '设施',
          minWidth: 160,
          formatter: (row: RoomListItem) => {
            const facilities = []
            if (row.hasAirConditioner === 1) facilities.push('空调')
            if (row.hasBathroom === 1) facilities.push('卫生间')
            if (row.hasBalcony === 1) facilities.push('阳台')
            return facilities.length > 0 ? facilities.join(' / ') : '-'
          }
        },
        {
          prop: 'status',
          label: '状态',
          width: 100,
          formatter: (row: RoomListItem) => {
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
          formatter: (row: RoomListItem) => [
            { type: 'view', label: '查看床位', onClick: () => handleViewBeds(row) },
            {
              type: 'edit',
              label: '编辑',
              onClick: () => handleEdit(row),
              auth: 'system:room:edit'
            },
            {
              type: 'add',
              label: '批量增加',
              onClick: () => handleBatchAddBeds(row),
              auth: 'system:room:batchAdd'
            },
            {
              type: 'delete',
              label: '删除',
              onClick: () => handleDelete(row),
              auth: 'system:room:delete',
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
    performance: {
      enableCache: true,
      cacheTime: 3 * 60 * 1000
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
      roomCode: '',
      roomNumber: '',
      locationCascader: [],
      floorCode: '',
      campusCode: '',
      roomType: '',
      roomStatus: undefined,
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
   * 新增房间
   */
  const handleAdd = (): void => {
    dialogType.value = 'add'
    editData.value = null
    dialogVisible.value = true
  }

  /**
   * 编辑房间
   */
  const handleEdit = (row: RoomListItem): void => {
    dialogType.value = 'edit'
    editData.value = { ...row }
    dialogVisible.value = true
  }

  /**
   * 删除房间
   */
  const handleDelete = async (row: RoomListItem): Promise<void> => {
    try {
      await ElMessageBox.confirm(
        `确定要删除房间"${row.roomNumber || row.roomCode}"吗？<br/>提示：删除房间后，该房间下的所有床位也会被删除。`,
        '删除确认',
        {
          type: 'warning',
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          dangerouslyUseHTMLString: true
        }
      )
      await fetchDeleteRoom(row.id)
      // 刷新房间列表缓存
      await referenceStore.refreshRoomList()
      await refreshRemove()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除房间失败:', error)
      }
    }
  }

  /**
   * 处理表格行选择变化
   */
  const handleSelectionChange = (selection: RoomListItem[]): void => {
    selectedRows.value = selection
  }

  /**
   * 批量删除房间
   */
  const handleBatchDelete = async (): Promise<void> => {
    if (selectedCount.value === 0) {
      ElMessage.warning('请至少选择一条数据')
      return
    }

    try {
      await ElMessageBox.confirm(
        `确定要批量删除选中的 ${selectedCount.value} 条房间数据吗？<br/>提示：删除房间后，这些房间下的所有床位也会被删除。`,
        '批量删除确认',
        {
          type: 'warning',
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          dangerouslyUseHTMLString: true
        }
      )
      await fetchBatchDeleteRoom(selectedIds.value as number[])
      // 刷新房间列表缓存
      await referenceStore.refreshRoomList()
      selectedRows.value = []
      await refreshRemove()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('批量删除房间失败:', error)
      }
    }
  }

  /**
   * 状态切换
   */
  const handleStatusChange = async (row: RoomListItem, enabled: boolean): Promise<void> => {
    // 如果是关闭操作（从启用变为停用），需要提示用户级联影响
    if (!enabled && row.status === 1) {
      try {
        let message = `确定要停用房间"${row.roomNumber || row.roomCode}"吗？<br/>提示：停用房间后，该房间下的所有床位也会被停用。`
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
      row.status = enabled ? 1 : 0
      await fetchUpdateRoomStatus(row.id, enabled ? 1 : 0)
    } catch (error) {
      console.error('更新房间状态失败:', error)
      row.status = originalStatus
    } finally {
      row._statusLoading = false
    }
  }

  /**
   * 弹窗提交
   */
  const handleSubmit = async (): Promise<void> => {
    dialogVisible.value = false
    // 刷新房间列表缓存
    await referenceStore.refreshRoomList()
    if (dialogType.value === 'add') {
      await refreshCreate()
    } else {
      await refreshUpdate()
    }
  }

  /**
   * 查看床位
   */
  const handleViewBeds = (row: RoomListItem): void => {
    drillDownType.value = 'bed'
    drillDownParentName.value = row.roomNumber || row.roomCode
    drillDownFilterParams.value = { roomCode: row.roomCode, pageNum: 1 }
    drillDownVisible.value = true
  }

  /**
   * 批量增加床位
   */
  const handleBatchAddBeds = (row: RoomListItem): void => {
    batchBedRoomData.value = { ...row }
    batchBedDialogVisible.value = true
  }

  /**
   * 批量增加床位提交回调
   */
  const handleBatchBedSubmit = async (): Promise<void> => {
    batchBedDialogVisible.value = false
    // 刷新房间数据
    await refreshData()
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
    // 打开查看床位的下钻弹窗
    drillDownType.value = 'bed'
    drillDownParentName.value = room.roomNumber || room.roomCode
    drillDownFilterParams.value = { roomCode: room.roomCode, pageNum: 1 }
    drillDownVisible.value = true
  }

  /**
   * 可视化视图：点击床位（非空床位）
   */
  const handleVisualBedClick = (bed: BedWithStudent, room: RoomWithBeds): void => {
    // 非空床位点击：打开下钻弹窗查看床位信息
    drillDownType.value = 'bed'
    drillDownParentName.value = room.roomNumber || room.roomCode
    drillDownFilterParams.value = { roomCode: room.roomCode, pageNum: 1 }
    drillDownVisible.value = true
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
    margin-top: 0;
    margin-left: -20px;

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
