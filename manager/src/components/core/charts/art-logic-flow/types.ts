/**
 * LogicFlow 组件类型定义
 *
 * @module components/core/charts/art-logic-flow/types
 * @author 陈鸿昇
 */

/**
 * LogicFlow 节点数据
 */
export interface LogicFlowNode {
  id: string
  type: string
  x: number
  y: number
  text?: string
  properties?: Record<string, any>
  [key: string]: any
}

/**
 * LogicFlow 边数据
 */
export interface LogicFlowEdge {
  id?: string
  sourceNodeId: string
  targetNodeId: string
  type?: string
  text?: string
  properties?: Record<string, any>
  [key: string]: any
}

/**
 * LogicFlow 数据
 */
export interface LogicFlowData {
  nodes: LogicFlowNode[]
  edges: LogicFlowEdge[]
}

/**
 * LogicFlow 配置选项
 */
export interface LogicFlowConfig {
  /** 是否开启网格 */
  grid?:
    | boolean
    | {
        size?: number
        visible?: boolean
        type?: 'dot' | 'mesh'
        config?: {
          color?: string
          thickness?: number
        }
      }
  /** 背景配置 */
  background?: {
    color?: string
    image?: string
    repeat?: string
    position?: string
    size?: string
  }
  /** 键盘配置 */
  keyboard?: {
    enabled?: boolean
  }
  /** 样式配置 */
  style?: Record<string, any>
  /** 其他 LogicFlow 配置 */
  [key: string]: any
}

/**
 * 组件 Props
 */
export interface ArtLogicFlowProps {
  /** 流程图数据 */
  data?: LogicFlowData
  /** LogicFlow 配置 */
  config?: LogicFlowConfig
  /** 是否只读模式 */
  readonly?: boolean
  /** 容器高度 */
  height?: string | number
  /** 容器宽度 */
  width?: string | number
  /** 是否自适应容器大小 */
  autoResize?: boolean
}

/**
 * 节点点击事件数据
 */
export interface NodeClickEvent {
  node: LogicFlowNode
  e: MouseEvent
}

/**
 * 边点击事件数据
 */
export interface EdgeClickEvent {
  edge: LogicFlowEdge
  e: MouseEvent
}

/**
 * 数据变化事件数据
 */
export interface DataChangeEvent {
  data: LogicFlowData
  type: 'add' | 'delete' | 'update' | 'move'
}

/**
 * 树形数据节点（用于数据转换）
 */
export interface TreeNode {
  id: string | number
  label?: string
  text?: string
  children?: TreeNode[]
  [key: string]: any
}

/**
 * 布局配置
 */
export interface LayoutConfig {
  /** 节点水平间距 */
  nodeSpacingX?: number
  /** 节点垂直间距 */
  nodeSpacingY?: number
  /** 起始 X 坐标 */
  startX?: number
  /** 起始 Y 坐标 */
  startY?: number
  /** 方向：'vertical' | 'horizontal' */
  direction?: 'vertical' | 'horizontal'
}
