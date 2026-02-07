<template>
  <view class="survey-page">
    <!-- 背景装饰 -->
    <view class="bg-decorations">
      <view class="blob blob-1" />
      <view class="blob blob-2" />
    </view>

    <!-- 顶部导航 -->
    <page-header title="生活习惯问卷" />

    <view v-if="!loading" class="page-content">
      <!-- 问卷状态提示 -->
      <view v-if="surveyStatus === 2" class="glass-card status-card locked">
        <u-icon name="lock" size="24" color="#ef4444" />
        <text>问卷已锁定，无法修改</text>
      </view>

      <!-- 问卷说明卡片 -->
      <view class="glass-card intro-card">
        <view class="intro-icon">
          <u-icon name="edit-pen" size="32" color="#0adbc3" />
        </view>
        <view class="intro-content">
          <view class="intro-title">
            完善您的生活习惯信息
          </view>
          <view class="intro-desc">
            我们将根据您的作息习惯、生活偏好等信息，为您智能匹配最合适的室友
          </view>
        </view>
      </view>

      <!-- 问卷进度 -->
      <view class="progress-section">
        <view class="progress-header">
          <text class="progress-label">
            完成进度
          </text>
          <text class="progress-value">
            {{ progressPercent }}%
          </text>
        </view>
        <view class="progress-bar">
          <view class="progress-fill" :style="{ width: `${progressPercent}%` }" />
        </view>
      </view>

      <!-- 问卷表单 -->
      <view class="survey-form">
        <!-- 作息时间 -->
        <view class="form-section">
          <view class="section-title">
            <view class="title-icon" />
            <text>作息时间</text>
          </view>

          <view class="glass-card form-card">
            <view class="form-item">
              <view class="item-label">
                作息规律
              </view>
              <view class="item-options">
                <view
                  v-for="option in sleepScheduleOptions"
                  :key="option.value"
                  class="option-tag"
                  :class="{ active: formData.sleepSchedule === option.value, disabled: isLocked }"
                  @click="selectOption('sleepSchedule', option.value)"
                >
                  {{ option.label }}
                </view>
              </view>
            </view>

            <view class="form-item">
              <view class="item-label">
                睡眠质量
              </view>
              <view class="item-options">
                <view
                  v-for="option in sleepQualityOptions"
                  :key="option.value"
                  class="option-tag"
                  :class="{ active: formData.sleepQuality === option.value, disabled: isLocked }"
                  @click="selectOption('sleepQuality', option.value)"
                >
                  {{ option.label }}
                </view>
              </view>
            </view>

            <view class="form-item">
              <view class="item-label">
                是否打呼噜
              </view>
              <view class="item-options">
                <view
                  v-for="option in snoresOptions"
                  :key="option.value"
                  class="option-tag"
                  :class="{ active: formData.snores === option.value, disabled: isLocked }"
                  @click="selectOption('snores', option.value)"
                >
                  {{ option.label }}
                </view>
              </view>
            </view>

            <view class="form-item">
              <view class="item-label">
                对光线敏感
              </view>
              <view class="item-options">
                <view
                  v-for="option in binaryOptions"
                  :key="option.value"
                  class="option-tag"
                  :class="{ active: formData.sensitiveToLight === option.value, disabled: isLocked }"
                  @click="selectOption('sensitiveToLight', option.value)"
                >
                  {{ option.label }}
                </view>
              </view>
            </view>

            <view class="form-item">
              <view class="item-label">
                对声音敏感
              </view>
              <view class="item-options">
                <view
                  v-for="option in binaryOptions"
                  :key="option.value"
                  class="option-tag"
                  :class="{ active: formData.sensitiveToSound === option.value, disabled: isLocked }"
                  @click="selectOption('sensitiveToSound', option.value)"
                >
                  {{ option.label }}
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- 生活习惯 -->
        <view class="form-section">
          <view class="section-title">
            <view class="title-icon" />
            <text>生活习惯</text>
          </view>

          <view class="glass-card form-card">
            <view class="form-item">
              <view class="item-label">
                是否吸烟
              </view>
              <view class="item-options">
                <view
                  v-for="option in smokingOptions"
                  :key="option.value"
                  class="option-tag"
                  :class="{ active: formData.smokingStatus === option.value, disabled: isLocked }"
                  @click="selectOption('smokingStatus', option.value)"
                >
                  {{ option.label }}
                </view>
              </view>
            </view>

            <view class="form-item">
              <view class="item-label">
                是否接受室友吸烟
              </view>
              <view class="item-options">
                <view
                  v-for="option in smokingToleranceOptions"
                  :key="option.value"
                  class="option-tag"
                  :class="{ active: formData.smokingTolerance === option.value, disabled: isLocked }"
                  @click="selectOption('smokingTolerance', option.value)"
                >
                  {{ option.label }}
                </view>
              </view>
            </view>

            <view class="form-item">
              <view class="item-label">
                整洁程度
              </view>
              <view class="item-options">
                <view
                  v-for="option in cleanlinessOptions"
                  :key="option.value"
                  class="option-tag"
                  :class="{ active: formData.cleanlinessLevel === option.value, disabled: isLocked }"
                  @click="selectOption('cleanlinessLevel', option.value)"
                >
                  {{ option.label }}
                </view>
              </view>
            </view>

            <view class="form-item">
              <view class="item-label">
                睡前整理习惯
              </view>
              <view class="item-options">
                <view
                  v-for="option in bedtimeCleanupOptions"
                  :key="option.value"
                  class="option-tag"
                  :class="{ active: formData.bedtimeCleanup === option.value, disabled: isLocked }"
                  @click="selectOption('bedtimeCleanup', option.value)"
                >
                  {{ option.label }}
                </view>
              </view>
            </view>

            <view class="form-item">
              <view class="item-label">
                是否在宿舍吃东西
              </view>
              <view class="item-options">
                <view
                  v-for="option in frequencyOptions"
                  :key="option.value"
                  class="option-tag"
                  :class="{ active: formData.eatInRoom === option.value, disabled: isLocked }"
                  @click="selectOption('eatInRoom', option.value)"
                >
                  {{ option.label }}
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- 社交偏好 -->
        <view class="form-section">
          <view class="section-title">
            <view class="title-icon" />
            <text>社交偏好</text>
          </view>

          <view class="glass-card form-card">
            <view class="form-item">
              <view class="item-label">
                社交类型
              </view>
              <view class="item-options">
                <view
                  v-for="option in socialOptions"
                  :key="option.value"
                  class="option-tag"
                  :class="{ active: formData.socialPreference === option.value, disabled: isLocked }"
                  @click="selectOption('socialPreference', option.value)"
                >
                  {{ option.label }}
                </view>
              </view>
            </view>

            <view class="form-item">
              <view class="item-label">
                是否允许访客
              </view>
              <view class="item-options">
                <view
                  v-for="option in visitorOptions"
                  :key="option.value"
                  class="option-tag"
                  :class="{ active: formData.allowVisitors === option.value, disabled: isLocked }"
                  @click="selectOption('allowVisitors', option.value)"
                >
                  {{ option.label }}
                </view>
              </view>
            </view>

            <view class="form-item">
              <view class="item-label">
                电话习惯
              </view>
              <view class="item-options">
                <view
                  v-for="option in phoneCallOptions"
                  :key="option.value"
                  class="option-tag"
                  :class="{ active: formData.phoneCallTime === option.value, disabled: isLocked }"
                  @click="selectOption('phoneCallTime', option.value)"
                >
                  {{ option.label }}
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- 学习娱乐 -->
        <view class="form-section">
          <view class="section-title">
            <view class="title-icon" />
            <text>学习娱乐</text>
          </view>

          <view class="glass-card form-card">
            <view class="form-item">
              <view class="item-label">
                在宿舍学习频率
              </view>
              <view class="item-options">
                <view
                  v-for="option in studyInRoomOptions"
                  :key="option.value"
                  class="option-tag"
                  :class="{ active: formData.studyInRoom === option.value, disabled: isLocked }"
                  @click="selectOption('studyInRoom', option.value)"
                >
                  {{ option.label }}
                </view>
              </view>
            </view>

            <view class="form-item">
              <view class="item-label">
                学习环境偏好
              </view>
              <view class="item-options">
                <view
                  v-for="option in studyEnvironmentOptions"
                  :key="option.value"
                  class="option-tag"
                  :class="{ active: formData.studyEnvironment === option.value, disabled: isLocked }"
                  @click="selectOption('studyEnvironment', option.value)"
                >
                  {{ option.label }}
                </view>
              </view>
            </view>

            <view class="form-item">
              <view class="item-label">
                电脑使用时间
              </view>
              <view class="item-options">
                <view
                  v-for="option in computerUsageOptions"
                  :key="option.value"
                  class="option-tag"
                  :class="{ active: formData.computerUsageTime === option.value, disabled: isLocked }"
                  @click="selectOption('computerUsageTime', option.value)"
                >
                  {{ option.label }}
                </view>
              </view>
            </view>

            <view class="form-item">
              <view class="item-label">
                游戏偏好
              </view>
              <view class="item-options">
                <view
                  v-for="option in gamingOptions"
                  :key="option.value"
                  class="option-tag"
                  :class="{ active: formData.gamingPreference === option.value, disabled: isLocked }"
                  @click="selectOption('gamingPreference', option.value)"
                >
                  {{ option.label }}
                </view>
              </view>
            </view>

            <view class="form-item">
              <view class="item-label">
                听音乐频率
              </view>
              <view class="item-options">
                <view
                  v-for="option in musicOptions"
                  :key="option.value"
                  class="option-tag"
                  :class="{ active: formData.musicPreference === option.value, disabled: isLocked }"
                  @click="selectOption('musicPreference', option.value)"
                >
                  {{ option.label }}
                </view>
              </view>
            </view>

            <view class="form-item">
              <view class="item-label">
                音乐音量偏好
              </view>
              <view class="item-options">
                <view
                  v-for="option in volumeOptions"
                  :key="option.value"
                  class="option-tag"
                  :class="{ active: formData.musicVolume === option.value, disabled: isLocked }"
                  @click="selectOption('musicVolume', option.value)"
                >
                  {{ option.label }}
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 底部按钮 -->
      <view v-if="!isLocked" class="bottom-actions">
        <view class="action-btn submit-btn" :class="{ disabled: submitting }" @click="handleSubmit">
          <text>{{ submitting ? '提交中...' : '提交问卷' }}</text>
        </view>
      </view>

      <!-- 底部安全区域 -->
      <view class="safe-bottom" />
    </view>

    <!-- 加载中 -->
    <view v-else class="loading-container">
      <u-loading-icon mode="circle" size="48" />
      <text class="loading-text">
        加载中...
      </text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { getSurveyDataAPI, submitSurveyAPI } from '@/api/allocation';
