import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import type { IOptionItem, IStudentHabitsForm } from '@/types/api/student-habits';
import { getStudentHabitsAPI, updateStudentHabitsAPI } from '@/api/student-habits';
import useUserStore from '@/store/modules/user';
import { ROUTE_CONSTANTS } from '@/constants';

// ===== 作息与睡眠选项 =====

export const sleepScheduleOptions: IOptionItem[] = [
  { label: '早睡早起（22:00-6:00）', value: 0 },
  { label: '正常（23:00-7:00）', value: 1 },
  { label: '晚睡晚起（24:00-8:00）', value: 2 },
  { label: '夜猫子（01:00-9:00）', value: 3 },
];

export const sleepQualityOptions: IOptionItem[] = [
  { label: '浅睡易醒', value: 0 },
  { label: '正常', value: 1 },
  { label: '深睡', value: 2 },
];

export const yesNoOptions: IOptionItem[] = [
  { label: '不打', value: 0 },
  { label: '打', value: 1 },
];

export const sensitiveOptions: IOptionItem[] = [
  { label: '不敏感', value: 0 },
  { label: '敏感', value: 1 },
];

export const smokingOptions: IOptionItem[] = [
  { label: '不吸烟', value: 0 },
  { label: '吸烟', value: 1 },
];

export const acceptOptions: IOptionItem[] = [
  { label: '不接受', value: 0 },
  { label: '接受', value: 1 },
];

// ===== 卫生习惯选项 =====

export const cleanlinessOptions: IOptionItem[] = [
  { label: '非常整洁', value: 1 },
  { label: '整洁', value: 2 },
  { label: '一般', value: 3 },
  { label: '随意', value: 4 },
  { label: '不整洁', value: 5 },
];

export const cleanupOptions: IOptionItem[] = [
  { label: '不整理', value: 0 },
  { label: '偶尔整理', value: 1 },
  { label: '经常整理', value: 2 },
  { label: '总是整理', value: 3 },
];

// ===== 社交与生活选项 =====

export const socialOptions: IOptionItem[] = [
  { label: '喜欢安静', value: 1 },
  { label: '中等', value: 2 },
  { label: '喜欢热闹', value: 3 },
];

export const visitorOptions: IOptionItem[] = [
  { label: '不允许', value: 0 },
  { label: '偶尔可以', value: 1 },
  { label: '可以', value: 2 },
];

export const phoneOptions: IOptionItem[] = [
  { label: '喜欢在宿舍打电话', value: 0 },
  { label: '偶尔在宿舍', value: 1 },
  { label: '不在宿舍打电话', value: 2 },
];

export const frequencyOptions: IOptionItem[] = [
  { label: '不', value: 0 },
  { label: '偶尔', value: 1 },
  { label: '经常', value: 2 },
];

// ===== 学习与娱乐选项 =====

export const studyFrequencyOptions: IOptionItem[] = [
  { label: '不在', value: 0 },
  { label: '偶尔', value: 1 },
  { label: '经常', value: 2 },
  { label: '总是', value: 3 },
];

export const studyEnvOptions: IOptionItem[] = [
  { label: '需要安静', value: 1 },
  { label: '需要轻音乐', value: 2 },
  { label: '可以接受声音', value: 3 },
];

export const computerOptions: IOptionItem[] = [
  { label: '不用', value: 0 },
  { label: '很少（1-2h/天）', value: 1 },
  { label: '正常（3-5h/天）', value: 2 },
  { label: '很多（6h+/天）', value: 3 },
];

export const volumeOptions: IOptionItem[] = [
  { label: '喜欢小声', value: 1 },
  { label: '中等', value: 2 },
  { label: '喜欢大声', value: 3 },
];

// ===== Composable =====

