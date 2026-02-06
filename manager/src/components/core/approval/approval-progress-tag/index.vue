<template>
  <ElPopover
    v-if="approvalProgress"
    placement="bottom-start"
    trigger="hover"
    :width="260"
    popper-class="approval-progress-popover"
  >
    <template #reference>
      <div class="approval-progress-card" :class="statusClass">
        <ElProgress
          v-if="isInProgress"
          :percentage="progressPercent"
          :stroke-width="15"
          :text-inside="true"
          :show-text="true"
          class="progress-bar"
        />
        <ElTag v-else :type="tagType" size="small" effect="plain" class="status-badge">
          {{ approvalProgress.statusText }}
        </ElTag>
      </div>
    </template>
    <div class="popover-content">
      <div class="popover-header">
        <div class="header-title">
          <ArtSvgIcon icon="ri:flow-chart" class="title-icon" />
          <span class="title-text">审批流程</span>
        </div>
        <div class="meta-grid">
          <div class="meta-item">
            <ArtSvgIcon icon="ri:user-line" class="meta-icon" />
            <span class="meta-text">{{ approvalProgress.applicantName || '--' }}</span>
          </div>
          <div class="meta-item">
            <ArtSvgIcon icon="ri:time-line" class="meta-icon" />
            <span class="meta-text">{{ approvalProgress.startTime || '--' }}</span>
          </div>
        </div>
      </div>
      <div class="popover-progress">
        <div class="progress-header">
          <span class="progress-label">总体进度</span>
          <span class="progress-value">{{ progressPercent }}%</span>
        </div>
        <ElProgress :percentage="progressPercent" :stroke-width="10" :show-text="false" />
      </div>
      <div v-if="nodeTimeline.length" class="popover-timeline">
        <div
          v-for="(node, index) in nodeTimeline"
          :key="node.nodeId"
          class="timeline-item"
          :class="getTimelineStatusClass(node.status)"
        >
          <div class="timeline-left">
            <div class="timeline-dot" />
            <div v-if="index < nodeTimeline.length - 1" class="timeline-line" />
          </div>
          <div class="timeline-right">
            <div class="node-name">{{ node.nodeName }}</div>
            <div class="node-info">
              <span class="node-assignee">{{ node.assigneeNames }}</span>
              <span v-if="node.approveTime" class="node-time">{{ node.approveTime }}</span>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="timeline-empty">暂无流程节点</div>
    </div>
  </ElPopover>
  <span v-else class="empty-text">--</span>
</template>

<script setup lang="ts">
  import { computed } from 'vue'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'

  defineOptions({
    name: 'ApprovalProgressTag'
  })

  interface Props {
    /** 审批进度信息 */
    approvalProgress?: Api.Common.ApprovalProgress
  }

  const props = defineProps<Props>()

  const isInProgress = computed(() => props.approvalProgress?.status === 1)

  const nodeTimeline = computed(() => props.approvalProgress?.nodeTimeline ?? [])

  const progressPercent = computed(() => {
    if (!props.approvalProgress) return 0
    const value = props.approvalProgress.progressPercent
    if (typeof value === 'number') return value
    const total = props.approvalProgress.totalNodes ?? 0
    const completed = props.approvalProgress.completedNodes ?? 0
    if (total <= 0) return 0
    return Math.min(100, Math.max(0, Math.round((completed * 100) / total)))
  })

  const getTimelineStatusClass = (status?: number) => {
    if (status === 2) return 'is-approved'
    if (status === 3) return 'is-rejected'
    return 'is-pending'
  }

  /**
   * 根据状态映射 Tag 颜色
   */
  const tagType = computed(() => {
    if (!props.approvalProgress) return 'info'

    const statusColorMap: Record<number, 'warning' | 'success' | 'danger' | 'info'> = {
      1: 'warning', // 待审核
      2: 'success', // 已通过
      3: 'danger', // 已拒绝
      4: 'info' // 已完成
    }

    return statusColorMap[props.approvalProgress.status] || 'info'
  })

  /**
   * 根据状态映射卡片样式类名
   */
  const statusClass = computed(() => {
    if (!props.approvalProgress) return ''

    const classMap: Record<number, string> = {
      1: 'status-pending',
      2: 'status-approved',
      3: 'status-rejected',
      4: 'status-completed'
    }

    return classMap[props.approvalProgress.status] || ''
  })
