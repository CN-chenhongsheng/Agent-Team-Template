<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="80%"
    top="5vh"
    align-center
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="grid grid-cols-[1fr_400px] gap-5 max-h-[75vh] overflow-y-auto">
      <!-- 左侧：核心配置区 -->
      <div class="flex flex-col gap-5">
        <!-- 硬约束设置 -->
        <div class="art-card-sm config-card">
          <div class="config-card__header">
            <div class="flex items-center gap-2">
              <div class="w-1 h-5 bg-primary rounded-full"></div>
              <span class="text-base font-semibold text-gray-800 dark:text-gray-100">硬约束设置</span>
            </div>
            <span class="text-xs text-gray-500 dark:text-gray-400 mt-1 block ml-3">设置匹配时的必要条件</span>
          </div>
          <div class="flex flex-col gap-3">
            <div class="constraint-item">
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-3">
                  <ArtSvgIcon icon="ri:fire-line" class="text-xl text-primary" />
                  <div class="flex flex-col gap-1">
                    <span class="text-sm font-medium text-gray-700 dark:text-gray-200">吸烟约束</span>
                    <span class="text-xs text-gray-500 dark:text-gray-400">吸烟者不能与不接受吸烟者同住</span>
                  </div>
                </div>
                <ArtSwitch
                  v-model="formData.smokingConstraint"
                  :active-value="1"
                  :inactive-value="0"
                  size="large"
                />
              </div>
            </div>
            <div class="constraint-item">
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-3">
                  <ArtSvgIcon icon="ri:group-line" class="text-xl text-primary" />
                  <div class="flex flex-col gap-1">
                    <span class="text-sm font-medium text-gray-700 dark:text-gray-200">性别约束</span>
                    <span class="text-xs text-gray-500 dark:text-gray-400">不同性别不能同住</span>
                  </div>
                </div>
                <ArtSwitch
                  v-model="formData.genderConstraint"
                  :active-value="1"
                  :inactive-value="0"
                  size="large"
                />
              </div>
            </div>
            <div class="constraint-item">
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-3">
                  <ArtSvgIcon icon="ri:moon-line" class="text-xl text-primary" />
                  <div class="flex flex-col gap-1">
                    <span class="text-sm font-medium text-gray-700 dark:text-gray-200">作息约束</span>
                    <span class="text-xs text-gray-500 dark:text-gray-400">作息差异≥3档不能同住</span>
                  </div>
                </div>
                <ArtSwitch
                  v-model="formData.sleepHardConstraint"
                  :active-value="1"
                  :inactive-value="0"
                  size="large"
                />
              </div>
            </div>
          </div>
        </div>

        <!-- 维度权重 -->
        <div class="art-card-sm config-card">
          <div class="config-card__header">
            <div class="flex items-center gap-2 mb-2">
              <div class="w-1 h-5 bg-primary rounded-full"></div>
              <span class="text-base font-semibold text-gray-800 dark:text-gray-100">维度权重</span>
              <div
                class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-medium transition-all duration-300"
                :class="
                  totalWeight === 100
                    ? 'bg-green-50 dark:bg-green-900/30 text-green-700 dark:text-green-400 shadow-sm shadow-green-200/50 dark:shadow-green-900/50'
                    : 'bg-red-50 dark:bg-red-900/30 text-red-700 dark:text-red-400 animate-pulse'
                "
              >
                <span class="font-semibold">{{ totalWeight }}%</span>
                <span class="opacity-80">{{ totalWeight === 100 ? '✓ 有效' : '需要100%' }}</span>
              </div>
            </div>
            <span class="text-xs text-gray-500 dark:text-gray-400 mt-1 block ml-3"
              >调整各维度在匹配算法中的权重，总和必须为100%</span
            >
            <!-- 权重进度条 -->
            <div class="mt-3 ml-3">
              <div class="h-2 bg-gray-200 dark:bg-gray-600 rounded-full overflow-hidden">
                <div
                  class="h-full rounded-full transition-all duration-500 ease-out"
                  :class="
                    totalWeight === 100
                      ? 'bg-gradient-to-r from-green-400 to-green-500'
                      : totalWeight > 100
                        ? 'bg-gradient-to-r from-red-400 to-red-500'
                        : 'bg-gradient-to-r from-blue-400 to-blue-500'
                  "
                  :style="{ width: Math.min(totalWeight, 100) + '%' }"
                ></div>
              </div>
            </div>
          </div>
          <div class="grid grid-cols-3 gap-4">
            <div class="weight-item">
              <div class="flex items-center justify-between mb-3">
                <div class="flex items-center gap-2">
                  <ArtSvgIcon icon="ri:moon-line" class="text-base text-primary" />
                  <span class="text-sm font-medium text-gray-700 dark:text-gray-200">睡眠维度</span>
                </div>
                <span class="text-sm font-semibold text-primary">{{ formData.sleepWeight }}%</span>
              </div>
              <ElSlider v-model="formData.sleepWeight" :max="100" />
            </div>
            <div class="weight-item">
              <div class="flex items-center justify-between mb-3">
                <div class="flex items-center gap-2">
                  <ArtSvgIcon icon="ri:fire-line" class="text-base text-primary" />
                  <span class="text-sm font-medium text-gray-700 dark:text-gray-200">吸烟维度</span>
                </div>
                <span class="text-sm font-semibold text-primary">{{ formData.smokingWeight }}%</span>
              </div>
              <ElSlider v-model="formData.smokingWeight" :max="100" />
            </div>
            <div class="weight-item">
              <div class="flex items-center justify-between mb-3">
                <div class="flex items-center gap-2">
                  <ArtSvgIcon icon="ri:sparkling-line" class="text-base text-primary" />
                  <span class="text-sm font-medium text-gray-700 dark:text-gray-200">整洁维度</span>
                </div>
                <span class="text-sm font-semibold text-primary"
                  >{{ formData.cleanlinessWeight }}%</span
                >
              </div>
              <ElSlider v-model="formData.cleanlinessWeight" :max="100" />
            </div>
            <div class="weight-item">
              <div class="flex items-center justify-between mb-3">
                <div class="flex items-center gap-2">
                  <ArtSvgIcon icon="ri:chat-3-line" class="text-base text-primary" />
                  <span class="text-sm font-medium text-gray-700 dark:text-gray-200">社交维度</span>
                </div>
                <span class="text-sm font-semibold text-primary">{{ formData.socialWeight }}%</span>
              </div>
              <ElSlider v-model="formData.socialWeight" :max="100" />
            </div>
            <div class="weight-item">
              <div class="flex items-center justify-between mb-3">
                <div class="flex items-center gap-2">
                  <ArtSvgIcon icon="ri:book-open-line" class="text-base text-primary" />
                  <span class="text-sm font-medium text-gray-700 dark:text-gray-200">学习维度</span>
                </div>
                <span class="text-sm font-semibold text-primary">{{ formData.studyWeight }}%</span>
              </div>
              <ElSlider v-model="formData.studyWeight" :max="100" />
            </div>
            <div class="weight-item">
              <div class="flex items-center justify-between mb-3">
                <div class="flex items-center gap-2">
                  <ArtSvgIcon icon="ri:gamepad-line" class="text-base text-primary" />
                  <span class="text-sm font-medium text-gray-700 dark:text-gray-200">娱乐维度</span>
                </div>
                <span class="text-sm font-semibold text-primary"
                  >{{ formData.entertainmentWeight }}%</span
                >
              </div>
              <ElSlider v-model="formData.entertainmentWeight" :max="100" />
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：辅助配置区 -->
      <div class="flex flex-col gap-5">
        <!-- 基础信息 -->
        <div class="art-card-sm config-card">
          <div class="config-card__header">
            <div class="flex items-center gap-2">
              <div class="w-1 h-5 bg-primary rounded-full"></div>
              <span class="text-base font-semibold text-gray-800 dark:text-gray-100">基础信息</span>
            </div>
            <span class="text-xs text-gray-500 dark:text-gray-400 mt-1 block ml-3">配置的基本信息</span>
          </div>
          <ElForm ref="formRef" :model="formData" :rules="formRules" label-width="110px" label-position="left">
            <ElFormItem prop="configName">
              <template #label>
                <div class="flex items-center gap-1.5">
                  <ArtSvgIcon icon="ri:file-text-line" class="text-base text-primary" />
                  <span>配置名称</span>
                </div>
              </template>
              <ElInput
                v-model="formData.configName"
                placeholder="请输入配置名称"
                class="w-full transition-all duration-300 focus-within:shadow-sm"
              />
            </ElFormItem>
          </ElForm>
        </div>

        <!-- 算法与阈值 -->
        <div class="art-card-sm config-card">
          <div class="config-card__header">
            <div class="flex items-center gap-2">
              <div class="w-1 h-5 bg-primary rounded-full"></div>
              <span class="text-base font-semibold text-gray-800 dark:text-gray-100">算法与阈值</span>
            </div>
            <span class="text-xs text-gray-500 dark:text-gray-400 mt-1 block ml-3">选择匹配算法并设置分数阈值</span>
          </div>
          <ElForm ref="formRef" :model="formData" :rules="formRules" label-width="110px" label-position="left">
            <ElFormItem prop="algorithmType">
              <template #label>
                <div class="flex items-center gap-1.5">
                  <ArtSvgIcon icon="ri:settings-3-line" class="text-base text-primary" />
                  <span>算法类型</span>
                </div>
              </template>
              <ElSelect v-model="formData.algorithmType" class="w-full">
                <ElOption
                  v-for="item in algorithmOptions"
                  :key="item.type"
                  :label="item.name"
                  :value="item.type"
                >
                  <div class="flex items-center justify-between">
                    <span>{{ item.name }}</span>
                    <ElTag v-if="item.recommended" type="success" size="small">推荐</ElTag>
                  </div>
                </ElOption>
              </ElSelect>
            </ElFormItem>
            <ElFormItem prop="minMatchScore">
              <template #label>
                <div class="flex items-center gap-1.5">
                  <ArtSvgIcon icon="ri:bar-chart-line" class="text-base text-primary" />
                  <span>最低匹配</span>
                </div>
              </template>
              <ElInputNumber v-model="formData.minMatchScore" :min="0" :max="100" />
              <span class="block text-xs text-gray-500 dark:text-gray-400 mt-1">低于此分数需人工审核</span>
            </ElFormItem>
          </ElForm>
        </div>

        <!-- 加分项 -->
        <div class="art-card-sm config-card">
          <div class="config-card__header">
            <div class="flex items-center gap-2">
              <div class="w-1 h-5 bg-primary rounded-full"></div>
              <span class="text-base font-semibold text-gray-800 dark:text-gray-100">加分项</span>
            </div>
            <span class="text-xs text-gray-500 dark:text-gray-400 mt-1 block ml-3">同一组织的学生可获得额外加分</span>
          </div>
          <ElForm ref="formRef" :model="formData" label-width="110px" label-position="left">
            <ElFormItem>
              <template #label>
                <div class="flex items-center gap-1.5">
                  <ArtSvgIcon icon="ri:building-line" class="text-base text-primary" />
                  <span>同院系加分</span>
                </div>
              </template>
              <ElInputNumber v-model="formData.sameDeptBonus" :min="0" :max="50" />
            </ElFormItem>
            <ElFormItem>
              <template #label>
                <div class="flex items-center gap-1.5">
                  <ArtSvgIcon icon="ri:book-open-line" class="text-base text-primary" />
                  <span>同专业加分</span>
                </div>
              </template>
              <ElInputNumber v-model="formData.sameMajorBonus" :min="0" :max="50" />
            </ElFormItem>
            <ElFormItem>
              <template #label>
                <div class="flex items-center gap-1.5">
                  <ArtSvgIcon icon="ri:graduation-cap-line" class="text-base text-primary" />
                  <span>同班级加分</span>
                </div>
              </template>
              <ElInputNumber v-model="formData.sameClassBonus" :min="0" :max="50" />
            </ElFormItem>
          </ElForm>
        </div>

        <!-- 备注 -->
        <div class="art-card-sm config-card">
          <div class="config-card__header">
            <div class="flex items-center gap-2">
              <div class="w-1 h-5 bg-primary rounded-full"></div>
              <span class="text-base font-semibold text-gray-800 dark:text-gray-100">备注</span>
            </div>
            <span class="text-xs text-gray-500 dark:text-gray-400 mt-1 block ml-3">配置的补充说明</span>
          </div>
          <ElForm ref="formRef" :model="formData" label-width="0">
            <ElFormItem label="">
              <ElInput
                v-model="formData.remark"
                type="textarea"
                :rows="4"
                placeholder="请输入备注信息（可选）"
                class="w-full"
              />
            </ElFormItem>
          </ElForm>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="flex gap-3 justify-end">
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ submitLoading ? '提交中...' : '确定' }}
        </ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'
  import { fetchAddConfig, fetchUpdateConfig } from '@/api/allocation-manage'

  interface Props {
    visible: boolean
    type: 'add' | 'edit'
    configData?: Partial<Api.Allocation.ConfigListItem> | null
    algorithmOptions?: Api.Allocation.AlgorithmOption[]
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'submit'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    configData: null,
    algorithmOptions: () => []
  })
  const emit = defineEmits<Emits>()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (val) => emit('update:visible', val)
  })

  const dialogTitle = computed(() => (props.type === 'add' ? '新增配置' : '编辑配置'))

  const formRef = ref<FormInstance>()
  const submitLoading = ref(false)

  // 默认表单数据
  const defaultFormData = (): Api.Allocation.ConfigSaveParams => ({
    configName: '',
    smokingConstraint: 1,
    genderConstraint: 1,
    sleepHardConstraint: 0,
    sleepWeight: 30,
    smokingWeight: 20,
    cleanlinessWeight: 15,
    socialWeight: 15,
    studyWeight: 10,
    entertainmentWeight: 10,
    algorithmType: 'kmeans',
    sameDeptBonus: 5,
    sameMajorBonus: 10,
    sameClassBonus: 15,
    minMatchScore: 60,
    remark: ''
  })

  const formData = ref<Api.Allocation.ConfigSaveParams>(defaultFormData())

  // 计算权重总和
  const totalWeight = computed(() => {
    return (
      formData.value.sleepWeight +
      formData.value.smokingWeight +
      formData.value.cleanlinessWeight +
      formData.value.socialWeight +
      formData.value.studyWeight +
      formData.value.entertainmentWeight
    )
  })

  // 表单验证规则
  const formRules: FormRules = {
    configName: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
    algorithmType: [{ required: true, message: '请选择算法类型', trigger: 'change' }],
    minMatchScore: [{ required: true, message: '请输入最低匹配分', trigger: 'blur' }]
  }

  // 监听visible变化，初始化表单数据
  watch(
    () => props.visible,
    (val) => {
      if (val) {
        if (props.type === 'edit' && props.configData) {
          formData.value = { ...defaultFormData(), ...props.configData }
        } else {
          formData.value = defaultFormData()
        }
        nextTick(() => {
          formRef.value?.clearValidate()
        })
      }
    }
  )

  const handleClose = () => {
    formRef.value?.resetFields()
  }

  const handleSubmit = async () => {
    if (totalWeight.value !== 100) {
      ElMessage.warning('维度权重总和必须为100')
      return
    }

    try {
      await formRef.value?.validate()
      submitLoading.value = true

      if (props.type === 'edit' && props.configData?.id) {
        await fetchUpdateConfig(props.configData.id, formData.value)
      } else {
        await fetchAddConfig(formData.value)
      }

      dialogVisible.value = false
      emit('submit')
    } catch (error) {
      console.error('提交配置失败:', error)
    } finally {
      submitLoading.value = false
    }
  }
