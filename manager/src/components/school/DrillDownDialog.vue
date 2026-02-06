<!-- 数据下钻弹框组件 -->
<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="90%"
    top="5vmin"
    append-to-body
    :close-on-click-modal="false"
    destroy-on-close
    @close="handleClose"
  >
    <div class="drill-down-dialog-content">
      <!-- 院系表格 -->
      <template v-if="drillType === 'department'">
        <ArtTable
          ref="tableRef"
          row-key="id"
          :loading="loading"
          :columns="departmentColumns"
          :data="data"
          :pagination="pagination"
          :stripe="false"
          :showTableHeader="false"
          height="100%"
          @pagination:size-change="handleSizeChange"
          @pagination:current-change="handleCurrentChange"
        />
      </template>

      <!-- 专业表格 -->
      <template v-else-if="drillType === 'major'">
        <ArtTable
          :loading="loading"
          :data="data"
          :columns="majorColumns"
          :pagination="pagination"
          :showTableHeader="false"
          height="100%"
          @pagination:size-change="handleSizeChange"
          @pagination:current-change="handleCurrentChange"
        />
      </template>

      <!-- 班级表格 -->
      <template v-else-if="drillType === 'class'">
        <ArtTable
          :loading="loading"
          :data="data"
          :columns="classColumns"
          :pagination="pagination"
          :showTableHeader="false"
          height="100%"
          @pagination:size-change="handleSizeChange"
          @pagination:current-change="handleCurrentChange"
        />
      </template>

      <!-- 楼层表格 -->
      <template v-else-if="drillType === 'floor'">
        <ArtTable
          :loading="loading"
          :data="data"
          :columns="floorColumns"
          :pagination="pagination"
          :showTableHeader="false"
          height="100%"
          @pagination:size-change="handleSizeChange"
          @pagination:current-change="handleCurrentChange"
        />
      </template>

      <!-- 房间表格 -->
      <template v-else-if="drillType === 'room'">
        <ArtTable
          :loading="loading"
          :data="data"
          :columns="roomColumns"
          :pagination="pagination"
          :showTableHeader="false"
          height="100%"
          @pagination:size-change="handleSizeChange"
          @pagination:current-change="handleCurrentChange"
        />
      </template>

      <!-- 床位表格 -->
      <template v-else-if="drillType === 'bed'">
        <ArtTable
          :loading="loading"
          :data="data"
          :columns="bedColumns"
          :pagination="pagination"
          :showTableHeader="false"
          height="100%"
          @pagination:size-change="handleSizeChange"
          @pagination:current-change="handleCurrentChange"
        />
      </template>
    </div>
  </ElDialog>
</template>

