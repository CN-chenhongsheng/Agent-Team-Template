/**
 * 权限分配弹窗的数据转换与选择计算
 *
 * 将后端菜单树转换为「模块 - 页面 - 操作」三层结构，
 * 并提供选中集合的规范化与提交数据组装。
 *
 * @module views/system/modules/permission-assign-dialog/helper
 */

type MenuListItem = Api.SystemManage.MenuListItem

/** 权限项不可选的原因 */
export type PermissionDisabledReason = 'status' | 'outOfScope'

/** 操作权限（按钮） */
export interface PermissionActionNode {
  id: number
  name: string
  code?: string
  disabled: boolean
  disabledReason?: PermissionDisabledReason
}

/** 页面权限（菜单） */
export interface PermissionPageNode {
  id: number
  name: string
  code?: string
  icon?: string
  pathLabel?: string
  disabled: boolean
  disabledReason?: PermissionDisabledReason
  actions: PermissionActionNode[]
}

/** 模块分组（目录） */
export interface PermissionGroupNode {
  id: number
  name: string
  icon?: string
  pages: PermissionPageNode[]
}

export interface PermissionStats {
  selected: number
  total: number
}

const isAction = (node: MenuListItem): boolean => node.menuType === 'F'

const hasContainerChild = (node: MenuListItem): boolean =>
  (node.children ?? []).some((child) => !isAction(child))

const getDisabledReason = (
  node: MenuListItem,
  disabledIds: Set<number>
): PermissionDisabledReason | undefined => {
  if (node.status === 0) return 'status'
  if (disabledIds.has(node.id)) return 'outOfScope'
  return undefined
}

const toAction = (node: MenuListItem, disabledIds: Set<number>): PermissionActionNode => {
  const disabledReason = getDisabledReason(node, disabledIds)
  return {
    id: node.id,
    name: node.menuName,
    code: node.permission,
    disabled: Boolean(disabledReason),
    disabledReason
  }
}

const toPage = (
  node: MenuListItem,
  pathLabel: string | undefined,
  disabledIds: Set<number>
): PermissionPageNode => {
  const disabledReason = getDisabledReason(node, disabledIds)
  return {
    id: node.id,
    name: node.menuName,
    code: node.permission,
    icon: node.icon,
    pathLabel,
    disabled: Boolean(disabledReason),
    disabledReason,
    actions: (node.children ?? []).filter(isAction).map((child) => toAction(child, disabledIds))
  }
}

const collectPages = (
  node: MenuListItem,
  group: PermissionGroupNode,
  pathLabel: string | undefined,
  disabledIds: Set<number>
): void => {
  if (!hasContainerChild(node)) {
    group.pages.push(toPage(node, pathLabel, disabledIds))
    return
  }

  const nextLabel = pathLabel ? `${pathLabel} / ${node.menuName}` : node.menuName
  ;(node.children ?? [])
    .filter((child) => !isAction(child))
    .forEach((child) => collectPages(child, group, nextLabel, disabledIds))
}

/**
 * 将菜单树转换为模块分组
 *
 * 目录作为分组容器，页面与操作作为真正可分配的权限项。
 */
export function buildPermissionGroups(
  menus: MenuListItem[],
  disabledIds: Set<number> = new Set()
): PermissionGroupNode[] {
  const groups: PermissionGroupNode[] = []

  menus.forEach((menu) => {
    if (isAction(menu)) return

    const group: PermissionGroupNode = {
      id: menu.id,
      name: menu.menuName,
      icon: menu.icon,
      pages: []
    }

    if (hasContainerChild(menu)) {
      ;(menu.children ?? [])
        .filter((child) => !isAction(child))
        .forEach((child) => collectPages(child, group, undefined, disabledIds))
    } else {
      group.pages.push(toPage(menu, undefined, disabledIds))
    }

    if (group.pages.length > 0) {
      groups.push(group)
    }
  })

  return groups
}

/**
 * 构建祖先节点映射，用于提交时补齐目录与页面节点
 */
export function buildAncestorMap(menus: MenuListItem[]): Map<number, number[]> {
  const ancestors = new Map<number, number[]>()

  const walk = (nodes: MenuListItem[], parents: number[]) => {
    nodes.forEach((node) => {
      ancestors.set(node.id, parents)
      if (node.children?.length) {
        walk(node.children, [...parents, node.id])
      }
    })
  }

  walk(menus, [])
  return ancestors
}

/**
 * 将后端返回的已授权菜单 ID 规范化为可分配的选中集合
 *
 * 目录 ID 不参与选中状态；操作被选中时自动补齐所属页面。
 */
export function normalizePermissionSelection(
  ids: number[],
  groups: PermissionGroupNode[]
): Set<number> {
  const pageByAction = new Map<number, number>()
  const assignableIds = new Set<number>()

  groups.forEach((group) => {
    group.pages.forEach((page) => {
      if (!page.disabled) assignableIds.add(page.id)
      page.actions.forEach((action) => {
        pageByAction.set(action.id, page.id)
        if (!action.disabled) assignableIds.add(action.id)
      })
    })
  })

  const selected = new Set<number>()
  ids.forEach((id) => {
    if (!assignableIds.has(id)) return
    selected.add(id)
    const pageId = pageByAction.get(id)
    if (pageId !== undefined) {
      selected.add(pageId)
    }
  })

  return selected
}

/**
 * 组装提交给后端的菜单 ID，自动补齐所有祖先节点
 */
export function buildPermissionPayload(
  selectedIds: Iterable<number>,
  ancestorMap: Map<number, number[]>
): number[] {
  const payload = new Set<number>()

  for (const id of selectedIds) {
    payload.add(id)
    ;(ancestorMap.get(id) ?? []).forEach((ancestorId) => payload.add(ancestorId))
  }

  return Array.from(payload)
}

/**
 * 按关键字切分文本，供搜索高亮使用
 */
export function splitHighlight(
  text: string,
  keyword: string
): Array<{ text: string; match: boolean }> {
  const trimmed = keyword.trim()
  if (!trimmed) {
    return [{ text, match: false }]
  }

  const lowerText = text.toLowerCase()
  const lowerKeyword = trimmed.toLowerCase()
  const parts: Array<{ text: string; match: boolean }> = []
  let cursor = 0

  while (cursor < text.length) {
    const found = lowerText.indexOf(lowerKeyword, cursor)
    if (found === -1) {
      parts.push({ text: text.slice(cursor), match: false })
      break
    }
    if (found > cursor) {
      parts.push({ text: text.slice(cursor, found), match: false })
    }
    parts.push({ text: text.slice(found, found + trimmed.length), match: true })
    cursor = found + trimmed.length
  }

  return parts.length > 0 ? parts : [{ text, match: false }]
}
