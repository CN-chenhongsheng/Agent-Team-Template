/**
 * 菜单处理器
 *
 * 负责菜单数据的获取、过滤和处理
 *
 * @module router/core/MenuProcessor
 * @author 陈鸿昇
 */

import type { AppRouteRecord } from '@/types/router'
import { useUserStore } from '@/store/modules/user'
import { useAppMode } from '@/hooks/core/useAppMode'
import { fetchGetUserMenuTree } from '@/api/system-manage'
import { asyncRoutes } from '../routes/asyncRoutes'
import { RoutesAlias } from '../routesAlias'
import { formatMenuTitle } from '@/utils'

export class MenuProcessor {
  /**
   * 获取菜单数据
   */
  async getMenuList(): Promise<AppRouteRecord[]> {
    const { isFrontendMode } = useAppMode()

    let menuList: AppRouteRecord[]
    if (isFrontendMode.value) {
      menuList = await this.processFrontendMenu()
    } else {
      menuList = await this.processBackendMenu()
    }

    // 在规范化路径之前，验证原始路径配置
    this.validateMenuPaths(menuList)

    // 规范化路径（将相对路径转换为完整路径）
    return this.normalizeMenuPaths(menuList)
  }

  /**
   * 处理前端控制模式的菜单
   */
  private async processFrontendMenu(): Promise<AppRouteRecord[]> {
    const userStore = useUserStore()
    const roles = userStore.info?.roles

    let menuList = [...asyncRoutes]

    // 根据角色过滤菜单
    if (roles && roles.length > 0) {
      menuList = this.filterMenuByRoles(menuList, roles)
    }

    return this.filterEmptyMenus(menuList)
  }

  /**
   * 处理后端控制模式的菜单
   */
  private async processBackendMenu(): Promise<AppRouteRecord[]> {
    // 使用用户菜单接口，根据当前登录用户的角色权限返回菜单
    const list = await fetchGetUserMenuTree()
    // 过滤掉按钮类型（menuType='F'），按钮不参与路由
    // 过滤掉 visible=0 的菜单（不显示在侧边栏，但路由仍然注册）
    const filteredList = this.filterMenuForRoutes(list)
    const routes = this.convertMenuToRoutes(filteredList)
    return this.filterEmptyMenus(routes)
  }

  /**
   * 过滤菜单用于路由注册
   * - 过滤掉按钮类型（menuType='F'），按钮不参与路由
   * - 保留 visible=0 的菜单用于路由注册，但在 meta.hidden 中标记
   */
  private filterMenuForRoutes(menuList: any[]): any[] {
    return menuList
      .filter((menu) => menu.menuType !== 'F') // 过滤掉按钮
      .map((menu) => ({
        ...menu,
        children: menu.children ? this.filterMenuForRoutes(menu.children) : []
      }))
  }

  /**
   * 将后端菜单数据转换为前端路由格式
   */
  private convertMenuToRoutes(menuList: any[]): AppRouteRecord[] {
    return menuList.map((menu) => {
      const route: AppRouteRecord = {
        name: this.generateRouteName(menu.menuName, menu.id),
        path: menu.path,
        component: menu.component,
        meta: {
          title: menu.menuName,
          icon: menu.icon,
          isHide: menu.visible === 0,
          keepAlive: menu.keepAlive === 1
        }
      }

      // 递归处理子菜单
      if (menu.children && menu.children.length > 0) {
        route.children = this.convertMenuToRoutes(menu.children)
      }

      return route
    })
  }

  /**
   * 生成路由名称
   */
  private generateRouteName(menuName: string, id: number): string {
    const nameMap: Record<string, string> = {
      工作台: 'Dashboard',
      控制台: 'Console',
      学校管理: 'School',
      校园管理: 'Campus',
      校区管理: 'Campus',
      学年管理: 'AcademicYear',
      组织管理: 'Organization',
      院系管理: 'OrganizationDepartment',
      专业管理: 'OrganizationMajor',
      班级管理: 'OrganizationClass',
      宿舍管理: 'Dormitory',
      楼层管理: 'Floor',
      房间管理: 'Room',
      床位管理: 'Bed',
      住宿管理: 'Accommodation',
      人员管理: 'AccommodationStudent',
      入住管理: 'AccommodationCheckIn',
      调宿管理: 'AccommodationTransfer',
      退宿管理: 'AccommodationCheckOut',
      留宿管理: 'AccommodationStay',
      系统管理: 'System',
      用户管理: 'User',
      角色管理: 'Role',
      菜单管理: 'Menus',
      字典管理: 'Dict',
      操作日志: 'OperLog',
      个人中心: 'UserCenter',
      异常页面: 'Exception',
      '403': 'Exception403',
      '404': 'Exception404',
      '500': 'Exception500',
      结果页面: 'Result',
      成功页: 'ResultSuccess',
      失败页: 'ResultFail',
      审批管理: 'Approval',
      流程配置: 'ApprovalFlowConfig',
      待办审批: 'ApprovalPending',
      审批记录: 'ApprovalHistory'
    }
    return nameMap[menuName] || `Menu${id}`
  }

