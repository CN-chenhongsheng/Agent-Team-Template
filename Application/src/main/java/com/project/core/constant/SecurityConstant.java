package com.project.core.constant;

/**
 * 安全相关常量
 *
 * @author 陈鸿昇
 * @since 2026-01-01
 */
public class SecurityConstant {

    /**
     * 免登录白名单路径（Ant 风格匹配规则）
     * 同时供 AuthInterceptor（登录校验）与 WebMvcConfig（拦截器排除路径）使用，避免两处配置漂移
     */
    public static final String[] PUBLIC_PATHS = {
            "/v1/auth/login",
            "/v1/auth/refresh",
            "/v1/auth/logout",
            "/doc.html",
            "/webjars/**",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/favicon.ico",
            "/error"
    };

    private SecurityConstant() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
