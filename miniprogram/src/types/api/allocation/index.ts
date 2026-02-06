/**
 * 智能分配模块类型定义
 * @module types/api/allocation
 */

/**
 * 问卷状态枚举
 */
export enum SurveyStatusEnum {
  /** 未填写 */
  NOT_FILLED = 0,
  /** 已填写 */
  FILLED = 1,
  /** 已锁定 */
  LOCKED = 2,
}

/**
 * 问卷状态文本映射
 */
export const SurveyStatusText: Record<SurveyStatusEnum, string> = {
  [SurveyStatusEnum.NOT_FILLED]: '未填写',
  [SurveyStatusEnum.FILLED]: '已填写',
  [SurveyStatusEnum.LOCKED]: '已锁定',
}

/**
 * 问卷状态信息
 */
export interface ISurveyStatus {
  /** 学生ID */
  studentId: number
  /** 问卷状态 0-未填写 1-已填写 2-已锁定 */
  surveyStatus: SurveyStatusEnum
  /** 问卷状态名称 */
  surveyStatusName: string
  /** 提交时间 */
  submitTime?: string
  /** 最后更新时间 */
  lastUpdateTime?: string
  /** 锁定时间 */
  lockTime?: string
  /** 是否可以修改 */
  canModify: boolean
}

/**
 * 问卷数据
 */
export interface ISurveyData {
  /** 是否吸烟 0不吸烟 1吸烟 */
  smokingStatus?: number
  /** 是否接受室友吸烟 0不接受 1接受 */
  smokingTolerance?: number
  /** 作息规律 0早睡早起 1正常 2晚睡晚起 3夜猫子 */
  sleepSchedule?: number
  /** 睡眠质量 0浅睡易醒 1正常 2深睡 */
  sleepQuality?: number
  /** 是否打呼噜 0不打 1打 */
  snores?: number
  /** 对光线敏感 0不敏感 1敏感 */
  sensitiveToLight?: number
  /** 对声音敏感 0不敏感 1敏感 */
  sensitiveToSound?: number
  /** 整洁程度 1非常整洁 2整洁 3一般 4随意 5不整洁 */
  cleanlinessLevel?: number
  /** 睡前整理习惯 0不整理 1偶尔整理 2经常整理 3总是整理 */
  bedtimeCleanup?: number
  /** 社交偏好 0喜欢安静 1中等 2喜欢热闹 */
  socialPreference?: number
  /** 是否允许访客 0不允许 1偶尔可以 2可以 */
  allowVisitors?: number
  /** 电话习惯 0不喜欢在宿舍打电话 1偶尔 2不介意 */
  phoneCallTime?: number
  /** 在宿舍学习频率 0不在 1偶尔 2经常 3总是 */
  studyInRoom?: number
  /** 学习环境偏好 0不需要安静 1需要安静 2需要轻音乐 3可以接受声音 */
  studyEnvironment?: number
  /** 电脑使用时间 0不用 1很少 2正常 3很多 */
  computerUsageTime?: number
  /** 游戏偏好 0不喜欢 1偶尔 2经常 */
  gamingPreference?: number
  /** 听音乐偏好 0不听 1偶尔听 2经常听 */
  musicPreference?: number
  /** 音乐音量偏好 0喜欢小声 1中等 2喜欢大声 */
  musicVolume?: number
  /** 是否在宿舍吃东西 0不吃 1偶尔 2经常 */
  eatInRoom?: number
}

/**
 * 问卷状态+数据（完整VO）
 */
export interface ISurveyVO extends ISurveyStatus, ISurveyData {}

/**
 * 分配结果
 */
export interface IAllocationResult {
  /** ID */
  id: number
  /** 任务ID */
  taskId: number
  /** 学生ID */
  studentId: number
  /** 学号 */
  studentNo: string
  /** 学生姓名 */
  studentName: string
  /** 分配床位ID */
  allocatedBedId: number
  /** 分配房间ID */
  allocatedRoomId: number
  /** 分配房间编码 */
  allocatedRoomCode: string
  /** 分配楼层编码 */
  allocatedFloorCode: string
  /** 匹配分数 */
  matchScore: number
  /** 匹配分数等级 */
  matchScoreLevel: string
  /** 冲突原因 */
  conflictReasons: string[]
  /** 匹配优势 */
  advantages: string[]
  /** 室友姓名列表 */
  roommateNames: string[]
  /** 状态 */
  status: number
  /** 状态名称 */
  statusName: string
}

/**
 * 室友信息
 */
export interface IRoommateInfo {
  /** 学生ID */
  studentId: number
  /** 学生姓名 */
  studentName: string
  /** 学号 */
  studentNo: string
  /** 性别 */
  gender: number
  /** 班级名称 */
  className: string
  /** 作息规律 */
  sleepSchedule: number
  /** 社交偏好 */
  socialPreference: number
  /** 作息规律文本 */
  sleepScheduleText: string
  /** 社交偏好文本 */
  socialPreferenceText: string
}

/**
 * 床位推荐
 */
export interface IBedRecommend {
  /** 房间ID */
  roomId: number
  /** 房间编码 */
  roomCode: string
  /** 房间号 */
  roomNumber: string
  /** 楼层编码 */
  floorCode: string
  /** 床位ID */
  bedId: number
  /** 床位编码 */
  bedCode: string
  /** 匹配分数 */
  matchScore: number
  /** 最低匹配分数 */
  minMatchScore: number
  /** 最高匹配分数 */
  maxMatchScore: number
  /** 匹配分数等级 */
  matchScoreLevel: string
  /** 室友数量 */
  roommateCount: number
  /** 匹配优势 */
  advantages: string[]
  /** 潜在冲突 */
  conflicts: string[]
}
