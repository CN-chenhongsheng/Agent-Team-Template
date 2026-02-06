/**
 * LogicFlow 审批流程自定义节点
 *
 * @module components/core/charts/art-logic-flow/approval-node
 */

import { HtmlNode, HtmlNodeModel } from '@logicflow/core'

// ==================== 工具函数 ====================

/**
 * 获取 CSS 变量值
 */
const getCSSVar = (varName: string, fallback: string = ''): string => {
  return getComputedStyle(document.documentElement).getPropertyValue(varName).trim() || fallback
}

/**
 * 获取系统主题色
 */
const getThemeColor = (): string => {
  return getCSSVar('--el-color-primary', '#5D87FF')
}

/**
 * 获取成功色（用于开始节点）
 */
const getSuccessColor = (): string => {
  return getCSSVar('--el-color-success', '#22c55e')
}

/**
 * 获取信息色（用于结束节点）
 */
const getInfoColor = (): string => {
  return getCSSVar('--el-color-info', '#6b7280')
}

/**
 * 获取背景色
 */
const getBgColor = (): string => {
  return getCSSVar('--el-bg-color', '#ffffff')
}

/**
 * 获取边框颜色
 */
const getBorderColor = (): string => {
  return getCSSVar('--el-border-color-light', '#e5e7eb')
}

/**
 * 获取填充色
 */
const getFillColor = (): string => {
  return getCSSVar('--el-fill-color', '#f5f7fa')
}

/**
 * 获取主要文字颜色
 */
const getTextPrimaryColor = (): string => {
  return getCSSVar('--el-text-color-primary', '#303133')
}

/**
 * 获取次要文字颜色
 */
const getTextSecondaryColor = (): string => {
  return getCSSVar('--el-text-color-secondary', '#606266')
}

/**
 * 获取占位符文字颜色
 */
const getTextPlaceholderColor = (): string => {
  return getCSSVar('--el-text-color-placeholder', '#a8abb2')
}

/**
 * 获取危险色
 */
const getDangerColor = (): string => {
  return getCSSVar('--el-color-danger', '#ef4444')
}

/**
 * 获取危险色浅色背景
 */
const getDangerLightBg = (): string => {
  return getCSSVar('--el-color-danger-light-9', '#fef2f2')
}

// ==================== 审批节点 ====================

/**
 * 审批节点数据模型
 */
class ApprovalNodeModel extends HtmlNodeModel {
  setAttributes() {
    this.width = 220
    this.height = 80
    this.text.editable = false
    this.text.value = ''
  }

  getConnectedTargetRules() {
    const rules = super.getConnectedTargetRules()
    return rules
  }
}

/**
 * 审批节点视图
 */
