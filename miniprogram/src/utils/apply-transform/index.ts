/**
 * 申请数据转换工具
 * @module utils/apply-transform
 */

import type {
  ApplyStatus,
  IApplyDisplay,
  IApplyListItem,
  ICheckIn,
  ICheckOut,
  IMyApply,
  IStay,
  ITransfer,
} from '@/types';
import { ApplyStatusText } from '@/types/api/common';

/**
 * 申请类型配置
 */
const APPLY_TYPE_CONFIG: Record<string, {
  typeName: string;
  icon: string;
  iconColor: string;
  bgColor: string;
}> = {
  checkIn: {
    typeName: '入住申请',
    icon: 'checkmark-circle',
    iconColor: '#22c55e',
    bgColor: 'rgba(34, 197, 94, 0.1)',
  },
  check_in: {
    typeName: '入住申请',
    icon: 'checkmark-circle',
    iconColor: '#22c55e',
    bgColor: 'rgba(34, 197, 94, 0.1)',
  },
  transfer: {
    typeName: '调宿申请',
    icon: 'reload',
    iconColor: '#6366f1',
    bgColor: 'rgba(99, 102, 241, 0.1)',
  },
  checkOut: {
    typeName: '退宿申请',
    icon: 'arrow-left',
    iconColor: '#f43f5e',
    bgColor: 'rgba(244, 63, 94, 0.1)',
  },
  check_out: {
    typeName: '退宿申请',
    icon: 'arrow-left',
    iconColor: '#f43f5e',
    bgColor: 'rgba(244, 63, 94, 0.1)',
  },
  stay: {
    typeName: '留宿申请',
    icon: 'calendar',
    iconColor: '#3b82f6',
    bgColor: 'rgba(59, 130, 246, 0.1)',
  },
};

/**
 * 申请状态样式类
 */
const STATUS_CLASS_MAP: Record<ApplyStatus, string> = {
  1: 'status-pending',
  2: 'status-approved',
  3: 'status-rejected',
  4: 'status-approved',
};

/**
 * 格式化日期为 MM-DD HH:mm 格式
 */
function formatDate(dateStr: string | undefined): string {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  return `${month}-${day} ${hours}:${minutes}`;
}

/**
 * 转换入住申请为首页展示格式
 */
export function transformCheckInToDisplay(item: ICheckIn): IApplyDisplay {
  const config = APPLY_TYPE_CONFIG.checkIn;
  return {
    id: item.id!,
    type: 'checkIn',
    typeName: config.typeName,
    icon: config.icon,
    iconColor: config.iconColor,
    bgColor: config.bgColor,
    statusText: ApplyStatusText[item.status as ApplyStatus],
    statusClass: STATUS_CLASS_MAP[item.status as ApplyStatus],
    applyDate: formatDate(item.createTime),
  };
}

/**
 * 转换调宿申请为首页展示格式
 */
export function transformTransferToDisplay(item: ITransfer): IApplyDisplay {
  const config = APPLY_TYPE_CONFIG.transfer;
  return {
    id: item.id!,
    type: 'transfer',
    typeName: config.typeName,
    icon: config.icon,
    iconColor: config.iconColor,
    bgColor: config.bgColor,
    statusText: ApplyStatusText[item.status as ApplyStatus],
    statusClass: STATUS_CLASS_MAP[item.status as ApplyStatus],
    applyDate: formatDate(item.createTime),
  };
}

/**
 * 转换退宿申请为首页展示格式
 */
export function transformCheckOutToDisplay(item: ICheckOut): IApplyDisplay {
  const config = APPLY_TYPE_CONFIG.checkOut;
  return {
    id: item.id!,
    type: 'checkOut',
    typeName: config.typeName,
    icon: config.icon,
    iconColor: config.iconColor,
    bgColor: config.bgColor,
    statusText: ApplyStatusText[item.status as ApplyStatus],
    statusClass: STATUS_CLASS_MAP[item.status as ApplyStatus],
    applyDate: formatDate(item.createTime),
  };
}

/**
 * 转换留宿申请为首页展示格式
 */
export function transformStayToDisplay(item: IStay): IApplyDisplay {
  const config = APPLY_TYPE_CONFIG.stay;
  return {
    id: item.id!,
    type: 'stay',
    typeName: config.typeName,
    icon: config.icon,
    iconColor: config.iconColor,
    bgColor: config.bgColor,
    statusText: ApplyStatusText[item.status as ApplyStatus],
    statusClass: STATUS_CLASS_MAP[item.status as ApplyStatus],
    applyDate: formatDate(item.createTime),
  };
}

/**
 * 转换入住申请为列表展示格式
 */
export function transformCheckInToListItem(item: ICheckIn): IApplyListItem {
  return {
    id: item.id!,
    type: 'checkIn',
    status: item.status as ApplyStatus,
    applyDate: item.applyDate,
    createTime: item.createTime || '',
    approveTime: item.approveTime,
    reason: item.applyReason || '',
  };
}

/**
 * 转换调宿申请为列表展示格式
 */
export function transformTransferToListItem(item: ITransfer): IApplyListItem {
  return {
    id: item.id!,
    type: 'transfer',
    status: item.status as ApplyStatus,
    applyDate: item.applyDate,
    createTime: item.createTime || '',
    approveTime: item.approveTime,
    reason: item.transferReason || '',
  };
}

/**
 * 转换退宿申请为列表展示格式
 */
export function transformCheckOutToListItem(item: ICheckOut): IApplyListItem {
  return {
    id: item.id!,
    type: 'checkOut',
    status: item.status as ApplyStatus,
    applyDate: item.applyDate,
    createTime: item.createTime || '',
    approveTime: item.approveTime,
    reason: item.checkOutReason || '',
  };
}

/**
 * 转换留宿申请为列表展示格式
 */
export function transformStayToListItem(item: IStay): IApplyListItem {
  return {
    id: item.id!,
    type: 'stay',
    status: item.status as ApplyStatus,
    applyDate: item.applyDate,
    createTime: item.createTime || '',
    approveTime: item.approveTime,
    reason: item.stayReason || '',
  };
}

/**
 * 转换统一申请VO为首页展示格式
 */
export function transformMyApplyToDisplay(item: IMyApply): IApplyDisplay {
  const config = APPLY_TYPE_CONFIG[item.applyType] || APPLY_TYPE_CONFIG.checkIn;
  return {
    id: item.id,
    type: item.applyType.replace('_', '') as 'checkIn' | 'transfer' | 'checkOut' | 'stay',
    typeName: item.applyTypeText || config.typeName,
    icon: config.icon,
    iconColor: config.iconColor,
    bgColor: config.bgColor,
    statusText: item.statusText || ApplyStatusText[item.status as ApplyStatus],
    statusClass: STATUS_CLASS_MAP[item.status as ApplyStatus],
    applyDate: formatDate(item.createTime),
  };
}

/**
 * 转换统一申请VO为列表展示格式
 */
export function transformMyApplyToListItem(item: IMyApply): IApplyListItem {
  return {
    id: item.id,
    type: item.applyType.replace('_', '') as 'checkIn' | 'transfer' | 'checkOut' | 'stay',
    status: item.status as ApplyStatus,
    applyDate: item.applyDate || '',
    createTime: item.createTime,
    approveTime: item.approveTime,
    reason: item.reason || '',
  };
}
