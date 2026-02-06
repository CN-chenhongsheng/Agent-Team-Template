<!-- 流程绑定弹窗 -->
<template>
  <ElDialog
    v-model="dialogVisible"
    title="流程绑定管理"
    width="700px"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <ElTable :data="bindingList" v-loading="loading">
      <ElTableColumn prop="businessTypeText" label="业务类型" />
      <ElTableColumn prop="flowName" label="绑定流程">
        <template #default="{ row }">
          <span v-if="row.flowName">{{ row.flowName }}</span>
          <span v-else class="text-gray-400">未绑定</span>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="statusText" label="状态">
        <template #default="{ row }">
          <ArtSwitch
            v-if="row.flowId"
            :modelValue="row.status === 1"
            :loading="row.statusLoading"
            inline-prompt
            @change="
              (val: boolean | string | number) =>
                handleStatusChange(row, val === true || val === 1 ? 1 : 0)
            "
          />
        </template>
      </ElTableColumn>
      <ElTableColumn label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <ElButton size="small" link type="primary" @click="openBindDialog(row)">
            {{ row.flowId ? '更换' : '绑定' }}
          </ElButton>
          <ElButton v-if="row.flowId" size="small" link type="danger" @click="handleUnbind(row)">
            解绑
          </ElButton>
        </template>
      </ElTableColumn>
    </ElTable>

    <template #footer>
      <ElButton @click="dialogVisible = false" v-ripple>关闭</ElButton>
    </template>

    <!-- 绑定选择弹窗 -->
    <ElDialog v-model="bindDialogVisible" title="选择流程" width="500px" append-to-body>
      <ElForm :model="bindForm" label-width="80px">
        <ElFormItem label="业务类型">
          <ElInput v-model="bindForm.businessTypeText" disabled />
        </ElFormItem>
        <ElFormItem label="选择流程" required>
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
              <span class="text-gray-400 ml-2">({{ flow.flowCode }})</span>
            </ElOption>
          </ElSelect>
        </ElFormItem>
      </ElForm>
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
  import { ElMessageBox, ElMessage } from 'element-plus'
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
