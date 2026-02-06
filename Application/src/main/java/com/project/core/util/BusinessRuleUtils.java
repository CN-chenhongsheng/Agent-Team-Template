package com.project.core.util;

import com.project.core.context.UserContext;
import com.project.core.exception.BusinessException;

import java.util.Objects;

/**
 * 业务规则工具
 * 提供通用的业务规则验证方法
 *
 * @author 陈鸿昇
 * @since 2025-01-01
 */
public class BusinessRuleUtils {

    /**
     * 超级管理员用户名
     */
    public static final String SUPER_ADMIN_USERNAME = "superAdmin";

    /**
     * 超级管理员角色编码
     */
    public static final String SUPER_ADMIN_ROLE_CODE = "SUPER_ADMIN";

    /**
     * 停用状态
     */
    private static final int STATUS_DISABLED = 0;

    /**
     * 判断用户名是否是超级管理员
     *
     * @param username 用户名
     * @return 是否是超级管理员
     */
    public static boolean isSuperAdminUser(String username) {
        return SUPER_ADMIN_USERNAME.equals(username);
    }

    /**
     * 判断角色编码是否是超级管理员
     *
     * @param roleCode 角色编码
     * @return 是否是超级管理员角色
     */
    public static boolean isSuperAdminRole(String roleCode) {
        return SUPER_ADMIN_ROLE_CODE.equals(roleCode);
    }

    /**
     * 验证用户名是否是超级管理员，如果是则抛出异常
     *
     * @param username 用户名
     * @param errorMessage 错误消息（如果为null则使用默认消息）
     * @throws BusinessException 如果是超级管理员
     */
    public static void validateNotSuperAdminUser(String username, String errorMessage) {
        if (isSuperAdminUser(username)) {
            throw new BusinessException(Objects.requireNonNullElse(errorMessage, "不能操作超级管理员"));
        }
    }

    /**
     * 验证角色编码是否是超级管理员，如果是则抛出异常
     *
     * @param roleCode 角色编码
     * @param errorMessage 错误消息（如果为null则使用默认消息）
     * @throws BusinessException 如果是超级管理员角色
     */
    public static void validateNotSuperAdminRole(String roleCode, String errorMessage) {
        if (isSuperAdminRole(roleCode)) {
            throw new BusinessException(Objects.requireNonNullElse(errorMessage, "不能操作超级管理员角色"));
        }
    }

    /**
     * 验证超级管理员角色是否允许停用
     *
     * @param roleCode 角色编码
     * @param status 状态：1正常 0停用
     * @throws BusinessException 如果是超级管理员且要停用
     */
    public static void validateSuperAdminRoleStatus(String roleCode, Integer status) {
        if (isSuperAdminRole(roleCode) && isDisabledStatus(status)) {
            throw new BusinessException("超级管理员角色不允许停用");
        }
    }

    /**
     * 验证超级管理员用户是否允许停用
     *
     * @param username 用户名
     * @param status 状态：1正常 0停用
     * @throws BusinessException 如果是超级管理员且要停用
     */
    public static void validateSuperAdminUserStatus(String username, Integer status) {
        if (isSuperAdminUser(username) && isDisabledStatus(status)) {
            throw new BusinessException("超级管理员不允许停用");
        }
    }

    /**
     * 验证用户是否可以删除
     *
     * @param userId 用户ID
     * @param username 用户名
     * @throws BusinessException 如果用户不能删除（超级管理员或自己）
     */
    public static void validateUserDeletion(Long userId, String username) {
        validateNotSuperAdminUser(username, "不能删除超级管理员");

        Long currentUserId = UserContext.getUserId();
        if (Objects.equals(currentUserId, userId)) {
            throw new BusinessException("不能删除当前登录用户");
        }
    }

    /**
     * 判断状态是否为停用
     *
     * @param status 状态
     * @return 是否为停用状态
     */
    private static boolean isDisabledStatus(Integer status) {
        return status != null && status == STATUS_DISABLED;
    }
}
