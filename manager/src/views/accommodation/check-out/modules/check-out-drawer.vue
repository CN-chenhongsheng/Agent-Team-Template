<!-- 退宿申请详情 Drawer 抽屉 -->
<template>
  <ArtDrawer
    v-model="drawerVisible"
    title="退宿申请详情"
    :loading="loading"
    :with-header="true"
    @close="handleClose"
  >
    <div class="check-out-detail-content">
      <!-- 顶部用户卡片 -->
      <ArtStudentHeaderCard :student="studentData" />

      <!-- 标签页内容 -->
      <ElTabs v-model="activeTab" class="check-out-detail-tabs">
        <!-- 学生信息标签页 -->
        <ElTabPane label="学生信息" name="student">
          <ArtBasicInfo :data="studentData" />
        </ElTabPane>

        <!-- 审批信息标签页 -->
        <ElTabPane label="审批信息" name="approval">
          <CheckOutApprovalInfo
            business-type="check_out"
            :business-id="checkOutId"
            :check-out-data="checkOutDetail"
            @approval-success="handleApprovalSuccess"
          />
        </ElTabPane>
      </ElTabs>
    </div>
  </ArtDrawer>
</template>

<script setup lang="ts">
  import { ref, watch, computed } from 'vue'
  import { ElTabs, ElTabPane } from 'element-plus'
  import ArtDrawer from '@/components/core/layouts/art-drawer/index.vue'
  import ArtBasicInfo from '@/components/core/layouts/art-basic-info/index.vue'
  import ArtStudentHeaderCard from '@/components/core/cards/art-student-header-card/index.vue'
  import CheckOutApprovalInfo from './check-out-approval-info.vue'
  import { fetchGetCheckOutDetail, fetchGetStudentDetail } from '@/api/accommodation-manage'

  defineOptions({ name: 'CheckOutDrawer' })

  type CheckOutListItem = Api.AccommodationManage.CheckOutListItem
  type StudentDetail = Api.AccommodationManage.StudentDetail

  interface Props {
    /** 是否显示 */
    visible: boolean
    /** 退宿申请ID */
    checkOutId: number | null
    /** 可选的退宿申请数据（用于避免重复请求） */
    checkOutData?: CheckOutListItem | null
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'close'): void
    (e: 'approval-success'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    visible: false,
    checkOutId: null,
    checkOutData: null
  })

  const emit = defineEmits<Emits>()

  const drawerVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const activeTab = ref('student')
  const loading = ref(false)
  const checkOutDetail = ref<CheckOutListItem | null>(null)
  const studentData = ref<Partial<StudentDetail>>({})

  // 加载退宿申请详情和学生信息
  const loadData = async () => {
    if (!props.checkOutId) {
      checkOutDetail.value = null
      studentData.value = {}
      return
    }

    try {
      loading.value = true

      // 优先使用 checkOutId 调用API获取完整数据
      // 这样可以确保数据是最新的和完整的
      const checkOutRes = await fetchGetCheckOutDetail(props.checkOutId)
      checkOutDetail.value = checkOutRes || null

      // 根据退宿申请中的 studentId 获取学生详情
      if (checkOutDetail.value) {
        const studentId = checkOutDetail.value.studentId
        if (checkOutDetail.value.studentInfo) {
          studentData.value = checkOutDetail.value.studentInfo
        } else if (studentId) {
          const studentRes = await fetchGetStudentDetail(studentId)
          if (studentRes) {
            studentData.value = studentRes
          }
        } else {
          studentData.value = {}
        }
      }
    } catch (error) {
      // 如果API调用失败，fallback到传入的 checkOutData
      if (props.checkOutData) {
        checkOutDetail.value = props.checkOutData
        if (checkOutDetail.value) {
          if (checkOutDetail.value.studentInfo) {
            studentData.value = checkOutDetail.value.studentInfo
          } else if (checkOutDetail.value.studentId) {
            try {
              const studentRes = await fetchGetStudentDetail(checkOutDetail.value.studentId)
              if (studentRes) {
                studentData.value = studentRes
              }
            } catch {
              studentData.value = {}
            }
          } else {
            studentData.value = {}
          }
        }
      }
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

  // 监听退宿申请ID变化
  watch(
    () => props.checkOutId,
    () => {
      if (props.visible) {
        loadData()
      }
    }
  )

  // 监听 checkOutData 变化（作为备用）
  watch(
    () => props.checkOutData,
    (newVal) => {
      if (props.visible && newVal && !props.checkOutId) {
        checkOutDetail.value = newVal
        if (newVal.studentInfo) {
          studentData.value = newVal.studentInfo
        } else if (newVal.studentId) {
          fetchGetStudentDetail(newVal.studentId).then((res) => {
            if (res) {
              studentData.value = res
            }
          })
        }
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
    emit('approval-success')
  }
</script>

<style lang="scss" scoped>
  .check-out-detail-content {
    display: flex;
    flex-direction: column;
    gap: 16px;
    padding: 0;
  }

  // 标签页样式
  .check-out-detail-tabs {
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
