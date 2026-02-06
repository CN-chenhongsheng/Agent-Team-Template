/**
 * LogicFlow 默认配置
 *
 * @module components/core/charts/art-logic-flow/config
 * @author 陈鸿昇
 */

import type { LogicFlowConfig } from './types'

/**
 * 获取默认配置
 */
export function getDefaultConfig(): LogicFlowConfig {
  return {
    grid: {
      size: 40,
      visible: false, // 禁用 LogicFlow 内部网格，使用 CSS 背景
      type: 'dot',
      config: {
        color: 'transparent',
        thickness: 0
      }
    },
    background: {
      color: 'transparent' // 确保画布透明
    },
    keyboard: {
      enabled: true
    },
    style: {
      rect: {
        rx: 8,
        ry: 8,
        strokeWidth: 1,
        stroke: '#d9d9d9',
        fill: '#ffffff',
        width: 160,
        height: 40
      },
      circle: {
        r: 30,
        strokeWidth: 1,
        stroke: '#d9d9d9',
        fill: '#ffffff'
      },
      polyline: {
        strokeWidth: 1.5,
        stroke: '#c0c4cc',
        outlineColor: 'transparent',
        hoverStroke: '#1890ff',
        selectedStroke: '#1890ff'
      },
      bezier: {
        strokeWidth: 1.5,
        stroke: '#c0c4cc', // 柔和的灰色
        outlineColor: 'transparent',
        hoverStroke: '#1890ff',
        selectedStroke: '#1890ff'
      },
      edgeText: {
        fill: '#8c8c8c',
        fontSize: 12
      }
    },
    // 禁用默认的右键菜单
    contextmenu: false,
    // 禁用默认的工具栏（如果需要可以启用）
    toolbar: false,
    // 允许节点拖拽
    isSilentMode: false,
    // 允许调整节点大小
    adjustNodePosition: true,
    // 允许调整边
    adjustEdgeStartAndEnd: true,
    // 允许节点重叠
    allowNodeOverlap: false,
    // 节点文本配置 - 禁用默认 SVG 文本显示
    nodeTextEdit: false,
    nodeTextDraggable: false
  }
}

/**
 * 获取只读模式配置
 */
export function getReadonlyConfig(): LogicFlowConfig {
  return {
    ...getDefaultConfig(),
    isSilentMode: true,
    adjustNodePosition: false,
    adjustEdgeStartAndEnd: false,
    keyboard: {
      enabled: false
    }
  }
}

/**
 * 暗色主题配置
 */
export function getDarkThemeConfig(): LogicFlowConfig {
  return {
    ...getDefaultConfig(),
    grid: {
      size: 10,
      visible: true,
      type: 'dot',
      config: {
        color: '#3a3a3a',
        thickness: 1
      }
    },
    background: {
      color: '#1a1a1a'
    }
  }
}
