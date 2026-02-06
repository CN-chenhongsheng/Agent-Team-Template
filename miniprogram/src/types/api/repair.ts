/**
 * 报修类型定义
 * @module types/api/repair
 */

import type { IBaseEntity, IPageParams, IPageResult } from './common';

/**
 * 报修类型枚举
 */
export enum RepairType {
  /** 水电 */
  WATER_ELECTRICITY = 1,
  /** 门窗 */
  DOOR_WINDOW = 2,
  /** 家具 */
  FURNITURE = 3,
  /** 网络 */
  NETWORK = 4,
  /** 其他 */
  OTHER = 5,
}

/**
 * 报修类型文本映射
 */
export const RepairTypeText: Record<RepairType, string> = {
  [RepairType.WATER_ELECTRICITY]: '水电',
  [RepairType.DOOR_WINDOW]: '门窗',
  [RepairType.FURNITURE]: '家具',
  [RepairType.NETWORK]: '网络',
  [RepairType.OTHER]: '其他',
};

/**
 * 报修状态枚举
 */
export enum RepairStatus {
  /** 待接单 */
  PENDING = 1,
  /** 已接单 */
  ACCEPTED = 2,
  /** 维修中 */
  IN_PROGRESS = 3,
  /** 已完成 */
  COMPLETED = 4,
  /** 已取消 */
  CANCELLED = 5,
}

/**
 * 报修状态文本映射
 */
export const RepairStatusText: Record<RepairStatus, string> = {
  [RepairStatus.PENDING]: '待接单',
  [RepairStatus.ACCEPTED]: '已接单',
  [RepairStatus.IN_PROGRESS]: '维修中',
  [RepairStatus.COMPLETED]: '已完成',
  [RepairStatus.CANCELLED]: '已取消',
};

/**
 * 紧急程度枚举
 */
export enum UrgentLevel {
  /** 一般 */
  NORMAL = 1,
  /** 紧急 */
  URGENT = 2,
  /** 非常紧急 */
  VERY_URGENT = 3,
}

/**
 * 紧急程度文本映射
 */
export const UrgentLevelText: Record<UrgentLevel, string> = {
  [UrgentLevel.NORMAL]: '一般',
  [UrgentLevel.URGENT]: '紧急',
  [UrgentLevel.VERY_URGENT]: '非常紧急',
};

/**
 * 报修信息
 */
export interface IRepair extends IBaseEntity {
  /** 学生ID */
  studentId: number;
  /** 学生姓名 */
  studentName?: string;
  /** 学号 */
  studentNo?: string;
  /** 房间ID */
  roomId?: number;
  /** 房间编码 */
  roomCode?: string;
  /** 床位ID */
  bedId?: number;
  /** 床位编码 */
  bedCode?: string;
  /** 报修类型 */
  repairType: number;
  /** 报修类型文本 */
  repairTypeText?: string;
  /** 故障描述 */
  faultDescription: string;
  /** 故障图片 */
  faultImages?: string[];
  /** 紧急程度 */
  urgentLevel: number;
  /** 紧急程度文本 */
  urgentLevelText?: string;
  /** 状态 */
  status: number;
  /** 状态文本 */
  statusText?: string;
  /** 维修人员ID */
  repairPersonId?: number;
  /** 维修人员姓名 */
  repairPersonName?: string;
  /** 预约时间 */
  appointmentTime?: string;
  /** 完成时间 */
  completeTime?: string;
  /** 维修结果描述 */
  repairResult?: string;
  /** 维修后图片 */
  repairImages?: string[];
  /** 评分 1-5星 */
  rating?: number;
  /** 评分文本 */
  ratingText?: string;
  /** 评价内容 */
  ratingComment?: string;
  /** 备注 */
  remark?: string;
}

/**
 * 报修查询参数
 */
export interface IRepairQueryParams extends IPageParams {
  /** 学生ID */
  studentId?: number;
  /** 学号 */
  studentNo?: string;
  /** 学生姓名 */
  studentName?: string;
  /** 报修类型 */
  repairType?: number;
  /** 房间ID */
  roomId?: number;
  /** 状态 */
  status?: number;
  /** 创建时间开始 */
  createTimeStart?: string;
  /** 创建时间结束 */
  createTimeEnd?: string;
}

/**
 * 报修提交参数
 */
export interface IRepairSubmitParams {
  /** 学生ID */
  studentId?: number;
  /** 学生姓名 */
  studentName?: string;
  /** 学号 */
  studentNo?: string;
  /** 房间ID */
  roomId?: number;
  /** 房间编码 */
  roomCode?: string;
  /** 床位ID */
  bedId?: number;
  /** 床位编码 */
  bedCode?: string;
  /** 报修类型 */
  repairType: number;
  /** 故障描述 */
  faultDescription: string;
  /** 故障图片 */
  faultImages?: string[];
  /** 紧急程度 */
  urgentLevel: number;
  /** 预约时间 */
  appointmentTime?: string;
  /** 备注 */
  remark?: string;
}

/**
 * 报修评价参数
 */
export interface IRepairRatingParams {
  /** 评分 1-5 */
  rating: number;
  /** 评价内容 */
  ratingComment?: string;
}

/**
 * 报修分页响应
 */
export type IRepairPageResult = IPageResult<IRepair>;
