/**
 * 调宿申请API
 * @module api/accommodation/transfer
 */

import { get, post } from '@/utils/request';
import type {
  ITransfer,
  ITransferApprovalParams,
  ITransferPageResult,
  ITransferQueryParams,
  ITransferSubmitParams,
} from '@/types';

/**
 * 获取调宿申请分页列表
 */
export function getTransferPageAPI(params: ITransferQueryParams) {
  return get<ITransferPageResult>({
    url: '/api/v1/system/transfer/page',
    data: params,
  });
}

/**
 * 获取调宿申请详情
 */
export function getTransferDetailAPI(id: number) {
  return get<ITransfer>({
    url: `/api/v1/system/transfer/${id}`,
  });
}

/**
 * 提交调宿申请
 */
export function submitTransferAPI(data: ITransferSubmitParams) {
  return post({
    url: '/api/v1/system/transfer',
    data,
  });
}

/**
 * 更新调宿申请
 */
export function updateTransferAPI(id: number, data: Partial<ITransferSubmitParams>) {
  return post({
    url: `/api/v1/system/transfer/${id}`,
    data,
  });
}

/**
 * 删除调宿申请
 */
export function deleteTransferAPI(id: number) {
  return post({
    url: `/api/v1/system/transfer/${id}/delete`,
  });
}

/**
 * 审批调宿申请
 */
export function approveTransferAPI(data: ITransferApprovalParams) {
  return post({
    url: '/api/v1/system/transfer/approve',
    data,
  });
}

/**
 * 撤回调宿申请
 */
export function cancelTransferAPI(id: number) {
  return post({
    url: `/api/v1/system/transfer/${id}/cancel`,
  });
}
