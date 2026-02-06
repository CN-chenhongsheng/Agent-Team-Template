/**
 * 退宿申请API
 * @module api/accommodation/check-out
 */

import { get, post } from '@/utils/request';
import type {
  ICheckOut,
  ICheckOutApprovalParams,
  ICheckOutPageResult,
  ICheckOutQueryParams,
  ICheckOutSubmitParams,
} from '@/types';

/**
 * 获取退宿申请分页列表
 */
export function getCheckOutPageAPI(params: ICheckOutQueryParams) {
  return get<ICheckOutPageResult>({
    url: '/api/v1/system/check-out/page',
    data: params,
  });
}

/**
 * 获取退宿申请详情
 */
export function getCheckOutDetailAPI(id: number) {
  return get<ICheckOut>({
    url: `/api/v1/system/check-out/${id}`,
  });
}

/**
 * 提交退宿申请
 */
export function submitCheckOutAPI(data: ICheckOutSubmitParams) {
  return post({
    url: '/api/v1/system/check-out',
    data,
  });
}

/**
 * 更新退宿申请
 */
export function updateCheckOutAPI(id: number, data: Partial<ICheckOutSubmitParams>) {
  return post({
    url: `/api/v1/system/check-out/${id}`,
    data,
  });
}

/**
 * 删除退宿申请
 */
export function deleteCheckOutAPI(id: number) {
  return post({
    url: `/api/v1/system/check-out/${id}/delete`,
  });
}

/**
 * 审批退宿申请
 */
export function approveCheckOutAPI(data: ICheckOutApprovalParams) {
  return post({
    url: '/api/v1/system/check-out/approve',
    data,
  });
}

/**
 * 撤回退宿申请
 */
export function cancelCheckOutAPI(id: number) {
  return post({
    url: `/api/v1/system/check-out/${id}/cancel`,
  });
}
