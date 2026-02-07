/**
 * 申请相关的工具函数和常量
 */

import type { ApplyStatus } from '@/types';
import { ApplyStatusColor, ApplyStatusText } from '@/types';

/**
 * 申请类型图标配置接口
 */
export interface ApplyTypeIconConfig {
  icon: string;
  iconColor: string;
  bgColor: string;
}

/**
 * 申请类型名称映射
 */
export const APPLY_TYPE_NAME_MAP: Record<string, string> = {
  checkIn: '入住申请',
  normalCheckIn: '正常入住',
  tempCheckIn: '临时入住',
  transfer: '调宿申请',
  checkOut: '退宿申请',
  stay: '留宿申请',
};

/**
 * 申请类型图标映射
 */
export const APPLY_TYPE_ICON_MAP: Record<string, ApplyTypeIconConfig> = {
  checkIn: { icon: 'home', iconColor: '#14b8a6', bgColor: 'rgba(20, 184, 166, 0.1)' },
  normalCheckIn: { icon: 'home', iconColor: '#14b8a6', bgColor: 'rgba(20, 184, 166, 0.1)' },
  tempCheckIn: { icon: 'home', iconColor: '#14b8a6', bgColor: 'rgba(20, 184, 166, 0.1)' },
  transfer: { icon: 'reload', iconColor: '#6366f1', bgColor: 'rgba(99, 102, 241, 0.1)' },
  checkOut: { icon: 'arrow-left', iconColor: '#f43f5e', bgColor: 'rgba(244, 63, 94, 0.1)' },
  stay: { icon: 'calendar', iconColor: '#3b82f6', bgColor: 'rgba(59, 130, 246, 0.1)' },
};

/**
 * 默认图标配置
 */
export const DEFAULT_ICON_CONFIG: ApplyTypeIconConfig = {
  icon: 'list',
  iconColor: '#6b7280',
  bgColor: 'rgba(107, 114, 128, 0.1)',
};

/**
 * 获取申请类型名称
 * @param type 申请类型
 * @returns 申请类型名称
 */
export function getApplyTypeName(type: string): string {
  return APPLY_TYPE_NAME_MAP[type] || '申请';
}

/**
 * 获取申请类型图标配置
 * @param type 申请类型
 * @returns 图标配置
 */
export function getApplyTypeIcon(type: string): ApplyTypeIconConfig {
  return APPLY_TYPE_ICON_MAP[type] || DEFAULT_ICON_CONFIG;
}

/**
 * 获取申请状态文本
 * @param status 申请状态
 * @returns 状态文本
 */
export function getStatusText(status: ApplyStatus): string {
  return ApplyStatusText[status] || '未知';
}

/**
 * 获取申请状态颜色
 * @param status 申请状态
 * @returns 状态颜色
 */
export function getStatusColor(status: ApplyStatus): string {
  return ApplyStatusColor[status] || '#999';
}
