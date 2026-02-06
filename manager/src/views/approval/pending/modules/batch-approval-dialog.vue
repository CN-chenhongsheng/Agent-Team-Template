<!-- 批量审核对话框 -->
<template>
  <ElDialog
    v-model="dialogVisible"
    title="批量审核"
    width="600px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
  >
    <div class="batch-approval-content">
      <ElAlert
        :title="`已选择 ${selectedInstances.length} 个审批项，请选择审核操作`"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 20px"
      />

      <ElForm label-width="100px">
        <ElFormItem label="审核操作">
          <ElRadioGroup v-model="approvalAction">
            <ElRadio :label="1">通过</ElRadio>
            <ElRadio :label="2">拒绝</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
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
      </ElForm>

      <!-- 待审核列表 -->
      <div class="approval-list">
        <div class="list-header">
          <span class="list-title">待审核列表</span>
          <span class="list-count">共 {{ selectedInstances.length }} 项</span>
        </div>
        <div class="list-content">
          <div v-for="(item, index) in selectedInstances" :key="item.id" class="list-item">
            <div class="item-index">{{ index + 1 }}</div>
            <div class="item-info">
              <div class="item-main">
                <ElTag type="info" size="small">{{ item.businessTypeText }}</ElTag>
                <span class="item-applicant">{{ item.applicantName }}</span>
                <span class="item-flow">{{ item.flowName }}</span>
              </div>
              <div class="item-sub">
                <span class="item-node">当前节点：{{ item.currentNodeName }}</span>
              </div>
            </div>
            <div v-if="processingStatus[item.id]" class="item-status">
              <ElIcon v-if="processingStatus[item.id] === 'success'" class="status-icon success">
                <Check />
              </ElIcon>
              <ElIcon v-else-if="processingStatus[item.id] === 'error'" class="status-icon error">
                <Close />
              </ElIcon>
              <ElIcon v-else class="status-icon loading">
                <Loading />
              </ElIcon>
            </div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <ElSpace>
        <ElButton @click="handleClose">取消</ElButton>
        <ElButton
          type="primary"
          :loading="processing"
          :disabled="processing"
          @click="handleSubmit"
          v-ripple
        >
          {{ processing ? '处理中...' : '确认审核' }}
        </ElButton>
      </ElSpace>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { ref, computed, watch } from 'vue'
  import {
    ElDialog,
    ElAlert,
    ElForm,
    ElFormItem,
    ElRadioGroup,
    ElRadio,
    ElInput,
    ElButton,
    ElSpace,
    ElTag,
    ElIcon,
    ElMessage
  } from 'element-plus'
  import { Check, Close, Loading } from '@element-plus/icons-vue'
  import { fetchDoApprove, type ApprovalInstance } from '@/api/approval-manage'

  defineOptions({ name: 'BatchApprovalDialog' })

  interface Props {
    /** 是否显示 */
    visible: boolean
    /** 选中的审批实例 */
    selectedInstances: ApprovalInstance[]
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'success'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    visible: false,
    selectedInstances: () => []
  })

  const emit = defineEmits<Emits>()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const approvalAction = ref<1 | 2>(1)
  const approvalOpinion = ref('')
  const processing = ref(false)
  const processingStatus = ref<Record<number, 'loading' | 'success' | 'error'>>({})

  // 监听对话框显示，重置状态
  watch(
    () => props.visible,
    (newVal) => {
      if (newVal) {
        approvalAction.value = 1
        approvalOpinion.value = ''
        processing.value = false
        processingStatus.value = {}
      }
    }
  )

  // 关闭对话框
  const handleClose = () => {
    if (processing.value) {
      ElMessage.warning('正在处理中，请稍候...')
      return
    }
    dialogVisible.value = false
  }

  // 提交批量审核
  const handleSubmit = async () => {
    if (props.selectedInstances.length === 0) {
      ElMessage.warning('没有选中的审批项')
      return
    }

    const actionText = approvalAction.value === 1 ? '通过' : '拒绝'

    try {
      processing.value = true
      let successCount = 0
      let failCount = 0
      const errors: string[] = []

      // 逐个处理审批项
      for (const instance of props.selectedInstances) {
        processingStatus.value[instance.id] = 'loading'

        try {
          await fetchDoApprove({
            instanceId: instance.id,
            action: approvalAction.value,
            opinion: approvalOpinion.value || undefined
          })
          processingStatus.value[instance.id] = 'success'
          successCount++
        } catch (error: any) {
          processingStatus.value[instance.id] = 'error'
          failCount++
          const errorMsg = error?.message || '审批失败'
          errors.push(`${instance.applicantName}：${errorMsg}`)
        }

        // 添加短暂延迟，让用户看到处理进度
        await new Promise((resolve) => setTimeout(resolve, 200))
      }

      // 显示处理结果
      if (failCount === 0) {
        ElMessage.success(`批量${actionText}成功，共处理 ${successCount} 项`)
      } else {
        ElMessage.warning(`批量${actionText}完成：成功 ${successCount} 项，失败 ${failCount} 项`)
        if (errors.length > 0) {
          console.error('批量审核错误详情:', errors)
        }
      }

      // 延迟关闭对话框，让用户看到处理结果
      setTimeout(() => {
        dialogVisible.value = false
        emit('success')
      }, 1000)
    } catch (error) {
      console.error('批量审核失败:', error)
      ElMessage.error('批量审核失败，请重试')
    } finally {
      processing.value = false
    }
  }
