<template>
  <ElDialog
    v-model="visible"
    :title="
      $t('system.user.permission.title', {
        name: props.userData?.nickname || props.userData?.username || ''
      })
    "
    width="520px"
    align-center
    class="el-dialog-border"
    @close="handleClose"
  >
    <ElScrollbar height="60vh" v-loading="loading">
      <!-- 如果用户没有角色，显示提示信息 -->
      <div v-if="!canAssign" class="no-roles-tip">
        <ElEmpty :description="$t('system.user.permission.noRoleTip')" />
      </div>
      <!-- 如果用户有角色，显示菜单树 -->
      <ElTree
        v-else
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
              {{ $t('system.user.permission.disabled') }}
            </ElTag>
          </div>
        </template>
      </ElTree>
    </ElScrollbar>

    <template #footer>
      <ElSpace>
        <ElButton @click="toggleExpandAll" :disabled="!canAssign">
          <template #icon>
            <ArtSvgIcon :icon="isExpandAll ? 'ri-skip-down-line' : 'ri-skip-up-line'" />
          </template>
          {{ isExpandAll ? $t('system.common.collapseAll') : $t('system.common.expandAll') }}
        </ElButton>
        <ElButton @click="toggleSelectAll" :disabled="!canAssign">
          <template #icon>
            <ArtSvgIcon
              :icon="isSelectAll ? 'ri-indeterminate-circle-line' : 'ri-checkbox-circle-line'"
            />
          </template>
          {{ isSelectAll ? $t('system.common.unselectAll') : $t('system.common.selectAll') }}
        </ElButton>
        <ElButton @click="handleClose">{{ $t('common.cancel') }}</ElButton>
        <ElButton
          type="primary"
          :loading="submitLoading"
          @click="savePermission"
          :disabled="!canAssign"
        >
          {{ submitLoading ? $t('system.common.saving') : $t('system.common.save') }}
        </ElButton>
      </ElSpace>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import {
    fetchGetUserPermissions,
    fetchAssignUserPermissions,
    fetchGetUserAvailableMenus,
    fetchGetMenuTreeForPermission
  } from '@/api/system-manage'
  import { usePermissionTree } from '@/composables/usePermissionTree'

  type UserListItem = Api.SystemManage.UserListItem
  type MenuListItem = Api.SystemManage.MenuListItem

  interface Props {
    modelValue: boolean
    userData?: UserListItem
  }

  interface Emits {
    (e: 'update:modelValue', value: boolean): void
    (e: 'success'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    modelValue: false,
    userData: undefined
  })
  const emit = defineEmits<Emits>()

  const visible = computed({
    get: () => props.modelValue,
    set: (value) => emit('update:modelValue', value)
  })

  let availableMenuIds: number[] = []

  const filterAvailableMenus = (menus: MenuListItem[], ids: number[]): MenuListItem[] =>
    menus
      .filter((menu) => ids.includes(menu.id))
      .map((menu) =>
        menu.children?.length
          ? { ...menu, children: filterAvailableMenus(menu.children, ids) }
          : menu
      )

  const {
    treeRef,
    loading,
    submitLoading,
    isExpandAll,
    isSelectAll,
    menuTree,
    canAssign,
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
    getTargetId: () => props.userData?.id,
    beforeLoad: async () => {
      availableMenuIds = await fetchGetUserAvailableMenus(props.userData!.id)
      return availableMenuIds.length > 0
    },
    fetchMenuTree: async () =>
      filterAvailableMenus(await fetchGetMenuTreeForPermission(), availableMenuIds),
    fetchCheckedPermissions: () => fetchGetUserPermissions(props.userData!.id),
    savePermissions: (menuIds) => fetchAssignUserPermissions(props.userData!.id, menuIds)
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
    availableMenuIds = []
  }

  const savePermission = async () => {
    if (await savePermissionTree()) {
      emit('success')
      handleClose()
    }
  }
</script>

<style scoped lang="scss">
  .no-roles-tip {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
    min-height: 200px;
  }
</style>
