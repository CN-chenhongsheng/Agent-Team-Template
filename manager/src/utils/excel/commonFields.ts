/**
 * Excel 模板通用字段配置
 *
 * 预定义常用的字段配置，可在各模块中复用
 *
 * @module utils/excel/commonFields
 * @author 陈鸿昇
 */
import type { TemplateColumn } from './templateGenerator'

/**
 * 通用字段配置集合
 *
 * 使用方式：
 * ```ts
 * import { COMMON_FIELDS } from '@/utils/excel/commonFields'
 *
 * const columns = [
 *   { title: '姓名', key: 'name' },
 *   COMMON_FIELDS.gender,
 *   COMMON_FIELDS.nation,
 * ]
 * ```
 */
export const COMMON_FIELDS: Record<string, TemplateColumn> = {
  /**
   * 性别字段
   * 固定选项：男、女
   */
  gender: {
    title: '性别',
    key: 'gender',
    width: 8,
    dropdown: {
      options: ['男', '女']
    },
    example: '男'
  },

  /**
   * 民族字段
   * 数据来源：student_nation 字典
   */
  nation: {
    title: '民族',
    key: 'nation',
    width: 10,
    dropdown: {
      dictCode: 'student_nation'
    },
    example: '汉族'
  },

  /**
   * 政治面貌字段
   * 数据来源：student_political_status 字典
   */
  politicalStatus: {
    title: '政治面貌',
    key: 'politicalStatus',
    width: 12,
    dropdown: {
      dictCode: 'student_political_status'
    },
    example: '共青团员'
  },

  /**
   * 学籍状态字段
   * 数据来源：academic_status 字典
   */
  academicStatus: {
    title: '学籍状态',
    key: 'academicStatus',
    width: 10,
    dropdown: {
      dictCode: 'academic_status'
    },
    example: '在读'
  },

  /**
   * 启用/禁用状态字段
   * 固定选项：启用、禁用
   */
  status: {
    title: '状态',
    key: 'status',
    width: 8,
    dropdown: {
      options: ['启用', '禁用']
    },
    example: '启用'
  },

  /**
   * 是/否字段
   * 固定选项：是、否
   */
  yesNo: {
    title: '是否',
    key: 'yesNo',
    width: 8,
    dropdown: {
      options: ['是', '否']
    },
    example: '是'
  },

  /**
   * 手机号字段
   */
  phone: {
    title: '手机号',
    key: 'phone',
    width: 15,
    example: '13800138000',
    comment: '请输入11位手机号'
  },

  /**
   * 邮箱字段
   */
  email: {
    title: '邮箱',
    key: 'email',
    width: 25,
    example: 'example@email.com',
    comment: '请输入有效的邮箱地址'
  },

  /**
   * 身份证号字段
   */
  idCard: {
    title: '身份证号',
    key: 'idCard',
    width: 22,
    example: '110101200001011234',
    comment: '请输入18位身份证号'
  },

  /**
   * 出生日期字段
   */
  birthDate: {
    title: '出生日期',
    key: 'birthDate',
    width: 14,
    example: '2000-01-01',
    comment: '格式：YYYY-MM-DD'
  },

  /**
   * 入学年份字段
   */
  enrollmentYear: {
    title: '入学年份',
    key: 'enrollmentYear',
    width: 12,
    example: '2024',
    comment: '格式：YYYY'
  },

  /**
   * 学制字段
   */
  schoolingLength: {
    title: '学制',
    key: 'schoolingLength',
    width: 8,
    dropdown: {
      options: ['3', '4', '5', '6', '7']
    },
    example: '4',
    comment: '学制年限'
  },

  /**
   * 当前年级字段
   */
  currentGrade: {
    title: '当前年级',
    key: 'currentGrade',
    width: 12,
    dropdown: {
      options: ['大一', '大二', '大三', '大四', '研一', '研二', '研三', '博一', '博二', '博三']
    },
    example: '大一'
  },

  // ===================== 生活习惯字段 =====================

  /**
   * 吸烟状态
   */
  smokingStatus: {
    title: '是否吸烟',
    key: 'smokingStatus',
    width: 10,
    dropdown: {
      dictCode: 'student_smoking_status'
    },
    example: '不吸烟'
  },

  /**
   * 是否接受室友吸烟
   */
  smokingTolerance: {
    title: '接受室友吸烟',
    key: 'smokingTolerance',
    width: 14,
    dropdown: {
      dictCode: 'student_smoking_tolerance'
    },
    example: '不接受'
  },

  /**
   * 作息时间
   */
  sleepSchedule: {
    title: '作息时间',
    key: 'sleepSchedule',
    width: 18,
    dropdown: {
      dictCode: 'student_sleep_schedule'
    },
    example: '正常(23:00-7:00)',
    comment: '选择您的日常作息习惯'
  },

  /**
   * 睡眠质量
   */
  sleepQuality: {
    title: '睡眠质量',
    key: 'sleepQuality',
    width: 12,
    dropdown: {
      dictCode: 'student_sleep_quality'
    },
    example: '正常'
  },

  /**
   * 是否打呼噜
   */
  snores: {
    title: '是否打呼噜',
    key: 'snores',
    width: 12,
    dropdown: {
      dictCode: 'student_snores'
    },
    example: '不打'
  },

  /**
   * 是否对光线敏感
   */
  sensitiveToLight: {
    title: '对光线敏感',
    key: 'sensitiveToLight',
    width: 12,
    dropdown: {
      dictCode: 'student_sensitive_to_light'
    },
    example: '不敏感'
  },

  /**
   * 是否对声音敏感
   */
  sensitiveToSound: {
    title: '对声音敏感',
    key: 'sensitiveToSound',
    width: 12,
    dropdown: {
      dictCode: 'student_sensitive_to_sound'
    },
    example: '不敏感'
  },

  /**
   * 整洁程度
   */
  cleanlinessLevel: {
    title: '整洁程度',
    key: 'cleanlinessLevel',
    width: 12,
    dropdown: {
      dictCode: 'student_cleanliness_level'
    },
    example: '整洁'
  },

  /**
   * 睡前是否整理
   */
  bedtimeCleanup: {
    title: '睡前整理习惯',
    key: 'bedtimeCleanup',
    width: 14,
    dropdown: {
      dictCode: 'student_bedtime_cleanup'
    },
    example: '偶尔整理'
  },

  /**
   * 社交偏好
   */
  socialPreference: {
    title: '社交偏好',
    key: 'socialPreference',
    width: 12,
    dropdown: {
      dictCode: 'student_social_preference'
    },
    example: '中等'
  },

  /**
   * 是否允许室友带访客
   */
  allowVisitors: {
    title: '允许访客',
    key: 'allowVisitors',
    width: 12,
    dropdown: {
      dictCode: 'student_allow_visitors'
    },
    example: '偶尔可以'
  },

  /**
   * 电话时间偏好
   */
  phoneCallTime: {
    title: '宿舍打电话习惯',
    key: 'phoneCallTime',
    width: 16,
    dropdown: {
      dictCode: 'student_phone_call_time'
    },
    example: '偶尔在宿舍'
  },

  /**
   * 是否在宿舍学习
   */
  studyInRoom: {
    title: '宿舍学习频率',
    key: 'studyInRoom',
    width: 14,
    dropdown: {
      dictCode: 'student_study_in_room'
    },
    example: '偶尔'
  },

  /**
   * 学习环境偏好
   */
  studyEnvironment: {
    title: '学习环境偏好',
    key: 'studyEnvironment',
    width: 14,
    dropdown: {
      dictCode: 'student_study_environment'
    },
    example: '需要安静'
  },

  /**
   * 电脑使用时间
   */
  computerUsageTime: {
    title: '电脑使用时间',
    key: 'computerUsageTime',
    width: 16,
    dropdown: {
      dictCode: 'student_computer_usage_time'
    },
    example: '正常(3-5h/天)'
  },

  /**
   * 游戏偏好
   */
  gamingPreference: {
    title: '游戏频率',
    key: 'gamingPreference',
    width: 12,
    dropdown: {
      dictCode: 'student_gaming_preference'
    },
    example: '偶尔玩'
  },

  /**
   * 听音乐偏好
   */
  musicPreference: {
    title: '听音乐频率',
    key: 'musicPreference',
    width: 12,
    dropdown: {
      dictCode: 'student_music_preference'
    },
    example: '偶尔听'
  },

  /**
   * 音乐音量偏好
   */
  musicVolume: {
    title: '音乐音量偏好',
    key: 'musicVolume',
    width: 14,
    dropdown: {
      dictCode: 'student_music_volume'
    },
    example: '中等'
  },

  /**
   * 是否在宿舍吃东西
   */
  eatInRoom: {
    title: '宿舍吃东西习惯',
    key: 'eatInRoom',
    width: 14,
    dropdown: {
      dictCode: 'student_eat_in_room'
    },
    example: '偶尔'
  }
}

/**
 * 获取通用字段配置
 * @param fieldKey 字段key
 * @returns 字段配置，如果不存在返回 null
 */
export function getCommonField(fieldKey: string): TemplateColumn | null {
  return COMMON_FIELDS[fieldKey] || null
}

/**
 * 批量获取通用字段配置
 * @param fieldKeys 字段key数组
 * @returns 字段配置数组
 */
export function getCommonFields(fieldKeys: string[]): TemplateColumn[] {
  return fieldKeys
    .map((key) => COMMON_FIELDS[key])
    .filter((field): field is TemplateColumn => field !== undefined)
}

/**
 * 创建自定义字段（基于通用字段扩展）
 * @param baseFieldKey 基础字段key
 * @param overrides 覆盖配置
 * @returns 新的字段配置
 */
export function extendCommonField(
  baseFieldKey: string,
  overrides: Partial<TemplateColumn>
): TemplateColumn | null {
  const baseField = COMMON_FIELDS[baseFieldKey]
  if (!baseField) {
    return null
  }

  return {
    ...baseField,
    ...overrides,
    dropdown: overrides.dropdown
      ? { ...baseField.dropdown, ...overrides.dropdown }
      : baseField.dropdown
  }
}
