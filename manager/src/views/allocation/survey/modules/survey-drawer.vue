<template>
  <ArtDrawer v-model="drawerVisible" :title="drawerTitle" width="700px">
    <div v-loading="loading">
      <template v-if="surveyData">
        <ElDescriptions :column="2" border class="mb-4">
        <ElDescriptionsItem label="学号">{{ surveyData.studentNo }}</ElDescriptionsItem>
        <ElDescriptionsItem label="姓名">{{ surveyData.studentName }}</ElDescriptionsItem>
        <ElDescriptionsItem label="班级">{{ surveyData.className }}</ElDescriptionsItem>
        <ElDescriptionsItem label="填写时间">{{ surveyData.fillTime || '-' }}</ElDescriptionsItem>
      </ElDescriptions>

      <ElDivider content-position="left">作息习惯</ElDivider>
      <ElDescriptions :column="2" border class="mb-4">
        <ElDescriptionsItem label="作息时间">
          {{ surveyData.sleepScheduleText || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="睡眠质量">
          {{ formatByDict('student_sleep_quality', surveyData.sleepQuality) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="是否打呼噜">
          {{ formatByDict('student_snores', surveyData.snores) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="光线敏感">
          {{ formatByDict('student_sensitive_to_light', surveyData.sensitiveToLight) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="声音敏感">
          {{ formatByDict('student_sensitive_to_sound', surveyData.sensitiveToSound) }}
        </ElDescriptionsItem>
      </ElDescriptions>

      <ElDivider content-position="left">生活习惯</ElDivider>
      <ElDescriptions :column="2" border class="mb-4">
        <ElDescriptionsItem label="吸烟情况">
          {{ surveyData.smokingStatusText || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="是否接受室友吸烟">
          {{ formatByDict('student_smoking_tolerance', surveyData.smokingTolerance) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="整洁程度">
          {{ surveyData.cleanlinessLevelText || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="睡前整理">
          {{ formatByDict('student_bedtime_cleanup', surveyData.bedtimeCleanup) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="是否在宿舍吃东西">
          {{ formatByDict('student_eat_in_room', surveyData.eatInRoom) }}
        </ElDescriptionsItem>
      </ElDescriptions>

      <ElDivider content-position="left">社交偏好</ElDivider>
      <ElDescriptions :column="2" border class="mb-4">
        <ElDescriptionsItem label="社交类型">
          {{ surveyData.socialPreferenceText || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="是否允许访客">
          {{ formatByDict('student_allow_visitors', surveyData.allowVisitors) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="电话习惯">
          {{ formatByDict('student_phone_call_time', surveyData.phoneCallTime) }}
        </ElDescriptionsItem>
      </ElDescriptions>

      <ElDivider content-position="left">学习娱乐</ElDivider>
      <ElDescriptions :column="2" border>
        <ElDescriptionsItem label="在宿舍学习">
          {{ formatByDict('student_study_in_room', surveyData.studyInRoom) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="学习环境偏好">
          {{ formatByDict('student_study_environment', surveyData.studyEnvironment) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="电脑使用时间">
          {{ formatByDict('student_computer_usage_time', surveyData.computerUsageTime) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="游戏偏好">
          {{ formatByDict('student_gaming_preference', surveyData.gamingPreference) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="听音乐频率">
          {{ formatByDict('student_music_preference', surveyData.musicPreference) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="音乐音量偏好">
          {{ formatByDict('student_music_volume', surveyData.musicVolume) }}
        </ElDescriptionsItem>
      </ElDescriptions>
      </template>
    </div>

    <template #footer>
      <ElButton @click="drawerVisible = false">关闭</ElButton>
    </template>
  </ArtDrawer>
</template>

<script setup lang="ts">
  import ArtDrawer from '@/components/core/layouts/art-drawer/index.vue'
  import { fetchGetSurveyDetail } from '@/api/allocation-manage'
  import { useDictStore } from '@/store/modules/dict'

  interface Props {
    visible: boolean
    studentId?: number
    studentName?: string
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const dictStore = useDictStore()

  const drawerVisible = computed({
    get: () => props.visible,
    set: (val) => emit('update:visible', val)
  })

  const drawerTitle = computed(() => `${props.studentName || ''} 的问卷详情`)

  const loading = ref(false)
  const surveyData = ref<Api.Allocation.SurveyDetail | null>(null)

  // 需要加载的字典编码列表
  const DICT_CODES = [
    'student_sleep_quality',
    'student_snores',
    'student_sensitive_to_light',
    'student_sensitive_to_sound',
    'student_smoking_tolerance',
    'student_bedtime_cleanup',
    'student_eat_in_room',
    'student_allow_visitors',
    'student_phone_call_time',
    'student_study_in_room',
    'student_study_environment',
    'student_computer_usage_time',
    'student_gaming_preference',
    'student_music_preference',
    'student_music_volume'
  ]

  // 批量加载字典数据
  const loadDictionaries = async () => {
    try {
      await dictStore.loadDictDataBatch(DICT_CODES)
    } catch (error) {
      console.error('加载字典数据失败:', error)
    }
  }

  // 通用字典格式化函数
  const formatByDict = (dictCode: string, value: number | undefined | null): string => {
    if (value === null || value === undefined) return '-'
    const dictData = dictStore.getDictData(dictCode)
    const item = dictData.find((d) => d.value === String(value))
    return item?.label || '-'
  }

  // 加载问卷详情
  const loadSurveyDetail = async () => {
    if (!props.studentId) return
    loading.value = true
    try {
      const data = await fetchGetSurveyDetail(props.studentId)
      surveyData.value = data
    } catch (error) {
      console.error('获取问卷详情失败:', error)
      ElMessage.error('获取问卷详情失败')
    } finally {
      loading.value = false
    }
  }

  // 监听visible变化
  watch(
    () => props.visible,
    (val) => {
      if (val && props.studentId) {
        loadSurveyDetail()
      } else {
        surveyData.value = null
      }
    }
  )

  // 组件挂载时加载字典
  onMounted(() => {
    loadDictionaries()
  })
</script>
