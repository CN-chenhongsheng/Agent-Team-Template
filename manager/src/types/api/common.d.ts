/**
 * API 通用类型定义
 *
 * 提供所有接口共用的基础类型，包括分页等
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

    /** 分页响应基础结构（与后端 PageResult 保持一致） */
    interface PaginatedResponse<T = any> {
      list: T[]
      total: number
      pageNum: number
      pageSize: number
      totalPages: number
    }
  }
}
