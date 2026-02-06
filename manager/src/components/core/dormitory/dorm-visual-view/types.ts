/**
 * 宿舍可视化视图 - 类型定义
 * @description 房间和床位可视化展示相关的类型定义
 * @author 陈鸿昇
 */

// ==================== 枚举定义 ====================

/**
 * 床位状态枚举
 */
export enum BedStatus {
  /** 空闲 */
  AVAILABLE = 1,
  /** 已占用 */
  OCCUPIED = 2,
  /** 维修中 */
  MAINTENANCE = 3,
  /** 已预订 */
  RESERVED = 4
}

/**
 * 房间状态枚举
 */
export enum RoomStatus {
  /** 空闲 */
  AVAILABLE = 1,
  /** 满员 */
  FULL = 2,
  /** 维修中 */
  MAINTENANCE = 3,
  /** 已预订 */
  RESERVED = 4
}

/**
 * 视图类型
 */
export type ViewType = 'table' | 'visual'

/**
 * 楼层选择值：
 * - number: 兼容旧逻辑的楼层ID
 * - all:${buildingCode}: 选择某楼栋的“全部层数”
 * - floor:${floorId}:${level}: 选择某楼栋的第 n 层
 */
export type FloorSelectValue = number | `all:${string}` | `floor:${number}:${number}` | null

// ==================== 学生信息 ====================

/**
 * 学生基本信息（用于床位悬浮展示）
 */
export interface StudentBasicInfo {
  /** 学生ID */
  studentId?: number
  /** 学号 */
  studentNo?: string
  /** 姓名 */
  studentName?: string
  /** 性别 (1男 2女) */
  gender?: number
  /** 性别文本 */
  genderText?: string
  /** 手机号 */
  phone?: string
  /** 民族 */
  nation?: string
  /** 政治面貌 */
  politicalStatus?: string
  /** 校区名称 */
  campusName?: string
  /** 院系名称 */
  deptName?: string
  /** 专业名称 */
  majorName?: string
  /** 班级名称 */
  className?: string
  /** 楼栋名称 */
  floorName?: string
  /** 房间名称 */
  roomName?: string
  /** 床位名称 */
  bedName?: string
  /** 学籍状态 */
  academicStatus?: number
  /** 学籍状态文本 */
  academicStatusText?: string
  /** 入学年份 */
  enrollmentYear?: number
  /** 当前年级 */
  currentGrade?: string
}

// ==================== 床位相关 ====================

/**
 * 床位信息（含学生信息）
 */
export interface BedWithStudent {
  /** 床位ID */
  id: number
  /** 床位编码 */
  bedCode: string
  /** 床位号 */
  bedNumber: string
  /** 床位状态 (number 以兼容后端返回的数值类型) */
  bedStatus: BedStatus | number
  /** 床位位置 */
  bedPosition?: string
  /** 床位位置文本 */
  bedPositionText?: string
  /** 学生ID */
  studentId?: number
  /** 学生信息 */
  studentInfo?: StudentBasicInfo
  /** 入住日期 */
  checkInDate?: string
  /** 排序 */
  sort?: number
  /** 状态 (1启用 0停用) */
  status?: number
}

// ==================== 房间相关 ====================

/**
 * 房间信息（含床位列表）
 */
export interface RoomWithBeds {
  /** 房间ID */
  id: number
  /** 房间编码 */
  roomCode: string
  /** 房间号 */
  roomNumber: string
  /** 房间状态 (number 以兼容后端返回的数值类型) */
  roomStatus: RoomStatus | number
  /** 房间状态文本 */
  roomStatusText?: string
  /** 楼层ID */
  floorId: number
  /** 楼层数（第几层），用于可视化筛选 */
  floorNumber?: number
  /** 楼层编码 */
  floorCode?: string
  /** 楼层名称 */
  floorName?: string
  /** 校区编码 */
  campusCode?: string
  /** 校区名称 */
  campusName?: string
  /** 房间类型 */
  roomType?: string
  /** 房间类型文本 */
  roomTypeText?: string
  /** 适用性别：1男 2女 3混合（来自楼层） */
  genderType?: number
  /** 适用性别文本 */
  genderTypeText?: string
  /** 床位数 */
  bedCount: number
  /** 当前入住人数 */
  currentOccupancy: number
  /** 床位列表 */
  beds: BedWithStudent[]
  /** 房间面积 */
  area?: number
  /** 是否有空调 */
  hasAirConditioner?: number
  /** 是否有卫生间 */
  hasBathroom?: number
  /** 是否有阳台 */
  hasBalcony?: number
  /** 排序 */
  sort?: number
  /** 状态 (1启用 0停用) */
  status?: number
  /** 备注 */
  remark?: string
}

// ==================== 楼栋/楼层选项 ====================

/**
 * 楼层选项（用于级联选择器）
 * @description 添加索引签名以兼容 Element Plus CascaderOption 类型
 */
