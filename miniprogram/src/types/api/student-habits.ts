/**
 * 学生生活习惯数据类型定义
 */

/**
 * 学生生活习惯表单数据
 */
export interface IStudentHabitsForm {
  // ========== 作息与睡眠 ==========
  /** 吸烟状态：0不吸烟 1吸烟 */
  smokingStatus?: number | null;
  /** 是否接受室友吸烟：0不接受 1接受 */
  smokingTolerance?: number | null;
  /** 作息时间：0早睡早起(22:00-6:00) 1正常(23:00-7:00) 2晚睡晚起(24:00-8:00) 3夜猫子(01:00-9:00) */
  sleepSchedule?: number | null;
  /** 睡眠质量：0浅睡易醒 1正常 2深睡 */
  sleepQuality?: number | null;
  /** 是否打呼噜：0不打 1打 */
  snores?: number | null;
  /** 是否对光线敏感：0不敏感 1敏感 */
  sensitiveToLight?: number | null;
  /** 是否对声音敏感：0不敏感 1敏感 */
  sensitiveToSound?: number | null;

  // ========== 卫生习惯 ==========
  /** 整洁程度：1非常整洁 2整洁 3一般 4随意 5不整洁 */
  cleanlinessLevel?: number | null;
  /** 睡前是否整理：0不整理 1偶尔整理 2经常整理 3总是整理 */
  bedtimeCleanup?: number | null;

  // ========== 社交与生活 ==========
  /** 社交偏好：1喜欢安静 2中等 3喜欢热闹 */
  socialPreference?: number | null;
  /** 是否允许室友带访客：0不允许 1偶尔可以 2可以 */
  allowVisitors?: number | null;
  /** 电话时间偏好：0喜欢在宿舍打电话 1偶尔在宿舍 2不在宿舍打电话 */
  phoneCallTime?: number | null;
  /** 是否在宿舍吃东西：0不吃 1偶尔 2经常 */
  eatInRoom?: number | null;

  // ========== 学习与娱乐 ==========
  /** 是否在宿舍学习：0不在 1偶尔 2经常 3总是 */
  studyInRoom?: number | null;
  /** 学习环境偏好：1需要安静 2需要轻音乐 3可以接受声音 */
  studyEnvironment?: number | null;
  /** 电脑使用时间：0不用 1很少(1-2h/天) 2正常(3-5h/天) 3很多(6h+/天) */
  computerUsageTime?: number | null;
  /** 游戏偏好：0不玩游戏 1偶尔玩 2经常玩 */
  gamingPreference?: number | null;
  /** 听音乐偏好：0不听 1偶尔听 2经常听 */
  musicPreference?: number | null;
  /** 音乐音量偏好：1喜欢小声 2中等 3喜欢大声 */
  musicVolume?: number | null;

  // ========== 额外信息 ==========
  /** 特殊需求（如过敏、健康问题等）最多500字符 */
  specialNeeds?: string | null;
  /** 室友偏好（如希望室友不抽烟、安静等）最多200字符 */
  roommatePreference?: string | null;
}

/**
 * 选项配置
 */
export interface IOptionItem {
  label: string;
  value: number;
  color?: string;
}

/**
 * 表单字段分组
 */
export interface IFormSection {
  title: string;
  icon: string;
  iconColor: string;
  bgGradient: string;
  fields: IFormField[];
}

/**
 * 表单字段配置
 */
export interface IFormField {
  key: keyof IStudentHabitsForm;
  label: string;
  type: 'radio' | 'textarea';
  options?: IOptionItem[];
  placeholder?: string;
  maxlength?: number;
  required?: boolean;
}
