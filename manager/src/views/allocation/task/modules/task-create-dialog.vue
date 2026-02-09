<template>
  <ElDialog
    v-model="dialogVisible"
    :title="isEditMode ? '编辑分配任务' : '创建分配任务'"
    width="80%"
    top="5vh"
    align-center
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div v-loading="formLoading" class="grid grid-cols-[1fr_400px] gap-5 max-h-[75vh] overflow-y-auto">
      <!-- 左侧：任务配置表单 -->
      <div class="flex flex-col gap-5">
        <!-- 基本信息 -->
        <div class="art-card-sm task-card">
          <div class="task-card__header">
            <div class="flex items-center gap-2">
              <div class="w-1 h-5 rounded-full" style="background: linear-gradient(to bottom, var(--el-color-primary), var(--el-color-primary-light-3))"></div>
              <span class="task-card__title">基本信息</span>
            </div>
            <span class="task-card__desc">配置任务的基本信息</span>
          </div>

          <ElForm
            ref="formRef"
            :model="formData"
            :rules="formRules"
            label-width="110px"
            label-position="left"
          >
            <ElFormItem prop="taskName">
              <template #label>
                <div class="flex items-center gap-1.5">
                  <ArtSvgIcon icon="ri:file-text-line" class="text-base" style="color: var(--el-color-primary)" />
                  <span>任务名称</span>
                </div>
              </template>
              <ElInput
                v-model="formData.taskName"
                placeholder="请输入任务名称，如：2026级新生批量分配"
                maxlength="100"
                show-word-limit
                class="w-full"
              />
            </ElFormItem>

            <ElFormItem prop="taskType">
              <template #label>
                <div class="flex items-center gap-1.5">
                  <ArtSvgIcon icon="ri:settings-3-line" class="text-base" style="color: var(--el-color-primary)" />
                  <span>任务类型</span>
                </div>
              </template>
              <ElRadioGroup v-model="formData.taskType" class="w-full">
                <div class="flex flex-wrap gap-3">
                  <div
                    v-for="item in taskTypeOptions"
                    :key="item.value"
                    class="task-type-option"
                    :class="{ 'task-type-option--active': formData.taskType === item.value }"
                    @click="formData.taskType = item.value"
                  >
                    <ElRadio :value="item.value" class="w-full !mr-0">
                      <div class="flex items-center justify-center gap-3">
                        <div class="task-type-option__icon"
                             :class="{ 'task-type-option__icon--active': formData.taskType === item.value }">
                          <ArtSvgIcon :icon="item.icon" class="text-xl" />
                        </div>
                        <div class="flex flex-col">
                          <span class="task-type-option__label">{{ item.label }}</span>
                          <span class="task-type-option__desc">{{ item.desc }}</span>
                        </div>
                      </div>
                    </ElRadio>
                  </div>
                </div>
              </ElRadioGroup>
            </ElFormItem>

            <ElFormItem prop="configId">
              <template #label>
                <div class="flex items-center gap-1.5">
                  <ArtSvgIcon icon="ri:settings-4-line" class="text-base" style="color: var(--el-color-primary)" />
                  <span>分配配置</span>
                </div>
              </template>
              <ElSelect
                v-model="formData.configId"
                placeholder="请选择分配配置"
                class="w-full"
                @change="handleConfigChange"
              >
                <ElOption
                  v-for="item in configOptions"
                  :key="item.id"
                  :label="item.configName"
                  :value="item.id"
                >
                  <div class="flex justify-between items-center w-full">
                    <span>{{ item.configName }}</span>
                    <ElTag size="small" type="primary" effect="light">{{ item.algorithmTypeName || item.algorithmType }}</ElTag>
                  </div>
                </ElOption>
              </ElSelect>
            </ElFormItem>
          </ElForm>
        </div>

        <!-- 学生筛选条件 -->
        <div class="art-card-sm task-card">
          <div class="task-card__header">
            <div class="flex items-center gap-2">
              <div class="w-1 h-5 rounded-full" style="background: linear-gradient(to bottom, var(--el-color-primary), var(--el-color-primary-light-3))"></div>
              <span class="task-card__title">学生筛选条件</span>
            </div>
            <span class="task-card__desc">设置要分配的学生范围（可选）</span>
          </div>

          <ElForm :model="formData" label-width="110px" label-position="left">
            <ElFormItem>
              <template #label>
                <div class="flex items-center gap-1.5">
                  <ArtSvgIcon icon="ri:calendar-line" class="text-base" style="color: var(--el-color-primary)" />
                  <span>入学年份</span>
                </div>
              </template>
              <ElSelect v-model="formData.targetEnrollmentYear" placeholder="不限" clearable class="w-full">
                <ElOption
                  v-for="item in academicYearOptions"
                  :key="item.id"
                  :label="item.yearName"
                  :value="Number(item.yearCode.split('-')[0])"
                />
              </ElSelect>
            </ElFormItem>

            <ElFormItem>
              <template #label>
                <div class="flex items-center gap-1.5">
                  <ArtSvgIcon icon="ri:group-line" class="text-base" style="color: var(--el-color-primary)" />
                  <span>性别</span>
                </div>
              </template>
              <ElRadioGroup v-model="formData.targetGender">
                <ElRadioButton
                  v-for="item in genderOptions"
                  :key="item.value"
                  :value="item.value"
                >
                  {{ item.label }}
                </ElRadioButton>
              </ElRadioGroup>
            </ElFormItem>

            <ElFormItem>
              <template #label>
                <div class="flex items-center gap-1.5">
                  <ArtSvgIcon icon="ri:building-2-line" class="text-base" style="color: var(--el-color-primary)" />
                  <span>校区</span>
                </div>
              </template>
              <ElSelect v-model="formData.targetCampusCode" placeholder="不限" clearable class="w-full">
                <ElOption
                  v-for="item in campusOptions"
                  :key="item.campusCode"
                  :label="item.campusName"
                  :value="item.campusCode"
                />
              </ElSelect>
            </ElFormItem>

            <ElFormItem>
              <template #label>
                <div class="flex items-center gap-1.5">
                  <ArtSvgIcon icon="ri:building-line" class="text-base" style="color: var(--el-color-primary)" />
                  <span>院系</span>
                </div>
              </template>
              <ElSelect v-model="formData.targetDeptCode" placeholder="不限" clearable class="w-full">
                <ElOption
                  v-for="item in deptOptions"
                  :key="item.deptCode"
                  :label="item.deptName"
                  :value="item.deptCode"
                />
              </ElSelect>
            </ElFormItem>

            <ElFormItem>
              <template #label>
                <div class="flex items-center gap-1.5">
                  <ArtSvgIcon icon="ri:book-open-line" class="text-base" style="color: var(--el-color-primary)" />
                  <span>专业</span>
                </div>
              </template>
              <ElSelect v-model="formData.targetMajorCode" placeholder="不限" clearable class="w-full">
                <ElOption
                  v-for="item in majorOptions"
                  :key="item.majorCode"
                  :label="item.majorName"
                  :value="item.majorCode"
                />
              </ElSelect>
            </ElFormItem>
          </ElForm>
        </div>

        <!-- 目标床位范围 -->
        <div class="art-card-sm task-card">
          <div class="task-card__header">
            <div class="flex items-center gap-2">
              <div class="w-1 h-5 rounded-full" style="background: linear-gradient(to bottom, var(--el-color-primary), var(--el-color-primary-light-3))"></div>
              <span class="task-card__title">目标床位范围</span>
            </div>
            <span class="task-card__desc">选择可分配的楼层范围（可选）</span>
          </div>

          <ElForm :model="formData" label-width="110px" label-position="left">
            <ElFormItem>
              <template #label>
                <div class="flex items-center gap-1.5">
                  <ArtSvgIcon icon="ri:home-line" class="text-base" style="color: var(--el-color-primary)" />
                  <span>目标楼层</span>
                </div>
              </template>
              <ElSelect
                v-model="formData.targetFloorIds"
                multiple
                placeholder="不限（可选择多个楼层）"
                clearable
                class="w-full"
              >
                <ElOption
                  v-for="item in floorOptions"
                  :key="item.id"
                  :label="`${item.floorName || item.floorCode}`"
                  :value="item.id"
                >
                  <div class="flex items-center justify-between w-full">
                    <span>{{ item.floorName || item.floorCode }}</span>
                    <ElTag
                      v-if="item.genderType"
                      :type="getGenderTagType(item.genderType)"
                      size="small"
                    >
                      {{ item.genderTypeText || getGenderLabel(item.genderType) }}
                    </ElTag>
                  </div>
                </ElOption>
              </ElSelect>
            </ElFormItem>
          </ElForm>
        </div>

        <!-- 备注 -->
        <div class="art-card-sm task-card">
          <div class="task-card__header">
            <div class="flex items-center gap-2">
              <div class="w-1 h-5 rounded-full" style="background: linear-gradient(to bottom, var(--el-color-primary), var(--el-color-primary-light-3))"></div>
              <span class="task-card__title">备注</span>
            </div>
            <span class="task-card__desc">任务的补充说明（可选）</span>
          </div>

          <ElForm :model="formData" label-width="0">
            <ElFormItem label="">
              <ElInput
                v-model="formData.remark"
                type="textarea"
                :rows="3"
                placeholder="请输入备注信息（可选）"
                maxlength="500"
                show-word-limit
                class="w-full"
              />
            </ElFormItem>
          </ElForm>
        </div>
      </div>

      <!-- 右侧：配置详情和预览 -->
      <div class="flex flex-col gap-5 sticky top-0">
        <!-- 配置详情卡片 -->
        <div v-if="selectedConfig" class="art-card-sm sidebar-card">
          <div class="sidebar-card__header">
            <ArtSvgIcon icon="ri:settings-4-line" class="sidebar-card__header-icon" />
            <span>配置详情</span>
          </div>
          <div class="sidebar-card__body">
            <!-- 配置名称 -->
            <div class="config-field">
              <span class="config-field__label">配置名称</span>
              <span class="config-field__value">{{ selectedConfig.configName }}</span>
            </div>
            <!-- 算法类型 -->
            <div class="config-field">
              <span class="config-field__label">算法类型</span>
              <div class="config-algo">
                <ArtSvgIcon icon="ri:cpu-line" class="config-algo__icon" />
                <span>{{ selectedConfig.algorithmTypeName || selectedConfig.algorithmType }}</span>
              </div>
            </div>
            <!-- 硬约束 -->
            <div class="config-field">
              <span class="config-field__label">硬约束</span>
              <div class="config-constraints">
                <span v-if="selectedConfig.smokingConstraint" class="constraint-badge constraint-badge--warning">
                  <ArtSvgIcon icon="ri:fire-line" class="text-xs" /> 吸烟
                </span>
                <span v-if="selectedConfig.genderConstraint" class="constraint-badge constraint-badge--success">
                  <ArtSvgIcon icon="ri:user-line" class="text-xs" /> 性别
                </span>
                <span v-if="selectedConfig.sleepHardConstraint" class="constraint-badge constraint-badge--info">
                  <ArtSvgIcon icon="ri:moon-line" class="text-xs" /> 作息
                </span>
                <span v-if="!selectedConfig.smokingConstraint && !selectedConfig.genderConstraint && !selectedConfig.sleepHardConstraint" class="text-xs text-[var(--el-text-color-placeholder)]">无约束</span>
              </div>
            </div>
            <!-- 最低匹配分 -->
            <div class="config-field config-field--score">
              <span class="config-field__label">最低匹配分</span>
              <div class="config-score">
                <span class="config-score__value">{{ selectedConfig.minMatchScore }}</span>
                <span class="config-score__unit">分</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 预览结果卡片 -->
        <div class="art-card-sm sidebar-card" v-loading="previewLoading">
          <div class="sidebar-card__header sidebar-card__header--with-action">
            <div class="flex items-center gap-2">
              <ArtSvgIcon icon="ri:eye-line" class="sidebar-card__header-icon" />
              <span>预览结果</span>
            </div>
            <ElButton
              link
              type="primary"
              size="small"
              @click="handlePreview"
              :disabled="!formData.configId"
            >
              <ArtSvgIcon icon="ri:refresh-line" class="mr-1" />
              刷新
            </ElButton>
          </div>

          <template v-if="previewData">
            <div class="sidebar-card__body">
              <!-- 数据概览：两列网格 -->
              <div class="preview-grid">
                <div class="preview-metric">
                  <div class="preview-metric__icon preview-metric__icon--primary">
                    <ArtSvgIcon icon="ri:user-line" />
                  </div>
                  <div class="preview-metric__value">{{ previewData.toBeAllocatedCount ?? previewData.totalStudents ?? 0 }}</div>
                  <div class="preview-metric__label">待分配学生</div>
                </div>
                <div class="preview-metric">
                  <div class="preview-metric__icon preview-metric__icon--success">
                    <ArtSvgIcon icon="ri:hotel-bed-line" />
                  </div>
                  <div class="preview-metric__value">{{ previewData.totalAvailableBeds ?? 0 }}</div>
                  <div class="preview-metric__label">可用床位</div>
                </div>
              </div>

              <!-- 床位状态横条 -->
              <div class="preview-status" :class="previewData.canExecute ? 'preview-status--ok' : 'preview-status--warn'">
                <ArtSvgIcon :icon="previewData.canExecute ? 'ri:checkbox-circle-line' : 'ri:error-warning-line'" class="text-base" />
                <span>床位{{ previewData.canExecute ? '充足，可以执行分配' : '不足，无法执行分配' }}</span>
              </div>

              <!-- 床位使用率 -->
              <div v-if="(previewData.toBeAllocatedCount ?? previewData.totalStudents ?? 0) > 0 && (previewData.totalAvailableBeds ?? 0) > 0" class="preview-usage">
                <div class="flex items-center justify-between mb-1.5">
                  <span class="preview-usage__label">床位使用率</span>
                  <span class="preview-usage__value">{{ Math.min(Math.round(((previewData.toBeAllocatedCount ?? previewData.totalStudents ?? 0) / (previewData.totalAvailableBeds ?? 1)) * 100), 100) }}%</span>
                </div>
                <ElProgress
                  :percentage="Math.min(Math.round(((previewData.toBeAllocatedCount ?? previewData.totalStudents ?? 0) / (previewData.totalAvailableBeds ?? 1)) * 100), 100)"
                  :color="previewData.canExecute ? '#10b981' : '#ef4444'"
                  :show-text="false"
                  :stroke-width="6"
                />
              </div>

              <!-- 警告信息 -->
              <div v-if="(previewData.surveyUnfilledCount ?? 0) > 0 || previewData.cannotExecuteReason" class="preview-alerts">
                <div v-if="(previewData.surveyUnfilledCount ?? 0) > 0" class="preview-alert">
                  <ArtSvgIcon icon="ri:error-warning-line" class="text-sm" />
                  <span>{{ previewData.surveyUnfilledCount }} 名学生未填写问卷</span>
                </div>
                <div v-if="previewData.cannotExecuteReason" class="preview-alert preview-alert--error">
                  <ArtSvgIcon icon="ri:close-circle-line" class="text-sm" />
                  <span>{{ previewData.cannotExecuteReason }}</span>
                </div>
              </div>
            </div>
          </template>

          <template v-else>
            <div class="text-center py-16">
              <ArtSvgIcon icon="ri:file-list-line" class="text-6xl empty-icon mb-4" />
              <div class="empty-text">请选择配置后点击预览</div>
            </div>
          </template>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="flex gap-3 justify-end">
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton @click="handlePreview" :loading="previewLoading" :disabled="!formData.configId">
          预览
        </ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ submitLoading ? '提交中...' : isEditMode ? '保存修改' : '创建任务' }}
        </ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'
  import {
    fetchGetConfigPage,
    fetchAddTask,
    fetchUpdateTask,
    fetchPreviewTask
  } from '@/api/allocation-manage'
  import {
    fetchGetCampusPage,
    fetchGetDepartmentPage,
    fetchGetMajorPage,
    fetchGetAcademicYearPage
  } from '@/api/school-manage'
  import { fetchGetFloorPage } from '@/api/dormitory-manage'
  import { useDictStore } from '@/store/modules/dict'

  type TaskListItem = Api.Allocation.TaskListItem

  interface Props {
    visible: boolean
    task?: TaskListItem
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'submit'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  // 使用字典 store
  const dictStore = useDictStore()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (val) => emit('update:visible', val)
  })

  const isEditMode = computed(() => !!props.task?.id)

  const formRef = ref<FormInstance>()
  const submitLoading = ref(false)
  const previewLoading = ref(false)
  const formLoading = ref(false)
  // 编辑模式初始化标记，防止级联 watch 清空回显值
  let isInitializing = false

  // 表单数据
  const formData = ref({
    taskName: '',
    taskType: 1,
    configId: undefined as number | undefined,
    targetEnrollmentYear: undefined as number | undefined,
    targetGender: '',
    targetCampusCode: '',
    targetDeptCode: '',
    targetMajorCode: '',
    targetFloorIds: [] as number[],
    remark: ''
  })

  // 表单验证规则
  const formRules: FormRules = {
    taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
    taskType: [{ required: true, message: '请选择任务类型', trigger: 'change' }],
    configId: [{ required: true, message: '请选择分配配置', trigger: 'change' }]
  }

  // 选项数据
  const configOptions = ref<Api.Allocation.ConfigListItem[]>([])
  const academicYearOptions = ref<any[]>([])
  const campusOptions = ref<any[]>([])
  const deptOptions = ref<any[]>([])
  const majorOptions = ref<any[]>([])
  const floorOptions = ref<any[]>([])
  const genderOptions = ref<Array<{ label: string; value: string }>>([])

  // 预览数据
  const previewData = ref<any>(null)

  // 任务类型选项
  const taskTypeOptions = [
    { label: '批量分配', value: 1, desc: '为指定范围内的学生批量分配床位', icon: 'ri:group-line' },
    { label: '单个推荐', value: 2, desc: '为单个学生推荐最佳床位', icon: 'ri:user-star-line' },
    { label: '调宿优化', value: 3, desc: '优化现有学生的住宿安排', icon: 'ri:refresh-line' }
  ]

  // 获取选中配置的详情
  const selectedConfig = computed(() => {
    return configOptions.value.find((c) => c.id === formData.value.configId)
  })

  // 获取配置列表
  const loadConfigList = async () => {
    try {
      const data = await fetchGetConfigPage({ pageNum: 1, pageSize: 100, status: 1 })
      configOptions.value = (data as any)?.list || []
    } catch (e) {
      console.error('获取配置列表失败', e)
    }
  }

  // 获取学年列表
  const loadAcademicYearList = async () => {
    try {
      const res = await fetchGetAcademicYearPage({ pageSize: 100 })
      academicYearOptions.value = (res as any)?.list || []
    } catch (e) {
      console.error('获取学年列表失败', e)
    }
  }

  // 加载字典数据
  const loadDictData = async () => {
    try {
      const dictRes = await dictStore.loadDictDataBatch(['dormitory_gender_type'])
      // 解析性别字典，添加"不限"选项
      const dictOptions = (dictRes.dormitory_gender_type || []).map(
        (item: Api.SystemManage.DictDataListItem) => ({
          label: item.label,
          value: item.value
        })
      )
      genderOptions.value = [...dictOptions]
    } catch (e) {
      console.error('加载字典数据失败', e)
    }
  }

  /** 获取性别类型的 Tag 类型 */
  type TagType = 'success' | 'warning' | 'danger' | 'info' | 'primary'
  const getGenderTagType = (genderType: number): TagType | undefined => {
    const dictData = dictStore.getDictData('dormitory_gender_type')
    const item = dictData.find((d) => d.value === String(genderType))
    const tagType = item?.listClass
    // 验证是否为有效的 tag 类型
    if (tagType && ['success', 'warning', 'danger', 'info', 'primary'].includes(tagType)) {
      return tagType as TagType
    }
    return undefined
  }

  /** 获取性别类型的标签文本 */
  const getGenderLabel = (genderType: number): string => {
    const dictData = dictStore.getDictData('dormitory_gender_type')
    const item = dictData.find((d) => d.value === String(genderType))
    return item?.label || ''
  }

  // 获取校区列表
  const loadCampusList = async () => {
    try {
      const res = await fetchGetCampusPage({ pageSize: 100, status: 1 })
      campusOptions.value = (res as any)?.list || []
    } catch (e) {
      console.error('获取校区列表失败', e)
    }
  }

  // 获取院系列表
  const loadDeptList = async () => {
    try {
      const params: any = { pageSize: 100, status: 1 }
      if (formData.value.targetCampusCode) {
        params.campusCode = formData.value.targetCampusCode
      }
      const res = await fetchGetDepartmentPage(params)
      deptOptions.value = (res as any)?.list || []
    } catch (e) {
      console.error('获取院系列表失败', e)
    }
  }

  // 获取专业列表
  const loadMajorList = async () => {
    try {
      const params: any = { pageSize: 100, status: 1 }
      if (formData.value.targetDeptCode) {
        params.deptCode = formData.value.targetDeptCode
      }
      const res = await fetchGetMajorPage(params)
      majorOptions.value = (res as any)?.list || []
    } catch (e) {
      console.error('获取专业列表失败', e)
    }
  }

  // 获取楼层列表
  const loadFloorList = async () => {
    try {
      const params: any = { pageSize: 100, status: 1 }
      if (formData.value.targetCampusCode) {
        params.campusCode = formData.value.targetCampusCode
      }
      const res = await fetchGetFloorPage(params)
      floorOptions.value = (res as any)?.list || []
    } catch (e) {
      console.error('获取楼层列表失败', e)
    }
  }

  // 监听校区变化 - 级联加载院系和楼层
  watch(
    () => formData.value.targetCampusCode,
    (val) => {
      // 编辑模式初始化时不清空下级值
      if (isInitializing) return
      // 清空下级选项
      formData.value.targetDeptCode = ''
      formData.value.targetMajorCode = ''
      formData.value.targetFloorIds = []
      deptOptions.value = []
      majorOptions.value = []
      floorOptions.value = []
      // 加载楼层（选择校区时加载该校区楼层，不选时加载全部楼层）
      loadFloorList()
      // 选择校区后才加载院系
      if (val) {
        loadDeptList()
      }
    }
  )

  // 监听院系变化 - 级联加载专业
  watch(
    () => formData.value.targetDeptCode,
    (val) => {
      // 编辑模式初始化时不清空下级值
      if (isInitializing) return
      // 清空下级选项
      formData.value.targetMajorCode = ''
      majorOptions.value = []
      // 选择院系后才加载专业
      if (val) {
        loadMajorList()
      }
    }
  )

  // 配置变化时清空预览数据
  const handleConfigChange = () => {
    previewData.value = null
  }

  // 预览分配
  const handlePreview = async () => {
    try {
      await formRef.value?.validate()
    } catch {
      return
    }

    previewLoading.value = true
    try {
      const data = await fetchPreviewTask({
        configId: formData.value.configId!,
        taskType: formData.value.taskType,
        targetEnrollmentYear: formData.value.targetEnrollmentYear,
        targetGender: formData.value.targetGender || undefined,
        targetCampusCode: formData.value.targetCampusCode || undefined,
        targetDeptCode: formData.value.targetDeptCode || undefined,
        targetMajorCode: formData.value.targetMajorCode || undefined,
        targetFloorIds:
          formData.value.targetFloorIds.length > 0 ? formData.value.targetFloorIds : undefined
      })
      previewData.value = data
    } catch (e) {
      console.error('预览失败', e)
    } finally {
      previewLoading.value = false
    }
  }

  // 提交创建/编辑
  const handleSubmit = async () => {
    try {
      await formRef.value?.validate()
    } catch {
      return
    }

    submitLoading.value = true
    try {
      const saveData: Api.Allocation.TaskSaveParams = {
        taskName: formData.value.taskName,
        taskType: formData.value.taskType,
        configId: formData.value.configId!,
        targetEnrollmentYear: formData.value.targetEnrollmentYear,
        targetGender: formData.value.targetGender || undefined,
        targetCampusCode: formData.value.targetCampusCode || undefined,
        targetDeptCode: formData.value.targetDeptCode || undefined,
        targetMajorCode: formData.value.targetMajorCode || undefined,
        targetFloorIds:
          formData.value.targetFloorIds.length > 0 ? formData.value.targetFloorIds : undefined,
        remark: formData.value.remark || undefined
      }

      if (isEditMode.value && props.task?.id) {
        await fetchUpdateTask(props.task.id, saveData)
      } else {
        await fetchAddTask(saveData)
      }
      dialogVisible.value = false
      emit('submit')
    } catch (e) {
      console.error('创建任务失败', e)
    } finally {
      submitLoading.value = false
    }
  }

  const handleClose = () => {
    formRef.value?.resetFields()
    previewData.value = null
  }

  // 监听visible变化，加载数据
  watch(
    () => props.visible,
    async (val) => {
      if (val) {
        formLoading.value = true
        previewData.value = null

        // 编辑模式：从 task prop 填充表单，并按级联顺序加载选项
        if (props.task?.id) {
          // 标记初始化中，防止 watch 清空回显值
          isInitializing = true

          formData.value = {
            taskName: props.task.taskName || '',
            taskType: props.task.taskType || 1,
            configId: props.task.configId,
            targetEnrollmentYear: props.task.targetEnrollmentYear,
            targetGender: props.task.targetGender || '',
            targetCampusCode: props.task.targetCampusCode || '',
            targetDeptCode: props.task.targetDeptCode || '',
            targetMajorCode: props.task.targetMajorCode || '',
            targetFloorIds: props.task.targetFloorIds || [],
            remark: (props.task as any).remark || ''
          }

          // 并行加载基础数据
          await Promise.all([
            loadConfigList(),
            loadAcademicYearList(),
            loadDictData(),
            loadCampusList(),
            loadFloorList()
          ])

          // 按级联顺序加载：院系 -> 专业（确保下拉选项可用才能回显）
          if (formData.value.targetCampusCode) {
            await loadDeptList()
          }
          if (formData.value.targetDeptCode) {
            await loadMajorList()
          }

          // 初始化完成，恢复 watch 正常行为
          isInitializing = false
        } else {
          formData.value = {
            taskName: '',
            taskType: 1,
            configId: undefined,
            targetEnrollmentYear: undefined,
            targetGender: '',
            targetCampusCode: '',
            targetDeptCode: '',
            targetMajorCode: '',
            targetFloorIds: [],
            remark: ''
          }
          // 清空下级选项
          deptOptions.value = []
          majorOptions.value = []
          floorOptions.value = []
          // 并行加载基础数据
          await Promise.all([
            loadConfigList(),
            loadAcademicYearList(),
            loadDictData(),
            loadCampusList(),
            loadFloorList()
          ])
        }

        nextTick(() => {
          formRef.value?.clearValidate()
        })
        formLoading.value = false
      }
    }
  )
