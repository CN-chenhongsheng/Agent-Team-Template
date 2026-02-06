/**
 * API 系统管理 - 通知管理类型定义
 *
 * @module types/api/system/notice
 * @author 陈鸿昇
 */

declare namespace Api {
  namespace SystemManage {
    /** ==================== 通知管理 ==================== */
    /** 通知查询参数 */
    interface NoticeSearchParams {
      /** 通知标题（模糊查询） */
      title?: string
      /** 通知类型（字典 notice_type） */
      noticeType?: number
      /** 发布人姓名（模糊查询） */
      publisherName?: string
      /** 状态：1-草稿 2-已发布 */
      status?: number
      /** 发布时间开始 */
      publishTimeStart?: string
      /** 发布时间结束 */
      publishTimeEnd?: string
      pageNum?: number
      pageSize?: number
    }

    /** 通知保存参数 */
    interface NoticeSaveParams {
      id?: number
      /** 通知标题 */
      title: string
      /** 通知类型 */
      noticeType?: number
      /** 通知内容（富文本） */
      content: string
      /** 封面图片 URL */
      coverImage?: string
      /** 附件 URL 列表（JSON 字符串或数组） */
      attachments?: string | string[]
      /** 是否置顶 */
      isTop?: boolean
      /** 状态：1-草稿 2-已发布 */
      status: number
      /** 目标楼层编码列表（不选则全部） */
      targetFloors?: string[]
      /** 备注 */
      remark?: string
    }

    /** 通知列表项 */
    interface NoticeListItem {
      id: number
      /** 通知标题 */
      title: string
      /** 通知类型 */
      noticeType: number
      /** 通知类型文本 */
      noticeTypeText?: string
      /** 通知内容（富文本 HTML） */
      content?: string
      /** 封面图片 URL */
      coverImage?: string
      /** 附件 URL 列表（JSON 字符串或数组） */
      attachments?: string | string[]
      /** 是否置顶 */
      isTop?: boolean
      /** 状态：1-草稿 2-已发布 */
      status: number
      /** 状态文本 */
      statusText?: string
      /** 发布人 ID */
      publisherId?: number
      /** 发布人姓名 */
      publisherName?: string
      /** 发布时间 */
      publishTime?: string
      /** 阅读次数 */
      readCount?: number
      /** 目标楼层编码列表（逗号分隔字符串或数组） */
      targetFloors?: string | string[]
      /** 目标楼层名称列表（逗号分隔字符串） */
      targetFloorNames?: string
      /** 备注 */
      remark?: string
      createTime?: string
      updateTime?: string
    }

    /** 通知分页响应 */
    interface NoticePageResponse extends Api.Common.PaginatedResponse<NoticeListItem> {
      records: NoticeListItem[]
      current: number
      size: number
      total: number
    }
  }
}
