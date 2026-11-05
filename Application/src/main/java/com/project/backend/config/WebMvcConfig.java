package com.project.backend.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import com.project.backend.config.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.project.core.constant.SecurityConstant;
import com.project.core.util.FileUtils;

/**
 * Web MVC 配置
 * 注册拦截器与静态资源映射
 *
 * @author 陈鸿昇
 * @since 2025-12-31
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${file.public-path:/v1/common/files}")
    private String publicPath;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(SecurityConstant.PUBLIC_PATHS)
                .order(1);

        // 注解鉴权（@SaCheckPermission），登录校验由 AuthInterceptor 负责
        registry.addInterceptor(new SaInterceptor(handle -> {}))
                .addPathPatterns("/**")
                .excludePathPatterns(SecurityConstant.PUBLIC_PATHS)
                .order(2);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 只暴露 public 目录下的图片等公开文件，导入文件等私有文件不提供静态访问
        String absoluteUploadDir = FileUtils.resolveUploadDir(uploadDir).toString().replace("\\", "/");
        registry.addResourceHandler(publicPath + "/uploads/public/**")
                .addResourceLocations("file:" + absoluteUploadDir + "/uploads/public/");
    }
}
