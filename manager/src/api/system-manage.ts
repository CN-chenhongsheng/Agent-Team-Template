/**
 * 系统管理模块 API
 * 包含用户管理、角色管理、菜单管理等接口
 *
 * @module api/system-manage
 * @author 陈鸿昇
 * @date 2025-12-30
 */

import request from '@/utils/http'

/** ==================== 用户管理 ==================== */

/**
 * 获取用户分页列表
 * @param params 查询参数
 */
export function fetchGetUserList(params: Api.SystemManage.UserSearchParams) {
  return request.get<Api.SystemManage.UserPageResponse>({
    url: '/api/v1/system/user/list',
    params
  })
}

/**
 * 获取用户详情
 * @param id 用户ID
 */
export function fetchGetUserDetail(id: number) {
  return request.get<Api.SystemManage.UserListItem>({
    url: `/api/v1/system/user/${id}`
  })
}

/**
 * 新增用户
 * @param data 用户数据
 */
export function fetchAddUser(data: Api.SystemManage.UserSaveParams) {
  return request.post({
    url: '/api/v1/system/user',
    data,
    showSuccessMessage: true
  })
}

/**
 * 编辑用户
 * @param id 用户ID
 * @param data 用户数据
 */
export function fetchUpdateUser(id: number, data: Api.SystemManage.UserSaveParams) {
  return request.put({
    url: `/api/v1/system/user/${id}`,
    data,
    showSuccessMessage: true
  })
}

/**
 * 删除用户
 * @param id 用户ID
 */
export function fetchDeleteUser(id: number) {
  return request.del({
    url: `/api/v1/system/user/${id}`,
    showSuccessMessage: true
  })
}

/**
 * 批量删除用户
 * @param ids 用户ID列表
 */
export function fetchBatchDeleteUser(ids: number[]) {
  return request.del({
    url: '/api/v1/system/user/batch',
    data: ids,
    showSuccessMessage: true
  })
}

/**
 * 重置用户密码
 * @param id 用户ID
 * @param data 新密码数据
 */
export function fetchResetUserPassword(id: number, data: Api.SystemManage.UserResetPasswordParams) {
  return request.put<void>({
    url: `/api/v1/system/user/${id}/reset-password`,
    data,
    showSuccessMessage: true
  })
}

/**
 * 修改用户状态
 * @param id 用户ID
 * @param status 状态
 */
export function fetchUpdateUserStatus(id: number, status: number) {
  return request.put({
    url: `/api/v1/system/user/${id}/status/${status}`,
    showSuccessMessage: true
  })
}

/** ==================== 角色管理 ==================== */

/**
 * 获取角色列表（分页）
 * @param params 查询参数
 */
export function fetchGetRoleList(params: Api.SystemManage.RoleSearchParams) {
  return request.get<Api.SystemManage.RoleList>({
    url: '/api/v1/system/role/list',
    params: {
      pageNum: params.pageNum || 1,
      pageSize: params.pageSize || 10,
      ...params
    }
  })
}

/**
 * 获取所有角色（不分页）
 */
export function fetchGetAllRoles() {
  return request.get<Api.SystemManage.RoleListItem[]>({
    url: '/api/v1/system/role/all'
  })
}

/**
 * 获取角色详情
 * @param id 角色ID
 */
export function fetchGetRoleDetail(id: number) {
  return request.get<Api.SystemManage.RoleListItem>({
    url: `/api/v1/system/role/${id}`
  })
}

/**
 * 新增角色
 * @param data 角色数据
 */
export function fetchAddRole(data: Api.SystemManage.RoleSaveParams) {
  return request.post({
    url: '/api/v1/system/role',
    data,
    showSuccessMessage: true
  })
}

/**
 * 编辑角色
 * @param id 角色ID
 * @param data 角色数据
 */
export function fetchUpdateRole(id: number, data: Api.SystemManage.RoleSaveParams) {
  return request.put({
    url: `/api/v1/system/role/${id}`,
    data,
    showSuccessMessage: true
  })
}

/**
 * 删除角色
 * @param id 角色ID
 */
export function fetchDeleteRole(id: number) {
  return request.del({
    url: `/api/v1/system/role/${id}`,
    showSuccessMessage: true
  })
}

/**
 * 批量删除角色
 * @param ids 角色ID列表
 */
export function fetchBatchDeleteRole(ids: number[]) {
  return request.del({
    url: '/api/v1/system/role/batch',
    data: ids,
    showSuccessMessage: true
  })
}

/**
 * 分配角色菜单权限
 * @param id 角色ID
 * @param menuIds 菜单ID列表
 */
export function fetchAssignRolePermissions(id: number, menuIds: number[]) {
  return request.put({
    url: `/api/v1/system/role/${id}/permissions`,
    data: menuIds,
    showSuccessMessage: true
  })
}

/**
 * 获取角色的菜单权限（包含菜单状态）
 * @param id 角色ID
 */
export function fetchGetRolePermissions(id: number) {
  return request.get<Api.SystemManage.RolePermissionItem[]>({
    url: `/api/v1/system/role/${id}/permissions`
  })
}

