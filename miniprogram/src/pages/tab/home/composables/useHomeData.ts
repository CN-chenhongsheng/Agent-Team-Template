/**
 * 首页数据加载逻辑
 */

import { computed, onMounted, ref } from 'vue';
import { storeToRefs } from 'pinia';
import type { IApplyDisplay, INoticeDisplay, IQuickService } from '@/types';
import useUserStore from '@/store/modules/user';
import { USE_MOCK } from '@/mock';
import { getMyAppliesAPI } from '@/api';
import { transformMyApplyToDisplay } from '@/utils/apply-transform';
import { useNotice } from '@/composables/useNotice';

export function useHomeData() {
  const userStore = useUserStore();
  const { userInfo, isLoggedIn } = storeToRefs(userStore);
  const { unreadCount: unreadNoticeCount, loadUnreadCount } = useNotice();

  const defaultAvatar = 'https://via.placeholder.com/150';

  // 是否是学生
  const isStudent = computed(() => userInfo.value?.role === 'student');

  // 宿舍信息
  const dormInfo = computed(() => {
    const student = userInfo.value?.studentInfo;
    if (!student || !student.floorCode || !student.roomCode) {
      return '未分配';
    }
    return `${student.floorCode} ${student.roomCode}室`;
  });

  // 当前日期
  const currentDate = computed(() => {
    const now = new Date();
    const weekDays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'];
    return `${now.getMonth() + 1}月${now.getDate()}日 ${weekDays[now.getDay()]}`;
  });

  // 问候语
  const greeting = computed((): string => {
    const hour = new Date().getHours();
    if (hour < 6) return '凌晨好';
    else if (hour < 9) return '早上好';
    else if (hour < 12) return '上午好';
    else if (hour < 14) return '中午好';
    else if (hour < 17) return '下午好';
    else if (hour < 19) return '傍晚好';
    else return '晚上好';
  });

  // 快捷服务
  const quickServices = ref<IQuickService[]>([
    { id: 1, name: '入住申请', icon: 'home', color: '#14b8a6', type: 'checkIn', path: '/pages/apply/form/index' },
    { id: 2, name: '调宿申请', icon: 'reload', color: '#6366f1', type: 'transfer', path: '/pages/apply/form/index' },
    { id: 3, name: '退宿申请', icon: 'arrow-left', color: '#f43f5e', type: 'checkOut', path: '/pages/apply/form/index' },
    { id: 4, name: '留宿申请', icon: 'calendar', color: '#3b82f6', type: 'stay', path: '/pages/apply/form/index' },
    { id: 5, name: '故障报修', icon: 'setting', color: '#f97316', type: 'repair', path: '/pages/apply/form/index' },
    { id: 6, name: '智能分配', icon: 'grid', color: '#8b5cf6', type: 'allocation', path: '/pages/allocation/survey/index' },
  ]);

  // 通知列表
  const noticeList = ref<INoticeDisplay[]>([]);

  // 申请列表
  const applyList = ref<IApplyDisplay[]>([]);

  // 水电统计数据
  const electricityData = ref({
    value: '1542.5',
    trend: -5,
    trendText: '-5% 同比',
    trendClass: 'trend-down',
    trendIcon: 'arrow-down',
    trendColor: '#22c55e',
  });

  const waterData = ref({
    value: '145.2',
    trend: 2,
    trendText: '+2% 同比',
    trendClass: 'trend-up',
    trendIcon: 'arrow-up',
    trendColor: '#ef4444',
  });

  // 用电图表配置
  const electricityChartOpts = ref({
    color: ['#0adbc3'],
    padding: [15, 2, 0, 2],
    enableScroll: false,
    legend: { show: false },
    xAxis: { disableGrid: true },
    yAxis: { gridType: 'dash', dashLength: 2, dataMin: 100, dataMax: 200, splitNumber: 5 },
    extra: { line: { type: 'curve', width: 3, activeType: 'hollow', activeRadius: 6 } },
  });

  const electricityChartData = computed(() => ({
    categories: ['1月', '4月', '8月', '12月'],
    series: [{ name: '用电量', data: [120, 180, 140, 160] }],
  }));

  // 用水图表配置
  const waterChartOpts = ref({
    color: ['#60a5fa'],
    padding: [15, 2, 0, 2],
    enableScroll: false,
    legend: { show: false },
    xAxis: { disableGrid: true },
    yAxis: { gridType: 'dash', dashLength: 2 },
    extra: { line: { type: 'curve', width: 3, activeType: 'hollow' } },
  });

  const waterChartData = computed(() => ({
    categories: ['1月', '4月', '8月', '12月'],
    series: [{ name: '用水量', data: [110, 95, 85, 75] }],
  }));

  // 从API加载申请列表
  async function loadApplyListFromAPI(): Promise<void> {
    try {
      const result = await getMyAppliesAPI({ limit: 4 });
      applyList.value = result.list.map(transformMyApplyToDisplay);
    }
    catch (error) {
      console.error('从API加载申请列表失败:', error);
      applyList.value = [];
    }
  }

  // 加载mock申请数据
  function loadMockApplyData(): void {
    applyList.value = [
      { id: 1, type: 'repair', typeName: '洗手台报修', icon: 'setting', iconColor: '#f97316', bgColor: 'rgba(249, 115, 22, 0.1)', statusText: '处理中', statusClass: 'status-processing', applyDate: '01-07 14:30' },
      { id: 2, type: 'transfer', typeName: '调宿申请', icon: 'reload', iconColor: '#6366f1', bgColor: 'rgba(99, 102, 241, 0.1)', statusText: '审核中', statusClass: 'status-pending', applyDate: '01-05 10:20' },
      { id: 3, type: 'check-in', typeName: '入住申请', icon: 'checkmark-circle', iconColor: '#22c55e', bgColor: 'rgba(34, 197, 94, 0.1)', statusText: '已通过', statusClass: 'status-approved', applyDate: '12-25' },
      { id: 4, type: 'stay', typeName: '留宿申请', icon: 'calendar', iconColor: '#3b82f6', bgColor: 'rgba(59, 130, 246, 0.1)', statusText: '已通过', statusClass: 'status-approved', applyDate: '12-20' },
    ];
  }

  // 加载数据
  async function loadData(): Promise<void> {
    try {
      // 通知列表
      noticeList.value = [
        { id: 1, title: '宿舍停水通知', desc: '因管道维修，明日9:00-17:00将暂停供水。', tag: '紧急', tagClass: 'tag-urgent', date: '10-23', icon: 'warning' },
        { id: 2, title: '宿舍文化节报名', desc: '本周五截止报名，欢迎各寝室踊跃参加！', tag: '活动', tagClass: 'tag-activity', date: '10-21', icon: 'bell' },
        { id: 3, title: '消防演练通知', desc: '本周六下午进行消防演练，请同学们积极配合。', tag: '通知', tagClass: 'tag-notice', date: '10-20', icon: 'info-circle' },
      ];

      // 申请列表
      if (USE_MOCK) {
        loadMockApplyData();
      }
      else {
        await loadApplyListFromAPI();
      }
    }
    catch (error) {
      console.error('加载首页数据失败:', error);
    }
  }

  // 初始化
  onMounted(async () => {
    await userStore.checkLoginStatus();
    loadData();
    if (isLoggedIn.value) {
      loadUnreadCount();
    }
  });

  return {
    userInfo,
    isLoggedIn,
    isStudent,
    defaultAvatar,
    dormInfo,
    currentDate,
    greeting,
    unreadNoticeCount,
    quickServices,
    noticeList,
    applyList,
    electricityData,
    waterData,
    electricityChartOpts,
    electricityChartData,
    waterChartOpts,
    waterChartData,
  };
}
