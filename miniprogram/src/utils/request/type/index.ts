/**
 * 统一响应结构（与后端R<T>对齐）
 * @template T 数据类型
 */
export interface IResponse<T = any> {
  /** 状态码：200成功，其他失败 */
  code: number;
  /** 提示信息 */
  message: string;
  /** 返回数据 */
  data: T;
  /** 时间戳 */
  timestamp?: number;
}

/**
 * 分页响应结构（与后端PageResult<T>对齐）
 * @template T 数据项类型
 */
export interface IPageResult<T = any> {
  /** 数据列表 */
  list: T[];
  /** 总记录数 */
  total: number;
  /** 当前页码 */
  pageNum: number;
  /** 每页条数 */
  pageSize: number;
  /** 总页数 */
  totalPages: number;
}
