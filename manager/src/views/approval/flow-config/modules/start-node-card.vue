<!-- 开始节点卡片 - 挂载到 LogicFlow HtmlNode 内部 -->
<!-- 使用原生 HTML 元素 + Tailwind，与审批节点卡片统一风格 -->
<template>
  <div
    class="w-full h-full select-none"
    :style="containerStyle"
    @mouseenter="hovered = true"
    @mouseleave="hovered = false"
  >
    <!-- ==================== 折叠态 ==================== -->
    <div v-if="!isExpanded" class="flex items-center gap-3 w-full h-full px-4">
      <!-- 左侧：绿色播放图标 -->
      <div
        class="w-10 h-10 rounded-[10px] flex items-center justify-center flex-shrink-0"
        :style="iconBgStyle"
      >
        <svg
          width="20"
          height="20"
          viewBox="0 0 24 24"
          fill="currentColor"
          :style="{ color: '#fff' }"
        >
          <path d="M8 5v14l11-7z" />
        </svg>
      </div>

      <!-- 中间：流程名称 + 业务类型 -->
      <div class="flex-1 min-w-0 flex flex-col gap-1">
        <div class="text-sm font-semibold truncate" :style="{ color: 'var(--el-color-success)' }">
          {{ flowName || '开始' }}
        </div>
        <div class="text-xs truncate" :style="{ color: 'var(--el-text-color-secondary)' }">
          {{ businessTypeLabel || '点击展开配置流程信息' }}
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
        <div class="flex items-center gap-2 min-w-0 flex-1">
          <div
            class="w-6 h-6 rounded-md flex items-center justify-center flex-shrink-0"
            :style="{ background: successColor }"
          >
            <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor" style="color: #fff">
              <path d="M8 5v14l11-7z" />
            </svg>
          </div>
          <span
            class="text-sm font-semibold truncate"
            :style="{ color: 'var(--el-color-success)' }"
          >
            {{ localFlowName || '流程配置' }}
          </span>
        </div>
        <!-- 折叠按钮 - 简约横线图标 -->
        <button
          class="w-6 h-6 rounded-md flex items-center justify-center transition-colors duration-200 hover:opacity-100 opacity-50 flex-shrink-0"
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

      <!-- 表单区域 -->
      <div class="flex-1 px-4 py-3 flex flex-col gap-3">
        <!-- 流程名称 -->
        <div class="flex flex-col gap-1.5">
          <label class="text-xs font-medium" :style="labelStyle">
            <span :style="{ color: 'var(--el-color-danger)' }">*</span>
            流程名称
          </label>
          <input
            type="text"
            class="w-full h-8 px-3 text-sm rounded-lg border outline-none transition-colors duration-200"
            :style="inputStyle"
            :value="localFlowName"
            placeholder="请输入流程名称"
            @input="handleFlowNameInput"
            @focus="focusedField = 'flowName'"
            @blur="focusedField = ''"
          />
        </div>

        <!-- 流程编码 -->
        <div class="flex flex-col gap-1.5">
          <label class="text-xs font-medium" :style="labelStyle">
            <span :style="{ color: 'var(--el-color-danger)' }">*</span>
            流程编码
          </label>
          <input
            type="text"
            class="w-full h-8 px-3 text-sm rounded-lg border outline-none transition-colors duration-200"
            :style="inputStyle"
            :class="{ 'opacity-60 cursor-not-allowed': dialogType === 'edit' }"
            :value="localFlowCode"
            placeholder="请输入流程编码（唯一）"
            :disabled="dialogType === 'edit'"
            @input="handleFlowCodeInput"
            @focus="focusedField = 'flowCode'"
            @blur="focusedField = ''"
          />
        </div>

        <!-- 业务类型 -->
        <div class="flex flex-col gap-1.5">
          <label class="text-xs font-medium" :style="labelStyle">
            <span :style="{ color: 'var(--el-color-danger)' }">*</span>
            业务类型
          </label>
          <select
            class="w-full h-8 px-3 text-sm rounded-lg border outline-none transition-colors duration-200 appearance-none"
            :style="selectStyle"
            :class="{ 'opacity-60 cursor-not-allowed': dialogType === 'edit' }"
            :value="localBusinessType"
            :disabled="dialogType === 'edit'"
            @change="handleBusinessTypeChange"
            @focus="focusedField = 'businessType'"
            @blur="focusedField = ''"
          >
            <option value="" disabled>请选择业务类型</option>
            <option v-for="opt in businessTypeOptions" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </option>
          </select>
        </div>

        <!-- 备注 -->
        <div class="flex flex-col gap-1.5">
          <label class="text-xs font-medium" :style="labelStyle"> 备注 </label>
          <textarea
            class="w-full px-3 py-2 text-sm rounded-lg border outline-none transition-colors duration-200 resize-none"
            :style="inputStyle"
            rows="3"
            :value="localDescription"
            placeholder="请输入备注信息"
            @input="handleDescriptionInput"
            @focus="focusedField = 'description'"
            @blur="focusedField = ''"
          ></textarea>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, computed, watch } from 'vue'

  interface BusinessTypeOption {
    label: string
    value: string
  }

  const props = defineProps<{
    flowName: string
    flowCode: string
    businessType: string
    description: string
    isExpanded: boolean
    isSelected: boolean
    nodeId: string
    dialogType: 'add' | 'edit'
    businessTypeOptions: BusinessTypeOption[]
  }>()

  // 本地可编辑状态
  const localFlowName = ref(props.flowName)
  const localFlowCode = ref(props.flowCode)
  const localBusinessType = ref(props.businessType)
  const localDescription = ref(props.description)

  const hovered = ref(false)
  const focusedField = ref('')

  // debounce timers
  let nameDebounceTimer: ReturnType<typeof setTimeout> | null = null
  let codeDebounceTimer: ReturnType<typeof setTimeout> | null = null
  let descDebounceTimer: ReturnType<typeof setTimeout> | null = null

  // 同步 props 到本地状态
  watch(
    () => props.flowName,
    (val) => {
      localFlowName.value = val
    }
  )
  watch(
    () => props.flowCode,
    (val) => {
      localFlowCode.value = val
    }
  )
  watch(
    () => props.businessType,
    (val) => {
      localBusinessType.value = val
    }
  )
  watch(
    () => props.description,
    (val) => {
      localDescription.value = val
    }
  )

  // 业务类型显示文本
  const businessTypeLabel = computed(() => {
    if (!props.businessType) return ''
    const opt = props.businessTypeOptions.find((o) => o.value === props.businessType)
    return opt?.label || props.businessType
  })

  // 成功色
  const successColor = 'var(--el-color-success)'

  // ==================== 样式计算 ====================

  const containerStyle = computed(() => {
    const selected = props.isSelected
    const expanded = props.isExpanded
    const hover = hovered.value

    return {
      background: 'var(--default-box-color)',
      border: `2px solid ${selected ? successColor : hover && !expanded ? successColor : 'var(--default-border)'}`,
      borderRadius: '12px',
      boxShadow: selected
        ? `0 4px 16px color-mix(in srgb, ${successColor} 30%, transparent)`
        : hover && !expanded
          ? `0 4px 12px color-mix(in srgb, ${successColor} 20%, transparent)`
          : '0 2px 8px rgba(0,0,0,0.06)',
      transition: 'all 0.2s ease',
      overflow: 'hidden'
    }
  })

  const iconBgStyle = computed(() => ({
    background: successColor
  }))

  const headerStyle = computed(() => ({
    borderColor: 'var(--default-border)',
    background: 'var(--el-fill-color-lighter)'
  }))

  const labelStyle = computed(() => ({
    color: 'var(--el-text-color-secondary)'
  }))

  const inputStyle = computed(() => ({
    background: 'var(--default-box-color)',
    border: `1px solid var(--default-border)`,
    color: 'var(--el-text-color-primary)'
  }))

  const selectStyle = computed(() => ({
    background: 'var(--default-box-color)',
    border: `1px solid var(--default-border)`,
    color: 'var(--el-text-color-primary)',
    backgroundImage: `url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%239ca3af' stroke-width='2'%3E%3Cpolyline points='6 9 12 15 18 9'%3E%3C/polyline%3E%3C/svg%3E")`,
    backgroundRepeat: 'no-repeat',
    backgroundPosition: 'right 10px center',
    paddingRight: '30px'
  }))

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

  const dispatchStartNodeUpdate = () => {
    dispatchEvent('lf-start-node-update', {
      nodeId: props.nodeId,
      data: {
        flowName: localFlowName.value,
        flowCode: localFlowCode.value,
        businessType: localBusinessType.value,
        description: localDescription.value
      }
    })
  }

  const handleToggleExpand = () => {
    dispatchEvent('lf-node-toggle-expand', { nodeId: props.nodeId })
  }

  const handleFlowNameInput = (e: Event) => {
    localFlowName.value = (e.target as HTMLInputElement).value
    if (nameDebounceTimer) clearTimeout(nameDebounceTimer)
    nameDebounceTimer = setTimeout(() => {
      dispatchStartNodeUpdate()
    }, 300)
  }

  const handleFlowCodeInput = (e: Event) => {
    localFlowCode.value = (e.target as HTMLInputElement).value
    if (codeDebounceTimer) clearTimeout(codeDebounceTimer)
    codeDebounceTimer = setTimeout(() => {
      dispatchStartNodeUpdate()
    }, 300)
  }

  const handleBusinessTypeChange = (e: Event) => {
    localBusinessType.value = (e.target as HTMLSelectElement).value
    dispatchStartNodeUpdate()
  }

  const handleDescriptionInput = (e: Event) => {
    localDescription.value = (e.target as HTMLTextAreaElement).value
    if (descDebounceTimer) clearTimeout(descDebounceTimer)
    descDebounceTimer = setTimeout(() => {
      dispatchStartNodeUpdate()
    }, 300)
  }
</script>
