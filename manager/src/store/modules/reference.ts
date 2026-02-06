/**
 * 参考数据状态管理模块
 *
 * 提供参考数据（下拉选项、树形数据等）的全局缓存和状态管理
 *
 * ## 主要功能
 *
 * - 数据缓存（按接口类型和参数）
 * - Promise 缓存（防止重复请求）
 * - 响应式数据共享
 * - 数据刷新机制
 * - 缓存清理机制
 * - 缓存过期时间管理
 * - **SWR 策略（Stale-While-Revalidate）**：先返回缓存数据，后台自动刷新
 *
 * ## 缓存策略（SWR）
 *
 * 采用 Stale-While-Revalidate 策略：
 * 1. **首次调用**：发起 API 请求，等待返回后存入缓存
 * 2. **后续调用**：
 *    - 如果缓存存在且未过期：立即返回缓存数据，同时在后台静默刷新
 *    - 如果缓存已过期：立即返回旧数据，同时在后台刷新
 *    - 后台刷新完成后自动更新缓存，下次调用使用新数据
 *
 * ## 使用场景
 *
 * - 校区、楼层、房间等下拉选项的统一管理
 * - 角色、用户、专业等参考数据的缓存
 * - 避免多个组件重复加载同一数据
 * - 在数据变更后刷新缓存
 * - 提升用户体验（快速响应 + 数据自动更新）
 *
 * @module store/modules/reference
 * @author 陈鸿昇
 */
import { defineStore } from 'pinia'
import { reactive } from 'vue'
import { fetchGetCampusTree, fetchGetDepartmentTree, fetchGetMajorPage } from '@/api/school-manage'
import { fetchGetFloorPage, fetchGetRoomPage } from '@/api/dormitory-manage'
import {
  fetchGetAllRoles,
  fetchGetUsersByRoleCodes,
  fetchGetMenuTreeSelect
} from '@/api/system-manage'

/** 缓存过期时间（5分钟） */
const CACHE_EXPIRE_TIME = 5 * 60 * 1000

/** 缓存项接口 */
interface ReferenceCache<T> {
  /** 缓存数据 */
  data: T
  /** 是否正在加载 */
  loading: boolean
  /** 是否正在后台刷新 */
  revalidating: boolean
  /** 加载 Promise（用于防止重复请求） */
  loadPromise: Promise<T> | null
  /** 加载时间戳 */
  timestamp: number
}

/** 创建空缓存项 */
function createEmptyCache<T>(defaultValue: T): ReferenceCache<T> {
  return {
    data: defaultValue,
    loading: false,
    revalidating: false,
    loadPromise: null,
    timestamp: 0
  }
}

/** 检查缓存是否过期 */
function isCacheExpired(timestamp: number): boolean {
  return Date.now() - timestamp > CACHE_EXPIRE_TIME
}

