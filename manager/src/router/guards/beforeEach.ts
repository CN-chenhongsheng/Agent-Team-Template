/**
 * 路由全局前置守卫模块
 *
 * 提供完整的路由导航守卫功能
 *
 * ## 主要功能
 *
 * - 登录状态验证和重定向
 * - 动态路由注册和权限控制
 * - 菜单数据获取和处理（前端/后端模式）
 * - 用户信息获取和缓存
 * - 页面标题设置
 * - 工作标签页管理
 * - 进度条和加载动画控制
 * - 静态路由识别和处理
 * - 错误处理和异常跳转
 *
 * ## 使用场景
 *
 * - 路由跳转前的权限验证
 * - 动态菜单加载和路由注册
 * - 用户登录状态管理
 * - 页面访问控制
 * - 路由级别的加载状态管理
 *
 * ## 工作流程
 *
 * 1. 检查登录状态，未登录跳转到登录页
 * 2. 首次访问时获取用户信息和菜单数据
 * 3. 根据权限动态注册路由
 * 4. 设置页面标题和工作标签页
 * 5. 处理根路径重定向到首页
 * 6. 未匹配路由跳转到 404 页面
 *
 * @module router/guards/beforeEach
 * @author 陈鸿昇
 */
import type { Router, RouteLocationNormalized, NavigationGuardNext } from 'vue-router'
import { nextTick } from 'vue'
import NProgress from 'nprogress'
import { useSettingStore } from '@/store/modules/setting'
import { useUserStore } from '@/store/modules/user'
import { useMenuStore } from '@/store/modules/menu'
import { setWorktab } from '@/utils/navigation'
import { setPageTitle } from '@/utils/router'
import { RoutesAlias } from '../routesAlias'
import { staticRoutes } from '../routes/staticRoutes'
import { loadingService } from '@/utils/ui'
import { useCommon } from '@/hooks/core/useCommon'
import { useWorktabStore } from '@/store/modules/worktab'
import { fetchGetUserInfo, fetchRefreshToken } from '@/api/auth'
import { ApiStatus } from '@/utils/http/status'
import { isHttpError } from '@/utils/http/error'
import { RouteRegistry, MenuProcessor, IframeRouteManager, RoutePermissionValidator } from '../core'
import type { AppRouteRecord } from '@/types/router'

// 路由注册器实例
let routeRegistry: RouteRegistry | null = null

// 菜单处理器实例
const menuProcessor = new MenuProcessor()

// 跟踪是否需要关闭 loading
let pendingLoading = false

// 路由初始化失败标记，防止死循环
// 一旦设置为 true，只有刷新页面或重新登录才能重置
let routeInitFailed = false

// 路由初始化进行中标记，防止并发请求
let routeInitInProgress = false

/**
 * 获取 pendingLoading 状态
 */
export function getPendingLoading(): boolean {
  return pendingLoading
}

/**
 * 重置 pendingLoading 状态
 */
export function resetPendingLoading(): void {
  pendingLoading = false
}

/**
 * 获取路由初始化失败状态
 */
export function getRouteInitFailed(): boolean {
  return routeInitFailed
}

/**
 * 重置路由初始化状态（用于重新登录场景）
 */
export function resetRouteInitState(): void {
  routeInitFailed = false
  routeInitInProgress = false
}

/**
 * 设置路由全局前置守卫
 */
export function setupBeforeEachGuard(router: Router): void {
  // 初始化路由注册器
  routeRegistry = new RouteRegistry(router)

  router.beforeEach(
    async (
      to: RouteLocationNormalized,
      from: RouteLocationNormalized,
      next: NavigationGuardNext
    ) => {
      try {
        await handleRouteGuard(to, from, next, router)
      } catch (error) {
        console.error('[RouteGuard] 路由守卫处理失败:', error)
        closeLoading()
        next({ name: 'Exception500' })
      }
    }
  )
}

/**
 * 关闭 loading 效果
 */
