/**
 * 报修 Service
 * @module services/repair
 */

import {
  cancelRepairAPI,
  getRepairDetailAPI,
  getRepairListAPI,
  rateRepairAPI,
  submitRepairAPI,
} from '@/api/service';
import type {
  IRepair,
  IRepairPageResult,
  IRepairQueryParams,
  IRepairRatingParams,
  IRepairSubmitParams,
} from '@/types';

/**
 * 报修服务 - 封装报修相关业务逻辑
 */
export const RepairService = {
  /**
   * 获取报修列表（分页）
   */
  async getRepairList(params: IRepairQueryParams): Promise<IRepairPageResult> {
    return getRepairListAPI(params);
  },

  /**
   * 获取报修详情
   */
  async getRepairDetail(id: number): Promise<IRepair> {
    return getRepairDetailAPI(id);
  },

  /**
   * 提交报修
   */
  async submitRepair(data: IRepairSubmitParams): Promise<void> {
    return submitRepairAPI(data);
  },

  /**
   * 取消报修
   */
  async cancelRepair(id: number): Promise<void> {
    return cancelRepairAPI(id);
  },

  /**
   * 提交评价
   */
  async submitRating(id: number, data: IRepairRatingParams): Promise<void> {
    return rateRepairAPI(id, data);
  },
};
