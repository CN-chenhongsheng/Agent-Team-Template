/**
 * 调宿申请类型定义
 * @module types/api/transfer
 */

import type { IBaseEntity, IPageParams, IPageResult } from './common';

/**
 * 调宿申请信息
 */
export interface ITransfer extends IBaseEntity {
  /** 学生ID */
  studentId: number;
  /** 学生姓名（冗余） */
  studentName?: string;
  /** 学号（冗余） */
  studentNo?: string;
  /** 原校区编码 */
  originalCampusCode?: string;
  /** 原楼层编码 */
  originalFloorCode?: string;
  /** 原房间ID */
  originalRoomId?: number;
  /** 原房间编码 */
  originalRoomCode?: string;
  /** 原床位ID */
  originalBedId?: number;
  /** 原床位编码 */
  originalBedCode?: string;
  /** 目标校区编码 */
  targetCampusCode?: string;
  /** 目标楼层编码 */
  targetFloorCode?: string;
  /** 目标房间ID */
  targetRoomId?: number;
  /** 目标房间编码 */
  targetRoomCode?: string;
  /** 目标床位ID */
  targetBedId?: number;
  /** 目标床位编码 */
  targetBedCode?: string;
  /** 申请日期 */
  applyDate: string;
  /** 调宿日期 */
  transferDate?: string;
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
  /** 调宿原因 */
  transferReason?: string;
  /** 备注 */
  remark?: string;
}

/**
 * 调宿申请查询参数
 */
export interface ITransferQueryParams extends IPageParams {
  /** 学生ID */
  studentId?: number;
  /** 学号 */
  studentNo?: string;
  /** 学生姓名 */
  studentName?: string;
  /** 原楼层编码 */
  originalFloorCode?: string;
  /** 目标楼层编码 */
  targetFloorCode?: string;
  /** 状态 */
  status?: number;
  /** 申请日期开始 */
  applyDateStart?: string;
  /** 申请日期结束 */
  applyDateEnd?: string;
}

/**
 * 调宿申请提交参数
 */
export interface ITransferSubmitParams {
  /** 目标校区编码 */
  targetCampusCode?: string;
  /** 目标楼层编码 */
  targetFloorCode?: string;
  /** 目标房间ID */
  targetRoomId?: number;
  /** 目标床位ID */
  targetBedId?: number;
  /** 调宿日期 */
  transferDate: string;
  /** 调宿原因 */
  transferReason: string;
}

/**
 * 调宿申请审批参数
 */
export interface ITransferApprovalParams {
  /** 申请ID */
  id: number;
  /** 审批结果 2=通过 3=拒绝 */
  status: 2 | 3;
  /** 分配的目标校区编码 */
  targetCampusCode?: string;
  /** 分配的目标楼层编码 */
  targetFloorCode?: string;
  /** 分配的目标房间ID */
  targetRoomId?: number;
  /** 分配的目标床位ID */
  targetBedId?: number;
  /** 审批意见 */
  approveOpinion?: string;
}

/**
 * 调宿申请分页响应
 */
export type ITransferPageResult = IPageResult<ITransfer>;
