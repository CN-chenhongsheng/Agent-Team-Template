/**
 * 留宿申请类型定义
 * @module types/api/stay
 */

import type { IBaseEntity, IPageParams, IPageResult } from './common';

/**
 * 留宿申请信息
 */
export interface IStay extends IBaseEntity {
  /** 学生ID */
  studentId: number;
  /** 学生姓名（冗余） */
  studentName?: string;
  /** 学号（冗余） */
  studentNo?: string;
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
  /** 留宿开始日期 */
  stayStartDate: string;
  /** 留宿结束日期 */
  stayEndDate: string;
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
  /** 留宿理由 */
  stayReason: string;
  /** 备注 */
  remark?: string;
}

/**
 * 留宿申请查询参数
 */
export interface IStayQueryParams extends IPageParams {
  /** 学生ID */
  studentId?: number;
  /** 学号 */
  studentNo?: string;
  /** 学生姓名 */
  studentName?: string;
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
  /** 留宿开始日期 */
  stayStartDate?: string;
  /** 留宿结束日期 */
  stayEndDate?: string;
}

/**
 * 留宿申请提交参数
 */
export interface IStaySubmitParams {
  /** 留宿开始日期 */
  stayStartDate: string;
  /** 留宿结束日期 */
  stayEndDate: string;
  /** 留宿理由 */
  stayReason: string;
  /** 家长姓名 */
  parentName: string;
  /** 家长电话 */
  parentPhone: string;
  /** 家长是否同意：agree-同意 disagree-不同意 */
  parentAgree: string;
  /** 本人签名图片URL数组 */
  signature: string[];
  /** 附件图片URL数组 */
  images?: string[];
}

/**
 * 留宿申请审批参数
 */
export interface IStayApprovalParams {
  /** 申请ID */
  id: number;
  /** 审批结果 2=通过 3=拒绝 */
  status: 2 | 3;
  /** 审批意见 */
  approveOpinion?: string;
}

/**
 * 留宿申请分页响应
 */
export type IStayPageResult = IPageResult<IStay>;