class ApprovalNodeView extends HtmlNode {
  setHtml(rootEl: HTMLElement | SVGForeignObjectElement) {
    const el = rootEl as HTMLElement
    const { properties } = this.props.model
    const {
      nodeName = '审批节点',
      nodeType = 1,
      assigneeCount = 0,
      isSelected = false
    } = properties as any

    const primaryColor = getThemeColor()

    // 节点类型配置
    const nodeTypeConfig: Record<number, { label: string; color: string; icon: string }> = {
      1: { label: '串行', color: primaryColor, icon: '🔹' },
      2: { label: '会签', color: '#f97316', icon: '🔷' },
      3: { label: '或签', color: '#22c55e', icon: '🔸' }
    }

    const config = nodeTypeConfig[nodeType] || nodeTypeConfig[1]

    el.innerHTML = ''

    const container = document.createElement('div')
    container.className = 'approval-node-container'
    const bgColor = getBgColor()
    const borderColor = getBorderColor()

    container.style.cssText = `
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      padding: 12px 16px;
      background: ${isSelected ? `${primaryColor}50` : bgColor};
      border: 2px solid ${isSelected ? config.color : borderColor};
      border-radius: 12px;
      box-shadow: ${isSelected ? `0 4px 12px ${config.color}70` : '0 2px 8px rgba(0,0,0,0.06)'};
      transition: all 0.2s ease;
      cursor: pointer;
      box-sizing: border-box;
      gap: 12px;
      position: relative;
    `

    // 左侧图标
    const iconWrapper = document.createElement('div')
    iconWrapper.style.cssText = `
      width: 40px;
      height: 40px;
      border-radius: 10px;
      background: ${config.color}15;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px;
      flex-shrink: 0;
    `
    iconWrapper.innerText = config.icon
    container.appendChild(iconWrapper)

    // 中间内容
    const content = document.createElement('div')
    content.style.cssText = `
      flex: 1;
      min-width: 0;
      display: flex;
      flex-direction: column;
      gap: 4px;
    `

    const textPrimaryColor = getTextPrimaryColor()

    const title = document.createElement('div')
    title.style.cssText = `
      font-size: 14px;
      font-weight: 600;
      color: ${textPrimaryColor};
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    `
    title.innerText = nodeName
    content.appendChild(title)

    const meta = document.createElement('div')
    meta.style.cssText = `
      display: flex;
      align-items: center;
      gap: 8px;
    `

    const badge = document.createElement('span')
    badge.style.cssText = `
      font-size: 11px;
      padding: 2px 8px;
      border-radius: 4px;
      background: ${config.color}15;
      color: ${config.color};
      font-weight: 500;
    `
    badge.innerText = config.label
    meta.appendChild(badge)

    content.appendChild(meta)
    container.appendChild(content)

    // 右侧审批人数
    const textSecondaryColor = getTextSecondaryColor()

    const countWrapper = document.createElement('div')
    countWrapper.style.cssText = `
      display: flex;
      align-items: center;
      gap: 4px;
      color: ${textSecondaryColor};
      font-size: 13px;
      flex-shrink: 0;
    `
    countWrapper.innerHTML = `<span style="font-size: 16px;">👤</span><span>${assigneeCount}人</span>`
    container.appendChild(countWrapper)

    // 右上角删除按钮
    const deleteBtn = document.createElement('div')
    deleteBtn.className = 'delete-btn'
    const dangerColor = getDangerColor()
    const dangerLightBg = getDangerLightBg()
    deleteBtn.style.cssText = `
      position: absolute;
      top: 4px;
      right: 4px;
      width: 20px;
      height: 20px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: ${dangerLightBg};
      border-radius: 4px;
      cursor: pointer;
      opacity: 0;
      transition: opacity 0.2s ease;
      z-index: 10;
    `
    deleteBtn.innerHTML = `
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="${dangerColor}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M3 6h18M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"></path>
        <line x1="10" y1="11" x2="10" y2="17"></line>
        <line x1="14" y1="11" x2="14" y2="17"></line>
      </svg>
    `

    // Hover 效果 + 显示删除按钮
    container.onmouseenter = () => {
      if (!isSelected) {
        container.style.borderColor = config.color
        container.style.boxShadow = `0 4px 12px ${config.color}20`
      }
      deleteBtn.style.opacity = '1'
    }
    container.onmouseleave = () => {
      if (!isSelected) {
        container.style.borderColor = borderColor
        container.style.boxShadow = '0 2px 8px rgba(0,0,0,0.06)'
      }
      deleteBtn.style.opacity = '0'
    }

    // 删除按钮点击事件 - 阻止冒泡，防止触发节点点击
    deleteBtn.onclick = (e) => {
      e.stopPropagation()
      e.preventDefault()

      // 获取节点数据 - 尝试多种方式获取 properties
      const nodeData = this.props.model.getData()
      const nodeId = nodeData.id

      // 尝试从多个来源获取 properties 和 originalId
      const propertiesFromModel = (this.props.model as any).properties || {}
      const propertiesFromData = nodeData.properties || {}
      const properties = { ...propertiesFromModel, ...propertiesFromData }
      const originalId = properties.originalId || nodeData.originalId

      // 通过自定义 DOM 事件触发删除（从 document 触发，确保能被监听器捕获）
      // 传递 originalId 用于查找，也传递 lfNodeId 作为备用
      const customEvent = new CustomEvent('logicflow-node-delete', {
        detail: { nodeId, originalId, nodeData, properties },
        bubbles: true,
        cancelable: true
      })
      // 从 document 触发事件，确保能被容器监听器捕获
      document.dispatchEvent(customEvent)
    }

    deleteBtn.onmouseenter = () => {
      deleteBtn.style.background = '#fee2e2'
    }
    deleteBtn.onmouseleave = () => {
      deleteBtn.style.background = '#fef2f2'
    }

    container.appendChild(deleteBtn)

    rootEl.appendChild(container)
  }
}

