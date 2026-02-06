/**
 * LogicFlow 自定义节点
 *
 * @module components/core/charts/art-logic-flow/custom-nodes
 */

import { HtmlNode, HtmlNodeModel } from '@logicflow/core'

/**
 * 获取 CSS 变量值
 */
const getCSSVar = (varName: string, fallback: string = ''): string => {
  return getComputedStyle(document.documentElement).getPropertyValue(varName).trim() || fallback
}

/**
 * 获取背景颜色
 */
const getBgColor = (): string => {
  return getCSSVar('--el-bg-color', '#ffffff')
}

/**
 * 获取主要文本颜色
 */
const getTextPrimaryColor = (): string => {
  return getCSSVar('--el-text-color-primary', '#303133')
}

/**
 * 获取次要文本颜色
 */
const getTextSecondaryColor = (): string => {
  return getCSSVar('--el-text-color-secondary', '#606266')
}

/**
 * 玻璃态悬浮节点
 */
class GlassNodeModel extends HtmlNodeModel {
  /**
   * 设置节点形状
   */
  setAttributes() {
    this.width = 180
    this.height = 60
    this.text.editable = false
    // 禁用默认文本显示，防止重复
    this.text.value = ''
  }
}

class GlassNodeView extends HtmlNode {
  /**
   * 渲染 HTML 内容
   */
  setHtml(rootEl: HTMLElement | SVGForeignObjectElement) {
    // LogicFlow 类型定义中可能是 SVGForeignObjectElement，但在运行时通常是 HTMLElement 兼容的
    const el = rootEl as HTMLElement
    const { properties } = this.props.model
    const { nodeType, isSelected, label } = properties as any
    // 使用 properties 中的 label，而不是 text.value
    const textLabel = label || ''

    // 获取主题颜色
    const bgColor = getBgColor()
    const textPrimaryColor = getTextPrimaryColor()
    const textSecondaryColor = getTextSecondaryColor()

    // 获取层级颜色
    const getColor = (type: string) => {
      const colors: Record<string, string> = {
        campus: '#1890ff',
        department: '#52c41a',
        major: '#faad14',
        class: '#eb2f96'
      }
      return colors[type] || '#1890ff'
    }

    const themeColor = getColor(nodeType)

    // 清空现有内容
    el.innerHTML = ''

    // 创建玻璃态容器
    const container = document.createElement('div')
    container.className = `glass-node-container ${isSelected ? 'is-selected' : ''}`

    // 注入样式
    // 边框始终使用主题色，选中时更亮更粗
    container.style.cssText = `
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      padding: 0 12px;
      background: ${bgColor};
      backdrop-filter: blur(12px);
      -webkit-backdrop-filter: blur(12px);
      border: ${isSelected ? '2px' : '1.5px'} solid ${isSelected ? themeColor : `${themeColor}50`};
      border-radius: 12px;
      box-shadow: ${isSelected ? `0 8px 20px -4px ${themeColor}35` : '0 2px 8px rgba(0, 0, 0, 0.04)'};
      transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
      position: relative;
      cursor: pointer;
      box-sizing: border-box;
    `

    // 添加鼠标移入背景色变化效果
    container.onmouseenter = () => {
      container.style.background = `${themeColor}10` // 淡淡的主题色背景
      container.style.borderColor = themeColor
    }
    container.onmouseleave = () => {
      container.style.background = bgColor
      container.style.borderColor = isSelected ? themeColor : `${themeColor}50`
    }

    // 创建顶部彩色点（未选中时暗，选中时亮）
    const dot = document.createElement('div')
    dot.style.cssText = `
      position: absolute;
      top: 6px;
      right: 8px;
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: ${isSelected ? themeColor : `${themeColor}60`};
      box-shadow: ${isSelected ? `0 0 8px ${themeColor}` : 'none'};
      transition: all 0.3s ease;
    `
    container.appendChild(dot)

    // 创建图标/标识
    const icon = document.createElement('div')
    icon.style.cssText = `
      width: 32px;
      height: 32px;
      border-radius: 8px;
      background: ${themeColor}15;
      color: ${themeColor};
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 10px;
      font-size: 16px;
      flex-shrink: 0;
    `
    // 根据类型设置简单标识
    const getSymbol = (type: string) => {
      const symbols: Record<string, string> = {
        campus: '🏫',
        department: '🏢',
        major: '🎓',
        class: '👥'
      }
      return symbols[type] || '📍'
    }
    icon.innerText = getSymbol(nodeType)
    container.appendChild(icon)

    // 创建文本内容
    const textContent = document.createElement('div')
    textContent.style.cssText = `
      flex: 1;
      min-width: 0;
    `

    const title = document.createElement('div')
    title.style.cssText = `
      font-size: 13px;
      font-weight: 500;
      color: ${textPrimaryColor};
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    `
    title.innerText = textLabel
    textContent.appendChild(title)

    const subTitle = document.createElement('div')
    subTitle.style.cssText = `
      font-size: 10px;
      color: ${textSecondaryColor};
      text-transform: uppercase;
      letter-spacing: 0.5px;
    `
    subTitle.innerText = nodeType
    textContent.appendChild(subTitle)

    container.appendChild(textContent)
    rootEl.appendChild(container)
  }
}

export const registerGlassNode = (lf: any) => {
  lf.register({
    type: 'glass-node',
    view: GlassNodeView,
    model: GlassNodeModel
  })
}
