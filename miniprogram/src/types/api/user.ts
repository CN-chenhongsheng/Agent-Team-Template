/**
 * 用户相关类型定义
 * @module types/api/user
 */

import type { IBaseEntity, IPageParams, IPageResult, UserRole } from './common';

/**
 * 学生信息
 */
export interface IStudent extends IBaseEntity {
  /** 学号 */
  studentNo: string;
  /** 姓名 */
  studentName: string;
  /** 性别 */
  gender: number;
  /** 身份证号 */
  idCard?: string;
  /** 手机号 */
  phone?: string;
  /** 邮箱 */
  email?: string;
  /** 出生日期 */
  birthDate?: string;
  /** 民族 */
  nation?: string;
  /** 政治面貌 */
  politicalStatus?: string;
  /** 入学年份 */
  enrollmentYear?: number;
  /** 学制（年） */
  schoolingLength?: number;
  /** 当前年级 */
  currentGrade?: string;
  /** 学籍状态 */
  academicStatus?: number;
  /** 家庭地址 */
  homeAddress?: string;
  /** 紧急联系人 */
  emergencyContact?: string;
  /** 紧急联系人电话 */
  emergencyPhone?: string;
  /** 家长姓名 */
  parentName?: string;
  /** 家长电话 */
  parentPhone?: string;
  /** 校区编码 */
  campusCode?: string;
  /** 院系编码 */
  deptCode?: string;
  /** 专业编码 */
  majorCode?: string;
  /** 班级ID */
  classId?: number;
  /** 班级编码 */
  classCode?: string;
  /** 楼层ID */
  floorId?: number;
  /** 楼层编码 */
  floorCode?: string;
  /** 房间ID */
  roomId?: number;
  /** 房间编码 */
  roomCode?: string;
  /** 床位ID */
  bedId?: number;
  /** 床位编码 */
  bedCode?: string;
  /** 状态 */
  status?: number;
  /** 备注 */
  remark?: string;
}

/**
 * 用户信息
 */
export interface IUser extends IBaseEntity {
  /** 用户名 */
  username: string;
  /** 昵称 */
  nickname?: string;
  /** 头像 */
  avatar?: string;
  /** 手机号 */
  phone?: string;
  /** 邮箱 */
  email?: string;
  /** 性别 */
  gender?: number;
  /** 角色 */
  role: UserRole;
  /** 角色名称 */
  roleName?: string;
  /** 状态 */
  status?: number;
  /** 学生信息（如果是学生角色） */
  studentInfo?: IStudent;
  /** 负责的楼栋/楼层（如果是宿管员） */
  managedFloors?: string[];
}

/**
 * 登录参数
 */
export interface ILoginParams {
  /** 用户名/学号 */
  username: string;
  /** 密码 */
  password: string;
  /** 验证码 */
  captcha?: string;
  /** 验证码key */
  captchaKey?: string;
}

/**
 * 登录响应
 */
export interface ILoginResponse {
  /** 访问令牌 */
  accessToken: string;
  /** 刷新令牌 */
  refreshToken?: string;
  /** 用户信息 */
  userInfo: IUser;
  /** 过期时间（秒） */
  expiresIn?: number;
}

/**
 * 用户查询参数
 */
export interface IUserQueryParams extends IPageParams {
  /** 用户名 */
  username?: string;
  /** 昵称 */
  nickname?: string;
  /** 手机号 */
  phone?: string;
  /** 角色 */
  role?: UserRole;
  /** 状态 */
  status?: number;
}

/**
 * 用户分页响应
 */
export type IUserPageResult = IPageResult<IUser>;

/**
 * 学生查询参数
 */
export interface IStudentQueryParams extends IPageParams {
  /** 学号 */
  studentNo?: string;
  /** 姓名 */
  studentName?: string;
  /** 手机号 */
  phone?: string;
  /** 校区编码 */
  campusCode?: string;
  /** 院系编码 */
  deptCode?: string;
  /** 专业编码 */
  majorCode?: string;
  /** 班级编码 */
  classCode?: string;
  /** 楼层编码 */
  floorCode?: string;
  /** 状态 */
  status?: number;
}

/**
 * 学生分页响应
 */
export type IStudentPageResult = IPageResult<IStudent>;
