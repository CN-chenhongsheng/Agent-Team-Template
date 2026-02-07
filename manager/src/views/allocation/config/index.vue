<template>
  <div class="config-page art-full-height">
    <!-- 搜索栏 -->
    <ConfigSearch
      v-show="showSearchBar"
      v-model="searchForm"
      :algorithm-options="algorithmOptions"
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
            <ElButton @click="handleAdd" v-ripple>新增配置</ElButton>
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
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <!-- 配置弹窗 -->
    <ConfigDialog
      v-model:visible="dialogVisible"
      :type="dialogType"
      :config-data="editData"
      :algorithm-options="algorithmOptions"
      @submit="handleDialogSubmit"
    />
  </div>
</template>

<script setup lang="ts">
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchGetConfigPage,
    fetchGetConfigDetail,
    fetchDeleteConfig,
    fetchCopyConfig,
    fetchGetAlgorithmList,
    fetchUpdateConfigStatus
  } from '@/api/allocation-manage'
  import ConfigSearch from './modules/config-search.vue'
  import ConfigDialog from './modules/config-dialog.vue'
  import ArtSwitch from '@/components/core/forms/art-switch/index.vue'
  import type { ActionButtonConfig } from '@/types/component'

  defineOptions({ name: 'AllocationConfig' })

  type ConfigListItem = Api.Allocation.ConfigListItem

  const showSearchBar = ref(false)
  const dialogType = ref<'add' | 'edit'>('add')
  const dialogVisible = ref(false)
  const editData = ref<Partial<ConfigListItem> | null>(null)

  // 算法选项
  const algorithmOptions = ref<Api.Allocation.AlgorithmOption[]>([])

  // 搜索表单
  const searchForm = ref<Api.Allocation.ConfigSearchParams>({
    pageNum: 1,
    configName: undefined,
    algorithmType: undefined,
    status: undefined
  })

  // 前置声明函数
  let handleEdit: (row: ConfigListItem) => Promise<void>
  let handleDelete: (row: ConfigListItem) => Promise<void>
  let handleCopy: (row: ConfigListItem) => Promise<void>

  /**
   * 获取操作配置（复用于操作列和右键菜单）
   */
  const getRowActions = (row: ConfigListItem): ActionButtonConfig[] => [
    {
      type: 'edit',
      label: '编辑',
      onClick: () => handleEdit(row),
      auth: 'allocation:config:edit'
    },
    {
      type: 'copy',
      label: '复制',
      onClick: () => handleCopy(row),
      auth: 'allocation:config:add'
    },
    {
      type: 'delete',
      label: '删除',
      onClick: () => handleDelete(row),
      danger: true,
      auth: 'allocation:config:delete'
    }
  ]

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
    refreshRemove,
    contextMenuItems,
    contextMenuWidth,
    handleRowContextmenu,
    handleContextMenuSelect
  } = useTable<typeof fetchGetConfigPage>({
    core: {
      apiFn: fetchGetConfigPage,
      apiParams: computed(() => ({
        pageNum: searchForm.value.pageNum,
        configName: searchForm.value.configName || undefined,
        algorithmType: searchForm.value.algorithmType || undefined,
        status: searchForm.value.status
      })),
      paginationKey: { current: 'pageNum', size: 'pageSize' },
      columnsFactory: () => [
        { prop: 'configName', label: '配置名称', minWidth: 150 },
        { prop: 'algorithmTypeName', label: '算法类型', width: 120 },
        {
          prop: 'constraints',
          label: '硬约束',
          width: 180,
          formatter: (row: ConfigListItem) => {
            const tags: VNode[] = []
            if (row.smokingConstraint) {
              tags.push(h(ElTag, { size: 'small', class: 'mr-1' }, () => '吸烟'))
            }
            if (row.genderConstraint) {
              tags.push(h(ElTag, { size: 'small', class: 'mr-1' }, () => '性别'))
            }
            if (row.sleepHardConstraint) {
              tags.push(h(ElTag, { size: 'small' }, () => '作息'))
            }
            return tags.length > 0 ? h('div', { class: 'flex gap-1' }, tags) : '--'
          }
        },
        { prop: 'minMatchScore', label: '最低阈值', width: 90 },
        {
          prop: 'status',
          label: '状态',
          width: 100,
          formatter: (row: ConfigListItem) => {
            return h(ArtSwitch, {
              modelValue: row.status === 1,
              inlinePrompt: true,
              onChange: async (value) => {
                await handleStatusChange(row, value ? 1 : 0)
              }
            })
          }
        },
        { prop: 'createTime', label: '创建时间', width: 170, sortable: true },
        {
          prop: 'action',
          label: '操作',
          width: 180,
          fixed: 'right',
          formatter: (row: ConfigListItem) => getRowActions(row)
        }
      ]
    },
    adaptive: {
      enabled: true
    },
    contextMenu: {
      enabled: true,
      actionsGetter: getRowActions
    }
  })

  // 搜索
  const handleSearch = async () => {
    searchForm.value.pageNum = 1
    await getData()
  }

  // 重置
  const handleReset = async () => {
    searchForm.value = {
      pageNum: 1,
      configName: undefined,
      algorithmType: undefined,
      status: undefined
    }
    await resetSearchParams()
  }

  // 新增
  const handleAdd = () => {
    dialogType.value = 'add'
    editData.value = null
    dialogVisible.value = true
  }

  // 编辑
  handleEdit = async (row: ConfigListItem) => {
    try {
      const detail = await fetchGetConfigDetail(row.id)
      dialogType.value = 'edit'
      editData.value = detail
      dialogVisible.value = true
    } catch (error) {
      console.error('获取配置详情失败:', error)
    }
  }

  // 删除
  handleDelete = async (row: ConfigListItem) => {
    try {
      await ElMessageBox.confirm('确定要删除该配置吗？', '提示', { type: 'warning' })
      await fetchDeleteConfig(row.id)
      await refreshRemove()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除配置失败:', error)
      }
    }
  }

  // 复制
  handleCopy = async (row: ConfigListItem) => {
    try {
      const { value } = await ElMessageBox.prompt('请输入新配置名称', '复制配置', {
        inputValue: `${row.configName}_副本`
      })
      await fetchCopyConfig(row.id, value)
      await refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('复制配置失败:', error)
      }
    }
  }

  // 状态切换
  const handleStatusChange = async (row: ConfigListItem, status: number) => {
    try {
      await fetchUpdateConfigStatus(row.id, status)
      row.status = status
    } catch (error) {
      console.error('更新状态失败:', error)
    }
  }

  // 弹窗提交回调
  const handleDialogSubmit = async () => {
    await refreshData()
  }

  // 加载算法列表
  const loadAlgorithms = async () => {
    try {
      const data = await fetchGetAlgorithmList()
      algorithmOptions.value = data || []
    } catch (error) {
      console.error('加载算法列表失败:', error)
    }
  }

  onMounted(() => {
    loadAlgorithms()
  })
</script>
