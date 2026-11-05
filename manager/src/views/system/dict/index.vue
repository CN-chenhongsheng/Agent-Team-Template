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
                {{ $t('system.dict.typeTitle') }}
              </span>
              <ElButton
                type="primary"
                size="small"
                @click="showTypeDialog('add')"
                v-ripple
                v-permission="PERMISSIONS.system.dict.type.add"
              >
                <template #icon><ArtSvgIcon :icon="'ri-add-line'" /></template>
                {{ $t('system.dict.addType') }}
              </ElButton>
            </div>
          </template>

          <!-- 类型搜索 -->
          <div class="search-bar">
            <div class="search-inputs">
              <ElInput
                v-model="typeFormFilters.dictName"
                :placeholder="$t('system.dict.searchName')"
                clearable
                @clear="handleTypeSearch"
                @keyup.enter="handleTypeSearch"
              >
                <template #prefix>
                  <ArtSvgIcon icon="ri:search-line" class="text-theme" />
                </template>
              </ElInput>
              <ElInput
                v-model="typeFormFilters.dictCode"
                :placeholder="$t('system.dict.searchCode')"
                clearable
                @clear="handleTypeSearch"
                @keyup.enter="handleTypeSearch"
              >
                <template #prefix>
                  <ArtSvgIcon icon="ri:code-line" class="text-theme" />
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
                {{ $t('system.dict.dataTitle') }}
              </span>
              <ElButton
                type="primary"
                size="small"
                :disabled="!selectedTypeId"
                @click="showDataDialog('add')"
                v-ripple
                v-permission="PERMISSIONS.system.dict.data.add"
              >
                <template #icon><ArtSvgIcon :icon="'ri-add-line'" /></template>
                {{ $t('system.dict.addData') }}
              </ElButton>
            </div>
          </template>

          <div v-if="!selectedTypeId" class="empty-tip">
            <ElEmpty :description="$t('system.dict.selectTypeFirst')" />
          </div>

          <template v-else>
            <!-- 数据搜索 -->
            <div class="search-bar">
              <ElInput
                v-model="dataFormFilters.label"
                :placeholder="$t('system.dict.searchLabel')"
                clearable
                @clear="handleDataSearch"
                @keyup.enter="handleDataSearch"
              >
                <template #prefix>
                  <ArtSvgIcon icon="ri:search-line" class="text-theme" />
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
  import { useI18n } from 'vue-i18n'
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
  import { h } from 'vue'
  import ArtSwitch from '@/components/core/forms/art-switch/index.vue'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import type { ActionButtonConfig } from '@/types/component'
  import type { DialogType } from '@/types'
  import { PERMISSIONS } from '@/utils/constants'

  defineOptions({ name: 'Dict' })

  const { t } = useI18n()

  type DictTypeListItem = Api.SystemManage.DictTypeListItem & { _statusLoading?: boolean }
  type DictDataListItem = Api.SystemManage.DictDataListItem & { _statusLoading?: boolean }

  // 字典类型相关
  const selectedTypeId = ref<number | null>(null)
  const selectedDictCode = ref<string>('')
  const typeFormFilters = ref<Api.SystemManage.DictTypeSearchParams>({
    pageNum: 1,
    dictName: undefined,
    dictCode: undefined,
    status: undefined
  })

  /**
   * 获取字典类型操作配置
   */
  const getDictTypeActions = (row: DictTypeListItem): ActionButtonConfig[] => [
    {
      type: 'edit',
      label: t('system.common.edit'),
      onClick: () => showTypeDialog('edit', row),
      auth: PERMISSIONS.system.dict.type.edit
    },
    {
      type: 'delete',
      label: t('system.common.delete'),
      onClick: () => deleteType(row),
      auth: PERMISSIONS.system.dict.type.delete,
      danger: true
    }
  ]

  /**
   * 获取字典数据操作配置
   */
  const getDictDataActions = (row: DictDataListItem): ActionButtonConfig[] => [
    {
      type: 'edit',
      label: t('system.common.edit'),
      onClick: () => showDataDialog('edit', row),
      auth: PERMISSIONS.system.dict.data.edit
    },
    {
      type: 'delete',
      label: t('system.common.delete'),
      onClick: () => deleteData(row),
      auth: PERMISSIONS.system.dict.data.delete,
      danger: true
    }
  ]

  const {
    columns: typeColumns,
    data: typeData,
    loading: typeLoading,
    pagination: typePagination,
    getData: getTypeData,
    refreshRemove: refreshTypeRemove,
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
        ...typeFormFilters.value
      })),
      paginationKey: {
        current: 'pageNum',
        size: 'pageSize'
      },
      columnsFactory: () => [
        {
          prop: 'dictName',
          label: t('system.dict.dictName'),
          minWidth: 120
        },
        {
          prop: 'dictCode',
          label: t('system.dict.dictCode'),
          minWidth: 150,
          showOverflowTooltip: true,
          formatter: (row: DictTypeListItem) => {
            return h('code', { class: 'dict-code' }, row.dictCode)
          }
        },
        {
          prop: 'status',
          label: t('system.common.status'),
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
          label: t('system.common.remark'),
          minWidth: 150,
          showOverflowTooltip: true
        },
        {
          prop: 'action',
          label: t('system.common.actions'),
          width: 120,
          fixed: 'right' as const,
          formatter: getDictTypeActions
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
  const dataFormFilters = ref({
    dictCode: '',
    label: undefined as string | undefined
  })

  const {
    columns: dataColumns,
    data: dataData,
    loading: dataLoading,
    pagination: dataPagination,
    getData: getDataData,
    refreshRemove: refreshDataRemove,
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
        ...dataFormFilters.value
      })),
      immediate: false, // 不自动加载，需要先选择字典类型
      paginationKey: {
        current: 'pageNum',
        size: 'pageSize'
      },
      columnsFactory: () => [
        {
          prop: 'label',
          label: t('system.dict.dictLabel'),
          minWidth: 120
        },
        {
          prop: 'value',
          label: t('system.dict.dictValue'),
          minWidth: 120,
          formatter: (row: DictDataListItem) => {
            return h('code', { class: 'dict-value' }, row.value)
          }
        },
        {
          prop: 'listClass',
          label: t('system.dict.style'),
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
          label: t('system.common.sort'),
          width: 80
        },
        {
          prop: 'status',
          label: t('system.common.status'),
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
          label: t('system.common.remark'),
          minWidth: 150,
          showOverflowTooltip: true
        },
        {
          prop: 'action',
          label: t('system.common.actions'),
          width: 120,
          fixed: 'right' as const,
          formatter: getDictDataActions
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
  const typeDialogType = ref<DialogType>('add')
  const currentTypeData = ref<DictTypeListItem | undefined>(undefined)

  const dataDialogVisible = ref(false)
  const dataDialogType = ref<DialogType>('add')
  const currentDataData = ref<DictDataListItem | undefined>(undefined)

  // 类型相关方法
  const handleTypeSearch = () => {
    typeFormFilters.value.pageNum = 1
    getTypeData()
  }

  const handleTypeRowClick = (row: DictTypeListItem) => {
    selectedTypeId.value = row.id
    selectedDictCode.value = row.dictCode
    dataFormFilters.value.dictCode = row.dictCode
    dataFormFilters.value.label = undefined
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
        t('system.dict.deleteTypeConfirm', { name: row.dictName }),
        t('system.common.deleteConfirmTitle'),
        {
          confirmButtonText: t('common.confirm'),
          cancelButtonText: t('common.cancel'),
          type: 'warning'
        }
      )

      await fetchDeleteDictType(row.id)
      await refreshTypeRemove()
      // 如果删除的是当前选中的类型，清空右侧数据
      if (selectedTypeId.value === row.id) {
        selectedTypeId.value = null
        selectedDictCode.value = ''
        dataFormFilters.value.dictCode = ''
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
    if (!dataFormFilters.value.dictCode) return
    getDataData()
  }

  const showDataDialog = (type: 'add' | 'edit', data?: DictDataListItem) => {
    dataDialogType.value = type
    currentDataData.value = data
    dataDialogVisible.value = true
  }

  const deleteData = async (row: DictDataListItem) => {
    try {
      await ElMessageBox.confirm(
        t('system.dict.deleteDataConfirm', { name: row.label }),
        t('system.common.deleteConfirmTitle'),
        {
          confirmButtonText: t('common.confirm'),
          cancelButtonText: t('common.cancel'),
          type: 'warning'
        }
      )

      await fetchDeleteDictData(row.id)
      await refreshDataRemove()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除字典数据失败:', error)
      }
    }
  }

  const handleDataRefresh = () => {
    if (!dataFormFilters.value.dictCode) return
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
    color: var(--theme-color);
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
