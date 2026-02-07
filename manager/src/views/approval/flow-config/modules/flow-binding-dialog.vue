<!-- 流程绑定弹窗 -->
<template>
  <ElDialog
    v-model="dialogVisible"
    title="流程绑定管理"
    width="620px"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <div v-loading="loading" class="flex flex-col gap-3 min-h-32">
      <div
        v-for="item in bindingList"
        :key="item.businessType"
        class="relative rounded-xl px-5 py-4 border overflow-hidden transition-all duration-300 hover:shadow-sm hover:-translate-y-px"
        :class="[
          item.flowId
            ? 'bg-white border-gray-200 hover:border-gray-300'
            : 'bg-gray-50 border-gray-100 hover:border-gray-200'
        ]"
      >
        <!-- 上方：业务类型 + 操作 -->
        <div class="flex items-center justify-between mb-2.5">
          <span class="text-sm font-semibold text-gray-800">
            {{ item.businessTypeText }}
          </span>
          <div class="flex items-center gap-1.5">
            <span
              class="inline-flex items-center text-xs px-3 py-1 rounded-full cursor-pointer font-medium select-none text-blue-500 bg-blue-50 transition-all duration-200 hover:text-white hover:bg-blue-500 hover:shadow-sm"
              @click="openBindDialog(item)"
            >
              {{ item.flowId ? '更换' : '绑定' }}
            </span>
            <span
              v-if="item.flowId"
              class="inline-flex items-center text-xs px-3 py-1 rounded-full cursor-pointer font-medium select-none text-red-400 bg-red-50 transition-all duration-200 hover:text-white hover:bg-red-400 hover:shadow-sm"
              @click="handleUnbind(item)"
            >
              解绑
            </span>
          </div>
        </div>

        <!-- 下方：流程信息 -->
        <div class="flex items-center gap-3">
          <template v-if="item.flowName">
            <ArtStatusDot :text="item.flowName" type="success" />
            <ArtSwitch
              class="shrink-0"
              :modelValue="item.status === 1"
              :loading="item.statusLoading"
              inline-prompt
              @change="
                (val: boolean | string | number) =>
                  handleStatusChange(item, val === true || val === 1 ? 1 : 0)
              "
            />
          </template>
          <template v-else>
            <ArtStatusDot text="暂未绑定流程" type="info" />
          </template>
        </div>
      </div>

      <!-- 空状态 -->
      <div
        v-if="!loading && bindingList.length === 0"
        class="flex items-center justify-center py-12 text-gray-300 text-sm"
      >
        暂无业务类型配置
      </div>
    </div>

    <template #footer>
      <ElButton @click="dialogVisible = false" v-ripple>关闭</ElButton>
    </template>

    <!-- 绑定选择弹窗 -->
    <ElDialog v-model="bindDialogVisible" title="选择流程" width="440px" append-to-body>
      <div class="flex flex-col gap-5">
        <div class="flex flex-col gap-2">
          <label class="text-xs text-gray-500 font-medium">业务类型</label>
          <div class="text-sm text-gray-800 px-3 py-2 bg-gray-50 rounded-lg border border-gray-200">
            {{ bindForm.businessTypeText }}
          </div>
        </div>
        <div class="flex flex-col gap-2">
          <label class="text-xs text-gray-500 font-medium">
            选择流程
            <span class="text-red-400 ml-0.5">*</span>
          </label>
          <ElSelect
            v-model="bindForm.flowId"
            placeholder="请选择流程"
            style="width: 100%"
            filterable
          >
            <ElOption
              v-for="flow in flowOptions"
              :key="flow.id"
              :label="flow.flowName"
              :value="flow.id!"
            >
              <span>{{ flow.flowName }}</span>
              <span class="text-gray-400 ml-2 text-xs">({{ flow.flowCode }})</span>
            </ElOption>
          </ElSelect>
        </div>
      </div>
      <template #footer>
        <ElButton @click="bindDialogVisible = false" v-ripple>取消</ElButton>
        <ElButton type="primary" :loading="bindLoading" @click="handleBind" v-ripple>
          确定
        </ElButton>
      </template>
    </ElDialog>
  </ElDialog>
