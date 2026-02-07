import { computed, onMounted, reactive, ref } from 'vue';

import { getSurveyDataAPI, submitSurveyAPI } from '@/api/allocation';
import type { ISurveyData } from '@/types/api';

/** 选项配置 */
export interface SurveyOption {
  label: string;
  value: number;
}

/** 表单项配置 */
export interface SurveyFormItemConfig {
  label: string;
  field: keyof ISurveyData;
  options: SurveyOption[];
}

/** 表单区块配置 */
export interface SurveySectionConfig {
  title: string;
  items: SurveyFormItemConfig[];
}

// ==================== 选项配置 ====================

const sleepScheduleOptions: SurveyOption[] = [
  { label: '早睡早起', value: 0 },
  { label: '正常作息', value: 1 },
  { label: '晚睡晚起', value: 2 },
  { label: '夜猫子', value: 3 },
];

const sleepQualityOptions: SurveyOption[] = [
  { label: '浅睡易醒', value: 0 },
  { label: '正常', value: 1 },
  { label: '深睡', value: 2 },
];

const snoresOptions: SurveyOption[] = [
  { label: '不打呼噜', value: 0 },
  { label: '打呼噜', value: 1 },
];

const binaryOptions: SurveyOption[] = [
  { label: '不敏感', value: 0 },
  { label: '敏感', value: 1 },
];

const smokingOptions: SurveyOption[] = [
  { label: '不吸烟', value: 0 },
  { label: '吸烟', value: 1 },
];

const smokingToleranceOptions: SurveyOption[] = [
  { label: '不接受', value: 0 },
  { label: '可以接受', value: 1 },
];

const cleanlinessOptions: SurveyOption[] = [
  { label: '非常整洁', value: 1 },
  { label: '整洁', value: 2 },
  { label: '一般', value: 3 },
  { label: '随意', value: 4 },
];

const bedtimeCleanupOptions: SurveyOption[] = [
  { label: '不整理', value: 0 },
  { label: '偶尔整理', value: 1 },
  { label: '经常整理', value: 2 },
  { label: '总是整理', value: 3 },
];

const frequencyOptions: SurveyOption[] = [
  { label: '从不', value: 0 },
  { label: '偶尔', value: 1 },
  { label: '经常', value: 2 },
];

const socialOptions: SurveyOption[] = [
  { label: '喜欢安静', value: 0 },
  { label: '中等', value: 1 },
  { label: '喜欢热闹', value: 2 },
];

const visitorOptions: SurveyOption[] = [
  { label: '不允许', value: 0 },
  { label: '偶尔可以', value: 1 },
  { label: '可以', value: 2 },
];

const phoneCallOptions: SurveyOption[] = [
  { label: '不喜欢在宿舍打', value: 0 },
  { label: '偶尔', value: 1 },
  { label: '不介意', value: 2 },
];

const studyInRoomOptions: SurveyOption[] = [
  { label: '不在宿舍学', value: 0 },
  { label: '偶尔', value: 1 },
  { label: '经常', value: 2 },
  { label: '总是', value: 3 },
];

const studyEnvironmentOptions: SurveyOption[] = [
  { label: '不需要安静', value: 0 },
  { label: '需要安静', value: 1 },
  { label: '需要轻音乐', value: 2 },
  { label: '可以接受声音', value: 3 },
];

const computerUsageOptions: SurveyOption[] = [
  { label: '不用电脑', value: 0 },
  { label: '很少', value: 1 },
  { label: '正常', value: 2 },
  { label: '很多', value: 3 },
];

const gamingOptions: SurveyOption[] = [
  { label: '不玩游戏', value: 0 },
  { label: '偶尔玩', value: 1 },
  { label: '经常玩', value: 2 },
];

const musicOptions: SurveyOption[] = [
  { label: '不听音乐', value: 0 },
  { label: '偶尔听', value: 1 },
  { label: '经常听', value: 2 },
];

const volumeOptions: SurveyOption[] = [
  { label: '喜欢小声', value: 0 },
  { label: '中等音量', value: 1 },
  { label: '喜欢大声', value: 2 },
];

// ==================== 区块配置 ====================

