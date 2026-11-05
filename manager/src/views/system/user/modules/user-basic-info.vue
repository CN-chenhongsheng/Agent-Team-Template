<!-- 用户基本信息模块 -->
<template>
  <div class="user-basic-info">
    <!-- 基本信息卡片 -->
    <ElCard class="info-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <ArtSvgIcon icon="ri:user-line" class="header-icon" />
          <span class="header-title">{{ $t('system.user.drawer.basicInfo') }}</span>
        </div>
      </template>
      <div class="info-list">
        <div v-if="data.username" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:account-circle-line" class="label-icon" />
            <span>{{ $t('system.user.drawer.username') }}</span>
          </div>
          <div class="row-value">{{ data.username }}</div>
        </div>
        <div v-if="data.nickname" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:user-smile-line" class="label-icon" />
            <span>{{ $t('system.user.drawer.nickname') }}</span>
          </div>
          <div class="row-value">{{ data.nickname }}</div>
        </div>
        <div v-if="data.gender !== undefined || data.genderText" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:genderless-line" class="label-icon" />
            <span>{{ $t('system.user.drawer.gender') }}</span>
          </div>
          <div class="row-value">{{ genderLabel }}</div>
        </div>
        <div v-if="data.phone" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:phone-line" class="label-icon" />
            <span>{{ $t('system.user.drawer.phone') }}</span>
          </div>
          <div class="row-value is-copyable">{{ data.phone }}</div>
        </div>
        <div v-if="data.email" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:mail-line" class="label-icon" />
            <span>{{ $t('system.user.drawer.email') }}</span>
          </div>
          <div class="row-value is-copyable">{{ data.email }}</div>
        </div>
        <div
          v-if="
            !data.username &&
            !data.nickname &&
            data.gender === undefined &&
            !data.genderText &&
            !data.phone &&
            !data.email
          "
          class="empty-state"
        >
          <span class="empty-text">{{ $t('system.user.drawer.noBasicInfo') }}</span>
        </div>
      </div>
    </ElCard>

    <!-- 系统信息卡片 -->
    <ElCard class="info-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <ArtSvgIcon icon="ri:settings-3-line" class="header-icon" />
          <span class="header-title">{{ $t('system.user.drawer.systemInfo') }}</span>
        </div>
      </template>
      <div class="info-list">
        <div class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:shield-check-line" class="label-icon" />
            <span>{{ $t('system.user.drawer.userStatus') }}</span>
          </div>
          <div class="row-value">
            <ElTag :type="data.status === 1 ? 'success' : 'info'" size="small">
              {{ data.status === 1 ? $t('system.common.normal') : $t('system.common.disabled') }}
            </ElTag>
          </div>
        </div>
        <div class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:wifi-line" class="label-icon" />
            <span>{{ $t('system.user.drawer.onlineStatus') }}</span>
          </div>
          <div class="row-value">
            <ElTag :type="isOnline ? 'success' : 'info'" size="small">
              {{ isOnline ? $t('system.user.online') : $t('system.user.offline') }}
            </ElTag>
          </div>
        </div>
        <div v-if="data.createTime" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:calendar-check-line" class="label-icon" />
            <span>{{ $t('system.user.drawer.createTime') }}</span>
          </div>
          <div class="row-value">{{ data.createTime }}</div>
        </div>
        <div v-if="data.lastLoginTime" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:time-line" class="label-icon" />
            <span>{{ $t('system.user.drawer.lastLogin') }}</span>
          </div>
          <div class="row-value">{{ data.lastLoginTime }}</div>
        </div>
      </div>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n'
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

  const { t } = useI18n()
  const { isUserOnline } = useUserOnlineStatus()

  const isOnline = computed(() => isUserOnline(props.data.id, props.data.isOnline))

  const genderLabel = computed(() => {
    if (props.data.gender === 1) return t('system.userCenter.genderMale')
    if (props.data.gender === 2) return t('system.userCenter.genderFemale')
    return t('system.userCenter.genderUnknown')
  })
</script>

<style lang="scss" scoped>
  .user-basic-info {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }
</style>
