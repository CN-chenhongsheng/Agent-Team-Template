<!-- 字典管理页面 -->
<template>
  <div class="dict-page art-full-height">
    <div class="dict-container">
      <!-- 左侧：字典类型列表 -->
      <div class="dict-type-panel">
        <ElCard shadow="never" class="panel-card">
          <template #header>
            <div class="panel-header">
              <span class="panel-title">
                <ArtSvgIcon icon="ri:bookmark-line" class="text-lg" />
                字典类型
              </span>
              <ElButton
                type="primary"
                size="small"
                @click="showTypeDialog('add')"
                v-ripple
                v-permission="'system:dict:type:add'"
              >
                <template #icon><ArtSvgIcon :icon="'ri-add-line'" /></template>
                新增类型
              </ElButton>
            </div>
          </template>

          <!-- 类型搜索 -->
          <div class="search-bar">
            <div class="search-inputs">
              <ElInput
                v-model="typeSearchForm.dictName"
                placeholder="搜索字典名称"
                clearable
                @clear="handleTypeSearch"
                @keyup.enter="handleTypeSearch"
              >
                <template #prefix>
                  <ArtSvgIcon icon="ri:search-line" class="text-main-color" />
                </template>
              </ElInput>
              <ElInput
                v-model="typeSearchForm.dictCode"
                placeholder="搜索字典编码"
                clearable
                @clear="handleTypeSearch"
                @keyup.enter="handleTypeSearch"
              >
                <template #prefix>
                  <ArtSvgIcon icon="ri:code-line" class="text-main-color" />
                </template>
              </ElInput>
            </div>
          </div>

          <!-- 类型表格 -->
          <ArtTable
            :loading="typeLoading"
            :data="typeData"
            :columns="typeColumns"
            :pagination="typePagination"
            :contextMenuItems="typeContextMenuItems"
            :contextMenuWidth="typeContextMenuWidth"
            :onRowContextmenu="handleTypeRowContextmenu as any"
            :onContextMenuSelect="handleTypeContextMenuSelect"
            @row-click="handleTypeRowClick"
            @pagination:size-change="handleTypeSizeChange"
            @pagination:current-change="handleTypeCurrentChange"
          />
        </ElCard>
      </div>

      <!-- 右侧：字典数据列表 -->
      <div class="dict-data-panel">
        <ElCard shadow="never" class="panel-card">
          <template #header>
            <div class="panel-header">
              <span class="panel-title">
                <ArtSvgIcon icon="ri:list-check" class="text-lg" />
                字典数据
              </span>
              <ElButton
                type="primary"
                size="small"
                :disabled="!selectedTypeId"
                @click="showDataDialog('add')"
                v-ripple
                v-permission="'system:dict:data:add'"
              >
                <template #icon><ArtSvgIcon :icon="'ri-add-line'" /></template>
                新增数据
              </ElButton>
            </div>
          </template>

          <div v-if="!selectedTypeId" class="empty-tip">
            <ElEmpty description="请先选择左侧字典类型" />
          </div>

          <template v-else>
            <!-- 数据搜索 -->
            <div class="search-bar">
              <ElInput
                v-model="dataSearchForm.label"
                placeholder="搜索字典标签"
                clearable
                @clear="handleDataSearch"
                @keyup.enter="handleDataSearch"
              >
                <template #prefix>
                  <ArtSvgIcon icon="ri:search-line" class="text-main-color" />
                </template>
              </ElInput>
            </div>

            <!-- 数据表格 -->
            <ArtTable
              :loading="dataLoading"
              :data="dataData"
              :columns="dataColumns"
              :pagination="dataPagination"
              :contextMenuItems="dataContextMenuItems"
              :contextMenuWidth="dataContextMenuWidth"
              :onRowContextmenu="handleDataRowContextmenu as any"
              :onContextMenuSelect="handleDataContextMenuSelect"
              @pagination:size-change="handleDataSizeChange"
              @pagination:current-change="handleDataCurrentChange"
            />
          </template>
        </ElCard>
      </div>
    </div>

    <!-- 字典类型弹窗 -->
    <DictTypeDialog
      v-model="typeDialogVisible"
      :dialog-type="typeDialogType"
      :type-data="currentTypeData"
      @success="handleTypeRefresh"
    />

    <!-- 字典数据弹窗 -->
    <DictDataDialog
      v-model="dataDialogVisible"
      :dialog-type="dataDialogType"
      :data-data="currentDataData"
      :dict-code="selectedDictCode"
      @success="handleDataRefresh"
    />
  </div>