function closeLoading(): void {
  if (pendingLoading) {
    nextTick(() => {
      loadingService.hideLoading()
      pendingLoading = false
    })
  }
}

/**
 * 处理路由守卫逻辑
 */
async function handleRouteGuard(
  to: RouteLocationNormalized,
  from: RouteLocationNormalized,
  next: NavigationGuardNext,
  router: Router
): Promise<void> {
  const settingStore = useSettingStore()
  const userStore = useUserStore()

  // 启动进度条
  if (settingStore.showNprogress) {
    NProgress.start()
  }

  // 1. 检查登录状态
  const loginStatusResult = await handleLoginStatus(to, userStore, next)
  if (!loginStatusResult) {
    return
  }

  // 2. 检查路由初始化是否已失败（防止死循环）
  if (routeInitFailed) {
    // 已经失败过，直接放行到错误页面，不再重试
    if (to.matched.length > 0) {
      next()
    } else {
      // 未匹配到路由，跳转到 500 页面
      next({ name: 'Exception500', replace: true })
    }
    return
  }

  // 3. 处理动态路由注册
  if (!routeRegistry?.isRegistered() && userStore.isLogin) {
    // 防止并发请求（快速连续导航场景）
    if (routeInitInProgress) {
      // 正在初始化中，等待完成后重新导航
      next(false)
      return
    }
    await handleDynamicRoutes(to, next, router)
    return
  }

  // 4. 处理根路径重定向
  if (handleRootPathRedirect(to, next)) {
    return
  }

  // 5. 处理已匹配的路由
  if (to.matched.length > 0) {
    setWorktab(to)
    setPageTitle(to)
    next()
    return
  }

  // 6. 未匹配到路由，跳转到 404
  next({ name: 'Exception404' })
}

/**
 * 处理登录状态
 * @returns true 表示可以继续，false 表示已处理跳转
 */
async function handleLoginStatus(
  to: RouteLocationNormalized,
  userStore: ReturnType<typeof useUserStore>,
  next: NavigationGuardNext
): Promise<boolean> {
  // 已登录或访问登录页或静态路由，直接放行
  if (userStore.isLogin || to.path === RoutesAlias.Login || isStaticRoute(to.path)) {
    return true
  }

  // 如果用户信息存在但 Access Token 丢失（页面刷新场景），尝试通过 Refresh Token 恢复
  if (hasCachedUserInfo(userStore) && !userStore.accessToken) {
    try {
      const newAccessToken = await fetchRefreshToken()
      userStore.setToken(newAccessToken)
      userStore.setLoginStatus(true)
      return true
    } catch (error) {
      // Refresh Token 无效或过期，清除用户信息并跳转登录页
      console.warn('[RouteGuard] Refresh Token 无效，跳转登录页:', error)
    }
  }

  // 未登录且访问需要权限的页面，跳转到登录页并携带 redirect 参数
  userStore.logOut()
  next({
    name: 'Login',
    query: { redirect: to.fullPath }
  })
  return false
}

/**
 * 检查路由是否为静态路由
 */
function isStaticRoute(path: string): boolean {
  const checkRoute = (routes: any[], targetPath: string): boolean => {
    return routes.some((route) => {
      // 处理动态路由参数匹配
      const routePath = route.path
      const pattern = routePath.replace(/:[^/]+/g, '[^/]+').replace(/\*/g, '.*')
      const regex = new RegExp(`^${pattern}$`)

      if (regex.test(targetPath)) {
        return true
      }
      if (route.children && route.children.length > 0) {
        return checkRoute(route.children, targetPath)
      }
      return false
    })
  }

  return checkRoute(staticRoutes, path)
}

/**
 * 检查是否有缓存的用户信息
 */
function hasCachedUserInfo(userStore: ReturnType<typeof useUserStore>): boolean {
  const info = userStore.info
  return !!(info && info.userId && Object.keys(info).length > 0)
}

/**
 * 检查是否有缓存的菜单数据
 */
