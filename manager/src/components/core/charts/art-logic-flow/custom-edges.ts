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

const getPrimaryColor = (): string => getCSSVar('--el-color-primary', '#5D87FF')
const getBorderColor = (): string => getCSSVar('--el-border-color', '#dcdfe6')
const getBoxColor = (): string => getCSSVar('--default-box-color', '#ffffff')
const getTextSecondary = (): string => getCSSVar('--el-text-color-secondary', '#909399')

/**
 * 计算三次贝塞尔曲线在 t 处的点
 * B(t) = (1-t)³P0 + 3(1-t)²tP1 + 3(1-t)t²P2 + t³P3
 */
const bezierPoint = (
  p0: { x: number; y: number },
  p1: { x: number; y: number },
  p2: { x: number; y: number },
  p3: { x: number; y: number },
  t: number
): { x: number; y: number } => {
  const mt = 1 - t
  const mt2 = mt * mt
  const mt3 = mt2 * mt
  const t2 = t * t
  const t3 = t2 * t
  return {
    x: mt3 * p0.x + 3 * mt2 * t * p1.x + 3 * mt * t2 * p2.x + t3 * p3.x,
    y: mt3 * p0.y + 3 * mt2 * t * p1.y + 3 * mt * t2 * p2.y + t3 * p3.y
  }
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
      stroke: isSelected || isHovered ? getPrimaryColor() : getBorderColor(),
      strokeWidth: isSelected || isHovered ? 2.5 : 1.5,
      strokeDasharray: '8, 4'
    }
  }
}

/**
 * 自定义边 View - 渲染边的视图和+号图标
 */
class CustomBezierEdgeView extends BezierEdge {
  /**
   * 处理+号按钮点击
   */
  handleAddClick(e: MouseEvent) {
    console.log('[CustomBezierEdge] +号按钮被点击')
    e.stopPropagation()
    e.preventDefault()

    // 触发自定义事件，通知外部组件
    const { model } = this.props
    const edgeData = (model as any).getData()

    // 通过 document 触发全局事件
    document.dispatchEvent(
      new CustomEvent('lf-edge-add-node-click', {
        detail: {
          edgeId: edgeData.id,
          sourceNodeId: edgeData.sourceNodeId,
          targetNodeId: edgeData.targetNodeId,
          edgeData: edgeData
        },
        bubbles: true,
        cancelable: true
      })
    )
  }

  /**
   * 获取边上的+号图标（渲染在贝塞尔曲线的中点）
   */
  getAddButton() {
    const { model } = this.props
    const { isHovered, isSelected } = model.properties

    // 计算贝塞尔曲线的实际中点
    let centerX: number
    let centerY: number
    const pointsList = (model as any).pointsList

    if (pointsList && pointsList.length >= 4) {
      // 使用贝塞尔公式计算 t=0.5 处的真实曲线中点
      const mid = bezierPoint(pointsList[0], pointsList[1], pointsList[2], pointsList[3], 0.5)
      centerX = mid.x
      centerY = mid.y
    } else {
      // 回退：使用起终点直线中点
      const { startPoint, endPoint } = model
      centerX = (startPoint.x + endPoint.x) / 2
      centerY = (startPoint.y + endPoint.y) / 2
    }

    const isActive = isHovered || isSelected
    const primaryColor = getPrimaryColor()

    // 创建一个唯一的类名用于查找
    const buttonClass = `lf-edge-add-button-${model.id}`

    const buttonG = h(
      'g',
      {
        className: `lf-edge-add-button ${buttonClass}`,
        transform: `translate(${centerX}, ${centerY})`,
        style: {
          opacity: isActive ? 1 : 0.6,
          transition: 'all 0.25s ease',
          cursor: 'pointer',
          pointerEvents: 'all' // 确保可以接收点击事件
        }
      },
      [
        // 外发光圈环（hover 时显示）
        h('circle', {
          r: 16,
          fill: 'none',
          stroke: primaryColor,
          strokeWidth: 1,
          strokeOpacity: isActive ? 0.25 : 0,
          style: {
            transition: 'all 0.25s ease',
            pointerEvents: 'none' // 不接收点击，让外层 g 处理
          }
        }),
        // 圆形背景（可点击区域）
        h('circle', {
          r: 10,
          fill: isActive ? primaryColor : getBoxColor(),
          stroke: isActive ? primaryColor : getBorderColor(),
          strokeWidth: isActive ? 1.5 : 1,
          style: {
            transition: 'all 0.25s ease',
            cursor: 'pointer'
          }
        }),
        // +号（使用两条交叉线而非文字，更精致）
        h('line', {
          x1: -4,
          y1: 0,
          x2: 4,
          y2: 0,
          stroke: isActive ? getBoxColor() : getTextSecondary(),
          strokeWidth: 1.5,
          strokeLinecap: 'round',
          style: {
            pointerEvents: 'none',
            transition: 'stroke 0.25s ease'
          }
        }),
        h('line', {
          x1: 0,
          y1: -4,
          x2: 0,
          y2: 4,
          stroke: isActive ? getBoxColor() : getTextSecondary(),
          strokeWidth: 1.5,
          strokeLinecap: 'round',
          style: {
            pointerEvents: 'none',
            transition: 'stroke 0.25s ease'
          }
        })
      ]
    )

    // 返回后，在下一个事件循环中绑定真实的 DOM 事件
    // 因为 h() 函数是虚拟 DOM，需要等它渲染成真实 DOM 后才能绑定事件
    setTimeout(() => {
      const button = document.querySelector(`.${buttonClass}`)

      if (button && !(button as any).__hasClickListener) {
        console.log('[CustomBezierEdge] 为+号按钮绑定点击事件')
        ;(button as any).__hasClickListener = true

        // 绑定点击事件
        const clickHandler = this.handleAddClick.bind(this)
        button.addEventListener('click', clickHandler, true)
        button.addEventListener(
          'mousedown',
          (e: Event) => {
            e.stopPropagation()
          },
          true
        )
      }
    }, 150)

    return buttonG
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
