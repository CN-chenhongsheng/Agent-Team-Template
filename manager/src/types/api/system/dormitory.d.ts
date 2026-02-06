/**
 * API 系统管理 - 宿舍管理类型定义
 *
 * 包括楼层、房间、床位等宿舍资源管理相关的类型定义
 *
 * @module types/api/system/dormitory
 * @author 陈鸿昇
 */

declare namespace Api {
  namespace SystemManage {
    /** ==================== 楼层管理 ==================== */
    /** 楼层查询参数 */
    interface FloorSearchParams {
      floorCode?: string
      floorName?: string
      campusCode?: string
      genderType?: number
      status?: number
      pageNum?: number
      pageSize?: number
    }

    /** 楼层保存参数 */
    interface FloorSaveParams {
      id?: number
      floorCode: string
      floorName?: string
      floorNumber: number
      campusCode: string
      genderType: number
      sort?: number
      status: number
      remark?: string
    }

    /** 楼层列表项 */
    interface FloorListItem {
      id: number
      floorCode: string
      floorName?: string
      floorNumber: number
      campusCode: string
      campusName?: string
      genderType: number
      genderTypeText?: string
      totalRooms?: number
      totalBeds?: number
      currentOccupancy?: number
      hasRooms?: boolean
      sort?: number
      status: number
      statusText?: string
      remark?: string
      createTime?: string
      updateTime?: string
    }

    /** 楼层分页响应 */
    interface FloorPageResponse extends Api.Common.PaginatedResponse<FloorListItem> {
      list: FloorListItem[]
      total: number
      pageNum: number
      pageSize: number
      totalPages: number
    }

    /** ==================== 房间管理 ==================== */
    /** 房间查询参数 */
    interface RoomSearchParams {
      roomCode?: string
      roomNumber?: string
      floorId?: number
      floorCode?: string
      campusCode?: string
      roomType?: string
      roomStatus?: number
      status?: number
      pageNum?: number
      pageSize?: number
    }

    /** 房间保存参数 */
    interface RoomSaveParams {
      id?: number
      roomCode: string
      roomNumber: string
      floorId: number
      floorNumber: number
      roomType?: string
      bedCount?: number
      maxOccupancy?: number
      area?: number
      hasAirConditioner?: number
      hasBathroom?: number
      hasBalcony?: number
      roomStatus?: number
      sort?: number
      status: number
      remark?: string
    }

    /** 房间批量创建参数 */
    interface RoomBatchCreateParams {
      floorId: number
      floorNumbers: number[]
      generateCount: number
      roomType?: string
      roomStatus?: number
      bedCount?: number
      area?: number
      maxOccupancy?: number
      status: number
      hasAirConditioner?: number
      hasBathroom?: number
      hasBalcony?: number
      remark?: string
    }

    /** 房间列表项 */
    interface RoomListItem {
      id: number
      roomCode: string
      roomNumber: string
      floorId: number
      floorNumber?: number
      floorCode?: string
      floorName?: string
      campusCode?: string
      campusName?: string
      roomType?: string
      roomTypeText?: string
      bedCount?: number
      totalBeds?: number
      currentOccupancy?: number
      maxOccupancy?: number
      area?: number
      hasAirConditioner?: number
      hasBathroom?: number
      hasBalcony?: number
      roomStatus?: number
      roomStatusText?: string
      sort?: number
      status: number
      statusText?: string
      remark?: string
      createTime?: string
      updateTime?: string
    }

    /** 房间分页响应 */
    interface RoomPageResponse extends Api.Common.PaginatedResponse<RoomListItem> {
      list: RoomListItem[]
      total: number
      pageNum: number
      pageSize: number
      totalPages: number
    }

    /**
     * 房间（含床位和学生信息）- 用于可视化视图
     * @description 房间列表含床位详情和入住学生信息
     */
    interface RoomWithBeds extends RoomListItem {
      /** 床位列表（含学生信息） */
      beds: BedListItem[]
    }

    /** ==================== 床位管理 ==================== */
    /** 床位查询参数 */
    interface BedSearchParams {
      bedCode?: string
      bedNumber?: string
      roomId?: number
      roomCode?: string
      floorId?: number
      floorCode?: string
      campusCode?: string
      bedPosition?: string
      bedStatus?: number
      status?: number
      pageNum?: number
      pageSize?: number
    }

    /** 床位保存参数 */
    interface BedSaveParams {
      id?: number
      bedCode: string
      bedNumber: string
      roomId: number | undefined
      bedPosition?: string
      bedStatus?: number
      studentId?: number
      studentName?: string
      checkInDate?: string
      checkOutDate?: string
      sort?: number
      status: number
      remark?: string
    }

    /** 床位批量创建参数 */
    interface BedBatchCreateParams {
      roomId: number
      generateCount: number
      bedPosition?: string
      bedStatus?: number
      checkInDate?: string
      checkOutDate?: string
      status: number
      remark?: string
    }

    /** 床位列表项 */
    interface BedListItem {
      id: number
      bedCode: string
      bedNumber: string
      roomId: number
      roomCode?: string
      roomNumber?: string
      floorId?: number
      floorCode?: string
      floorName?: string
      campusCode?: string
      campusName?: string
      bedPosition?: string
      bedPositionText?: string
      bedStatus?: number
      bedStatusText?: string
      studentId?: number
      studentInfo?: Api.Common.StudentBasicInfo
      checkInDate?: string
      checkOutDate?: string
      sort?: number
      status: number
      statusText?: string
      remark?: string
      createTime?: string
      updateTime?: string
    }

    /** 床位分页响应 */
    interface BedPageResponse extends Api.Common.PaginatedResponse<BedListItem> {
      list: BedListItem[]
      total: number
      pageNum: number
      pageSize: number
      totalPages: number
    }
  }
}