<script setup lang="ts">
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import { fetchGetDepartmentPage, fetchGetMajorPage, fetchGetClassPage } from '@/api/school-manage'
  import { fetchGetFloorPage, fetchGetRoomPage, fetchGetBedPage } from '@/api/dormitory-manage'
  import { defaultResponseAdapter } from '@/utils/table/tableUtils'
  import { h } from 'vue'

  defineOptions({ name: 'DrillDownDialog' })

  type DrillDownType = 'department' | 'major' | 'class' | 'floor' | 'room' | 'bed'

  interface Props {
    visible: boolean
    drillType: DrillDownType
    parentName: string
    filterParams: Record<string, any>
  }

  const props = withDefaults(defineProps<Props>(), {
    visible: false,
    drillType: 'department',
    parentName: '',
    filterParams: () => ({})
  })

  const emit = defineEmits<{
    'update:visible': [value: boolean]
    'drill-down': [type: DrillDownType, row: any]
  }>()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (val) => emit('update:visible', val)
  })

  const dialogTitle = computed(() => {
    const typeMap = {
      department: '查看院系',
      major: '查看专业',
      class: '查看班级',
      floor: '查看楼层',
      room: '查看房间',
      bed: '查看床位'
    }
    const title = typeMap[props.drillType] || '查看数据'
    return props.parentName ? `${title} - ${props.parentName}` : title
  })

  const tableRef = ref()

  // 院系列表项类型
  type DepartmentListItem = Api.SystemManage.DepartmentListItem
  // 专业列表项类型
  type MajorListItem = Api.SystemManage.MajorListItem
  // 班级列表项类型
  type ClassListItem = Api.SystemManage.ClassListItem
  // 楼层列表项类型
  type FloorListItem = Api.SystemManage.FloorListItem
  // 房间列表项类型
  type RoomListItem = Api.SystemManage.RoomListItem
  // 床位列表项类型
  type BedListItem = Api.SystemManage.BedListItem

  /**
   * 处理下钻
   */
  const handleDrillDown = (type: DrillDownType, row: any): void => {
    // 触发事件，让父组件处理嵌套下钻
    emit('drill-down', type, row)
  }

  /**
   * 获取院系表格列配置
   */
  const getDepartmentColumns = () => [
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
      minWidth: 120
    },
    {
      prop: 'status',
      label: '状态',
      width: 100,
      formatter: (row: DepartmentListItem) => {
        return row.status === 1 ? '启用' : '停用'
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
      width: 120,
      fixed: 'right' as const,
      formatter: (row: DepartmentListItem) => {
        return h('div', { class: 'flex gap-1' }, [
          h(ArtButtonTable, {
            type: 'view',
            onClick: () => handleDrillDown('major', row)
          })
        ])
      }
    }
  ]

  /**
   * 获取专业表格列配置
   */
  const getMajorColumns = () => [
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
        return row.status === 1 ? '启用' : '停用'
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
      width: 120,
      fixed: 'right' as const,
      formatter: (row: MajorListItem) => {
        return h('div', { class: 'flex gap-1' }, [
          h(ArtButtonTable, {
            type: 'view',
            onClick: () => handleDrillDown('class', row)
          })
        ])
      }
    }
  ]

  /**
   * 获取班级表格列配置
   */
  const getClassColumns = () => [
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
      prop: 'teacher',
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
        return row.status === 1 ? '启用' : '停用'
      }
    },
    {
      prop: 'createTime',
      label: '创建时间',
      width: 180,
      sortable: true
    }
  ]

  /**
   * 获取楼层表格列配置
   */
  const getFloorColumns = () => [
    {
      prop: 'floorCode',
      label: '楼层编码',
      minWidth: 120
    },
    {
      prop: 'floorName',
      label: '楼层名称',
      minWidth: 120
    },
    {
      prop: 'floorNumber',
      label: '楼层数',
      width: 100
    },
    {
      prop: 'campusName',
      label: '所属校区',
      minWidth: 120
    },
    {
      prop: 'genderTypeText',
      label: '性别类型',
      width: 100
    },
    {
      prop: 'totalRooms',
      label: '房间数',
      width: 100
    },
    {
      prop: 'totalBeds',
      label: '床位数',
      width: 100
    },
    {
      prop: 'currentOccupancy',
      label: '入住人数',
      width: 100
    },
    {
      prop: 'status',
      label: '状态',
      width: 100,
      formatter: (row: FloorListItem) => {
        return row.status === 1 ? '启用' : '停用'
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
      width: 120,
      fixed: 'right' as const,
      formatter: (row: FloorListItem) => {
        return h('div', { class: 'flex gap-1' }, [
          h(ArtButtonTable, {
            type: 'view',
            onClick: () => handleDrillDown('room', row)
          })
        ])
      }
    }
  ]

  /**
   * 获取房间表格列配置
   */
  const getRoomColumns = () => [
    {
      prop: 'roomCode',
      label: '房间编码',
      minWidth: 120
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
      prop: 'status',
      label: '状态',
      width: 100,
      formatter: (row: RoomListItem) => {
        return row.status === 1 ? '启用' : '停用'
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
      width: 120,
      fixed: 'right' as const,
      formatter: (row: RoomListItem) => {
        return h('div', { class: 'flex gap-1' }, [
          h(ArtButtonTable, {
            type: 'view',
            onClick: () => handleDrillDown('bed', row)
          })
        ])
      }
    }
  ]

  /**
   * 获取床位表格列配置
   */
  const getBedColumns = () => [
    {
      prop: 'bedCode',
      label: '床位编码',
      minWidth: 120
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
      prop: 'studentName',
      label: '入住学生',
      minWidth: 120
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
        return row.status === 1 ? '启用' : '停用'
      }
    },
    {
      prop: 'createTime',
      label: '创建时间',
      width: 180,
      sortable: true
    }
  ]

  // 根据类型选择API和配置
  const getApiConfig = () => {
    switch (props.drillType) {
      case 'department':
        return {
          apiFn: fetchGetDepartmentPage,
          apiParams: computed(
            () =>
              ({
                ...props.filterParams
              }) as Partial<Api.SystemManage.DepartmentSearchParams>
          ),
          paginationKey: {
            current: 'pageNum',
            size: 'pageSize'
          },
          columnsFactory: () => getDepartmentColumns()
        }
      case 'major':
        return {
          apiFn: fetchGetMajorPage,
          apiParams: computed(
            () =>
              ({
                ...props.filterParams
              }) as Partial<Api.SystemManage.MajorSearchParams>
          ),
          paginationKey: {
            current: 'pageNum',
            size: 'pageSize'
          },
          columnsFactory: () => getMajorColumns()
        }
      case 'class':
        return {
          apiFn: fetchGetClassPage,
          apiParams: computed(
            () =>
              ({
                ...props.filterParams
              }) as Partial<Api.SystemManage.ClassSearchParams>
          ),
          paginationKey: {
            current: 'pageNum',
            size: 'pageSize'
          },
          columnsFactory: () => getClassColumns()
        }
      case 'floor':
        return {
          apiFn: fetchGetFloorPage,
          apiParams: computed(
            () =>
              ({
                ...props.filterParams
              }) as Partial<Api.SystemManage.FloorSearchParams>
          ),
          paginationKey: {
            current: 'pageNum',
            size: 'pageSize'
          },
          columnsFactory: () => getFloorColumns()
        }
      case 'room':
        return {
          apiFn: fetchGetRoomPage,
          apiParams: computed(
            () =>
              ({
                ...props.filterParams
              }) as Partial<Api.SystemManage.RoomSearchParams>
          ),
          paginationKey: {
            current: 'pageNum',
            size: 'pageSize'
          },
          columnsFactory: () => getRoomColumns()
        }
      case 'bed':
        return {
          apiFn: fetchGetBedPage,
          apiParams: computed(
            () =>
              ({
                ...props.filterParams
              }) as Partial<Api.SystemManage.BedSearchParams>
          ),
          paginationKey: {
            current: 'pageNum',
            size: 'pageSize'
          },
          columnsFactory: () => getBedColumns()
        }
      default:
        throw new Error(`Unknown drill type: ${props.drillType}`)
    }
  }

  const { data, loading, pagination, getData, handleSizeChange, handleCurrentChange } =
    useTable<any>({
      core: {
        ...getApiConfig(),
        immediate: false // 禁用自动加载，由 watch 控制何时加载数据
      } as any,
      transform: {
        responseAdapter: (response: any) => {
          // 处理可能的嵌套 data 字段
          const actualResponse = response?.data || response

          // 院系分页响应
          if (props.drillType === 'department') {
            const list = actualResponse?.list || response?.list || []
            return {
              records: list,
              total: actualResponse?.total || response?.total || 0,
              current:
                actualResponse?.current ||
                actualResponse?.pageNum ||
                response?.current ||
                response?.pageNum ||
                1,
              size:
                actualResponse?.size ||
                actualResponse?.pageSize ||
                response?.size ||
                response?.pageSize ||
                20
            }
          }
          // 专业分页响应使用 list 字段
          if (props.drillType === 'major') {
            const list = actualResponse?.list || response?.list || []
            return {
              records: list,
              total: actualResponse?.total || response?.total || 0,
              current:
                actualResponse?.current ||
                actualResponse?.pageNum ||
                response?.current ||
                response?.pageNum ||
                1,
              size:
                actualResponse?.size ||
                actualResponse?.pageSize ||
                response?.size ||
                response?.pageSize ||
                20
            }
          }
          // 班级分页响应使用 list 字段
          if (props.drillType === 'class') {
            const list = actualResponse?.list || response?.list || []
            return {
              records: list,
              total: actualResponse?.total || response?.total || 0,
              current:
                actualResponse?.current ||
                actualResponse?.pageNum ||
                response?.current ||
                response?.pageNum ||
                1,
              size:
                actualResponse?.size ||
                actualResponse?.pageSize ||
                response?.size ||
                response?.pageSize ||
                20
            }
          }
          // 楼层分页响应使用 list 字段
          if (props.drillType === 'floor') {
            const list = actualResponse?.list || response?.list || []
            return {
              records: list,
              total: actualResponse?.total || response?.total || 0,
              current:
                actualResponse?.current ||
                actualResponse?.pageNum ||
                response?.current ||
                response?.pageNum ||
                1,
              size:
                actualResponse?.size ||
                actualResponse?.pageSize ||
                response?.size ||
                response?.pageSize ||
                20
            }
          }
          // 房间分页响应使用 list 字段
          if (props.drillType === 'room') {
            const list = actualResponse?.list || response?.records || []
            return {
              records: list,
              total: actualResponse?.total || response?.total || 0,
              current:
                actualResponse?.current ||
                actualResponse?.pageNum ||
                response?.current ||
                response?.pageNum ||
                1,
              size:
                actualResponse?.size ||
                actualResponse?.pageSize ||
                response?.size ||
                response?.pageSize ||
                20
            }
          }
          // 床位分页响应使用 list 字段
          if (props.drillType === 'bed') {
            const list = actualResponse?.list || response?.list || []
            return {
              records: list,
              total: actualResponse?.total || response?.total || 0,
              current:
                actualResponse?.current ||
                actualResponse?.pageNum ||
                response?.current ||
                response?.pageNum ||
                1,
              size:
                actualResponse?.size ||
                actualResponse?.pageSize ||
                response?.size ||
                response?.pageSize ||
                20
            }
          }
          // 默认使用标准适配器
          return defaultResponseAdapter(response)
        }
      }
    })

  // 院系表格列配置
  const departmentColumns = computed(() => {
    if (props.drillType !== 'department') return []
    return getDepartmentColumns()
  })

  // 专业表格列配置
  const majorColumns = computed(() => {
    if (props.drillType !== 'major') return []
    return getMajorColumns()
  })

  // 班级表格列配置
  const classColumns = computed(() => {
    if (props.drillType !== 'class') return []
    return getClassColumns()
  })

  // 楼层表格列配置
  const floorColumns = computed(() => {
    if (props.drillType !== 'floor') return []
    return getFloorColumns()
  })

  // 房间表格列配置
  const roomColumns = computed(() => {
    if (props.drillType !== 'room') return []
    return getRoomColumns()
  })

  // 床位表格列配置
  const bedColumns = computed(() => {
    if (props.drillType !== 'bed') return []
    return getBedColumns()
  })

  /**
   * 关闭弹框
   */
  const handleClose = (): void => {
    emit('update:visible', false)
  }

  // 监听visible和filterParams变化，重新加载数据
  watch(
    () => [props.visible, props.filterParams],
    ([newVisible, newParams], oldValue) => {
      const [oldVisible, oldParams] = oldValue || [undefined, undefined]
      // 只在弹窗打开时加载数据
      if (newVisible && (!oldVisible || JSON.stringify(newParams) !== JSON.stringify(oldParams))) {
        nextTick(() => {
          getData()
        })
      }
    },
    { deep: true, immediate: true }
  )
</script>

<style scoped lang="scss">
  .drill-down-dialog-content {
    height: 800px;
    min-height: 600px;
  }
</style>