  /**
   * 根据角色过滤菜单
   */
  private filterMenuByRoles(menu: AppRouteRecord[], roles: string[]): AppRouteRecord[] {
    return menu.reduce((acc: AppRouteRecord[], item) => {
      const itemRoles = item.meta?.roles
      const hasPermission = !itemRoles || itemRoles.some((role) => roles?.includes(role))

      if (hasPermission) {
        const filteredItem = { ...item }
        if (filteredItem.children?.length) {
          filteredItem.children = this.filterMenuByRoles(filteredItem.children, roles)
        }
        acc.push(filteredItem)
      }

      return acc
    }, [])
  }

  /**
   * 递归过滤空菜单项
   */
  private filterEmptyMenus(menuList: AppRouteRecord[]): AppRouteRecord[] {
    return menuList
      .map((item) => {
        // 如果有子菜单，先递归过滤子菜单
        if (item.children && item.children.length > 0) {
          const filteredChildren = this.filterEmptyMenus(item.children)
          return {
            ...item,
            children: filteredChildren
          }
        }
        return item
      })
      .filter((item) => {
        // 如果定义了 children 属性（即使是空数组），说明这是一个目录菜单，应该保留
        if ('children' in item) {
          return true
        }

        // 如果有外链或 iframe，保留
        if (item.meta?.isIframe === true || item.meta?.link) {
          return true
        }

        // 如果有有效的 component，保留
        if (item.component && item.component !== '' && item.component !== RoutesAlias.Layout) {
          return true
        }

        // 其他情况过滤掉
        return false
      })
  }

  /**
   * 验证菜单列表是否有效
   */
  validateMenuList(menuList: AppRouteRecord[]): boolean {
    return Array.isArray(menuList) && menuList.length > 0
  }

  /**
   * 规范化菜单路径
   * 将相对路径转换为完整路径，确保菜单跳转正确
   */
  private normalizeMenuPaths(menuList: AppRouteRecord[], parentPath = ''): AppRouteRecord[] {
    return menuList.map((item) => {
      // 构建完整路径
      const fullPath = this.buildFullPath(item.path || '', parentPath)

      // 递归处理子菜单
      const children = item.children?.length
        ? this.normalizeMenuPaths(item.children, fullPath)
        : item.children

      return {
        ...item,
        path: fullPath,
        children
      }
    })
  }

  /**
   * 验证菜单路径配置
   * 检测非一级菜单是否错误使用了 / 开头的路径
   */
  /**
   * 验证菜单路径配置
   * 检测非一级菜单是否错误使用了 / 开头的路径
   */
  private validateMenuPaths(menuList: AppRouteRecord[], level = 1): void {
    menuList.forEach((route) => {
      if (!route.children?.length) return

      const parentName = String(route.name || route.path || '未知路由')

      route.children.forEach((child) => {
        const childPath = child.path || ''

        // 跳过合法的绝对路径：外部链接和 iframe 路由
        if (this.isValidAbsolutePath(childPath)) return

        // 检测非法的绝对路径
        if (childPath.startsWith('/')) {
          this.logPathError(child, childPath, parentName, level)
        }
      })

      // 递归检查更深层级的子路由
      this.validateMenuPaths(route.children, level + 1)
    })
  }

  /**
   * 判断是否为合法的绝对路径
   */
  private isValidAbsolutePath(path: string): boolean {
    return (
      path.startsWith('http://') ||
      path.startsWith('https://') ||
      path.startsWith('/outside/iframe/')
    )
  }

  /**
   * 输出路径配置错误日志
   */
  private logPathError(
    route: AppRouteRecord,
    path: string,
    parentName: string,
    level: number
  ): void {
    const routeName = String(route.name || path || '未知路由')
    const menuTitle = route.meta?.title || routeName
    const suggestedPath = path.split('/').pop() || path.slice(1)

    console.error(
      `[路由配置错误] 菜单 "${formatMenuTitle(menuTitle)}" (name: ${routeName}, path: ${path}) 配置错误\n` +
        `  位置: ${parentName} > ${routeName}\n` +
        `  问题: ${level + 1}级菜单的 path 不能以 / 开头\n` +
        `  当前配置: path: '${path}'\n` +
        `  应该改为: path: '${suggestedPath}'`
    )
  }

  /**
   * 构建完整路径
   */
  private buildFullPath(path: string, parentPath: string): string {
    if (!path) return ''

    // 外部链接直接返回
    if (path.startsWith('http://') || path.startsWith('https://')) {
      return path
    }

    // 如果已经是绝对路径，直接返回
    if (path.startsWith('/')) {
      return path
    }

    // 拼接父路径和当前路径
    if (parentPath) {
      // 移除父路径末尾的斜杠，移除子路径开头的斜杠，然后拼接
      const cleanParent = parentPath.replace(/\/$/, '')
      const cleanChild = path.replace(/^\//, '')
      return `${cleanParent}/${cleanChild}`
    }

    // 没有父路径，添加前导斜杠
    return `/${path}`
  }
}