export const useReferenceStore = defineStore('referenceStore', () => {
  // ==================== 缓存存储 ====================

  /** 校区树缓存 */
  const campusTreeCache = reactive<ReferenceCache<Api.SystemManage.CampusTreeList>>(
    createEmptyCache([])
  )

  /** 楼层列表缓存（按校区代码） */
  const floorListCache = reactive<Map<string, ReferenceCache<Api.SystemManage.FloorListItem[]>>>(
    new Map()
  )

  /** 房间列表缓存（按楼层ID） */
  const roomListCache = reactive<Map<number, ReferenceCache<Api.SystemManage.RoomListItem[]>>>(
    new Map()
  )

  /** 角色列表缓存 */
  const allRolesCache = reactive<ReferenceCache<Api.SystemManage.RoleListItem[]>>(
    createEmptyCache([])
  )

  /** 用户列表缓存（按角色代码） */
  const usersByRoleCodesCache = reactive<
    Map<string, ReferenceCache<Api.SystemManage.UserSimpleItem[]>>
  >(new Map())

  /** 专业列表缓存 */
  const majorListCache = reactive<ReferenceCache<Api.SystemManage.MajorListItem[]>>(
    createEmptyCache([])
  )

  /** 院系树缓存 */
  const departmentTreeCache = reactive<ReferenceCache<Api.SystemManage.DepartmentTreeList>>(
    createEmptyCache([])
  )

  /** 菜单树缓存 */
  const menuTreeSelectCache = reactive<ReferenceCache<Api.SystemManage.MenuTreeList>>(
    createEmptyCache([])
  )

  // ==================== 校区相关 ====================

  /**
   * 加载校区树（SWR 策略：先返回缓存，后台刷新）
   * @param force 是否强制重新加载（忽略缓存）
   * @returns 校区树列表
   */
  const loadCampusTree = async (force = false): Promise<Api.SystemManage.CampusTreeList> => {
    // 如果有缓存且不强制刷新，立即返回缓存数据
    if (!force && campusTreeCache.data.length > 0) {
      // 如果缓存未过期，直接返回
      if (!isCacheExpired(campusTreeCache.timestamp)) {
        // 后台静默刷新（如果不在刷新中）
        if (!campusTreeCache.revalidating && !campusTreeCache.loading) {
          revalidateCampusTree()
        }
        return campusTreeCache.data
      }
      // 缓存已过期，但先返回旧数据，后台刷新
      if (!campusTreeCache.revalidating && !campusTreeCache.loading) {
        revalidateCampusTree()
      }
      return campusTreeCache.data
    }

    // 如果没有缓存或强制刷新，等待 API 返回
    // 如果正在加载，返回同一个 Promise（防止重复请求）
    if (campusTreeCache.loading && campusTreeCache.loadPromise) {
      return campusTreeCache.loadPromise
    }

    campusTreeCache.loading = true

    const loadPromise = fetchGetCampusTree()
      .then((response) => {
        campusTreeCache.data = response || []
        campusTreeCache.timestamp = Date.now()
        return campusTreeCache.data
      })
      .catch((error) => {
        console.error('加载校区树失败:', error)
        return campusTreeCache.data
      })
      .finally(() => {
        campusTreeCache.loading = false
        campusTreeCache.loadPromise = null
      })

    campusTreeCache.loadPromise = loadPromise
    return loadPromise
  }

  /**
   * 后台刷新校区树（不阻塞，静默更新缓存）
   */
  const revalidateCampusTree = async (): Promise<void> => {
    if (campusTreeCache.revalidating || campusTreeCache.loading) {
      return
    }

    campusTreeCache.revalidating = true

    fetchGetCampusTree()
      .then((response) => {
        campusTreeCache.data = response || []
        campusTreeCache.timestamp = Date.now()
      })
      .catch((error) => {
        console.error('后台刷新校区树失败:', error)
      })
      .finally(() => {
        campusTreeCache.revalidating = false
      })
  }

  /**
   * 获取校区树（如果已缓存则直接返回）
   * @returns 校区树列表
   */
  const getCampusTree = (): Api.SystemManage.CampusTreeList => {
    return campusTreeCache.data
  }

  /**
   * 刷新校区树缓存
   */
  const refreshCampusTree = async (): Promise<void> => {
    await loadCampusTree(true)
  }

  // ==================== 楼层相关 ====================

  /**
   * 加载楼层列表（按校区，SWR 策略：先返回缓存，后台刷新）
   * @param campusCode 校区代码
   * @param force 是否强制重新加载（忽略缓存）
   * @returns 楼层列表
   */
  const loadFloorList = async (
    campusCode: string,
    force = false
  ): Promise<Api.SystemManage.FloorListItem[]> => {
    if (!campusCode) {
      return []
    }

    const cache = floorListCache.get(campusCode)

    // 如果有缓存且不强制刷新，立即返回缓存数据
    if (cache && !force && cache.data.length > 0) {
      // 如果缓存未过期，直接返回
      if (!isCacheExpired(cache.timestamp)) {
        // 后台静默刷新（如果不在刷新中）
        if (!cache.revalidating && !cache.loading) {
          revalidateFloorList(campusCode)
        }
        return cache.data
      }
      // 缓存已过期，但先返回旧数据，后台刷新
      if (!cache.revalidating && !cache.loading) {
        revalidateFloorList(campusCode)
      }
      return cache.data
    }

    // 如果没有缓存或强制刷新，等待 API 返回
    // 如果正在加载，返回同一个 Promise（防止重复请求）
    if (cache?.loading && cache.loadPromise) {
      return cache.loadPromise
    }

    // 初始化或更新缓存项
    if (!cache) {
      floorListCache.set(campusCode, createEmptyCache([]))
    }

    const currentCache = floorListCache.get(campusCode)!
    currentCache.loading = true

    const loadPromise = fetchGetFloorPage({
      campusCode,
      pageNum: 1,
      pageSize: 1000
    })
      .then((response) => {
        currentCache.data = response?.list || []
        currentCache.timestamp = Date.now()
        return currentCache.data
      })
      .catch((error) => {
        console.error(`加载楼层列表失败 [${campusCode}]:`, error)
        return currentCache.data
      })
      .finally(() => {
        currentCache.loading = false
        currentCache.loadPromise = null
      })

    currentCache.loadPromise = loadPromise
    return loadPromise
  }

  /**
   * 后台刷新楼层列表（不阻塞，静默更新缓存）
   */
  const revalidateFloorList = async (campusCode: string): Promise<void> => {
    if (!campusCode) {
      return
    }

    const cache = floorListCache.get(campusCode)
    if (!cache || cache.revalidating || cache.loading) {
      return
    }

    cache.revalidating = true

    fetchGetFloorPage({
      campusCode,
      pageNum: 1,
      pageSize: 1000
    })
      .then((response) => {
        cache.data = response?.list || []
        cache.timestamp = Date.now()
      })
      .catch((error) => {
        console.error(`后台刷新楼层列表失败 [${campusCode}]:`, error)
      })
      .finally(() => {
        cache.revalidating = false
      })
  }

  /**
   * 获取楼层列表（如果已缓存则直接返回）
   * @param campusCode 校区代码
   * @returns 楼层列表
   */
  const getFloorList = (campusCode: string): Api.SystemManage.FloorListItem[] => {
    return floorListCache.get(campusCode)?.data || []
  }

  /**
   * 刷新楼层列表缓存
   * @param campusCode 校区代码（可选，不传则清空所有楼层缓存）
   */
  const refreshFloorList = async (campusCode?: string): Promise<void> => {
    if (campusCode) {
      await loadFloorList(campusCode, true)
    } else {
      floorListCache.clear()
    }
  }

  // ==================== 房间相关 ====================

  /**
   * 加载房间列表（按楼层，SWR 策略：先返回缓存，后台刷新）
   * @param floorId 楼层ID
   * @param force 是否强制重新加载（忽略缓存）
   * @returns 房间列表
   */
  const loadRoomList = async (
    floorId: number,
    force = false
  ): Promise<Api.SystemManage.RoomListItem[]> => {
    if (!floorId) {
      return []
    }

    const cache = roomListCache.get(floorId)

    // 如果有缓存且不强制刷新，立即返回缓存数据
    if (cache && !force && cache.data.length > 0) {
      // 如果缓存未过期，直接返回
      if (!isCacheExpired(cache.timestamp)) {
        // 后台静默刷新（如果不在刷新中）
        if (!cache.revalidating && !cache.loading) {
          revalidateRoomList(floorId)
        }
        return cache.data
      }
      // 缓存已过期，但先返回旧数据，后台刷新
      if (!cache.revalidating && !cache.loading) {
        revalidateRoomList(floorId)
      }
      return cache.data
    }

    // 如果没有缓存或强制刷新，等待 API 返回
    // 如果正在加载，返回同一个 Promise（防止重复请求）
    if (cache?.loading && cache.loadPromise) {
      return cache.loadPromise
    }

    // 初始化或更新缓存项
    if (!cache) {
      roomListCache.set(floorId, createEmptyCache([]))
    }

    const currentCache = roomListCache.get(floorId)!
    currentCache.loading = true

    const loadPromise = fetchGetRoomPage({
      floorId,
      pageNum: 1,
      pageSize: 1000
    })
      .then((response) => {
        currentCache.data = response?.list || []
        currentCache.timestamp = Date.now()
        return currentCache.data
      })
      .catch((error) => {
        console.error(`加载房间列表失败 [floorId=${floorId}]:`, error)
        return currentCache.data
      })
      .finally(() => {
        currentCache.loading = false
        currentCache.loadPromise = null
      })

    currentCache.loadPromise = loadPromise
    return loadPromise
  }

  /**
   * 后台刷新房间列表（不阻塞，静默更新缓存）
   */
  const revalidateRoomList = async (floorId: number): Promise<void> => {
    if (!floorId) {
      return
    }

    const cache = roomListCache.get(floorId)
    if (!cache || cache.revalidating || cache.loading) {
      return
    }

    cache.revalidating = true

    fetchGetRoomPage({
      floorId,
      pageNum: 1,
      pageSize: 1000
    })
      .then((response) => {
        cache.data = response?.list || []
        cache.timestamp = Date.now()
      })
      .catch((error) => {
        console.error(`后台刷新房间列表失败 [floorId=${floorId}]:`, error)
      })
      .finally(() => {
        cache.revalidating = false
      })
  }

  /**
   * 获取房间列表（如果已缓存则直接返回）
   * @param floorId 楼层ID
   * @returns 房间列表
   */
  const getRoomList = (floorId: number): Api.SystemManage.RoomListItem[] => {
    return roomListCache.get(floorId)?.data || []
  }

  /**
   * 刷新房间列表缓存
   * @param floorId 楼层ID（可选，不传则清空所有房间缓存）
   */
  const refreshRoomList = async (floorId?: number): Promise<void> => {
    if (floorId) {
      await loadRoomList(floorId, true)
    } else {
      roomListCache.clear()
    }
  }

  // ==================== 角色相关 ====================

  /**
   * 加载所有角色（SWR 策略：先返回缓存，后台刷新）
   * @param force 是否强制重新加载（忽略缓存）
   * @returns 角色列表
   */
  const loadAllRoles = async (force = false): Promise<Api.SystemManage.RoleListItem[]> => {
    // 如果有缓存且不强制刷新，立即返回缓存数据
    if (!force && allRolesCache.data.length > 0) {
      // 如果缓存未过期，直接返回
      if (!isCacheExpired(allRolesCache.timestamp)) {
        // 后台静默刷新（如果不在刷新中）
        if (!allRolesCache.revalidating && !allRolesCache.loading) {
          revalidateAllRoles()
        }
        return allRolesCache.data
      }
      // 缓存已过期，但先返回旧数据，后台刷新
      if (!allRolesCache.revalidating && !allRolesCache.loading) {
        revalidateAllRoles()
      }
      return allRolesCache.data
    }

    // 如果没有缓存或强制刷新，等待 API 返回
    // 如果正在加载，返回同一个 Promise（防止重复请求）
    if (allRolesCache.loading && allRolesCache.loadPromise) {
      return allRolesCache.loadPromise
    }

    allRolesCache.loading = true

    const loadPromise = fetchGetAllRoles()
      .then((response) => {
        allRolesCache.data = response || []
        allRolesCache.timestamp = Date.now()
        return allRolesCache.data
      })
      .catch((error) => {
        console.error('加载角色列表失败:', error)
        return allRolesCache.data
      })
      .finally(() => {
        allRolesCache.loading = false
        allRolesCache.loadPromise = null
      })

    allRolesCache.loadPromise = loadPromise
    return loadPromise
  }

  /**
   * 后台刷新角色列表（不阻塞，静默更新缓存）
   */
  const revalidateAllRoles = async (): Promise<void> => {
    if (allRolesCache.revalidating || allRolesCache.loading) {
      return
    }

    allRolesCache.revalidating = true

    fetchGetAllRoles()
      .then((response) => {
        allRolesCache.data = response || []
        allRolesCache.timestamp = Date.now()
      })
      .catch((error) => {
        console.error('后台刷新角色列表失败:', error)
      })
      .finally(() => {
        allRolesCache.revalidating = false
      })
  }

  /**
   * 获取所有角色（如果已缓存则直接返回）
   * @returns 角色列表
   */
  const getAllRoles = (): Api.SystemManage.RoleListItem[] => {
    return allRolesCache.data
  }

  /**
   * 刷新角色列表缓存
   */
  const refreshAllRoles = async (): Promise<void> => {
    await loadAllRoles(true)
  }

  // ==================== 用户相关 ====================

  /**
   * 加载用户列表（按角色代码，SWR 策略：先返回缓存，后台刷新）
   * @param roleCodes 角色代码数组
   * @param force 是否强制重新加载（忽略缓存）
   * @returns 用户列表
   */
  const loadUsersByRoleCodes = async (
    roleCodes: string[],
    force = false
  ): Promise<Api.SystemManage.UserSimpleItem[]> => {
    if (!roleCodes || roleCodes.length === 0) {
      return []
    }

    // 使用排序后的角色代码作为缓存键
    const cacheKey = [...roleCodes].sort().join(',')
    const cache = usersByRoleCodesCache.get(cacheKey)

    // 如果有缓存且不强制刷新，立即返回缓存数据
    if (cache && !force && cache.data.length > 0) {
      // 如果缓存未过期，直接返回
      if (!isCacheExpired(cache.timestamp)) {
        // 后台静默刷新（如果不在刷新中）
        if (!cache.revalidating && !cache.loading) {
          revalidateUsersByRoleCodes(roleCodes)
        }
        return cache.data
      }
      // 缓存已过期，但先返回旧数据，后台刷新
      if (!cache.revalidating && !cache.loading) {
        revalidateUsersByRoleCodes(roleCodes)
      }
      return cache.data
    }

    // 如果没有缓存或强制刷新，等待 API 返回
    // 如果正在加载，返回同一个 Promise（防止重复请求）
    if (cache?.loading && cache.loadPromise) {
      return cache.loadPromise
    }

    // 初始化或更新缓存项
    if (!cache) {
      usersByRoleCodesCache.set(cacheKey, createEmptyCache([]))
    }

    const currentCache = usersByRoleCodesCache.get(cacheKey)!
    currentCache.loading = true

    const loadPromise = fetchGetUsersByRoleCodes(roleCodes)
      .then((response) => {
        // 合并所有角色的用户列表并去重
        const allUsers: Api.SystemManage.UserSimpleItem[] = []
        const userIds = new Set<number>()

        if (response) {
          Object.values(response).forEach((users) => {
            users.forEach((user) => {
              if (!userIds.has(user.id)) {
                userIds.add(user.id)
                allUsers.push(user)
              }
            })
          })
        }

        currentCache.data = allUsers
        currentCache.timestamp = Date.now()
        return currentCache.data
      })
      .catch((error) => {
        console.error(`加载用户列表失败 [roleCodes=${roleCodes.join(',')}]:`, error)
        return currentCache.data
      })
      .finally(() => {
        currentCache.loading = false
        currentCache.loadPromise = null
      })

    currentCache.loadPromise = loadPromise
    return loadPromise
  }

  /**
   * 后台刷新用户列表（不阻塞，静默更新缓存）
   */
  const revalidateUsersByRoleCodes = async (roleCodes: string[]): Promise<void> => {
    if (!roleCodes || roleCodes.length === 0) {
      return
    }

    const cacheKey = [...roleCodes].sort().join(',')
    const cache = usersByRoleCodesCache.get(cacheKey)
    if (!cache || cache.revalidating || cache.loading) {
      return
    }

    cache.revalidating = true

    fetchGetUsersByRoleCodes(roleCodes)
      .then((response) => {
        // 合并所有角色的用户列表并去重
        const allUsers: Api.SystemManage.UserSimpleItem[] = []
        const userIds = new Set<number>()

        if (response) {
          Object.values(response).forEach((users) => {
            users.forEach((user) => {
              if (!userIds.has(user.id)) {
                userIds.add(user.id)
                allUsers.push(user)
              }
            })
          })
        }

        cache.data = allUsers
        cache.timestamp = Date.now()
      })
      .catch((error) => {
        console.error(`后台刷新用户列表失败 [roleCodes=${roleCodes.join(',')}]:`, error)
      })
      .finally(() => {
        cache.revalidating = false
      })
  }

  /**
   * 获取用户列表（如果已缓存则直接返回）
   * @param roleCodes 角色代码数组
   * @returns 用户列表
   */
  const getUsersByRoleCodes = (roleCodes: string[]): Api.SystemManage.UserSimpleItem[] => {
    if (!roleCodes || roleCodes.length === 0) {
      return []
    }
    const cacheKey = [...roleCodes].sort().join(',')
    return usersByRoleCodesCache.get(cacheKey)?.data || []
  }

  /**
   * 刷新用户列表缓存
   * @param roleCodes 角色代码数组（可选，不传则清空所有用户缓存）
   */
  const refreshUsersByRoleCodes = async (roleCodes?: string[]): Promise<void> => {
    if (roleCodes && roleCodes.length > 0) {
      await loadUsersByRoleCodes(roleCodes, true)
    } else {
      usersByRoleCodesCache.clear()
    }
  }

  // ==================== 专业相关 ====================

  /**
   * 加载专业列表（SWR 策略：先返回缓存，后台刷新）
   * @param force 是否强制重新加载（忽略缓存）
   * @returns 专业列表
   */
  const loadMajorList = async (force = false): Promise<Api.SystemManage.MajorListItem[]> => {
    // 如果有缓存且不强制刷新，立即返回缓存数据
    if (!force && majorListCache.data.length > 0) {
      // 如果缓存未过期，直接返回
      if (!isCacheExpired(majorListCache.timestamp)) {
        // 后台静默刷新（如果不在刷新中）
        if (!majorListCache.revalidating && !majorListCache.loading) {
          revalidateMajorList()
        }
        return majorListCache.data
      }
      // 缓存已过期，但先返回旧数据，后台刷新
      if (!majorListCache.revalidating && !majorListCache.loading) {
        revalidateMajorList()
      }
      return majorListCache.data
    }

    // 如果没有缓存或强制刷新，等待 API 返回
    // 如果正在加载，返回同一个 Promise（防止重复请求）
    if (majorListCache.loading && majorListCache.loadPromise) {
      return majorListCache.loadPromise
    }

    majorListCache.loading = true

    const loadPromise = fetchGetMajorPage({
      pageNum: 1,
      pageSize: 1000
    })
      .then((response) => {
        majorListCache.data = response?.list || []
        majorListCache.timestamp = Date.now()
        return majorListCache.data
      })
      .catch((error) => {
        console.error('加载专业列表失败:', error)
        return majorListCache.data
      })
      .finally(() => {
        majorListCache.loading = false
        majorListCache.loadPromise = null
      })

    majorListCache.loadPromise = loadPromise
    return loadPromise
  }

  /**
   * 后台刷新专业列表（不阻塞，静默更新缓存）
   */
  const revalidateMajorList = async (): Promise<void> => {
    if (majorListCache.revalidating || majorListCache.loading) {
      return
    }

    majorListCache.revalidating = true

    fetchGetMajorPage({
      pageNum: 1,
      pageSize: 1000
    })
      .then((response) => {
        majorListCache.data = response?.list || []
        majorListCache.timestamp = Date.now()
      })
      .catch((error) => {
        console.error('后台刷新专业列表失败:', error)
      })
      .finally(() => {
        majorListCache.revalidating = false
      })
  }

  /**
   * 获取专业列表（如果已缓存则直接返回）
   * @returns 专业列表
   */
  const getMajorList = (): Api.SystemManage.MajorListItem[] => {
    return majorListCache.data
  }

  /**
   * 刷新专业列表缓存
   */
  const refreshMajorList = async (): Promise<void> => {
    await loadMajorList(true)
  }

  // ==================== 院系相关 ====================

  /**
   * 加载院系树（SWR 策略：先返回缓存，后台刷新）
   * @param force 是否强制重新加载（忽略缓存）
   * @returns 院系树列表
   */
  const loadDepartmentTree = async (
    force = false
  ): Promise<Api.SystemManage.DepartmentTreeList> => {
    // 如果有缓存且不强制刷新，立即返回缓存数据
    if (!force && departmentTreeCache.data.length > 0) {
      // 如果缓存未过期，直接返回
      if (!isCacheExpired(departmentTreeCache.timestamp)) {
        // 后台静默刷新（如果不在刷新中）
        if (!departmentTreeCache.revalidating && !departmentTreeCache.loading) {
          revalidateDepartmentTree()
        }
        return departmentTreeCache.data
      }
      // 缓存已过期，但先返回旧数据，后台刷新
      if (!departmentTreeCache.revalidating && !departmentTreeCache.loading) {
        revalidateDepartmentTree()
      }
      return departmentTreeCache.data
    }

    // 如果没有缓存或强制刷新，等待 API 返回
    // 如果正在加载，返回同一个 Promise（防止重复请求）
    if (departmentTreeCache.loading && departmentTreeCache.loadPromise) {
      return departmentTreeCache.loadPromise
    }

    departmentTreeCache.loading = true

    const loadPromise = fetchGetDepartmentTree()
      .then((response) => {
        departmentTreeCache.data = response || []
        departmentTreeCache.timestamp = Date.now()
        return departmentTreeCache.data
      })
      .catch((error) => {
        console.error('加载院系树失败:', error)
        return departmentTreeCache.data
      })
      .finally(() => {
        departmentTreeCache.loading = false
        departmentTreeCache.loadPromise = null
      })

    departmentTreeCache.loadPromise = loadPromise
    return loadPromise
  }

  /**
   * 后台刷新院系树（不阻塞，静默更新缓存）
   */
  const revalidateDepartmentTree = async (): Promise<void> => {
    if (departmentTreeCache.revalidating || departmentTreeCache.loading) {
      return
    }

    departmentTreeCache.revalidating = true

    fetchGetDepartmentTree()
      .then((response) => {
        departmentTreeCache.data = response || []
        departmentTreeCache.timestamp = Date.now()
      })
      .catch((error) => {
        console.error('后台刷新院系树失败:', error)
      })
      .finally(() => {
        departmentTreeCache.revalidating = false
      })
  }

  /**
   * 获取院系树（如果已缓存则直接返回）
   * @returns 院系树列表
   */
  const getDepartmentTree = (): Api.SystemManage.DepartmentTreeList => {
    return departmentTreeCache.data
  }

  /**
   * 刷新院系树缓存
   */
  const refreshDepartmentTree = async (): Promise<void> => {
    await loadDepartmentTree(true)
  }

  // ==================== 菜单相关 ====================

  /**
   * 加载菜单树选择器（SWR 策略：先返回缓存，后台刷新）
   * @param force 是否强制重新加载（忽略缓存）
   * @returns 菜单树列表
   */
  const loadMenuTreeSelect = async (force = false): Promise<Api.SystemManage.MenuTreeList> => {
    // 如果有缓存且不强制刷新，立即返回缓存数据
    if (!force && menuTreeSelectCache.data.length > 0) {
      // 如果缓存未过期，直接返回
      if (!isCacheExpired(menuTreeSelectCache.timestamp)) {
        // 后台静默刷新（如果不在刷新中）
        if (!menuTreeSelectCache.revalidating && !menuTreeSelectCache.loading) {
          revalidateMenuTreeSelect()
        }
        return menuTreeSelectCache.data
      }
      // 缓存已过期，但先返回旧数据，后台刷新
      if (!menuTreeSelectCache.revalidating && !menuTreeSelectCache.loading) {
        revalidateMenuTreeSelect()
      }
      return menuTreeSelectCache.data
    }

    // 如果没有缓存或强制刷新，等待 API 返回
    // 如果正在加载，返回同一个 Promise（防止重复请求）
    if (menuTreeSelectCache.loading && menuTreeSelectCache.loadPromise) {
      return menuTreeSelectCache.loadPromise
    }

    menuTreeSelectCache.loading = true

    const loadPromise = fetchGetMenuTreeSelect()
      .then((response) => {
        menuTreeSelectCache.data = response || []
        menuTreeSelectCache.timestamp = Date.now()
        return menuTreeSelectCache.data
      })
      .catch((error) => {
        console.error('加载菜单树失败:', error)
        return menuTreeSelectCache.data
      })
      .finally(() => {
        menuTreeSelectCache.loading = false
        menuTreeSelectCache.loadPromise = null
      })

    menuTreeSelectCache.loadPromise = loadPromise
    return loadPromise
  }

  /**
   * 后台刷新菜单树（不阻塞，静默更新缓存）
   */
  const revalidateMenuTreeSelect = async (): Promise<void> => {
    if (menuTreeSelectCache.revalidating || menuTreeSelectCache.loading) {
      return
    }

    menuTreeSelectCache.revalidating = true

    fetchGetMenuTreeSelect()
      .then((response) => {
        menuTreeSelectCache.data = response || []
        menuTreeSelectCache.timestamp = Date.now()
      })
      .catch((error) => {
        console.error('后台刷新菜单树失败:', error)
      })
      .finally(() => {
        menuTreeSelectCache.revalidating = false
      })
  }

  /**
   * 获取菜单树选择器（如果已缓存则直接返回）
   * @returns 菜单树列表
   */
  const getMenuTreeSelect = (): Api.SystemManage.MenuTreeList => {
    return menuTreeSelectCache.data
  }

  /**
   * 刷新菜单树缓存
   */
  const refreshMenuTreeSelect = async (): Promise<void> => {
    await loadMenuTreeSelect(true)
  }

  // ==================== 通用方法 ====================

  /**
   * 清空缓存
   * @param key 缓存键（可选，不传则清空所有）
   */
  const clearCache = (key?: string): void => {
    if (!key) {
      // 清空所有缓存
      campusTreeCache.data = []
      campusTreeCache.timestamp = 0
      floorListCache.clear()
      roomListCache.clear()
      allRolesCache.data = []
      allRolesCache.timestamp = 0
      usersByRoleCodesCache.clear()
      majorListCache.data = []
      majorListCache.timestamp = 0
      departmentTreeCache.data = []
      departmentTreeCache.timestamp = 0
      menuTreeSelectCache.data = []
      menuTreeSelectCache.timestamp = 0
      return
    }

    // 根据键清空特定缓存
    switch (key) {
      case 'campusTree':
        campusTreeCache.data = []
        campusTreeCache.timestamp = 0
        break
      case 'floorList':
        floorListCache.clear()
        break
      case 'roomList':
        roomListCache.clear()
        break
      case 'allRoles':
        allRolesCache.data = []
        allRolesCache.timestamp = 0
        break
      case 'usersByRoleCodes':
        usersByRoleCodesCache.clear()
        break
      case 'majorList':
        majorListCache.data = []
        majorListCache.timestamp = 0
        break
      case 'departmentTree':
        departmentTreeCache.data = []
        departmentTreeCache.timestamp = 0
        break
      case 'menuTreeSelect':
        menuTreeSelectCache.data = []
        menuTreeSelectCache.timestamp = 0
        break
    }
  }

  return {
    // 校区相关
    loadCampusTree,
    getCampusTree,
    refreshCampusTree,

    // 楼层相关
    loadFloorList,
    getFloorList,
    refreshFloorList,

    // 房间相关
    loadRoomList,
    getRoomList,
    refreshRoomList,

    // 角色相关
    loadAllRoles,
    getAllRoles,
    refreshAllRoles,

    // 用户相关
    loadUsersByRoleCodes,
    getUsersByRoleCodes,
    refreshUsersByRoleCodes,

    // 专业相关
    loadMajorList,
    getMajorList,
    refreshMajorList,

    // 院系相关
    loadDepartmentTree,
    getDepartmentTree,
    refreshDepartmentTree,

    // 菜单相关
    loadMenuTreeSelect,
    getMenuTreeSelect,
    refreshMenuTreeSelect,

    // 通用方法
    clearCache,

    /**
     * 级联选择器懒加载方法（校区 → 楼层 → 房间）
     * @param node 当前节点
     * @param resolve 解析回调
     * @param maxLevel 最大层级（1: 仅校区, 2: 校区+楼层, 3: 校区+楼层+房间）
     */
    async cascaderLazyLoad(
      node: { level: number; value?: string | number; data?: any },
      resolve: (nodes: any[]) => void,
      maxLevel: 1 | 2 | 3 = 3
    ) {
      const { level, data } = node

      // 第一级：加载校区
      if (level === 0) {
        const campusList = await loadCampusTree()
        const nodes = campusList.map((campus) => ({
          value: campus.campusCode,
          label: campus.campusName,
          leaf: maxLevel === 1
        }))
        resolve(nodes)
        return
      }

      // 第二级：加载楼层
      if (level === 1 && maxLevel >= 2) {
        const campusCode = data.value
        const floorList = await loadFloorList(campusCode)
        const nodes = floorList.map((floor) => ({
          value: floor.floorCode,
          label: floor.floorName,
          floorId: floor.id,
          leaf: maxLevel === 2
        }))
        resolve(nodes)
        return
      }

      // 第三级：加载房间
      if (level === 2 && maxLevel >= 3) {
        const floorId = data.floorId
        if (floorId) {
          const roomList = await loadRoomList(floorId)
          const nodes = roomList.map((room) => ({
            value: room.roomCode,
            label: room.roomNumber || room.roomCode,
            leaf: true
          }))
          resolve(nodes)
        } else {
          resolve([])
        }
        return
      }

      // 默认返回空
      resolve([])
    },

    /**
     * 组织管理级联选择器懒加载方法（校区 → 院系 → 专业）
     * @param node 当前节点
     * @param resolve 解析回调
     * @param maxLevel 最大层级（1: 仅校区, 2: 校区+院系, 3: 校区+院系+专业）
     */
    async orgCascaderLazyLoad(
      node: { level: number; value?: string | number; data?: any },
      resolve: (nodes: any[]) => void,
      maxLevel: 1 | 2 | 3 = 3
    ) {
      const { level, data } = node

      // 第一级：加载校区
      if (level === 0) {
        const campusList = await loadCampusTree()
        const nodes = campusList.map((campus) => ({
          value: campus.campusCode,
          label: campus.campusName,
          leaf: maxLevel === 1
        }))
        resolve(nodes)
        return
      }

      // 第二级：加载院系（按校区过滤）
      if (level === 1 && maxLevel >= 2) {
        const campusCode = data.value
        const deptList = await loadDepartmentTree()
        // 过滤出属于该校区的院系
        const filteredDepts = deptList.filter((dept) => dept.campusCode === campusCode)
        const nodes = filteredDepts.map((dept) => ({
          value: dept.deptCode,
          label: dept.deptName,
          leaf: maxLevel === 2
        }))
        resolve(nodes)
        return
      }

      // 第三级：加载专业（按院系过滤）
      if (level === 2 && maxLevel >= 3) {
        const deptCode = data.value
        const majorList = await loadMajorList()
        // 过滤出属于该院系的专业
        const filteredMajors = majorList.filter((major) => major.deptCode === deptCode)
        const nodes = filteredMajors.map((major) => ({
          value: major.majorCode,
          label: major.majorName,
          leaf: true
        }))
        resolve(nodes)
        return
      }

      // 默认返回空
      resolve([])
    }
  }
})
