<!-- 审批人选择弹窗 - 卡片网格式展示 -->
<template>
  <ElDialog
    v-model="dialogVisible"
    title="选择审批人"
    width="1000px"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <!-- 顶部工具栏 -->
    <div class="dialog-header">
      <!-- 搜索 -->
      <ElInput v-model="searchKeyword" placeholder="搜索角色或用户" clearable size="default">
        <template #prefix>
          <i class="ri-search-line"></i>
        </template>
      </ElInput>
    </div>

    <!-- 左右分栏布局 -->
    <div class="split-layout">
      <!-- 左侧：角色列表 -->
      <div class="left-panel">
        <div class="panel-title">
          <i class="ri-shield-user-line"></i>
          <span>选择角色</span>
        </div>
        <div v-if="filteredRoles.length > 0" class="role-list">
          <div
            v-for="role in filteredRoles"
            :key="role.id"
            class="role-item"
            :class="{ active: selectedRoleIds.includes(role.id) }"
            @click="toggleRoleSelection(role.id)"
          >
            <div class="role-content">
              <div class="role-name">{{ role.roleName }}</div>
              <div class="role-meta">
                <span class="role-code">{{ role.roleCode }}</span>
                <span v-if="getRoleUserCount(role.id) > 0" class="role-count">
                  {{ getRoleUserCount(role.id) }}人
                </span>
              </div>
            </div>
            <div
              v-if="selectedRoleIds.includes(role.id) && getRoleUserCount(role.id) > 0"
              class="role-selected-info"
            >
              {{ getCheckedCount(role.id) }} / {{ getRoleUserCount(role.id) }} 已选
            </div>
          </div>
        </div>
        <div v-else class="empty-tip">
          <i class="ri-information-line"></i>
          <span>未找到匹配的角色</span>
        </div>
      </div>

      <!-- 右侧：用户列表 -->
      <div class="right-panel">
        <div class="panel-title">
          <i class="ri-team-line"></i>
          <span>选择用户</span>
          <span class="selected-count">(已选 {{ selectedUserCount }} 人)</span>
        </div>
        <div
          v-if="selectedRoleIds.length > 0 && roleUserGroups.length > 0"
          class="user-panel-content"
        >
          <template v-for="group in roleUserGroups" :key="group.roleId">
            <div v-show="expandedRoles.has(group.roleId)" class="user-group">
              <!-- 角色标题栏 -->
              <div class="group-header">
                <div class="group-title">
                  <i class="ri-shield-user-line"></i>
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
              <div v-if="getFilteredUsers(group.users).length > 0" class="user-cards-grid">
                <div
                  v-for="user in getFilteredUsers(group.users)"
                  :key="user.id"
                  class="user-card"
                  :class="{ selected: selectedUserIds.has(user.id) }"
                  @click="handleUserCheck(user.id, !selectedUserIds.has(user.id))"
                >
                  <div class="user-avatar">
                    <span>{{ (user.nickname || user.username).charAt(0).toUpperCase() }}</span>
                  </div>
                  <div class="user-info">
                    <div class="user-name">{{ user.nickname || user.username }}</div>
                    <div v-if="user.phone" class="user-phone">
                      <i class="ri-phone-line"></i>
                      {{ user.phone }}
                    </div>
                  </div>
                  <div v-if="selectedUserIds.has(user.id)" class="user-check-mark">
                    <i class="ri-checkbox-circle-fill"></i>
                  </div>
                </div>
              </div>
              <div v-else class="empty-tip">
                <span>未找到匹配的用户</span>
              </div>
            </div>
          </template>
        </div>
        <div v-else class="empty-state">
          <i class="ri-user-add-line"></i>
          <p>请从左侧选择角色，系统将自动加载该角色下的用户</p>
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
  import { ElMessage } from 'element-plus'

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

  // 监听弹窗打开
  watch(
    () => props.modelValue,
    async (val: boolean) => {
      if (val) {
        await loadRoles()
        // 根据已选用户ID反推并设置
        selectedUserIds.clear()
        if (props.selectedIds && props.selectedIds.length > 0) {
          props.selectedIds.forEach((id: number) => selectedUserIds.add(id))
        }
        selectedRoleIds.value = []
        roleUserGroups.value = []
        expandedRoles.clear()
      } else {
        // 清空状态
        searchKeyword.value = ''
        selectedRoleIds.value = []
        selectedUserIds.clear()
        roleUserGroups.value = []
        expandedRoles.clear()
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

<style lang="scss" scoped>
  // 顶部工具栏
  .dialog-header {
    margin-bottom: 20px;
  }

  // 左右分栏布局
  .split-layout {
    display: flex;
    gap: 16px;
    height: 500px;
    overflow: hidden;
  }

  // 左侧面板：角色列表
  .left-panel {
    display: flex;
    flex-direction: column;
    width: 280px;
    padding-right: 16px;
    border-right: 1px solid #e4e7ed;

    .panel-title {
      display: flex;
      gap: 8px;
      align-items: center;
      padding: 12px 0;
      margin-bottom: 12px;
      font-size: 14px;
      font-weight: 600;
      color: #303133;
      border-bottom: 1px solid #e4e7ed;

      i {
        font-size: 16px;
        color: #409eff;
      }
    }

    .role-list {
      flex: 1;
      padding-right: 4px;
      overflow-y: auto;
    }

    .role-item {
      padding: 12px;
      margin-bottom: 8px;
      cursor: pointer;
      background: #fff;
      border: 2px solid #e4e7ed;
      border-radius: 8px;
      transition: all 0.3s ease;

      &:hover {
        background: #f0f9ff;
        border-color: #409eff;
      }

      &.active {
        background: #ecf5ff;
        border-color: #409eff;
      }

      .role-content {
        .role-name {
          margin-bottom: 6px;
          font-size: 14px;
          font-weight: 600;
          color: #303133;
        }

        .role-meta {
          display: flex;
          align-items: center;
          justify-content: space-between;
          font-size: 12px;

          .role-code {
            color: #909399;
          }

          .role-count {
            padding: 2px 6px;
            font-weight: 500;
            color: #606266;
            background: #f0f2f5;
            border-radius: 4px;
          }
        }
      }

      .role-selected-info {
        padding-top: 8px;
        margin-top: 8px;
        font-size: 12px;
        font-weight: 500;
        color: #409eff;
        border-top: 1px solid #e4e7ed;
      }
    }
  }

  // 右侧面板：用户列表
  .right-panel {
    display: flex;
    flex: 1;
    flex-direction: column;
    overflow: hidden;

    .panel-title {
      display: flex;
      gap: 8px;
      align-items: center;
      padding: 12px 0;
      margin-bottom: 12px;
      font-size: 14px;
      font-weight: 600;
      color: #303133;
      border-bottom: 1px solid #e4e7ed;

      i {
        font-size: 16px;
        color: #409eff;
      }

      .selected-count {
        margin-left: auto;
        font-size: 12px;
        font-weight: normal;
        color: #409eff;
      }
    }

    .user-panel-content {
      flex: 1;
      padding-right: 4px;
      overflow-y: auto;
    }

    .user-group {
      margin-bottom: 20px;

      .group-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 8px 12px;
        margin-bottom: 12px;
        background: #f5f7fa;
        border-radius: 8px;

        .group-title {
          display: flex;
          gap: 8px;
          align-items: center;
          font-size: 14px;
          font-weight: 600;
          color: #303133;

          i {
            font-size: 16px;
            color: #409eff;
          }
        }
      }

      .user-cards-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
        gap: 12px;
      }
    }
  }

  // 用户卡片
  .user-card {
    position: relative;
    display: flex;
    gap: 12px;
    align-items: center;
    padding: 12px;
    cursor: pointer;
    background: #fff;
    border: 2px solid #e4e7ed;
    border-radius: 10px;
    transition: all 0.3s ease;

    &:hover {
      border-color: #409eff;
      box-shadow: 0 2px 8px rgb(64 158 255 / 15%);
      transform: translateY(-1px);
    }

    &.selected {
      background: #ecf5ff;
      border-color: #409eff;

      .user-check-mark {
        opacity: 1;
      }
    }

    .user-avatar {
      display: flex;
      flex-shrink: 0;
      align-items: center;
      justify-content: center;
      width: 40px;
      height: 40px;
      font-size: 16px;
      font-weight: 600;
      color: #fff;
      background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
      border-radius: 50%;
    }

    .user-info {
      flex: 1;
      min-width: 0;

      .user-name {
        margin-bottom: 4px;
        overflow: hidden;
        font-size: 14px;
        font-weight: 500;
        color: #303133;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .user-phone {
        display: flex;
        gap: 4px;
        align-items: center;
        font-size: 12px;
        color: #909399;

        i {
          font-size: 12px;
        }
      }
    }

    .user-check-mark {
      position: absolute;
      top: 8px;
      right: 8px;
      font-size: 18px;
      color: #409eff;
      opacity: 0;
      transition: opacity 0.3s;
    }
  }

  // 空状态
  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 60px 20px;
    color: #909399;
    text-align: center;

    i {
      margin-bottom: 12px;
      font-size: 48px;
      color: #dcdfe6;
    }

    p {
      margin: 0;
      font-size: 14px;
    }
  }

  .empty-tip {
    display: flex;
    gap: 6px;
    align-items: center;
    justify-content: center;
    padding: 20px;
    font-size: 13px;
    color: #909399;
    text-align: center;

    i {
      font-size: 16px;
    }
  }

  // 滚动条样式
  .role-list,
  .user-panel-content {
    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-track {
      background: #f1f1f1;
      border-radius: 3px;
    }

    &::-webkit-scrollbar-thumb {
      background: #c1c1c1;
      border-radius: 3px;

      &:hover {
        background: #a8a8a8;
      }
    }
  }
</style>
