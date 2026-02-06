package com.project.core.context;

import lombok.Data;

/**
 * 用户上下文工具类
 * 使用 ThreadLocal 存储当前登录用户信息
 * 
 * @author 陈鸿昇
 * @since 2025-12-31
 */
public class UserContext {

    private static final ThreadLocal<LoginUser> USER_CONTEXT = new ThreadLocal<>();

    /**
     * 设置当前登录用户
     */
    public static void setUser(LoginUser user) {
        USER_CONTEXT.set(user);
    }

    /**
     * 获取当前登录用户
     */
    public static LoginUser getUser() {
        return USER_CONTEXT.get();
    }

    /**
     * 获取当前登录用户ID
     */
    public static Long getUserId() {
        LoginUser user = getUser();
        return user != null ? user.getUserId() : null;
    }

    /**
     * 获取当前登录用户
     */
    public static String getUsername() {
        LoginUser user = getUser();
        return user != null ? user.getUsername() : null;
    }

    /**
     * 清除当前线程的用户信息
     * 防止内存泄漏
     */
    public static void clear() {
        USER_CONTEXT.remove();
    }

    /**
     * 登录用户信息
     */
    @Data
    public static class LoginUser {
        /**
         * 用户ID
         */
        private Long userId;

        /**
         * 用户名
         */
        private String username;

        /**
         * 昵称
         */
        private String nickname;

        /**
         * 头像
         */
        private String avatar;
    }
}