</script>

<style lang="scss" scoped>
  .approval-progress-card {
    display: inline-flex;
    align-items: center;
    width: 100%;
    max-width: 100%;
    height: 100%;
    padding: 4px 2px;
    cursor: var(--cursor-pointer);
    border-radius: 4px;
    transition: all 0.2s ease;

    .status-badge {
      font-weight: 500;
    }

    .progress-bar {
      width: 100%;
      cursor: var(--cursor-pointer);
    }

    :deep(.el-progress-bar) {
      cursor: var(--cursor-pointer);
    }

    :deep(.el-progress-bar__outer) {
      cursor: var(--cursor-pointer);
      overflow: visible;
    }

    :deep(.el-progress-bar__inner) {
      box-shadow: 0 0 5px 0 var(--el-color-primary);
    }

    :deep(.el-progress__text) {
      font-size: 10px;
    }
  }

  .empty-text {
    font-size: 14px;
    color: var(--el-text-color-placeholder);
  }

  .approval-progress-popover {
    .popover-content {
      display: flex;
      flex-direction: column;
      gap: 14px;
    }

    .popover-header {
      display: flex;
      flex-direction: column;
      gap: 10px;

      .header-title {
        display: flex;
        gap: 8px;
        align-items: center;
        padding-bottom: 8px;
        border-bottom: 1px solid var(--el-border-color-lighter);

        .title-icon {
          font-size: 18px;
          color: var(--el-color-primary);
        }

        .title-text {
          font-size: 14px;
          font-weight: 600;
          color: var(--el-text-color-primary);
        }
      }

      .meta-grid {
        display: flex;
        flex-direction: column;
        gap: 8px;
      }

      .meta-item {
        display: flex;
        gap: 8px;
        align-items: center;
        font-size: 12px;

        .meta-icon {
          flex-shrink: 0;
          font-size: 14px;
          color: var(--el-color-primary);
        }

        .meta-text {
          color: var(--el-text-color-regular);
        }
      }
    }

    .popover-progress {
      display: flex;
      flex-direction: column;
      gap: 6px;

      .progress-header {
        display: flex;
        align-items: center;
        justify-content: space-between;

        .progress-label {
          font-size: 12px;
          font-weight: 500;
          color: var(--el-text-color-secondary);
        }

        .progress-value {
          font-size: 14px;
          font-weight: 600;
          color: var(--el-color-primary);
        }
      }
    }

    .popover-timeline {
      display: flex;
      flex-direction: column;
      max-height: 200px;
      overflow-y: auto;
    }

    .timeline-item {
      display: flex;
      gap: 10px;

      .timeline-left {
        display: flex;
        flex-direction: column;
        flex-shrink: 0;
        align-items: center;
        width: 10px;
      }

      .timeline-dot {
        flex-shrink: 0;
        width: 10px;
        height: 10px;
        background-color: var(--el-border-color);
        border-radius: 50%;
      }

      .timeline-line {
        flex: 1;
        width: 1px;
        min-height: 12px;
        background-color: var(--el-border-color-lighter);
      }

      .timeline-right {
        flex: 1;
        padding-bottom: 12px;
      }

      .node-name {
        margin-bottom: 4px;
        font-size: 13px;
        line-height: 1;
        color: var(--el-text-color-primary);
      }

      .node-info {
        display: flex;
        gap: 8px;
        justify-content: space-between;
        font-size: 12px;
        color: var(--el-text-color-secondary);
      }

      .node-assignee {
        flex: 1;
      }

      .node-time {
        flex-shrink: 0;
      }

      &.is-approved .timeline-dot {
        background-color: var(--el-color-success);
      }

      &.is-rejected .timeline-dot {
        background-color: var(--el-color-danger);
      }

      &.is-pending .timeline-dot {
        background-color: var(--el-color-warning);
      }
    }

    .timeline-empty {
      padding: 12px 0;
      font-size: 12px;
      color: var(--el-text-color-secondary);
      text-align: center;
    }
  }
</style>
