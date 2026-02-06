<!-- 报修头部卡片组件 -->
<template>
  <div class="repair-header-card">
    <!-- 主信息区 -->
    <div class="main-info">
      <!-- 图标 -->
      <div class="icon-wrapper">
        <div class="icon-ring">
          <div class="icon-box">
            <ArtSvgIcon icon="ri:tools-line" class="header-icon" />
          </div>
        </div>
        <span v-if="repair?.status !== undefined" class="status-badge" :class="statusBadgeClass">
          {{ repair?.statusText || '-' }}
        </span>
      </div>

      <!-- 核心信息 -->
      <div class="core-info">
        <div class="title-line">
          <h3 class="repair-title">报修单 #{{ repair?.id ?? '-' }}</h3>
        </div>
        <div class="repair-no">
          <ArtSvgIcon icon="ri:calendar-line" class="no-icon" />
          <span class="no-text">{{ repair?.createTime || '-' }}</span>
        </div>
      </div>
    </div>

    <!-- 扩展信息 -->
    <div class="extra-info">
      <div v-if="repair?.studentInfo?.studentName" class="info-chip">
        <ArtSvgIcon icon="ri:user-line" class="chip-icon" />
        <span>{{ repair.studentInfo.studentName }}</span>
      </div>
      <div v-if="repair?.roomCode" class="info-chip">
        <ArtSvgIcon icon="ri:home-4-line" class="chip-icon" />
        <span>{{ repair.roomCode }}{{ repair?.bedCode ? ` - ${repair.bedCode}` : '' }}</span>
      </div>
      <div v-if="repair?.repairTypeText" class="info-chip">
        <ArtSvgIcon icon="ri:settings-3-line" class="chip-icon" />
        <span>{{ repair.repairTypeText }}</span>
      </div>
      <div v-if="repair?.urgentLevelText" class="info-chip">
        <ArtSvgIcon icon="ri:alert-line" class="chip-icon" />
        <span>{{ repair.urgentLevelText }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import { computed } from 'vue'

  defineOptions({ name: 'ArtRepairHeaderCard' })

  type RepairDetail = {
    id?: number
    studentInfo?: Api.Common.StudentBasicInfo
    roomCode?: string
    bedCode?: string
    repairType?: number
    repairTypeText?: string
    urgentLevel?: number
    urgentLevelText?: string
    status?: number
    statusText?: string
    createTime?: string
  }

  interface Props {
    /** 报修数据 */
    repair?: Partial<RepairDetail>
  }

  const props = withDefaults(defineProps<Props>(), {
    repair: undefined
  })

  const statusBadgeClass = computed(() => {
    const s = props.repair?.status
    return {
      'is-pending': s === 1,
      'is-accepted': s === 2,
      'is-repairing': s === 3,
      'is-done': s === 4
    }
  })
</script>

<style lang="scss" scoped>
  .repair-header-card {
    display: flex;
    flex-direction: column;
    gap: 16px;
    padding: 20px;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: calc(var(--custom-radius) / 2 + 2px) !important;
    transition:
      border-color 0.2s ease,
      box-shadow 0.2s ease;

    &:hover {
      background: linear-gradient(
        0deg,
        #fff0 0%,
        color-mix(in srgb, var(--el-color-primary) 3%, var(--el-bg-color)) 100%
      );
      border-color: var(--el-color-primary-light-7);
      box-shadow: 0 2px 5px rgb(0 0 0 / 4%);
    }

    .main-info {
      display: flex;
      gap: 16px;
      align-items: center;
    }

    .icon-wrapper {
      position: relative;
      flex-shrink: 0;

      .icon-ring {
        padding: 3px;
        background: linear-gradient(
          135deg,
          var(--el-color-primary-light-5),
          var(--el-color-primary-light-8)
        );
        border-radius: 50%;
      }

      .icon-box {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 64px;
        height: 64px;
        background: var(--el-bg-color);
        border-radius: 50%;
      }

      .status-badge {
        position: absolute;
        right: 50%;
        bottom: -6px;
        padding: 2px 8px;
        font-size: 11px;
        font-weight: 500;
        color: var(--el-text-color-secondary);
        white-space: nowrap;
        background: var(--el-fill-color);
        border-radius: 10px;
        transform: translateX(50%);

        &.is-pending {
          color: var(--el-color-warning);
          background: var(--el-color-warning-light-9);
        }

        &.is-accepted,
        &.is-repairing {
          color: var(--el-color-primary);
          background: var(--el-color-primary-light-9);
        }

        &.is-done {
          color: var(--el-color-success);
          background: var(--el-color-success-light-9);
        }
      }
    }

    .core-info {
      flex: 1;
      min-width: 0;

      .title-line {
        margin-bottom: 3px;
      }

      .repair-title {
        margin: 0;
        font-size: 20px;
        font-weight: 600;
        line-height: 1.3;
        color: var(--el-text-color-primary);
      }

      .repair-no {
        display: inline-flex;
        align-items: center;
        padding-left: 4px;
        font-size: 13px;
        font-weight: 500;
        color: var(--el-text-color-regular);
        background: var(--el-color-primary-light-9);
        border-top-left-radius: var(--el-border-radius-base);

        .no-icon {
          width: 12px;
          height: 12px;
          padding-right: 2px;
          color: var(--el-text-color-placeholder);
        }

        .no-text {
          display: inline-block;
          padding-left: 2px;
          font-size: 13px;
          font-weight: 500;
          background: var(--el-color-primary-light-9);
          border-top-left-radius: var(--el-border-radius-base);
        }
      }
    }

    .extra-info {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      padding-top: 12px;
      border-top: 1px dashed var(--el-border-color-lighter);

      .info-chip {
        display: inline-flex;
        gap: 6px;
        align-items: center;
        padding: 6px 12px;
        font-size: 13px;
        color: var(--el-text-color-regular);
        background: var(--el-bg-color);
        border: 1px solid var(--el-border-color-lighter);
        border-radius: 16px;
        transition: all 0.15s ease;

        &:hover {
          background: var(--el-color-primary-light-9);
          border-color: var(--el-color-primary-light-7);
        }

        .chip-icon {
          width: 14px;
          height: 14px;
          color: var(--el-color-primary);
        }
      }
    }
  }

  .header-icon {
    width: 32px;
    height: 32px;
    font-size: 32px;
    color: var(--el-color-primary-light-3);
  }
</style>