</script>

<style scoped lang="scss">
  .text-primary {
    color: var(--el-color-primary);
  }

  .bg-primary {
    background-color: var(--el-color-primary);
  }

  // 配置卡片样式（继承系统 art-card-sm 的边框/阴影/圆角）
  .config-card {
    padding: 20px;

    &__header {
      margin-bottom: 16px;
      padding-bottom: 12px;
      border-bottom: 1px solid var(--el-border-color-lighter);
    }
  }

  // 约束项样式
  .constraint-item {
    background: var(--el-bg-color);
    border-radius: 8px;
    padding: 16px;
    border: 1px solid transparent;
    transition: all 0.3s;

    &:hover {
      transform: translateY(-2px);
      border-color: var(--el-color-primary-light-7);
      box-shadow: 0 4px 12px color-mix(in srgb, var(--el-color-primary) 15%, transparent);
    }
  }

  // 权重项样式
  .weight-item {
    background: var(--el-bg-color);
    border-radius: 8px;
    padding: 16px;
    transition: all 0.3s;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px color-mix(in srgb, var(--el-color-primary) 15%, transparent);
    }
  }

  // 自定义动画：抖动提示
  @keyframes shake {
    0%,
    100% {
      transform: translateX(0);
    }
    25% {
      transform: translateX(-2px);
    }
    75% {
      transform: translateX(2px);
    }
  }
</style>