function hasCachedMenuData(menuStore: ReturnType<typeof useMenuStore>): boolean {
  return !!(menuStore.menuList && menuStore.menuList.length > 0)
}

/**
 * 处理动态路由注册
 */
async function handleDynamicRoutes(
  to: RouteLocationNormalized,
  next: NavigationGuardNext,
  router: Router
): Promise<void> {
  // 标记初始化进行中
  routeInitInProgress = true

  const userStore = useUserStore()
  const menuStore = useMenuStore()

  // 检查是否有缓存数据
  const hasUserInfoCache = hasCachedUserInfo(userStore)
  const hasMenuDataCache = hasCachedMenuData(menuStore)

  // 如果都有缓存，直接使用缓存，无需显示 loading 和调用 API
  if (hasUserInfoCache && hasMenuDataCache) {
    try {
      // 使用缓存数据
      const menuList = menuStore.menuList

      // 验证菜单数据有效性
      if (!menuProcessor.validateMenuList(menuList)) {
        // 缓存数据无效，清空并重新获取
        menuStore.setMenuList([])
        throw new Error('缓存菜单数据无效，需要重新获取')
      }

      // 1. 注册动态路由（使用缓存数据）
      routeRegistry?.register(menuList)
      menuStore.addRemoveRouteFns(routeRegistry?.getRemoveRouteFns() || [])

      // 2. 保存 iframe 路由
      IframeRouteManager.getInstance().save()

      // 3. 验证工作标签页
      useWorktabStore().validateWorktabs(router)

      // 4. 验证目标路径权限
      const { homePath } = useCommon()
      const { path: validatedPath, hasPermission } = RoutePermissionValidator.validatePath(
        to.path,
        menuList,
        homePath.value || '/'
      )

      // 初始化成功，重置进行中标记
      routeInitInProgress = false

      // 5. 重新导航到目标路由
      if (!hasPermission) {
        console.warn(`[RouteGuard] 用户无权限访问路径: ${to.path}，已跳转到首页`)
        next({
          path: validatedPath,
          replace: true
        })
      } else {
        next({
          path: to.path,
          query: to.query,
          hash: to.hash,
          replace: true
        })
      }

      // 后台静默刷新数据（不阻塞导航）
      refreshDataInBackground(userStore, menuStore).catch((error) => {
        console.warn('[RouteGuard] 后台刷新数据失败，使用缓存数据继续:', error)
      })

      return
    } catch (error) {
      // 缓存数据有问题，继续走正常流程重新获取
      console.warn('[RouteGuard] 使用缓存数据失败，重新获取:', error)
      menuStore.setMenuList([])
    }
  }

  // 没有缓存或缓存无效，显示 loading 并调用 API
  pendingLoading = true
  loadingService.showLoading()

  try {
    // 1. 并行获取用户信息和菜单数据（两者互不依赖，并行可将等待时间减半）
    const needUserInfo = !hasUserInfoCache
    const needMenuData = !hasMenuDataCache

    const [, fetchedMenuList] = await Promise.all([
      needUserInfo ? fetchUserInfo() : Promise.resolve(),
      needMenuData ? menuProcessor.getMenuList() : Promise.resolve(undefined)
    ])

    // 2. 处理菜单数据
    let menuList: AppRouteRecord[]
    if (needMenuData && fetchedMenuList) {
      // 验证菜单数据
      if (!menuProcessor.validateMenuList(fetchedMenuList)) {
        throw new Error('获取菜单列表失败，请重新登录')
      }

      // 保存菜单数据到 store（会自动持久化）
      menuStore.setMenuList(fetchedMenuList)
      menuList = fetchedMenuList
    } else {
      // 使用缓存数据
      menuList = menuStore.menuList
    }

    // 5. 注册动态路由
    routeRegistry?.register(menuList)
    menuStore.addRemoveRouteFns(routeRegistry?.getRemoveRouteFns() || [])

    // 6. 保存 iframe 路由
    IframeRouteManager.getInstance().save()

    // 7. 验证工作标签页
    useWorktabStore().validateWorktabs(router)

    // 8. 验证目标路径权限
    const { homePath } = useCommon()
    const { path: validatedPath, hasPermission } = RoutePermissionValidator.validatePath(
      to.path,
      menuList,
      homePath.value || '/'
    )

    // 初始化成功，重置进行中标记
    routeInitInProgress = false

    // 关闭 loading
    closeLoading()

    // 9. 重新导航到目标路由
    if (!hasPermission) {
      // 无权限访问，跳转到首页
      console.warn(`[RouteGuard] 用户无权限访问路径: ${to.path}，已跳转到首页`)

      // 直接跳转到首页
      next({
        path: validatedPath,
        replace: true
      })
    } else {
      // 有权限，正常导航
      next({
        path: to.path,
        query: to.query,
        hash: to.hash,
        replace: true
      })
    }
  } catch (error) {
    console.error('[RouteGuard] 动态路由注册失败:', error)

    // 关闭 loading
    closeLoading()

    // 401 错误：axios 拦截器已处理退出登录，取消当前导航
    if (isUnauthorizedError(error)) {
      // 重置状态，允许重新登录后再次初始化
      routeInitInProgress = false
      // 清空可能已失效的缓存
      userStore.setUserInfo({} as Api.Auth.UserInfo)
      menuStore.setMenuList([])
      next(false)
      return
    }

    // 标记初始化失败，防止死循环
    routeInitFailed = true
    routeInitInProgress = false

    // 输出详细错误信息，便于排查
    if (isHttpError(error)) {
      console.error(`[RouteGuard] 错误码: ${error.code}, 消息: ${error.message}`)
    }

    // 跳转到 500 页面，使用 replace 避免产生历史记录
    next({ name: 'Exception500', replace: true })
  }
}

