<!-- 审批详情 Drawer 抽屉 -->
<template>
  <ArtDrawer
    v-model="drawerVisible"
    title="审批处理"
    :loading="loading"
    :with-header="true"
    @close="handleClose"
  >
    <div class="approval-detail-content">
      <!-- 顶部用户卡片 -->
      <ArtStudentHeaderCard :student="studentData" />

      <!-- 标签页内容 -->
      <ElTabs v-model="activeTab" class="approval-detail-tabs">
        <!-- 学生信息标签页 -->
        <ElTabPane label="学生信息" name="student">
          <ArtBasicInfo :data="studentData" />
        </ElTabPane>

        <!-- 审批信息标签页 -->
        <ElTabPane label="审批信息" name="approval">
          <PendingApprovalInfo
            :business-type="businessType"
            :business-id="businessId"
            @approval-success="handleApprovalSuccess"
          />
        </ElTabPane>
      </ElTabs>
    </div>
  </ArtDrawer>
</template>

<script setup lang="ts">
  import { ref, watch, computed } from 'vue'
  import { ElTabs, ElTabPane, ElMessage } from 'element-plus'
  import ArtDrawer from '@/components/core/layouts/art-drawer/index.vue'
  import ArtBasicInfo from '@/components/core/layouts/art-basic-info/index.vue'
  import ArtStudentHeaderCard from '@/components/core/cards/art-student-header-card/index.vue'
  import PendingApprovalInfo from './pending-approval-info.vue'
  import {
    fetchGetCheckInDetail,
    fetchGetTransferDetail,
    fetchGetCheckOutDetail,
    fetchGetStayDetail,
    fetchGetStudentDetail
  } from '@/api/accommodation-manage'
  import type { ApprovalInstance } from '@/api/approval-manage'

  defineOptions({ name: 'ApprovalDrawer' })

  type StudentDetail = Api.AccommodationManage.StudentDetail

  interface Props {
    /** 是否显示 */
    visible: boolean
    /** 审批实例数据 */
    instanceData: ApprovalInstance | null
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'close'): void
    (e: 'success'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    visible: false,
    instanceData: null
  })

  const emit = defineEmits<Emits>()

  const drawerVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const activeTab = ref('student')
  const loading = ref(false)
  const studentData = ref<Partial<StudentDetail>>({})

  // 从审批实例中提取业务类型和业务ID
  const businessType = computed(() => props.instanceData?.businessType || '')
  const businessId = computed(() => props.instanceData?.businessId || null)

  // 加载数据
  const loadData = async () => {
    if (!props.instanceData) {
      studentData.value = {}
      return
    }

    try {
      loading.value = true

      // 根据业务类型获取业务详情

      let businessData: any = null
      const type = props.instanceData.businessType
      const id = props.instanceData.businessId

      if (id) {
        switch (type) {
          case 'check_in':
            businessData = await fetchGetCheckInDetail(id)
            break
          case 'transfer':
            businessData = await fetchGetTransferDetail(id)
            break
          case 'check_out':
            businessData = await fetchGetCheckOutDetail(id)
            break
          case 'stay':
            businessData = await fetchGetStayDetail(id)
            break
        }
      }

      // 根据业务详情中的 studentId 获取学生详情
      if (businessData?.studentId) {
        const studentRes = await fetchGetStudentDetail(businessData.studentId)
        if (studentRes) {
          studentData.value = studentRes
        }
      } else if (businessData?.studentInfo) {
        studentData.value = businessData.studentInfo
      } else if (businessData?.applicantName) {
        studentData.value = { studentName: businessData.applicantName }
      }
    } catch (error) {
      ElMessage.error('获取详情失败')
      console.error('获取详情失败:', error)
    } finally {
      loading.value = false
    }
  }

  // 监听抽屉显示状态
  watch(
    () => props.visible,
    (newVal) => {
      if (newVal) {
        activeTab.value = 'student'
        loadData()
      }
    }
  )

  // 监听审批实例数据变化
  watch(
    () => props.instanceData,
    () => {
      if (props.visible) {
        loadData()
      }
    }
  )

  // 关闭抽屉
  const handleClose = () => {
    emit('update:visible', false)
    emit('close')
  }

  // 审批成功处理
  const handleApprovalSuccess = () => {
    emit('success')
  }
</script>

<style lang="scss" scoped>
  .approval-detail-content {
    display: flex;
    flex-direction: column;
    gap: 16px;
    padding: 0;
  }

  // 标签页样式
  .approval-detail-tabs {
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
</style>
