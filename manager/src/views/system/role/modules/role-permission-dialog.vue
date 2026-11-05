<template>
  <ElDialog
    v-model="visible"
    :title="$t('system.role.permission.title', { name: props.roleData?.roleName || '' })"
    width="520px"
    align-center
    class="el-dialog-border"
    @close="handleClose"
  >
    <ElScrollbar height="60vh" v-loading="loading">
      <ElTree
        ref="treeRef"
        :data="menuTree"
        show-checkbox
        node-key="id"
        :default-expand-all="isExpandAll"
        :props="defaultProps"
        @check="handleTreeCheck"
      >
        <template #default="{ data }">
          <div
            style="display: flex; align-items: center"
            :style="
              data.status === 0 ? { opacity: 0.5, color: 'var(--el-text-color-disabled)' } : {}
            "
          >
            <i :class="getMenuIcon(data)" style="margin-right: 8px"></i>
            <span>{{ data.menuName }}</span>
            <ElTag
              v-if="data.menuType"
              :type="getMenuTypeTag(data.menuType)"
              size="small"
              style="margin-left: 8px"
            >
              {{ getMenuTypeText(data.menuType) }}
            </ElTag>
            <ElTag v-if="data.status === 0" type="info" size="small" style="margin-left: 8px">
              {{ $t('system.role.permission.disabled') }}
            </ElTag>
          </div>
        </template>
      </ElTree>
    </ElScrollbar>

    <template #footer>
      <ElSpace>
        <ElButton @click="toggleExpandAll">
          <template #icon>
            <ArtSvgIcon :icon="isExpandAll ? 'ri-skip-down-line' : 'ri-skip-up-line'" />
          </template>
          {{ isExpandAll ? $t('system.common.collapseAll') : $t('system.common.expandAll') }}
        </ElButton>
        <ElButton @click="toggleSelectAll">
          <template #icon>
            <ArtSvgIcon
              :icon="isSelectAll ? 'ri-indeterminate-circle-line' : 'ri-checkbox-circle-line'"
            />
          </template>
          {{ isSelectAll ? $t('system.common.unselectAll') : $t('system.common.selectAll') }}
        </ElButton>
        <ElButton @click="handleClose">{{ $t('common.cancel') }}</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="savePermission">
          {{ submitLoading ? $t('system.common.saving') : $t('system.common.save') }}
        </ElButton>
      </ElSpace>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import {
    fetchGetRolePermissions,
    fetchAssignRolePermissions,
    fetchGetMenuTreeForPermission
  } from '@/api/system-manage'
  import { usePermissionTree } from '@/composables/usePermissionTree'

  type RoleListItem = Api.SystemManage.RoleListItem
  type MenuListItem = Api.SystemManage.MenuListItem

  interface Props {
    modelValue: boolean
    roleData?: RoleListItem
  }

  interface Emits {
    (e: 'update:modelValue', value: boolean): void
    (e: 'success'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    modelValue: false,
    roleData: undefined
  })
  const emit = defineEmits<Emits>()

  const visible = computed({
    get: () => props.modelValue,
    set: (value) => emit('update:modelValue', value)
  })

  const filterTopMenu = (menus: MenuListItem[]): MenuListItem[] =>
    menus
      .filter((menu) => menu.id !== 0)
      .map((menu) =>
        menu.children?.length ? { ...menu, children: filterTopMenu(menu.children) } : menu
      )

  const {
    treeRef,
    loading,
    submitLoading,
    isExpandAll,
    isSelectAll,
    menuTree,
    defaultProps,
    getMenuIcon,
    getMenuTypeTag,
    getMenuTypeText,
    loadData,
    reset,
    savePermission: savePermissionTree,
    toggleExpandAll,
    toggleSelectAll,
    handleTreeCheck
  } = usePermissionTree({
    getTargetId: () => props.roleData?.id,
    fetchMenuTree: async () => filterTopMenu(await fetchGetMenuTreeForPermission()),
    fetchCheckedPermissions: () => fetchGetRolePermissions(props.roleData!.id),
    savePermissions: (menuIds) => fetchAssignRolePermissions(props.roleData!.id, menuIds)
  })

  watch(
    () => props.modelValue,
    (newVal) => {
      if (newVal) loadData()
    }
  )

  const handleClose = () => {
    visible.value = false
    reset()
  }

  const savePermission = async () => {
    if (await savePermissionTree()) {
      emit('success')
      handleClose()
    }
  }
</script>
