<template>
  <div class="flow-editor-page w-full h-screen">
    <el-skeleton v-if="isLoading" :rows="10" animated />
    <FlowEditor
      v-else
      :flow-id="flowId"
      :flow-name="flowName"
      :flow-code="flowCode"
      :business-type="businessType"
      :description="description"
      :flow-data="flowData"
      :is-add="isAdd"
      @success="handleSuccess"
      @cancel="handleCancel"
      @close="handleClose"
    />
  </div>
</template>

<script setup lang="ts">
  import { ref, onMounted, onUnmounted } from 'vue'
  import { useRoute } from 'vue-router'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import FlowEditor from '@/components/core/flow-editor/index.vue'
  import { useFlowData } from '@/components/core/flow-editor/composables/useFlowData'
  import { useFlowCommunication } from '@/components/core/flow-editor/composables/useFlowCommunication'
  import { fetchGetFlowDetail, fetchAddFlow, fetchUpdateFlow } from '@/api/approval-manage'
  import type { ApprovalNode, ApprovalFlow } from '@/api/approval-manage'
  import type { FlowSaveData } from '@/components/core/flow-editor/types'
  import { useUserStore } from '@/store/modules/user'
  import { fetchRefreshToken } from '@/api/auth'

  // ========== 路由参数 ==========
  const route = useRoute()
  const flowId = ref<number | undefined>(
    route.params.flowId ? Number(route.params.flowId) : undefined
  )
  const isAdd = ref(!flowId.value)

  // ========== 数据状态 ==========
  const flowName = ref('')
  const flowCode = ref('')
  const businessType = ref('')
  const description = ref('')
  const flowData = ref<ApprovalNode[]>([])
  const isLoading = ref(true)

  // ========== Composables ==========
  const { loadCache, saveCache, clearCache } = useFlowData()
  const { sendMessage } = useFlowCommunication()

  // ========== 数据加载 ==========
  /**
   * 从缓存加载数据
   */
  const loadFromCache = (): boolean => {
    const cached = loadCache(flowId.value)
    if (cached) {
      flowName.value = cached.flowName || ''
      flowData.value = cached.flowData || []
      return true
    }
    return false
  }

  /**
   * 确保 accessToken 可用（新标签页中 token 不持久化，需要先刷新）
   */
  const ensureTokenReady = async (): Promise<void> => {
    const userStore = useUserStore()
    if (userStore.isLogin && !userStore.accessToken) {
      try {
        const newAccessToken = await fetchRefreshToken()
        userStore.setToken(newAccessToken)
      } catch {
        // 刷新失败，后续 API 调用会自行处理
      }
    }
  }

  /**
   * 从 API 加载数据（支持重试）
   */
  const loadFromApi = async (retryCount = 0) => {
    if (!flowId.value) {
      // 新增模式，无需加载
      isLoading.value = false
      return
    }

    try {
      const data = await fetchGetFlowDetail(flowId.value)
      flowName.value = data.flowName || ''
      flowCode.value = data.flowCode || ''
      businessType.value = data.businessType || ''
      description.value = data.description || data.remark || ''
      flowData.value = data.nodes || []

      // 保存到缓存
      saveCache({
        flowId: flowId.value,
        flowName: flowName.value,
        flowData: flowData.value,
        isAdd: false
      })
      isLoading.value = false
    } catch (error: any) {
      // 首次失败时重试一次（可能是 token 刷新竞态导致）
      if (retryCount < 1) {
        await new Promise((r) => setTimeout(r, 800))
        return loadFromApi(retryCount + 1)
      }
      ElMessage.error(error.message || '加载流程数据失败')
      isLoading.value = false
    }
  }

  /**
   * 初始化数据
   */
  const initData = async () => {
    isLoading.value = true

    if (flowId.value) {
      // 编辑模式：先确保 token 可用，再从 API 加载最新数据
      await ensureTokenReady()
      await loadFromApi()
    } else {
      // 新增模式：尝试从缓存加载未保存的草稿
      const hasCache = loadFromCache()
      if (!hasCache) {
        isLoading.value = false
      } else {
        isLoading.value = false
      }
    }
  }

  // ========== 事件处理 ==========
  /**
   * 保存流程（调用后端 API 持久化）
   */
  const handleSuccess = async (data: FlowSaveData) => {
    try {
      const flowPayload: ApprovalFlow = {
        flowName: data.flowName,
        flowCode: data.flowCode,
        businessType: data.businessType,
        description: data.description,
        status: 1,
        nodes: data.nodes
      }

      if (isAdd.value) {
        await fetchAddFlow(flowPayload)
      } else if (flowId.value) {
        await fetchUpdateFlow(flowId.value, flowPayload)
      }

      // 发送跨标签页消息
      sendMessage('flow-saved', {
        flowId: flowId.value || data.flowId,
        flowName: data.flowName
      })

      // 清除缓存
      clearCache(flowId.value)

      // 延迟关闭窗口（给用户看到后端返回的成功提示）
      setTimeout(() => {
        window.close()
      }, 1200)
    } catch (error: any) {
      ElMessage.error(error.message || '保存失败，请重试')
    }
  }

  /**
   * 取消编辑
   */
  const handleCancel = () => {
    ElMessageBox.confirm('确定要取消编辑吗？未保存的数据将丢失。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(() => {
        // 清除缓存
        clearCache(flowId.value)

        // 发送关闭消息
        sendMessage('flow-closed', {
          flowId: flowId.value
        })

        // 关闭窗口
        window.close()
      })
      .catch(() => {
        // 用户取消
      })
  }

  /**
   * 关闭编辑器
   */
  const handleClose = () => {
    handleCancel()
  }

  // ========== 生命周期 ==========
  onMounted(() => {
    initData()
  })

  onUnmounted(() => {
    // 清理缓存（可选）
    // clearCache(flowId.value)
  })
</script>

<style scoped lang="scss">
  .flow-editor-page {
    @apply w-full h-screen overflow-hidden;
  }
</style>
