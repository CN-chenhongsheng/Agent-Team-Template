<!-- 用户详情查看抽屉 -->
<template>
  <ArtDrawer
    v-model="drawerVisible"
    title="查看用户"
    :loading="loading"
    :with-header="true"
    @close="handleClose"
  >
    <div class="user-detail-content">
      <!-- 顶部用户卡片 -->
      <div class="user-header-card">
        <ElImage
          :src="detailData.avatar || '/default-avatar.png'"
          class="user-avatar"
          fit="cover"
        />
        <div class="user-info">
          <div class="user-name">{{ detailData.nickname || '--' }}</div>
          <div class="user-meta">
            <span class="user-id">#{{ detailData.id }}</span>
            <ElTag :type="detailData.status === 1 ? 'success' : 'info'" size="small">
              {{ detailData.status === 1 ? '正常' : '停用' }}
            </ElTag>
            <ElTag :type="isOnline ? 'success' : 'info'" size="small">
              {{ isOnline ? '在线' : '离线' }}
            </ElTag>
          </div>
        </div>
      </div>

      <!-- 标签页内容 -->
      <ElTabs v-model="activeTab" class="user-detail-tabs">
        <!-- 基本信息标签页 -->
        <ElTabPane label="基本信息" name="basic">
          <UserBasicInfo :data="detailData" />
        </ElTabPane>

        <!-- 权限信息标签页 -->
        <ElTabPane label="权限信息" name="permission">
          <UserPermissionInfo :data="detailData" />
        </ElTabPane>
      </ElTabs>
    </div>
  </ArtDrawer>
</template>

<script setup lang="ts">
  import { ref, computed, watch } from 'vue'
  import ArtDrawer from '@/components/core/layouts/art-drawer/index.vue'
  import UserBasicInfo from './user-basic-info.vue'
  import UserPermissionInfo from './user-permission-info.vue'
  import { fetchGetUserDetail } from '@/api/system-manage'
  import { useUserOnlineStatus } from '@/hooks/core/useUserOnlineStatus'

  defineOptions({ name: 'UserDrawer' })

  type UserListItem = Api.SystemManage.UserListItem

  interface Props {
    visible: boolean
    userId?: number
    userData?: Partial<UserListItem>
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'close'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    visible: false,
    userId: undefined,
    userData: undefined
  })

  const emit = defineEmits<Emits>()

  const drawerVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const { onlineStatusMap } = useUserOnlineStatus()

  const activeTab = ref('basic')
  const loading = ref(false)
  const detailData = ref<Partial<UserListItem>>({})

  // 在线状态
  const isOnline = computed(() => {
    if (!detailData.value.id) return false
    return onlineStatusMap.value[detailData.value.id] ?? false
  })

  // 加载用户详情
  const loadUserDetail = async () => {
    if (!props.userId) {
      detailData.value = props.userData || {}
      return
    }

    try {
      loading.value = true
      const res = await fetchGetUserDetail(props.userId)
      detailData.value = res
    } catch (error) {
      console.error('获取用户详情失败:', error)
      detailData.value = props.userData || {}
    } finally {
      loading.value = false
    }
  }

  // 关闭抽屉
  const handleClose = () => {
    emit('update:visible', false)
    emit('close')
  }

  // 监听抽屉显示状态
  watch(
    () => props.visible,
    (newVal) => {
      if (newVal) {
        activeTab.value = 'basic'
        loadUserDetail()
      }
    }
  )

  // 监听用户ID变化
  watch(
    () => props.userId,
    () => {
      if (props.visible) {
        loadUserDetail()
      }
    }
  )

  // 监听 userData 变化（作为备用）
  watch(
    () => props.userData,
    (newVal) => {
      if (props.visible && newVal && !props.userId) {
        detailData.value = { ...newVal }
      }
    }
  )
</script>

<style lang="scss" scoped>
  .user-detail-content {
    display: flex;
    flex-direction: column;
    gap: 16px;
    padding: 0;
  }

  .user-header-card {
    display: flex;
    gap: 16px;
    align-items: center;
    padding: 20px;
    background: var(--el-fill-color-light);
    border-radius: 8px;

    .user-avatar {
      flex-shrink: 0;
      width: 64px;
      height: 64px;
      border-radius: 50%;
    }

    .user-info {
      flex: 1;
      min-width: 0;

      .user-name {
        margin-bottom: 8px;
        font-size: 18px;
        font-weight: 600;
        color: var(--el-text-color-primary);
      }

      .user-meta {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        align-items: center;

        .user-id {
          font-size: 13px;
          color: var(--el-text-color-secondary);
        }
      }
    }
  }

  .user-detail-tabs {
    :deep(.el-tabs__header) {
      padding: 0;
      margin: 0 0 24px;
    }

    :deep(.el-tabs__nav-wrap::after) {
      background-color: var(--el-border-color-lighter);
    }

    :deep(.el-tabs__item) {
      height: 48px;
      padding: 0 20px;
      font-size: 15px;
      font-weight: 500;
      line-height: 48px;

      &.is-active {
        font-weight: 600;
        color: var(--el-color-primary);
      }
    }

    :deep(.el-tabs__content) {
      padding: 0;
    }

    :deep(.el-tab-pane) {
      padding: 0;
    }
  }
</style>
