<!-- 审批记录页面 -->
<template>
  <div class="art-full-height">
    <!-- 搜索栏 -->
    <HistorySearch
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
          <ArtViewSwitcher
            v-model="viewMode"
            :options="viewModeOptions"
            @update:model-value="handleViewModeChange"
          />
        </template>
      </ArtTableHeader>

      <!-- 表格 -->
      <ArtTable
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchGetRecordList,
    fetchGetMyRecordList,
    type ApprovalRecord,
    type ApprovalRecordQueryParams
  } from '@/api/approval-manage'
  import HistorySearch from './modules/history-search.vue'
  import ArtViewSwitcher from '@/components/core/base/art-view-switcher/index.vue'
  import { ElTag } from 'element-plus'

  defineOptions({ name: 'ApprovalHistory' })

  // 搜索相关
  const formFilters = ref({
    pageNum: 1,
    businessType: undefined,
    applicantName: undefined,
    approverName: undefined,
    action: undefined
  })
  const showSearchBar = ref(false)
  const viewMode = ref<'all' | 'my'>('all')

  /** 视图模式选项 */
  const viewModeOptions = [
    { value: 'all', label: '全部记录' },
    { value: 'my', label: '我的审批' }
  ]

  // 动态API函数
  const getApiFn = () => {
    return viewMode.value === 'my' ? fetchGetMyRecordList : fetchGetRecordList
  }

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
    refreshData
  } = useTable({
    core: {
      apiFn: getApiFn(),
      apiParams: computed(
        () =>
          ({
            pageNum: formFilters.value.pageNum,
            businessType: formFilters.value.businessType || undefined,
            applicantName: formFilters.value.applicantName || undefined,
            approverName: formFilters.value.approverName || undefined,
            action: formFilters.value.action
          }) as ApprovalRecordQueryParams
      ),
      paginationKey: {
        current: 'pageNum',
        size: 'pageSize'
      },
      columnsFactory: () => [
        {
          prop: 'businessType',
          label: '业务类型',
          width: 120,
          formatter: (row: ApprovalRecord) => {
            return h(ElTag, { type: 'primary', size: 'small' }, () => row.businessTypeText)
          }
        },
        {
          prop: 'applicantName',
          label: '申请人',
          width: 100
        },
        {
          prop: 'flowName',
          label: '审批流程',
          minWidth: 150
        },
        {
          prop: 'nodeName',
          label: '审批节点',
          width: 120
        },
        {
          prop: 'approverName',
          label: '审批人',
          width: 100
        },
        {
          prop: 'action',
          label: '审批结果',
          width: 100,
          formatter: (row: ApprovalRecord) => {
            return h(
              ElTag,
              {
                type: row.action === 1 ? 'success' : 'danger',
                size: 'small'
              },
              () => row.actionText
            )
          }
        },
        {
          prop: 'opinion',
          label: '审批意见',
          minWidth: 200,
          showOverflowTooltip: true
        },
        {
          prop: 'approveTime',
          label: '审批时间',
          width: 180,
          sortable: true
        }
      ],
      immediate: true
    },
    adaptive: {
      enabled: true
    }
  })

  const handleSearch = async (): Promise<void> => {
    formFilters.value.pageNum = 1
    await getData()
  }

  const handleReset = async (): Promise<void> => {
    formFilters.value = {
      pageNum: 1,
      businessType: undefined,
      applicantName: undefined,
      approverName: undefined,
      action: undefined
    }
    await resetSearchParams()
  }

  const handleViewModeChange = async () => {
    formFilters.value.pageNum = 1
    // 重新获取数据需要更新API函数
    await getData()
  }
</script>
