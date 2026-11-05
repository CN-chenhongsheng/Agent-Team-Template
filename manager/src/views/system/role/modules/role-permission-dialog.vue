<template>
  <PermissionAssignDialog
    v-model="visible"
    :title="$t('system.role.permission.title', { name: props.roleData?.roleName || '' })"
    :menus="menuTree"
    :selected-ids="selectedIds"
    :loading="loading"
    :saving="saving"
    @save="handleSave"
  />
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n'
  import {
    fetchAssignRolePermissions,
    fetchGetMenuTreeForPermission,
    fetchGetRolePermissions
  } from '@/api/system-manage'
  import PermissionAssignDialog from '@/views/system/modules/permission-assign-dialog/index.vue'

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

  const { t } = useI18n()

  const visible = computed({
    get: () => props.modelValue,
    set: (value) => emit('update:modelValue', value)
  })

  const menuTree = ref<MenuListItem[]>([])
  const selectedIds = ref<number[]>([])
  const loading = ref(false)
  const saving = ref(false)

  const loadData = async () => {
    const roleId = props.roleData?.id
    if (!roleId) return

    loading.value = true
    try {
      const [tree, permissions] = await Promise.all([
        fetchGetMenuTreeForPermission(),
        fetchGetRolePermissions(roleId)
      ])
      menuTree.value = tree
      selectedIds.value = permissions.map((item) => item.menuId)
    } catch (error) {
      console.error('加载角色权限失败:', error)
    } finally {
      loading.value = false
    }
  }

  watch(
    () => props.modelValue,
    (open) => {
      if (open) loadData()
    }
  )

  const handleSave = async (menuIds: number[]) => {
    const roleId = props.roleData?.id
    if (!roleId) {
      ElMessage.warning(t('system.common.incompleteData'))
      return
    }

    saving.value = true
    try {
      await fetchAssignRolePermissions(roleId, menuIds)
      emit('success')
      visible.value = false
    } catch (error) {
      console.error('保存角色权限失败:', error)
    } finally {
      saving.value = false
    }
  }
</script>
