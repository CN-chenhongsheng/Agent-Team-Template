<template>
  <div class="scope-editor-page w-full h-screen flex flex-col">
    <el-skeleton v-if="isLoading" :rows="10" animated class="p-6" />

    <template v-else>
      <!-- 工具栏 -->
      <div
        class="scope-editor-toolbar flex items-center justify-between px-6 py-4 bg-[var(--default-box-color)] border-b border-[var(--default-border)]"
      >
        <div class="flex items-center gap-2 text-sm text-[var(--el-text-color-regular)]">
          <span>正在编辑：</span>
          <el-tag type="primary">{{ nickname }}</el-tag>
          <span class="text-[var(--el-text-color-secondary)]">({{ username }})</span>
        </div>
        <div class="flex items-center gap-3">
          <el-button @click="handleCancel">取消</el-button>
          <el-button type="primary" :loading="isSaving" @click="handleSave">保存</el-button>
        </div>
      </div>

      <!-- 画布区域 -->
      <div class="scope-editor-canvas flex-1 relative overflow-hidden">
        <div v-if="error" class="flex items-center justify-center h-full p-6">
          <el-alert type="error" :title="error" show-icon :closable="false" />
        </div>

        <ArtLogicFlow
          v-else-if="flowData && flowData.nodes.length > 0"
          ref="logicFlowRef"
          :data="flowData"
          :readonly="false"
          :allow-edge-adjustment="false"
          :show-undo="false"
          :show-redo="false"
          layout-direction="horizontal"
          :node-spacing-x="250"
          :node-spacing-y="120"
          height="100%"
          @node:click="handleNodeClick"
        />

        <div v-else class="flex items-center justify-center h-full">
          <el-empty description="暂无数据" />
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
  import { ref, onMounted, nextTick } from 'vue'
  import { useRoute } from 'vue-router'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import ArtLogicFlow from '@/components/core/charts/art-logic-flow/index.vue'
  import type { LogicFlowData } from '@/components/core/charts/art-logic-flow/types'
  import {
    loadFullHierarchy,
    hierarchyToLogicFlowData,
    type FullHierarchyTree
  } from '@/utils/school/scopeDataLoader'
  import {
    parseManageScope,
    serializeManageScope,
    toggleNodeSelection,
    isNodeSelected,
    findParentNodes,
    findChildNodes,
    type SelectedNodes
  } from '@/utils/school/scopeSelector'
  import { fetchUpdateUser } from '@/api/system-manage'
  import { fetchRefreshToken } from '@/api/auth'
  import { useUserStore } from '@/store/modules/user'
  import { useScopeCache } from '../composables/useScopeCache'
  import { useScopeCommunication } from '../composables/useScopeCommunication'

  // ========== 路由参数 ==========
  const route = useRoute()
  const userId = ref<number>(Number(route.params.userId))

  // ========== 数据状态 ==========
  const username = ref('')
  const nickname = ref('')
  const manageScope = ref('')
  const userData = ref<Record<string, any>>({})
  const isLoading = ref(true)
  const isSaving = ref(false)
  const error = ref('')

  // ========== LogicFlow 状态 ==========
  const logicFlowRef = ref<InstanceType<typeof ArtLogicFlow>>()
  const flowData = ref<LogicFlowData | null>(null)
  const hierarchyTree = ref<FullHierarchyTree | null>(null)
  const selectedNodes = ref<SelectedNodes>({
    campusIds: [],
    departmentIds: [],
    majorIds: [],
    classIds: []
  })

  // 节点ID到类型的映射
  const nodeTypeMap = ref<
    Map<string, { type: 'campus' | 'department' | 'major' | 'class'; id: number }>
  >(new Map())

  // ========== Composables ==========
  const { loadCache, clearCache } = useScopeCache()
  const { sendMessage } = useScopeCommunication()

  // ========== Token 刷新 ==========
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

  // ========== 数据加载 ==========
  /**
   * 从缓存加载用户数据
   */
  const loadFromCache = (): boolean => {
    const cached = loadCache(userId.value)
    if (cached) {
      username.value = cached.username || ''
      nickname.value = cached.nickname || ''
      manageScope.value = cached.manageScope || ''
      userData.value = cached.userData || {}
      return true
    }
    return false
  }

  /**
   * 构建节点ID到类型的映射
   */
  const buildNodeTypeMap = (data: LogicFlowData) => {
    nodeTypeMap.value.clear()

    data.nodes.forEach((node) => {
      const properties = node.properties || {}
      const originalId = properties.originalId
      const nodeType = properties.nodeType

      if (originalId != null && nodeType) {
        const id =
          typeof originalId === 'string' ? Number.parseInt(originalId, 10) : Number(originalId)
        if (!Number.isNaN(id) && id > 0) {
          nodeTypeMap.value.set(node.id, {
            type: nodeType as 'campus' | 'department' | 'major' | 'class',
            id
          })
        }
      }
    })
  }

  /**
   * 更新节点选择状态（通过修改节点样式）
   */
  const updateNodeSelection = (data: LogicFlowData) => {
    if (!data || !logicFlowRef.value) return

    const lfInstance = logicFlowRef.value.getInstance()
    if (!lfInstance) {
      // 延迟重试
      setTimeout(() => {
        if (logicFlowRef.value && data) updateNodeSelection(data)
      }, 200)
      return
    }

    nextTick(() => {
      data.nodes.forEach((node) => {
        const nodeInfo = nodeTypeMap.value.get(node.id)
        if (nodeInfo) {
          const selected = isNodeSelected(nodeInfo.id, nodeInfo.type, selectedNodes.value)
          try {
            const nodeModel = lfInstance.getNodeModelById(node.id)
            if (nodeModel) {
              nodeModel.setProperties({
                ...nodeModel.properties,
                isSelected: selected
              })
            }
          } catch {
            // ignore
          }
        }
      })
    })
  }

  /**
   * 加载层级数据并渲染画布
   */
  const loadHierarchyData = async (retryCount = 0) => {
    try {
      const tree = await loadFullHierarchy()
      hierarchyTree.value = tree

      const data = hierarchyToLogicFlowData(tree)
      buildNodeTypeMap(data)

      // 解析用户当前的管理范围
      const currentScope = parseManageScope(manageScope.value)
      selectedNodes.value = { ...currentScope }

      // 设置 flowData，让 LogicFlow 开始渲染
      flowData.value = data

      // 等待渲染完成后更新节点样式
      await nextTick()
      setTimeout(() => {
        if (logicFlowRef.value) {
          const lfInstance = logicFlowRef.value.getInstance()
          if (lfInstance) {
            lfInstance.resize()
            updateNodeSelection(data)
            lfInstance.fitView(50)
          }
        }
      }, 200)
    } catch (err: any) {
      if (retryCount < 1) {
        await new Promise((r) => setTimeout(r, 800))
        return loadHierarchyData(retryCount + 1)
      }
      console.error('[ScopeEditorPage] 加载层级数据失败:', err)
      error.value = err?.message || '加载层级数据失败，请稍后重试'
    }
  }

  /**
   * 初始化数据
   */
  const initData = async () => {
    isLoading.value = true
    error.value = ''

    // 1. 从缓存加载用户信息
    const hasCache = loadFromCache()
    if (!hasCache) {
      error.value = '未找到用户数据，请从用户管理页面重新打开'
      isLoading.value = false
      return
    }

    // 2. 确保 token 可用
    await ensureTokenReady()

    // 3. 加载层级数据
    isLoading.value = false
    await loadHierarchyData()
  }

  // ========== 事件处理 ==========
  /**
   * 处理节点点击
   */
  const handleNodeClick = (event: { node: any; e: MouseEvent }) => {
    const nodeId = event.node.id
    const nodeInfo = nodeTypeMap.value.get(nodeId)

    if (!nodeInfo) return
    if (nodeInfo.id == null || Number.isNaN(nodeInfo.id)) return

    // 切换节点选中状态
    const isNowSelected = toggleNodeSelection(
      nodeInfo.id,
      nodeInfo.type,
      selectedNodes.value,
      hierarchyTree.value || undefined
    )

    // 更新相关节点的样式
    const lfInstance = logicFlowRef.value?.getInstance()
    if (lfInstance && hierarchyTree.value) {
      try {
        const affectedNodes = new Set<string>()
        affectedNodes.add(nodeId)

        if (isNowSelected) {
          const parents = findParentNodes(nodeInfo.id, nodeInfo.type, hierarchyTree.value)
          parents.forEach((p) => {
            const pid = Array.from(nodeTypeMap.value.entries()).find(
              ([, info]) => info.id === p.id && info.type === p.type
            )?.[0]
            if (pid) affectedNodes.add(pid)
          })
        } else {
          const children = findChildNodes(nodeInfo.id, nodeInfo.type, hierarchyTree.value)
          children.forEach((c) => {
            const cid = Array.from(nodeTypeMap.value.entries()).find(
              ([, info]) => info.id === c.id && info.type === c.type
            )?.[0]
            if (cid) affectedNodes.add(cid)
          })
        }

        affectedNodes.forEach((id) => {
          const info = nodeTypeMap.value.get(id)
          const nodeModel = lfInstance.getNodeModelById(id)
          if (info && nodeModel) {
            const selected = isNodeSelected(info.id, info.type, selectedNodes.value)
            nodeModel.setProperties({
              ...nodeModel.properties,
              isSelected: selected
            })
          }
        })
      } catch {
        if (flowData.value) updateNodeSelection(flowData.value)
      }
    }
  }

  /**
   * 保存
   */
  const handleSave = async () => {
    isSaving.value = true

    try {
      const scopeJson = serializeManageScope(selectedNodes.value)

      // 构建更新数据
      const updateData: Api.SystemManage.UserSaveParams = {
        id: userId.value,
        username: userData.value.username || username.value,
        nickname: userData.value.nickname || nickname.value,
        email: userData.value.email,
        phone: userData.value.phone,
        gender: userData.value.gender,
        status: userData.value.status,
        roleIds: userData.value.roleIds,
        manageScope: scopeJson
      }

      await fetchUpdateUser(userId.value, updateData)

      // 发送跨标签页消息
      sendMessage('scope-saved', {
        userId: userId.value,
        nickname: nickname.value
      })

      // 清除缓存
      clearCache(userId.value)

      // 延迟关闭窗口
      setTimeout(() => {
        window.close()
      }, 1200)
    } catch (err: any) {
      ElMessage.error(err?.message || '保存失败，请重试')
    } finally {
      isSaving.value = false
    }
  }

  /**
   * 取消
   */
  const handleCancel = () => {
    ElMessageBox.confirm('确定要取消编辑吗？未保存的数据将丢失。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(() => {
        sendMessage('scope-closed', { userId: userId.value })
        clearCache(userId.value)
        window.close()
      })
      .catch(() => {
        // 用户取消
      })
  }

  // ========== 监听 flowData 和 logicFlowRef 就绪 ==========
  watch(
    [() => flowData.value, () => logicFlowRef.value],
    ([newData, newRef]) => {
      if (!newData || newData.nodes.length === 0 || !newRef) return

      const lfInstance = newRef.getInstance()
      if (!lfInstance) return

      const hasSelection = Object.keys(selectedNodes.value).some(
        (key) => (selectedNodes.value[key as keyof SelectedNodes] as number[]).length > 0
      )

      setTimeout(() => {
        lfInstance.resize()
        if (hasSelection) updateNodeSelection(newData)
        lfInstance.fitView(50)
      }, 150)
    },
    { deep: true }
  )

  // ========== 生命周期 ==========
  onMounted(() => {
    initData()
  })
</script>

<style scoped lang="scss">
  .scope-editor-page {
    @apply w-full h-screen overflow-hidden;
  }

  .scope-editor-canvas {
    background-color: var(--el-fill-color-light);
  }
</style>