export function useStudentHabits() {
  const userStore = useUserStore();

  // 加载状态
  const loading = ref(false);

  // 表单数据
  const formData = ref<IStudentHabitsForm>({
    sleepSchedule: null,
    sleepQuality: null,
    snores: null,
    sensitiveToLight: null,
    sensitiveToSound: null,
    smokingStatus: null,
    smokingTolerance: null,
    cleanlinessLevel: null,
    bedtimeCleanup: null,
    socialPreference: null,
    allowVisitors: null,
    phoneCallTime: null,
    eatInRoom: null,
    studyInRoom: null,
    studyEnvironment: null,
    computerUsageTime: null,
    gamingPreference: null,
    musicPreference: null,
    musicVolume: null,
  });

  // 返回
  function handleBack(): void {
    uni.navigateBack();
  }

  // 验证表单
  function validateForm(): boolean {
    // 检查必填的选择题是否已填写（至少一项）
    const hasBasicInfo = formData.value.sleepSchedule !== undefined
      || formData.value.sleepQuality !== undefined
      || formData.value.cleanlinessLevel !== undefined;

    if (!hasBasicInfo) {
      uni.showToast({
        title: '请至少填写一项生活习惯',
        icon: 'none',
        duration: 2000,
      });
      return false;
    }

    return true;
  }

  // 加载数据
  async function loadData() {
    loading.value = true;
    try {
      const data = await getStudentHabitsAPI();
      if (data) {
        // 填充表单数据，处理null值
        formData.value = {
          sleepSchedule: data.sleepSchedule ?? null,
          sleepQuality: data.sleepQuality ?? null,
          snores: data.snores ?? null,
          sensitiveToLight: data.sensitiveToLight ?? null,
          sensitiveToSound: data.sensitiveToSound ?? null,
          smokingStatus: data.smokingStatus ?? null,
          smokingTolerance: data.smokingTolerance ?? null,
          cleanlinessLevel: data.cleanlinessLevel ?? null,
          bedtimeCleanup: data.bedtimeCleanup ?? null,
          socialPreference: data.socialPreference ?? null,
          allowVisitors: data.allowVisitors ?? null,
          phoneCallTime: data.phoneCallTime ?? null,
          eatInRoom: data.eatInRoom ?? null,
          studyInRoom: data.studyInRoom ?? null,
          studyEnvironment: data.studyEnvironment ?? null,
          computerUsageTime: data.computerUsageTime ?? null,
          gamingPreference: data.gamingPreference ?? null,
          musicPreference: data.musicPreference ?? null,
          musicVolume: data.musicVolume ?? null,
        };
      }
    }
    catch (error: any) {
      // 如果是404或数据不存在，不显示错误，允许用户填写新数据
      if (error?.statusCode !== 404 && error?.data?.code !== 404) {
        uni.showToast({
          title: error?.data?.message || error?.message || '获取数据失败',
          icon: 'none',
          duration: 2000,
        });
      }
    }
    finally {
      loading.value = false;
    }
  }

  // 提交
  async function handleSubmit(): Promise<void> {
    // 验证表单
    if (!validateForm()) {
      return;
    }

    // 显示加载提示
    uni.showLoading({
      title: '提交中...',
      mask: true,
    });

    try {
      await updateStudentHabitsAPI(formData.value);
      uni.hideLoading();
      uni.showModal({
        title: '提交成功',
        content: '您的生活习惯信息已保存，我们将为您匹配更合适的室友。',
        showCancel: false,
        success: () => {
          setTimeout(() => {
            uni.navigateBack();
          }, 500);
        },
      });
    }
    catch (error: any) {
      uni.hideLoading();
      uni.showToast({
        title: error?.data?.message || error?.message || '提交失败，请稍后重试',
        icon: 'none',
        duration: 2000,
      });
    }
  }

  // 页面加载时获取数据
  onLoad(async () => {
    // 先检查登录状态（如果有token会自动获取用户信息）
    const isUserLoggedIn = await userStore.checkLoginStatus();

    // 如果未登录，显示提示
    if (!isUserLoggedIn) {
      uni.showModal({
        title: '提示',
        content: '请先登录后再查看和完善个人情况',
        confirmText: '去登录',
        cancelText: '返回',
        success: (res) => {
          if (res.confirm) {
            // 跳转到登录页
            uni.redirectTo({
              url: ROUTE_CONSTANTS.LOGIN,
            });
          }
          else {
            // 返回上一页
            uni.navigateBack();
          }
        },
      });
      return;
    }

    // 已登录，加载数据
    loadData();
  });

  return {
    loading,
    formData,
    handleBack,
    handleSubmit,
  };
}
