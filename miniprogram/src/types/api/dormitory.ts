/**
 * 宿舍信息相关类型定义
 * @module types/api/dormitory
 */

/**
 * 宿舍信息
 */
export interface IDormInfo {
  campusName: string;
  buildingName: string;
  floorName: string;
  roomCode: string;
  bedCode: string;
  checkInDate: string;
}

/**
 * 室友信息
 */
export interface IRoommate {
  id: number;
  studentNo: string;
  studentName: string;
  avatar?: string;
  phone?: string;
  bedCode?: string;
}

/**
 * 校区信息
 */
export interface ICampus {
  id: number;
  campusCode: string;
  campusName: string;
  status: number;
}

/**
 * 楼层信息
 */
export interface IFloor {
  id: number;
  floorCode: string;
  floorName: string;
  campusCode: string;
  campusName: string;
  genderType: number;
  status: number;
}

/**
 * 房间信息
 */
export interface IRoom {
  id: number;
  roomCode: string;
  roomNumber: string;
  floorCode: string;
  campusCode: string;
  roomStatus: number; // 1空闲 2已满 3维修中
  capacity: number;
  occupiedCount: number;
  status: number;
}

/**
 * 床位信息
 */
export interface IBed {
  id: number;
  bedCode: string;
  bedNumber: string;
  roomId: number;
  bedStatus: number; // 1空闲 2已占用 3维修中 4已预订
  status: number;
}

/**
 * 校区列表响应
 */
export interface ICampusListResponse {
  list: ICampus[];
  total: number;
}

/**
 * 楼层列表响应
 */
export interface IFloorListResponse {
  list: IFloor[];
  total: number;
}

/**
 * 房间列表响应
 */
export interface IRoomListResponse {
  list: IRoom[];
  total: number;
}

/**
 * 床位列表响应
 */
export interface IBedListResponse {
  list: IBed[];
  total: number;
}
