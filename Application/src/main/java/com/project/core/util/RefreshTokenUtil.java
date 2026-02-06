package com.project.core.util;

import cn.dev33.satoken.stp.StpLogic;
import lombok.extern.slf4j.Slf4j;

/**
 * Refresh Token 工具类
 * 使用独立的 StpLogic 管理 Refresh Token
 * 
 * @author 陈鸿昇
 * @since 2025-01-01
 */
@Slf4j
public class RefreshTokenUtil {

    /**
     * Refresh Token 的 StpLogic 实例
     * 使用独立的 token-name 和更长的有效期
     */
    private static final StpLogic REFRESH_TOKEN_LOGIC = new StpLogic("refresh-token");

    /**
     * Refresh Token 有效期：7 天（604800 秒）
     */
    private static final long REFRESH_TOKEN_TIMEOUT = 7 * 24 * 60 * 60;

    /**
     * 登录并生成 Refresh Token
     * 
     * @param userId 用户ID
     * @return Refresh Token 值
     */
    public static String login(Long userId) {
        // 使用 login 方法的重载版本，指定超时时间为 7 天
        REFRESH_TOKEN_LOGIC.login(userId, REFRESH_TOKEN_TIMEOUT);
        String token = REFRESH_TOKEN_LOGIC.getTokenValue();
        log.debug("生成 Refresh Token，用户ID: {}, Token: {}", userId, token);
        return token;
    }

    /**
     * 验证 Refresh Token 并获取用户ID
     * 
     * @param token Refresh Token
     * @return 用户ID，如果token无效返回null
     */
    public static Long getLoginIdByToken(String token) {
        try {
            Object loginId = REFRESH_TOKEN_LOGIC.getLoginIdByToken(token);
            if (loginId == null) {
                return null;
            }
            return Long.valueOf(loginId.toString());
        } catch (Exception e) {
            log.warn("Refresh Token 验证失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 验证 Refresh Token 是否有效
     * 
     * @param token Refresh Token
     * @return 是否有效
     */
    public static boolean isValid(String token) {
        try {
            return REFRESH_TOKEN_LOGIC.getLoginIdByToken(token) != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 登出（清除 Refresh Token）
     * 
     * @param token Refresh Token
     */
    public static void logout(String token) {
        try {
            REFRESH_TOKEN_LOGIC.logoutByTokenValue(token);
            log.debug("清除 Refresh Token: {}", token);
        } catch (Exception e) {
            log.warn("清除 Refresh Token 失败: {}", e.getMessage());
        }
    }

    /**
     * 根据用户ID登出所有 Refresh Token
     * 
     * @param userId 用户ID
     */
    public static void logoutByUserId(Long userId) {
        try {
            REFRESH_TOKEN_LOGIC.logout(userId);
            log.debug("清除用户所有 Refresh Token，用户ID: {}", userId);
        } catch (Exception e) {
            log.warn("清除用户 Refresh Token 失败: {}", e.getMessage());
        }
    }

    /**
     * 获取 Refresh Token 值
     * 
     * @return Refresh Token 值
     */
    public static String getTokenValue() {
        return REFRESH_TOKEN_LOGIC.getTokenValue();
    }
}
