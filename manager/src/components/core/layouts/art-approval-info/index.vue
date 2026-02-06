<!-- 通用审批信息组件 -->
<template>
  <div class="art-approval-info" v-loading="loading">
    <!-- 申请信息插槽 -->
    <slot />

    <template v-if="approvalInstance">
      <!-- 审批状态卡片 -->
      <ElCard class="info-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <ArtSvgIcon icon="ri:file-check-line" class="header-icon" />
            <span class="header-title">审批状态</span>
          </div>
        </template>
        <div class="info-list">
          <div class="info-row">
            <div class="row-label">
              <ArtSvgIcon icon="ri:check-line" class="label-icon" />
              <span>审批状态</span>
            </div>
            <div class="row-value">
              <ElTag :type="getStatusType(approvalInstance.status)" size="small">
                {{ approvalInstance.statusText }}
              </ElTag>
            </div>
          </div>
          <div v-if="approvalInstance.currentNodeName" class="info-row">
            <div class="row-label">
              <ArtSvgIcon icon="ri:node-tree" class="label-icon" />
              <span>当前节点</span>
            </div>
            <div class="row-value">
              <ElTag type="warning" size="small">
                {{ approvalInstance.currentNodeName }}
              </ElTag>
            </div>
          </div>
          <div v-if="approvalInstance.flowName" class="info-row">
            <div class="row-label">
              <ArtSvgIcon icon="ri:flow-chart" class="label-icon" />
              <span>审批流程</span>
            </div>
            <div class="row-value">{{ approvalInstance.flowName }}</div>
          </div>
          <div v-if="approvalInstance.startTime" class="info-row">
            <div class="row-label">
              <ArtSvgIcon icon="ri:calendar-line" class="label-icon" />
              <span>提交时间</span>
            </div>
            <div class="row-value">{{ approvalInstance.startTime }}</div>
          </div>
        </div>
      </ElCard>

      <!-- 审批进度卡片 -->
      <ElCard class="info-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <ArtSvgIcon icon="ri:history-line" class="header-icon" />
            <span class="header-title">审批进度</span>
          </div>
        </template>
        <div class="timeline-content">
          <ElTimeline v-if="timelineItems.length > 0">
            <ElTimelineItem
              v-for="(item, index) in timelineItems"
              :key="`${item.type}-${item.node?.id || index}`"
              :type="getTimelineType(item)"
              :timestamp="item.timestamp || ''"
              placement="top"
            >
              <div class="timeline-item">
                <div class="timeline-header">
                  <span class="timeline-node-name">{{ item.nodeName }}</span>
                  <span
                    v-if="item.type === 'node' && getNodeTypeText(item.node)"
                    class="timeline-node-type"
                  >
                    {{ getNodeTypeText(item.node) }}
                  </span>
                  <ElTag v-if="item.type === 'start'" type="primary" size="small"> 已发起 </ElTag>
                  <ElTag
                    v-else-if="item.type === 'end'"
                    :type="item.status === 2 ? 'success' : 'danger'"
                    size="small"
                  >
                    {{ item.status === 2 ? '已完成' : '已拒绝' }}
                  </ElTag>
                  <ElTag
                    v-else-if="item.record"
                    :type="item.record.action === 1 ? 'success' : 'danger'"
                    size="small"
                  >
                    {{ item.record.actionText }}
                  </ElTag>
                  <ElTag v-else type="warning" size="small">待审批</ElTag>
                </div>
                <div v-if="item.type === 'start'" class="timeline-meta">
                  <span class="timeline-approver">发起人：{{ item.applicantName }}</span>
                </div>
                <div v-else-if="item.type === 'node' && item.record" class="timeline-meta">
                  <span class="timeline-approver">审批人：{{ item.record.approverName }}</span>
                </div>
                <div
                  v-else-if="
                    item.type === 'node' && !item.record && getNodeAssigneeNames(item.node)
                  "
                  class="timeline-meta"
                >
                  <span class="timeline-approver pending"
                    >审批人：{{ getNodeAssigneeNames(item.node) }}</span
                  >
                </div>
                <div v-if="item.type === 'node' && item.record?.opinion" class="timeline-opinion">
                  意见：{{ item.record.opinion }}
                </div>
              </div>
            </ElTimelineItem>
          </ElTimeline>
          <div v-else class="empty-state">
            <span class="empty-text">暂无审批节点</span>
          </div>
        </div>
      </ElCard>

      <!-- 审批操作卡片 -->
      <ElCard
        v-if="approvalInstance.canApprove && approvalInstance.status === 1"
        class="info-card"
        shadow="hover"
      >
        <template #header>
          <div class="card-header">
            <ArtSvgIcon icon="ri:edit-line" class="header-icon" />
            <span class="header-title">审批操作</span>
          </div>
        </template>
        <div class="approval-action-content">
          <ElForm label-width="80px">
            <ElFormItem label="审批意见">
              <ElInput
                v-model="approvalOpinion"
                type="textarea"
                :rows="4"
                maxlength="200"
                show-word-limit
                word-limit-position="outside"
                placeholder="请输入审批意见（可选）"
              />
            </ElFormItem>
            <ElFormItem>
              <ElSpace>
                <ElButton type="danger" :loading="rejectLoading" @click="handleApprove(2)" v-ripple>
                  拒绝
                </ElButton>
                <ElButton
                  type="primary"
                  :loading="approveLoading"
                  @click="handleApprove(1)"
                  v-ripple
                >
                  通过
                </ElButton>
              </ElSpace>
            </ElFormItem>
          </ElForm>
        </div>
      </ElCard>
    </template>

    <!-- 未发起审批 -->
    <ElCard v-else class="info-card" shadow="hover">
      <div class="empty-state">
        <ElEmpty description="该申请尚未发起审批流程" :image-size="80" />
      </div>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { ref, computed, watch } from 'vue'
  import {
    ElCard,
    ElTag,
    ElTimeline,
    ElTimelineItem,
    ElEmpty,
    ElForm,
    ElFormItem,
    ElInput,
    ElButton,
    ElSpace,
    ElMessage,
    ElMessageBox
  } from 'element-plus'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import {
    fetchGetInstanceByBusiness,
    fetchDoApprove,
    type ApprovalInstance,
    type ApprovalNode,
    type ApprovalRecord
  } from '@/api/approval-manage'

  defineOptions({ name: 'ArtApprovalInfo' })

  interface Props {
    /** 业务类型 */
    businessType: string
    /** 业务ID */
    businessId: number | null
    /** 加载状态 */
    loading?: boolean
  }

  interface Emits {
    (e: 'approval-success'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    loading: false
  })

  const emit = defineEmits<Emits>()

  const approvalLoading = ref(false)
  const approveLoading = ref(false)
  const rejectLoading = ref(false)
  const approvalInstance = ref<ApprovalInstance | null>(null)
  const approvalOpinion = ref('')

  // 合并节点和记录，显示所有流程节点（包括开始和结束）
  const timelineItems = computed(() => {
    if (!approvalInstance.value) return []

    const nodes = approvalInstance.value.nodes || []
    const records = approvalInstance.value.records || []
    const items: Array<{
      type: 'start' | 'node' | 'end'
      node?: ApprovalNode
      record?: ApprovalRecord
      nodeName: string
      timestamp?: string
      applicantName?: string
      status?: number
    }> = []

    // 1. 添加开始节点
    items.push({
      type: 'start',
      nodeName: '发起申请',
      timestamp: approvalInstance.value.startTime,
      applicantName: approvalInstance.value.applicantName
    })

    // 2. 添加所有审批节点
    const sortedNodes = [...nodes].sort((a, b) => a.nodeOrder - b.nodeOrder)
    sortedNodes.forEach((node) => {
      const record = records.find((r) => r.nodeId === node.id)
      items.push({
        type: 'node',
        node,
        record,
        nodeName: node.nodeName,
        timestamp: record?.approveTime
      })
    })

    // 3. 如果流程已结束，添加结束节点
    const status = approvalInstance.value.status
    if (status === 2 || status === 3) {
      // 2已通过 3已拒绝
      items.push({
        type: 'end',
        nodeName: status === 2 ? '流程结束（已通过）' : '流程结束（已拒绝）',
        timestamp: approvalInstance.value.endTime,
        status
      })
    }

    return items
  })

  const getStatusType = (status?: number) => {
    switch (status) {
      case 1:
        return 'warning'
      case 2:
        return 'success'
      case 3:
        return 'danger'
      case 4:
        return 'info'
      default:
        return 'info'
    }
  }

  // 获取 Timeline 项的类型
  const getTimelineType = (item: {
    type: 'start' | 'node' | 'end'
    record?: ApprovalRecord
    status?: number
  }): 'primary' | 'success' | 'warning' | 'info' | 'danger' => {
    if (item.type === 'start') return 'primary'
    if (item.type === 'end') {
      return item.status === 2 ? 'success' : 'danger'
    }
    if (!item.record) return 'warning' // 待审批状态 - 黄色
    return item.record.action === 1 ? 'success' : 'danger' // 通过-绿色，驳回-红色
  }

  // 获取节点审批人名称列表
  const getNodeAssigneeNames = (node?: ApprovalNode) => {
    if (!node?.assignees?.length) return ''
    return node.assignees
      .map((a) => a.assigneeName)
      .filter(Boolean)
      .join('、')
  }

  // 获取节点类型文本
  const getNodeTypeText = (node?: ApprovalNode) => {
    if (!node) return ''
    if (node.nodeTypeText) return node.nodeTypeText
    switch (node.nodeType) {
      case 1:
        return '串行'
      case 2:
        return '会签'
      case 3:
        return '或签'
      default:
        return ''
    }
  }

  const loadApprovalInfo = async () => {
    if (!props.businessId) {
      approvalInstance.value = null
      return
    }

    approvalLoading.value = true
    try {
      approvalInstance.value = await fetchGetInstanceByBusiness(
        props.businessType,
        props.businessId
      )
    } catch (error) {
      console.error('加载审批信息失败:', error)
      approvalInstance.value = null
    } finally {
      approvalLoading.value = false
    }
  }

  const handleApprove = async (action: 1 | 2) => {
    if (!approvalInstance.value) return

    const actionText = action === 1 ? '通过' : '拒绝'

    try {
      await ElMessageBox.confirm(`确定要${actionText}该申请吗？`, `确认${actionText}`, {
        type: action === 1 ? 'success' : 'warning'
      })

      if (action === 1) {
        approveLoading.value = true
      } else {
        rejectLoading.value = true
      }

      await fetchDoApprove({
        instanceId: approvalInstance.value.id,
        action,
        opinion: approvalOpinion.value || undefined
      })

      ElMessage.success(`审批${actionText}成功`)
      approvalOpinion.value = ''

      // 重新加载审批信息
      await loadApprovalInfo()

      emit('approval-success')
    } catch (error) {
      if (error !== 'cancel') {
        console.error('审批失败:', error)
      }
    } finally {
      approveLoading.value = false
      rejectLoading.value = false
    }
  }

  // 监听业务ID变化
  watch(
    () => props.businessId,
    () => {
      loadApprovalInfo()
    },
    { immediate: true }
  )

  // 合并 loading 状态
  const loading = computed(() => props.loading || approvalLoading.value)
</script>

<style lang="scss" scoped>
  .art-approval-info {
    display: flex;
    flex-direction: column;
    gap: 16px;

    .info-card {
      .timeline-content {
        min-height: 200px;
        max-height: 600px;
        padding: 12px 0;
        overflow-y: auto;

        :deep(.el-timeline) {
          padding-left: 4px;
        }

        .timeline-item {
          .timeline-header {
            display: flex;
            gap: 8px;
            align-items: center;
            margin-bottom: 8px;

            .timeline-node-name {
              font-size: 14px;
              font-weight: 500;
              color: var(--el-text-color-primary);
            }

            .timeline-node-type {
              margin-left: 3px;
              font-size: 12px;
              color: var(--el-text-color-secondary);
            }
          }

          .timeline-meta {
            margin-bottom: 4px;

            .timeline-approver {
              font-size: 13px;
              color: var(--el-text-color-secondary);

              &.pending {
                color: var(--el-color-warning);
              }
            }
          }

          .timeline-opinion {
            font-size: 13px;
            line-height: 1.5;
            color: var(--el-text-color-regular);
          }
        }
      }

      .approval-action-content {
        padding: 12px 0;

        :deep(.el-form-item) {
          margin-bottom: 16px;
        }
      }
    }
  }
</style>
