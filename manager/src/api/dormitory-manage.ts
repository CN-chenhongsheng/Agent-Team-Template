/**
 * 宿舍管理模块 API
 * 包含楼层管理、房间管理、床位管理等接口
 *
 * @module api/dormitory-manage
 * @author 陈鸿昇
 * @date 2026-01-03
 */

import request from '@/utils/http'

/** ==================== 楼层管理 ==================== */

/**
 * 获取楼层分页列表
 * @param params 查询参数
 */
export function fetchGetFloorPage(params: Api.SystemManage.FloorSearchParams) {
  return request.get<Api.SystemManage.FloorPageResponse>({
    url: '/api/v1/system/floor/page',
    params
  })
}

/**
 * 获取楼层详情
 * @param id 楼层ID
 */
export function fetchGetFloorDetail(id: number) {
  return request.get<Api.SystemManage.FloorListItem>({
    url: `/api/v1/system/floor/${id}`
  })
}

/**
 * 新增楼层
 * @param data 楼层数据
 */
export function fetchAddFloor(data: Api.SystemManage.FloorSaveParams) {
  return request.post({
    url: '/api/v1/system/floor',
    data,
    showSuccessMessage: true
  })
}

/**
 * 编辑楼层
 * @param id 楼层ID
 * @param data 楼层数据
 */
export function fetchUpdateFloor(id: number, data: Api.SystemManage.FloorSaveParams) {
  return request.put({
    url: `/api/v1/system/floor/${id}`,
    data,
    showSuccessMessage: true
  })
}

/**
 * 删除楼层
 * @param id 楼层ID
 */
export function fetchDeleteFloor(id: number) {
  return request.del({
    url: `/api/v1/system/floor/${id}`,
    showSuccessMessage: true
  })
}

/**
 * 批量删除楼层
 * @param ids 楼层ID数组
 */
export function fetchBatchDeleteFloor(ids: number[]) {
  return request.del({
    url: '/api/v1/system/floor/batch',
    data: ids,
    showSuccessMessage: true
  })
}

/**
 * 修改楼层状态
 * @param id 楼层ID
 * @param status 状态：1正常 0停用
 */
export function fetchUpdateFloorStatus(id: number, status: number) {
  return request.put({
    url: `/api/v1/system/floor/${id}/status/${status}`,
    showSuccessMessage: true
  })
}

/**
 * 检查楼层是否被房间关联
 * @param id 楼层ID
 */
export function fetchCheckFloorHasRooms(id: number) {
  return request.get<boolean>({
    url: `/api/v1/system/floor/${id}/check-rooms`
  })
}

/**
 * 获取楼层列表（非分页，用于下拉选择）
 * @param params 查询参数
 */
export function fetchGetFloorList(params?: { status?: number; campusCode?: string }) {
  return request.get<Api.SystemManage.FloorPageResponse>({
    url: '/api/v1/system/floor/page',
    params: { ...params, pageSize: 1000 }
  })
}

/** ==================== 房间管理 ==================== */

/**
 * 获取房间分页列表
 * @param params 查询参数
 */
export function fetchGetRoomPage(params: Api.SystemManage.RoomSearchParams) {
  return request.get<Api.SystemManage.RoomPageResponse>({
    url: '/api/v1/system/room/page',
    params
  })
}

/**
 * 获取房间列表（含床位和学生信息，用于可视化视图）
 * @param floorId 楼层ID
 */
export function fetchGetRoomListWithBeds(floorId: number) {
  return request.get<Api.SystemManage.RoomWithBeds[]>({
    url: '/api/v1/system/room/visual/list',
    params: { floorId }
  })
}

/**
 * 检查房间是否被床位关联
 * @param id 房间ID
 */
export function fetchCheckRoomHasBeds(id: number) {
  return request.get<boolean>({
    url: `/api/v1/system/room/${id}/check-beds`
  })
}

/**
 * 获取房间详情
 * @param id 房间ID
 */
