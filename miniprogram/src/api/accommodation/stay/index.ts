/**
 * 留宿申请API
 * @module api/accommodation/stay
 */

import { get, post } from '@/utils/request';
import type {
  IStay,
  IStayApprovalParams,
  IStayPageResult,
  IStayQueryParams,
  IStaySubmitParams,
} from '@/types';

/**
 * 获取留宿申请分页列表
 */
export function getStayPageAPI(params: IStayQueryParams) {
  return get<IStayPageResult>({
    url: '/api/v1/system/stay/page',
    data: params,
  });
}

/**
 * 获取留宿申请详情
 */
export function getStayDetailAPI(id: number) {
  return get<IStay>({
    url: `/api/v1/system/stay/${id}`,
  });
}

/**
 * 提交留宿申请
 */
export function submitStayAPI(data: IStaySubmitParams) {
  return post({
    url: '/api/v1/system/stay',
    data,
  });
}

/**
 * 更新留宿申请
 */
export function updateStayAPI(id: number, data: Partial<IStaySubmitParams>) {
  return post({
    url: `/api/v1/system/stay/${id}`,
    data,
  });
}

/**
 * 删除留宿申请
 */
export function deleteStayAPI(id: number) {
  return post({
    url: `/api/v1/system/stay/${id}/delete`,
  });
}

/**
 * 审批留宿申请
 */
export function approveStayAPI(data: IStayApprovalParams) {
  return post({
    url: '/api/v1/system/stay/approve',
    data,
  });
}

/**
 * 撤回留宿申请
 */
export function cancelStayAPI(id: number) {
  return post({
    url: `/api/v1/system/stay/${id}/cancel`,
  });
}
