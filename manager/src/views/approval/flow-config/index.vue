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
            <ElButton @click="openFlowEditor()" v-ripple> 新增流程 </ElButton>
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
  import FlowBindingDialog from './modules/flow-binding-dialog.vue'
  import ArtSwitch from '@/components/core/forms/art-switch/index.vue'
  import ArtStatusDot from '@/components/core/tables/art-status-dot/index.vue'
  import { useBusinessType } from '@/hooks'
  import { useFlowData } from '@/components/core/flow-editor/composables/useFlowData'
  import { useFlowCommunication } from '@/components/core/flow-editor/composables/useFlowCommunication'

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
  const bindingDialogVisible = ref(false)

  // 跨标签页通信和数据缓存
  const { saveCache } = useFlowData()
  const { onMessage } = useFlowCommunication()

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
            return h(ArtStatusDot, {
              text: row.bound ? '已绑定' : '未绑定',
              type: row.bound ? 'success' : 'info'
            })
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
              { type: 'edit', onClick: () => openFlowEditor(row), label: '编辑' },
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
      ]
    },
    adaptive: {
      enabled: true
    },
    contextMenu: {
      enabled: true
    }
  })

  /**
   * 打开流程编辑器（新标签页）
   */
  const openFlowEditor = (row?: FlowListItem) => {
    const flowId = row?.id

    // 如果是编辑模式，保存数据到缓存
    if (row) {
      saveCache({
        flowId: row.id,
        flowName: row.flowName,
        flowData: row.nodes || [],
        isAdd: false
      })
    }

    // 构建 URL（hash 模式路由）
    const baseUrl = window.location.origin
    const editorPath = flowId ? `/approval/flow-editor/${flowId}` : '/approval/flow-editor'
    const editorUrl = `${baseUrl}/#${editorPath}`

    // 打开新标签页
    const newWindow = window.open(editorUrl, '_blank')

    if (!newWindow) {
      ElMessage.warning('请允许浏览器弹出窗口')
    }
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

  /**
   * 监听跨标签页消息
   */
  onMessage((message) => {
    if (message.type === 'flow-saved') {
      // 流程保存成功，刷新列表
      ElMessage.success(`流程"${message.flowName}"保存成功`)
      refreshData()
    } else if (message.type === 'flow-closed') {
      // 流程编辑器关闭
      console.log('流程编辑器已关闭')
    } else if (message.type === 'flow-error') {
      // 流程编辑器错误
      ElMessage.error(message.error || '流程编辑器发生错误')
    }
  })

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
