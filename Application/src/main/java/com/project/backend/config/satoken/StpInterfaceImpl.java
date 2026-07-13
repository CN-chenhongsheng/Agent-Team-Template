package com.project.backend.config.satoken;

import cn.dev33.satoken.stp.StpInterface;
import com.project.backend.system.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sa-Token 权限认证接口实现
 *
 * @author 陈鸿昇
 * @since 2026-07-10
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final PermissionService permissionService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return permissionService.getPermissionCodes(Long.valueOf(loginId.toString()));
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return permissionService.getRoleCodes(Long.valueOf(loginId.toString()));
    }
}