/**
 * 获取用户权限列表
 * @param id 用户ID
 */
export function fetchGetUserPermissions(id: number) {
  return request.get<Api.SystemManage.UserPermissionItem[]>({
    url: `/api/v1/system/user/${id}/permissions`
  })
}

/**
 * 分配用户菜单权限
 * @param id 用户ID
 * @param menuIds 菜单ID列表
 */
export function fetchAssignUserPermissions(id: number, menuIds: number[]) {
  return request.put({
    url: `/api/v1/system/user/${id}/permissions`,
    data: menuIds,
    showSuccessMessage: true
  })
}

/**
 * 获取用户可选的菜单列表（用于权限分配界面）
 * @param id 用户ID
 */
export function fetchGetUserAvailableMenus(id: number) {
  return request.get<number[]>({
    url: `/api/v1/system/user/${id}/available-menus`
  })
}

/**
 * 修改角色状态
 * @param id 角色ID
 * @param status 状态：1正常 0停用
 */
export function fetchUpdateRoleStatus(id: number, status: number) {
  return request.put({
    url: `/api/v1/system/role/${id}/status/${status}`,
    showSuccessMessage: true
  })
}

/**
 * 根据角色代码列表查询用户列表
 * @param roleCodes 角色代码列表
 * @returns Map格式，key为角色代码，value为用户列表
 */
export function fetchGetUsersByRoleCodes(roleCodes: string[]) {
  return request.post<Api.SystemManage.RoleUserMap>({
    url: '/api/v1/system/user/by-roles',
    data: {
      roleCodes: roleCodes
    }
  })
}

/** ==================== 操作日志管理 ==================== */

/**
 * 获取操作日志分页列表
 * @param params 查询参数
 */
export function fetchGetOperLogList(params: Api.SystemManage.OperLogSearchParams) {
  return request.get<Api.SystemManage.OperLogPageResponse>({
    url: '/api/v1/system/oper-log/page',
    params
  })
}

/**
 * 获取操作日志详情
 * @param id 日志ID
 */
export function fetchGetOperLogDetail(id: number) {
  return request.get<Api.SystemManage.OperLogListItem>({
    url: `/api/v1/system/oper-log/${id}`
  })
}

/**
 * 批量删除操作日志
 * @param ids 日志ID列表
 */
export function fetchBatchDeleteOperLog(ids: number[]) {
  return request.del({
    url: '/api/v1/system/oper-log/batch',
    data: ids,
    showSuccessMessage: true
  })
}

/**
 * 清空操作日志
 */
export function fetchCleanOperLog() {
  return request.del({
    url: '/api/v1/system/oper-log/clean',
    showSuccessMessage: true
  })
}

/** ==================== 菜单管理 ==================== */

/**
 * 获取菜单树形列表
 * @param params 查询参数
 */
export function fetchGetMenuList(params?: Api.SystemManage.MenuSearchParams) {
  return request.get<Api.SystemManage.MenuTreeList>({
    url: '/api/v1/system/menu/tree',
    params
  })
}

/**
 * 获取菜单树形选择器
 */
export function fetchGetMenuTreeSelect() {
  return request.get<Api.SystemManage.MenuTreeList>({
    url: '/api/v1/system/menu/tree-select'
  })
}

/**
 * 获取菜单树用于权限分配（包含所有类型，不包含顶级菜单）
 */
export function fetchGetMenuTreeForPermission() {
  return request.get<Api.SystemManage.MenuTreeList>({
    url: '/api/v1/system/menu/tree-permission'
  })
}

/**
 * 获取当前用户菜单树
 * 根据当前登录用户的角色权限返回有权限查看的菜单树
 */
export function fetchGetUserMenuTree() {
  return request.get<Api.SystemManage.MenuTreeList>({
    url: '/api/v1/system/menu/user-tree'
  })
}

/**
 * 获取菜单详情
 * @param id 菜单ID
 */
export function fetchGetMenuDetail(id: number) {
  return request.get<Api.SystemManage.MenuListItem>({
    url: `/api/v1/system/menu/${id}`
  })
}

/**
 * 新增菜单
 * @param data 菜单数据
 */
export function fetchAddMenu(data: Api.SystemManage.MenuSaveParams) {
  return request.post({
    url: '/api/v1/system/menu',
    data,
    showSuccessMessage: true
  })
}

/**
 * 编辑菜单
 * @param id 菜单ID
 * @param data 菜单数据
 */
export function fetchUpdateMenu(id: number, data: Api.SystemManage.MenuSaveParams) {
  return request.put({
    url: `/api/v1/system/menu/${id}`,
    data,
    showSuccessMessage: true
  })
}

/**
 * 删除菜单
 * @param id 菜单ID
 */
export function fetchDeleteMenu(id: number) {
  return request.del({
    url: `/api/v1/system/menu/${id}`,
    showSuccessMessage: true
  })
}

/**
 * 修改菜单状态
 * @param id 菜单ID
 * @param status 状态：1正常 0停用
 */
export function fetchUpdateMenuStatus(id: number, status: number) {
  return request.put({
    url: `/api/v1/system/menu/${id}/status/${status}`,
    showSuccessMessage: true
  })
}