import type { ISurveyData } from '@/types/api';

// 加载状态
const loading = ref(true);
const submitting = ref(false);
const surveyStatus = ref(0); // 0-未填写 1-已填写 2-已锁定

// 是否锁定
const isLocked = computed(() => surveyStatus.value === 2);

// 表单数据
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

// 选项配置
const sleepScheduleOptions = [
  { label: '早睡早起', value: 0 },
  { label: '正常作息', value: 1 },
  { label: '晚睡晚起', value: 2 },
  { label: '夜猫子', value: 3 },
];

const sleepQualityOptions = [
  { label: '浅睡易醒', value: 0 },
  { label: '正常', value: 1 },
  { label: '深睡', value: 2 },
];

const snoresOptions = [
  { label: '不打呼噜', value: 0 },
  { label: '打呼噜', value: 1 },
];

const binaryOptions = [
  { label: '不敏感', value: 0 },
  { label: '敏感', value: 1 },
];

const smokingOptions = [
  { label: '不吸烟', value: 0 },
  { label: '吸烟', value: 1 },
];

const smokingToleranceOptions = [
  { label: '不接受', value: 0 },
  { label: '可以接受', value: 1 },
];

const cleanlinessOptions = [
  { label: '非常整洁', value: 1 },
  { label: '整洁', value: 2 },
  { label: '一般', value: 3 },
  { label: '随意', value: 4 },
];

