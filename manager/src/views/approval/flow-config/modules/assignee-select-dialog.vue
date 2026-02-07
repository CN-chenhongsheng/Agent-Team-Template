<!-- 审批人选择弹窗 -->
<template>
  <ElDialog
    v-model="dialogVisible"
    title="选择审批人"
    width="1000px"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <!-- 顶部搜索 -->
    <div class="mb-5">
      <ElInput v-model="searchKeyword" placeholder="搜索角色或用户" clearable size="default">
        <template #prefix>
          <i class="ri-search-line"></i>
        </template>
      </ElInput>
    </div>

    <!-- 左右分栏布局 -->
    <div class="flex gap-4 h-[500px] overflow-hidden">
      <!-- 左侧：角色列表 -->
      <div class="flex flex-col w-[280px] shrink-0 pr-4 border-r border-[var(--el-border-color)]">
        <div
          class="flex items-center gap-2 py-3 mb-3 text-sm font-semibold text-[var(--el-text-color-primary)] border-b border-[var(--el-border-color)]"
        >
          <i class="ri-shield-user-line text-base text-[var(--el-color-primary)]"></i>
          <span>选择角色</span>
        </div>

        <div v-if="filteredRoles.length > 0" class="flex-1 overflow-y-auto pr-1">
          <div
            v-for="role in filteredRoles"
            :key="role.id"
            class="p-3 mb-2 cursor-pointer rounded-xl border-2 transition-all duration-200 hover:bg-[var(--el-color-primary-light-9)] hover:border-[var(--el-color-primary)]"
            :class="
              selectedRoleIds.includes(role.id)
                ? 'bg-[var(--el-color-primary-light-9)] border-[var(--el-color-primary)]'
                : 'bg-[var(--el-bg-color)] border-[var(--el-border-color)]'
            "
            @click="toggleRoleSelection(role.id)"
          >
            <div class="text-sm font-semibold text-[var(--el-text-color-primary)] mb-1.5">
              {{ role.roleName }}
            </div>
            <div class="flex items-center justify-between text-xs">
              <span class="text-[var(--el-text-color-secondary)]">{{ role.roleCode }}</span>
              <span
                v-if="selectedRoleIds.includes(role.id) && getRoleUserCount(role.id) > 0"
                class="px-1.5 py-0.5 font-medium text-[var(--el-color-primary)] bg-[var(--el-color-primary-light-9)] rounded"
              >
                {{ getCheckedCount(role.id) }}/{{ getRoleUserCount(role.id) }} 已选
              </span>
              <span
                v-else-if="getRoleUserCount(role.id) > 0"
                class="px-1.5 py-0.5 font-medium text-[var(--el-text-color-regular)] bg-[var(--el-fill-color-light)] rounded"
              >
                {{ getRoleUserCount(role.id) }}人
              </span>
              <span
                v-else
                class="px-1.5 py-0.5 font-medium text-[var(--el-text-color-placeholder)] bg-[var(--el-fill-color-light)] rounded"
              >
                0人
              </span>
            </div>
          </div>
        </div>

        <div
          v-else
          class="flex items-center justify-center gap-1.5 py-5 text-[13px] text-[var(--el-text-color-placeholder)]"
        >
          <i class="ri-information-line text-base"></i>
          <span>未找到匹配的角色</span>
        </div>
      </div>

      <!-- 右侧：用户列表 -->
      <div class="flex flex-col flex-1 min-w-0 overflow-hidden">
        <div
          class="flex items-center gap-2 py-3 mb-3 text-sm font-semibold text-[var(--el-text-color-primary)] border-b border-[var(--el-border-color)]"
        >
          <i class="ri-team-line text-base text-[var(--el-color-primary)]"></i>
          <span>选择用户</span>
          <span class="ml-auto text-xs font-normal text-[var(--el-color-primary)]">
            (已选 {{ selectedUserCount }} 人)
          </span>
        </div>

        <div
          v-if="selectedRoleIds.length > 0 && roleUserGroups.length > 0"
          class="flex-1 overflow-y-auto pr-1"
        >
          <template v-for="group in roleUserGroups" :key="group.roleId">
            <div v-show="expandedRoles.has(group.roleId)" class="mb-5">
              <!-- 角色标题栏 -->
              <div
                class="flex items-center justify-between px-3 py-2 mb-3 rounded-lg bg-[var(--el-fill-color-light)]"
              >
                <div
                  class="flex items-center gap-2 text-sm font-semibold text-[var(--el-text-color-primary)]"
                >
                  <i class="ri-shield-user-line text-base text-[var(--el-color-primary)]"></i>
                  <span>{{ group.roleName }}</span>
                </div>
                <ElButton
                  size="small"
                  text
                  @click="handleRoleCheckAllChange(group.roleId, !isRoleAllChecked(group.roleId))"
                >
                  {{ isRoleAllChecked(group.roleId) ? '取消全选' : '全选' }}
                </ElButton>
              </div>

              <!-- 用户卡片网格 -->
              <div
                v-if="getFilteredUsers(group.users).length > 0"
                class="grid grid-cols-[repeat(auto-fill,minmax(160px,1fr))] gap-3"
              >
                <div
                  v-for="user in getFilteredUsers(group.users)"
                  :key="user.id"
                  class="relative flex items-center gap-3 p-3 cursor-pointer rounded-xl border-2 transition-all duration-200 hover:border-[var(--el-color-primary)] hover:-translate-y-px hover:shadow-md"
                  :class="
                    selectedUserIds.has(user.id)
                      ? 'bg-[var(--el-color-primary-light-9)] border-[var(--el-color-primary)]'
                      : 'bg-[var(--el-bg-color)] border-[var(--el-border-color)]'
                  "
                  @click="handleUserCheck(user.id, !selectedUserIds.has(user.id))"
                >
                  <!-- 头像 -->
                  <div
                    class="shrink-0 w-10 h-10 flex items-center justify-center rounded-full text-base font-semibold text-white bg-gradient-to-br from-[var(--el-color-success)] to-[var(--el-color-success-light-3)]"
                  >
                    {{ (user.nickname || user.username).charAt(0).toUpperCase() }}
                  </div>
                  <!-- 信息 -->
                  <div class="flex-1 min-w-0">
                    <div
                      class="text-sm font-medium text-[var(--el-text-color-primary)] truncate mb-1"
                    >
                      {{ user.nickname || user.username }}
                    </div>
                    <div
                      v-if="user.phone"
                      class="flex items-center gap-1 text-xs text-[var(--el-text-color-secondary)]"
                    >
                      <i class="ri-phone-line text-xs"></i>
                      {{ user.phone }}
                    </div>
                  </div>
                  <!-- 选中标记 -->
                  <div
                    v-if="selectedUserIds.has(user.id)"
                    class="absolute top-2 right-2 text-lg text-[var(--el-color-primary)]"
                  >
                    <i class="ri-checkbox-circle-fill"></i>
                  </div>
                </div>
              </div>

              <div
                v-else
                class="flex items-center justify-center py-5 text-[13px] text-[var(--el-text-color-placeholder)]"
              >
                <span>未找到匹配的用户</span>
              </div>
            </div>
          </template>
        </div>

        <!-- 空状态 -->
        <div
          v-else
          class="flex flex-col items-center justify-center py-15 text-[var(--el-text-color-placeholder)] text-center"
        >
          <i class="ri-user-add-line text-5xl text-[var(--el-border-color)] mb-3"></i>
          <p class="m-0 text-sm">请从左侧选择角色，系统将自动加载该角色下的用户</p>
        </div>
      </div>
    </div>

    <template #footer>
      <ElButton @click="dialogVisible = false" v-ripple>取消</ElButton>
      <ElButton type="primary" :disabled="selectedUserCount === 0" @click="handleConfirm" v-ripple>
        确定 ({{ selectedUserCount }})
      </ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { fetchGetAllRoles, fetchGetUsersByRoleCodes } from '@/api/system-manage'

  interface RoleItem {
    id: number
    roleName: string
    roleCode: string
  }

  interface UserItem {
    id: number
    username: string
    nickname?: string
    phone?: string
    email?: string
  }

  interface RoleUserGroup {
    roleId: number
    roleName: string
    roleCode: string
    users: UserItem[]
  }

  const props = defineProps<{
    modelValue: boolean
    selectedIds: number[] // 已选中的用户ID列表
  }>()

  const emit = defineEmits<{
    'update:modelValue': [value: boolean]
    confirm: [selected: Array<{ id: number; name: string }>]
  }>()

  const dialogVisible = computed({
    get: () => props.modelValue,
    set: (val: boolean) => emit('update:modelValue', val)
  })

  // 状态
  const searchKeyword = ref('')
  const roles = ref<RoleItem[]>([])
  const selectedRoleIds = ref<number[]>([])
  const roleUserGroups = ref<RoleUserGroup[]>([])
  const selectedUserIds = reactive(new Set<number>())
  const expandedRoles = reactive(new Set<number>()) // 展开的角色ID集合
  const loading = ref(false)
  const isRestoring = ref(false) // 是否正在恢复预选状态（防止 watcher 覆盖）

  // 计算已选用户数量
  const selectedUserCount = computed(() => selectedUserIds.size)

  // 过滤角色列表
  const filteredRoles = computed(() => {
    if (!searchKeyword.value) return roles.value
    const keyword = searchKeyword.value.toLowerCase()
    return roles.value.filter(
      (role: RoleItem) =>
        role.roleName.toLowerCase().includes(keyword) ||
        role.roleCode.toLowerCase().includes(keyword)
    )
  })

  // 过滤用户列表
  const getFilteredUsers = (users: UserItem[]) => {
    if (!searchKeyword.value) return users
    const keyword = searchKeyword.value.toLowerCase()
    return users.filter(
      (user) =>
        user.username.toLowerCase().includes(keyword) ||
        user.nickname?.toLowerCase().includes(keyword) ||
        user.phone?.includes(keyword)
    )
  }

  // 获取角色下的用户数量
  const getRoleUserCount = (roleId: number) => {
    const group = roleUserGroups.value.find((g: RoleUserGroup) => g.roleId === roleId)
    return group ? group.users.length : 0
  }

  // 获取角色已选中用户数
  const getCheckedCount = (roleId: number) => {
    const group = roleUserGroups.value.find((g: RoleUserGroup) => g.roleId === roleId)
    if (!group) return 0
    return group.users.filter((u: UserItem) => selectedUserIds.has(u.id)).length
  }

  // 切换角色选择
  const toggleRoleSelection = (roleId: number) => {
    const index = selectedRoleIds.value.indexOf(roleId)
    if (index > -1) {
      // 如果已选中，取消选中（移除角色选择）
      selectedRoleIds.value.splice(index, 1)
      // 同时移除展开状态
      expandedRoles.delete(roleId)
    } else {
      // 如果未选中，选中角色并展开
      selectedRoleIds.value.push(roleId)
      // 立即添加展开状态，确保用户能看到该角色的用户列表
      expandedRoles.add(roleId)
    }
  }

  // 判断角色是否全选
  const isRoleAllChecked = (roleId: number) => {
    const group = roleUserGroups.value.find((g: RoleUserGroup) => g.roleId === roleId)
    if (!group || group.users.length === 0) return false
    return group.users.every((u: UserItem) => selectedUserIds.has(u.id))
  }

  // 角色全选/取消
  const handleRoleCheckAllChange = (roleId: number, checked: boolean) => {
    const group = roleUserGroups.value.find((g: RoleUserGroup) => g.roleId === roleId)
    if (!group) return

    if (checked) {
      // 全选该角色的用户
      group.users.forEach((u: UserItem) => selectedUserIds.add(u.id))
    } else {
      // 取消该角色的用户
      group.users.forEach((u: UserItem) => selectedUserIds.delete(u.id))
    }
  }

  // 用户勾选
  const handleUserCheck = (userId: number, checked: boolean) => {
    if (checked) {
      selectedUserIds.add(userId)
    } else {
      selectedUserIds.delete(userId)
    }
  }

  // 监听角色选择变化
  watch(
    selectedRoleIds,
    async (newRoleIds: number[], oldRoleIds: number[]) => {
      // 恢复预选状态期间，跳过 watcher 的自动处理
      if (isRestoring.value) return

      if (newRoleIds.length === 0) {
        roleUserGroups.value = []
        selectedUserIds.clear()
        expandedRoles.clear()
        return
      }

      try {
        loading.value = true

        // 获取选中角色的代码
        const selectedRoles = roles.value.filter((r: RoleItem) => newRoleIds.includes(r.id))
        const roleCodes = selectedRoles.map((r: RoleItem) => r.roleCode)

        // 调用API获取用户
        const userMap = await fetchGetUsersByRoleCodes(roleCodes)

        // 构建分组数据
        const newGroups: RoleUserGroup[] = []
        const newUserIds = new Set<number>()

        for (const role of selectedRoles) {
          const users = userMap[role.roleCode] || []
          newGroups.push({
            roleId: role.id,
            roleName: role.roleName,
            roleCode: role.roleCode,
            users
          })

          // 默认全选新增角色的用户
          if (!oldRoleIds || !oldRoleIds.includes(role.id)) {
            users.forEach((u: UserItem) => {
              selectedUserIds.add(u.id)
            })
            // 新增角色时自动展开
            expandedRoles.add(role.id)
          }

          // 收集所有当前角色的用户ID
          users.forEach((u: UserItem) => newUserIds.add(u.id))
        }

        // 移除已取消角色的用户
        const idsToRemove: number[] = []
        selectedUserIds.forEach((id: number) => {
          if (!newUserIds.has(id)) {
            idsToRemove.push(id)
          }
        })
        idsToRemove.forEach((id: number) => selectedUserIds.delete(id))

        // 移除已取消角色的展开状态
        expandedRoles.forEach((roleId: number | undefined) => {
          if (roleId && roleId !== undefined && !newRoleIds.includes(roleId)) {
            expandedRoles.delete(roleId)
          }
        })

        roleUserGroups.value = newGroups
      } catch (error) {
        console.error('加载用户失败:', error)
        ElMessage.error('加载用户列表失败')
      } finally {
        loading.value = false
      }
    },
    { deep: true }
  )

  /**
   * 恢复预选审批人状态：加载所有角色的用户，找到包含已选用户的角色并展开
   */
  const restorePreSelection = async (preSelectedIds: number[]) => {
    if (roles.value.length === 0 || preSelectedIds.length === 0) return

    isRestoring.value = true
    try {
      // 加载所有角色的用户，找出包含已选用户的角色
      const allRoleCodes = roles.value.map((r: RoleItem) => r.roleCode)
      const userMap = await fetchGetUsersByRoleCodes(allRoleCodes)

      const rolesToSelect: number[] = []
      const groups: RoleUserGroup[] = []

      for (const role of roles.value) {
        const users = userMap[role.roleCode] || []
        const hasPreSelected = users.some((u: UserItem) => preSelectedIds.includes(u.id))

        if (hasPreSelected) {
          rolesToSelect.push(role.id)
          groups.push({
            roleId: role.id,
            roleName: role.roleName,
            roleCode: role.roleCode,
            users
          })
          expandedRoles.add(role.id)
        }
      }

      // 设置状态（跳过 watcher）
      roleUserGroups.value = groups
      preSelectedIds.forEach((id: number) => selectedUserIds.add(id))
      selectedRoleIds.value = rolesToSelect
    } catch (error) {
      console.error('恢复预选审批人失败:', error)
    } finally {
      isRestoring.value = false
    }
  }

  // 监听弹窗打开
  watch(
    () => props.modelValue,
    async (val: boolean) => {
      if (val) {
        // 先清空所有状态
        isRestoring.value = true
        searchKeyword.value = ''
        selectedRoleIds.value = []
        selectedUserIds.clear()
        roleUserGroups.value = []
        expandedRoles.clear()
        isRestoring.value = false

        await loadRoles()

        // 如果有预选用户，自动恢复选择状态
        if (props.selectedIds && props.selectedIds.length > 0) {
          await restorePreSelection(props.selectedIds)
        }
      } else {
        // 关闭时清空状态
        searchKeyword.value = ''
        isRestoring.value = true
        selectedRoleIds.value = []
        selectedUserIds.clear()
        roleUserGroups.value = []
        expandedRoles.clear()
        isRestoring.value = false
      }
    }
  )

  // 加载角色列表
  const loadRoles = async () => {
    try {
      const res = await fetchGetAllRoles()
      roles.value = res.map((item: RoleItem) => ({
        id: item.id,
        roleName: item.roleName,
        roleCode: item.roleCode
      }))
    } catch (error) {
      console.error('加载角色失败:', error)
      ElMessage.error('加载角色列表失败')
    }
  }

  // 确认选择
  const handleConfirm = () => {
    // 收集所有选中的用户
    const selectedUsers: Array<{ id: number; name: string }> = []

    roleUserGroups.value.forEach((group: RoleUserGroup) => {
      group.users.forEach((user: UserItem) => {
        if (selectedUserIds.has(user.id)) {
          selectedUsers.push({
            id: user.id,
            name: user.nickname || user.username
          })
        }
      })
    })

    // 去重（防止用户在多个角色中）
    const uniqueUsers = Array.from(
      new Map(selectedUsers.map((u: { id: number; name: string }) => [u.id, u])).values()
    )

    emit('confirm', uniqueUsers)
    dialogVisible.value = false
  }
</script>
