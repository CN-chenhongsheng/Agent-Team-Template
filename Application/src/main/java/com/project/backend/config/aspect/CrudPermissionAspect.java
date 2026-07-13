package com.project.backend.config.aspect;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.project.core.annotation.PermissionAction;
import com.project.core.annotation.PermissionModule;
import com.project.core.util.BusinessRuleUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * 为标注 {@link PermissionModule} 的控制器补充声明式权限校验
 *
 * @author 陈鸿昇
 * @since 2026-07-10
 */
@Slf4j
@Aspect
@Component
@Order(0)
public class CrudPermissionAspect {

    @Before("@within(com.project.core.annotation.PermissionModule) && @annotation(com.project.core.annotation.PermissionAction)")
    public void checkCrudPermission(JoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        if (method.isAnnotationPresent(SaCheckPermission.class)) {
            return;
        }

        PermissionModule permissionModule = joinPoint.getTarget().getClass().getAnnotation(PermissionModule.class);
        if (permissionModule == null) {
            return;
        }

        if (StpUtil.hasRole(BusinessRuleUtils.SUPER_ADMIN_ROLE_CODE)) {
            return;
        }

        PermissionAction permissionAction = method.getAnnotation(PermissionAction.class);
        String permission = resolvePermission(permissionModule, permissionAction.value());
        StpUtil.checkPermission(permission);
    }

    private String resolvePermission(PermissionModule permissionModule, String action) {
        return switch (action) {
            case "view" -> StringUtils.hasText(permissionModule.view())
                    ? permissionModule.view()
                    : permissionModule.value() + ":view";
            case "add" -> StringUtils.hasText(permissionModule.add())
                    ? permissionModule.add()
                    : permissionModule.value() + ":add";
            case "edit" -> StringUtils.hasText(permissionModule.edit())
                    ? permissionModule.edit()
                    : permissionModule.value() + ":edit";
            case "delete" -> StringUtils.hasText(permissionModule.delete())
                    ? permissionModule.delete()
                    : permissionModule.value() + ":delete";
            default -> permissionModule.value() + ":" + action;
        };
    }
}
