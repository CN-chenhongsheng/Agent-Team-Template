package com.project.core.enums;

import lombok.Getter;

/**
 * 菜单类型枚举
 *
 * @author 陈鸿昇
 * @since 2025-01-05
 */
@Getter
public enum MenuType {

    /**
     * 目录
     */
    DIRECTORY("directory", "目录"),

    /**
     * 菜单
     */
    MENU("menu", "菜单"),

    /**
     * 按钮
     */
    BUTTON("button", "按钮");

    /**
     * 类型代码
     */
    private final String code;

    /**
     * 类型描述
     */
    private final String description;

    MenuType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据类型代码获取枚举
     *
     * @param code 类型代码
     * @return 菜单类型枚举
     */
    public static MenuType getByCode(String code) {
        for (MenuType type : MenuType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
