<!-- 字典管理工作台 -->
<template>
  <div class="dict-page art-full-height">
    <ElCard class="art-table-card dict-workbench" shadow="never" style="margin-top: 0">
      <div class="dict-workbench__body">
        <!-- 左侧：字典类型导航 -->
        <aside class="dict-sidebar">
          <div class="dict-sidebar__header">
            <div class="dict-sidebar__heading">
              <ArtSvgIcon icon="ri:book-open-line" class="dict-sidebar__heading-icon" />
              <span class="dict-sidebar__title">{{ $t('system.dict.typeTitle') }}</span>
              <span class="dict-sidebar__badge">{{ filteredTypes.length }}</span>
            </div>
          </div>

          <div class="dict-sidebar__search">
            <ElInput
              v-model="typeKeyword"
              :placeholder="$t('system.dict.searchTypePlaceholder')"
              clearable
              @keyup.enter="selectFirstFilteredType"
            >
              <template #prefix>
                <ArtSvgIcon icon="ri:search-line" />
              </template>
            </ElInput>
          </div>

          <div
            ref="typeListRef"
            v-loading="typeLoading"
            class="dict-sidebar__list"
            role="listbox"
            :aria-label="$t('system.dict.typeTitle')"
            tabindex="0"
            @keydown.up.prevent="moveSelection(-1)"
            @keydown.down.prevent="moveSelection(1)"
          >
            <div
              v-for="type in filteredTypes"
              :key="type.id"
              class="dict-type-item"
              :class="{ 'is-active': type.id === selectedTypeId, 'is-disabled': type.status !== 1 }"
              role="option"
              :aria-selected="type.id === selectedTypeId"
              @click="handleTypeSelect(type)"
              @contextmenu="handleTypeContextmenu($event, type)"
            >
              <span
                class="dict-type-item__dot"
                :class="type.status === 1 ? 'is-on' : 'is-off'"
                :title="getTypeStatusText(type)"
              ></span>
              <span class="dict-type-item__main">
                <span class="dict-type-item__name" :title="type.dictName">{{ type.dictName }}</span>
                <code class="dict-type-item__code" :title="type.dictCode">{{ type.dictCode }}</code>
              </span>
              <span v-if="type.dataCount !== undefined" class="dict-type-item__count">
                {{ type.dataCount }}
              </span>
              <ElDropdown
                v-if="canEditType || canDeleteType"
                class="dict-type-item__actions"
                trigger="click"
                @click.stop
                @command="(command) => handleTypeCommand(command, type)"
              >
                <button
                  type="button"
                  class="dict-type-item__more"
                  :aria-label="$t('system.common.more')"
                  @click.stop
                >
                  <ArtSvgIcon icon="ri:more-2-fill" />
                </button>
                <template #dropdown>
                  <ElDropdownMenu>
                    <ElDropdownItem v-if="canEditType" command="edit">
                      {{ $t('system.common.edit') }}
                    </ElDropdownItem>
                    <ElDropdownItem v-if="canEditType" command="toggle">
                      {{
                        type.status === 1
                          ? $t('system.common.disabled')
                          : $t('system.common.enabled')
                      }}
                    </ElDropdownItem>
                    <ElDropdownItem v-if="canDeleteType" command="delete" :divided="canEditType">
                      {{ $t('system.common.delete') }}
                    </ElDropdownItem>
                  </ElDropdownMenu>
                </template>
              </ElDropdown>
            </div>

            <ElEmpty
              v-if="!typeLoading && filteredTypes.length === 0"
              :description="
                typeKeyword ? $t('system.dict.noMatchingType') : $t('system.dict.emptyType')
              "
              :image-size="60"
            />
          </div>

          <div v-permission="PERMISSIONS.system.dict.type.add" class="dict-sidebar__footer">
            <ElButton class="dict-sidebar__add" v-ripple @click="showTypeDialog('add')">
              <ArtSvgIcon icon="ri-add-line" class="dict-sidebar__add-icon" />
              <span>{{ $t('system.dict.addType') }}</span>
            </ElButton>
          </div>
        </aside>

        <!-- 右侧：字典数据工作区 -->
        <section class="dict-main">
          <div class="dict-main__context">
            <div class="dict-main__heading">
              <ArtSvgIcon icon="ri:list-check" class="dict-main__heading-icon" />
              <span class="dict-main__title">{{ $t('system.dict.dataTitle') }}</span>
              <template v-if="selectedType">
                <span class="dict-main__separator">·</span>
                <span class="dict-main__name">{{ selectedType.dictName }}</span>
              </template>
            </div>
            <div v-if="selectedType" class="dict-main__meta">
              <code class="dict-main__code">{{ selectedType.dictCode }}</code>
              <ElTooltip :content="$t('system.dict.copyCode')" placement="top">
                <ElButton link size="small" class="dict-main__copy" @click="copyDictCode">
                  <ArtSvgIcon icon="ri:file-copy-line" />
                </ElButton>
              </ElTooltip>
              <span class="dict-main__count">
                {{ $t('system.dict.itemCount', { count: dataPagination.total }) }}
              </span>
            </div>
          </div>

          <div v-if="selectedType" class="dict-main__workspace">
            <ArtTableHeader
              v-model:columns="dataColumnChecks"
              :loading="dataLoading"
              layout="refresh,size,fullscreen,columns,settings"
              @refresh="handleDataRefresh"
            >
              <template #left>
                <ElSpace wrap>
                  <ElButton
                    v-ripple
                    v-permission="PERMISSIONS.system.dict.data.add"
                    @click="showDataDialog('add')"
                  >
                    {{ $t('system.dict.addData') }}
                  </ElButton>
                  <ElInput
                    v-model="dataFormFilters.label"
                    class="dict-main__search"
                    :placeholder="$t('system.dict.searchLabel')"
                    clearable
                    @clear="handleDataSearch"
                    @keyup.enter="handleDataSearch"
                  >
                    <template #prefix>
                      <ArtSvgIcon icon="ri:search-line" />
                    </template>
                  </ElInput>
                </ElSpace>
              </template>
            </ArtTableHeader>

            <ArtTable
              :loading="dataLoading"
              :data="dataData"
              :columns="dataColumns"
              :pagination="dataPagination"
              :empty-text="$t('system.dict.emptyData')"
              empty-height="calc(100% - 44px)"
              :contextMenuItems="dataContextMenuItems"
              :contextMenuWidth="dataContextMenuWidth"
              :onRowContextmenu="handleDataRowContextmenu as any"
              :onContextMenuSelect="handleDataContextMenuSelect"
              @pagination:size-change="handleDataSizeChange"
              @pagination:current-change="handleDataCurrentChange"
            />
          </div>

          <div v-else v-loading="typeLoading" class="dict-main__empty">
            <ElEmpty
              v-if="!typeLoading"
              :description="
                typeList.length === 0
                  ? $t('system.dict.emptyTypeTip')
                  : $t('system.dict.selectTypeFirst')
              "
              :image-size="90"
            >
              <ElButton
                v-if="typeList.length === 0"
                type="primary"
                v-permission="PERMISSIONS.system.dict.type.add"
                @click="showTypeDialog('add')"
              >
                {{ $t('system.dict.addType') }}
              </ElButton>
            </ElEmpty>
          </div>
        </section>
      </div>
    </ElCard>

    <!-- 字典类型右键菜单 -->
    <ArtMenuRight
      ref="typeMenuRef"
      :menu-items="typeContextMenuItems"
      :menu-width="130"
      @select="handleTypeContextMenuSelect"
      @hide="typeContextRow = null"
    />

    <!-- 字典类型弹窗 -->
    <DictTypeDialog
      v-model="typeDialogVisible"
      :dialog-type="typeDialogType"
      :type-data="currentTypeData"
      @success="handleTypeSuccess"
    />

    <!-- 字典数据弹窗 -->
    <DictDataDialog
      v-model="dataDialogVisible"
      :dialog-type="dataDialogType"
      :data-data="currentDataData"
      :dict-code="selectedDictCode"
      :dict-name="selectedType?.dictName || ''"
      @success="handleDataSuccess"
    />
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n'
  import { h } from 'vue'
  import { useTable } from '@/hooks/core/useTable'
  import { hasPermission } from '@/directives/core/permission'
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
  import ArtSwitch from '@/components/core/forms/art-switch/index.vue'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import type { ActionButtonConfig } from '@/types/component'
  import type { MenuItemType } from '@/components/core/others/art-menu-right/index.vue'
  import type { DialogType } from '@/types'
  import { PERMISSIONS } from '@/utils/constants'

  defineOptions({ name: 'Dict' })

  const { t } = useI18n()

  type DictTypeListItem = Api.SystemManage.DictTypeListItem & { _statusLoading?: boolean }
  type DictDataListItem = Api.SystemManage.DictDataListItem & { _statusLoading?: boolean }
  type TagType = 'primary' | 'success' | 'info' | 'warning' | 'danger'

  const TYPE_PAGE_SIZE = 1000
  const TYPE_STORAGE_KEY = 'dict:selected-type-id'
  const TAG_TYPES: TagType[] = ['primary', 'success', 'info', 'warning', 'danger']

  const canEditType = computed(() => hasPermission(PERMISSIONS.system.dict.type.edit))
  const canDeleteType = computed(() => hasPermission(PERMISSIONS.system.dict.type.delete))

  // 字典类型导航
  const typeList = ref<DictTypeListItem[]>([])
  const typeLoading = ref(false)
  const typeKeyword = ref('')
  const typeListRef = ref<HTMLElement>()
  const typeMenuRef = ref<{ show: (event: MouseEvent) => void }>()
  const typeContextRow = ref<DictTypeListItem | null>(null)
  const selectedTypeId = ref<number | null>(null)

  const selectedType = computed<DictTypeListItem | null>(
    () => typeList.value.find((item) => item.id === selectedTypeId.value) ?? null
  )

  const selectedDictCode = computed(() => selectedType.value?.dictCode ?? '')

  const filteredTypes = computed(() => {
    const keyword = typeKeyword.value.trim().toLowerCase()
    if (!keyword) return typeList.value
    return typeList.value.filter(
      (item) =>
        item.dictName.toLowerCase().includes(keyword) ||
        item.dictCode.toLowerCase().includes(keyword)
    )
  })

  const getTagType = (value?: string): TagType =>
    TAG_TYPES.includes(value as TagType) ? (value as TagType) : 'info'

  const getTypeStatusText = (type: DictTypeListItem): string =>
    type.status === 1 ? t('system.dict.enabled') : t('system.dict.disabled')

  const loadTypes = async () => {
    typeLoading.value = true
    try {
      const res = await fetchGetDictTypePage({ pageNum: 1, pageSize: TYPE_PAGE_SIZE })
      typeList.value = res.list || []
    } catch (error) {
      console.error('加载字典类型失败:', error)
    } finally {
      typeLoading.value = false
    }
  }

  // 字典数据
  const dataFormFilters = ref({
    dictCode: '',
    label: undefined as string | undefined
  })

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
    columns: dataColumns,
    data: dataData,
    loading: dataLoading,
    pagination: dataPagination,
    columnChecks: dataColumnChecks,
    getData: getDataData,
    refreshData,
    refreshCreate,
    refreshUpdate,
    refreshRemove,
    clearData,
    handleSizeChange: handleDataSizeChange,
    handleCurrentChange: handleDataCurrentChange,
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
      immediate: false, // 先选择字典类型再加载数据
      paginationKey: {
        current: 'pageNum',
        size: 'pageSize'
      },
      columnsFactory: () => [
        {
          prop: 'label',
          label: t('system.dict.dictLabel'),
          minWidth: 120,
          formatter: (row: DictDataListItem) => {
            return h('div', { class: 'dict-label-cell' }, [
              h('span', { class: 'dict-label-cell__text' }, row.label),
              row.isDefault === 1
                ? h(
                    ElTag,
                    { type: 'warning', size: 'small', class: 'dict-label-cell__badge' },
                    () => t('system.dict.defaultTag')
                  )
                : null
            ])
          }
        },
        {
          prop: 'value',
          label: t('system.dict.dictValue'),
          minWidth: 100,
          formatter: (row: DictDataListItem) => {
            return h('code', { class: 'dict-code' }, row.value)
          }
        },
        {
          prop: 'listClass',
          label: t('system.dict.style'),
          width: 120,
          formatter: (row: DictDataListItem) => {
            if (!row.listClass) {
              return h('span', { class: 'dict-style-empty' }, '-')
            }
            return h(
              ElTag,
              { type: getTagType(row.listClass), size: 'small', class: 'dict-style-preview' },
              () => row.label || row.listClass
            )
          }
        },
        {
          prop: 'sort',
          label: t('system.common.sort'),
          width: 70
        },
        {
          prop: 'status',
          label: t('system.common.status'),
          width: 90,
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
          minWidth: 120,
          showOverflowTooltip: true
        },
        {
          prop: 'action',
          label: t('system.common.actions'),
          width: 110,
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

  const handleTypeSelect = (type: DictTypeListItem) => {
    selectedTypeId.value = type.id
    dataFormFilters.value.dictCode = type.dictCode
    dataFormFilters.value.label = undefined
    localStorage.setItem(TYPE_STORAGE_KEY, String(type.id))
    nextTick(() => {
      getDataData()
      typeListRef.value?.focus()
    })
  }

  const clearSelection = () => {
    selectedTypeId.value = null
    dataFormFilters.value.dictCode = ''
    dataFormFilters.value.label = undefined
    clearData()
    localStorage.removeItem(TYPE_STORAGE_KEY)
  }

  const moveSelection = (delta: number) => {
    const list = filteredTypes.value
    if (!list.length) return
    const index = list.findIndex((item) => item.id === selectedTypeId.value)
    const start = index >= 0 ? index : delta > 0 ? -1 : list.length
    const next = Math.min(Math.max(start + delta, 0), list.length - 1)
    const target = list[next]
    if (target && target.id !== selectedTypeId.value) {
      handleTypeSelect(target)
    }
  }

  const selectFirstFilteredType = () => {
    const first = filteredTypes.value[0]
    if (first) handleTypeSelect(first)
  }

  // 字典类型弹窗
  const typeDialogVisible = ref(false)
  const typeDialogType = ref<DialogType>('add')
  const currentTypeData = ref<DictTypeListItem | undefined>(undefined)

  const showTypeDialog = (type: DialogType, data?: DictTypeListItem) => {
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
      const wasSelected = row.id === selectedTypeId.value
      const fallbackId = typeList.value.find((item) => item.id !== row.id)?.id
      await loadTypes()

      if (wasSelected) {
        const next = typeList.value.find((item) => item.id === fallbackId) || typeList.value[0]
        if (next) {
          handleTypeSelect(next)
        } else {
          clearSelection()
        }
      }
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除字典类型失败:', error)
      }
    }
  }

  const handleTypeStatusChange = async (row: DictTypeListItem, value: boolean) => {
    if (row._statusLoading) return

    const originalStatus = row.status
    row._statusLoading = true
    row.status = value ? 1 : 0

    try {
      const detail = await fetchGetDictTypeDetail(row.id)
      await fetchUpdateDictType(row.id, {
        ...detail,
        status: value ? 1 : 0
      })
    } catch (error) {
      row.status = originalStatus
      console.error('更新字典类型状态失败:', error)
    } finally {
      row._statusLoading = false
    }
  }

  const handleTypeCommand = (command: string | number | object, type: DictTypeListItem) => {
    if (command === 'edit') {
      showTypeDialog('edit', type)
    } else if (command === 'delete') {
      deleteType(type)
    } else if (command === 'toggle') {
      handleTypeStatusChange(type, type.status !== 1)
    }
  }

  const typeContextMenuItems = computed<MenuItemType[]>(() => {
    const type = typeContextRow.value
    if (!type) return []

    const items: MenuItemType[] = []

    if (canEditType.value) {
      items.push({
        key: 'edit',
        label: t('system.common.edit'),
        icon: 'ri:edit-line',
        onClick: () => showTypeDialog('edit', type)
      })
      items.push({
        key: 'toggle',
        label: type.status === 1 ? t('system.common.disabled') : t('system.common.enabled'),
        icon: type.status === 1 ? 'ri:pause-circle-line' : 'ri:play-circle-line',
        showLine: canDeleteType.value,
        onClick: () => handleTypeStatusChange(type, type.status !== 1)
      })
    }

    if (canDeleteType.value) {
      items.push({
        key: 'delete',
        label: t('system.common.delete'),
        icon: 'ri:delete-bin-line',
        onClick: () => deleteType(type)
      })
    }

    return items
  })

  const handleTypeContextmenu = (event: MouseEvent, type: DictTypeListItem) => {
    if (!canEditType.value && !canDeleteType.value) return

    event.preventDefault()
    event.stopPropagation()
    typeContextRow.value = type
    nextTick(() => typeMenuRef.value?.show(event))
  }

  const handleTypeContextMenuSelect = (item: MenuItemType) => {
    if (typeof item.onClick === 'function') {
      item.onClick()
    }
  }

  const handleTypeSuccess = async (payload: { action: DialogType; dictCode: string }) => {
    await loadTypes()

    if (payload.action === 'add') {
      const created = typeList.value.find((item) => item.dictCode === payload.dictCode)
      if (created) handleTypeSelect(created)
    }
  }

  const copyText = async (text: string) => {
    if (navigator.clipboard?.writeText) {
      await navigator.clipboard.writeText(text)
      return
    }

    const textarea = document.createElement('textarea')
    textarea.value = text
    textarea.style.position = 'fixed'
    textarea.style.opacity = '0'
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
  }

  const copyDictCode = async () => {
    if (!selectedDictCode.value) return
    try {
      await copyText(selectedDictCode.value)
      ElMessage.success(t('system.dict.copySuccess'))
    } catch (error) {
      console.error('复制字典编码失败:', error)
      ElMessage.error(t('system.dict.copyFailed'))
    }
  }

  // 字典数据弹窗
  const dataDialogVisible = ref(false)
  const dataDialogType = ref<DialogType>('add')
  const currentDataData = ref<DictDataListItem | undefined>(undefined)

  const showDataDialog = (type: DialogType, data?: DictDataListItem) => {
    dataDialogType.value = type
    currentDataData.value = data
    dataDialogVisible.value = true
  }

  const handleDataSearch = () => {
    if (!selectedDictCode.value) return
    getDataData()
  }

  const handleDataRefresh = () => {
    if (!selectedDictCode.value) return
    refreshData()
  }

  const adjustSelectedTypeCount = (delta: number) => {
    const type = selectedType.value
    if (!type) return
    type.dataCount = Math.max(0, (type.dataCount ?? 0) + delta)
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
      await refreshRemove()
      adjustSelectedTypeCount(-1)
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除字典数据失败:', error)
      }
    }
  }

  const handleDataSuccess = async (payload: { action: DialogType }) => {
    if (payload.action === 'add') {
      adjustSelectedTypeCount(1)
      await refreshCreate()
    } else {
      await refreshUpdate()
    }
  }

  const handleDataStatusChange = async (row: DictDataListItem, value: boolean) => {
    const originalStatus = row.status
    row._statusLoading = true
    row.status = value ? 1 : 0

    try {
      const detail = await fetchGetDictDataDetail(row.id)
      await fetchUpdateDictData(row.id, {
        ...detail,
        status: value ? 1 : 0
      })
    } catch (error) {
      row.status = originalStatus
      console.error('更新字典数据状态失败:', error)
    } finally {
      row._statusLoading = false
    }
  }

  onMounted(async () => {
    await loadTypes()

    if (!typeList.value.length) {
      clearSelection()
      return
    }

    const savedId = Number(localStorage.getItem(TYPE_STORAGE_KEY))
    const saved = typeList.value.find((item) => item.id === savedId)
    handleTypeSelect(saved || typeList.value[0])
  })