// ==================== 开始节点 ====================

/**
 * 开始节点数据模型
 */
class StartNodeModel extends HtmlNodeModel {
  setAttributes() {
    this.width = 220
    this.height = 80
    this.text.editable = false
    this.text.value = ''
  }

  getConnectedSourceRules() {
    const rules = super.getConnectedSourceRules()
    return rules
  }
}

/**
 * 开始节点视图 - 卡片形式
 */
class StartNodeView extends HtmlNode {
  setHtml(rootEl: HTMLElement | SVGForeignObjectElement) {
    const el = rootEl as HTMLElement
    const { properties } = this.props.model
    const { flowName = '', isSelected = false } = properties as any

    const successColor = getSuccessColor()
    const bgColor = getBgColor()
    const borderColor = getBorderColor()

    el.innerHTML = ''

    const container = document.createElement('div')
    container.className = 'start-node-container'
    container.style.cssText = `
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      padding: 12px 16px;
      background: ${isSelected ? `${successColor}50` : bgColor};
      border: 2px solid ${isSelected ? successColor : borderColor};
      border-radius: 12px;
      box-shadow: ${isSelected ? `0 4px 12px ${successColor}70` : '0 2px 8px rgba(0,0,0,0.06)'};
      transition: all 0.2s ease;
      cursor: pointer;
      box-sizing: border-box;
      gap: 12px;
    `

    // Hover 效果
    container.onmouseenter = () => {
      if (!isSelected) {
        container.style.borderColor = successColor
        container.style.boxShadow = `0 4px 12px ${successColor}20`
      }
    }
    container.onmouseleave = () => {
      if (!isSelected) {
        container.style.borderColor = borderColor
        container.style.boxShadow = '0 2px 8px rgba(0,0,0,0.06)'
      }
    }

    // 左侧图标
    const iconWrapper = document.createElement('div')
    iconWrapper.style.cssText = `
      width: 40px;
      height: 40px;
      border-radius: 10px;
      background: ${successColor};
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 18px;
      flex-shrink: 0;
      color: #ffffff;
    `
    iconWrapper.innerHTML =
      '<svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>'
    container.appendChild(iconWrapper)

    // 中间内容
    const content = document.createElement('div')
    content.style.cssText = `
      flex: 1;
      min-width: 0;
      display: flex;
      flex-direction: column;
      gap: 4px;
    `

    const title = document.createElement('div')
    title.style.cssText = `
      font-size: 14px;
      font-weight: 600;
      color: ${successColor};
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    `
    title.innerText = '开始'
    content.appendChild(title)

    const textSecondaryColor = getTextSecondaryColor()
    const textPlaceholderColor = getTextPlaceholderColor()

    const meta = document.createElement('div')
    meta.style.cssText = `
      font-size: 12px;
      color: ${textSecondaryColor};
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    `
    meta.innerText = flowName || '点击配置流程信息'
    content.appendChild(meta)

    container.appendChild(content)

    // 右侧箭头指示
    const arrow = document.createElement('div')
    arrow.style.cssText = `
      color: ${textPlaceholderColor};
      font-size: 16px;
      flex-shrink: 0;
    `
    arrow.innerHTML =
      '<svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M8.59 16.59L13.17 12 8.59 7.41 10 6l6 6-6 6-1.41-1.41z"/></svg>'
    container.appendChild(arrow)

    rootEl.appendChild(container)
  }
}

// ==================== 结束节点 ====================

/**
 * 结束节点数据模型
 */
class EndNodeModel extends HtmlNodeModel {
  setAttributes() {
    this.width = 220
    this.height = 80
    this.text.editable = false
    this.text.value = ''
  }

  getConnectedTargetRules() {
    const rules = super.getConnectedTargetRules()
    return rules
  }
}

/**
 * 结束节点视图 - 卡片形式
 */
