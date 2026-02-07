package com.project.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * 跨域配置
 * 生产环境应通过 cors.allowed-origins 配置项限制允许的源
 * 
 * @author 陈鸿昇
 * @since 2025-12-30
 */
@Configuration
public class CorsConfig {

    /**
     * 允许的跨域源列表，默认 * 允许所有（开发环境）
     * 生产环境应配置为具体域名，如：http://admin.example.com,http://m.example.com
     */
    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 允许的源（支持通配符和多域名配置）
        if ("*".equals(allowedOrigins)) {
            config.addAllowedOriginPattern("*");
        } else {
            List<String> origins = Arrays.asList(allowedOrigins.split(","));
            origins.forEach(origin -> config.addAllowedOriginPattern(origin.trim()));
        }

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