</script>

<style scoped lang="scss">
  .dict-workbench {
    :deep(.el-card__body) {
      padding: 0;
    }

    &__body {
      display: flex;
      height: 100%;
      min-height: 0;
    }
  }

  .dict-sidebar {
    display: flex;
    flex-direction: column;
    width: 300px;
    min-width: 300px;
    height: 100%;
    border-right: 1px solid var(--el-border-color-lighter);

    &__header {
      display: flex;
      flex-shrink: 0;
      align-items: center;
      padding: 16px 16px 0;
    }

    &__heading {
      display: flex;
      gap: 6px;
      align-items: center;
      min-width: 0;
    }

    &__heading-icon {
      font-size: 16px;
      color: var(--theme-color);
    }

    &__title {
      font-size: 14px;
      font-weight: 600;
      color: var(--theme-color);
      white-space: nowrap;
    }

    &__badge {
      padding: 0 6px;
      font-size: 11px;
      line-height: 18px;
      color: var(--el-text-color-secondary);
      background: var(--el-fill-color);
      border-radius: 999px;
    }

    &__search {
      flex-shrink: 0;
      padding: 12px 16px;
    }

    &__list {
      position: relative;
      flex: 1;
      min-height: 0;
      padding: 0 8px 12px;
      overflow-y: auto;
      outline: none;
    }

    &__footer {
      flex-shrink: 0;
      padding: 8px 8px 12px;
    }

    &__add {
      display: inline-flex;
      gap: 6px;
      align-items: center;
      justify-content: center;
      width: 100%;
      height: 34px;
      padding: 0 12px;
      font-size: 13px;
      color: var(--el-text-color-secondary);
      background: transparent;
      border: 0;
      border-radius: 6px;
      transition:
        color 0.2s,
        background-color 0.2s;

      &:hover {
        color: var(--el-color-primary);
        background: var(--el-fill-color-light);
      }
    }

    &__add-icon {
      margin-right: 6px;
      font-size: 15px;
    }
  }

  .dict-type-item {
    display: flex;
    gap: 8px;
    align-items: center;
    padding: 8px 10px 8px 12px;
    margin-bottom: 4px;
    cursor: var(--cursor-pointer);
    border-radius: 6px;
    transition: background-color 0.2s;

    &:not(.is-active):hover {
      background: var(--el-fill-color-light);
    }

    &.is-active {
      background: var(--el-color-primary-light-9);

      .dict-type-item__name {
        font-weight: 600;
        color: var(--el-color-primary);
      }
    }

    &.is-disabled:not(.is-active) .dict-type-item__name {
      color: var(--el-text-color-secondary);
    }

    &__dot {
      flex-shrink: 0;
      width: 7px;
      height: 7px;
      border-radius: 50%;

      &.is-on {
        background: var(--el-color-primary);
        box-shadow: 0 0 0 2px var(--el-color-primary-light-9);
      }

      &.is-off {
        background: var(--el-text-color-placeholder);
        box-shadow: 0 0 0 2px var(--el-fill-color);
      }
    }

    &__main {
      display: flex;
      flex: 1;
      flex-direction: column;
      gap: 2px;
      min-width: 0;
    }

    &__name {
      overflow: hidden;
      font-size: 13px;
      color: var(--el-text-color-regular);
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    &__code {
      overflow: hidden;
      font-family: 'Courier New', monospace;
      font-size: 11px;
      color: var(--el-text-color-secondary);
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    &__count {
      flex-shrink: 0;
      min-width: 22px;
      padding: 1px 6px;
      font-size: 11px;
      color: var(--el-text-color-secondary);
      text-align: center;
      background: var(--el-fill-color);
      border-radius: 999px;
    }

    &__actions {
      flex-shrink: 0;
      opacity: 0;
      transition: opacity 0.2s;
    }

    &:hover &__actions,
    &.is-active &__actions,
    &:focus-within &__actions {
      opacity: 1;
    }

    &__more {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 22px;
      height: 22px;
      padding: 0;
      font-size: 14px;
      color: var(--el-text-color-secondary);
      cursor: var(--cursor-pointer);
      background: transparent;
      border: 0;
      border-radius: 4px;

      &:hover {
        background: var(--el-fill-color);
      }
    }
  }

  .dict-main {
    display: flex;
    flex: 1;
    flex-direction: column;
    min-width: 0;
    min-height: 0;

    &__context {
      display: flex;
      flex-shrink: 0;
      flex-wrap: wrap;
      gap: 8px 16px;
      align-items: center;
      justify-content: space-between;
      padding: 14px 16px 11px;
    }

    &__heading {
      display: flex;
      gap: 6px;
      align-items: center;
      min-width: 0;
    }

    &__heading-icon {
      font-size: 16px;
      color: var(--theme-color);
    }

    &__title {
      font-size: 14px;
      font-weight: 600;
      color: var(--theme-color);
      white-space: nowrap;
    }

    &__separator {
      color: var(--el-text-color-placeholder);
    }

    &__name {
      overflow: hidden;
      font-size: 13px;
      color: var(--el-text-color-primary);
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    &__meta {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      align-items: center;
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }

    &__code {
      padding: 1px 6px;
      font-family: 'Courier New', monospace;
      font-size: 12px;
      color: var(--el-text-color-regular);
      background: var(--el-fill-color-light);
      border-radius: 4px;
    }

    &__copy {
      padding: 0;
      color: var(--el-text-color-secondary);
    }

    &__count {
      color: var(--el-text-color-secondary);
    }

    &__workspace {
      display: flex;
      flex: 1;
      flex-direction: column;
      min-height: 0;
      padding: 0 16px 12px;
      overflow: hidden;
    }

    &__search {
      width: 220px;
    }

    &__empty {
      position: relative;
      display: flex;
      flex: 1;
      align-items: center;
      justify-content: center;
      min-height: 0;
    }
  }

  :deep(.dict-label-cell) {
    display: flex;
    gap: 10px;
    align-items: center;
    min-width: 0;
    max-width: 100%;

    .dict-label-cell__text {
      min-width: 0;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .dict-label-cell__badge {
      flex-shrink: 0;
    }
  }

  :deep(.dict-style-preview) {
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  :deep(.dict-style-empty) {
    color: var(--el-text-color-placeholder);
  }

  :deep(.dict-code) {
    padding: 2px 6px;
    font-family: 'Courier New', monospace;
    font-size: 12px;
    background-color: var(--el-fill-color-light);
    border-radius: 3px;
  }

  @media (hover: none) {
    .dict-type-item__actions {
      opacity: 1;
    }
  }

  @media (width <= 1024px) {
    .dict-workbench__body {
      flex-direction: column;
    }

    .dict-sidebar {
      width: 100%;
      min-width: 0;
      max-height: 280px;
      border-right: 0;
      border-bottom: 1px solid var(--el-border-color-lighter);
    }

    .dict-main {
      min-height: 420px;
    }
  }
</style>
