<template>
  <ElDialog
    v-model="visible"
    :title="title"
    width="760px"
    align-center
    class="permission-assign-dialog"
  >
    <div class="permission-assign">
      <div v-if="banner" class="permission-assign__banner">
        <ArtSvgIcon icon="ri-information-line" class="permission-assign__banner-icon" />
        <span>{{ banner }}</span>
      </div>

      <div class="permission-assign__toolbar">
        <ElInput
          v-model="keyword"
          class="permission-assign__search"
          :placeholder="$t('system.permissionAssign.searchPlaceholder')"
          clearable
        >
          <template #prefix>
            <ArtSvgIcon icon="ri-search-line" class="permission-assign__search-icon" />
          </template>
        </ElInput>
        <span class="permission-assign__summary">
          {{
            $t('system.permissionAssign.selectedSummary', {
              selected: selectedCount,
              total: totalCount
            })
          }}
        </span>
        <ElCheckbox v-model="onlySelected" size="small">
          {{ $t('system.permissionAssign.onlySelected') }}
        </ElCheckbox>
        <ElCheckbox v-model="linkActions" size="small">
          {{ $t('system.permissionAssign.linkActions') }}
        </ElCheckbox>
      </div>

      <div v-loading="loading" class="permission-assign__body">
        <ElEmpty
          v-if="!loading && visibleGroups.length === 0"
          :description="emptyDescription"
          :image-size="72"
        />
        <ElScrollbar v-else height="52vh">
          <div v-for="group in visibleGroups" :key="group.id" class="permission-group">
            <div class="permission-group__header">
              <button
                type="button"
                class="permission-group__arrow"
                :aria-expanded="isGroupExpanded(group.id)"
                :aria-label="group.name"
                @click="toggleGroupExpanded(group.id)"
              >
                <ArtSvgIcon
                  :icon="
                    isGroupExpanded(group.id) ? 'ri-arrow-down-s-line' : 'ri-arrow-right-s-line'
                  "
                />
              </button>
              <ElCheckbox
                :model-value="isGroupChecked(group)"
                :indeterminate="isGroupIndeterminate(group)"
                :disabled="getGroupStats(group).total === 0"
                :aria-label="group.name"
                @change="(value) => onGroupChange(group, value)"
              />
              <i :class="getGroupIcon(group)" class="permission-group__icon"></i>
              <button
                type="button"
                class="permission-group__name"
                @click="toggleGroupExpanded(group.id)"
              >
                {{ group.name }}
              </button>
              <span class="permission-group__count">
                {{ getGroupStats(group).selected }}/{{ getGroupStats(group).total }}
              </span>
            </div>

            <div v-show="isGroupExpanded(group.id)" class="permission-group__pages">
              <div v-for="page in group.pages" :key="page.id" class="permission-page">
                <div class="permission-page__row">
                  <ElCheckbox
                    :model-value="selected.has(page.id)"
                    :disabled="page.disabled"
                    @change="(value) => onPageChange(page, value)"
                  >
                    <i :class="getPageIcon(page)" class="permission-page__icon"></i>
                    <span v-if="page.pathLabel" class="permission-page__path">
                      {{ page.pathLabel }} /
                    </span>
                    <span class="permission-page__name" :title="page.code">
                      <HighlightText :text="page.name" :keyword="keyword" />
                    </span>
                    <ElTag
                      v-if="page.disabled"
                      size="small"
                      type="info"
                      class="permission-page__tag"
                    >
                      {{ getDisabledText(page.disabledReason) }}
                    </ElTag>
                  </ElCheckbox>
                  <span v-if="page.actions.length" class="permission-page__count">
                    {{ getPageStats(page).selected }}/{{ getPageStats(page).total }}
                  </span>
                </div>

                <div v-if="page.actions.length" class="permission-page__actions">
                  <ElCheckbox
                    v-for="action in page.actions"
                    :key="action.id"
                    class="permission-chip"
                    :class="{ 'is-disabled': action.disabled }"
                    :model-value="selected.has(action.id)"
                    :disabled="action.disabled"
                    size="small"
                    @change="(value) => onActionChange(page, action, value)"
                  >
                    <span :title="action.code">
                      <HighlightText :text="action.name" :keyword="keyword" />
                    </span>
                  </ElCheckbox>
                </div>
              </div>
            </div>
          </div>
        </ElScrollbar>
      </div>
    </div>

    <template #footer>
      <div class="permission-assign__footer">
        <div class="permission-assign__footer-tools">
          <ElButton link @click="toggleExpandAll">
            {{
              isAllExpanded
                ? $t('system.permissionAssign.collapseAll')
                : $t('system.permissionAssign.expandAll')
            }}
          </ElButton>
          <ElButton link @click="resetSelection">
            {{ $t('system.permissionAssign.reset') }}
          </ElButton>
        </div>
        <ElSpace>
          <ElButton @click="handleClose">{{ $t('common.cancel') }}</ElButton>
          <ElButton
            type="primary"
            :loading="saving"
            :disabled="loading || menus.length === 0"
            @click="handleSave"
          >
            {{
              saving
                ? $t('system.common.saving')
                : $t('system.permissionAssign.saveWithCount', { count: selectedCount })
            }}
          </ElButton>
        </ElSpace>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n'
  import type { CheckboxValueType } from 'element-plus'
  import HighlightText from './highlight-text.vue'
  import {
    buildAncestorMap,
    buildPermissionGroups,
    buildPermissionPayload,
    normalizePermissionSelection,
    type PermissionActionNode,
    type PermissionDisabledReason,
    type PermissionGroupNode,
    type PermissionPageNode,
    type PermissionStats
  } from './helper'

  type MenuListItem = Api.SystemManage.MenuListItem

  defineOptions({ name: 'PermissionAssignDialog' })

  interface Props {
    modelValue: boolean
    title: string
    menus?: MenuListItem[]
    selectedIds?: number[]
    disabledIds?: number[]
    banner?: string
    loading?: boolean
    saving?: boolean
    emptyText?: string
  }

  const props = withDefaults(defineProps<Props>(), {
    menus: () => [],
    selectedIds: () => [],
    disabledIds: () => [],
    banner: '',
    loading: false,
    saving: false,
    emptyText: ''
  })

  const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void
    (e: 'save', ids: number[]): void
  }>()

  const { t } = useI18n()

  const visible = computed({
    get: () => props.modelValue,
    set: (value) => emit('update:modelValue', value)
  })

  const selected = ref<Set<number>>(new Set())
  const initialSelected = ref<Set<number>>(new Set())
  const keyword = ref('')
  const onlySelected = ref(false)
  const linkActions = ref(true)
  const expandedGroups = ref<Set<number>>(new Set())

  let initialized = false

  const disabledIdSet = computed(() => new Set(props.disabledIds))

  const groups = computed(() => buildPermissionGroups(props.menus, disabledIdSet.value))

  const ancestorMap = computed(() => buildAncestorMap(props.menus))

  const statsByPage = computed(() => {
    const stats = new Map<number, PermissionStats>()

    groups.value.forEach((group) => {
      group.pages.forEach((page) => {
        let selectedCount = 0
        let total = 0

        page.actions.forEach((action) => {
          if (action.disabled) return
          total += 1
          if (selected.value.has(action.id)) selectedCount += 1
        })

        stats.set(page.id, { selected: selectedCount, total })
      })
    })

    return stats
  })

  const selectedCount = computed(() => selected.value.size)

  const totalCount = computed(() =>
    groups.value.reduce((total, group) => {
      let count = 0
      group.pages.forEach((page) => {
        if (!page.disabled) count += 1
        page.actions.forEach((action) => {
          if (!action.disabled) count += 1
        })
      })
      return total + count
    }, 0)
  )

  const normalizedKeyword = computed(() => keyword.value.trim().toLowerCase())

  const pageMatches = (page: PermissionPageNode, search: string): boolean => {
    if (page.name.toLowerCase().includes(search)) return true
    if ((page.code ?? '').toLowerCase().includes(search)) return true
    return page.actions.some(
      (action) =>
        action.name.toLowerCase().includes(search) ||
        (action.code ?? '').toLowerCase().includes(search)
    )
  }

  const pageHasSelection = (page: PermissionPageNode): boolean =>
    selected.value.has(page.id) || page.actions.some((action) => selected.value.has(action.id))

  const visibleGroups = computed<PermissionGroupNode[]>(() => {
    const search = normalizedKeyword.value

    return groups.value
      .map((group) => ({
        ...group,
        pages: group.pages.filter((page) => {
          if (onlySelected.value && !pageHasSelection(page)) return false
          return !search || pageMatches(page, search)
        })
      }))
      .filter((group) => group.pages.length > 0)
  })

  const emptyDescription = computed(() => {
    if (props.emptyText) return props.emptyText
    if (groups.value.length === 0) return t('system.permissionAssign.empty')
    if (onlySelected.value && selectedCount.value === 0) {
      return t('system.permissionAssign.nothingSelected')
    }
    return t('system.permissionAssign.noResult')
  })

  const isAllExpanded = computed(
    () =>
      groups.value.length > 0 && groups.value.every((group) => expandedGroups.value.has(group.id))
  )

  const getGroupIcon = (group: PermissionGroupNode): string => group.icon || 'i-carbon-folder'

  const getPageIcon = (page: PermissionPageNode): string => page.icon || 'i-carbon-document'

  const getDisabledText = (reason?: PermissionDisabledReason): string =>
    reason === 'outOfScope'
      ? t('system.permissionAssign.outOfScope')
      : t('system.permissionAssign.disabled')

  // 分组统计基于当前可见页面，搜索或“仅看已选”生效时只统计过滤后的结果
  const getGroupStats = (group: PermissionGroupNode): PermissionStats => {
    let selectedCount = 0
    let total = 0

    group.pages.forEach((page) => {
      if (!page.disabled) {
        total += 1
        if (selected.value.has(page.id)) selectedCount += 1
      }
      page.actions.forEach((action) => {
        if (action.disabled) return
        total += 1
        if (selected.value.has(action.id)) selectedCount += 1
      })
    })

    return { selected: selectedCount, total }
  }

  const getPageStats = (page: PermissionPageNode): PermissionStats =>
    statsByPage.value.get(page.id) ?? { selected: 0, total: 0 }

  const isGroupExpanded = (groupId: number): boolean => expandedGroups.value.has(groupId)

  const toggleGroupExpanded = (groupId: number) => {
    const next = new Set(expandedGroups.value)
    if (next.has(groupId)) {
      next.delete(groupId)
    } else {
      next.add(groupId)
    }
    expandedGroups.value = next
  }

  const expandAllGroups = () => {
    expandedGroups.value = new Set(groups.value.map((group) => group.id))
  }

  const toggleExpandAll = () => {
    if (isAllExpanded.value) {
      expandedGroups.value = new Set()
    } else {
      expandAllGroups()
    }
  }

  const isGroupChecked = (group: PermissionGroupNode): boolean => {
    const stats = getGroupStats(group)
    return stats.total > 0 && stats.selected === stats.total
  }

  const isGroupIndeterminate = (group: PermissionGroupNode): boolean => {
    const stats = getGroupStats(group)
    return stats.selected > 0 && stats.selected < stats.total
  }

  const onGroupChange = (group: PermissionGroupNode, value: CheckboxValueType) => {
    const checked = Boolean(value)
    const next = new Set(selected.value)

    group.pages.forEach((page) => {
      if (!page.disabled) {
        if (checked) next.add(page.id)
        else next.delete(page.id)
      }
      page.actions.forEach((action) => {
        if (action.disabled) return
        if (checked) next.add(action.id)
        else next.delete(action.id)
      })
    })

    selected.value = next
  }

  const onPageChange = (page: PermissionPageNode, value: CheckboxValueType) => {
    const checked = Boolean(value)
    const next = new Set(selected.value)

    if (checked) {
      next.add(page.id)
      if (linkActions.value) {
        page.actions.forEach((action) => {
          if (!action.disabled) next.add(action.id)
        })
      }
    } else {
      next.delete(page.id)
      page.actions.forEach((action) => next.delete(action.id))
    }

    selected.value = next
  }

  const onActionChange = (
    page: PermissionPageNode,
    action: PermissionActionNode,
    value: CheckboxValueType
  ) => {
    const checked = Boolean(value)
    const next = new Set(selected.value)

    if (checked) {
      next.add(action.id)
      if (!page.disabled) next.add(page.id)
    } else {
      next.delete(action.id)
    }

    selected.value = next
  }

  const resetSelection = () => {
    selected.value = new Set(initialSelected.value)
  }

  const initialize = () => {
    const normalized = normalizePermissionSelection(props.selectedIds, groups.value)
    selected.value = new Set(normalized)
    initialSelected.value = new Set(normalized)
    keyword.value = ''
    onlySelected.value = false
    linkActions.value = true
    expandedGroups.value = new Set(groups.value.map((group) => group.id))
  }

  const tryInitialize = () => {
    if (!props.modelValue || props.loading || initialized) return
    initialized = true
    initialize()
  }

  watch(
    () => props.modelValue,
    (open) => {
      initialized = false
      if (open) nextTick(tryInitialize)
    }
  )

  watch([() => props.menus, () => props.selectedIds, () => props.loading], () => tryInitialize(), {
    immediate: true
  })

  watch(normalizedKeyword, (search) => {
    if (search) expandAllGroups()
  })

  watch(onlySelected, (value) => {
    if (value) expandAllGroups()
  })

  const handleClose = () => {
    visible.value = false
  }

  const handleSave = () => {
    emit('save', buildPermissionPayload(selected.value, ancestorMap.value))
  }
