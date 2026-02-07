<!-- 审批节点卡片 - 挂载到 LogicFlow HtmlNode 内部 -->
<!-- 使用原生 HTML 元素 + Tailwind，避免 SVG foreignObject 中的 teleport/overlay 问题 -->
<template>
  <div
    class="w-full h-full select-none"
    :style="containerStyle"
    @mouseenter="hovered = true"
    @mouseleave="hovered = false"
  >
    <!-- ==================== 折叠态 ==================== -->
    <div v-if="!isExpanded" class="flex items-center gap-3 w-full h-full px-4">
      <!-- 左侧：审批类型图标 -->
      <div
        class="w-10 h-10 rounded-[10px] flex items-center justify-center flex-shrink-0 text-lg"
        :style="iconBgStyle"
      >
        {{ nodeTypeConfig.icon }}
      </div>

      <!-- 中间：名称 + badge + 审批人数 -->
      <div class="flex-1 min-w-0 flex flex-col gap-1">
        <div
          class="text-sm font-semibold truncate"
          :style="{ color: 'var(--el-text-color-primary)' }"
        >
          {{ nodeName || '未命名节点' }}
        </div>
        <div class="flex items-center gap-2">
          <span class="text-[11px] px-2 py-[2px] rounded font-medium" :style="badgeStyle">
            {{ nodeTypeConfig.label }}
          </span>
          <span class="text-[12px]" :style="{ color: 'var(--el-text-color-secondary)' }">
            {{ assignees.length }}人
          </span>
        </div>
      </div>

      <!-- 右侧：展开箭头（仅此处可点击展开） -->
      <div
        class="flex-shrink-0 transition-transform duration-200 cursor-pointer rounded-md w-8 h-8 flex items-center justify-center hover:opacity-100 opacity-60"
        :style="{ color: 'var(--el-text-color-placeholder)' }"
        title="展开"
        @click.stop="handleToggleExpand"
      >
        <svg
          width="16"
          height="16"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
        >
          <polyline points="6 9 12 15 18 9"></polyline>
        </svg>
      </div>
    </div>

    <!-- ==================== 展开态 ==================== -->
    <div v-else class="flex flex-col w-full h-full">
      <!-- Header -->
      <div class="flex items-center justify-between px-4 py-2 border-b" :style="headerStyle">
        <!-- 左侧：类型图标 + 节点名 -->
        <div class="flex items-center gap-2 min-w-0 flex-1">
          <div
            class="w-6 h-6 rounded-md flex items-center justify-center flex-shrink-0 text-xs"
            :style="iconBgStyle"
          >
            {{ nodeTypeConfig.icon }}
          </div>
          <span
            class="text-sm font-semibold truncate"
            :style="{ color: 'var(--el-text-color-primary)' }"
          >
            {{ localName || '未命名节点' }}
          </span>
        </div>
        <div class="flex items-center gap-0.5 flex-shrink-0">
          <!-- 删除按钮 -->
          <button
            class="w-6 h-6 rounded-md flex items-center justify-center transition-colors duration-200 hover:opacity-100 opacity-50"
            :style="deleteBtnStyle"
            title="删除节点"
            @click.stop="handleDelete"
          >
            <svg
              width="13"
              height="13"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <path
                d="M3 6h18M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"
              ></path>
            </svg>
          </button>
          <!-- 折叠按钮 - 简约横线图标 -->
          <button
            class="w-6 h-6 rounded-md flex items-center justify-center transition-colors duration-200 hover:opacity-100 opacity-50"
            :style="{ color: 'var(--el-text-color-secondary)' }"
            title="折叠"
            @click.stop="handleToggleExpand"
          >
            <svg
              width="14"
              height="14"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2.5"
              stroke-linecap="round"
            >
              <line x1="6" y1="12" x2="18" y2="12"></line>
            </svg>
          </button>
        </div>
      </div>

      <!-- 表单区域 -->
      <div class="flex-1 px-4 py-3 flex flex-col gap-3">
        <!-- 节点名称 -->
        <div class="flex flex-col gap-1.5">
          <label class="text-xs font-medium" :style="{ color: 'var(--el-text-color-secondary)' }">
            节点名称
          </label>
          <input
            type="text"
            class="w-full h-8 px-3 text-sm rounded-lg border outline-none transition-colors duration-200"
            :style="inputStyle"
            :value="localName"
            placeholder="请输入节点名称"
            @input="handleNameInput"
            @focus="inputFocused = true"
            @blur="inputFocused = false"
          />
        </div>

        <!-- 审批类型 chip 选择 -->
        <div class="flex flex-col gap-1.5">
          <label class="text-xs font-medium" :style="{ color: 'var(--el-text-color-secondary)' }">
            审批类型
          </label>
          <div class="flex gap-2">
            <button
              v-for="opt in nodeTypeOptions"
              :key="opt.value"
              class="flex-1 h-8 text-xs font-medium rounded-lg border-2 transition-all duration-200 cursor-pointer"
              :style="chipStyle(opt.value, localNodeType, opt.color)"
              @click.stop="handleNodeTypeChange(opt.value)"
            >
              {{ opt.label }}
            </button>
          </div>
        </div>

        <!-- 驳回处理 chip 选择 -->
        <div class="flex flex-col gap-1.5">
          <label class="text-xs font-medium" :style="{ color: 'var(--el-text-color-secondary)' }">
            驳回处理
          </label>
          <div class="flex gap-2">
            <button
              v-for="opt in rejectOptions"
              :key="opt.value"
              class="flex-1 h-8 text-xs font-medium rounded-lg border-2 transition-all duration-200 cursor-pointer"
              :style="chipStyle(opt.value, localRejectAction, 'var(--el-color-primary)')"
              @click.stop="handleRejectActionChange(opt.value)"
            >
              {{ opt.label }}
            </button>
          </div>
        </div>

        <!-- 审批人 -->
        <div class="flex flex-col gap-1.5">
          <label class="text-xs font-medium" :style="{ color: 'var(--el-text-color-secondary)' }">
            审批人
          </label>
          <div
            class="flex flex-wrap gap-1.5 min-h-[32px] p-2 rounded-lg"
            :style="{ background: 'var(--el-fill-color-lighter)' }"
          >
            <template v-if="localAssignees.length > 0">
              <span
                v-for="(assignee, idx) in localAssignees"
                :key="assignee.assigneeId || idx"
                class="inline-flex items-center gap-1 h-6 px-2 text-xs rounded-md"
                :style="tagStyle"
              >
                {{ assignee.assigneeName || '未知' }}
                <button
                  class="w-4 h-4 rounded-full flex items-center justify-center hover:opacity-100 opacity-60 transition-opacity duration-150"
                  :style="{ color: 'var(--el-text-color-secondary)' }"
                  @click.stop="handleRemoveAssignee(idx)"
                >
                  <svg
                    width="10"
                    height="10"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="3"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  >
                    <line x1="18" y1="6" x2="6" y2="18"></line>
                    <line x1="6" y1="6" x2="18" y2="18"></line>
                  </svg>
                </button>
              </span>
            </template>
            <span
              v-else
              class="text-xs py-1 w-full text-center"
              :style="{ color: 'var(--el-text-color-placeholder)' }"
            >
              暂无审批人
            </span>
          </div>
          <button
            class="w-full h-8 text-xs font-medium rounded-lg border border-dashed transition-colors duration-200 cursor-pointer flex items-center justify-center gap-1"
            :style="addBtnStyle"
            @click.stop="handleSelectApprover"
          >
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
              <path d="M16 21v-2a4 4 0 00-4-4H5a4 4 0 00-4-4v2"></path>
              <circle cx="8.5" cy="7" r="4"></circle>
              <line x1="20" y1="8" x2="20" y2="14"></line>
              <line x1="23" y1="11" x2="17" y2="11"></line>
            </svg>
            选择审批人
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, computed, watch } from 'vue'

  interface Assignee {
    assigneeType: number
    assigneeId: number
    assigneeName?: string
  }

  const props = defineProps<{
    nodeName: string
    nodeType: number
    rejectAction: number
    assignees: Assignee[]
    isExpanded: boolean
    isSelected: boolean
    nodeOrder: number
    nodeId: string
  }>()

  // 本地可编辑状态
  const localName = ref(props.nodeName)
  const localNodeType = ref(props.nodeType)
  const localRejectAction = ref(props.rejectAction)
  const localAssignees = ref<Assignee[]>([...props.assignees])

  const hovered = ref(false)
  const inputFocused = ref(false)

  // debounce timer for name input
  let nameDebounceTimer: ReturnType<typeof setTimeout> | null = null

  // 同步 props 到本地状态
  watch(
    () => props.nodeName,
    (val) => {
      localName.value = val
    }
  )
  watch(
    () => props.nodeType,
    (val) => {
      localNodeType.value = val
    }
  )
  watch(
    () => props.rejectAction,
    (val) => {
      localRejectAction.value = val
    }
  )
  watch(
    () => props.assignees,
    (val) => {
      localAssignees.value = [...val]
    },
    { deep: true }
  )

  // 节点类型配置
  const nodeTypeOptions = [
    { value: 1, label: '串行', color: 'var(--el-color-primary)' },
    { value: 2, label: '会签', color: '#f97316' },
    { value: 3, label: '或签', color: '#22c55e' }
  ]

  const rejectOptions = [
    { value: 1, label: '直接结束' },
    { value: 2, label: '退回申请人' },
    { value: 3, label: '退回上一节点' }
  ]

  const nodeTypeConfig = computed(() => {
    const configs: Record<number, { label: string; color: string; icon: string }> = {
      1: { label: '串行', color: 'var(--el-color-primary)', icon: '\u{1F539}' },
      2: { label: '会签', color: '#f97316', icon: '\u{1F537}' },
      3: { label: '或签', color: '#22c55e', icon: '\u{1F538}' }
    }
    return configs[props.nodeType] || configs[1]
  })

  // ==================== 样式计算 ====================

  const containerStyle = computed(() => {
    const baseColor = nodeTypeConfig.value.color
    const selected = props.isSelected
    const expanded = props.isExpanded
    const hover = hovered.value

    return {
      background: 'var(--default-box-color)',
      border: `2px solid ${selected ? baseColor : hover && !expanded ? baseColor : 'var(--default-border)'}`,
      borderRadius: '12px',
      boxShadow: selected
        ? `0 4px 16px color-mix(in srgb, ${baseColor} 30%, transparent)`
        : hover && !expanded
          ? `0 4px 12px color-mix(in srgb, ${baseColor} 20%, transparent)`
          : '0 2px 8px rgba(0,0,0,0.06)',
      transition: 'all 0.2s ease',
      overflow: 'hidden'
    }
  })

  const iconBgStyle = computed(() => ({
    background: `color-mix(in srgb, ${nodeTypeConfig.value.color} 12%, transparent)`
  }))

  const badgeStyle = computed(() => ({
    background: `color-mix(in srgb, ${nodeTypeConfig.value.color} 12%, transparent)`,
    color: nodeTypeConfig.value.color
  }))

  const headerStyle = computed(() => ({
    borderColor: 'var(--default-border)',
    background: 'var(--el-fill-color-lighter)'
  }))

  const deleteBtnStyle = computed(() => ({
    color: 'var(--el-color-danger)',
    background: 'transparent'
  }))

  const inputStyle = computed(() => ({
    background: 'var(--default-box-color)',
    border: `1px solid ${inputFocused.value ? 'var(--el-color-primary)' : 'var(--default-border)'}`,
    color: 'var(--el-text-color-primary)',
    '::placeholder': { color: 'var(--el-text-color-placeholder)' }
  }))

  const tagStyle = computed(() => ({
    background: `color-mix(in srgb, var(--el-color-success) 12%, transparent)`,
    color: 'var(--el-color-success)'
  }))

  const addBtnStyle = computed(() => ({
    color: 'var(--el-color-primary)',
    borderColor: 'var(--el-color-primary)',
    background: `color-mix(in srgb, var(--el-color-primary) 4%, transparent)`
  }))

  // chip 选中/未选中样式
  const chipStyle = (value: number, current: number, activeColor: string) => {
    const isActive = value === current
    return {
      background: isActive
        ? `color-mix(in srgb, ${activeColor} 12%, transparent)`
        : 'var(--default-box-color)',
      borderColor: isActive ? activeColor : 'var(--default-border)',
      color: isActive ? activeColor : 'var(--el-text-color-secondary)'
    }
  }

  // ==================== 事件处理 ====================

  const dispatchEvent = (name: string, detail: Record<string, unknown>) => {
    document.dispatchEvent(
      new CustomEvent(name, {
        detail,
        bubbles: true,
        cancelable: true
      })
    )
  }

  const dispatchUpdate = () => {
    dispatchEvent('lf-node-update', {
      nodeId: props.nodeId,
      data: {
        nodeName: localName.value,
        nodeType: localNodeType.value,
        rejectAction: localRejectAction.value,
        assignees: [...localAssignees.value]
      }
    })
  }

  const handleToggleExpand = () => {
    dispatchEvent('lf-node-toggle-expand', { nodeId: props.nodeId })
  }

  const handleNameInput = (e: Event) => {
    const value = (e.target as HTMLInputElement).value
    localName.value = value
    if (nameDebounceTimer) clearTimeout(nameDebounceTimer)
    nameDebounceTimer = setTimeout(() => {
      dispatchUpdate()
    }, 300)
  }

  const handleNodeTypeChange = (value: number) => {
    localNodeType.value = value
    dispatchUpdate()
  }

  const handleRejectActionChange = (value: number) => {
    localRejectAction.value = value
    dispatchUpdate()
  }

  const handleRemoveAssignee = (index: number) => {
    localAssignees.value.splice(index, 1)
    dispatchUpdate()
  }

  const handleSelectApprover = () => {
    dispatchEvent('lf-node-select-approver', { nodeId: props.nodeId })
  }

  const handleDelete = () => {
    dispatchEvent('logicflow-node-delete', {
      nodeId: props.nodeId,
      properties: {
        nodeName: localName.value,
        nodeOrder: props.nodeOrder
      }
    })
  }
</script>
