/**
 * 全局弹窗动画起点
 *
 * 监听全局指针按下，把"点击点 → 视口中心"的偏移写入根元素 CSS 变量
 * （--art-dx / --art-dy），供全局 el-dialog 的入场/出场动画使用，
 * 实现"从按钮位置放大居中、关闭缩回按钮"的效果。
 *
 * 关键点：当弹窗已打开时（点击发生在 .el-overlay 内，如取消/关闭按钮、
 * 表单、遮罩）不更新起点，使整个弹窗生命周期内起点保持为"打开时的按钮"，
 * 因此关闭动画会缩回原触发按钮。
 *
 * @module utils/sys/dialog-origin
 * @author 陈鸿昇
 * @date 2026-06-17
 */

/**
 * 注册全局弹窗动画起点追踪（应在应用启动时调用一次）
 */
export function setupDialogOrigin(): void {
  if (typeof window === 'undefined') return

  window.addEventListener(
    'pointerdown',
    (e: PointerEvent) => {
      const target = e.target as HTMLElement | null

      // 点击发生在任意弹窗遮罩内时，不更新起点（保持打开时的按钮位置）
      if (target?.closest('.el-overlay')) return

      const dx = e.clientX - window.innerWidth / 2
      const dy = e.clientY - window.innerHeight / 2

      const root = document.documentElement
      root.style.setProperty('--art-dx', `${dx}px`)
      root.style.setProperty('--art-dy', `${dy}px`)
    },
    { capture: true, passive: true }
  )
}
