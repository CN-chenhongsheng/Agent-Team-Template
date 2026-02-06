<!-- 用户管理范围分配对话框 -->
<template>
  <ElDialog
    v-model="dialogVisible"
    title="分配管理范围"
    width="90%"
    top="5vmin"
    :close-on-click-modal="false"
    :close-on-press-escape="true"
    @close="handleClose"
  >
    <div v-loading="loading" class="scope-dialog-content">
      <div v-if="error" class="error-message">
        <ElAlert type="error" :title="error" show-icon :closable="false" />
      </div>

      <div v-else-if="flowData && flowData.nodes.length > 0" class="logic-flow-wrapper">
        <ArtLogicFlow
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
      </div>

      <div v-else-if="!loading" class="empty-message">
        <ElEmpty description="暂无数据" />
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="handleClose">取消</ElButton>
        <ElButton type="primary" @click="handleSubmit" :loading="submitting"> 确定 </ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { nextTick } from 'vue'
  import { ElDialog, ElButton, ElAlert, ElEmpty } from 'element-plus'
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

  defineOptions({ name: 'UserScopeDialog' })

  interface Props {
    visible: boolean
    userData?: {
      id?: number
      manageScope?: string
      [key: string]: any
    }
  }

  const props = withDefaults(defineProps<Props>(), {
    visible: false,
    userData: () => ({})
  })

  interface Emits {
    (e: 'update:visible', visible: boolean): void
    (e: 'submit', manageScope: string): void
  }

  const emit = defineEmits<Emits>()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const logicFlowRef = ref<InstanceType<typeof ArtLogicFlow>>()
  const loading = ref(false)
  const error = ref('')
  const submitting = ref(false)
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

  /**
   * 加载层级数据
   */
  const loadData = async () => {
    if (loading.value) return

    loading.value = true
    error.value = ''

    try {
      // 加载完整层级数据
      const tree = await loadFullHierarchy()
      hierarchyTree.value = tree

      // 转换为 LogicFlow 数据格式
      const data = hierarchyToLogicFlowData(tree)

      // 构建节点ID到类型的映射
      buildNodeTypeMap(data)

      // 解析用户当前的管理范围
      const currentScope = parseManageScope(props.userData?.manageScope)
      selectedNodes.value = { ...currentScope }

      // 先设置 flowData，让 LogicFlow 开始渲染
      flowData.value = data

      // 等待 LogicFlow 完全渲染后再更新节点样式和居中
      // 使用多个 nextTick 确保 LogicFlow 实例已准备好
      await nextTick()

      // 延迟一点时间确保 LogicFlow 完全初始化
      setTimeout(() => {
        if (logicFlowRef.value) {
          const lfInstance = logicFlowRef.value.getInstance()
          if (lfInstance) {
            // 先调整画布大小，确保 lf-graph 填充父容器
            lfInstance.resize()
            // 然后更新节点样式
            updateNodeSelection(data)
            // 最后确保画布居中显示
            lfInstance.fitView(50)
          }
        }
      }, 200)
    } catch (err: any) {
      console.error('[UserScopeDialog] 加载数据失败:', err)
      error.value = err?.message || '加载数据失败，请稍后重试'
    } finally {
      loading.value = false
    }
  }

  /**
   * 构建节点ID到类型的映射
   */
  const buildNodeTypeMap = (data: typeof flowData.value) => {
    if (!data) return

    nodeTypeMap.value.clear()

    data.nodes.forEach((node) => {
      const properties = node.properties || {}
      const originalId = properties.originalId
      const nodeType = properties.nodeType

      // 确保 originalId 是有效的数字
      if (originalId != null && nodeType) {
        const id =
          typeof originalId === 'string' ? Number.parseInt(originalId, 10) : Number(originalId)
        if (!Number.isNaN(id) && id > 0) {
          nodeTypeMap.value.set(node.id, {
            type: nodeType as 'campus' | 'department' | 'major' | 'class',
            id
          })
        } else {
          console.warn('[UserScopeDialog] 无效的 originalId:', originalId, '节点:', node)
        }
      } else {
        console.warn('[UserScopeDialog] 节点缺少 originalId 或 nodeType:', node)
      }
    })
  }

  /**
   * 更新节点选择状态（通过修改节点样式）
   */
  const updateNodeSelection = (data: typeof flowData.value) => {
    if (!data) {
      console.warn('[UserScopeDialog] updateNodeSelection: data 为空')
      return
    }

    if (!logicFlowRef.value) {
      console.warn('[UserScopeDialog] updateNodeSelection: logicFlowRef 未准备好，延迟重试')
      // 延迟重试
      setTimeout(() => {
        if (logicFlowRef.value && data) {
          updateNodeSelection(data)
        }
      }, 200)
      return
    }

    const lfInstance = logicFlowRef.value.getInstance()
    if (!lfInstance) {
      console.warn('[UserScopeDialog] updateNodeSelection: LogicFlow 实例未准备好，延迟重试')
      // 延迟重试
      setTimeout(() => {
        if (logicFlowRef.value && data) {
          updateNodeSelection(data)
        }
      }, 200)
      return
    }

    // 使用 nextTick 确保 LogicFlow 已完全初始化
    nextTick(() => {
      // 更新节点样式以显示选中状态
      data.nodes.forEach((node) => {
        const nodeInfo = nodeTypeMap.value.get(node.id)
        if (nodeInfo) {
          const isSelected = isNodeSelected(nodeInfo.id, nodeInfo.type, selectedNodes.value)

          try {
            // 更新节点属性
            const nodeModel = lfInstance.getNodeModelById(node.id)
            if (nodeModel) {
              nodeModel.setProperties({
                ...nodeModel.properties,
                isSelected
              })
            }
          } catch (err) {
            console.warn('[UserScopeDialog] 更新节点样式失败:', err)
          }
        }
      })
    })
  }

  /**
   * 处理节点点击
   */
  const handleNodeClick = (event: { node: any; e: MouseEvent }) => {
    const nodeId = event.node.id
    const nodeInfo = nodeTypeMap.value.get(nodeId)

    if (!nodeInfo) {
      console.warn('[UserScopeDialog] 未找到节点信息:', nodeId)
      return
    }

    // 验证节点ID是否有效
    if (nodeInfo.id == null || Number.isNaN(nodeInfo.id)) {
      console.warn('[UserScopeDialog] 无效的节点ID:', nodeInfo.id, '节点信息:', nodeInfo)
      return
    }

    // 切换节点选中状态（传入层级树以支持自动回填父级节点）
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
        // 更新受影响的节点（当前节点、父级、子级）
        const affectedNodes = new Set<string>()
        affectedNodes.add(nodeId)

        // 如果选中了节点，找出所有父级
        if (isNowSelected) {
          const parents = findParentNodes(nodeInfo.id, nodeInfo.type, hierarchyTree.value)
          parents.forEach((p) => {
            const pid = Array.from(nodeTypeMap.value.entries()).find(
              ([, info]) => info.id === p.id && info.type === p.type
            )?.[0]
            if (pid) affectedNodes.add(pid)
          })
        } else {
          // 如果取消选中，找出所有子级
          const children = findChildNodes(nodeInfo.id, nodeInfo.type, hierarchyTree.value)
          children.forEach((c) => {
            const cid = Array.from(nodeTypeMap.value.entries()).find(
              ([, info]) => info.id === c.id && info.type === c.type
            )?.[0]
            if (cid) affectedNodes.add(cid)
          })
        }

        // 统一更新受影响节点的样式
        affectedNodes.forEach((id) => {
          const info = nodeTypeMap.value.get(id)
          const nodeModel = lfInstance.getNodeModelById(id)
          if (info && nodeModel) {
            const isSelected = isNodeSelected(info.id, info.type, selectedNodes.value)

            nodeModel.setProperties({
              ...nodeModel.properties,
              isSelected
            })
          }
        })
      } catch (err) {
        console.warn('[UserScopeDialog] 点击后更新节点样式失败:', err)
        if (flowData.value) updateNodeSelection(flowData.value)
      }
    }
  }

  /**
   * 处理提交
   */
  const handleSubmit = async () => {
    try {
      submitting.value = true

      // 序列化选中的节点
      const manageScope = serializeManageScope(selectedNodes.value)

      // 触发提交事件
      emit('submit', manageScope)

      // 关闭对话框
      dialogVisible.value = false
    } catch (err: any) {
      console.error('[UserScopeDialog] 提交失败:', err)
    } finally {
      submitting.value = false
    }
  }

  /**
   * 处理关闭
   */
  const handleClose = () => {
    dialogVisible.value = false
  }

  // 监听 visible 变化，打开时加载数据
  watch(
    () => props.visible,
    (newVal) => {
      if (newVal) {
        loadData()
      } else {
        // 重置状态
        flowData.value = null
        hierarchyTree.value = null
        error.value = ''
        selectedNodes.value = {
          campusIds: [],
          departmentIds: [],
          majorIds: [],
          classIds: []
        }
        nodeTypeMap.value.clear()
      }
    },
    { immediate: true }
  )

  // 监听 flowData 和 logicFlowRef，当两者都准备好时更新节点样式（用于回显）
  watch(
    [() => flowData.value, () => logicFlowRef.value],
    ([newData, newRef]) => {
      if (
        newData &&
        newData.nodes.length > 0 &&
        newRef &&
        selectedNodes.value &&
        Object.keys(selectedNodes.value).some(
          (key) => (selectedNodes.value[key as keyof SelectedNodes] as number[]).length > 0
        )
      ) {
        // 确保 LogicFlow 实例已准备好
        const lfInstance = newRef.getInstance()
        if (lfInstance) {
          // 延迟一点时间确保节点已完全渲染
          setTimeout(() => {
            // 先调整画布大小，确保 lf-graph 填充父容器
            lfInstance.resize()
            // 然后更新节点样式
            updateNodeSelection(newData)
            // 最后确保画布居中显示
            lfInstance.fitView(50)
          }, 150)
        }
      } else if (newData && newData.nodes.length > 0 && newRef) {
        // 即使没有选中节点，也要确保画布居中
        const lfInstance = newRef.getInstance()
        if (lfInstance) {
          setTimeout(() => {
            // 先调整画布大小，确保 lf-graph 填充父容器
            lfInstance.resize()
            // 然后确保画布居中显示
            lfInstance.fitView(50)
          }, 150)
        }
      }
    },
    { deep: true }
  )
</script>

<style scoped lang="scss">
  .scope-dialog-content {
    display: flex;
    flex-direction: column;
    height: 700px;
    min-height: 700px;

    .error-message {
      margin-bottom: 20px;
    }

    .logic-flow-wrapper {
      flex: 1;
      width: 100%;
      min-height: 0;
      overflow: hidden;
      background-color: var(--el-fill-color-light);
      border: 1px solid var(--el-border-color-light);
      border-radius: var(--el-border-radius-base);
    }

    .empty-message {
      display: flex;
      flex: 1;
      align-items: center;
      justify-content: center;
      min-height: 0;
    }
  }

  .dialog-footer {
    display: flex;
    gap: 12px;
    justify-content: flex-end;
  }

  // 暗色主题适配
  :global(.dark) {
    .scope-dialog-content {
      .logic-flow-wrapper {
        background-image: none;
      }
    }
  }
</style>
