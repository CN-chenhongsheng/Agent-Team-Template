/**
 * LogicFlow 审批流程自定义节点
 * 使用 createVNode + render 在 HtmlNode 内挂载 Vue 卡片组件
 *
 * @module components/core/charts/art-logic-flow/approval-node
 */

import { HtmlNode, HtmlNodeModel } from '@logicflow/core'
import { createVNode, render } from 'vue'
import ApprovalNodeCard from '@/views/approval/flow-config/modules/approval-node-card.vue'
import StartNodeCard from '@/views/approval/flow-config/modules/start-node-card.vue'

// ==================== 工具函数 ====================

/**
 * 获取 CSS 变量值
 */
const getCSSVar = (varName: string, fallback: string = ''): string => {
  return getComputedStyle(document.documentElement).getPropertyValue(varName).trim() || fallback
}

/**
 * 获取信息色（用于结束节点）
 */
const getInfoColor = (): string => {
  return getCSSVar('--el-color-info', '#6b7280')
}

/**
 * 获取填充色
 */
const getFillColor = (): string => {
  return getCSSVar('--el-fill-color', '#f5f7fa')
}

/**
 * 获取边框颜色
 */
const getBorderColor = (): string => {
  return getCSSVar('--el-border-color-light', '#e5e7eb')
}

/**
 * 获取占位符文字颜色
 */
const getTextPlaceholderColor = (): string => {
  return getCSSVar('--el-text-color-placeholder', '#a8abb2')
}

// ==================== 审批节点 ====================

/**
 * 审批节点数据模型
 * 宽度 320，高度根据 isExpanded 动态设置
 */
class ApprovalNodeModel extends HtmlNodeModel {
  setAttributes() {
    this.width = 320
    const isExpanded = (this.properties as any)?.isExpanded || false
    this.height = isExpanded ? 440 : 80
    this.text.editable = false
    this.text.value = ''
  }

  // 只保留左右锚点（水平布局）
  getDefaultAnchor() {
    const { x, y, width } = this
    return [
      { x: x - width / 2, y, id: `${this.id}_left` },
      { x: x + width / 2, y, id: `${this.id}_right` }
    ]
  }

  getConnectedTargetRules() {
    const rules = super.getConnectedTargetRules()
    return rules
  }
}

/**
 * 审批节点视图 - 使用 createVNode + render 挂载 ApprovalNodeCard
 */
class ApprovalNodeView extends HtmlNode {
  setHtml(rootEl: HTMLElement | SVGForeignObjectElement) {
    const el = rootEl as HTMLElement
    const properties = (this.props.model as any).properties || {}
    const modelId = this.props.model.id

    // 检查缓存的容器
    let container = (el as any).__vueContainer as HTMLElement | undefined
    if (!container) {
      el.innerHTML = ''
      container = document.createElement('div')
      container.style.cssText = 'width: 100%; height: 100%;'
      el.appendChild(container)
      ;(el as any).__vueContainer = container
    }

    // 创建 VNode 并渲染（Vue 会自动 patch 已有实例）
    const vnode = createVNode(ApprovalNodeCard, {
      nodeName: properties.nodeName || '',
      nodeType: properties.nodeType || 1,
      rejectAction: properties.rejectAction || 1,
      assignees: [...(properties.assignees || [])],
      isExpanded: properties.isExpanded || false,
      isSelected: properties.isSelected || false,
      nodeOrder: properties.nodeOrder || 1,
      nodeId: modelId
    })
    render(vnode, container)
  }
}

// ==================== 开始节点 ====================

/**
 * 开始节点数据模型
 * 宽度 320，高度根据 isExpanded 动态设置
 */
class StartNodeModel extends HtmlNodeModel {
  setAttributes() {
    this.width = 320
    const isExpanded = (this.properties as any)?.isExpanded || false
    this.height = isExpanded ? 380 : 80
    this.text.editable = false
    this.text.value = ''
  }

  // 只保留左右锚点（水平布局）
  getDefaultAnchor() {
    const { x, y, width } = this
    return [
      { x: x - width / 2, y, id: `${this.id}_left` },
      { x: x + width / 2, y, id: `${this.id}_right` }
    ]
  }

  getConnectedSourceRules() {
    const rules = super.getConnectedSourceRules()
    return rules
  }
}

/**
 * 开始节点视图 - 使用 createVNode + render 挂载 StartNodeCard
 */
class StartNodeView extends HtmlNode {
  setHtml(rootEl: HTMLElement | SVGForeignObjectElement) {
    const el = rootEl as HTMLElement
    const properties = (this.props.model as any).properties || {}
    const modelId = this.props.model.id

    // 检查缓存的容器
    let container = (el as any).__vueContainer as HTMLElement | undefined
    if (!container) {
      el.innerHTML = ''
      container = document.createElement('div')
      container.style.cssText = 'width: 100%; height: 100%;'
      el.appendChild(container)
      ;(el as any).__vueContainer = container
    }

    const vnode = createVNode(StartNodeCard, {
      flowName: properties.flowName || '',
      flowCode: properties.flowCode || '',
      businessType: properties.businessType || '',
      description: properties.description || '',
      isExpanded: properties.isExpanded || false,
      isSelected: properties.isSelected || false,
      nodeId: modelId,
      dialogType: properties.dialogType || 'add',
      businessTypeOptions: properties.businessTypeOptions || []
    })
    render(vnode, container)
  }
}

// ==================== 结束节点 ====================

/**
 * 结束节点数据模型
 * 宽度 320（统一宽度），保留原生 DOM 渲染
 */
class EndNodeModel extends HtmlNodeModel {
  setAttributes() {
    this.width = 320
    this.height = 80
    this.text.editable = false
    this.text.value = ''
  }

  // 只保留左右锚点（水平布局）
  getDefaultAnchor() {
    const { x, y, width } = this
    return [
      { x: x - width / 2, y, id: `${this.id}_left` },
      { x: x + width / 2, y, id: `${this.id}_right` }
    ]
  }

  getConnectedTargetRules() {
    const rules = super.getConnectedTargetRules()
    return rules
  }
}

/**
 * 结束节点视图 - 保留原生 DOM 渲染（无需编辑功能）
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

// ==================== 注册函数 ====================

/**
 * 注册审批相关节点（移除 add-button-node）
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
}
