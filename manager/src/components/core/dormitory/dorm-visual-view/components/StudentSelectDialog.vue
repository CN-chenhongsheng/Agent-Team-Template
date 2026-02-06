<!--
  StudentSelectDialog 学生选择弹窗组件
  @description 用于空床位分配学生，支持搜索和选择学生
  @author 陈鸿昇
-->
<template>
  <ElDialog
    v-model="dialogVisible"
    title="分配学生到床位"
    width="1100px"
    :close-on-click-modal="false"
    class="student-select-dialog"
    @close="handleClose"
  >
    <!-- 目标床位信息卡片 -->
    <div class="target-card">
      <div class="target-card__icon">
        <ArtSvgIcon icon="ri:hotel-bed-line" class="text-2xl" />
      </div>
      <div class="target-card__info">
        <div class="target-card__title">
          <span class="target-card__room">{{ roomInfo?.roomNumber }}</span>
          <span class="target-card__bed">{{ bedInfo?.bedNumber }}号床</span>
        </div>
        <div class="target-card__meta">
          <span v-if="roomInfo?.floorName" class="target-card__location">
            <ArtSvgIcon icon="ri:building-line" class="mr-1 text-xs" />
            {{ roomInfo?.floorName }}
          </span>
          <span v-if="dormitoryGenderText" class="target-card__gender">
            <ArtSvgIcon
              :icon="genderFilter === 1 ? 'ri:men-line' : 'ri:women-line'"
              class="mr-1 text-xs"
            />
            {{ dormitoryGenderText }}
          </span>
        </div>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="search-section">
      <div class="search-section__title">
        <ArtSvgIcon icon="ri:search-line" class="mr-1.5" />
        搜索学生
      </div>
      <div class="search-section__form">
        <!-- 第一行：基础搜索 -->
        <div class="search-row">
          <ElInput
            v-model="searchParams.studentNo"
            placeholder="学号"
            clearable
            class="search-input"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <ArtSvgIcon icon="ri:hashtag" class="text-g-400" />
            </template>
          </ElInput>
          <ElInput
            v-model="searchParams.studentName"
            placeholder="姓名"
            clearable
            class="search-input"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <ArtSvgIcon icon="ri:user-3-line" class="text-g-400" />
            </template>
          </ElInput>
          <ElInput
            v-model="searchParams.phone"
            placeholder="手机号"
            clearable
            class="search-input"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <ArtSvgIcon icon="ri:phone-line" class="text-g-400" />
            </template>
          </ElInput>
        </div>
        <!-- 第二行：级联选择 -->
        <div class="search-row">
          <ElSelect
            v-model="searchParams.deptCode"
            placeholder="选择学院"
            clearable
            filterable
            class="search-select"
            @change="handleDeptChange"
          >
            <ElOption
              v-for="item in deptOptions"
              :key="item.deptCode"
              :label="item.deptName"
              :value="item.deptCode"
            />
          </ElSelect>
          <ElSelect
            v-model="searchParams.majorCode"
            placeholder="选择专业"
            clearable
            filterable
            class="search-select"
            :disabled="!searchParams.deptCode"
            @change="handleMajorChange"
          >
            <ElOption
              v-for="item in majorOptions"
              :key="item.majorCode"
              :label="item.majorName"
              :value="item.majorCode"
            />
          </ElSelect>
          <ElSelect
            v-model="searchParams.classId"
            placeholder="选择班级"
            clearable
            filterable
            class="search-select"
            :disabled="!searchParams.majorCode"
          >
            <ElOption
              v-for="item in classOptions"
              :key="item.id"
              :label="item.className"
              :value="item.id"
            />
          </ElSelect>
        </div>
        <!-- 操作按钮 -->
        <div class="search-actions">
          <ElButton type="primary" @click="handleSearch">
            <ArtSvgIcon icon="ri:search-line" class="mr-1" />
            搜索
          </ElButton>
          <ElButton @click="handleReset">
            <ArtSvgIcon icon="ri:refresh-line" class="mr-1" />
            重置
          </ElButton>
        </div>
      </div>
    </div>

    <!-- 学生列表 -->
    <div class="table-section">
      <ElTable
        v-loading="loading"
        :data="studentList"
        :row-class-name="getRowClassName"
        :max-height="300"
        @row-click="handleRowClick"
        class="student-table"
      >
        <ElTableColumn prop="studentNo" label="学号" min-width="100" />
        <ElTableColumn prop="studentName" label="姓名" min-width="75">
          <template #default="{ row }">
            <span class="font-medium text-g-800">{{ row.studentName }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="genderText" label="性别" min-width="55" align="center">
          <template #default="{ row }">
            <span :class="row.gender === 1 ? 'text-primary' : 'text-pink-500'">
              {{ row.genderText }}
            </span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="phone" label="手机号" min-width="110" />
        <ElTableColumn prop="deptName" label="院系" min-width="130" show-overflow-tooltip />
        <ElTableColumn prop="majorName" label="专业" min-width="110" show-overflow-tooltip />
        <ElTableColumn prop="className" label="班级" min-width="90" show-overflow-tooltip />
        <ElTableColumn label="状态" min-width="70" align="center">
          <template #default="{ row }">
            <ElTag v-if="row.bedId" type="info" size="small" effect="plain" round> 已分配 </ElTag>
            <ElTag v-else type="success" size="small" effect="plain" round> 可选 </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn label="操作" width="80" align="center" fixed="right">
          <template #default="{ row }">
            <ElButton
              v-if="!row.bedId"
              :type="selectedStudent?.id === row.id ? 'success' : 'primary'"
              size="small"
              link
              @click.stop="handleSelect(row)"
            >
              <ArtSvgIcon
                :icon="
                  selectedStudent?.id === row.id
                    ? 'ri:checkbox-circle-fill'
                    : 'ri:checkbox-blank-circle-line'
                "
                class="mr-1"
              />
              {{ selectedStudent?.id === row.id ? '已选' : '选择' }}
            </ElButton>
            <span v-else class="text-g-300">-</span>
          </template>
        </ElTableColumn>
      </ElTable>
    </div>

    <!-- 底部栏 -->
    <div class="footer-bar">
      <!-- 分页 -->
      <ElPagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        small
        background
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />

      <!-- 操作按钮 -->
      <div class="footer-actions">
        <transition name="slide-fade">
          <div v-if="selectedStudent" class="selected-info">
            <ArtSvgIcon icon="ri:user-follow-fill" class="selected-info__icon" />
            <div class="selected-info__text">
              <span class="selected-info__label">已选择</span>
              <span class="selected-info__name">{{ selectedStudent.studentName }}</span>
              <span class="selected-info__no">{{ selectedStudent.studentNo }}</span>
            </div>
          </div>
        </transition>
        <ElButton size="large" @click="handleClose">取消</ElButton>
        <ElButton
          type="primary"
          size="large"
          :disabled="!selectedStudent"
          :loading="submitting"
          @click="handleConfirm"
        >
          <ArtSvgIcon icon="ri:check-line" class="mr-1" />
          确认分配
        </ElButton>
      </div>
    </div>
  </ElDialog>
</template>

<script setup lang="ts">
  import { ref, reactive, watch, computed } from 'vue'
  import {
    ElDialog,
    ElInput,
    ElButton,
    ElTable,
    ElTableColumn,
    ElPagination,
    ElMessage,
    ElTag,
    ElSelect,
    ElOption
  } from 'element-plus'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import { fetchGetStudentPage, fetchAdminAssignBed } from '@/api/accommodation-manage'
  import { fetchGetDepartmentPage, fetchGetMajorPage, fetchGetClassPage } from '@/api/school-manage'
  import type { BedWithStudent, RoomWithBeds } from '../types'

  defineOptions({ name: 'StudentSelectDialog' })

  // ==================== Props ====================
  interface Props {
    /** 弹窗显示状态 */
    visible: boolean
    /** 目标床位信息 */
    bedInfo: BedWithStudent | null
    /** 目标房间信息 */
    roomInfo: RoomWithBeds | null
  }

  const props = withDefaults(defineProps<Props>(), {
    visible: false,
    bedInfo: null,
    roomInfo: null
  })

  // ==================== Emits ====================
  const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void
    (e: 'success'): void
  }>()

  // ==================== State ====================

  /** 弹窗显示状态 */
  const dialogVisible = computed({
    get: () => props.visible,
    set: (val) => emit('update:visible', val)
  })

  /** 搜索参数 */
  const searchParams = reactive({
    studentNo: '',
    studentName: '',
    phone: '',
    deptCode: '',
    majorCode: '',
    classId: undefined as number | undefined
  })

  /** 分页参数 */
  const pagination = reactive({
    pageNum: 1,
    pageSize: 10,
    total: 0
  })

  /** 加载状态 */
  const loading = ref(false)

  /** 提交状态 */
  const submitting = ref(false)

  /** 学生列表 */
  const studentList = ref<Api.AccommodationManage.StudentListItem[]>([])

  /** 选中的学生 */
  const selectedStudent = ref<Api.AccommodationManage.StudentListItem | null>(null)

  /** 院系选项 */
  const deptOptions = ref<Api.SystemManage.DepartmentListItem[]>([])

  /** 专业选项 */
  const majorOptions = ref<Api.SystemManage.MajorListItem[]>([])

  /** 班级选项 */
  const classOptions = ref<Api.SystemManage.ClassListItem[]>([])

  // ==================== Computed ====================

  /**
   * 根据楼层性别类型获取性别过滤条件
   * 楼层性别：1-男生宿舍 → 只查男生(gender=1)
   *          2-女生宿舍 → 只查女生(gender=2)
   *          3-混合宿舍 → 不限制性别
   */
  const genderFilter = computed(() => {
    const genderType = props.roomInfo?.genderType
    if (genderType === 1) return 1 // 男生
    if (genderType === 2) return 2 // 女生
    return undefined // 混合或其他，不限制
  })

  /**
   * 宿舍性别类型文本
   */
  const dormitoryGenderText = computed(() => {
    const genderType = props.roomInfo?.genderType
    if (genderType === 1) return '（仅显示男生）'
    if (genderType === 2) return '（仅显示女生）'
    return ''
  })

  // ==================== Methods ====================

  /**
   * 加载学生列表
   */
  const loadStudentList = async () => {
    loading.value = true
    try {
      const res = await fetchGetStudentPage({
        ...searchParams,
        pageNum: pagination.pageNum,
        pageSize: pagination.pageSize,
        status: 1, // 只查询启用状态的学生
        gender: genderFilter.value // 根据宿舍类型过滤性别
      })

      studentList.value = res?.list || []
      pagination.total = res?.total || 0
    } catch (error) {
      console.error('加载学生列表失败:', error)
      studentList.value = []
    } finally {
      loading.value = false
    }
  }

  /**
   * 搜索
   */
  const handleSearch = () => {
    pagination.pageNum = 1
    selectedStudent.value = null
    loadStudentList()
  }

  /**
   * 重置搜索
   */
  const handleReset = () => {
    searchParams.studentNo = ''
    searchParams.studentName = ''
    searchParams.phone = ''
    searchParams.deptCode = ''
    searchParams.majorCode = ''
    searchParams.classId = undefined
    majorOptions.value = []
    classOptions.value = []
    handleSearch()
  }

  /**
   * 加载院系选项
   */
  const loadDeptOptions = async () => {
    try {
      const res = await fetchGetDepartmentPage({ status: 1, pageSize: 1000 })
      // 兼容 list 和 records 两种返回格式
      deptOptions.value = (res as any)?.list || res?.records || []
    } catch (error) {
      console.error('加载院系选项失败:', error)
      deptOptions.value = []
    }
  }

  /**
   * 加载专业选项
   */
  const loadMajorOptions = async (deptCode: string) => {
    if (!deptCode) {
      majorOptions.value = []
      return
    }
    try {
      const res = await fetchGetMajorPage({ deptCode, status: 1, pageSize: 1000 })
      // 兼容 list 和 records 两种返回格式
      majorOptions.value = (res as any)?.list || res?.records || []
    } catch (error) {
      console.error('加载专业选项失败:', error)
      majorOptions.value = []
    }
  }

  /**
   * 加载班级选项
   */
  const loadClassOptions = async (majorCode: string) => {
    if (!majorCode) {
      classOptions.value = []
      return
    }
    try {
      const res = await fetchGetClassPage({ majorCode, status: 1, pageSize: 1000 })
      // 兼容 list 和 records 两种返回格式
      classOptions.value = (res as any)?.list || res?.records || []
    } catch (error) {
      console.error('加载班级选项失败:', error)
      classOptions.value = []
    }
  }

  /**
   * 院系变更
   */
  const handleDeptChange = (deptCode: string) => {
    searchParams.majorCode = ''
    searchParams.classId = undefined
    classOptions.value = []
    if (deptCode) {
      loadMajorOptions(deptCode)
    } else {
      majorOptions.value = []
    }
  }

  /**
   * 专业变更
   */
  const handleMajorChange = (majorCode: string) => {
    searchParams.classId = undefined
    if (majorCode) {
      loadClassOptions(majorCode)
    } else {
      classOptions.value = []
    }
  }

  /**
   * 分页大小变化
   */
  const handleSizeChange = () => {
    pagination.pageNum = 1
    loadStudentList()
  }

  /**
   * 页码变化
   */
  const handleCurrentChange = () => {
    loadStudentList()
  }

  /**
   * 点击行选中/取消选中
   */
  const handleRowClick = (row: Api.AccommodationManage.StudentListItem) => {
    if (row.bedId) {
      ElMessage.warning('该学生已分配床位，请选择其他学生')
      return
    }
    // 点击已选中的行，取消选中
    if (selectedStudent.value?.id === row.id) {
      selectedStudent.value = null
    } else {
      selectedStudent.value = row
    }
  }

  /**
   * 点击选择/取消按钮
   */
  const handleSelect = (row: Api.AccommodationManage.StudentListItem) => {
    // 点击已选中的行，取消选中
    if (selectedStudent.value?.id === row.id) {
      selectedStudent.value = null
    } else {
      selectedStudent.value = row
    }
  }

  /**
   * 获取行样式类名
   */
  const getRowClassName = ({ row }: { row: Api.AccommodationManage.StudentListItem }) => {
    if (row.bedId) return 'row-disabled'
    if (selectedStudent.value?.id === row.id) return 'row-selected'
    return ''
  }

  /**
   * 确认分配
   */
  const handleConfirm = async () => {
    if (!selectedStudent.value || !props.bedInfo || !props.roomInfo) {
      ElMessage.warning('请选择要分配的学生')
      return
    }

    submitting.value = true
    try {
      await fetchAdminAssignBed({
        studentId: selectedStudent.value.id,
        bedId: props.bedInfo.id,
        roomId: props.roomInfo.id,
        roomCode: props.roomInfo.roomCode,
        bedCode: props.bedInfo.bedCode,
        checkInType: 1, // 入住类型：1-正常入住
        checkInDate: new Date().toISOString().split('T')[0] // 当前日期
      })

      ElMessage.success(
        `学生 ${selectedStudent.value.studentName} 已成功分配到 ${props.roomInfo.roomNumber}-${props.bedInfo.bedNumber}号床`
      )
      emit('success')
      handleClose()
    } catch (error) {
      console.error('分配床位失败:', error)
    } finally {
      submitting.value = false
    }
  }

  /**
   * 关闭弹窗
   */
  const handleClose = () => {
    dialogVisible.value = false
    // 重置状态
    selectedStudent.value = null
    searchParams.studentNo = ''
    searchParams.studentName = ''
    searchParams.phone = ''
    searchParams.deptCode = ''
    searchParams.majorCode = ''
    searchParams.classId = undefined
    majorOptions.value = []
    classOptions.value = []
    pagination.pageNum = 1
  }

  // ==================== Watch ====================

  // 监听弹窗显示，加载数据
  watch(
    () => props.visible,
    (val) => {
      if (val) {
        loadStudentList()
        loadDeptOptions()
      }
    }
  )