/** ==================== 字典类型管理 ==================== */

/**
 * 获取字典类型分页列表
 * @param params 查询参数
 */
export function fetchGetDictTypePage(params: Api.SystemManage.DictTypeSearchParams) {
  return request.get<Api.SystemManage.DictTypePageResponse>({
    url: '/api/v1/system/dict/type/page',
    params
  })
}

/**
 * 获取字典类型详情
 * @param id 字典类型ID
 */
export function fetchGetDictTypeDetail(id: number) {
  return request.get<Api.SystemManage.DictTypeListItem>({
    url: `/api/v1/system/dict/type/${id}`
  })
}

/**
 * 新增字典类型
 * @param data 字典类型数据
 */
export function fetchAddDictType(data: Api.SystemManage.DictTypeSaveParams) {
  return request.post({
    url: '/api/v1/system/dict/type',
    data,
    showSuccessMessage: true
  })
}

/**
 * 编辑字典类型
 * @param id 字典类型ID
 * @param data 字典类型数据
 */
export function fetchUpdateDictType(id: number, data: Api.SystemManage.DictTypeSaveParams) {
  return request.put({
    url: `/api/v1/system/dict/type/${id}`,
    data,
    showSuccessMessage: true
  })
}

/**
 * 删除字典类型
 * @param id 字典类型ID
 */
export function fetchDeleteDictType(id: number) {
  return request.del({
    url: `/api/v1/system/dict/type/${id}`,
    showSuccessMessage: true
  })
}

/** ==================== 字典数据管理 ==================== */

/**
 * 获取字典数据分页列表
 * @param params 查询参数
 */
export function fetchGetDictDataPage(params: Api.SystemManage.DictDataSearchParams) {
  return request.get<Api.SystemManage.DictDataPageResponse>({
    url: '/api/v1/system/dict/data/page',
    params
  })
}

/**
 * 根据字典编码获取字典数据列表
 * @param dictCodes 字典编码或字典编码数组
 */
export function fetchGetDictDataList(dictCodes: string | string[]) {
  // 如果是单个字符串，转换为数组
  const codes = Array.isArray(dictCodes) ? dictCodes : [dictCodes]

  return request.post<Record<string, Api.SystemManage.DictDataList>>({
    url: '/api/v1/system/dict/type/batch',
    data: {
      dictCodes: codes
    }
  })
}

/**
 * 获取字典数据详情
 * @param id 字典数据ID
 */
export function fetchGetDictDataDetail(id: number) {
  return request.get<Api.SystemManage.DictDataListItem>({
    url: `/api/v1/system/dict/data/${id}`
  })
}

/**
 * 新增字典数据
 * @param data 字典数据
 */
export function fetchAddDictData(data: Api.SystemManage.DictDataSaveParams) {
  return request.post({
    url: '/api/v1/system/dict/data',
    data,
    showSuccessMessage: true
  })
}

/**
 * 编辑字典数据
 * @param id 字典数据ID
 * @param data 字典数据
 */
export function fetchUpdateDictData(id: number, data: Api.SystemManage.DictDataSaveParams) {
  return request.put({
    url: `/api/v1/system/dict/data/${id}`,
    data,
    showSuccessMessage: true
  })
}

/**
 * 删除字典数据
 * @param id 字典数据ID
 */
export function fetchDeleteDictData(id: number) {
  return request.del({
    url: `/api/v1/system/dict/data/${id}`,
    showSuccessMessage: true
  })
}

/** ==================== 通知管理 ==================== */

/**
 * 获取通知分页列表
 * @param params 查询参数
 */
export function fetchGetNoticePage(params: Api.SystemManage.NoticeSearchParams) {
  return request.get<Api.SystemManage.NoticePageResponse>({
    url: '/api/v1/system/notice/page',
    params
  })
}

/**
 * 获取通知详情
 * @param id 通知ID
 */
export function fetchGetNoticeDetail(id: number) {
  return request.get<Api.SystemManage.NoticeListItem>({
    url: `/api/v1/system/notice/${id}`
  })
}

/**
 * 新增通知
 * @param data 通知数据
 */
export function fetchAddNotice(data: Api.SystemManage.NoticeSaveParams) {
  return request.post({
    url: '/api/v1/system/notice',
    data,
    showSuccessMessage: true
  })
}

/**
 * 编辑通知
 * @param id 通知ID
 * @param data 通知数据
 */
export function fetchUpdateNotice(id: number, data: Api.SystemManage.NoticeSaveParams) {
  return request.put({
    url: `/api/v1/system/notice/${id}`,
    data,
    showSuccessMessage: true
  })
}

/**
 * 删除通知
 * @param id 通知ID
 */
export function fetchDeleteNotice(id: number) {
  return request.del({
    url: `/api/v1/system/notice/${id}`,
    showSuccessMessage: true
  })
}

/**
 * 批量删除通知
 * @param ids 通知ID列表
 */
export function fetchBatchDeleteNotice(ids: number[]) {
  return request.del({
    url: '/api/v1/system/notice/batch',
    data: ids,
    showSuccessMessage: true
  })
}
