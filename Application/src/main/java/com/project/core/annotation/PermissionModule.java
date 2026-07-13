package com.project.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注 CRUD 控制器对应的权限模块前缀
 *
 * @author 陈鸿昇
 * @since 2026-07-10
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PermissionModule {

    /**
     * 默认权限前缀，例如 system:user
     */
    String value();

    /**
     * 查询权限，默认使用 {value}:view
     */
    String view() default "";

    /**
     * 新增权限，默认使用 {value}:add
     */
    String add() default "";

    /**
     * 编辑权限，默认使用 {value}:edit
     */
    String edit() default "";

    /**
     * 删除权限，默认使用 {value}:delete
     */
    String delete() default "";
}
