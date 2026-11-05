/**
 * 表格操作按钮工具函数
 *
 * 提供操作列按钮的自动渲染和数量管理功能
 *
 * @module utils/table/actionButtons
 * @author 陈鸿昇
 */

import { h, VNode } from 'vue'
import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
import ArtButtonMore from '@/components/core/forms/art-button-more/index.vue'
import type { ButtonMoreItem } from '@/components/core/forms/art-button-more/index.vue'
import type { ActionButtonConfig } from '@/types/component'
import { hasPermission } from '@/directives/core/permission'
import { $t } from '@/locales'

/**
 * 按钮类型对应的文案 key
 */
const BUTTON_LABEL_KEYS: Record<string, string> = {
  view: 'utils.actionButtons.view',
  add: 'utils.actionButtons.add',
  edit: 'utils.actionButtons.edit',
  delete: 'utils.actionButtons.delete',
  detail: 'utils.actionButtons.detail',
  download: 'utils.actionButtons.download',
  upload: 'utils.actionButtons.upload',
  assign: 'utils.actionButtons.assign',
  reset: 'utils.actionButtons.reset',
  share: 'utils.actionButtons.share',
  link: 'utils.actionButtons.link',
  notify: 'utils.actionButtons.notify',
  copy: 'utils.actionButtons.copy',
  play: 'utils.actionButtons.play',
  pause: 'utils.actionButtons.pause',
  cancel: 'utils.actionButtons.cancel',
  refresh: 'utils.actionButtons.refresh',
  export: 'utils.actionButtons.export',
  import: 'utils.actionButtons.import',
  print: 'utils.actionButtons.print'
}

/**
 * 按钮类型对应的图标
 */
export const BUTTON_ICONS: Record<string, string> = {
  view: 'ri:eye-line',
  add: 'ri:add-line',
  edit: 'ri:edit-line',
  delete: 'ri:delete-bin-line',
  detail: 'ri:file-list-line',
  download: 'ri:download-line',
  upload: 'ri:upload-line',
  assign: 'ri:user-settings-line',
  reset: 'ri:refresh-line',
  share: 'ri:share-line',
  link: 'ri:link',
  notify: 'ri:notification-line',
  copy: 'ri:file-copy-2-line',
  play: 'ri:play-circle-line',
  pause: 'ri:pause-circle-line',
  cancel: 'ri:close-circle-line',
  refresh: 'ri:refresh-line',
  export: 'ri:download-2-line',
  import: 'ri:upload-2-line',
  print: 'ri:printer-line'
}

/**
 * 获取按钮的标签文本
 */
function getButtonLabel(type: string): string {
  const key = BUTTON_LABEL_KEYS[type]
  return key ? $t(key) : type
}

/**
 * 获取按钮的图标
 */
function getButtonIcon(type: string): string {
  return BUTTON_ICONS[type] || 'ri:more-line'
}

/**
 * 检测是否为按钮配置数组
 */
export function isActionButtonConfig(value: any): value is ActionButtonConfig[] {
  return (
    Array.isArray(value) &&
    value.length > 0 &&
    value.every(
      (item) =>
        item &&
        typeof item === 'object' &&
        'type' in item &&
        'onClick' in item &&
        typeof item.onClick === 'function'
    )
  )
}

/**
 * 渲染操作按钮
 *
 * @param buttons 按钮配置数组
 * @param maxVisible 最大直接显示的按钮数量，默认为 3
 * @returns 渲染的 VNode
 */
export function renderActionButtons(buttons: ActionButtonConfig[], maxVisible: number = 3): VNode {
  // 过滤有权限的按钮
  const visibleButtons = buttons.filter((btn) => !btn.auth || hasPermission(btn.auth))

  // 如果没有按钮，返回空
  if (visibleButtons.length === 0) {
    return h('div', { class: 'flex gap-1' })
  }

  // 如果按钮数量不超过最大显示数量，直接渲染所有按钮
  if (visibleButtons.length <= maxVisible) {
    return h(
      'div',
      { class: 'flex gap-1' },
      visibleButtons.map((btn) =>
        h(ArtButtonTable, {
          type: btn.type,
          disabled: btn.disabled,
          label: btn.label,
          onClick: btn.onClick
        })
      )
    )
  }

  // 按钮数量超过最大显示数量，前 (maxVisible-1) 个按钮 + 更多按钮
  const directButtons = visibleButtons.slice(0, maxVisible - 1)
  const moreButtons = visibleButtons.slice(maxVisible - 1)

  // 构建"更多"菜单项 - 使用索引作为 key，避免相同 type 的按钮冲突
  const moreItems: ButtonMoreItem[] = moreButtons.map((btn, index) => ({
    key: `more-${index}`, // 使用索引作为唯一标识
    label: btn.label || getButtonLabel(btn.type),
    icon: btn.icon || getButtonIcon(btn.type),
    color: btn.danger ? 'var(--el-color-danger)' : undefined,
    disabled: btn.disabled
  }))

  return h('div', { class: 'flex gap-1' }, [
    // 直接显示的按钮
    ...directButtons.map((btn) =>
      h(ArtButtonTable, {
        type: btn.type,
        disabled: btn.disabled,
        label: btn.label,
        onClick: btn.onClick
      })
    ),
    // "更多"按钮
    h(ArtButtonMore, {
      list: moreItems,
      onClick: (item: ButtonMoreItem) => {
        // 通过索引找到对应的按钮配置并执行 onClick
        // item.key 格式为 "more-{index}"
        const indexMatch = item.key.toString().match(/^more-(\d+)$/)
        if (indexMatch) {
          const index = parseInt(indexMatch[1], 10)
          const button = moreButtons[index]
          if (button && button.onClick) {
            button.onClick()
          }
        }
      }
    })
  ])
}
