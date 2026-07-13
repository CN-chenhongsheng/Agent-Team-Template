package com.project.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注方法对应的权限动作，与 {@link PermissionModule} 配合使用
 *
 * @author 陈鸿昇
 * @since 2026-07-10
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PermissionAction {

    /**
     * 权限动作：view / add / edit / delete，对应 {@link PermissionModule} 的四个属性
     */
    String value();
}
