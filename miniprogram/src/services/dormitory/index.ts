/**
 * 宿舍 Service
 * @module services/dormitory
 */

import {
  getBedListAPI,
  getCampusListAPI,
  getDormInfoAPI,
  getFloorListAPI,
  getRoomListAPI,
  getRoommatesAPI,
} from '@/api/dormitory';
import type {
  IBedListResponse,
  ICampusListResponse,
  IDormInfo,
  IFloorListResponse,
  IRoomListResponse,
  IRoommate,
} from '@/types/api';

/**
 * 宿舍服务 - 封装宿舍信息相关业务逻辑
 */
export const DormitoryService = {
  /**
   * 获取当前学生宿舍信息
   */
  async getDormInfo(): Promise<IDormInfo> {
    return getDormInfoAPI();
  },

  /**
   * 获取室友列表
   */
  async getRoommates(): Promise<IRoommate[]> {
    return getRoommatesAPI();
  },

  /**
   * 获取校区列表
   */
  async getCampusList(params?: { status?: number }): Promise<ICampusListResponse> {
    return getCampusListAPI(params);
  },

  /**
   * 获取楼层列表
   */
  async getFloors(params: { campusCode?: string; status?: number }): Promise<IFloorListResponse> {
    return getFloorListAPI(params);
  },

  /**
   * 获取房间列表
   */
  async getRooms(params: {
    floorCode?: string;
    campusCode?: string;
    roomStatus?: number;
    status?: number;
  }): Promise<IRoomListResponse> {
    return getRoomListAPI(params);
  },

  /**
   * 获取床位列表
   */
  async getBeds(params: {
    roomId?: number;
    floorCode?: string;
    campusCode?: string;
    bedStatus?: number;
    status?: number;
  }): Promise<IBedListResponse> {
    return getBedListAPI(params);
  },
};
