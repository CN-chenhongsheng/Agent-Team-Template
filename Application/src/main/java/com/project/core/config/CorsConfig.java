package com.project.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置
 * 
 * @author 陈鸿昇
 * @since 2025-12-30
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 允许的源（使 OriginPattern 支持通配符，同时允许 credentials）
        config.addAllowedOriginPattern("*");

        // 允许的请求头
        config.addAllowedHeader("*");

        // 允许的请求方法（包括 GET，SSE 使用 GET）
        config.addAllowedMethod("*");

        // 是否允许携带凭证
        config.setAllowCredentials(true);

        // 暴露的响应头（SSE 需要）
        config.addExposedHeader("*");

        // 预检请求的有效期，单位为秒
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 确保所有路径都应用 CORS 配置，包括 SSE 路径
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