</script>

<style lang="scss" scoped>
  // 弹窗整体样式
  :deep(.student-select-dialog) {
    .el-dialog__body {
      padding: 0 24px 24px;
    }

    .el-dialog__header {
      padding: 20px 24px 16px;
      border-bottom: 1px solid var(--art-gray-200);
    }

    .el-dialog__footer {
      display: none;
    }
  }

  // 目标床位卡片
  .target-card {
    display: flex;
    gap: 16px;
    align-items: center;
    padding: 16px 20px;
    margin-bottom: 20px;
    background: linear-gradient(
      135deg,
      color-mix(in srgb, var(--theme-color) 8%, transparent) 0%,
      color-mix(in srgb, var(--theme-color) 4%, transparent) 100%
    );
    border: 1px solid color-mix(in srgb, var(--theme-color) 20%, transparent);
    border-radius: 12px;

    &__icon {
      display: flex;
      flex-shrink: 0;
      align-items: center;
      justify-content: center;
      width: 48px;
      height: 48px;
      color: white;
      background: var(--theme-color);
      border-radius: 10px;
    }

    &__info {
      flex: 1;
    }

    &__title {
      display: flex;
      gap: 8px;
      align-items: baseline;
      margin-bottom: 4px;
    }

    &__room {
      font-size: 20px;
      font-weight: 700;
      color: var(--art-gray-900);
    }

    &__bed {
      font-size: 15px;
      font-weight: 600;
      color: var(--theme-color);
    }

    &__meta {
      display: flex;
      gap: 16px;
      align-items: center;
    }

    &__location,
    &__gender {
      display: flex;
      align-items: center;
      font-size: 13px;
      color: var(--art-gray-600);
    }

    &__gender {
      font-weight: 500;
      color: var(--theme-color);
    }
  }

  // 搜索区域
  .search-section {
    margin-bottom: 16px;

    &__title {
      display: flex;
      align-items: center;
      margin-bottom: 12px;
      font-size: 13px;
      font-weight: 500;
      color: var(--art-gray-600);
    }

    &__form {
      display: flex;
      flex-wrap: wrap;
      gap: 12px;
      align-items: center;
      background: var(--art-gray-50);
      border-radius: 10px;
    }

    .search-row {
      display: flex;
      gap: 12px;
      align-items: center;
      width: 100%;
    }

    .search-input {
      flex: 1;
      min-width: 120px;
    }

    .search-select {
      flex: 1;
      min-width: 140px;
    }

    .search-actions {
      display: flex;
      gap: 8px;
      margin-left: auto;
    }
  }

  // 表格区域
  .table-section {
    overflow: hidden;
    border: 1px solid var(--art-gray-200);
    border-radius: 10px;
  }

  // 学生表格
  .student-table {
    :deep(.el-table__header-wrapper) {
      th {
        font-weight: 600;
        background: var(--art-gray-50) !important;
      }
    }

    :deep(.el-table__row) {
      cursor: pointer;
      transition: all 0.15s ease;
    }

    :deep(.row-disabled) {
      color: var(--art-gray-400);
      cursor: not-allowed;
      background-color: var(--art-gray-50);

      &:hover > td {
        background-color: var(--art-gray-50) !important;
      }
    }

    :deep(.row-selected) {
      background-color: color-mix(in srgb, var(--el-color-success) 10%, transparent);

      &:hover > td {
        background-color: color-mix(in srgb, var(--el-color-success) 15%, transparent) !important;
      }

      td {
        border-bottom-color: color-mix(
          in srgb,
          var(--el-color-success) 20%,
          transparent
        ) !important;
      }
    }
  }

  // 底部栏
  .footer-bar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-top: 20px;
    margin-top: 20px;
    border-top: 1px solid var(--art-gray-200);
  }

  .footer-actions {
    display: flex;
    gap: 12px;
    align-items: center;
  }

  // 选中信息
  .selected-info {
    display: flex;
    gap: 12px;
    align-items: center;
    padding: 10px 16px;
    background: linear-gradient(
      135deg,
      color-mix(in srgb, var(--el-color-success) 12%, transparent) 0%,
      color-mix(in srgb, var(--el-color-success) 6%, transparent) 100%
    );
    border: 1px solid color-mix(in srgb, var(--el-color-success) 25%, transparent);
    border-radius: 8px;

    &__icon {
      font-size: 20px;
      color: var(--el-color-success);
    }

    &__text {
      display: flex;
      gap: 6px;
      align-items: baseline;
    }

    &__label {
      font-size: 12px;
      font-weight: 500;
      color: var(--el-color-success);
    }

    &__name {
      font-size: 14px;
      font-weight: 600;
      color: var(--el-color-success-dark-2);
    }

    &__no {
      font-size: 12px;
      color: var(--art-gray-500);
    }
  }

  // 滑入动画
  .slide-fade-enter-active {
    transition: all 0.25s ease-out;
  }

  .slide-fade-leave-active {
    transition: all 0.2s ease-in;
  }

  .slide-fade-enter-from {
    opacity: 0;
    transform: translateX(20px);
  }

  .slide-fade-leave-to {
    opacity: 0;
    transform: translateX(-10px);
  }

  // 暗黑模式适配
  .dark {
    :deep(.student-select-dialog) {
      .el-dialog__header {
        border-color: var(--art-gray-300);
      }
    }

    .target-card {
      background: linear-gradient(
        135deg,
        color-mix(in srgb, var(--theme-color) 15%, transparent) 0%,
        color-mix(in srgb, var(--theme-color) 8%, transparent) 100%
      );
      border-color: color-mix(in srgb, var(--theme-color) 30%, transparent);

      &__room {
        color: var(--art-gray-100);
      }
    }

    .search-section {
      &__form {
        background: var(--art-gray-200);
      }
    }

    .table-section {
      border-color: var(--art-gray-300);
    }

    .student-table {
      :deep(.el-table__header-wrapper) {
        th {
          background: var(--art-gray-200) !important;
        }
      }

      :deep(.row-disabled) {
        background-color: var(--art-gray-200);

        &:hover > td {
          background-color: var(--art-gray-200) !important;
        }
      }
    }

    .footer-bar {
      border-color: var(--art-gray-300);
    }

    .selected-info {
      &__name {
        color: var(--el-color-success-light-3);
      }
    }
  }
</style>
