<!-- 用户基本信息模块 -->
<template>
  <div class="user-basic-info">
    <!-- 基本信息卡片 -->
    <ElCard class="info-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <ArtSvgIcon icon="ri:user-line" class="header-icon" />
          <span class="header-title">基本信息</span>
        </div>
      </template>
      <div class="info-list">
        <div v-if="data.username" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:account-circle-line" class="label-icon" />
            <span>用户名</span>
          </div>
          <div class="row-value">{{ data.username }}</div>
        </div>
        <div v-if="data.nickname" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:user-smile-line" class="label-icon" />
            <span>昵称</span>
          </div>
          <div class="row-value">{{ data.nickname }}</div>
        </div>
        <div v-if="data.genderText" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:genderless-line" class="label-icon" />
            <span>性别</span>
          </div>
          <div class="row-value">{{ data.genderText }}</div>
        </div>
        <div v-if="data.phone" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:phone-line" class="label-icon" />
            <span>手机号</span>
          </div>
          <div class="row-value is-copyable">{{ data.phone }}</div>
        </div>
        <div v-if="data.email" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:mail-line" class="label-icon" />
            <span>邮箱</span>
          </div>
          <div class="row-value is-copyable">{{ data.email }}</div>
        </div>
        <div
          v-if="!data.username && !data.nickname && !data.genderText && !data.phone && !data.email"
          class="empty-state"
        >
          <span class="empty-text">暂无基本信息</span>
        </div>
      </div>
    </ElCard>

    <!-- 系统信息卡片 -->
    <ElCard class="info-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <ArtSvgIcon icon="ri:settings-3-line" class="header-icon" />
          <span class="header-title">系统信息</span>
        </div>
      </template>
      <div class="info-list">
        <div class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:shield-check-line" class="label-icon" />
            <span>用户状态</span>
          </div>
          <div class="row-value">
            <ElTag :type="data.status === 1 ? 'success' : 'info'" size="small">
              {{ data.status === 1 ? '正常' : '停用' }}
            </ElTag>
          </div>
        </div>
        <div class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:wifi-line" class="label-icon" />
            <span>在线状态</span>
          </div>
          <div class="row-value">
            <ElTag :type="isOnline ? 'success' : 'info'" size="small">
              {{ isOnline ? '在线' : '离线' }}
            </ElTag>
          </div>
        </div>
        <div v-if="data.createTime" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:calendar-check-line" class="label-icon" />
            <span>创建时间</span>
          </div>
          <div class="row-value">{{ data.createTime }}</div>
        </div>
        <div v-if="data.lastLoginTime" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:time-line" class="label-icon" />
            <span>最后登录</span>
          </div>
          <div class="row-value">{{ data.lastLoginTime }}</div>
        </div>
      </div>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { computed } from 'vue'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import { useUserOnlineStatus } from '@/hooks/core/useUserOnlineStatus'

  defineOptions({ name: 'UserBasicInfo' })

  interface Props {
    data: Partial<Api.SystemManage.UserListItem>
  }

  const props = withDefaults(defineProps<Props>(), {
    data: () => ({})
  })

  const { onlineStatusMap } = useUserOnlineStatus()

  const isOnline = computed(() => {
    if (!props.data.id) return false
    return onlineStatusMap.value[props.data.id] ?? false
  })
</script>

<style lang="scss" scoped>
  .user-basic-info {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }
</style>
