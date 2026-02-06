/**
 * API 通用类型定义
 *
 * 提供所有接口共用的基础类型，包括分页、搜索、审批进度、学生基本信息等
 *
 * @module types/api/common
 * @author 陈鸿昇
 */

declare namespace Api {
  /** 通用类型 */
  namespace Common {
    /** 分页参数 */
    interface PaginationParams {
      /** 当前页码 */
      current: number
      /** 每页条数 */
      size: number
      /** 总条数 */
      total: number
    }

    /** 通用搜索参数 */
    type CommonSearchParams = Pick<PaginationParams, 'current' | 'size'>

    /** 分页响应基础结构 */
    interface PaginatedResponse<T = any> {
      records: T[]
      current: number
      size: number
      total: number
    }

    /** 启用状态 */
    type EnableStatus = '1' | '2'

    /** 审批进度节点信息 */
    interface ApprovalProgressNode {
      /** 节点ID */
      nodeId: number
      /** 节点名称 */
      nodeName: string
      /** 审批人姓名（多个用顿号分隔） */
      assigneeNames: string
      /** 节点状态：1-待处理 2-已通过 3-已拒绝 */
      status: number
      /** 节点状态文本 */
      statusText: string
      /** 审批动作文本 */
      actionText?: string
      /** 审批时间 */
      approveTime?: string
    }

    /** 审批进度信息 */
    interface ApprovalProgress {
      /** 审批状态：1-待审核 2-已通过 3-已拒绝 4-已完成 */
      status: number
      /** 审批状态文本 */
      statusText: string
      /** 申请人姓名 */
      applicantName?: string
      /** 流程发起时间 */
      startTime?: string
      /** 下一审批人姓名 */
      nextApproverName?: string
      /** 审批进度描述文本 */
      progressText: string
      /** 已完成节点数 */
      completedNodes?: number
      /** 节点总数 */
      totalNodes?: number
      /** 审批进度百分比 */
      progressPercent?: number
      /** 审批流程节点进度列表 */
      nodeTimeline?: ApprovalProgressNode[]
    }

    /** 学生基本信息（嵌套于报修/入住/床位/审批等列表与详情） */
    interface StudentBasicInfo {
      studentName?: string
      studentNo?: string
      gender?: number
      genderText?: string
      phone?: string
      idCard?: string
      email?: string
      birthDate?: string
      schoolingLength?: number
      nation?: string
      politicalStatus?: string
      campusCode?: string
      campusName?: string
      deptCode?: string
      deptName?: string
      majorCode?: string
      majorName?: string
      classId?: number
      classCode?: string
      className?: string
      floorId?: number
      floorCode?: string
      floorName?: string
      roomId?: number
      roomCode?: string
      roomName?: string
      bedId?: number
      bedCode?: string
      bedName?: string
      academicStatus?: number
      academicStatusText?: string
      enrollmentYear?: number
      currentGrade?: string
      homeAddress?: string
      emergencyContact?: string
      emergencyPhone?: string
      parentName?: string
      parentPhone?: string
    }
  }
}