</script>

<style scoped lang="scss">
  :deep(.el-radio) {
    width: 100%;
    height: auto;
    margin-right: 0;
  }

  :deep(.el-radio__label) {
    width: 100%;
    display: flex;
    justify-content: center;
  }

  // 通用卡片样式（继承系统 art-card-sm 的边框/阴影/圆角）
  .task-card {
    padding: 20px;

    &__header {
      margin-bottom: 16px;
      padding-bottom: 12px;
      border-bottom: 1px solid var(--el-border-color-lighter);
    }

    &__title {
      font-size: 16px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }

    &__desc {
      font-size: 12px;
      color: var(--el-text-color-secondary);
      margin-top: 4px;
      display: block;
      margin-left: 12px;
    }
  }

  // 任务类型选项样式
  .task-type-option {
    background: var(--el-bg-color);
    border-radius: 8px;
    padding: 2px 10px;
    border: 2px solid var(--el-border-color);
    transition: all 0.3s;
    cursor: pointer;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);

    &:hover:not(.task-type-option--active) {
      border-color: var(--el-color-primary-light-5);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
      transform: translateY(-2px);
    }

    &--active {
      border-color: var(--el-color-primary);
      background: var(--el-color-primary-light-9);
      box-shadow: 0 4px 12px rgba(var(--el-color-primary-rgb), 0.15);
    }

    &__icon {
      width: 40px;
      height: 40px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.3s;
      background: var(--el-fill-color-light);
      color: var(--el-text-color-secondary);

      &--active {
        background: var(--el-color-primary);
        color: #fff;
      }
    }

    &__label {
      font-weight: 500;
      color: var(--el-text-color-primary);
    }

    &__desc {
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }
  }

  // 预览卡片样式（继承系统 art-card-sm 的边框/阴影/圆角）
  // ========== 右侧侧边卡片 ==========
  .sidebar-card {
    overflow: hidden;

    &__header {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 14px 20px;
      font-size: 14px;
      font-weight: 600;
      color: var(--el-text-color-primary);
      border-bottom: 1px solid var(--el-border-color-extra-light);

      &--with-action {
        justify-content: space-between;
      }
    }

    &__header-icon {
      font-size: 16px;
      color: var(--el-color-primary);
    }

    &__body {
      display: flex;
      flex-direction: column;
      gap: 16px;
      padding: 20px;
    }
  }

  // ========== 配置详情字段 ==========
  .config-field {
    display: flex;
    flex-direction: column;
    gap: 6px;

    &__label {
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }

    &__value {
      font-size: 14px;
      font-weight: 500;
      color: var(--el-text-color-primary);
    }

    &--score {
      padding-top: 4px;
      border-top: 1px dashed var(--el-border-color-extra-light);
    }
  }

  .config-algo {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 5px 12px;
    font-size: 13px;
    font-weight: 500;
    color: var(--el-color-primary);
    background-color: rgba(59, 130, 246, 0.06);
    border-radius: 6px;
    width: fit-content;

    &__icon {
      font-size: 14px;
    }
  }

  .config-constraints {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
  }

  .constraint-badge {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 3px 10px;
    font-size: 12px;
    font-weight: 500;
    border-radius: 9999px;

    &--warning {
      color: #d97706;
      background-color: rgba(245, 158, 11, 0.08);
    }

    &--success {
      color: #059669;
      background-color: rgba(16, 185, 129, 0.08);
    }

    &--info {
      color: #6b7280;
      background-color: rgba(107, 114, 128, 0.08);
    }
  }

  .config-score {
    display: flex;
    align-items: baseline;
    gap: 2px;

    &__value {
      font-size: 28px;
      font-weight: 700;
      line-height: 1;
      color: var(--el-color-primary);
    }

    &__unit {
      font-size: 13px;
      color: var(--el-text-color-secondary);
    }
  }

  // ========== 预览数据网格 ==========
  .preview-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 12px;
  }

  .preview-metric {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6px;
    padding: 16px 12px;
    border-radius: 10px;
    background-color: var(--el-fill-color-extra-light);
    text-align: center;

    &__icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 36px;
      height: 36px;
      font-size: 18px;
      border-radius: 8px;

      &--primary {
        color: #2563eb;
        background-color: rgba(59, 130, 246, 0.1);
      }

      &--success {
        color: #059669;
        background-color: rgba(16, 185, 129, 0.1);
      }
    }

    &__value {
      font-size: 24px;
      font-weight: 700;
      line-height: 1;
      color: var(--el-text-color-primary);
    }

    &__label {
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }
  }

  // 状态横条
  .preview-status {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 14px;
    font-size: 13px;
    font-weight: 500;
    border-radius: 8px;

    &--ok {
      color: #059669;
      background-color: rgba(16, 185, 129, 0.06);
    }

    &--warn {
      color: #dc2626;
      background-color: rgba(239, 68, 68, 0.06);
    }
  }

  // 使用率
  .preview-usage {
    &__label {
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }

    &__value {
      font-size: 12px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }
  }

  // 警告信息
  .preview-alerts {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .preview-alert {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 12px;
    font-size: 12px;
    color: #d97706;
    background-color: rgba(245, 158, 11, 0.06);
    border-radius: 6px;

    &--error {
      color: #dc2626;
      background-color: rgba(239, 68, 68, 0.06);
    }
  }

  // 空状态
  .empty-icon {
    color: var(--el-text-color-placeholder);
  }

  .empty-text {
    font-size: 14px;
    color: var(--el-text-color-placeholder);
  }
</style>
