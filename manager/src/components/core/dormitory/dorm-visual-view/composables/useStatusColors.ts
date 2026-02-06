/**
 * 状态颜色映射 Composable
 * @description 提供床位和房间状态的 Tailwind 颜色类映射
 * @author 陈鸿昇
 */

import {
  BedStatus,
  RoomStatus,
  type RoomWithBeds,
  type RoomDisplayStatus,
  type StatusColorConfig
} from '../types'

/**
 * 状态颜色映射 Hook
 */
export function useStatusColors() {
  /**
   * 床位状态颜色配置
   * 使用 Tailwind 主题色，自动适配暗黑模式
   */
  const bedStatusColors: Record<BedStatus, StatusColorConfig> = {
    [BedStatus.AVAILABLE]: {
      border: 'border-dashed border-g-300 dark:border-g-500',
      bg: 'bg-g-100 dark:bg-g-300/30',
      text: 'text-g-500 dark:text-g-400',
      hoverBorder: 'hover:border-g-400 dark:hover:border-g-400',
      hoverBg: 'hover:bg-g-200/80 dark:hover:bg-g-400/30'
    },
    [BedStatus.OCCUPIED]: {
      border: 'border-primary dark:border-primary/70',
      bg: 'bg-primary/20 dark:bg-primary/30',
      text: 'text-primary',
      hoverBg: 'hover:bg-primary/30'
    },
    [BedStatus.MAINTENANCE]: {
      border: 'border-warning dark:border-warning/70',
      bg: 'bg-warning/20 dark:bg-warning/30',
      text: 'text-warning'
    },
    [BedStatus.RESERVED]: {
      border: 'border-secondary dark:border-secondary/70',
      bg: 'bg-secondary/20 dark:bg-secondary/30',
      text: 'text-secondary',
      hoverBg: 'hover:bg-secondary/30'
    }
  }

  /**
   * 房间显示状态颜色配置
   * 根据入住情况动态计算显示状态
   */
  const roomDisplayColors: Record<RoomDisplayStatus, StatusColorConfig> = {
    available: {
      border: 'border-success/30',
      bg: 'bg-success/5',
      text: 'text-success'
    },
    partial: {
      border: 'border-primary/30',
      bg: 'bg-primary/5',
      text: 'text-primary'
    },
    full: {
      border: 'border-info/30',
      bg: 'bg-info/5',
      text: 'text-info'
    },
    maintenance: {
      border: 'border-warning/30',
      bg: 'bg-warning/5',
      text: 'text-warning'
    },
    reserved: {
      border: 'border-secondary/30',
      bg: 'bg-secondary/5',
      text: 'text-secondary'
    }
  }

  /**
   * 房间状态到显示状态的映射
   */
  const roomStatusToDisplay: Record<RoomStatus, RoomDisplayStatus> = {
    [RoomStatus.AVAILABLE]: 'available',
    [RoomStatus.FULL]: 'full',
    [RoomStatus.MAINTENANCE]: 'maintenance',
    [RoomStatus.RESERVED]: 'reserved'
  }

  /**
   * 根据入住率计算房间显示状态
   * @param room 房间数据
   * @returns 房间显示状态
   */
  const getRoomDisplayStatus = (room: RoomWithBeds): RoomDisplayStatus => {
    // 维修中状态优先
    if (room.roomStatus === RoomStatus.MAINTENANCE) {
      return 'maintenance'
    }
    // 已预订状态
    if (room.roomStatus === RoomStatus.RESERVED) {
      return 'reserved'
    }
    // 根据入住情况判断
    if (room.currentOccupancy === 0) {
      return 'available'
    }
    if (room.currentOccupancy >= room.bedCount) {
      return 'full'
    }
    return 'partial'
  }

  /**
   * 获取床位状态颜色类
   * @param status 床位状态
   * @returns Tailwind 类名字符串
   */
  const getBedStatusClasses = (status: BedStatus): string => {
    const config = bedStatusColors[status]
    if (!config) return ''

    const classes = [config.border, config.bg, config.text]
    if (config.hoverBorder) classes.push(config.hoverBorder)
    if (config.hoverBg) classes.push(config.hoverBg)

    return classes.join(' ')
  }

  /**
   * 获取房间显示状态颜色类
   * @param room 房间数据
   * @returns Tailwind 类名字符串
   */
  const getRoomStatusClasses = (room: RoomWithBeds): string => {
    const displayStatus = getRoomDisplayStatus(room)
    const config = roomDisplayColors[displayStatus]
    if (!config) return ''

    return [config.border, config.bg].join(' ')
  }

  /**
   * 获取房间状态文字颜色类
   * @param room 房间数据
   * @returns Tailwind 类名字符串
   */
  const getRoomStatusTextClass = (room: RoomWithBeds): string => {
    const displayStatus = getRoomDisplayStatus(room)
    const config = roomDisplayColors[displayStatus]
    return config?.text || ''
  }

  /**
   * 获取房间显示状态文本
   * @param room 房间数据
   * @returns 状态文本
   */
  const getRoomStatusText = (room: RoomWithBeds): string => {
    const displayStatus = getRoomDisplayStatus(room)
    const statusTexts: Record<RoomDisplayStatus, string> = {
      available: '空闲',
      partial: '部分入住',
      full: '满员',
      maintenance: '维修中',
      reserved: '已预订'
    }
    return statusTexts[displayStatus]
  }

  /**
   * 获取床位状态文本
   * @param status 床位状态
   * @returns 状态文本
   */
  const getBedStatusText = (status: BedStatus): string => {
    const statusTexts: Record<BedStatus, string> = {
      [BedStatus.AVAILABLE]: '空闲',
      [BedStatus.OCCUPIED]: '已入住',
      [BedStatus.MAINTENANCE]: '维修中',
      [BedStatus.RESERVED]: '已预订'
    }
    return statusTexts[status] || '未知'
  }

  /**
   * 获取床位状态图标
   * @param status 床位状态
   * @returns 图标名称
   */
  const getBedStatusIcon = (status: BedStatus): string => {
    const statusIcons: Record<BedStatus, string> = {
      [BedStatus.AVAILABLE]: '',
      [BedStatus.OCCUPIED]: 'ri:user-fill',
      [BedStatus.MAINTENANCE]: 'ri:tools-fill',
      [BedStatus.RESERVED]: 'ri:bookmark-fill'
    }
    return statusIcons[status] || ''
  }

  return {
    bedStatusColors,
    roomDisplayColors,
    roomStatusToDisplay,
    getRoomDisplayStatus,
    getBedStatusClasses,
    getRoomStatusClasses,
    getRoomStatusTextClass,
    getRoomStatusText,
    getBedStatusText,
    getBedStatusIcon
  }
}
