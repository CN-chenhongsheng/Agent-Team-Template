package com.project.core.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 
 * @author 陈鸿昇
 * @since 2025-01-01
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    /**
     * 操作模块
     */
    String title() default "";

    /**
     * 业务类型：0其它 1新增 2修改 3删除
     */
    int businessType() default 0;

    /**
     * 是否保存请求参数
     */
    boolean saveRequestData() default true;

    /**
     * 是否保存响应参数
     */
    boolean saveResponseData() default true;
}
