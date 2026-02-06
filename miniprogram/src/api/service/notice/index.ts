/**
 * 通知公告API
 * @module api/service/notice
 */

import { get, post } from '@/utils/request';
import type {
  INotice,
  INoticePageResult,
  INoticeQueryParams,
  INoticeSubmitParams,
} from '@/types';

/**
 * 获取通知分页列表
 */
export function getNoticePageAPI(params: INoticeQueryParams) {
  return get<INoticePageResult>({
    url: '/api/v1/service/notice/page',
    data: params,
  });
}

/**
 * 获取通知详情
 */
export function getNoticeDetailAPI(id: number) {
  return get<INotice>({
    url: `/api/v1/service/notice/${id}`,
  });
}

/**
 * 发布通知
 */
export function publishNoticeAPI(data: INoticeSubmitParams) {
  return post({
    url: '/api/v1/service/notice',
    data,
  });
}

/**
 * 更新通知
 */
export function updateNoticeAPI(id: number, data: Partial<INoticeSubmitParams>) {
  return post({
    url: `/api/v1/service/notice/${id}`,
    data,
  });
}

/**
 * 删除通知
 */
export function deleteNoticeAPI(id: number) {
  return post({
    url: `/api/v1/service/notice/${id}/delete`,
  });
}

/**
 * 标记通知为已读
 */
export function markNoticeReadAPI(id: number) {
  return post({
    url: `/api/v1/service/notice/${id}/read`,
  });
}

/**
 * 获取未读通知数量
 */
export function getUnreadNoticeCountAPI() {
  return get<number>({
    url: '/api/v1/service/notice/unread-count',
  });
}
