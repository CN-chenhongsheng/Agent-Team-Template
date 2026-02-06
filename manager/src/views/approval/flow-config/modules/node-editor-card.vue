<!-- 节点编辑卡片 - 内联编辑器 -->
<template>
  <Transition name="slide-fade">
    <div
      v-if="visible && (nodeData || nodeType === 'start')"
      ref="cardRef"
      class="node-editor-card"
      :style="cardStyle"
    >
      <!-- 卡片头部 -->
      <div
        class="card-header draggable-header"
        :class="{ 'start-header': nodeType === 'start' }"
        @mousedown="handleDragStart"
      >
        <span class="card-title">{{ nodeType === 'start' ? '流程配置' : '编辑节点' }}</span>
        <div class="card-actions">
          <ElButton size="small" text @click="handleClose" class="close-btn">
            <svg
              width="14"
              height="14"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </ElButton>
          <ElButton size="small" text type="primary" @click="handleConfirm" class="confirm-btn">
            <svg
              width="14"
              height="14"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <polyline points="20 6 9 17 4 12"></polyline>
            </svg>
          </ElButton>
        </div>
      </div>

      <!-- 开始节点编辑内容 -->
      <div v-if="nodeType === 'start'" class="card-body">
        <!-- 流程名称 -->
        <div class="form-item">
          <label class="form-label required">流程名称</label>
          <ElInput v-model="localFlowData.flowName" placeholder="请输入流程名称" size="small" />
        </div>

        <!-- 流程编码 -->
        <div class="form-item">
          <label class="form-label required">流程编码</label>
          <ElInput
            v-model="localFlowData.flowCode"
            placeholder="请输入流程编码（唯一）"
            size="small"
            :disabled="dialogType === 'edit'"
          />
        </div>

        <!-- 业务类型 -->
        <div class="form-item">
          <label class="form-label required">业务类型</label>
          <ElSelect
            v-model="localFlowData.businessType"
            placeholder="请选择业务类型"
            size="small"
            style="width: 100%"
            :disabled="dialogType === 'edit'"
          >
            <ElOption
              v-for="item in businessTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </ElSelect>
        </div>

        <!-- 备注 -->
        <div class="form-item">
          <label class="form-label">备注</label>
          <ElInput
            v-model="localFlowData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
            size="small"
          />
        </div>
      </div>

      <!-- 审批节点编辑内容 -->
      <div v-else class="card-body">
        <!-- 节点名称 -->
        <div class="form-item">
          <label class="form-label">节点名称</label>
          <ElInput v-model="localData.nodeName" placeholder="请输入节点名称" size="small" />
        </div>

        <!-- 节点类型 -->
        <div class="form-item">
          <label class="form-label">审批类型</label>
          <ElSelect
            v-model="localData.nodeType"
            placeholder="请选择"
            size="small"
            style="width: 100%"
          >
            <ElOption label="串行（单人审批）" :value="1" />
            <ElOption label="会签（所有人通过）" :value="2" />
            <ElOption label="或签（任一人通过）" :value="3" />
          </ElSelect>
        </div>

        <!-- 驳回动作 -->
        <div class="form-item">
          <label class="form-label">驳回处理</label>
          <ElSelect
            v-model="localData.rejectAction"
            placeholder="请选择"
            size="small"
            style="width: 100%"
          >
            <ElOption label="直接结束" :value="1" />
            <ElOption label="退回申请人" :value="2" />
            <ElOption label="退回上一节点" :value="3" />
          </ElSelect>
        </div>

        <!-- 审批人 -->
        <div class="form-item">
          <label class="form-label">审批人</label>
          <div v-if="localData.assignees && localData.assignees.length > 0" class="assignee-list">
            <ElTag
              v-for="(assignee, index) in localData.assignees"
              :key="assignee.assigneeId || index"
              closable
              type="success"
              size="small"
              @close="handleRemoveAssignee(index)"
            >
              {{ assignee.assigneeName }}
            </ElTag>
          </div>
          <div v-else class="assignee-list">
            <div class="empty-assignee">
              <span>暂无审批人</span>
            </div>
          </div>
          <div class="assignee-actions">
            <ElButton size="small" @click="handleSelectApprover">
              <i class="ri-user-add-line mr-1"></i>
              选择审批人
            </ElButton>
          </div>
        </div>
      </div>

      <!-- 卡片箭头指示器 -->
      <div class="card-arrow"></div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
  import { computed, watch, reactive, ref, onMounted, onUnmounted } from 'vue'
  import type { ApprovalNode, ApprovalAssignee, ApprovalFlow } from '@/api/approval-manage'

  interface BusinessTypeOption {
    label: string
    value: string
  }

  interface Props {
    visible: boolean
    nodeData: ApprovalNode | null
    nodeType?: 'start' | 'approval' | 'end' | null
    position?: { x: number; y: number }
    flowData?: ApprovalFlow
    businessTypeOptions?: BusinessTypeOption[]
    dialogType?: 'add' | 'edit'
  }

  const props = withDefaults(defineProps<Props>(), {
    visible: false,
    nodeData: null,
    nodeType: 'approval',
    position: () => ({ x: 0, y: 0 }),
    flowData: () => ({
      flowName: '',
      flowCode: '',
      businessType: '',
      status: 1,
      description: '',
      nodes: []
    }),
    businessTypeOptions: () => [],
    dialogType: 'add'
  })

  const emit = defineEmits<{
    'update:visible': [value: boolean]
    update: [data: ApprovalNode]
    delete: []
    'select-approver': []
    'update-flow': [
      data: {
        flowName?: string
        flowCode?: string
        businessType?: string
        status?: number
        description?: string
      }
    ]
    'update:position': [position: { x: number; y: number }]
  }>()

  const cardRef = ref<HTMLElement>()

  // 拖拽状态
  const isDragging = ref(false)
  const dragStartPos = ref({ x: 0, y: 0 })
  const dragOffset = ref({ x: 0, y: 0 })

  // 本地数据副本 - 审批节点
  const localData = reactive<ApprovalNode>({
    nodeName: '',
    nodeOrder: 1,
    nodeType: 1,
    rejectAction: 1,
    assignees: []
  })

  // 本地数据副本 - 流程信息
  const localFlowData = reactive({
    flowName: '',
    flowCode: '',
    businessType: '',
    status: 1,
    description: ''
  })

  // 计算卡片位置样式（支持拖拽偏移）
  const cardStyle = computed(() => {
    const { x, y } = props.position
    const baseX = x + 130 // 节点右侧偏移
    const baseY = y - 100 // 垂直居中偏移
    return {
      left: `${baseX + dragOffset.value.x}px`,
      top: `${baseY + dragOffset.value.y}px`
    }
  })

  // 监听节点数据变化
  watch(
    () => props.nodeData,
    (newData) => {
      if (newData) {
        localData.id = newData.id
        localData.nodeName = newData.nodeName || ''
        localData.nodeOrder = newData.nodeOrder
        localData.nodeType = newData.nodeType || 1
        localData.rejectAction = newData.rejectAction || 1
        localData.assignees = [...(newData.assignees || [])]
      }
    },
    { immediate: true, deep: true }
  )

  // 监听流程数据变化
  watch(
    () => props.flowData,
    (newData) => {
      if (newData) {
        localFlowData.flowName = newData.flowName || ''
        localFlowData.flowCode = newData.flowCode || ''
        localFlowData.businessType = newData.businessType || ''
        localFlowData.status = 1 // 默认启用
        localFlowData.description = newData.description || ''
      }
    },
    { immediate: true, deep: true }
  )

  // 拖拽处理
  const handleDragStart = (e: MouseEvent) => {
    // 如果点击的是按钮，不启动拖拽
    const target = e.target as HTMLElement
    if (target.closest('button') || target.closest('.card-actions')) {
      return
    }

    if (cardRef.value) {
      isDragging.value = true
      dragStartPos.value = { x: e.clientX, y: e.clientY }
      // 阻止默认行为
      e.preventDefault()
      e.stopPropagation()
    }
  }

  const handleDragMove = (e: MouseEvent) => {
    if (!isDragging.value) return

    // 计算拖拽偏移量
    const deltaX = e.clientX - dragStartPos.value.x
    const deltaY = e.clientY - dragStartPos.value.y

    dragOffset.value = { x: deltaX, y: deltaY }
  }

  const handleDragEnd = () => {
    if (!isDragging.value) return

    // 计算新的绝对位置并发送给父组件
    const { x, y } = props.position
    const newPosition = {
      x: x + dragOffset.value.x,
      y: y + dragOffset.value.y
    }

    // 重置拖拽偏移（位置已保存到父组件）
    dragOffset.value = { x: 0, y: 0 }
    isDragging.value = false

    // 通知父组件更新位置
    emit('update:position', newPosition)
  }

  // 点击外部关闭
  const handleClickOutside = (e: MouseEvent) => {
    // 如果正在拖拽，不关闭
    if (isDragging.value) return

    if (cardRef.value && !cardRef.value.contains(e.target as Node)) {
      // 检查是否点击了选择弹窗（不关闭）
      const target = e.target as HTMLElement
      if (target.closest('.el-dialog') || target.closest('.el-select-dropdown')) {
        return
      }
      handleClose()
    }
  }

  onMounted(() => {
    document.addEventListener('mousedown', handleClickOutside)
    document.addEventListener('mousemove', handleDragMove)
    document.addEventListener('mouseup', handleDragEnd)
  })

  onUnmounted(() => {
    document.removeEventListener('mousedown', handleClickOutside)
    document.removeEventListener('mousemove', handleDragMove)
    document.removeEventListener('mouseup', handleDragEnd)
  })

  const handleUpdate = () => {
    emit('update', { ...localData })
  }

  const handleFlowUpdate = () => {
    emit('update-flow', {
      flowName: localFlowData.flowName,
      flowCode: localFlowData.flowCode,
      businessType: localFlowData.businessType,
      status: 1, // 默认启用
      description: localFlowData.description
    })
  }

  const handleClose = () => {
    emit('update:visible', false)
  }

  const handleConfirm = () => {
    if (props.nodeType === 'start') {
      handleFlowUpdate()
    } else {
      handleUpdate()
    }
    handleClose()
  }

  const handleRemoveAssignee = (index: number) => {
    localData.assignees.splice(index, 1)
    handleUpdate()
  }

  const handleSelectApprover = () => {
    emit('select-approver')
  }

  // 暴露方法供父组件调用
  defineExpose({
    updateAssignees: (assignees: ApprovalAssignee[]) => {
      localData.assignees = [...assignees]
      handleUpdate()
    },
    addAssignees: (newAssignees: ApprovalAssignee[]) => {
      // 合并审批人，避免重复
      newAssignees.forEach((newAssignee) => {
        const exists = localData.assignees.some(
          (a) =>
            a.assigneeType === newAssignee.assigneeType && a.assigneeId === newAssignee.assigneeId
        )
        if (!exists) {
          localData.assignees.push(newAssignee)
        }
      })
      handleUpdate()
    }
  })
