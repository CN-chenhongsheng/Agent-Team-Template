package com.project.core.annotation;

import java.lang.annotation.*;

/**
 * 接口限流注解
 * 
 * @author 陈鸿昇
 * @since 2025-01-01
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流key，支持SpEL表达式
     * 默认为方法名
     */
    String key() default "";

    /**
     * 时间窗口（秒）
     */
    int time() default 60;

    /**
     * 时间窗口内允许的请求次数
     */
    int count() default 100;

    /**
     * 限流提示消息
     */
    String message() default "请求过于频繁，请稍后再试";
}