</script>

<style scoped lang="scss">
  .permission-assign {
    &__banner {
      display: flex;
      gap: 8px;
      align-items: flex-start;
      padding: 8px 12px;
      margin-bottom: 12px;
      font-size: 12px;
      line-height: 1.6;
      color: var(--el-text-color-regular);
      background: var(--el-color-primary-light-9);
      border-radius: 6px;
    }

    &__banner-icon {
      flex-shrink: 0;
      margin-top: 0.3em;
      color: var(--el-color-primary);
    }

    &__toolbar {
      display: flex;
      flex-wrap: wrap;
      gap: 12px;
      align-items: center;
      padding-bottom: 10px;
    }

    &__search {
      width: 260px;
    }

    &__search-icon {
      color: var(--el-text-color-placeholder);
    }

    &__summary {
      margin-left: auto;
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }

    &__body {
      min-height: 240px;
      overflow: hidden;
      border: 1px solid var(--el-border-color-lighter);
      border-radius: 8px;

      // 粘性表头 z-index: 2，滚动条需要更高才能保持可见
      :deep(.el-scrollbar__bar) {
        z-index: 3;
      }
    }

    &__footer {
      display: flex;
      align-items: center;
      justify-content: space-between;
      width: 100%;
    }

    &__footer-tools {
      display: flex;
      gap: 4px;
      align-items: center;
    }
  }

  .permission-group {
    &__header {
      position: sticky;
      top: 0;
      // Element Plus 复选框内部为 z-index: 1，表头必须更高才能盖住滚动内容
      z-index: 2;
      display: flex;
      gap: 6px;
      align-items: center;
      padding: 8px 12px;
      background: var(--el-fill-color-light);
      border-bottom: 1px solid var(--el-border-color-lighter);
    }

    &__arrow {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 20px;
      height: 20px;
      padding: 0;
      color: var(--el-text-color-secondary);
      cursor: var(--cursor-pointer);
      background: transparent;
      border: 0;
      border-radius: 4px;

      &:hover {
        background: var(--el-fill-color);
      }
    }

    &__icon {
      font-size: 16px;
      color: var(--el-text-color-secondary);
    }

    &__name {
      padding: 0;
      font-size: 13px;
      font-weight: 600;
      color: var(--el-text-color-primary);
      cursor: var(--cursor-pointer);
      background: transparent;
      border: 0;
    }

    &__count {
      margin-left: auto;
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }

    &__pages {
      padding: 4px 12px 8px 40px;
    }
  }

  .permission-page {
    border-bottom: 1px dashed var(--el-border-color-lighter);

    &:last-child {
      border-bottom: 0;
    }

    &__row {
      display: flex;
      align-items: center;
      min-height: 34px;
    }

    &__icon {
      margin-right: 6px;
      font-size: 15px;
      color: var(--el-text-color-secondary);
    }

    &__path {
      margin-right: 4px;
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }

    &__name {
      font-size: 13px;
      color: var(--el-text-color-regular);
    }

    &__tag {
      margin-left: 8px;
    }

    &__count {
      margin-left: auto;
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }

    &__actions {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
      padding: 0 0 8px 24px;
    }
  }

  .permission-chip {
    height: 26px;
    padding: 0 8px;
    margin-right: 0;
    background: var(--el-fill-color-lighter);
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 6px;
    transition:
      border-color 0.2s,
      background-color 0.2s;

    :deep(.el-checkbox__label) {
      padding-left: 4px;
      font-size: 12px;
      color: var(--el-text-color-regular);
    }

    &:hover {
      border-color: var(--el-border-color);
    }

    &.is-checked {
      background: var(--el-color-primary-light-9);
      border-color: var(--el-color-primary-light-5);

      :deep(.el-checkbox__label) {
        color: var(--el-color-primary);
      }
    }

    &.is-disabled {
      opacity: 0.55;
    }
  }
</style>