</template>

<script setup lang="ts">
  import {
    fetchGetAllBindings,
    fetchGetAllFlows,
    fetchBindFlow,
    fetchUnbindFlow,
    type ApprovalFlowBinding,
    type ApprovalFlow
  } from '@/api/approval-manage'
  import { useBusinessType } from '@/hooks'
  import ArtSwitch from '@/components/core/forms/art-switch/index.vue'

  interface BindingItem extends Partial<ApprovalFlowBinding> {
    businessType: string
    businessTypeText: string
    statusLoading?: boolean
  }

  const props = defineProps<{
    modelValue: boolean
  }>()

  const emit = defineEmits<{
    'update:modelValue': [value: boolean]
    success: []
  }>()

  const dialogVisible = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
  })

  const loading = ref(false)
  const bindingList = ref<BindingItem[]>([])
  const flowOptions = ref<ApprovalFlow[]>([])
  const bindDialogVisible = ref(false)
  const bindLoading = ref(false)
  const bindForm = reactive({
    businessType: '',
    businessTypeText: '',
    flowId: null as number | null
  })

  // 业务类型（从字典获取）
  const { businessTypeOptions, fetchBusinessTypes } = useBusinessType()

  // 监听弹窗打开
  watch(
    () => props.modelValue,
    async (val) => {
      if (val) {
        // 先获取业务类型，再加载数据
        await fetchBusinessTypes()
        await loadData()
      }
    }
  )

  const loadData = async () => {
    loading.value = true
    try {
      const [bindings, flows] = await Promise.all([fetchGetAllBindings(), fetchGetAllFlows()])

      flowOptions.value = flows

      // 构建绑定列表（确保所有业务类型都显示）
      bindingList.value = businessTypeOptions.value.map((bt) => {
        const binding = bindings.find((b) => b.businessType === bt.value)
        return {
          businessType: bt.value,
          businessTypeText: bt.label,
          statusLoading: false,
          ...binding
        }
      })
    } catch (error) {
      console.error('加载数据失败:', error)
    } finally {
      loading.value = false
    }
  }

  const openBindDialog = async (row: BindingItem) => {
    bindForm.businessType = row.businessType
    bindForm.businessTypeText = row.businessTypeText
    bindForm.flowId = row.flowId || null

    // 加载该业务类型对应的流程
    try {
      const flows = await fetchGetAllFlows(row.businessType)
      flowOptions.value = flows
    } catch (error) {
      console.error('加载流程失败:', error)
    }

    bindDialogVisible.value = true
  }

  const handleBind = async () => {
    if (!bindForm.flowId) {
      ElMessage.warning('请选择流程')
      return
    }

    bindLoading.value = true
    try {
      await fetchBindFlow({
        businessType: bindForm.businessType,
        flowId: bindForm.flowId,
        status: 1
      })
      bindDialogVisible.value = false
      await loadData()
      emit('success')
    } catch (error) {
      console.error('绑定失败:', error)
    } finally {
      bindLoading.value = false
    }
  }

  const handleStatusChange = async (row: BindingItem, status: number) => {
    if (!row.flowId) {
      return
    }

    const oldStatus = row.status ?? 0
    row.status = status
    row.statusLoading = true

    try {
      await fetchBindFlow({
        businessType: row.businessType,
        flowId: row.flowId,
        status
      })
      await loadData()
      emit('success')
    } catch (error) {
      console.error('更新状态失败:', error)
      // 恢复原状态
      row.status = oldStatus
      ElMessage.error('更新状态失败')
    } finally {
      row.statusLoading = false
    }
  }

  const handleUnbind = async (row: BindingItem) => {
    try {
      await ElMessageBox.confirm(`确定要解绑"${row.businessTypeText}"的流程吗？`, '解绑确认', {
        type: 'warning'
      })
      await fetchUnbindFlow(row.businessType)
      await loadData()
      emit('success')
    } catch (error) {
      if (error !== 'cancel') {
        console.error('解绑失败:', error)
      }
    }
  }
</script>
