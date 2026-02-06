/**
 * 统计数据类型定义
 * @module types/api/statistics
 */

import type { IPageParams, IPageResult } from './common';

/**
 * 首页统计数据（学生端）
 */
export interface IStudentHomeStatistics {
  /** 待审核申请数 */
  pendingApplyCount: number;
  /** 待处理报修数 */
  pendingRepairCount: number;
  /** 未读通知数 */
  unreadNoticeCount: number;
  /** 我的宿舍信息 */
  dormInfo?: {
    /** 校区名称 */
    campusName?: string;
    /** 楼栋名称 */
    floorName?: string;
    /** 房间号 */
    roomCode?: string;
    /** 床位号 */
    bedCode?: string;
  };
}

/**
 * 申请统计数据
 */
export interface IApplyStatistics {
  /** 全部申请数 */
  allCount: number;
  /** 待审核申请数 */
  pendingCount: number;
  /** 已通过申请数 */
  approvedCount: number;
  /** 已拒绝申请数 */
  rejectedCount: number;
}

/**
 * 我的申请查询参数
 */
export interface IMyApplyQueryParams extends IPageParams {
  /** 申请状态：1-待审核，2-已通过，3-已拒绝，4-已完成 */
  status?: number;
  /** 限制返回数量（用于首页等场景） */
  limit?: number;
}

/**
 * 我的申请 VO
 */
export interface IMyApply {
  /** 申请ID */
  id: number;
  /** 申请类型：check_in-入住，transfer-调宿，check_out-退宿，stay-留宿 */
  applyType: string;
  /** 申请类型文本 */
  applyTypeText?: string;
  /** 申请状态：1-待审核，2-已通过，3-已拒绝，4-已完成 */
  status: number;
  /** 状态文本 */
  statusText?: string;
  /** 申请日期（yyyy-MM-dd 格式） */
  applyDate?: string;
  /** 创建时间 */
  createTime: string;
  /** 审批时间 */
  approveTime?: string;
  /** 申请原因 */
  reason?: string;
}

/**
 * 我的申请分页结果
 */
export type IMyApplyPageResult = IPageResult<IMyApply>;

/**
 * 申请详情 VO
 */
export interface IApplyDetail {
  /** 申请ID */
  id: number;
  /** 申请类型：check_in-入住，transfer-调宿，check_out-退宿，stay-留宿 */
  applyType: string;
  /** 申请类型文本 */
  applyTypeText?: string;
  /** 申请状态：1-待审核，2-已通过，3-已拒绝，4-已完成 */
  status: number;
  /** 状态文本 */
  statusText?: string;
  /** 申请日期（yyyy-MM-dd 格式） */
  applyDate?: string;
  /** 创建时间 */
  createTime: string;
  /** 审批时间 */
  approveTime?: string;
  /** 申请原因 */
  reason?: string;

  // 入住申请特有
  /** 入住类型：1-新生入住，2-插班入住，3-返校入住 */
  checkInType?: number;
  /** 入住类型文本 */
  checkInTypeText?: string;
  /** 预计退宿日期 */
  expectedCheckOutDate?: string;
  /** 入住日期 */
  checkInDate?: string;

  // 调宿申请特有
  /** 原宿舍地址 */
  originalDormAddress?: string;
  /** 目标宿舍地址 */
  targetDormAddress?: string;
  /** 调宿日期 */
  transferDate?: string;

  // 退宿申请特有
  /** 退宿日期 */
  checkOutDate?: string;

  // 留宿申请特有
  /** 留宿开始日期 */
  stayStartDate?: string;
  /** 留宿结束日期 */
  stayEndDate?: string;
  /** 家长姓名 */
  parentName?: string;
  /** 家长电话 */
  parentPhone?: string;
  /** 家长是否同意：0-不同意，1-同意 */
  parentAgree?: string;
  /** 家长是否同意文本 */
  parentAgreeText?: string;
}
