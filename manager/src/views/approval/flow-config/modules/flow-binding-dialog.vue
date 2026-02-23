<!-- 流程绑定弹窗 -->
<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="460px"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <div v-loading="loading" class="flex flex-col gap-4">
      <!-- 流程信息 -->
      <div class="flex items-start gap-3 px-3 py-2.5 rounded-lg bg-[var(--el-fill-color-light)]">
        <div class="flex-1 min-w-0 flex flex-col gap-1">
          <span class="text-sm font-medium text-[var(--el-text-color-primary)] truncate">{{
            flow?.flowName
          }}</span>
          <span class="text-xs text-[var(--el-text-color-placeholder)] font-mono">{{
            flow?.flowCode
          }}</span>
        </div>
        <ElTag type="primary" size="small" class="shrink-0 mt-0.5">{{ businessTypeText }}</ElTag>
      </div>

      <!-- 分割线 + 当前绑定 -->
      <div class="flex flex-col gap-1.5">
        <span class="text-xs text-[var(--el-text-color-secondary)]">当前绑定</span>
        <div
          v-if="currentBinding"
          class="flex items-center justify-between px-3 py-2.5 rounded-lg border border-[var(--el-border-color-light)]"
        >
          <ArtStatusDot :text="currentBinding.flowName || '已绑定'" type="success" />
          <div class="flex items-center gap-2.5">
            <ArtSwitch
              :modelValue="currentBinding.status === 1"
              :loading="statusLoading"
              inline-prompt
              @change="
                (val: boolean | string | number) =>
                  handleStatusChange(val === true || val === 1 ? 1 : 0)
              "
            />
            <span
              class="text-xs text-[var(--el-color-danger)] cursor-pointer hover:opacity-70 transition-opacity"
              @click="handleUnbind"
              >解绑</span
            >
          </div>
        </div>
        <div v-else class="px-3 py-2.5 rounded-lg border border-[var(--el-border-color-light)]">
          <ArtStatusDot text="暂未绑定" type="info" />
        </div>
      </div>

      <!-- 选择流程 -->
      <div class="flex flex-col gap-1.5">
        <span class="text-xs text-[var(--el-text-color-secondary)]">
          {{ currentBinding ? '更换流程' : '选择流程'
          }}<span class="text-[var(--el-color-danger)] ml-0.5">*</span>
        </span>
        <ElSelect
          v-model="selectedFlowId"
          placeholder="请选择流程"
          style="width: 100%"
          filterable
          clearable
        >
          <ElOption
            v-for="item in flowOptions"
            :key="item.id"
            :label="item.flowName"
            :value="item.id!"
          >
            <span>{{ item.flowName }}</span>
            <span class="text-[var(--el-text-color-placeholder)] ml-2 text-xs"
              >({{ item.flowCode }})</span
            >
          </ElOption>
        </ElSelect>
      </div>
    </div>

    <template #footer>
      <ElButton @click="dialogVisible = false" v-ripple>取消</ElButton>
      <ElButton
        type="primary"
        :loading="bindLoading"
        :disabled="!selectedFlowId"
        @click="handleBind"
        v-ripple
      >
        {{ currentBinding ? '更换绑定' : '确认绑定' }}
      </ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import {
    fetchGetBinding,
    fetchGetAllFlows,
    fetchBindFlow,
    fetchUnbindFlow,
    type ApprovalFlowBinding,
    type ApprovalFlow
  } from '@/api/approval-manage'
  import { useBusinessType } from '@/hooks'
  import ArtSwitch from '@/components/core/forms/art-switch/index.vue'
  import ArtStatusDot from '@/components/core/tables/art-status-dot/index.vue'

  interface Props {
    modelValue: boolean
    flow: ApprovalFlow | null
  }

  interface Emits {
    'update:modelValue': [value: boolean]
    success: []
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const dialogVisible = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
  })

  const dialogTitle = computed(() => {
    const name = props.flow?.flowName || '流程'
    return `绑定管理 · ${name}`
  })

  const loading = ref(false)
  const bindLoading = ref(false)
  const statusLoading = ref(false)
  const currentBinding = ref<ApprovalFlowBinding | null>(null)
  const flowOptions = ref<ApprovalFlow[]>([])
  const selectedFlowId = ref<number | null>(null)

  const { businessTypeOptions, fetchBusinessTypes } = useBusinessType()

  const businessTypeText = computed(() => {
    const bt = props.flow?.businessType
    return businessTypeOptions.value.find((o) => o.value === bt)?.label || bt || ''
  })

  watch(
    () => props.modelValue,
    async (val) => {
      if (val && props.flow) {
        await fetchBusinessTypes()
        await loadData()
      }
    }
  )

  const loadData = async () => {
    if (!props.flow?.businessType) return
    loading.value = true
    selectedFlowId.value = null
    try {
      const [binding, flows] = await Promise.all([
        fetchGetBinding(props.flow.businessType).catch(() => null),
        fetchGetAllFlows(props.flow.businessType)
      ])
      currentBinding.value = binding
      flowOptions.value = flows
      selectedFlowId.value = binding?.flowId ?? null
    } finally {
      loading.value = false
    }
  }

  const handleBind = async () => {
    if (!selectedFlowId.value || !props.flow?.businessType) return
    bindLoading.value = true
    try {
      await fetchBindFlow({
        businessType: props.flow.businessType,
        flowId: selectedFlowId.value,
        status: 1
      })
      selectedFlowId.value = null
      await loadData()
      emit('success')
    } finally {
      bindLoading.value = false
    }
  }

  const handleStatusChange = async (status: number) => {
    if (!currentBinding.value || !props.flow?.businessType) return
    statusLoading.value = true
    const old = currentBinding.value.status
    currentBinding.value.status = status
    try {
      await fetchBindFlow({
        businessType: props.flow.businessType,
        flowId: currentBinding.value.flowId,
        status
      })
      emit('success')
    } catch {
      currentBinding.value.status = old
    } finally {
      statusLoading.value = false
    }
  }

  const handleUnbind = async () => {
    if (!props.flow?.businessType) return
    try {
      await ElMessageBox.confirm(`确定要解绑此流程的绑定关系吗？`, '解绑确认', { type: 'warning' })
      await fetchUnbindFlow(props.flow.businessType)
      await loadData()
      emit('success')
    } catch (error) {
      if (error !== 'cancel') console.error('解绑失败:', error)
    }
  }
</script>
