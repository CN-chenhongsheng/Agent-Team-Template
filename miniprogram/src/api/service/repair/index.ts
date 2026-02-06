/**
 * 报修API
 * @module api/service/repair
 */

import { get, post } from '@/utils/request';
import type {
  IRepair,
  IRepairPageResult,
  IRepairQueryParams,
  IRepairRatingParams,
  IRepairSubmitParams,
} from '@/types';

/**
 * 提交报修（小程序端）
 */
export function submitRepairAPI(data: IRepairSubmitParams) {
  return post({
    url: '/api/v1/app/repair/submit',
    data,
  });
}

/**
 * 获取我的报修列表（小程序端）
 */
export function getRepairListAPI(params: IRepairQueryParams) {
  return get<IRepairPageResult>({
    url: '/api/v1/app/repair/list',
    data: params,
  });
}

/**
 * 获取报修详情（小程序端）
 */
export function getRepairDetailAPI(id: number) {
  return get<IRepair>({
    url: `/api/v1/app/repair/detail/${id}`,
  });
}

/**
 * 取消报修（小程序端）
 */
export function cancelRepairAPI(id: number) {
  return post({
    url: `/api/v1/app/repair/cancel/${id}`,
  });
}

/**
 * 评价报修（小程序端）
 */
export function rateRepairAPI(id: number, data: IRepairRatingParams) {
  return post({
    url: `/api/v1/app/repair/rate/${id}`,
    data,
  });
}

/**
 * 上传图片
 */
export function uploadImageAPI(filePath: string) {
  return new Promise<string>((resolve, reject) => {
    uni.uploadFile({
      url: `${import.meta.env.VITE_APP_BASE_API}/api/v1/common/upload`,
      filePath,
      name: 'file',
      success: (res) => {
        if (res.statusCode === 200) {
          const data = JSON.parse(res.data);
          resolve(data.data);
        }
        else {
          reject(new Error('上传失败'));
        }
      },
      fail: reject,
    });
  });
}