export const surveySections: SurveySectionConfig[] = [
  {
    title: '作息时间',
    items: [
      { label: '作息规律', field: 'sleepSchedule', options: sleepScheduleOptions },
      { label: '睡眠质量', field: 'sleepQuality', options: sleepQualityOptions },
      { label: '是否打呼噜', field: 'snores', options: snoresOptions },
      { label: '对光线敏感', field: 'sensitiveToLight', options: binaryOptions },
      { label: '对声音敏感', field: 'sensitiveToSound', options: binaryOptions },
    ],
  },
  {
    title: '生活习惯',
    items: [
      { label: '是否吸烟', field: 'smokingStatus', options: smokingOptions },
      { label: '是否接受室友吸烟', field: 'smokingTolerance', options: smokingToleranceOptions },
      { label: '整洁程度', field: 'cleanlinessLevel', options: cleanlinessOptions },
      { label: '睡前整理习惯', field: 'bedtimeCleanup', options: bedtimeCleanupOptions },
      { label: '是否在宿舍吃东西', field: 'eatInRoom', options: frequencyOptions },
    ],
  },
  {
    title: '社交偏好',
    items: [
      { label: '社交类型', field: 'socialPreference', options: socialOptions },
      { label: '是否允许访客', field: 'allowVisitors', options: visitorOptions },
      { label: '电话习惯', field: 'phoneCallTime', options: phoneCallOptions },
    ],
  },
  {
    title: '学习娱乐',
    items: [
      { label: '在宿舍学习频率', field: 'studyInRoom', options: studyInRoomOptions },
      { label: '学习环境偏好', field: 'studyEnvironment', options: studyEnvironmentOptions },
      { label: '电脑使用时间', field: 'computerUsageTime', options: computerUsageOptions },
      { label: '游戏偏好', field: 'gamingPreference', options: gamingOptions },
      { label: '听音乐频率', field: 'musicPreference', options: musicOptions },
      { label: '音乐音量偏好', field: 'musicVolume', options: volumeOptions },
    ],
  },
];

// ==================== Composable ====================

export function useSurveyForm() {
  const loading = ref(true);
  const submitting = ref(false);
  const surveyStatus = ref(0); // 0-未填写 1-已填写 2-已锁定

  const isLocked = computed(() => surveyStatus.value === 2);

  const formData = reactive<ISurveyData>({
    smokingStatus: undefined,
    smokingTolerance: undefined,
    sleepSchedule: undefined,
    sleepQuality: undefined,
    snores: undefined,
    sensitiveToLight: undefined,
    sensitiveToSound: undefined,
    cleanlinessLevel: undefined,
    bedtimeCleanup: undefined,
    socialPreference: undefined,
    allowVisitors: undefined,
    phoneCallTime: undefined,
    studyInRoom: undefined,
    studyEnvironment: undefined,
    computerUsageTime: undefined,
    gamingPreference: undefined,
    musicPreference: undefined,
    musicVolume: undefined,
    eatInRoom: undefined,
  });

  /** 计算进度百分比 */
  const progressPercent = computed(() => {
    const fields = Object.values(formData);
    const filled = fields.filter(v => v !== undefined && v !== null).length;
    return Math.round((filled / fields.length) * 100);
  });

  /** 选择选项 */
  function selectOption(field: keyof ISurveyData, value: number): void {
    if (isLocked.value) return;
    const data = formData as Record<string, unknown>;
    data[field] = value;
  }

  /** 加载问卷数据 */
  async function loadSurveyData(): Promise<void> {
    try {
      loading.value = true;
      const data = await getSurveyDataAPI();
      if (data) {
        surveyStatus.value = data.surveyStatus || 0;
        Object.keys(formData).forEach((key) => {
          const value = (data as any)[key];
          if (value !== undefined && value !== null) {
            ;(formData as any)[key] = value;
          }
        });
      }
    }
    catch (error) {
      console.error('加载问卷数据失败:', error);
      uni.showToast({ title: '加载失败', icon: 'none' });
    }
    finally {
      loading.value = false;
    }
  }

  /** 提交问卷 */
  async function handleSubmit(): Promise<void> {
    if (submitting.value || isLocked.value) return;

    const fields = Object.values(formData);
    const filled = fields.filter(v => v !== undefined && v !== null).length;
    if (filled < fields.length) {
      uni.showToast({ title: '请完成所有问题', icon: 'none' });
      return;
    }

    uni.showModal({
      title: '确认提交',
      content: '提交后将用于室友匹配，确定提交吗？',
      success: async (res) => {
        if (res.confirm) {
          submitting.value = true;
          try {
            await submitSurveyAPI(formData);
            uni.showToast({ title: '提交成功', icon: 'success' });
            surveyStatus.value = 1;
            setTimeout(() => {
              uni.navigateBack();
            }, 1500);
          }
          catch (error) {
            console.error('提交问卷失败:', error);
            uni.showToast({ title: '提交失败', icon: 'none' });
          }
          finally {
            submitting.value = false;
          }
        }
      },
    });
  }

  onMounted(() => {
    loadSurveyData();
  });

  return {
    loading,
    submitting,
    surveyStatus,
    isLocked,
    formData,
    progressPercent,
    selectOption,
    handleSubmit,
  };
}