</script>

<style lang="scss" scoped>
  .batch-approval-content {
    .approval-list {
      margin-top: 20px;
      overflow: hidden;
      border: 1px solid var(--el-border-color-lighter);
      border-radius: var(--el-border-radius-base);

      .list-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 12px 16px;
        background: var(--el-fill-color-lighter);
        border-bottom: 1px solid var(--el-border-color-lighter);

        .list-title {
          font-size: 14px;
          font-weight: 500;
          color: var(--el-text-color-primary);
        }

        .list-count {
          font-size: 13px;
          color: var(--el-text-color-regular);
        }
      }

      .list-content {
        max-height: 300px;
        padding: 8px;
        overflow-y: auto;

        .list-item {
          display: flex;
          gap: 12px;
          align-items: center;
          padding: 10px;
          border-bottom: 1px solid var(--el-border-color-lighter);
          transition: background-color 0.2s;

          &:last-child {
            border-bottom: none;
          }

          &:hover {
            background: var(--el-fill-color-lighter);
          }

          .item-index {
            display: flex;
            flex-shrink: 0;
            align-items: center;
            justify-content: center;
            width: 24px;
            height: 24px;
            font-size: 12px;
            font-weight: 500;
            color: var(--el-text-color-regular);
            background: var(--el-fill-color);
            border-radius: 50%;
          }

          .item-info {
            flex: 1;
            min-width: 0;

            .item-main {
              display: flex;
              gap: 8px;
              align-items: center;
              margin-bottom: 4px;

              .item-applicant {
                font-size: 14px;
                font-weight: 500;
                color: var(--el-text-color-primary);
              }

              .item-flow {
                font-size: 13px;
                color: var(--el-text-color-regular);
              }
            }

            .item-sub {
              .item-node {
                font-size: 12px;
                color: var(--el-text-color-secondary);
              }
            }
          }

          .item-status {
            flex-shrink: 0;

            .status-icon {
              font-size: 18px;

              &.success {
                color: var(--el-color-success);
              }

              &.error {
                color: var(--el-color-danger);
              }

              &.loading {
                color: var(--el-color-primary);
                animation: rotating 1s linear infinite;
              }
            }
          }
        }
      }
    }
  }

  @keyframes rotating {
    from {
      transform: rotate(0deg);
    }

    to {
      transform: rotate(360deg);
    }
  }
</style>
