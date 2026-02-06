/**
 * API 审批管理类型定义
 *
 * 包括审批流程配置、审批操作、审批实例、审批记录等类型定义
 *
 * @module types/api/approval
 * @author 陈鸿昇
 */

declare namespace Api {
  /** 审批管理类型 */
  namespace ApprovalManage {
    /** ==================== 审批流程配置 ==================== */

    /** 审批人 */
    interface ApprovalAssignee {
      id?: number
      nodeId?: number
      /** 审批人类型：1-角色 2-用户 */
      assigneeType: number
      /** 审批人ID */
      assigneeId: number
      /** 审批人姓名 */
      assigneeName?: string
      /** 审批人类型文本 */
      assigneeTypeText?: string
    }

    /** 审批节点 */
    interface ApprovalNode {
      id?: number
      flowId?: number
      /** 节点名称 */
      nodeName: string
      /** 节点顺序 */
      nodeOrder: number
      /** 节点类型：1-串行 2-会签 3-或签 */
      nodeType: number
      /** 节点类型文本 */
      nodeTypeText?: string
      /** 拒绝动作：1-直接结束 2-退回申请人 3-退回上一节点 */
      rejectAction: number
      /** 拒绝动作文本 */
      rejectActionText?: string
      /** 备注 */
      remark?: string
      /** 审批人列表 */
      assignees: ApprovalAssignee[]
      /** 创建时间 */
      createTime?: string
      /** 节点位置坐标 X（用于保存用户自定义布局） */
      x?: number
      /** 节点位置坐标 Y（用于保存用户自定义布局） */
      y?: number
      /** 临时唯一标识符（用于新节点追踪，不提交到后端） */
      tempId?: string
    }

    /** 审批流程 */
    interface ApprovalFlow {
      id?: number
      /** 流程名称 */
      flowName: string
      /** 流程编码 */
      flowCode: string
      /** 业务类型 */
      businessType: string
      /** 业务类型文本 */
      businessTypeText?: string
      /** 流程描述 */
      description?: string
      /** 状态：1-启用 0-禁用 */
      status: number
      /** 状态文本 */
      statusText?: string
      /** 备注 */
      remark?: string
      /** 审批节点列表 */
      nodes?: ApprovalNode[]
      /** 是否已绑定业务类型 */
      bound?: boolean
      /** 创建时间 */
      createTime?: string
      /** 更新时间 */
      updateTime?: string
    }

    /** 流程查询参数 */
    interface ApprovalFlowQueryParams {
      flowName?: string
      flowCode?: string
      businessType?: string
      status?: number
      pageNum?: number
      pageSize?: number
    }

    /** 流程分页响应 */
    interface ApprovalFlowPageResponse {
      list: ApprovalFlow[]
      total: number
      pageNum: number
      pageSize: number
    }

    /** 流程绑定 */
    interface ApprovalFlowBinding {
      id?: number
      /** 业务类型 */
      businessType: string
      /** 业务类型文本 */
      businessTypeText?: string
      /** 流程ID */
      flowId: number
      /** 流程名称 */
      flowName?: string
      /** 流程编码 */
      flowCode?: string
      /** 状态：1-启用 0-禁用 */
      status: number
      /** 状态文本 */
      statusText?: string
      /** 备注 */
      remark?: string
      /** 创建时间 */
      createTime?: string
      /** 更新时间 */
      updateTime?: string
    }

    /** 流程绑定参数 */
    interface ApprovalFlowBindParams {
      /** 业务类型 */
      businessType: string
      /** 流程ID */
      flowId: number
      /** 状态：1-启用 0-禁用 */
      status?: number
    }

    /** ==================== 审批实例 ==================== */

    /** 审批实例 */
    interface ApprovalInstance {
      id: number
      /** 流程ID */
      flowId: number
      /** 流程名称 */
      flowName: string
      /** 业务类型 */
      businessType: string
      /** 业务类型文本 */
      businessTypeText?: string
      /** 业务ID */
      businessId: number
      /** 申请人ID */
      applicantId: number
      /** 申请人姓名 */
      applicantName: string
      /** 学生基本信息（当业务类型为学生相关业务时返回） */
      studentInfo?: Api.Common.StudentBasicInfo
      /** 当前节点ID */
      currentNodeId?: number
      /** 当前节点名称 */
      currentNodeName?: string
      /** 状态：1-待审核 2-已通过 3-已拒绝 4-已撤回 */
      status: number
      /** 状态文本 */
      statusText?: string
      /** 开始时间 */
      startTime?: string
      /** 结束时间 */
      endTime?: string
      /** 备注 */
      remark?: string
      /** 审批节点列表 */
      nodes?: ApprovalNode[]
      /** 审批记录列表 */
      records?: ApprovalRecord[]
      /** 是否可以审批 */
      canApprove?: boolean
      /** 创建时间 */
      createTime?: string
    }

    /** 审批实例查询参数 */
    interface ApprovalInstanceQueryParams {
      businessType?: string
      applicantName?: string
      flowName?: string
      status?: number
      pageNum?: number
      pageSize?: number
    }

    /** 审批实例分页响应 */
    interface ApprovalInstancePageResponse {
      list: ApprovalInstance[]
      total: number
      pageNum: number
      pageSize: number
    }

    /** ==================== 审批记录 ==================== */

    /** 审批记录 */
    interface ApprovalRecord {
      id: number
      /** 实例ID */
      instanceId: number
      /** 节点ID */
      nodeId: number
      /** 节点名称 */
      nodeName: string
      /** 审批人ID */
      approverId: number
      /** 审批人姓名 */
      approverName: string
      /** 审批动作：1-通过 2-拒绝 */
      action: number
      /** 审批动作文本 */
      actionText?: string
      /** 审批意见 */
      opinion?: string
      /** 审批时间 */
      approveTime: string
      /** 业务类型 */
      businessType?: string
      /** 业务类型文本 */
      businessTypeText?: string
      /** 申请人姓名 */
      applicantName?: string
      /** 流程名称 */
      flowName?: string
    }

    /** 审批记录查询参数 */
    interface ApprovalRecordQueryParams {
      businessType?: string
      applicantName?: string
      approverName?: string
      action?: number
      pageNum?: number
      pageSize?: number
    }

    /** 审批记录分页响应 */
    interface ApprovalRecordPageResponse {
      list: ApprovalRecord[]
      total: number
      pageNum: number
      pageSize: number
    }

    /** ==================== 审批操作 ==================== */

    /** 审批操作参数 */
    interface ApprovalActionParams {
      /** 实例ID */
      instanceId: number
      /** 审批动作：1-通过 2-拒绝 */
      action: number
      /** 审批意见 */
      opinion?: string
    }

    /** 待办审批数量统计（key为业务类型，value为数量） */
    type PendingCountMap = Record<string, number>
  }
}
