<!-- 报修详情 Drawer 抽屉 -->
<template>
  <ArtDrawer
    v-model="drawerVisible"
    title="报修详情"
    :loading="loading"
    :with-header="true"
    @close="handleClose"
  >
    <div class="repair-detail-content">
      <!-- 顶部报修卡片 -->
      <ArtRepairHeaderCard :repair="repairDetail" />

      <!-- 标签页内容 -->
      <ElTabs v-model="activeTab" class="repair-detail-tabs">
        <!-- 基本信息 -->
        <ElTabPane label="基本信息" name="basic">
          <ArtBasicInfo :data="repairDetail?.studentInfo ?? {}" />
        </ElTabPane>

        <!-- 故障描述 -->
        <ElTabPane label="故障描述" name="fault">
          <ElCard class="info-card fault-desc-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <ArtSvgIcon icon="ri:file-warning-line" class="header-icon" />
                <span class="header-title">故障描述</span>
              </div>
            </template>
            <div class="info-list">
              <div class="info-row fault-desc-row">
                <div class="row-label">
                  <ArtSvgIcon icon="ri:article-line" class="label-icon" />
                  <span>描述</span>
                </div>
                <div class="row-value row-value--wrap">
                  <div
                    class="fault-desc-text"
                    :class="{ 'is-empty': !repairDetail.faultDescription }"
                  >
                    {{ repairDetail.faultDescription || '暂无描述' }}
                  </div>
                </div>
              </div>
              <div v-if="faultImages.length > 0" class="info-row">
                <div class="row-label">
                  <ArtSvgIcon icon="ri:image-line" class="label-icon" />
                  <span>故障图片</span>
                  <span class="image-count">共 {{ faultImages.length }} 张</span>
                </div>
                <div class="row-value row-value--wrap">
                  <div
                    class="image-list"
                    :class="{ 'image-list--compact': faultImages.length > 4 }"
                  >
                    <div v-for="(img, index) in faultImages" :key="index" class="image-item">
                      <ElImage
                        :src="img"
                        :preview-src-list="faultImages"
                        fit="cover"
                        class="info-card-image"
                      />
                      <span class="image-index-badge">{{ Number(index) + 1 }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </ElCard>
        </ElTabPane>

        <!-- 维修信息 -->
        <ElTabPane label="维修信息" name="repair">
          <ElCard v-if="showRepairInfo" class="info-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <ArtSvgIcon icon="ri:tools-line" class="header-icon" />
                <span class="header-title">维修信息</span>
              </div>
            </template>
            <div class="info-list">
              <div class="info-row">
                <div class="row-label">
                  <ArtSvgIcon icon="ri:user-line" class="label-icon" />
                  <span>维修人员</span>
                </div>
                <div class="row-value">{{ repairDetail.repairPersonName || '-' }}</div>
              </div>
              <div class="info-row">
                <div class="row-label">
                  <ArtSvgIcon icon="ri:calendar-line" class="label-icon" />
                  <span>预约时间</span>
                </div>
                <div class="row-value">{{ repairDetail.appointmentTime || '-' }}</div>
              </div>
              <div v-if="repairDetail.repairResult" class="info-row">
                <div class="row-label">
                  <ArtSvgIcon icon="ri:checkbox-circle-line" class="label-icon" />
                  <span>维修结果</span>
                </div>
                <div class="row-value row-value--wrap">{{ repairDetail.repairResult }}</div>
              </div>
              <div v-if="repairImages.length > 0" class="info-row">
                <div class="row-label">
                  <ArtSvgIcon icon="ri:image-line" class="label-icon" />
                  <span>维修后图片</span>
                  <span class="image-count">共 {{ repairImages.length }} 张</span>
                </div>
                <div class="row-value row-value--wrap">
                  <div
                    class="image-list"
                    :class="{ 'image-list--compact': repairImages.length > 4 }"
                  >
                    <div v-for="(img, index) in repairImages" :key="index" class="image-item">
                      <ElImage
                        :src="img"
                        :preview-src-list="repairImages"
                        fit="cover"
                        class="info-card-image"
                      />
                      <span class="image-index-badge">{{ Number(index) + 1 }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </ElCard>
          <ElCard v-else class="info-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <ArtSvgIcon icon="ri:tools-line" class="header-icon" />
                <span class="header-title">维修信息</span>
              </div>
            </template>
            <div class="empty-state">
              <span class="empty-text">暂无维修信息</span>
            </div>
          </ElCard>
        </ElTabPane>

        <!-- 评价信息 -->
        <ElTabPane label="评价信息" name="rating">
          <ElCard v-if="repairDetail.rating" class="info-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <ArtSvgIcon icon="ri:star-smile-line" class="header-icon" />
                <span class="header-title">评价信息</span>
              </div>
            </template>
            <div class="info-list">
              <div class="info-row">
                <div class="row-label">
                  <ArtSvgIcon icon="ri:star-line" class="label-icon" />
                  <span>评分</span>
                </div>
                <div class="row-value">
                  <ElRate :model-value="repairDetail.rating" disabled />
                </div>
              </div>
              <div class="info-row">
                <div class="row-label">
                  <ArtSvgIcon icon="ri:chat-quote-line" class="label-icon" />
                  <span>评价内容</span>
                </div>
                <div class="row-value row-value--wrap">
                  {{ repairDetail.ratingComment || '暂无评价' }}
                </div>
              </div>
            </div>
          </ElCard>
          <ElCard v-else class="info-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <ArtSvgIcon icon="ri:star-smile-line" class="header-icon" />
                <span class="header-title">评价信息</span>
              </div>
            </template>
            <div class="empty-state">
              <span class="empty-text">暂无评价</span>
            </div>
          </ElCard>
        </ElTabPane>
      </ElTabs>

      <!-- 操作按钮 -->
      <div v-if="showActions" class="action-buttons">
        <ElButton
          v-if="canAccept"
          type="primary"
          @click="handleAccept"
          v-ripple
          v-permission="'system:repair:accept'"
        >
          接单
        </ElButton>
        <ElButton
          v-if="canComplete"
          type="success"
          @click="handleComplete"
          v-ripple
          v-permission="'system:repair:complete'"
        >
          完成维修
        </ElButton>
      </div>
    </div>
  </ArtDrawer>
</template>

<script setup lang="ts">
  import { ref, computed, watch } from 'vue'
  import {
    ElButton,
    ElCard,
    ElImage,
    ElRate,
    ElMessage,
    ElMessageBox,
    ElTabs,
    ElTabPane
  } from 'element-plus'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import ArtDrawer from '@/components/core/layouts/art-drawer/index.vue'
  import ArtBasicInfo from '@/components/core/layouts/art-basic-info/index.vue'
  import ArtRepairHeaderCard from '@/components/core/cards/art-repair-header-card/index.vue'
  import { fetchGetRepairDetail, fetchAcceptRepair, fetchCompleteRepair } from '@/api/repair-manage'

  defineOptions({ name: 'RepairDrawer' })

  type RepairListItem = Api.RepairManage.RepairListItem

  interface Props {
    visible: boolean
    repairId: number | null
    repairData?: RepairListItem | null
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'close'): void
    (e: 'refresh'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    visible: false,
    repairId: null,
    repairData: null
  })

  const emit = defineEmits<Emits>()

  const drawerVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const activeTab = ref('basic')
  const loading = ref(false)
  const repairDetail = ref<Partial<RepairListItem>>({})

  // 故障图片
  const faultImages = computed(() => {
    if (repairDetail.value.faultImages) {
      return Array.isArray(repairDetail.value.faultImages)
        ? repairDetail.value.faultImages
        : JSON.parse(repairDetail.value.faultImages as unknown as string)
    }
    return []
  })

  // 维修后图片
  const repairImages = computed(() => {
    if (repairDetail.value.repairImages) {
      return Array.isArray(repairDetail.value.repairImages)
        ? repairDetail.value.repairImages
        : JSON.parse(repairDetail.value.repairImages as unknown as string)
    }
    return []
  })

  // 是否显示维修信息
  const showRepairInfo = computed(() => {
    const status = repairDetail.value.status
    return status && status >= 2
  })

  // 是否显示操作按钮
  const showActions = computed(() => {
    const status = repairDetail.value.status
    return status && [1, 2, 3].includes(status)
  })

  // 是否可以接单
  const canAccept = computed(() => {
    return repairDetail.value.status === 1
  })

  // 是否可以完成
  const canComplete = computed(() => {
    const status = repairDetail.value.status
    return status === 2 || status === 3
  })

  // 加载报修详情
  const loadData = async () => {
    if (!props.repairId) {
      repairDetail.value = props.repairData || {}
      return
    }

    try {
      loading.value = true
      const res = await fetchGetRepairDetail(props.repairId)
      repairDetail.value = res || null
    } catch (error) {
      if (props.repairData) {
        repairDetail.value = props.repairData
      }
      ElMessage.error('获取详情失败')
      console.error('获取详情失败:', error)
    } finally {
      loading.value = false
    }
  }

  // 接单
  const handleAccept = async () => {
    if (!props.repairId) return

    try {
      await ElMessageBox.confirm('确定要接单吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      loading.value = true
      await fetchAcceptRepair(props.repairId)
      ElMessage.success('接单成功')
      emit('refresh')
      loadData()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('接单失败')
        console.error('接单失败:', error)
      }
    } finally {
      loading.value = false
    }
  }

  // 完成维修
  const handleComplete = async () => {
    if (!props.repairId) return

    try {
      await ElMessageBox.confirm('确定要完成维修吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      loading.value = true
      await fetchCompleteRepair(props.repairId, {
        repairResult: '已完成维修',
        repairImages: []
      })
      ElMessage.success('操作成功')
      emit('refresh')
      loadData()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('操作失败')
        console.error('完成维修失败:', error)
      }
    } finally {
      loading.value = false
    }
  }

  // 关闭抽屉
  const handleClose = () => {
    emit('update:visible', false)
    emit('close')
  }

  // 监听抽屉显示状态
  watch(
    () => props.visible,
    (newVal) => {
      if (newVal) {
        activeTab.value = 'basic'
        loadData()
      }
    }
  )

  // 监听报修ID变化
  watch(
    () => props.repairId,
    () => {
      if (props.visible) {
        loadData()
      }
    }
  )

  // 监听 repairData 变化
  watch(
    () => props.repairData,
    (newVal) => {
      if (props.visible && newVal && !props.repairId) {
        repairDetail.value = newVal
      }
    }
  )
</script>

<style lang="scss" scoped>
  .repair-detail-content {
    display: flex;
    flex-direction: column;
    gap: 16px;
    padding: 0;
  }

  .repair-detail-tabs {
    :deep(.el-tabs__header) {
      padding: 0;
      margin: 0 0 24px;
    }

    :deep(.el-tabs__nav-wrap::after) {
      background-color: var(--el-border-color-lighter);
    }

    :deep(.el-tabs__item) {
      height: 48px;
      padding: 0 20px;
      font-size: 15px;
      font-weight: 500;
      line-height: 48px;

      &.is-active {
        font-weight: 600;
        color: var(--el-color-primary);
      }
    }

    :deep(.el-tabs__content) {
      padding: 0;
    }

    :deep(.el-tab-pane) {
      padding: 0;
    }
  }

  .row-value--wrap {
    flex-wrap: wrap;
    overflow: visible;
    white-space: normal;
  }

  .fault-desc-card .fault-desc-row {
    align-items: flex-start;
  }

  .fault-desc-text {
    box-sizing: border-box;
    margin: 0;
    font-size: 14px;
    color: var(--el-text-color-primary);
    word-break: break-word;
    white-space: pre-wrap;

    &.is-empty {
      color: var(--el-text-color-placeholder);
      background: var(--el-fill-color-lighter);
      border-left-color: var(--el-border-color-lighter);
    }
  }

  .image-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;

    &.image-list--compact {
      display: grid;
      grid-template-columns: repeat(5, 64px);
      gap: 6px;
    }
  }

  .image-item {
    position: relative;
    line-height: 0;
  }

  .image-index-badge {
    position: absolute;
    bottom: 4px;
    left: 4px;
    padding: 2px 6px;
    font-size: 11px;
    font-weight: 500;
    line-height: 1.2;
    color: #fff;
    background: rgba(0 0 0 / 55%);
    border-radius: 4px;
  }

  .row-label .image-count {
    margin-left: 6px;
    font-size: 12px;
    font-weight: 400;
    color: var(--el-text-color-secondary);
  }

  .info-card-image {
    width: 80px;
    height: 80px;
    cursor: pointer;
    border-radius: 4px;

    .image-list--compact & {
      width: 64px;
      height: 64px;
    }
  }

  .action-buttons {
    display: flex;
    flex-direction: column;
    gap: 12px;

    :deep(.el-button) {
      width: 100%;
    }
  }
</style>