const bedtimeCleanupOptions = [
  { label: '不整理', value: 0 },
  { label: '偶尔整理', value: 1 },
  { label: '经常整理', value: 2 },
  { label: '总是整理', value: 3 },
];

const frequencyOptions = [
  { label: '从不', value: 0 },
  { label: '偶尔', value: 1 },
  { label: '经常', value: 2 },
];

const socialOptions = [
  { label: '喜欢安静', value: 0 },
  { label: '中等', value: 1 },
  { label: '喜欢热闹', value: 2 },
];

const visitorOptions = [
  { label: '不允许', value: 0 },
  { label: '偶尔可以', value: 1 },
  { label: '可以', value: 2 },
];

const phoneCallOptions = [
  { label: '不喜欢在宿舍打', value: 0 },
  { label: '偶尔', value: 1 },
  { label: '不介意', value: 2 },
];

const studyInRoomOptions = [
  { label: '不在宿舍学', value: 0 },
  { label: '偶尔', value: 1 },
  { label: '经常', value: 2 },
  { label: '总是', value: 3 },
];

const studyEnvironmentOptions = [
  { label: '不需要安静', value: 0 },
  { label: '需要安静', value: 1 },
  { label: '需要轻音乐', value: 2 },
  { label: '可以接受声音', value: 3 },
];

