package com.project.core.constant;

/**
 * 缓存常量
 *
 * @author 陈鸿昇
 * @since 2025-01-05
 */
public class CacheConstant {

    /**
     * 缓存命名空间
     */
    public static final String CACHE_NAMESPACE = "project:";

    /**
     * 登录用户缓存
     * 缓存时间30分钟
     */
    public static final String LOGIN_USER_KEY = CACHE_NAMESPACE + "login_user:";
    public static final long LOGIN_USER_TTL = 30;

    private CacheConstant() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
