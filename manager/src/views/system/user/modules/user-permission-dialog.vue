<template>
  <PermissionAssignDialog
    v-model="visible"
    :title="
      $t('system.user.permission.title', {
        name: props.userData?.nickname || props.userData?.username || ''
      })
    "
    :menus="canAssign ? menuTree : []"
    :selected-ids="selectedIds"
    :disabled-ids="disabledIds"
    :banner="banner"
    :loading="loading"
    :saving="saving"
    :empty-text="emptyText"
    @save="handleSave"
  />
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n'
  import {
    fetchAssignUserPermissions,
    fetchGetMenuTreeForPermission,
    fetchGetUserAvailableMenus,
    fetchGetUserPermissions
  } from '@/api/system-manage'
  import PermissionAssignDialog from '@/views/system/modules/permission-assign-dialog/index.vue'

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

  const { t } = useI18n()

  const visible = computed({
    get: () => props.modelValue,
    set: (value) => emit('update:modelValue', value)
  })

  const menuTree = ref<MenuListItem[]>([])
  const selectedIds = ref<number[]>([])
  const disabledIds = ref<number[]>([])
  const allowedIds = ref<Set<number>>(new Set())
  const canAssign = ref(true)
  const loading = ref(false)
  const saving = ref(false)

  const collectMenuIds = (menus: MenuListItem[]): number[] => {
    const ids: number[] = []
    const walk = (nodes: MenuListItem[]) => {
      nodes.forEach((node) => {
        ids.push(node.id)
        if (node.children?.length) walk(node.children)
      })
    }
    walk(menus)
    return ids
  }

  const banner = computed(() => {
    if (!canAssign.value) return ''
    const roleNames = props.userData?.roleNames?.length
      ? props.userData.roleNames.join(t('system.common.listSeparator'))
      : t('system.common.none')
    return t('system.permissionAssign.roleScopeTip', { roles: roleNames })
  })

  const emptyText = computed(() => (canAssign.value ? '' : t('system.user.permission.noRoleTip')))

  const loadData = async () => {
    const userId = props.userData?.id
    if (!userId) return

    loading.value = true
    try {
      const [tree, permissions, available] = await Promise.all([
        fetchGetMenuTreeForPermission(),
        fetchGetUserPermissions(userId),
        fetchGetUserAvailableMenus(userId)
      ])

      menuTree.value = tree
      selectedIds.value = permissions.map((item) => item.menuId)

      const allowed = new Set(available)
      allowedIds.value = allowed
      canAssign.value = allowed.size > 0
      disabledIds.value = canAssign.value
        ? collectMenuIds(tree).filter((id) => !allowed.has(id))
        : []
    } catch (error) {
      console.error('加载用户权限失败:', error)
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
    const userId = props.userData?.id
    if (!userId) {
      ElMessage.warning(t('system.common.incompleteData'))
      return
    }

    saving.value = true
    try {
      const payload = menuIds.filter((id) => allowedIds.value.has(id))
      await fetchAssignUserPermissions(userId, payload)
      emit('success')
      visible.value = false
    } catch (error) {
      console.error('保存用户权限失败:', error)
    } finally {
      saving.value = false
    }
  }
</script>
