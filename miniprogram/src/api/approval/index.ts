/**
 * 审批相关API
 * @module api/approval
 */

import { get } from '@/utils/request';
import type {
  IApprovalInstance,
  IApprovalRecord,
} from '@/types/api';

/**
 * 根据业务查询审批实例
 */
export function getApprovalInstanceByBusinessAPI(businessType: string, businessId: number) {
  return get<IApprovalInstance>({
    url: `/api/v1/system/approval/instance/business/${businessType}/${businessId}`,
  });
}

/**
 * 获取审批实例详情
 */
export function getApprovalInstanceDetailAPI(id: number) {
  return get<IApprovalInstance>({
    url: `/api/v1/system/approval/instance/${id}`,
  });
}

/**
 * 获取审批实例的审批记录
 */
export function getApprovalRecordsByInstanceAPI(instanceId: number) {
  return get<IApprovalRecord[]>({
    url: `/api/v1/system/approval/record/instance/${instanceId}`,
  });
}