/**
 * 后台静默刷新数据（不阻塞导航）
 */
async function refreshDataInBackground(
  userStore: ReturnType<typeof useUserStore>,
  menuStore: ReturnType<typeof useMenuStore>
): Promise<void> {
  try {
    // 并行刷新用户信息和菜单数据
    const [, menuList] = await Promise.all([fetchUserInfo(), menuProcessor.getMenuList()])

    if (menuProcessor.validateMenuList(menuList)) {
      menuStore.setMenuList(menuList)
    }
  } catch (error) {
    // 刷新失败不影响当前使用，只记录日志
    console.warn('[RouteGuard] 后台刷新数据失败:', error)
  }
}

/**
 * 获取用户信息
 */
async function fetchUserInfo(): Promise<void> {
  const userStore = useUserStore()
  const data = await fetchGetUserInfo()
  userStore.setUserInfo(data)
  // 检查并清理工作台标签页（如果是不同用户登录）
  userStore.checkAndClearWorktabs()
}

/**
 * 重置路由相关状态
 */
export function resetRouterState(delay: number): void {
  setTimeout(() => {
    routeRegistry?.unregister()
    IframeRouteManager.getInstance().clear()

    const menuStore = useMenuStore()
    menuStore.removeAllDynamicRoutes()
    menuStore.setMenuList([])

    // 重置路由初始化状态，允许重新登录后再次初始化
    resetRouteInitState()
  }, delay)
}

/**
 * 处理根路径重定向到首页
 * @returns true 表示已处理跳转，false 表示无需跳转
 */
function handleRootPathRedirect(to: RouteLocationNormalized, next: NavigationGuardNext): boolean {
  if (to.path !== '/') {
    return false
  }

  const { homePath } = useCommon()
  if (homePath.value && homePath.value !== '/') {
    next({ path: homePath.value, replace: true })
    return true
  }

  return false
}

/**
 * 判断是否为未授权错误（401）
 */
function isUnauthorizedError(error: unknown): boolean {
  return isHttpError(error) && error.code === ApiStatus.unauthorized
}
