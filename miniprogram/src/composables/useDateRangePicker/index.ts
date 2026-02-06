/**
 * 日期范围选择器组合式函数
 */

import { reactive } from 'vue';

interface DateRange {
  startDate?: string;
  endDate?: string;
}

export type DateRangePickerMode = 'range' | 'start' | 'end';

type DateTab = 'start' | 'end';

interface DateRangePickerState {
  show: boolean;
  mode: DateRangePickerMode;
  activeTab: DateTab;
  startDatePickerValue: number[];
  endDatePickerValue: number[];
  years: number[];
  months: number[];
  startDays: number[];
  endDays: number[];
  currentDateRange: DateRange | null;
  onConfirm: ((value: DateRange) => void) | null;
}

export function useDateRangePicker() {
  const state = reactive<DateRangePickerState>({
    show: false,
    mode: 'range',
    activeTab: 'start',
    startDatePickerValue: [0, 0, 0],
    endDatePickerValue: [0, 0, 0],
    years: [],
    months: [],
    startDays: [],
    endDays: [],
    currentDateRange: null,
    onConfirm: null,
  });

  /**
   * 初始化日期选项（始终初始化起止，mode 仅影响弹窗展示与确认结果）
   */
  function initDateOptions(currentValue?: DateRange) {
    const currentYear = new Date().getFullYear();
    const currentMonth = new Date().getMonth() + 1;
    const currentDay = new Date().getDate();

    state.years = [];
    state.months = [];
    for (let i = 0; i < 6; i++) {
      state.years.push(currentYear + i);
    }
    for (let i = 1; i <= 12; i++) {
      state.months.push(i);
    }

    if (currentValue?.startDate) {
      const [year, month, day] = currentValue.startDate.split('-').map(Number);
      const yearIndex = state.years.indexOf(year);
      const finalYearIndex = yearIndex >= 0 ? yearIndex : 0;
      state.startDatePickerValue = [
        finalYearIndex,
        Math.max(0, Math.min(11, month - 1)),
        Math.max(0, day - 1),
      ];
      updateStartDays();
      if (state.startDatePickerValue[2] >= state.startDays.length) {
        state.startDatePickerValue[2] = Math.max(0, state.startDays.length - 1);
      }
    }
    else {
      state.startDatePickerValue = [0, currentMonth - 1, currentDay - 1];
      updateStartDays();
      if (state.startDatePickerValue[2] >= state.startDays.length) {
        state.startDatePickerValue[2] = Math.max(0, state.startDays.length - 1);
      }
    }

    if (currentValue?.endDate) {
      const [year, month, day] = currentValue.endDate.split('-').map(Number);
      const yearIndex = state.years.indexOf(year);
      const finalYearIndex = yearIndex >= 0 ? yearIndex : 0;
      state.endDatePickerValue = [
        finalYearIndex,
        Math.max(0, Math.min(11, month - 1)),
        Math.max(0, day - 1),
      ];
      updateEndDays();
      if (state.endDatePickerValue[2] >= state.endDays.length) {
        state.endDatePickerValue[2] = Math.max(0, state.endDays.length - 1);
      }
    }
    else {
      state.endDatePickerValue = [0, currentMonth - 1, currentDay - 1];
      updateEndDays();
      if (state.endDatePickerValue[2] >= state.endDays.length) {
        state.endDatePickerValue[2] = Math.max(0, state.endDays.length - 1);
      }
    }
  }

  /**
   * 更新开始日期的天数
   */
  function updateStartDays() {
    const year = state.years[state.startDatePickerValue[0]];
    const month = state.months[state.startDatePickerValue[1]];
    const maxDay = new Date(year, month, 0).getDate();
    state.startDays = [];
    for (let i = 1; i <= maxDay; i++) {
      state.startDays.push(i);
    }
    if (state.startDatePickerValue[2] >= maxDay) {
      state.startDatePickerValue[2] = maxDay - 1;
    }
  }

  /**
   * 更新结束日期的天数
   */
  function updateEndDays() {
    const year = state.years[state.endDatePickerValue[0]];
    const month = state.months[state.endDatePickerValue[1]];
    const maxDay = new Date(year, month, 0).getDate();
    state.endDays = [];
    for (let i = 1; i <= maxDay; i++) {
      state.endDays.push(i);
    }
    if (state.endDatePickerValue[2] >= maxDay) {
      state.endDatePickerValue[2] = maxDay - 1;
    }
  }

  /**
   * 处理日期选择器变化
   */
  function handleDatePickerChange(e: { detail: { value: number[] } }) {
    const oldValue = state.activeTab === 'start'
      ? [...state.startDatePickerValue]
      : [...state.endDatePickerValue];

    if (state.activeTab === 'start') {
      state.startDatePickerValue = e.detail.value;
      if (oldValue[0] !== e.detail.value[0] || oldValue[1] !== e.detail.value[1]) {
        updateStartDays();
      }
    }
    else {
      state.endDatePickerValue = e.detail.value;
      if (oldValue[0] !== e.detail.value[0] || oldValue[1] !== e.detail.value[1]) {
        updateEndDays();
      }
    }
  }

  /**
   * 格式化日期数字
   */
  function formatDateNumber(num: number): string {
    return num < 10 ? `0${num}` : String(num);
  }

  /**
   * 打开日期范围选择器
   * @param mode 'range' 双日期 | 'start' 仅开始 | 'end' 仅结束
   */
  function openPicker(
    currentValue: DateRange,
    onConfirm: (value: DateRange) => void,
    mode: DateRangePickerMode = 'range',
  ) {
    state.currentDateRange = currentValue;
    state.onConfirm = onConfirm;
    state.mode = mode;
    state.activeTab = mode === 'end' ? 'end' : 'start';
    initDateOptions(currentValue);
    state.show = true;
  }

  /**
   * 关闭日期范围选择器
   */
  function closePicker() {
    state.show = false;
  }

  /**
   * 确认日期范围
   */
  function confirmDateRange() {
    updateStartDays();
    updateEndDays();

    const startYearIndex = Math.max(0, Math.min(state.years.length - 1, state.startDatePickerValue[0] || 0));
    const startMonthIndex = Math.max(0, Math.min(11, state.startDatePickerValue[1] || 0));
    const startDayIndex = Math.max(0, Math.min(state.startDays.length - 1, state.startDatePickerValue[2] || 0));

    const startYear = state.years[startYearIndex];
    const startMonth = state.months[startMonthIndex];
    const startDay = state.startDays[startDayIndex];

    const endYearIndex = Math.max(0, Math.min(state.years.length - 1, state.endDatePickerValue[0] || 0));
    const endMonthIndex = Math.max(0, Math.min(11, state.endDatePickerValue[1] || 0));
    const endDayIndex = Math.max(0, Math.min(state.endDays.length - 1, state.endDatePickerValue[2] || 0));

    const endYear = state.years[endYearIndex];
    const endMonth = state.months[endMonthIndex];
    const endDay = state.endDays[endDayIndex];

    const startDate = `${startYear}-${formatDateNumber(startMonth)}-${formatDateNumber(startDay)}`;
    const endDate = `${endYear}-${formatDateNumber(endMonth)}-${formatDateNumber(endDay)}`;

    const mode = state.mode;
    if (mode === 'range' && new Date(endDate) < new Date(startDate)) {
      uni.showToast({
        title: '结束日期不能早于开始日期',
        icon: 'none',
      });
      return;
    }

    if (state.onConfirm) {
      try {
        const dateValue: DateRange
          = mode === 'start'
            ? { startDate, endDate: undefined }
            : mode === 'end'
              ? { startDate: undefined, endDate }
              : { startDate, endDate };
        state.onConfirm(dateValue);
      }
      catch (error) {
        console.error('日期选择器回调执行失败:', error);
        uni.showToast({
          title: '日期选择失败，请重试',
          icon: 'none',
        });
        return;
      }
    }
    else {
      console.warn('日期选择器 onConfirm 回调未设置');
      uni.showToast({
        title: '日期选择器未初始化',
        icon: 'none',
      });
      return;
    }
    closePicker();
  }

  return {
    state,
    openPicker,
    closePicker,
    confirmDateRange,
    handleDatePickerChange,
  };
}
