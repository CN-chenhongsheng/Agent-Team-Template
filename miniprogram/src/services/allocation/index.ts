/**
 * 智能分配 Service
 * @module services/allocation
 */

import {
  canModifySurveyAPI,
  getMyAllocationResultAPI,
  getRecommendBedsAPI,
  getRoommatesInfoAPI,
  getSurveyDataAPI,
  getSurveyStatusAPI,
  getTransferRecommendAPI,
  submitSurveyAPI,
} from '@/api/allocation';
import type {
  IAllocationResult,
  IBedRecommend,
  IRoommateInfo,
  ISurveyData,
  ISurveyStatus,
  ISurveyVO,
} from '@/types/api';

/**
 * 分配服务 - 封装问卷和分配结果相关的复杂业务逻辑
 */
export const AllocationService = {
  /**
   * 获取完整的问卷状态信息
   * 包含是否已填写、是否可修改等状态
   */
  async getSurveyFullStatus(): Promise<{
    status: ISurveyStatus;
    canModify: boolean;
    data: ISurveyVO | null;
  }> {
    const [status, canModify] = await Promise.all([
      getSurveyStatusAPI(),
      canModifySurveyAPI().catch(() => false),
    ]);

    let data: ISurveyVO | null = null;
    if (status) {
      try {
        data = await getSurveyDataAPI();
      }
      catch {
        data = null;
      }
    }

    return { status, canModify, data };
  },

  /**
   * 提交问卷数据
   */
  async submitSurvey(data: ISurveyData): Promise<void> {
    return submitSurveyAPI(data);
  },

  /**
   * 获取分配结果及室友信息
   */
  async getAllocationResultWithRoommates(): Promise<{
    result: IAllocationResult | null;
    roommates: IRoommateInfo[];
  }> {
    try {
      const [result, roommates] = await Promise.all([
        getMyAllocationResultAPI(),
        getRoommatesInfoAPI(),
      ]);
      return { result, roommates };
    }
    catch {
      return { result: null, roommates: [] };
    }
  },

  /**
   * 获取推荐床位
   */
  async getRecommendBeds(limit = 5): Promise<IBedRecommend[]> {
    return getRecommendBedsAPI(limit);
  },

  /**
   * 获取调宿推荐
   */
  async getTransferRecommendBeds(limit = 5): Promise<IBedRecommend[]> {
    return getTransferRecommendAPI(limit);
  },
};