class EndNodeView extends HtmlNode {
  setHtml(rootEl: HTMLElement | SVGForeignObjectElement) {
    const el = rootEl as HTMLElement
    const { properties } = this.props.model
    const { isSelected = false } = properties as any

    const infoColor = getInfoColor()
    const fillColor = getFillColor()
    const borderColor = getBorderColor()

    el.innerHTML = ''

    const container = document.createElement('div')
    container.className = 'end-node-container'
    container.style.cssText = `
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      padding: 12px 16px;
      background: ${isSelected ? `${infoColor}50` : fillColor};
      border: 2px solid ${isSelected ? infoColor : borderColor};
      border-radius: 12px;
      box-shadow: ${isSelected ? `0 4px 12px ${infoColor}70` : '0 2px 8px rgba(0,0,0,0.06)'};
      transition: all 0.2s ease;
      cursor: pointer;
      box-sizing: border-box;
      gap: 12px;
    `

    // Hover 效果
    container.onmouseenter = () => {
      if (!isSelected) {
        container.style.borderColor = infoColor
        container.style.boxShadow = `0 4px 12px ${infoColor}20`
      }
    }
    container.onmouseleave = () => {
      if (!isSelected) {
        container.style.borderColor = borderColor
        container.style.boxShadow = '0 2px 8px rgba(0,0,0,0.06)'
      }
    }

    // 左侧图标
    const iconWrapper = document.createElement('div')
    iconWrapper.style.cssText = `
      width: 40px;
      height: 40px;
      border-radius: 10px;
      background: ${infoColor};
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 18px;
      flex-shrink: 0;
      color: #ffffff;
    `
    iconWrapper.innerHTML =
      '<svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M6 6h12v12H6z"/></svg>'
    container.appendChild(iconWrapper)

    // 中间内容
    const content = document.createElement('div')
    content.style.cssText = `
      flex: 1;
      min-width: 0;
      display: flex;
      flex-direction: column;
      gap: 4px;
    `

    const title = document.createElement('div')
    title.style.cssText = `
      font-size: 14px;
      font-weight: 600;
      color: ${infoColor};
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    `
    title.innerText = '结束'
    content.appendChild(title)

    const textPlaceholderColor = getTextPlaceholderColor()

    const meta = document.createElement('div')
    meta.style.cssText = `
      font-size: 12px;
      color: ${textPlaceholderColor};
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    `
    meta.innerText = '流程结束'
    content.appendChild(meta)

    container.appendChild(content)

    rootEl.appendChild(container)
  }
}

// ==================== 添加节点按钮 ====================

/**
 * 添加节点按钮数据模型
 */
class AddButtonNodeModel extends HtmlNodeModel {
  setAttributes() {
    this.width = 32
    this.height = 32
    this.text.editable = false
    this.text.value = ''
  }
}

/**
 * 添加节点按钮视图
 */
class AddButtonNodeView extends HtmlNode {
  setHtml(rootEl: HTMLElement | SVGForeignObjectElement) {
    const el = rootEl as HTMLElement
    const { properties } = this.props.model
    const { isHovered = false } = properties as any

    const primaryColor = getThemeColor()
    const borderColor = getBorderColor()
    const textSecondaryColor = getTextSecondaryColor()
    const textPlaceholderColor = getTextPlaceholderColor()

    el.innerHTML = ''

    const button = document.createElement('div')
    button.style.cssText = `
      width: 28px;
      height: 28px;
      border-radius: 50%;
      background: ${isHovered ? primaryColor : borderColor};
      border: 2px dashed ${isHovered ? primaryColor : textPlaceholderColor};
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: all 0.2s ease;
      color: ${isHovered ? getCSSVar('--el-bg-color', '#ffffff') : textSecondaryColor};
      font-size: 18px;
      font-weight: bold;
    `
    button.innerText = '+'

    button.onmouseenter = () => {
      const color = getThemeColor()
      button.style.background = color
      button.style.borderColor = color
      button.style.color = getCSSVar('--el-bg-color', '#ffffff')
    }
    button.onmouseleave = () => {
      button.style.background = getBorderColor()
      button.style.borderColor = getTextPlaceholderColor()
      button.style.color = getTextSecondaryColor()
    }

    rootEl.appendChild(button)
  }
}

/**
 * 注册审批相关节点
 */
export const registerApprovalNodes = (lf: any) => {
  lf.register({
    type: 'approval-node',
    view: ApprovalNodeView,
    model: ApprovalNodeModel
  })

  lf.register({
    type: 'start-node',
    view: StartNodeView,
    model: StartNodeModel
  })

  lf.register({
    type: 'end-node',
    view: EndNodeView,
    model: EndNodeModel
  })

  lf.register({
    type: 'add-button-node',
    view: AddButtonNodeView,
    model: AddButtonNodeModel
  })
}
