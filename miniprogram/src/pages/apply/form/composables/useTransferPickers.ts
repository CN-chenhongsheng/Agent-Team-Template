import { computed, ref, watch } from 'vue';
import type { Ref } from 'vue';
import {
  getBedListAPI,
  getCampusListAPI,
  getFloorListAPI,
  getRoomListAPI,
} from '@/api/dormitory';
import type { IBed, ICampus, IFloor, IRoom } from '@/types/api/dormitory';

interface TransferFormData {
  targetCampusCode?: string;
  targetFloorCode?: string;
  targetRoomId?: number;
  targetBedId?: number;
}

interface UseTransferPickersOptions {
  formData: Ref<TransferFormData> | TransferFormData;
}

export function useTransferPickers(options: UseTransferPickersOptions) {
  const { formData } = options;

  // 获取 formData 的响应式引用
  const getFormData = () => {
    return 'value' in formData ? formData.value : formData;
  };

  // 数据源
  const campusList = ref<ICampus[]>([]);
  const floorList = ref<IFloor[]>([]);
  const roomList = ref<IRoom[]>([]);
  const bedList = ref<IBed[]>([]);

  // 选择器显示状态
  const showCampusPicker = ref(false);
  const showFloorPicker = ref(false);
  const showRoomPicker = ref(false);
  const showBedPicker = ref(false);

  // 关闭动画状态
  const isClosingCampusPicker = ref(false);
  const isClosingFloorPicker = ref(false);
  const isClosingRoomPicker = ref(false);
  const isClosingBedPicker = ref(false);

  // 确认回调
  let campusConfirmCallback: ((value: string) => void) | null = null;
  let floorConfirmCallback: ((value: string) => void) | null = null;
  let roomConfirmCallback: ((value: number) => void) | null = null;
  let bedConfirmCallback: ((value: number) => void) | null = null;

  // u-picker 格式的选项
  const campusPickerOptions = computed(() =>
    campusList.value.map(item => ({
      text: item.campusName,
      value: item.campusCode,
    })),
  );

  const floorPickerOptions = computed(() =>
    floorList.value.map(item => ({
      text: item.floorName,
      value: item.floorCode,
    })),
  );

  const roomPickerOptions = computed(() =>
    roomList.value.map(item => ({
      text: item.roomNumber,
      value: item.id,
    })),
  );

  const bedPickerOptions = computed(() =>
    bedList.value.map(item => ({
      text: item.bedNumber,
      value: item.id,
    })),
  );

  // OptionPicker 格式的选项
  const campusOptions = computed(() =>
    campusList.value.map(item => ({
      label: item.campusName,
      value: item.campusCode,
    })),
  );

  const floorOptions = computed(() =>
    floorList.value.map(item => ({
      label: item.floorName,
      value: item.floorCode,
    })),
  );

  const roomOptions = computed(() =>
    roomList.value.map(item => ({
      label: item.roomNumber,
      value: item.id,
    })),
  );

  const bedOptions = computed(() =>
    bedList.value.map(item => ({
      label: item.bedNumber,
      value: item.id,
    })),
  );

  // 数据加载函数
  async function loadCampusList() {
    try {
      const res = await getCampusListAPI({ status: 1 });
      campusList.value = res.list || [];
      if (!res.list || res.list.length === 0) {
        uni.showToast({ title: '暂无可选校区', icon: 'none' });
      }
    }
    catch (error) {
      console.error('加载校区列表失败:', error);
      uni.showToast({ title: '加载校区列表失败', icon: 'none' });
    }
  }

  async function loadFloorList(campusCode: string) {
    try {
      const res = await getFloorListAPI({ campusCode, status: 1 });
      floorList.value = res.list || [];
      if (!res.list || res.list.length === 0) {
        uni.showToast({ title: '该校区暂无可选楼栋', icon: 'none' });
      }
    }
    catch (error) {
      console.error('加载楼栋列表失败:', error);
      uni.showToast({ title: '加载楼栋列表失败', icon: 'none' });
    }
  }

  async function loadRoomList(floorCode: string) {
    try {
      const res = await getRoomListAPI({ floorCode, status: 1 });
      // 过滤出有空床位的房间（不是已满状态）
      roomList.value = (res.list || []).filter(room => room.roomStatus !== 2);
      if (roomList.value.length === 0) {
        uni.showToast({ title: '该楼栋暂无可用房间', icon: 'none' });
      }
    }
    catch (error) {
      console.error('加载房间列表失败:', error);
      uni.showToast({ title: '加载房间列表失败', icon: 'none' });
    }
  }

  async function loadBedList(roomId: number) {
    try {
      const res = await getBedListAPI({ roomId, bedStatus: 1, status: 1 });
      bedList.value = res.list || [];
      if (!res.list || res.list.length === 0) {
        uni.showToast({ title: '该房间暂无空闲床位', icon: 'none' });
      }
    }
    catch (error) {
      console.error('加载床位列表失败:', error);
      uni.showToast({ title: '加载床位列表失败', icon: 'none' });
    }
  }

  // 打开选择器的函数
  function openCampusPicker(onConfirm: (value: string) => void) {
    if (campusList.value.length === 0) {
      uni.showToast({ title: '暂无可选校区', icon: 'none' });
      return;
    }
    campusConfirmCallback = onConfirm;
    showCampusPicker.value = true;
  }

  function openFloorPicker(onConfirm: (value: string) => void) {
    const data = getFormData();
    if (!data.targetCampusCode) {
      uni.showToast({ title: '请先选择校区', icon: 'none' });
      return;
    }
    if (floorList.value.length === 0) {
      uni.showToast({ title: '暂无可选楼栋', icon: 'none' });
      return;
    }
    floorConfirmCallback = onConfirm;
    showFloorPicker.value = true;
  }

  function openRoomPicker(onConfirm: (value: number) => void) {
    const data = getFormData();
    if (!data.targetFloorCode) {
      uni.showToast({ title: '请先选择楼栋', icon: 'none' });
      return;
    }
    if (roomList.value.length === 0) {
      uni.showToast({ title: '暂无可用房间', icon: 'none' });
      return;
    }
    roomConfirmCallback = onConfirm;
    showRoomPicker.value = true;
  }

  function openBedPicker(onConfirm: (value: number) => void) {
    const data = getFormData();
    if (!data.targetRoomId) {
      uni.showToast({ title: '请先选择房间', icon: 'none' });
      return;
    }
    if (bedList.value.length === 0) {
      uni.showToast({ title: '暂无空闲床位', icon: 'none' });
      return;
    }
    bedConfirmCallback = onConfirm;
    showBedPicker.value = true;
  }

  // 带动画的关闭函数
  function closeCampusPickerWithAnimation() {
    isClosingCampusPicker.value = true;
    setTimeout(() => {
      showCampusPicker.value = false;
      isClosingCampusPicker.value = false;
    }, 300);
  }

  function closeFloorPickerWithAnimation() {
    isClosingFloorPicker.value = true;
    setTimeout(() => {
      showFloorPicker.value = false;
      isClosingFloorPicker.value = false;
    }, 300);
  }

  function closeRoomPickerWithAnimation() {
    isClosingRoomPicker.value = true;
    setTimeout(() => {
      showRoomPicker.value = false;
      isClosingRoomPicker.value = false;
    }, 300);
  }

  function closeBedPickerWithAnimation() {
    isClosingBedPicker.value = true;
    setTimeout(() => {
      showBedPicker.value = false;
      isClosingBedPicker.value = false;
    }, 300);
  }

  // 确认选择
  // u-picker confirm 事件返回的 e.value[0] 是完整对象 {text, value}，需要提取 value 字段
  function handleCampusConfirm(e: any) {
    const selected = e.value[0];
    if (selected !== undefined && campusConfirmCallback) {
      // 提取 value 字段，兼容对象和原始值
      const value = typeof selected === 'object' ? selected.value : selected;
      campusConfirmCallback(value);
    }
    showCampusPicker.value = false;
  }

  function handleFloorConfirm(e: any) {
    const selected = e.value[0];
    if (selected !== undefined && floorConfirmCallback) {
      const value = typeof selected === 'object' ? selected.value : selected;
      floorConfirmCallback(value);
    }
    showFloorPicker.value = false;
  }

  function handleRoomConfirm(e: any) {
    const selected = e.value[0];
    if (selected !== undefined && roomConfirmCallback) {
      const value = typeof selected === 'object' ? selected.value : selected;
      roomConfirmCallback(value);
    }
    showRoomPicker.value = false;
  }

  function handleBedConfirm(e: any) {
    const selected = e.value[0];
    if (selected !== undefined && bedConfirmCallback) {
      const value = typeof selected === 'object' ? selected.value : selected;
      bedConfirmCallback(value);
    }
    showBedPicker.value = false;
  }

  // 级联加载逻辑
  function setupCascadeWatchers(formDataRef: Ref<TransferFormData> | TransferFormData) {
    const getData = () => ('value' in formDataRef ? formDataRef.value : formDataRef);

    watch(
      () => getData().targetCampusCode,
      (campusCode) => {
        if (campusCode) {
          loadFloorList(campusCode);
        }
        else {
          floorList.value = [];
          roomList.value = [];
          bedList.value = [];
        }
      },
    );

    watch(
      () => getData().targetFloorCode,
      (floorCode) => {
        if (floorCode) {
          loadRoomList(floorCode);
        }
        else {
          roomList.value = [];
          bedList.value = [];
        }
      },
    );

    watch(
      () => getData().targetRoomId,
      (roomId) => {
        if (roomId) {
          loadBedList(roomId);
        }
        else {
          bedList.value = [];
        }
      },
    );
  }

  // 初始化
  function init() {
    loadCampusList();
    setupCascadeWatchers(formData);
  }

  return {
    // 数据源
    campusList,
    floorList,
    roomList,
    bedList,
    // OptionPicker 选项
    campusOptions,
    floorOptions,
    roomOptions,
    bedOptions,
    // u-picker 选项
    campusPickerOptions,
    floorPickerOptions,
    roomPickerOptions,
    bedPickerOptions,
    // 选择器显示状态
    showCampusPicker,
    showFloorPicker,
    showRoomPicker,
    showBedPicker,
    // 关闭动画状态
    isClosingCampusPicker,
    isClosingFloorPicker,
    isClosingRoomPicker,
    isClosingBedPicker,
    // 打开选择器
    openCampusPicker,
    openFloorPicker,
    openRoomPicker,
    openBedPicker,
    // 关闭选择器（带动画）
    closeCampusPickerWithAnimation,
    closeFloorPickerWithAnimation,
    closeRoomPickerWithAnimation,
    closeBedPickerWithAnimation,
    // 确认选择
    handleCampusConfirm,
    handleFloorConfirm,
    handleRoomConfirm,
    handleBedConfirm,
    // 初始化
    init,
  };
}
