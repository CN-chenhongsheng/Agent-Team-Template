/**
 * 入住申请API
 * @module api/accommodation/check-in
 */

import { get, post } from '@/utils/request';
import type {
  ICheckIn,
  ICheckInApprovalParams,
  ICheckInPageResult,
  ICheckInQueryParams,
  ICheckInSubmitParams,
} from '@/types';

/**
 * 获取入住申请分页列表
 */
export function getCheckInPageAPI(params: ICheckInQueryParams) {
  return get<ICheckInPageResult>({
    url: '/api/v1/system/check-in/page',
    data: params,
  });
}

/**
 * 获取入住申请详情
 */
export function getCheckInDetailAPI(id: number) {
  return get<ICheckIn>({
    url: `/api/v1/system/check-in/${id}`,
  });
}

/**
 * 提交入住申请
 */
export function submitCheckInAPI(data: ICheckInSubmitParams) {
  return post({
    url: '/api/v1/system/check-in',
    data,
  });
}

/**
 * 更新入住申请
 */
export function updateCheckInAPI(id: number, data: Partial<ICheckInSubmitParams>) {
  return post({
    url: `/api/v1/system/check-in/${id}`,
    data,
  });
}

/**
 * 删除入住申请
 */
export function deleteCheckInAPI(id: number) {
  return post({
    url: `/api/v1/system/check-in/${id}/delete`,
  });
}

/**
 * 审批入住申请
 */
export function approveCheckInAPI(data: ICheckInApprovalParams) {
  return post({
    url: '/api/v1/system/check-in/approve',
    data,
  });
}

/**
 * 撤回入住申请
 */
export function cancelCheckInAPI(id: number) {
  return post({
    url: `/api/v1/system/check-in/${id}/cancel`,
  });
}