export function fetchGetRoomDetail(id: number) {
  return request.get<Api.SystemManage.RoomListItem>({
    url: `/api/v1/system/room/${id}`
  })
}

/**
 * 新增房间
 * @param data 房间数据
 */
export function fetchAddRoom(data: Api.SystemManage.RoomSaveParams) {
  return request.post({
    url: '/api/v1/system/room',
    data,
    showSuccessMessage: true
  })
}

/**
 * 编辑房间
 * @param id 房间ID
 * @param data 房间数据
 */
export function fetchUpdateRoom(id: number, data: Api.SystemManage.RoomSaveParams) {
  return request.put({
    url: `/api/v1/system/room/${id}`,
    data,
    showSuccessMessage: true
  })
}

/**
 * 删除房间
 * @param id 房间ID
 */
export function fetchDeleteRoom(id: number) {
  return request.del({
    url: `/api/v1/system/room/${id}`,
    showSuccessMessage: true
  })
}

/**
 * 批量删除房间
 * @param ids 房间ID数组
 */
export function fetchBatchDeleteRoom(ids: number[]) {
  return request.del({
    url: '/api/v1/system/room/batch',
    data: ids,
    showSuccessMessage: true
  })
}

/**
 * 修改房间状态
 * @param id 房间ID
 * @param status 状态：1正常 0停用
 */
export function fetchUpdateRoomStatus(id: number, status: number) {
  return request.put({
    url: `/api/v1/system/room/${id}/status/${status}`,
    showSuccessMessage: true
  })
}

/**
 * 批量创建房间
 * @param data 批量创建参数
 */
export function fetchBatchCreateRooms(data: Api.SystemManage.RoomBatchCreateParams) {
  return request.post<number>({
    url: '/api/v1/system/room/batch-create',
    data,
    showSuccessMessage: true
  })
}

/** ==================== 床位管理 ==================== */

/**
 * 获取床位分页列表
 * @param params 查询参数
 */
export function fetchGetBedPage(params: Api.SystemManage.BedSearchParams) {
  return request.get<Api.SystemManage.BedPageResponse>({
    url: '/api/v1/system/bed/page',
    params
  })
}

/**
 * 获取床位详情
 * @param id 床位ID
 */
export function fetchGetBedDetail(id: number) {
  return request.get<Api.SystemManage.BedListItem>({
    url: `/api/v1/system/bed/${id}`
  })
}

/**
 * 新增床位
 * @param data 床位数据
 */
export function fetchAddBed(data: Api.SystemManage.BedSaveParams) {
  return request.post({
    url: '/api/v1/system/bed',
    data,
    showSuccessMessage: true
  })
}

/**
 * 编辑床位
 * @param id 床位ID
 * @param data 床位数据
 */
export function fetchUpdateBed(id: number, data: Api.SystemManage.BedSaveParams) {
  return request.put({
    url: `/api/v1/system/bed/${id}`,
    data,
    showSuccessMessage: true
  })
}

/**
 * 删除床位
 * @param id 床位ID
 */
export function fetchDeleteBed(id: number) {
  return request.del({
    url: `/api/v1/system/bed/${id}`,
    showSuccessMessage: true
  })
}

/**
 * 批量删除床位
 * @param ids 床位ID数组
 */
export function fetchBatchDeleteBed(ids: number[]) {
  return request.del({
    url: '/api/v1/system/bed/batch',
    data: ids,
    showSuccessMessage: true
  })
}

/**
 * 修改床位状态
 * @param id 床位ID
 * @param status 状态：1正常 0停用
 */
export function fetchUpdateBedStatus(id: number, status: number) {
  return request.put({
    url: `/api/v1/system/bed/${id}/status/${status}`,
    showSuccessMessage: true
  })
}

/**
 * 批量创建床位
 * @param data 批量创建参数
 */
export function fetchBatchCreateBeds(data: Api.SystemManage.BedBatchCreateParams) {
  return request.post<number>({
    url: '/api/v1/system/bed/batch-create',
    data,
    showSuccessMessage: true
  })
}
