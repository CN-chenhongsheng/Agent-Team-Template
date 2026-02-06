<!-- 流程配置页面 -->
<template>
  <div class="art-full-height">
    <!-- 搜索栏 -->
    <FlowSearch
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
      <ArtTableHeader
        v-model:columns="columnChecks"
        v-model:showSearchBar="showSearchBar"
        :loading="loading"
        @refresh="refreshData"
      >
        <template #left>
          <ElSpace wrap>
            <ElButton @click="showDialog('add')" v-ripple>
              <i class="ri-add-line mr-1"></i>
              新增流程
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
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <!-- 流程编辑弹窗 -->
    <FlowEditDialog
      v-model="dialogVisible"
      :dialog-type="dialogType"
      :flow-data="editData"
      @success="handleEditDialogSuccess"
    />

    <!-- 流程绑定弹窗 -->
    <FlowBindingDialog v-model="bindingDialogVisible" @success="refreshData" />
  </div>
</template>

<script setup lang="ts">
  import { onMounted } from 'vue'
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchGetFlowList,
    fetchDeleteFlow,
    fetchUpdateFlowStatus,
    type ApprovalFlow
  } from '@/api/approval-manage'
  import FlowSearch from './modules/flow-search.vue'
  import FlowEditDialog from './modules/flow-edit-dialog.vue'
  import FlowBindingDialog from './modules/flow-binding-dialog.vue'
  import { ElMessageBox, ElMessage, ElTag } from 'element-plus'
  import ArtSwitch from '@/components/core/forms/art-switch/index.vue'
  import { useBusinessType } from '@/hooks'

  defineOptions({ name: 'FlowConfig' })

  type FlowListItem = ApprovalFlow & { _statusLoading?: boolean }

  // 业务类型（从字典获取）
  const { businessTypeOptions, fetchBusinessTypes } = useBusinessType()

  onMounted(() => {
    fetchBusinessTypes()
  })

  // 搜索相关
  const formFilters = ref({
    pageNum: 1,
    flowName: undefined,
    flowCode: undefined,
    businessType: undefined,
    status: undefined
  })

  const showSearchBar = ref(false)
  const dialogVisible = ref(false)
  const bindingDialogVisible = ref(false)
  const editData = ref<FlowListItem | null>(null)

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
  } = useTable<typeof fetchGetFlowList>({
    core: {
      apiFn: fetchGetFlowList,
      apiParams: computed(() => {
        return {
          pageNum: formFilters.value.pageNum,
          flowName: formFilters.value.flowName || undefined,
          flowCode: formFilters.value.flowCode || undefined,
          businessType: formFilters.value.businessType || undefined,
          status: formFilters.value.status
        }
      }),
      paginationKey: {
        current: 'pageNum',
        size: 'pageSize'
      },
      columnsFactory: () => [
        {
          prop: 'flowName',
          label: '流程名称',
          minWidth: 180,
          formatter: (row: FlowListItem) => {
            return h('div', { class: 'flex items-center gap-2' }, [
              h('span', { class: 'font-medium' }, row.flowName)
            ])
          }
        },
        {
          prop: 'flowCode',
          label: '流程编码',
          minWidth: 150
        },
        {
          prop: 'businessType',
          label: '业务类型',
          width: 120,
          formatter: (row: FlowListItem) => {
            const option = businessTypeOptions.value.find(
              (o: { value: string; label: string }) => o.value === row.businessType
            )
            return h(
              ElTag,
              { type: 'primary', size: 'small' },
              () => option?.label || row.businessType
            )
          }
        },
        {
          prop: 'bound',
          label: '绑定状态',
          width: 100,
          formatter: (row: FlowListItem) => {
            return h(
              ElTag,
              {
                type: row.bound ? 'success' : 'info',
                size: 'small'
              },
              () => (row.bound ? '已绑定' : '未绑定')
            )
          }
        },
        {
          prop: 'status',
          label: '状态',
          width: 100,
          formatter: (row: FlowListItem) => {
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
          prop: 'description',
          label: '流程描述',
          minWidth: 200,
          showOverflowTooltip: true
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
          width: 200,
          fixed: 'right',
          formatter: (row: FlowListItem) => {
            return [
              { type: 'edit', onClick: () => showDialog('edit', row), label: '编辑' },
              {
                type: 'link',
                onClick: () => {
                  bindingDialogVisible.value = true
                },
                label: '绑定'
              },
              {
                type: 'delete',
                onClick: () => deleteFlow(row),
                danger: true,
                disabled: row.bound,
                label: '删除'
              }
            ]
          }
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

  const dialogType = ref<'add' | 'edit'>('add')

  const showDialog = (type: 'add' | 'edit', row?: FlowListItem) => {
    dialogVisible.value = true
    dialogType.value = type
    editData.value = row ? { ...row } : null
  }

  const handleSearch = async (): Promise<void> => {
    formFilters.value.pageNum = 1
    await getData()
  }

  const handleReset = async (): Promise<void> => {
    formFilters.value = {
      pageNum: 1,
      flowName: undefined,
      flowCode: undefined,
      businessType: undefined,
      status: undefined
    }
    await resetSearchParams()
  }

  const handleEditDialogSuccess = async () => {
    if (dialogType.value === 'add') {
      await refreshCreate()
    } else {
      await refreshUpdate()
    }
  }

  const deleteFlow = async (row: FlowListItem) => {
    if (row.bound) {
      ElMessage.warning('流程已绑定业务，请先解绑')
      return
    }

    try {
      await ElMessageBox.confirm(`确定要删除流程"${row.flowName}"吗？`, '删除确认', {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      })

      await fetchDeleteFlow(row.id!)
      await refreshRemove()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除流程失败:', error)
      }
    }
  }

  const handleStatusChange = async (row: FlowListItem, value: boolean): Promise<void> => {
    const originalStatus = row.status
    try {
      row._statusLoading = true
      row.status = value ? 1 : 0
      await fetchUpdateFlowStatus(row.id!, value ? 1 : 0)
    } catch (error) {
      console.error('更新流程状态失败:', error)
      row.status = originalStatus
    } finally {
      row._statusLoading = false
    }
  }
</script>
