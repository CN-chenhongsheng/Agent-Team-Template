/**
 * API 报修管理类型定义
 *
 * 提供报修申请、维修处理、评价等报修业务相关的类型定义
 *
 * @module types/api/repair
 * @author 陈鸿昇
 */

declare namespace Api {
  /** 报修管理类型 */
  namespace RepairManage {
    /** 报修管理查询参数（与后端 RepairQueryDTO 对齐，请求时时间字段使用 startTime/endTime） */
    interface RepairSearchParams {
      studentId?: number
      studentNo?: string
      studentName?: string
      roomCode?: string
      repairType?: number
      status?: number
      urgentLevel?: number
      repairPersonId?: number
      repairPersonName?: string
      /** 关键词搜索（学生姓名/学号/房间编码） */
      keyword?: string
      /** 开始时间（ISO 或 yyyy-MM-dd），请求时对应后端 startTime */
      startTime?: string
      /** 结束时间（ISO 或 yyyy-MM-dd），请求时对应后端 endTime */
      endTime?: string
      createDateStart?: string
      createDateEnd?: string
      pageNum?: number
      pageSize?: number
    }

    /** 报修管理保存参数（与后端 RepairSaveDTO 对齐，faultImages 可为 JSON 字符串或数组） */
    interface RepairSaveParams {
      id?: number
      studentId?: number
      studentName?: string
      studentNo?: string
      roomId?: number
      roomCode?: string
      bedId?: number
      bedCode?: string
      repairType: number
      faultDescription: string
      faultImages?: string | string[]
      urgentLevel: number
      repairPersonId?: number
      repairPersonName?: string
      appointmentTime?: string
      completeTime?: string
      status?: number
      repairResult?: string
      repairImages?: string | string[]
      rating?: number
      ratingComment?: string
      remark?: string
    }

    /** 完成维修参数（repairImages 可传 string 或 string[]，接口会统一为后端要求的 JSON 字符串） */
    interface CompleteRepairParams {
      repairResult: string
      repairImages?: string | string[]
    }

    /** 报修管理列表项（与后端 RepairVO 对齐） */
    interface RepairListItem {
      id: number
      studentId?: number
      studentInfo?: Api.Common.StudentBasicInfo
      roomId?: number
      roomCode?: string
      roomName?: string
      bedId?: number
      bedCode?: string
      bedName?: string
      repairType: number
      repairTypeText?: string
      faultDescription?: string
      faultImages?: string | string[]
      urgentLevel: number
      urgentLevelText?: string
      status: number
      statusText?: string
      repairPersonId?: number
      repairPersonName?: string
      appointmentTime?: string
      completeTime?: string
      repairResult?: string
      repairImages?: string | string[]
      rating?: number
      ratingText?: string
      ratingComment?: string
      remark?: string
      createTime?: string
      updateTime?: string
      createBy?: number
      updateBy?: number
    }

    /** 报修管理分页响应 */
    interface RepairPageResponse {
      list: RepairListItem[]
      total: number
      pageNum: number
      pageSize: number
      totalPages: number
    }
  }
}
