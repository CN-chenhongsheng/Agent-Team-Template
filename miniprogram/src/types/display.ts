/**
 * 前端展示类型定义
 */

import type { ApplyStatus } from './api';

/** 首页通知列表展示项 */
export interface INoticeDisplay {
  id: number;
  title: string;
  desc: string;
  tag: string;
  tagClass: string;
  date: string;
  icon: string;
}

/** 首页申请列表展示项 */
export interface IApplyDisplay {
  id: number;
  type: string;
  typeName: string;
  icon: string;
  iconColor: string;
  bgColor: string;
  statusText: string;
  statusClass: string;
  applyDate: string;
}

/** 申请列表页数据项 */
export interface IApplyListItem {
  id: number;
  type: string;
  status: ApplyStatus;
  applyDate: string;
  createTime: string;
  approveTime?: string;
  reason: string;
}

/** 申请类型 */
export type ApplyType = 'normalCheckIn' | 'tempCheckIn' | 'transfer' | 'checkOut' | 'stay' | 'repair' | '';

/** 申请表单数据 */
export interface IApplyFormData {
  applyType: ApplyType;
  reason: string;
  images: string[];
  signature: string;
  parentName: string;
  parentPhone: string;
  parentAgree: string;
  stayStartDate: string;
  stayEndDate: string;
  // 报修申请字段
  repairType?: number;
  phone?: string;
  description?: string;
  urgentLevel?: number;
  // 调宿申请字段
  transferDate?: string;
  targetCampusCode?: string;
  targetFloorCode?: string;
  targetRoomId?: number;
  targetBedId?: number;
}

/** 首页快捷服务入口 */
export interface IQuickService {
  id: number;
  name: string;
  icon: string;
  color: string;
  type: string;
  path: string;
}