export interface FloorOption {
  /** ID */
  id: number | string
  /** 名称 */
  name: string
  /** 编码 */
  code?: string
  /** 楼层数 */
  floorNumber?: number
  /** 子楼层（用于楼栋-楼层级联） */
  children?: FloorOption[]
  /** 索引签名，兼容 CascaderOption */
  [key: string]: unknown
}

/**
 * 楼栋选项（含楼层列表）
 * @description 添加索引签名以兼容 Element Plus CascaderOption 类型
 */
export interface BuildingOption {
  /** 楼栋ID（通常使用楼栋编码，便于级联选择稳定） */
  id: string
  /** 楼栋名称 */
  name: string
  /** 楼栋编码 */
  code?: string
  /** 校区编码 */
  campusCode?: string
  /** 楼层列表 */
  floors: FloorOption[]
  /** 索引签名，兼容 CascaderOption */
  [key: string]: unknown
}

// ==================== 统计数据 ====================

/**
 * 可视化统计数据
 */
export interface VisualStats {
  /** 总房间数 */
  total: number
  /** 空闲房间数 */
  available: number
  /** 部分入住房间数 */
  partial: number
  /** 满员房间数 */
  full: number
  /** 维修中房间数 */
  maintenance: number
  /** 入住率 (百分比) */
  occupancyRate: number
}

// ==================== 状态颜色配置 ====================

/**
 * 状态颜色配置
 */
export interface StatusColorConfig {
  /** 边框类 */
  border: string
  /** 背景类 */
  bg: string
  /** 文字类 */
  text: string
  /** 悬停边框类 */
  hoverBorder?: string
  /** 悬停背景类 */
  hoverBg?: string
}

/**
 * 房间显示状态类型
 */
export type RoomDisplayStatus = 'available' | 'partial' | 'full' | 'maintenance' | 'reserved'

// ==================== 组件 Props ====================

/**
 * BedSlot 组件 Props
 */
export interface BedSlotProps {
  /** 床位数据 */
  bed: BedWithStudent
  /** 是否可点击 */
  clickable?: boolean
}

/**
 * RoomCard 组件 Props
 */
export interface RoomCardProps {
  /** 房间数据 */
  room: RoomWithBeds
  /** 是否可点击 */
  clickable?: boolean
}

/**
 * RoomGrid 组件 Props
 */
export interface RoomGridProps {
  /** 房间列表 */
  rooms: RoomWithBeds[]
  /** 加载状态 */
  loading?: boolean
}

/**
 * ViewSwitcher 组件 Props
 */
export interface ViewSwitcherProps {
  /** 当前视图 */
  modelValue: ViewType
}

/**
 * FloorSelector 组件 Props
 */
export interface FloorSelectorProps {
  /** 选中的楼层ID */
  modelValue?: FloorSelectValue
  /** 楼栋选项列表 */
  options: BuildingOption[]
  /** 占位文本 */
  placeholder?: string
}

/**
 * DormVisualView 组件 Props
 */
export interface DormVisualViewProps {
  /** 楼栋选项列表 */
  buildingOptions?: BuildingOption[]
  /** 默认楼层ID */
  defaultFloorId?: number
  /** 是否显示统计卡片 */
  showStats?: boolean
  /** 是否显示视图切换 */
  showViewSwitcher?: boolean
}

// ==================== 组件 Emits ====================

/**
 * BedSlot 组件 Emits
 */
export interface BedSlotEmits {
  (e: 'click', bed: BedWithStudent): void
  (e: 'empty-bed-click', bed: BedWithStudent): void
}

/**
 * RoomCard 组件 Emits
 */
export interface RoomCardEmits {
  (e: 'click', room: RoomWithBeds): void
  (e: 'bed-click', bed: BedWithStudent, room: RoomWithBeds): void
  (e: 'empty-bed-click', bed: BedWithStudent, room: RoomWithBeds): void
}

/**
 * RoomGrid 组件 Emits
 */
export interface RoomGridEmits {
  (e: 'room-click', room: RoomWithBeds): void
  (e: 'bed-click', bed: BedWithStudent, room: RoomWithBeds): void
  (e: 'empty-bed-click', bed: BedWithStudent, room: RoomWithBeds): void
}

/**
 * ViewSwitcher 组件 Emits
 */
export interface ViewSwitcherEmits {
  (e: 'update:modelValue', value: ViewType): void
}

/**
 * FloorSelector 组件 Emits
 */
export interface FloorSelectorEmits {
  (e: 'update:modelValue', value: FloorSelectValue): void
  (e: 'change', value: FloorSelectValue): void
}

/**
 * DormVisualView 组件 Emits
 */
export interface DormVisualViewEmits {
  (e: 'room-click', room: RoomWithBeds): void
  (e: 'bed-click', bed: BedWithStudent, room: RoomWithBeds): void
  (e: 'empty-bed-click', bed: BedWithStudent, room: RoomWithBeds): void
  (e: 'view-change', view: ViewType): void
  (e: 'floor-change', value: FloorSelectValue): void
}
