/**
 * 审批相关类型定义
 * @module types/api/approval
 */

/**
 * 审批实例
 */
export interface IApprovalInstance {
  id: number;
  flowId: number;
  flowName: string;
  businessType: string;
  businessTypeText?: string;
  businessId: number;
  applicantId: number;
  applicantName: string;
  currentNodeId?: number;
  currentNodeName?: string;
  status: number;
  statusText?: string;
  startTime?: string;
  endTime?: string;
  nodes?: IApprovalNode[];
  records?: IApprovalRecord[];
}

/**
 * 审批节点
 */
export interface IApprovalNode {
  id: number;
  flowId: number;
  nodeName: string;
  nodeOrder: number;
  nodeType: number;
  nodeTypeText?: string;
  rejectAction: number;
  rejectActionText?: string;
  assignees?: IApprovalAssignee[];
}

/**
 * 审批人
 */
export interface IApprovalAssignee {
  id: number;
  nodeId: number;
  assigneeType: number;
  assigneeId: number;
  assigneeName?: string;
}

/**
 * 审批记录
 */
export interface IApprovalRecord {
  id: number;
  instanceId: number;
  nodeId: number;
  nodeName: string;
  approverId: number;
  approverName: string;
  action: number;
  actionText?: string;
  opinion?: string;
  approveTime: string;
}
