/**
 * API 系统管理 - 日志管理类型定义
 *
 * @module types/api/system/log
 * @author 陈鸿昇
 */

declare namespace Api {
  namespace SystemManage {
    /** ==================== 操作日志管理 ==================== */
    /** 操作日志查询参数 */
    interface OperLogSearchParams {
      title?: string
      operName?: string
      businessType?: number
      status?: number
      operTimeStart?: string
      operTimeEnd?: string
      pageNum?: number
      pageSize?: number
    }

    /** 操作日志列表项 */
    interface OperLogListItem {
      id: number
      title: string
      businessType: number
      businessTypeText: string
      method: string
      requestMethod: string
      operatorType: number
      operName: string
      deviceType: number
      deviceTypeText: string
      operUrl: string
      operIp: string
      operLocation: string
      operParam: string
      jsonResult: string
      status: number
      statusText: string
      errorMsg: string
      operTime: string
      costTime: number
    }

    /** 操作日志分页响应 */
    interface OperLogPageResponse {
      list: OperLogListItem[]
      total: number
      pageNum: number
      pageSize: number
    }
  }
}
