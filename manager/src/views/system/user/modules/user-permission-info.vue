<!-- 用户权限信息模块 -->
<template>
  <div class="user-permission-info">
    <!-- 角色信息卡片 -->
    <ElCard class="info-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <ArtSvgIcon icon="ri:shield-user-line" class="header-icon" />
          <span class="header-title">角色列表</span>
        </div>
      </template>
      <div class="info-list">
        <div class="role-tags">
          <ElTag v-for="role in roleList" :key="role" type="primary" size="small">
            {{ role }}
          </ElTag>
          <span v-if="roleList.length === 0" class="empty-text">暂无角色</span>
        </div>
      </div>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { computed } from 'vue'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'

  defineOptions({ name: 'UserPermissionInfo' })

  interface Props {
    data: Partial<Api.SystemManage.UserListItem>
  }

  const props = withDefaults(defineProps<Props>(), {
    data: () => ({})
  })

  const roleList = computed(() => {
    if (!props.data.roleNames) return []
    if (Array.isArray(props.data.roleNames)) {
      return props.data.roleNames
    }
    return (props.data.roleNames as unknown as string).split(',').filter(Boolean)
  })
</script>

<style lang="scss" scoped>
  .user-permission-info {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .role-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    padding: 12px 0;

    .empty-text {
      font-size: 14px;
      color: var(--el-text-color-placeholder);
    }
  }
</style>
