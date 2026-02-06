package com.project.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置
 * 配置 Sa-Token 的拦截器，但实际认证逻辑由自定义 AuthInterceptor 处理
 * 
 * @author 陈鸿昇
 * @since 2025-01-01
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 注册 Sa-Token 拦截器
     * 注意：这里不启用 Sa-Token 的自动认证，因为我们使用自定义的 AuthInterceptor
     * 来处理 Bearer Token 格式
     */
    @Override
    public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
        // 不启用 Sa-Token 的拦截器，使用自定义 AuthInterceptor
        // 这样可以完全控制 Bearer Token 的解析和验证逻辑
    }
}