</script>

<style scoped lang="scss">
  .node-editor-card {
    position: absolute;
    z-index: 100;
    width: 280px;
    overflow: hidden;
    background: var(--el-bg-color);
    border-radius: 12px;
    box-shadow:
      0 8px 24px rgb(0 0 0 / 12%),
      0 2px 8px rgb(0 0 0 / 8%);

    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 12px 16px;
      background: linear-gradient(
        135deg,
        var(--el-fill-color-lighter) 0%,
        color-mix(in srgb, var(--el-color-primary) 6%, var(--el-bg-color)) 100%
      );
      border-bottom: 1px solid var(--el-border-color-lighter);

      &.draggable-header {
        cursor: var(--cursor-grab);
        user-select: none;
      }

      &.start-header {
        background: linear-gradient(
          135deg,
          var(--el-color-success-light-9) 0%,
          var(--el-color-success-light-8) 100%
        );
      }

      .card-title {
        font-size: 14px;
        font-weight: 600;
        color: var(--el-text-color-primary);
      }

      .card-actions {
        display: flex;
        gap: 4px;

        .close-btn,
        .confirm-btn {
          padding: 5px 6px !important;
        }

        .close-btn {
          color: var(--el-text-color-secondary);
          opacity: 0.8;

          &:hover {
            color: var(--el-text-color-primary);
            opacity: 1;
          }
        }

        .confirm-btn {
          color: var(--el-color-primary);
          opacity: 0.8;

          &:hover {
            color: var(--el-color-primary);
            opacity: 1;
          }
        }
      }
    }

    .card-body {
      max-height: 400px;
      padding: 16px;
      overflow-y: auto;

      .form-item {
        margin-bottom: 16px;

        &:last-child {
          margin-bottom: 0;
        }

        .form-label {
          display: block;
          margin-bottom: 6px;
          font-size: 12px;
          font-weight: 500;
          color: var(--el-text-color-secondary);

          &.required::before {
            margin-right: 4px;
            color: var(--el-color-danger);
            content: '*';
          }
        }
      }

      .assignee-list {
        display: flex;
        flex-wrap: wrap;
        gap: 6px;
        min-height: 28px;
        padding: 8px;
        margin-bottom: 8px;
        background: var(--el-fill-color-lighter);
        border-radius: 8px;

        .empty-assignee {
          width: 100%;
          padding: 4px 0;
          text-align: center;

          span {
            font-size: 12px;
            color: #9ca3af;
          }
        }
      }

      .assignee-actions {
        display: flex;
        gap: 8px;
      }
    }

    .card-arrow {
      position: absolute;
      top: 50%;
      left: -8px;
      width: 0;
      height: 0;
      filter: drop-shadow(-2px 0 2px rgb(0 0 0 / 6%));
      border-top: 8px solid transparent;
      border-right: 8px solid #fff;
      border-bottom: 8px solid transparent;
      transform: translateY(-50%);
    }
  }

  // 进入/离开动画
  .slide-fade-enter-active {
    transition: all 0.2s ease-out;
  }

  .slide-fade-leave-active {
    transition: all 0.15s ease-in;
  }

  .slide-fade-enter-from {
    opacity: 0;
    transform: translateX(-10px);
  }

  .slide-fade-leave-to {
    opacity: 0;
    transform: translateX(-10px);
  }

  // 暗色主题已通过 CSS 变量自动适配，无需额外样式
</style>
