/**
 * API 系统管理 - 菜单管理类型定义
 *
 * @module types/api/system/menu
 * @author 陈鸿昇
 */

declare namespace Api {
  namespace SystemManage {
    /** ==================== 菜单管理 ==================== */
    /** 菜单查询参数 */
    interface MenuSearchParams {
      menuName?: string
      menuType?: string
      status?: number
    }

    /** 菜单保存参数 */
    interface MenuSaveParams {
      id?: number
      parentId: number
      menuName: string
      menuType: string
      path?: string
      component?: string
      permission?: string
      icon?: string
      sort?: number
      visible?: number
      status?: number
      keepAlive?: number
    }

    /** 菜单列表项 */
    interface MenuListItem {
      id: number
      parentId: number
      menuName: string
      menuType: string
      menuTypeText?: string
      path?: string
      component?: string
      permission?: string
      icon?: string
      sort?: number
      visible?: number
      visibleText?: string
      status: number
      statusText?: string
      keepAlive?: number
      children?: MenuListItem[]
      createTime?: string
      updateTime?: string
    }

    /** 菜单树列表 */
    type MenuTreeList = MenuListItem[]
  }
}
