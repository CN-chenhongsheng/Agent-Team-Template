package com.project.backend.system.service.impl;

import com.project.backend.system.mapper.MenuMapper;
import com.project.backend.system.mapper.RoleMapper;
import com.project.backend.system.service.PermissionService;
import com.project.core.util.BusinessRuleUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 权限查询服务实现
 *
 * @author 陈鸿昇
 * @since 2026-07-10
 */
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final RoleMapper roleMapper;
    private final MenuMapper menuMapper;

    @Override
    @Cacheable(value = "userRoles", key = "#userId", unless = "#result == null")
    public List<String> getRoleCodes(Long userId) {
        return roleMapper.selectRoleCodesByUserId(userId);
    }

    @Override
    @Cacheable(value = "userPermissions", key = "#userId", unless = "#result == null")
    public List<String> getPermissionCodes(Long userId) {
        List<String> roleCodes = getRoleCodes(userId);
        if (roleCodes.contains(BusinessRuleUtils.SUPER_ADMIN_ROLE_CODE)) {
            return menuMapper.selectAllPermissions();
        }
        return menuMapper.selectPermissionsByUserId(userId);
    }
}
