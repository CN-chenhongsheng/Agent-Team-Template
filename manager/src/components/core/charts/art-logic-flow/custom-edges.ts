/**
 * LogicFlow 自定义边 - 带+号添加图标
 */
import { BezierEdge, BezierEdgeModel } from '@logicflow/core'
import { h } from '@logicflow/core'

/**
 * 获取 CSS 变量值
 */
const getCSSVar = (varName: string, fallback: string = ''): string => {
  return getComputedStyle(document.documentElement).getPropertyValue(varName).trim() || fallback
}

/**
 * 获取主题色
 */
const getPrimaryColor = (): string => {
  return getCSSVar('--el-color-primary', '#5D87FF')
}

/**
 * 自定义边 Model - 管理边的数据和状态
 */
class CustomBezierEdgeModel extends BezierEdgeModel {
  /**
   * 设置边的样式
   */
  setAttributes() {
    this.isAnimation = false // 禁用动画
  }

  /**
   * 获取边的样式
   */
  getEdgeStyle() {
    const style = super.getEdgeStyle()
    const { isSelected, isHovered } = this.properties

    return {
      ...style,
      stroke: isSelected || isHovered ? getPrimaryColor() : '#94a3b8',
      strokeWidth: isSelected || isHovered ? 2 : 1,
      strokeDasharray: '5, 5'
    }
  }
}

/**
 * 自定义边 View - 渲染边的视图和+号图标
 */
class CustomBezierEdgeView extends BezierEdge {
  /**
   * 获取边上的+号图标（渲染在边的中点）
   */
  getAddButton() {
    const { model } = this.props
    const { isHovered, isSelected } = model.properties

    // 计算边的中点位置
    const { startPoint, endPoint } = model
    const centerX = (startPoint.x + endPoint.x) / 2
    const centerY = (startPoint.y + endPoint.y) / 2

    // hover 或选中时更明显
    const isVisible = isHovered || isSelected

    return h(
      'g',
      {
        className: 'lf-edge-add-button',
        transform: `translate(${centerX}, ${centerY})`,
        style: {
          opacity: isVisible ? 1 : 0.6,
          transition: 'opacity 0.2s',
          cursor: 'pointer'
        }
      },
      [
        // 圆形背景
        h('circle', {
          r: 14,
          fill: isVisible ? getPrimaryColor() : `${getPrimaryColor()}80`,
          stroke: getPrimaryColor(),
          strokeWidth: 2,
          style: {
            transition: 'all 0.2s'
          }
        }),
        // +号文字
        h(
          'text',
          {
            x: 0,
            y: 0,
            textAnchor: 'middle',
            dominantBaseline: 'middle',
            fill: '#ffffff',
            fontSize: 20,
            fontWeight: 'bold',
            style: {
              pointerEvents: 'none',
              userSelect: 'none'
            }
          },
          '+'
        )
      ]
    )
  }

  /**
   * 重写 getShape 方法，添加+号图标
   */
  getShape() {
    const edgePath = super.getShape()
    const addButton = this.getAddButton()

    return h('g', {}, [edgePath, addButton])
  }
}

/**
 * 注册自定义边
 */
export function registerCustomEdge(lf: any) {
  lf.register({
    type: 'custom-bezier',
    view: CustomBezierEdgeView,
    model: CustomBezierEdgeModel
  })
}
