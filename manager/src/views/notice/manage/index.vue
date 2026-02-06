<!-- 通知管理页面 -->
<template>
  <div class="notice-page art-full-height">
    <!-- 搜索栏 -->
    <NoticeSearch
      v-show="showSearchBar"
      v-model="formFilters"
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
            <ElButton @click="showDialog('add')" v-ripple v-permission="'system:notice:add'">
              新增通知
            </ElButton>
            <ElButton
              :disabled="selectedCount === 0"
              @click="handleBatchDelete"
              v-ripple
              v-permission="'system:notice:delete'"
            >
              批量删除{{ selectedCount > 0 ? `(${selectedCount})` : '' }}
            </ElButton>
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
        @selection-change="handleSelectionChange"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      >
      </ArtTable>

      <!-- 详情抽屉 -->
      <NoticeDrawer
        v-show="drawerVisible"
        v-model:visible="drawerVisible"
        :notice-id="currentNoticeId"
        :notice-data="currentNoticeData"
        @edit="handleDrawerEdit"
      />

      <!-- 编辑对话框 -->
      <NoticeDialog
        v-show="dialogVisible"
        v-model:visible="dialogVisible"
        :type="dialogType"
        :notice-data="editData"
        @submit="handleDialogSubmit"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchGetNoticePage,
    fetchDeleteNotice,
    fetchBatchDeleteNotice
  } from '@/api/system-manage'
  import NoticeSearch from './modules/notice-search.vue'
  import NoticeDrawer from './modules/notice-drawer.vue'
  import NoticeDialog from './modules/notice-dialog.vue'
  import { ElTag, ElMessageBox } from 'element-plus'
  import type { DialogType } from '@/types'

  defineOptions({ name: 'Notice' })

  type NoticeListItem = Api.SystemManage.NoticeListItem

  const showSearchBar = ref(false)
  const dialogType = ref<DialogType>('add')
  const dialogVisible = ref(false)
  const drawerVisible = ref(false)
  const editData = ref<Partial<NoticeListItem>>({})
  const currentNoticeId = ref<number>()
  const currentNoticeData = ref<NoticeListItem>()

  // 选中行
  const selectedRows = ref<NoticeListItem[]>([])
  const selectedCount = computed(() => selectedRows.value.length)

  // 搜索相关
  const formFilters = ref<Api.SystemManage.NoticeSearchParams>({
    pageNum: 1,
    title: undefined,
    noticeType: undefined,
    publisherName: undefined,
    status: undefined,
    publishTimeStart: undefined,
    publishTimeEnd: undefined
  })

  // 前置声明函数
  let showDialog: (type: DialogType, row?: NoticeListItem) => void
  let deleteNotice: (row: NoticeListItem) => Promise<void>
  let showDrawer: (row: NoticeListItem) => void

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
  } = useTable<typeof fetchGetNoticePage>({
    core: {
      apiFn: fetchGetNoticePage,
      apiParams: computed(() => ({
        pageNum: formFilters.value.pageNum,
        title: formFilters.value.title || undefined,
        noticeType: formFilters.value.noticeType,
        publisherName: formFilters.value.publisherName || undefined,
        status: formFilters.value.status,
        publishTimeStart: formFilters.value.publishTimeStart || undefined,
        publishTimeEnd: formFilters.value.publishTimeEnd || undefined
      })),
      paginationKey: {
        current: 'pageNum',
        size: 'pageSize'
      },
      columnsFactory: () => [
        { type: 'selection', width: 50 },
        { type: 'index', label: '序号', width: 80 },
        {
          prop: 'title',
          label: '通知标题',
          minWidth: 200,
          showOverflowTooltip: true,
          formatter: (row: NoticeListItem) => {
            return h('div', { class: 'flex items-center gap-2' }, [
              row.isTop ? h(ElTag, { type: 'danger', size: 'small' }, () => '置顶') : null,
              h('span', row.title)
            ])
          }
        },
        {
          prop: 'noticeType',
          label: '通知类型',
          minWidth: 100,
          formatter: (row: NoticeListItem) =>
            row.noticeTypeText ? h(ElTag, { type: 'primary' }, () => row.noticeTypeText) : '--'
        },
        {
          prop: 'publisherName',
          label: '发布人',
          minWidth: 100
        },
        {
          prop: 'publishTime',
          label: '发布时间',
          minWidth: 160
        },
        {
          prop: 'status',
          label: '状态',
          minWidth: 80,
          formatter: (row: NoticeListItem) => {
            const statusMap = {
              1: { text: '草稿', type: 'info' },
              2: { text: '已发布', type: 'success' }
            }
            const config = statusMap[row.status as 1 | 2] || { text: '未知', type: 'info' }
            return h(ElTag, { type: config.type as any }, () => config.text)
          }
        },
        {
          prop: 'readCount',
          label: '阅读次数',
          minWidth: 100,
          formatter: (row: NoticeListItem) => row.readCount || 0
        },
        {
          prop: 'action',
          label: '操作',
          width: 160,
          fixed: 'right',
          formatter: (row: NoticeListItem) => {
            return [
              {
                type: 'view',
                label: '查看',
                onClick: () => showDrawer(row),
                auth: 'system:notice:query'
              },
              {
                type: 'edit',
                label: '编辑',
                onClick: () => showDialog('edit', row),
                auth: 'system:notice:edit'
              },
              {
                type: 'delete',
                label: '删除',
                onClick: () => deleteNotice(row),
                danger: true,
                auth: 'system:notice:delete'
              }
            ]
          }
        }
      ]
    },
    contextMenu: {
      enabled: true
    }
  })

  // 显示详情抽屉
  showDrawer = (row: NoticeListItem) => {
    currentNoticeId.value = row.id
    currentNoticeData.value = row
    drawerVisible.value = true
    dialogVisible.value = false
  }

  // 抽屉中点击编辑
  const handleDrawerEdit = (noticeData: NoticeListItem) => {
    drawerVisible.value = false
    dialogVisible.value = true
    dialogType.value = 'edit'
    editData.value = noticeData
  }

  // 显示新增/编辑对话框
  showDialog = (type: DialogType, row?: NoticeListItem) => {
    dialogType.value = type
    if (type === 'edit' && row) {
      editData.value = { ...row }
    } else {
      editData.value = {}
    }
    dialogVisible.value = true
    drawerVisible.value = false
  }

  // 删除通知
  deleteNotice = async (row: NoticeListItem) => {
    await ElMessageBox.confirm(`确认删除通知"${row.title}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await fetchDeleteNotice(row.id)
    refreshRemove()
  }

  // 批量删除
  const handleBatchDelete = async () => {
    if (selectedRows.value.length === 0) return
    await ElMessageBox.confirm(`确认删除选中的 ${selectedRows.value.length} 条通知吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const ids = selectedRows.value.map((row) => row.id)
    await fetchBatchDeleteNotice(ids)
    refreshRemove()
  }

  // 对话框提交
  const handleDialogSubmit = () => {
    if (dialogType.value === 'add') {
      refreshCreate()
    } else {
      refreshUpdate()
    }
  }

  // 选择变化
  const handleSelectionChange = (rows: NoticeListItem[]) => {
    selectedRows.value = rows
  }

  // 搜索
  const handleSearch = (params: Api.SystemManage.NoticeSearchParams) => {
    Object.assign(formFilters.value, params)
    getData()
  }

  // 重置
  const handleReset = () => {
    resetSearchParams()
  }
</script>
