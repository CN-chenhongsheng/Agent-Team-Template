/**
 * 通用API类型定义
 * @module types/api/common
 */

import type { IPageResult, IResponse } from '@/utils/request/type';

/**
 * 分页查询参数
 */
export interface IPageParams {
  /** 当前页码 */
  pageNum?: number;
  /** 每页条数 */
  pageSize?: number;
}

/**
 * 基础实体字段
 */
export interface IBaseEntity {
  /** 主键ID */
  id?: number;
  /** 创建人 */
  createBy?: string;
  /** 创建时间 */
  createTime?: string;
  /** 更新人 */
  updateBy?: string;
  /** 更新时间 */
  updateTime?: string;
  /** 删除标志 0=正常 1=已删除 */
  delFlag?: number;
}

/**
 * 申请状态枚举
 */
export enum ApplyStatus {
  /** 待审核 */
  PENDING = 1,
  /** 已通过 */
  APPROVED = 2,
  /** 已拒绝 */
  REJECTED = 3,
  /** 已完成 */
  DONE = 4,
}

/**
 * 申请状态文本映射
 */
export const ApplyStatusText: Record<ApplyStatus, string> = {
  [ApplyStatus.PENDING]: '待审核',
  [ApplyStatus.APPROVED]: '已通过',
  [ApplyStatus.REJECTED]: '已拒绝',
  [ApplyStatus.DONE]: '已完成',
};

/**
 * 申请状态颜色映射
 */
export const ApplyStatusColor: Record<ApplyStatus, string> = {
  [ApplyStatus.PENDING]: '#FF9800',
  [ApplyStatus.APPROVED]: '#4CAF50',
  [ApplyStatus.REJECTED]: '#F44336',
  [ApplyStatus.DONE]: '#2196F3',
};

/**
 * 性别枚举
 */
export enum Gender {
  /** 未知 */
  UNKNOWN = 0,
  /** 男 */
  MALE = 1,
  /** 女 */
  FEMALE = 2,
}

/**
 * 性别文本映射
 */
export const GenderText: Record<Gender, string> = {
  [Gender.UNKNOWN]: '未知',
  [Gender.MALE]: '男',
  [Gender.FEMALE]: '女',
};

/**
 * 通用状态枚举
 */
export enum CommonStatus {
  /** 停用 */
  DISABLED = 0,
  /** 启用 */
  ENABLED = 1,
}

/**
 * 用户角色枚举
 */
export enum UserRole {
  /** 学生 */
  STUDENT = 'student',
  /** 宿管员 */
  DORM_MANAGER = 'dorm_manager',
  /** 管理员 */
  ADMIN = 'admin',
}

/**
 * 用户角色文本映射
 */
export const UserRoleText: Record<UserRole, string> = {
  [UserRole.STUDENT]: '学生',
  [UserRole.DORM_MANAGER]: '宿管员',
  [UserRole.ADMIN]: '管理员',
};

/**
 * 通用结果
 */
export interface CommonResult {
  [key: string]: any;
}

/**
 * 文件上传返回对象（单个文件）
 */
export interface UploadFileItem {
  /** 文件访问URL */
  url: string;
  /** 原始文件名 */
  name: string;
  /** 文件大小（字节） */
  size: number;
}

/**
 * 发送验证码参数
 */
export interface SendCodeParams {
  phone: number;
  code: number;
}

/**
 * 发送验证码结果
 */
export interface SendCodeResult {
  code: number;
}

// 导出响应类型
export type { IPageResult, IResponse };
