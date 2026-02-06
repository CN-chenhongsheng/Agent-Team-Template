/**
 * 入住申请类型定义
 * @module types/api/check-in
 */

import type { IBaseEntity, IPageParams, IPageResult } from './common';

/**
 * 入住类型枚举
 */
export enum CheckInType {
  /** 长期住宿 */
  LONG_TERM = 1,
  /** 临时住宿 */
  TEMPORARY = 2,
}

/**
 * 入住申请信息
 */
export interface ICheckIn extends IBaseEntity {
  /** 学生ID */
  studentId: number;
  /** 学生姓名（冗余） */
  studentName?: string;
  /** 学号（冗余） */
  studentNo?: string;
  /** 入住类型 */
  checkInType: CheckInType;
  /** 校区编码 */
  campusCode?: string;
  /** 楼层编码 */
  floorCode?: string;
  /** 房间ID */
  roomId?: number;
  /** 房间编码 */
  roomCode?: string;
  /** 床位ID */
  bedId?: number;
  /** 床位编码 */
  bedCode?: string;
  /** 申请日期 */
  applyDate: string;
  /** 入住日期 */
  checkInDate?: string;
  /** 预计退宿日期（临时住宿） */
  expectedCheckOutDate?: string;
  /** 状态 */
  status: number;
  /** 审核人ID */
  approverId?: number;
  /** 审核人姓名 */
  approverName?: string;
  /** 审核时间 */
  approveTime?: string;
  /** 审核意见 */
  approveOpinion?: string;
  /** 申请原因/备注 */
  applyReason?: string;
  /** 备注 */
  remark?: string;
}

/**
 * 入住申请查询参数
 */
export interface ICheckInQueryParams extends IPageParams {
  /** 学生ID */
  studentId?: number;
  /** 学号 */
  studentNo?: string;
  /** 学生姓名 */
  studentName?: string;
  /** 入住类型 */
  checkInType?: CheckInType;
  /** 校区编码 */
  campusCode?: string;
  /** 楼层编码 */
  floorCode?: string;
  /** 状态 */
  status?: number;
  /** 申请日期开始 */
  applyDateStart?: string;
  /** 申请日期结束 */
  applyDateEnd?: string;
}

/**
 * 入住申请提交参数
 */
export interface ICheckInSubmitParams {
  /** 入住类型 */
  checkInType: CheckInType;
  /** 意向校区编码 */
  campusCode?: string;
  /** 意向楼层编码 */
  floorCode?: string;
  /** 意向房间ID */
  roomId?: number;
  /** 意向床位ID */
  bedId?: number;
  /** 入住日期 */
  checkInDate: string;
  /** 预计退宿日期（临时住宿） */
  expectedCheckOutDate?: string;
  /** 申请原因 */
  applyReason?: string;
}

/**
 * 入住申请审批参数
 */
export interface ICheckInApprovalParams {
  /** 申请ID */
  id: number;
  /** 审批结果 2=通过 3=拒绝 */
  status: 2 | 3;
  /** 分配的校区编码 */
  campusCode?: string;
  /** 分配的楼层编码 */
  floorCode?: string;
  /** 分配的房间ID */
  roomId?: number;
  /** 分配的床位ID */
  bedId?: number;
  /** 审批意见 */
  approveOpinion?: string;
}

/**
 * 入住申请分页响应
 */
export type ICheckInPageResult = IPageResult<ICheckIn>;
