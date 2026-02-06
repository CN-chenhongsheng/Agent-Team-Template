<!-- 学生生活习惯模块 -->
<template>
  <div class="student-lifestyle-info">
    <!-- 作息习惯卡片 -->
    <ElCard class="info-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <ArtSvgIcon icon="ri:moon-line" class="header-icon" />
          <span class="header-title">作息习惯</span>
        </div>
      </template>
      <div class="info-list">
        <div v-if="data.sleepScheduleText" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:time-line" class="label-icon" />
            <span>作息时间</span>
          </div>
          <div class="row-value">{{ data.sleepScheduleText }}</div>
        </div>
        <div v-if="data.sleepQualityText" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:zzz-line" class="label-icon" />
            <span>睡眠质量</span>
          </div>
          <div class="row-value">{{ data.sleepQualityText }}</div>
        </div>
        <div v-if="data.snores !== undefined && data.snores !== null" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:volume-up-line" class="label-icon" />
            <span>是否打呼噜</span>
          </div>
          <div class="row-value" :class="data.snores === 1 ? 'is-warn' : 'is-good'">
            <span class="value-dot"></span>
            {{ data.snoresText || '-' }}
          </div>
        </div>
        <div
          v-if="data.sensitiveToLight !== undefined && data.sensitiveToLight !== null"
          class="info-row"
        >
          <div class="row-label">
            <ArtSvgIcon icon="ri:sun-line" class="label-icon" />
            <span>光线敏感</span>
          </div>
          <div class="row-value" :class="data.sensitiveToLight === 1 ? 'is-warn' : 'is-good'">
            <span class="value-dot"></span>
            {{ data.sensitiveToLightText || '-' }}
          </div>
        </div>
        <div
          v-if="data.sensitiveToSound !== undefined && data.sensitiveToSound !== null"
          class="info-row"
        >
          <div class="row-label">
            <ArtSvgIcon icon="ri:headphone-line" class="label-icon" />
            <span>声音敏感</span>
          </div>
          <div class="row-value" :class="data.sensitiveToSound === 1 ? 'is-warn' : 'is-good'">
            <span class="value-dot"></span>
            {{ data.sensitiveToSoundText || '-' }}
          </div>
        </div>
        <div
          v-if="
            !data.sleepScheduleText &&
            data.snores === undefined &&
            data.sensitiveToLight === undefined &&
            data.sensitiveToSound === undefined
          "
          class="empty-state"
        >
          <span class="empty-text">暂无作息习惯信息</span>
        </div>
      </div>
    </ElCard>

    <!-- 生活习惯卡片 -->
    <ElCard class="info-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <ArtSvgIcon icon="ri:heart-line" class="header-icon" />
          <span class="header-title">生活习惯</span>
        </div>
      </template>
      <div class="info-list">
        <div
          v-if="data.smokingStatus !== undefined && data.smokingStatus !== null"
          class="info-row"
        >
          <div class="row-label">
            <ArtSvgIcon icon="ri:fire-line" class="label-icon" />
            <span>吸烟状态</span>
          </div>
          <div class="row-value" :class="data.smokingStatus === 1 ? 'is-warn' : 'is-good'">
            <span class="value-dot"></span>
            {{ data.smokingStatusText || '-' }}
          </div>
        </div>
        <div
          v-if="data.smokingTolerance !== undefined && data.smokingTolerance !== null"
          class="info-row"
        >
          <div class="row-label">
            <ArtSvgIcon icon="ri:user-heart-line" class="label-icon" />
            <span>接受室友吸烟</span>
          </div>
          <div class="row-value" :class="data.smokingTolerance === 1 ? 'is-good' : ''">
            <span v-if="data.smokingTolerance === 1" class="value-dot"></span>
            {{ data.smokingToleranceText || '-' }}
          </div>
        </div>
        <div v-if="data.cleanlinessLevelText" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:sparkling-line" class="label-icon" />
            <span>整洁程度</span>
          </div>
          <div class="row-value">{{ data.cleanlinessLevelText }}</div>
        </div>
        <div v-if="data.bedtimeCleanupText" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:layout-grid-line" class="label-icon" />
            <span>睡前整理</span>
          </div>
          <div class="row-value">{{ data.bedtimeCleanupText }}</div>
        </div>
        <div
          v-if="
            data.smokingStatus === undefined &&
            data.smokingTolerance === undefined &&
            !data.cleanlinessLevelText &&
            !data.bedtimeCleanupText
          "
          class="empty-state"
        >
          <span class="empty-text">暂无生活习惯信息</span>
        </div>
      </div>
    </ElCard>

    <!-- 社交偏好卡片 -->
    <ElCard class="info-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <ArtSvgIcon icon="ri:team-line" class="header-icon" />
          <span class="header-title">社交偏好</span>
        </div>
      </template>
      <div class="info-list">
        <div v-if="data.socialPreferenceText" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:chat-smile-2-line" class="label-icon" />
            <span>社交偏好</span>
          </div>
          <div class="row-value">{{ data.socialPreferenceText }}</div>
        </div>
        <div v-if="data.allowVisitorsText" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:door-open-line" class="label-icon" />
            <span>允许访客</span>
          </div>
          <div class="row-value">{{ data.allowVisitorsText }}</div>
        </div>
        <div v-if="data.phoneCallTimeText" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:phone-line" class="label-icon" />
            <span>电话时间偏好</span>
          </div>
          <div class="row-value">{{ data.phoneCallTimeText }}</div>
        </div>
        <div
          v-if="!data.socialPreferenceText && !data.allowVisitorsText && !data.phoneCallTimeText"
          class="empty-state"
        >
          <span class="empty-text">暂无社交偏好信息</span>
        </div>
      </div>
    </ElCard>

    <!-- 学习习惯卡片 -->
    <ElCard class="info-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <ArtSvgIcon icon="ri:book-read-line" class="header-icon" />
          <span class="header-title">学习习惯</span>
        </div>
      </template>
      <div class="info-list">
        <div v-if="data.studyInRoomText" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:home-office-line" class="label-icon" />
            <span>宿舍学习</span>
          </div>
          <div class="row-value">{{ data.studyInRoomText }}</div>
        </div>
        <div v-if="data.studyEnvironmentText" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:leaf-line" class="label-icon" />
            <span>学习环境偏好</span>
          </div>
          <div class="row-value">{{ data.studyEnvironmentText }}</div>
        </div>
        <div v-if="data.computerUsageTimeText" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:computer-line" class="label-icon" />
            <span>电脑使用时间</span>
          </div>
          <div class="row-value">{{ data.computerUsageTimeText }}</div>
        </div>
        <div
          v-if="!data.studyInRoomText && !data.studyEnvironmentText && !data.computerUsageTimeText"
          class="empty-state"
        >
          <span class="empty-text">暂无学习习惯信息</span>
        </div>
      </div>
    </ElCard>

    <!-- 娱乐习惯卡片 -->
    <ElCard class="info-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <ArtSvgIcon icon="ri:gamepad-line" class="header-icon" />
          <span class="header-title">娱乐习惯</span>
        </div>
      </template>
      <div class="info-list">
        <div v-if="data.gamingPreferenceText" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:gamepad-line" class="label-icon" />
            <span>游戏偏好</span>
          </div>
          <div class="row-value">{{ data.gamingPreferenceText }}</div>
        </div>
        <div v-if="data.musicPreferenceText" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:music-2-line" class="label-icon" />
            <span>听音乐偏好</span>
          </div>
          <div class="row-value">{{ data.musicPreferenceText }}</div>
        </div>
        <div v-if="data.musicVolumeText" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:volume-down-line" class="label-icon" />
            <span>音乐音量偏好</span>
          </div>
          <div class="row-value">{{ data.musicVolumeText }}</div>
        </div>
        <div v-if="data.eatInRoomText" class="info-row">
          <div class="row-label">
            <ArtSvgIcon icon="ri:restaurant-line" class="label-icon" />
            <span>宿舍饮食</span>
          </div>
          <div class="row-value">{{ data.eatInRoomText }}</div>
        </div>
        <div
          v-if="
            !data.gamingPreferenceText &&
            !data.musicPreferenceText &&
            !data.musicVolumeText &&
            !data.eatInRoomText
          "
          class="empty-state"
        >
          <span class="empty-text">暂无娱乐习惯信息</span>
        </div>
      </div>
    </ElCard>

    <!-- 特殊需求卡片 -->
    <ElCard v-if="data.specialNeeds || data.roommatePreference" class="info-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <ArtSvgIcon icon="ri:information-line" class="header-icon" />
          <span class="header-title">特殊需求</span>
        </div>
      </template>
      <div class="special-content">
        <div v-if="data.specialNeeds" class="special-item">
          <span class="special-label">特殊需求</span>
          <p class="special-text">{{ data.specialNeeds }}</p>
        </div>
        <div v-if="data.roommatePreference" class="special-item">
          <span class="special-label">室友偏好</span>
          <p class="special-text">{{ data.roommatePreference }}</p>
        </div>
      </div>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { ElCard } from 'element-plus'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'

  defineOptions({ name: 'StudentLifestyleInfo' })

  interface Props {
    /** 学生数据 */
    data: Partial<Api.StudentManage.StudentDetail>
  }

  withDefaults(defineProps<Props>(), {
    data: () => ({})
  })
</script>

<style lang="scss" scoped>
  .student-lifestyle-info {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .info-card {
    .special-content {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .special-item {
      display: flex;
      flex-direction: column;
      gap: 8px;

      .special-label {
        font-size: 13px;
        font-weight: 600;
        color: var(--el-text-color-secondary);
      }

      .special-text {
        padding: 12px;
        margin: 0;
        font-size: 14px;
        line-height: 1.6;
        color: var(--el-text-color-regular);
        word-break: break-word;
        background: var(--el-fill-color-extra-light);
        border-radius: var(--el-border-radius-small);
      }
    }
  }
</style>