</template>

<script setup lang="ts">
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchGetDictTypePage,
    fetchGetDictTypeDetail,
    fetchUpdateDictType,
    fetchDeleteDictType,
    fetchGetDictDataPage,
    fetchGetDictDataDetail,
    fetchUpdateDictData,
    fetchDeleteDictData
  } from '@/api/system-manage'
  import DictTypeDialog from './modules/dict-type-dialog.vue'
  import DictDataDialog from './modules/dict-data-dialog.vue'
  import { ElTag, ElMessageBox } from 'element-plus'
  import { h } from 'vue'
  import ArtSwitch from '@/components/core/forms/art-switch/index.vue'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'

  defineOptions({ name: 'Dict' })

  type DictTypeListItem = Api.SystemManage.DictTypeListItem & { _statusLoading?: boolean }
  type DictDataListItem = Api.SystemManage.DictDataListItem & { _statusLoading?: boolean }

  // 字典类型相关
  const selectedTypeId = ref<number | null>(null)
  const selectedDictCode = ref<string>('')
  const typeSearchForm = ref<Api.SystemManage.DictTypeSearchParams>({
    pageNum: 1,
    dictName: undefined,
    dictCode: undefined,
    status: undefined
  })

  const {
    columns: typeColumns,
    data: typeData,
    loading: typeLoading,
    pagination: typePagination,
    getData: getTypeData,
    handleSizeChange: handleTypeSizeChange,
    handleCurrentChange: handleTypeCurrentChange,
    contextMenuItems: typeContextMenuItems,
    contextMenuWidth: typeContextMenuWidth,
    handleRowContextmenu: handleTypeRowContextmenu,
    handleContextMenuSelect: handleTypeContextMenuSelect
  } = useTable<typeof fetchGetDictTypePage>({
    core: {
      apiFn: fetchGetDictTypePage,
      apiParams: computed(() => ({
        pageNum: typeSearchForm.value.pageNum,
        dictName: typeSearchForm.value.dictName,
        dictCode: typeSearchForm.value.dictCode,
        status: typeSearchForm.value.status
      })),
      paginationKey: {
        current: 'pageNum',
        size: 'pageSize'
      },
      columnsFactory: () => [
        {
          prop: 'dictName',
          label: '字典名称',
          minWidth: 120
        },
        {
          prop: 'dictCode',
          label: '字典编码',
          minWidth: 150,
          showOverflowTooltip: true,
          formatter: (row: DictTypeListItem) => {
            return h('code', { class: 'dict-code' }, row.dictCode)
          }
        },
        {
          prop: 'status',
          label: '状态',
          width: 100,
          formatter: (row: DictTypeListItem) => {
            return h(ArtSwitch, {
              modelValue: row.status === 1,
              loading: row._statusLoading || false,
              inlinePrompt: true,
              onChange: (value: string | number | boolean) => {
                handleTypeStatusChange(row, value === true || value === 1)
              }
            })
          }
        },
        {
          prop: 'remark',
          label: '备注',
          minWidth: 150,
          showOverflowTooltip: true
        },
        {
          prop: 'action',
          label: '操作',
          width: 120,
          fixed: 'right' as const,
          formatter: (row: DictTypeListItem) => {
            const buttons = []
            buttons.push({
              type: 'edit',
              label: '编辑',
              onClick: () => showTypeDialog('edit', row),
              auth: 'system:dict:type:edit'
            })
            buttons.push({
              type: 'delete',
              label: '删除',
              onClick: () => deleteType(row),
              auth: 'system:dict:type:delete',
              danger: true
            })
            return buttons
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

  // 字典数据相关
  const dataSearchForm = ref({
    dictCode: '',
    label: undefined as string | undefined
  })

  const {
    columns: dataColumns,
    data: dataData,
    loading: dataLoading,
    pagination: dataPagination,
    getData: getDataData,
    handleSizeChange: handleDataSizeChange,
    handleCurrentChange: handleDataCurrentChange,
    fetchData: fetchDataData,
    contextMenuItems: dataContextMenuItems,
    contextMenuWidth: dataContextMenuWidth,
    handleRowContextmenu: handleDataRowContextmenu,
    handleContextMenuSelect: handleDataContextMenuSelect
  } = useTable<typeof fetchGetDictDataPage>({
    core: {
      apiFn: fetchGetDictDataPage,
      apiParams: computed(() => ({
        dictCode: dataSearchForm.value.dictCode,
        label: dataSearchForm.value.label
      })),
      immediate: false, // 不自动加载，需要先选择字典类型
      paginationKey: {
        current: 'pageNum',
        size: 'pageSize'
      },
      columnsFactory: () => [
        {
          prop: 'label',
          label: '字典标签',
          minWidth: 120
        },
        {
          prop: 'value',
          label: '字典值',
          minWidth: 120,
          formatter: (row: DictDataListItem) => {
            return h('code', { class: 'dict-value' }, row.value)
          }
        },
        {
          prop: 'listClass',
          label: '样式',
          width: 100,
          formatter: (row: DictDataListItem) => {
            if (!row.listClass) return '-'
            const tagType = (row.listClass || 'info') as
              | 'primary'
              | 'success'
              | 'info'
              | 'warning'
              | 'danger'
            return h(ElTag, { type: tagType, size: 'small' }, () => row.listClass || '-')
          }
        },
        {
          prop: 'sort',
          label: '排序',
          width: 80
        },
        {
          prop: 'status',
          label: '状态',
          width: 100,
          formatter: (row: DictDataListItem) => {
            return h(ArtSwitch, {
              modelValue: row.status === 1,
              loading: row._statusLoading || false,
              inlinePrompt: true,
              onChange: (value: string | number | boolean) => {
                handleDataStatusChange(row, value === true || value === 1)
              }
            })
          }
        },
        {
          prop: 'remark',
          label: '备注',
          minWidth: 150,
          showOverflowTooltip: true
        },
        {
          prop: 'action',
          label: '操作',
          width: 120,
          fixed: 'right' as const,
          formatter: (row: DictDataListItem) => {
            const buttons = []
            buttons.push({
              type: 'edit',
              label: '编辑',
              onClick: () => showDataDialog('edit', row),
              auth: 'system:dict:data:edit'
            })
            buttons.push({
              type: 'delete',
              label: '删除',
              onClick: () => deleteData(row),
              auth: 'system:dict:data:delete',
              danger: true
            })
            return buttons
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

  // 弹窗相关
  const typeDialogVisible = ref(false)
  const typeDialogType = ref<'add' | 'edit'>('add')
  const currentTypeData = ref<DictTypeListItem | undefined>(undefined)

  const dataDialogVisible = ref(false)
  const dataDialogType = ref<'add' | 'edit'>('add')
  const currentDataData = ref<DictDataListItem | undefined>(undefined)

  // 类型相关方法
  const handleTypeSearch = () => {
    typeSearchForm.value.pageNum = 1
    getTypeData()
  }

  const handleTypeRowClick = (row: DictTypeListItem) => {
    selectedTypeId.value = row.id
    selectedDictCode.value = row.dictCode
    dataSearchForm.value.dictCode = row.dictCode
    dataSearchForm.value.label = undefined
    // 使用 nextTick 确保 useTable 的内部 watch 已完成参数同步
    nextTick(() => {
      getDataData()
    })
  }

  const showTypeDialog = (type: 'add' | 'edit', data?: DictTypeListItem) => {
    typeDialogType.value = type
    currentTypeData.value = data
    typeDialogVisible.value = true
  }

  const deleteType = async (row: DictTypeListItem) => {
    try {
      await ElMessageBox.confirm(
        `确定删除字典类型"${row.dictName}"吗？此操作不可恢复！`,
        '删除确认',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )

      await fetchDeleteDictType(row.id)
      handleTypeRefresh()
      // 如果删除的是当前选中的类型，清空右侧数据
      if (selectedTypeId.value === row.id) {
        selectedTypeId.value = null
        selectedDictCode.value = ''
        dataSearchForm.value.dictCode = ''
        dataData.value = []
      }
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除字典类型失败:', error)
      }
    }
  }

  const handleTypeRefresh = () => {
    getTypeData()
  }

  // 更新字典类型状态
  const handleTypeStatusChange = async (row: DictTypeListItem, value: boolean) => {
    // 保存原始状态
    const originalStatus = row.status
    try {
      // 设置loading状态
      row._statusLoading = true

      // 先更新UI状态（乐观更新）
      row.status = value ? 1 : 0

      // 获取详情
      const detail = await fetchGetDictTypeDetail(row.id)

      // 更新状态
      await fetchUpdateDictType(row.id, {
        ...detail,
        status: value ? 1 : 0
      })
    } catch (error) {
      console.error('更新字典类型状态失败:', error)
      // 恢复原状态
      row.status = originalStatus
    } finally {
      row._statusLoading = false
    }
  }

  // 数据相关方法
  const handleDataSearch = () => {
    if (!dataSearchForm.value.dictCode) return
    getDataData()
  }

  const showDataDialog = (type: 'add' | 'edit', data?: DictDataListItem) => {
    dataDialogType.value = type
    currentDataData.value = data
    dataDialogVisible.value = true
  }

  const deleteData = async (row: DictDataListItem) => {
    try {
      await ElMessageBox.confirm(`确定删除字典数据"${row.label}"吗？此操作不可恢复！`, '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })

      await fetchDeleteDictData(row.id)
      handleDataRefresh()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除字典数据失败:', error)
      }
    }
  }

  const handleDataRefresh = () => {
    if (!dataSearchForm.value.dictCode) return
    fetchDataData()
  }

  // 更新字典数据状态
  const handleDataStatusChange = async (row: DictDataListItem, value: boolean) => {
    // 保存原始状态
    const originalStatus = row.status
    try {
      // 设置loading状态
      row._statusLoading = true

      // 先更新UI状态（乐观更新）
      row.status = value ? 1 : 0

      // 获取详情
      const detail = await fetchGetDictDataDetail(row.id)

      // 更新状态
      await fetchUpdateDictData(row.id, {
        ...detail,
        status: value ? 1 : 0
      })
    } catch (error) {
      console.error('更新字典数据状态失败:', error)
      // 恢复原状态
      row.status = originalStatus
    } finally {
      row._statusLoading = false
    }
  }

  // 初始化
  // 注意：因为启用了自适应分页（adaptive: { enabled: true }），
  // 首次数据加载由 useTable 的 watch 自动处理，无需手动调用 getTypeData()
  // onMounted(() => {
  //   getTypeData()
  // })
</script>

<style scoped lang="scss">
  .dict-container {
    display: flex;
    gap: 16px;
    height: 100%;
  }

  .dict-type-panel,
  .dict-data-panel {
    display: flex;
    flex: 1;
    flex-direction: column;
    min-width: 0;
  }

  .panel-card {
    display: flex;
    flex: 1;
    flex-direction: column;
    overflow: hidden;
    border-radius: calc(var(--custom-radius) / 2 + 2px) !important;

    :deep(.el-card__body) {
      display: flex;
      flex: 1;
      flex-direction: column;
      overflow: hidden;
    }
  }

  .panel-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .panel-title {
    font-size: 14px;
    font-weight: 500;
    color: var(--main-color);
  }

  .search-bar {
    margin-bottom: 12px;

    .search-inputs {
      display: flex;
      gap: 8px;

      .el-input {
        flex: 1;
      }
    }
  }

  .empty-tip {
    display: flex;
    flex: 1;
    align-items: center;
    justify-content: center;
  }

  .dict-code,
  .dict-value {
    padding: 2px 6px;
    font-family: 'Courier New', monospace;
    font-size: 12px;
    background-color: var(--el-fill-color-light);
    border-radius: 3px;
  }
</style>