const computerUsageOptions = [
  { label: '不用电脑', value: 0 },
  { label: '很少', value: 1 },
  { label: '正常', value: 2 },
  { label: '很多', value: 3 },
];

const gamingOptions = [
  { label: '不玩游戏', value: 0 },
  { label: '偶尔玩', value: 1 },
  { label: '经常玩', value: 2 },
];

const musicOptions = [
  { label: '不听音乐', value: 0 },
  { label: '偶尔听', value: 1 },
  { label: '经常听', value: 2 },
];

const volumeOptions = [
  { label: '喜欢小声', value: 0 },
  { label: '中等音量', value: 1 },
  { label: '喜欢大声', value: 2 },
];

// 计算进度
const progressPercent = computed(() => {
  const fields = Object.values(formData);
  const filled = fields.filter(v => v !== undefined && v !== null).length;
  return Math.round((filled / fields.length) * 100);
});

// 选择选项
function selectOption(field: keyof ISurveyData, value: number): void {
  if (isLocked.value) return;
  const data = formData as Record<string, unknown>;
  data[field] = value;
}

// 加载问卷数据
async function loadSurveyData(): Promise<void> {
  try {
    loading.value = true;
    const data = await getSurveyData();
    if (data) {
      surveyStatus.value = data.surveyStatus || 0;
      // 填充已有数据
      Object.keys(formData).forEach((key) => {
        const value = (data as any)[key];
        if (value !== undefined && value !== null) {
          const fd = formData as Record<string, unknown>;
          fd[key] = value;
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

// 提交问卷
async function handleSubmit(): Promise<void> {
  if (submitting.value || isLocked.value) return;

  // 检查是否填写完整
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
          await submitSurvey(formData);
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
</script>

<style lang="scss" scoped>
.survey-page {
  position: relative;
  min-height: 100vh;
  background: $bg-light;
}

// 背景装饰
.bg-decorations {
  position: fixed;
  inset: 0;
  z-index: 0;
  overflow: hidden;
  pointer-events: none;

  .blob {
    position: absolute;
    border-radius: 50%;
    filter: blur(80rpx);

    &.blob-1 {
      top: -5%;
      right: -10%;
      width: 500rpx;
      height: 500rpx;
      background: rgb(10 219 195 / 20%);
    }

    &.blob-2 {
      top: 30%;
      left: -15%;
      width: 400rpx;
      height: 400rpx;
      background: rgb(99 102 241 / 15%);
    }
  }
}

.page-content {
  position: relative;
  z-index: 10;
  padding: 0 $spacing-lg calc(env(safe-area-inset-bottom) + 120rpx);
}

// 状态卡片
.status-card {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: $spacing-md;
  margin-bottom: $spacing-md;
  gap: $spacing-sm;

  &.locked {
    background: rgb(239 68 68 / 10%);
    border: 1rpx solid rgb(239 68 68 / 30%);

    text {
      font-size: $font-sm;
      color: #ef4444;
    }
  }
}

// 说明卡片
.intro-card {
  display: flex;
  align-items: center;
  padding: $spacing-lg;
  margin-bottom: $spacing-lg;
  gap: $spacing-md;

  .intro-icon {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 80rpx;
    height: 80rpx;
    background: rgb(10 219 195 / 10%);
    border-radius: 20rpx;
    flex-shrink: 0;
  }

  .intro-content {
    flex: 1;

    .intro-title {
      margin-bottom: $spacing-xs;
      font-size: $font-lg;
      color: $text-main;
      font-weight: $font-bold;
    }

    .intro-desc {
      font-size: $font-sm;
      color: $text-sub;
      line-height: 1.5;
    }
  }
}

// 进度条
.progress-section {
  margin-bottom: $spacing-lg;

  .progress-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-sm;

    .progress-label {
      font-size: $font-sm;
      color: $text-sub;
    }

    .progress-value {
      font-size: $font-md;
      color: $primary;
      font-weight: $font-bold;
    }
  }

  .progress-bar {
    overflow: hidden;
    height: 12rpx;
    background: rgb(10 219 195 / 15%);
    border-radius: 6rpx;

    .progress-fill {
      height: 100%;
      background: linear-gradient(90deg, $primary 0%, #14b8a6 100%);
      border-radius: 6rpx;
      transition: width 0.3s ease;
    }
  }
}

// 表单区块
.form-section {
  margin-bottom: $spacing-lg;

  .section-title {
    display: flex;
    align-items: center;
    padding-left: 4rpx;
    gap: 12rpx;

    .title-icon {
      width: 8rpx;
      height: 32rpx;
      background: linear-gradient(180deg, $primary 0%, #6366f1 100%);
      border-radius: 4rpx;
    }

    text {
      font-size: $font-lg;
      color: $text-main;
      font-weight: $font-bold;
    }
  }
}

.form-card {
  padding: $spacing-md;
}

.form-item {
  padding: $spacing-md 0;
  border-bottom: 1rpx solid rgb(0 0 0 / 5%);

  &:last-child {
    padding-bottom: 0;
    border-bottom: none;
  }

  .item-label {
    margin-bottom: $spacing-sm;
    font-size: $font-md;
    color: $text-main;
    font-weight: $font-semibold;
  }

  .item-options {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-sm;
  }

  .option-tag {
    padding: $spacing-sm $spacing-md;
    font-size: $font-sm;
    color: $text-sub;
    background: #f5f5f5;
    border: 2rpx solid transparent;
    border-radius: $radius-full;
    transition: all 0.2s;

    &.active {
      color: $primary;
      background: rgb(10 219 195 / 10%);
      border-color: $primary;
    }

    &.disabled {
      opacity: 0.6;
      pointer-events: none;
    }
  }
}

// 底部按钮
.bottom-actions {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 100;
  display: flex;
  padding: $spacing-md $spacing-lg;
  padding-bottom: calc(env(safe-area-inset-bottom) + $spacing-md);
  background: rgb(255 255 255 / 95%);
  backdrop-filter: blur(20rpx);
  gap: $spacing-md;

  .action-btn {
    display: flex;
    justify-content: center;
    align-items: center;
    flex: 1;
    height: 88rpx;
    font-size: $font-md;
    border-radius: $radius-md;
    font-weight: $font-semibold;
    transition: all 0.2s;

    &:active {
      transform: scale(0.98);
    }

    &.disabled {
      opacity: 0.6;
      pointer-events: none;
    }
  }

  .submit-btn {
    color: #fff;
    background: linear-gradient(135deg, $primary 0%, #14b8a6 100%);
    box-shadow: 0 8rpx 24rpx rgb(10 219 195 / 30%);
  }
}

// .safe-bottom 使用全局 components.scss 定义

// 加载中
// .loading-container 使用全局 components.scss 定义
</style>
