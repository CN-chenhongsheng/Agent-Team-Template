package com.project.backend.system.service;

import java.util.List;

/**
 * 权限查询服务
 *
 * @author 陈鸿昇
 * @since 2026-07-10
 */
public interface PermissionService {

    /**
     * 获取用户角色编码列表
     *
     * @param userId 用户ID
     * @return 角色编码列表
     */
    List<String> getRoleCodes(Long userId);

    /**
     * 获取用户权限标识列表
     *
     * @param userId 用户ID
     * @return 权限标识列表
     */
    List<String> getPermissionCodes(Long userId);
}
