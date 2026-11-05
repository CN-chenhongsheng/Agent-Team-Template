<!-- 用户详情查看抽屉 -->
<template>
  <ArtDrawer
    v-model="drawerVisible"
    :title="$t('system.user.drawer.title')"
    :loading="loading"
    :with-header="true"
    @close="handleClose"
  >
    <div class="user-detail-content">
      <!-- 顶部用户卡片 -->
      <div class="user-header-card">
        <ElImage :src="detailData.avatar || undefined" class="user-avatar" fit="cover">
          <template #error>
            <div class="user-avatar-empty">
              <ElIcon :size="28"><User /></ElIcon>
            </div>
          </template>
        </ElImage>
        <div class="user-info">
          <div class="user-name">{{ detailData.nickname || '--' }}</div>
          <div class="user-meta">
            <span class="user-id">#{{ detailData.id }}</span>
            <ElTag :type="detailData.status === 1 ? 'success' : 'info'" size="small">
              {{
                detailData.status === 1 ? $t('system.common.normal') : $t('system.common.disabled')
              }}
            </ElTag>
            <ElTag :type="isOnline ? 'success' : 'info'" size="small">
              {{ isOnline ? $t('system.user.online') : $t('system.user.offline') }}
            </ElTag>
          </div>
        </div>
      </div>

      <!-- 标签页内容 -->
      <ElTabs v-model="activeTab" class="user-detail-tabs">
        <!-- 基本信息标签页 -->
        <ElTabPane :label="$t('system.user.drawer.basicInfo')" name="basic">
          <UserBasicInfo :data="detailData" />
        </ElTabPane>

        <!-- 权限信息标签页 -->
        <ElTabPane :label="$t('system.user.drawer.permissionInfo')" name="permission">
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
  import { User } from '@element-plus/icons-vue'

  defineOptions({ name: 'UserDrawer' })

  type UserListItem = Api.SystemManage.UserListItem

  interface Props {
    modelValue: boolean
    userId?: number
    userData?: Partial<UserListItem>
  }

  interface Emits {
    (e: 'update:modelValue', value: boolean): void
    (e: 'close'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    modelValue: false,
    userId: undefined,
    userData: undefined
  })

  const emit = defineEmits<Emits>()

  const drawerVisible = computed({
    get: () => props.modelValue,
    set: (value) => emit('update:modelValue', value)
  })

  const { isUserOnline } = useUserOnlineStatus()

  const activeTab = ref('basic')
  const loading = ref(false)
  const detailData = ref<Partial<UserListItem>>({})

  const isOnline = computed(() => isUserOnline(detailData.value.id, detailData.value.isOnline))

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
    emit('update:modelValue', false)
    emit('close')
  }

  // 监听抽屉显示状态
  watch(
    () => props.modelValue,
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
      if (props.modelValue) {
        loadUserDetail()
      }
    }
  )

  // 监听 userData 变化（作为备用）
  watch(
    () => props.userData,
    (newVal) => {
      if (props.modelValue && newVal && !props.userId) {
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
      box-sizing: border-box;
      flex-shrink: 0;
      width: 64px;
      height: 64px;
      overflow: hidden;
      background: var(--el-bg-color);
      border: 1px solid var(--el-border-color);
      border-radius: 50%;

      :deep(.el-image__wrapper),
      :deep(.el-image__inner),
      :deep(.el-image__error),
      :deep(.el-image__placeholder) {
        width: 100%;
        height: 100%;
      }

      :deep(.el-image__error) {
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 0;
        background: var(--el-fill-color-light);
      }

      :deep(.el-image__inner) {
        border-radius: 50%;
      }
    }

    .user-avatar-empty {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 100%;
      height: 100%;
      color: var(--el-text-color-placeholder);
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
