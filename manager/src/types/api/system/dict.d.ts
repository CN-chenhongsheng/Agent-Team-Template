/**
 * API 系统管理 - 字典管理类型定义
 *
 * @module types/api/system/dict
 * @author 陈鸿昇
 */

declare namespace Api {
  namespace SystemManage {
    /** ==================== 字典类型管理 ==================== */
    /** 字典类型查询参数 */
    interface DictTypeSearchParams {
      dictName?: string
      dictCode?: string
      status?: number
      pageNum?: number
      pageSize?: number
    }

    /** 字典类型保存参数 */
    interface DictTypeSaveParams {
      id?: number
      dictName: string
      dictCode: string
      status?: number
      remark?: string
    }

    /** 字典类型列表项 */
    interface DictTypeListItem {
      id: number
      dictName: string
      dictCode: string
      status: number
      statusText?: string
      remark?: string
      createTime?: string
      updateTime?: string
    }

    /** 字典类型分页响应 */
    interface DictTypePageResponse extends Api.Common.PaginatedResponse<DictTypeListItem> {
      records: DictTypeListItem[]
      current: number
      size: number
      total: number
    }

    /** ==================== 字典数据管理 ==================== */
    /** 字典数据查询参数 */
    interface DictDataSearchParams {
      dictCode?: string
      label?: string
      status?: number
      pageNum?: number
      pageSize?: number
    }

    /** 字典数据保存参数 */
    interface DictDataSaveParams {
      id?: number
      dictCode: string
      label: string
      value: string
      cssClass?: string
      listClass?: string
      sort?: number
      isDefault?: number
      status?: number
      remark?: string
    }

    /** 字典数据列表项 */
    interface DictDataListItem {
      id: number
      dictCode: string
      label: string
      value: string
      cssClass?: string
      listClass?: string
      sort?: number
      isDefault?: number
      status: number
      statusText?: string
      remark?: string
      createTime?: string
      updateTime?: string
    }

    /** 字典数据分页响应 */
    interface DictDataPageResponse extends Api.Common.PaginatedResponse<DictDataListItem> {
      records: DictDataListItem[]
      current: number
      size: number
      total: number
    }

    /** 字典数据列表 */
    type DictDataList = DictDataListItem[]
  }
}
