/**
 * 全局常量定义
 * 包含应用中使用的所有魔法数字和硬编码字符串
 *
 * 业务常量；构建/环境相关见 build/constant.ts、.env
 */

/**
 * 请求相关常量
 */
export const REQUEST_CONSTANTS = {
  // 请求间隔时间 (ms)
  RETRY_INTERVAL: 1000,
  // 请求超时时间 (ms)
  REQUEST_TIMEOUT: 30000,
  // Token 刷新间隔 (ms)
  TOKEN_REFRESH_INTERVAL: 5 * 60 * 1000,
};

/**
 * 用户相关常量
 */
export const USER_CONSTANTS = {
  // 默认头像
  DEFAULT_AVATAR: 'https://via.placeholder.com/150',
  // 默认用户名
  DEFAULT_USERNAME: '用户',
};

/**
 * 申请相关常量
 */
export const APPLY_CONSTANTS = {
  // 申请编号前缀
  APPLY_NO_PREFIX: 'NO.',
  // 默认申请编号
  DEFAULT_APPLY_NO: '20231024001',
  // 默认申请类型
  DEFAULT_APPLY_TYPE: '申请',
  // 默认状态文本
  DEFAULT_STATUS_TEXT: '未知',
  // 默认状态颜色
  DEFAULT_STATUS_COLOR: '#999',
};

/**
 * 时间相关常量
 */
export const TIME_CONSTANTS = {
  // 预计审核时间 (小时)
  ESTIMATED_REVIEW_HOURS: 24,
  // 时间格式
  DATE_FORMAT: 'YYYY-MM-DD',
  DATE_TIME_FORMAT: 'YYYY-MM-DD HH:mm',
};

/**
 * 页面路由常量
 */
export const ROUTE_CONSTANTS = {
  // 首页
  HOME: '/pages/tab/home/index',
  // 申请列表页
  APPLY_LIST: '/pages/tab/apply/index',
  // 个人中心页
  PROFILE: '/pages/tab/profile/index',
  // 消息页
  MESSAGE: '/pages/service/message/index',
  // 学生申请详情页
  STUDENT_APPLY_DETAIL: '/pages/apply/detail/index',
  // 申请表单页
  APPLY_FORM: '/pages/apply/form/index',
  // 登录页
  LOGIN: '/pages/common/login/index',
  // 通知详情页
  NOTICE_DETAIL: '/pages/service/notice-detail/index',
  // 学生生活习惯页
  STUDENT_HABITS: '/pages/profile/student-habits/index',
  // 智能分配 - 生活习惯问卷
  ALLOCATION_SURVEY: '/pages/allocation/survey/index',
  // 智能分配 - 分配结果
  ALLOCATION_RESULT: '/pages/allocation/result/index',
  // 智能分配 - 床位推荐
  ALLOCATION_RECOMMEND: '/pages/allocation/recommend/index',
};

/**
 * 颜色常量
 */
export const COLOR_CONSTANTS = {
  // 主色调
  PRIMARY: '#0adbc3',
  PRIMARY_DARK: '#08bda8',
  PRIMARY_LIGHT: '#e0fbf8',
  // 辅助色
  ACCENT: '#ff9f43',
  ACCENT_DARK: '#ff8f2b',
  // 文本颜色
  TEXT_MAIN: '#111817',
  TEXT_SUB: '#6b7280',
  TEXT_DISABLED: '#9ca3af',
  TEXT_PLACEHOLDER: '#9ca3af',
  // 中性色
  GRAY_LIGHT: '#f5f8f8',
  GRAY_MEDIUM: '#e5e7eb',
  GRAY_DARK: '#6b7280',
};

/**
 * 动画相关常量
 */
export const ANIMATION_CONSTANTS = {
  // 快速过渡 (ms)
  TRANSITION_FAST: 200,
  // 正常过渡 (ms)
  TRANSITION_NORMAL: 300,
  // 缓慢过渡 (ms)
  TRANSITION_SLOW: 500,
  // 列表项动画延迟基数 (ms)
  LIST_ITEM_DELAY_BASE: 50,
};

/**
 * UI 相关常量
 */
export const UI_CONSTANTS = {
  // 加载动画显示延迟 (ms)
  LOADING_DELAY: 1000,
  // 空状态图标大小 (rpx)
  EMPTY_ICON_SIZE: 120,
  // 骨架屏项数
  SKELETON_ITEM_COUNT: 5,
};

/**
 * 验证相关常量
 */
export const VALIDATION_CONSTANTS = {
  // 手机号正则表达式
  PHONE_REGEX: /^1[3-9]\d{9}$/,
  // 邮箱正则表达式
  EMAIL_REGEX: /^[^\s@]+@[^\s@][^\s.@]*\.[^\s@]+$/,
  // 身份证正则表达式
  ID_CARD_REGEX: /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dX]$/i,
};

/**
 * 存储相关常量
 */
export const STORAGE_CONSTANTS = {
  // 用户信息存储键
  USER_INFO_KEY: 'user_info',
  // Token 存储键
  TOKEN_KEY: 'access_token',
  // 刷新 Token 存储键
  REFRESH_TOKEN_KEY: 'refresh_token',
  // 申请草稿存储键
  APPLY_DRAFT_KEY: 'apply_draft',
  // 用户偏好设置存储键
  USER_PREFERENCES_KEY: 'user_preferences',
};
