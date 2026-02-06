/**
 * 宿舍信息相关API
 * @module api/dormitory
 */

import { get } from '@/utils/request';
import type {
  IBedListResponse,
  ICampusListResponse,
  IDormInfo,
  IFloorListResponse,
  IRoomListResponse,
  IRoommate,
} from '@/types/api';

/**
 * 获取当前学生宿舍信息
 */
export function getDormInfoAPI() {
  return get<IDormInfo>({
    url: '/api/v1/student/dorm-info',
  });
}

/**
 * 获取室友列表
 */
export function getRoommatesAPI() {
  return get<IRoommate[]>({
    url: '/api/v1/student/roommates',
  });
}

/**
 * 获取校区列表
 */
export function getCampusListAPI(params?: { status?: number }) {
  return get<ICampusListResponse>({
    url: '/api/v1/system/campus/page',
    data: params,
  });
}

/**
 * 获取楼层列表（支持按校区过滤）
 */
export function getFloorListAPI(params: { campusCode?: string; status?: number }) {
  return get<IFloorListResponse>({
    url: '/api/v1/system/floor/page',
    data: params,
  });
}

/**
 * 获取房间列表（支持按楼层、校区、状态过滤）
 */
export function getRoomListAPI(params: {
  floorCode?: string;
  campusCode?: string;
  roomStatus?: number;
  status?: number;
}) {
  return get<IRoomListResponse>({
    url: '/api/v1/system/room/page',
    data: params,
  });
}

/**
 * 获取床位列表（支持按房间、楼层、校区、状态过滤）
 */
export function getBedListAPI(params: {
  roomId?: number;
  floorCode?: string;
  campusCode?: string;
  bedStatus?: number;
  status?: number;
}) {
  return get<IBedListResponse>({
    url: '/api/v1/system/bed/page',
    data: params,
  });
}
