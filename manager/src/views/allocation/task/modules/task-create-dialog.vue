<template>
  <ElDialog
    v-model="dialogVisible"
    title="创建分配任务"
    width="80%"
    top="5vh"
    align-center
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="grid grid-cols-[1fr_400px] gap-5 max-h-[75vh] overflow-y-auto">
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
        <div v-if="selectedConfig" class="art-card-sm preview-card preview-card--config">
          <div class="preview-card__header">
            <div class="flex items-center gap-2">
              <ArtSvgIcon icon="ri:settings-4-line" class="text-lg" style="color: var(--el-color-primary)" />
              <span class="preview-card__title">配置详情</span>
            </div>
          </div>
          <div class="p-5 space-y-4">
            <div class="flex flex-col gap-2">
              <span class="preview-card__label">配置名称</span>
              <span class="preview-card__value">{{ selectedConfig.configName }}</span>
            </div>
            <div class="flex flex-col gap-2">
              <span class="preview-card__label">算法类型</span>
              <ElTag size="small" type="primary">{{ selectedConfig.algorithmTypeName || selectedConfig.algorithmType }}</ElTag>
            </div>
            <div class="flex flex-col gap-2">
              <span class="preview-card__label">硬约束</span>
              <div class="flex flex-wrap gap-1.5">
                <ElTag v-if="selectedConfig.smokingConstraint" size="small" type="warning">吸烟</ElTag>
                <ElTag v-if="selectedConfig.genderConstraint" size="small" type="success">性别</ElTag>
                <ElTag v-if="selectedConfig.sleepHardConstraint" size="small" type="info">作息</ElTag>
                <span v-if="!selectedConfig.smokingConstraint && !selectedConfig.genderConstraint && !selectedConfig.sleepHardConstraint" class="preview-card__empty">无</span>
              </div>
            </div>
            <div class="flex flex-col gap-2">
              <span class="preview-card__label">最低匹配分</span>
              <div class="flex items-center gap-2">
                <span class="text-2xl font-bold" style="color: var(--el-color-primary)">{{ selectedConfig.minMatchScore }}</span>
                <span class="preview-card__unit">分</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 预览结果卡片 -->
        <div class="art-card-sm preview-card preview-card--preview" v-loading="previewLoading">
          <div class="preview-card__header preview-card__header--with-action">
            <div class="flex items-center gap-2">
              <ArtSvgIcon icon="ri:eye-line" class="text-lg" style="color: var(--el-color-primary)" />
              <span class="preview-card__title">预览结果</span>
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
            <div class="p-5 space-y-3">
              <!-- 待分配学生 -->
              <div class="stat-item stat-item--primary">
                <div class="stat-item__icon stat-item__icon--primary">
                  <ArtSvgIcon icon="ri:user-line" class="text-2xl" />
                </div>
                <div class="flex flex-col gap-1 flex-1">
                  <span class="stat-item__label">待分配学生</span>
                  <span class="stat-item__value stat-item__value--primary">{{ previewData.totalStudents || 0 }}</span>
                </div>
              </div>

              <!-- 可用床位 -->
              <div class="stat-item stat-item--success">
                <div class="stat-item__icon stat-item__icon--success">
                  <ArtSvgIcon icon="ri:hotel-bed-line" class="text-2xl" />
                </div>
                <div class="flex flex-col gap-1 flex-1">
                  <span class="stat-item__label">可用床位</span>
                  <span class="stat-item__value stat-item__value--success">{{ previewData.totalBeds || 0 }}</span>
                </div>
              </div>

              <!-- 床位状态 -->
              <div class="stat-item" :class="previewData.canAllocate ? 'stat-item--success' : 'stat-item--danger'">
                <div class="stat-item__icon" :class="previewData.canAllocate ? 'stat-item__icon--success' : 'stat-item__icon--danger'">
                  <ArtSvgIcon :icon="previewData.canAllocate ? 'ri:checkbox-circle-line' : 'ri:error-warning-line'" class="text-2xl" />
                </div>
                <div class="flex flex-col gap-1 flex-1">
                  <span class="stat-item__label">床位状态</span>
                  <span class="stat-item__value" :class="previewData.canAllocate ? 'stat-item__value--success' : 'stat-item__value--danger'">
                    {{ previewData.canAllocate ? '充足' : '不足' }}
                  </span>
                </div>
              </div>

              <!-- 进度条 -->
              <div v-if="previewData.totalStudents > 0 && previewData.totalBeds > 0" class="progress-bar">
                <div class="flex items-center justify-between">
                  <span class="progress-bar__label">床位使用率</span>
                  <span class="progress-bar__value">{{ Math.min(Math.round((previewData.totalStudents / previewData.totalBeds) * 100), 100) }}%</span>
                </div>
                <ElProgress
                  :percentage="Math.min(Math.round((previewData.totalStudents / previewData.totalBeds) * 100), 100)"
                  :color="previewData.canAllocate ? '#67c23a' : '#f56c6c'"
                  :show-text="false"
                  :stroke-width="8"
                />
              </div>
            </div>

            <div class="px-5 pb-5 space-y-2">
              <div v-if="previewData.unfilledSurveyCount > 0" class="alert-item alert-item--warning">
                <ArtSvgIcon icon="ri:error-warning-line" class="text-base" />
                <span>{{ previewData.unfilledSurveyCount }} 名学生未填写问卷</span>
              </div>
              <div v-if="previewData.estimatedTime" class="alert-item alert-item--info">
                <ArtSvgIcon icon="ri:time-line" class="text-base" />
                <span>预计执行：{{ previewData.estimatedTime }}</span>
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
          {{ submitLoading ? '提交中...' : '创建任务' }}
        </ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'
  import { fetchGetConfigPage, fetchAddTask, fetchPreviewTask } from '@/api/allocation-manage'
  import {
    fetchGetCampusPage,
    fetchGetDepartmentPage,
    fetchGetMajorPage,
    fetchGetAcademicYearPage
  } from '@/api/school-manage'
  import { fetchGetFloorPage } from '@/api/dormitory-manage'
  import { useDictStore } from '@/store/modules/dict'

  interface Props {
    visible: boolean
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

  const formRef = ref<FormInstance>()
  const submitLoading = ref(false)
  const previewLoading = ref(false)

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
        targetEnrollmentYear: formData.value.targetEnrollmentYear,
        targetGender: formData.value.targetGender || undefined,
        targetCampusId: undefined,
        targetBuildingId: undefined
      })
      previewData.value = data
    } catch (e) {
      console.error('预览失败', e)
    } finally {
      previewLoading.value = false
    }
  }

  // 提交创建
  const handleSubmit = async () => {
    try {
      await formRef.value?.validate()
    } catch {
      return
    }

    submitLoading.value = true
    try {
      await fetchAddTask({
        taskName: formData.value.taskName,
        taskType: formData.value.taskType,
        configId: formData.value.configId!,
        targetEnrollmentYear: formData.value.targetEnrollmentYear,
        targetGender: formData.value.targetGender || undefined,
        remark: formData.value.remark || undefined
      })
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
    (val) => {
      if (val) {
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
        previewData.value = null
        // 清空下级选项
        deptOptions.value = []
        majorOptions.value = []
        floorOptions.value = []
        nextTick(() => {
          formRef.value?.clearValidate()
        })
        // 加载配置列表、学年列表、字典数据、校区列表和楼层列表
        loadConfigList()
        loadAcademicYearList()
        loadDictData()
        loadCampusList()
        loadFloorList()
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
  .preview-card {
    overflow: hidden;

    &__header {
      padding: 16px 20px;
      border-bottom: 1px solid var(--el-border-color-lighter);
      background: var(--el-fill-color-light);

      &--with-action {
        display: flex;
        align-items: center;
        justify-content: space-between;
      }
    }

    &__title {
      font-size: 16px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }

    &__label {
      font-size: 12px;
      font-weight: 500;
      color: var(--el-text-color-secondary);
    }

    &__value {
      font-size: 14px;
      font-weight: 500;
      color: var(--el-text-color-primary);
    }

    &__unit {
      font-size: 14px;
      color: var(--el-text-color-secondary);
    }

    &__empty {
      font-size: 14px;
      color: var(--el-text-color-placeholder);
    }
  }

  // 统计项样式
  .stat-item {
    background: var(--el-bg-color);
    border-radius: 8px;
    padding: 16px;
    display: flex;
    align-items: center;
    gap: 16px;
    border: 2px solid var(--el-border-color-light);
    transition: all 0.3s;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);

    &:hover {
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
      transform: translateY(-2px);
    }

    &--primary {
      border-color: var(--el-color-primary-light-7);
    }

    &--success {
      border-color: var(--el-color-success-light-5);
    }

    &--danger {
      border-color: var(--el-color-danger-light-5);
    }

    &__icon {
      width: 48px;
      height: 48px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.3s;

      &--primary {
        background: var(--el-color-primary-light-9);
        color: var(--el-color-primary);
      }

      &--success {
        background: var(--el-color-success-light-9);
        color: var(--el-color-success);
      }

      &--danger {
        background: var(--el-color-danger-light-9);
        color: var(--el-color-danger);
      }
    }

    &__label {
      font-size: 12px;
      font-weight: 500;
      color: var(--el-text-color-secondary);
    }

    &__value {
      font-size: 24px;
      font-weight: 700;

      &--primary {
        color: var(--el-color-primary);
      }

      &--success {
        color: var(--el-color-success);
      }

      &--danger {
        color: var(--el-color-danger);
      }
    }
  }

  // 进度条样式
  .progress-bar {
    background: var(--el-fill-color-light);
    border-radius: 8px;
    padding: 16px;

    &__label {
      font-size: 12px;
      color: var(--el-text-color-regular);
    }

    &__value {
      font-size: 12px;
      font-weight: 600;
      color: var(--el-color-primary);
    }
  }

  // 提示项样式
  .alert-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 16px;
    border-radius: 8px;
    font-size: 14px;

    &--warning {
      background: var(--el-color-warning-light-9);
      color: var(--el-color-warning);
      border: 1px solid var(--el-color-warning-light-5);
    }

    &--info {
      background: var(--el-color-primary-light-9);
      color: var(--el-color-primary);
      border: 1px solid var(--el-color-primary-light-5);
    }
  }

  // 空状态样式
  .empty-icon {
    color: var(--el-text-color-placeholder);
  }

  .empty-text {
    font-size: 14px;
    color: var(--el-text-color-placeholder);
  }
</style>
