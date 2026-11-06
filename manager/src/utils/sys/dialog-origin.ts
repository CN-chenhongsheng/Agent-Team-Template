/**
 * 全局弹窗动画起点
 *
 * 通过 Element Plus 的全局 dialog.transition 配置，让每个 el-dialog
 * 以「触发按钮方向」为 transform-origin 做方向性缩放，关闭时缩回同一方向。
 *
 * 触发源在全局 click 阶段记录，弹窗真正完成布局后（Transition onEnter）
 * 再测量弹窗 rect 并换算成相对弹窗的像素坐标，避免视口中心、滚动条和
 * 非居中弹窗带来的偏差。
 *
 * @module utils/sys/dialog-origin
 * @author 陈鸿昇
 * @date 2026-11-06
 */
import type { DialogConfigContext } from 'element-plus'

/** 可作为弹窗触发源的可交互元素 */
const TRIGGER_SELECTOR = [
  'button',
  '[role="button"]',
  '[role="menuitem"]',
  '.el-dropdown-menu__item',
  '.el-select-dropdown__item',
  '.el-menu-item',
  'a'
].join(',')

/** 触发源有效期：超过后视为无来源，居中缩放 */
const TRIGGER_TTL = 2500
/** 触发元素已卸载时，点击坐标的有效期 */
const POINT_TTL = 400
/** 触发点离弹窗中心的最大距离，超出后向中心收缩，避免长距离扫屏 */
const MAX_ORIGIN_DISTANCE = 360

interface DialogTrigger {
  el: HTMLElement
  x: number
  y: number
  ts: number
}

let trigger: DialogTrigger | null = null

/**
 * 记录最近一次点击的触发元素
 */
function onClick(e: MouseEvent): void {
  if (e.button !== 0) return

  const target = e.target
  if (!(target instanceof Element)) return

  const el = target.closest(TRIGGER_SELECTOR)
  trigger = el instanceof HTMLElement ? { el, x: e.clientX, y: e.clientY, ts: Date.now() } : null
}

/**
 * 取出并消费最近一次触发源
 */
function consumeTrigger(): DialogTrigger | null {
  const current = trigger
  trigger = null
  if (!current || Date.now() - current.ts > TRIGGER_TTL) {
    return null
  }
  return current
}

/**
 * 解析触发点的视口坐标，无法确定时返回 null
 */
function resolveTriggerPoint(info: DialogTrigger): { x: number; y: number } | null {
  if (info.el.isConnected) {
    const rect = info.el.getBoundingClientRect()
    if (rect.width > 0 && rect.height > 0) {
      return { x: rect.left + rect.width / 2, y: rect.top + rect.height / 2 }
    }
  }

  if (Date.now() - info.ts <= POINT_TTL) {
    return { x: info.x, y: info.y }
  }

  return null
}

/**
 * Transition onEnter：为当前弹窗写入 transform-origin
 * 只声明一个参数，Vue 就不会等待 done 回调
 */
function resolveDialogOrigin(overlay: Element): void {
  const dialog = overlay.querySelector<HTMLElement>('.el-dialog')
  if (!dialog) return

  // 先回到中心原点，确保测量不受上一次动画残留的 origin 影响
  dialog.style.setProperty('--art-origin-x', '50%')
  dialog.style.setProperty('--art-origin-y', '50%')

  const rect = dialog.getBoundingClientRect()
  const width = dialog.offsetWidth
  const height = dialog.offsetHeight
  if (width <= 0 || height <= 0) return

  // 视觉中心在缩放时不变（origin 已重置为中心），用它还原布局左上角
  const centerX = rect.left + rect.width / 2
  const centerY = rect.top + rect.height / 2
  const left = centerX - width / 2
  const top = centerY - height / 2

  const info = consumeTrigger()
  // 触发元素位于本次弹窗内时不作为起点，避免从弹窗内部边缘展开
  const point = info && !overlay.contains(info.el) ? resolveTriggerPoint(info) : null
  if (!point) {
    // 无有效来源：保持居中缩放
    return
  }

  // 超远距离向中心收缩，保留方向感但避免长距离扫屏
  let dx = point.x - centerX
  let dy = point.y - centerY
  const distance = Math.hypot(dx, dy)
  if (distance > MAX_ORIGIN_DISTANCE) {
    const ratio = MAX_ORIGIN_DISTANCE / distance
    dx *= ratio
    dy *= ratio
  }

  dialog.style.setProperty('--art-origin-x', `${centerX + dx - left}px`)
  dialog.style.setProperty('--art-origin-y', `${centerY + dy - top}px`)
}

/**
 * 注册全局弹窗动画起点追踪（应用启动时调用一次）
 */
export function setupDialogOrigin(): void {
  if (typeof window === 'undefined') return
  document.addEventListener('click', onClick, true)
}

/**
 * Element Plus 全局弹窗过渡配置
 * 传入 ElConfigProvider 的 dialog 属性，对所有 el-dialog 生效
 */
export const dialogConfig: DialogConfigContext = {
  transition: {
    name: 'art-dialog',
    duration: { enter: 240, leave: 280 },
    onEnter: resolveDialogOrigin
  }
}
