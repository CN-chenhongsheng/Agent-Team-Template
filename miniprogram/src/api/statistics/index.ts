/**
 * 统计数据API
 * @module api/statistics
 */

import { get } from '@/utils/request';
import type { IApplyDetail, IApplyStatistics, IMyApplyPageResult, IMyApplyQueryParams, IStudentHomeStatistics } from '@/types';

/**
 * 获取学生端首页统计数据
 */
export function getStudentHomeStatisticsAPI() {
  return get<IStudentHomeStatistics>({
    url: '/api/v1/statistics/student-home',
  });
}

/**
 * 获取申请统计数据
 */
export function getApplyStatisticsAPI() {
  return get<IApplyStatistics>({
    url: '/api/v1/statistics/apply',
  });
}

/**
 * 获取我的申请列表（合并所有类型）
 */
export function getMyAppliesAPI(params: IMyApplyQueryParams) {
  return get<IMyApplyPageResult>({
    url: '/api/v1/statistics/my-applies',
    data: params,
  });
}

/**
 * 获取申请详情
 * @param applyId - 申请ID
 * @param applyType - 申请类型（check_in/transfer/check_out/stay）
 */
export function getApplyDetailAPI(applyId: number, applyType: string) {
  return get<IApplyDetail>({
    url: '/api/v1/statistics/apply-detail',
    data: { applyId, applyType },
  });
}
