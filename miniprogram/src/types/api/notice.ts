/**
 * 通知公告类型定义
 * @module types/api/notice
 */

import type { IBaseEntity, IPageParams, IPageResult } from './common';

/**
 * 通知类型枚举
 */
export enum NoticeType {
  /** 系统通知 */
  SYSTEM = 1,
  /** 宿舍公告 */
  DORM = 2,
  /** 安全提醒 */
  SAFETY = 3,
  /** 停水停电 */
  MAINTENANCE = 4,
  /** 其他 */
  OTHER = 99,
}

/**
 * 通知类型文本映射
 */
export const NoticeTypeText: Record<NoticeType, string> = {
  [NoticeType.SYSTEM]: '系统通知',
  [NoticeType.DORM]: '宿舍公告',
  [NoticeType.SAFETY]: '安全提醒',
  [NoticeType.MAINTENANCE]: '停水停电',
  [NoticeType.OTHER]: '其他',
};

/**
 * 通知信息
 */
export interface INotice extends IBaseEntity {
  /** 标题 */
  title: string;
  /** 内容 */
  content: string;
  /** 通知类型 */
  noticeType: NoticeType;
  /** 封面图片 */
  coverImage?: string;
  /** 附件 */
  attachments?: string[];
  /** 发布人ID */
  publisherId?: number;
  /** 发布人姓名 */
  publisherName?: string;
  /** 发布时间 */
  publishTime?: string;
  /** 是否置顶 */
  isTop?: boolean;
  /** 目标楼层（为空表示全部） */
  targetFloors?: string[];
  /** 阅读次数 */
  readCount?: number;
  /** 状态 0=草稿 1=已发布 */
  status?: number;
  /** 备注 */
  remark?: string;
}

/**
 * 通知查询参数
 */
export interface INoticeQueryParams extends IPageParams {
  /** 标题 */
  title?: string;
  /** 通知类型 */
  noticeType?: NoticeType;
  /** 发布人姓名 */
  publisherName?: string;
  /** 状态 */
  status?: number;
  /** 发布时间开始 */
  publishTimeStart?: string;
  /** 发布时间结束 */
  publishTimeEnd?: string;
}

/**
 * 通知提交参数
 */
export interface INoticeSubmitParams {
  /** 标题 */
  title: string;
  /** 内容 */
  content: string;
  /** 通知类型 */
  noticeType: NoticeType;
  /** 封面图片 */
  coverImage?: string;
  /** 附件 */
  attachments?: string[];
  /** 是否置顶 */
  isTop?: boolean;
  /** 目标楼层 */
  targetFloors?: string[];
  /** 状态 0=草稿 1=发布 */
  status?: number;
}

/**
 * 通知分页响应
 */
export type INoticePageResult = IPageResult<INotice>;

/**
 * 通知阅读记录
 */
export interface INoticeRead {
  /** 通知ID */
  noticeId: number;
  /** 用户ID */
  userId: number;
  /** 阅读时间 */
  readTime: string;
}
