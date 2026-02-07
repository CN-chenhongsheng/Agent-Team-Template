<!-- 学生详情 Drawer 抽屉 -->
<template>
  <ArtDrawer
    v-model="drawerVisible"
    title="查看学生"
    :loading="loading"
    :with-header="true"
    @close="handleClose"
  >
    <div class="student-detail-content">
      <!-- 顶部用户卡片 -->
      <ArtStudentHeaderCard :student="formData" />

      <!-- 标签页内容 -->
      <ElTabs v-model="activeTab" class="student-detail-tabs">
        <!-- 基本信息标签页 -->
        <ElTabPane label="基本信息" name="basic">
          <ArtBasicInfo :data="formData" />
        </ElTabPane>

        <!-- 生活习惯标签页 -->
        <ElTabPane label="生活习惯" name="lifestyle">
          <StudentLifestyleInfo :data="formData" />
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
  import StudentLifestyleInfo from './student-lifestyle-info.vue'
  import { fetchGetStudentDetail } from '@/api/student-manage'

  defineOptions({ name: 'StudentDrawer' })

  type StudentDetail = Api.StudentManage.StudentDetail

  interface Props {
    /** 是否显示 */
    visible: boolean
    /** 对话框类型 */
    dialogType: 'edit' | 'view'
    /** 学生ID */
    studentId?: number
    /** 学生数据 */
    studentData?: StudentDetail | null
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'close'): void
    (e: 'saved'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    visible: false,
    dialogType: 'view',
    studentId: undefined,
    studentData: null
  })

  const emit = defineEmits<Emits>()

  const drawerVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const activeTab = ref('basic')
  const loading = ref(false)
  const formData = ref<Partial<StudentDetail>>({})

  // 获取学生详情
  const loadStudentDetail = async () => {
    if (!props.studentId) {
      formData.value = {}
      return
    }

    // 优先使用 studentId 调用API获取完整数据
    // 这样可以确保数据是最新的和完整的
    try {
      loading.value = true
      const res = await fetchGetStudentDetail(props.studentId)
      if (res) {
        formData.value = res
      }
    } catch {
      // 如果API调用失败，fallback到传入的 studentData
      if (props.studentData) {
        formData.value = { ...props.studentData }
      }
      ElMessage.error('获取学生详情失败')
    } finally {
      loading.value = false
    }
  }

  // 监听抽屉显示状态
  watch(
    () => props.visible,
    (newVal) => {
      if (newVal) {
        activeTab.value = 'basic'
        loadStudentDetail()
      }
    }
  )

  // 监听学生ID变化
  watch(
    () => props.studentId,
    () => {
      if (props.visible) {
        loadStudentDetail()
      }
    }
  )

  // 监听 studentData 变化（作为备用）
  watch(
    () => props.studentData,
    (newVal) => {
      if (props.visible && newVal && !props.studentId) {
        // 只有在没有 studentId 时才使用传入的数据
        formData.value = { ...newVal }
      }
    }
  )

  // 关闭抽屉
  const handleClose = () => {
    emit('update:visible', false)
    emit('close')
  }
</script>

<style lang="scss" scoped>
  .student-detail-content {
    display: flex;
    flex-direction: column;
    gap: 16px;
    padding: 0;
  }

  // 标签页样式
  .student-detail-tabs {
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
