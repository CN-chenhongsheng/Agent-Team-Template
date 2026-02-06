package com.project.core.enums;

import lombok.Getter;

/**
 * 用户状态枚举
 *
 * @author 陈鸿昇
 * @since 2025-01-05
 */
@Getter
public enum UserStatus {

    /**
     * 启用
     */
    ENABLED(1, "启用"),

    /**
     * 禁用
     */
    DISABLED(0, "禁用");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 状态描述
     */
    private final String description;

    UserStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据状态码获取枚举
     *
     * @param code 状态码
     * @return 用户状态枚举
     */
    public static UserStatus getByCode(Integer code) {
        for (UserStatus status : UserStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断是否启用
     *
     * @param code 状态码
     * @return true-启用 false-禁用
     */
    public static boolean isEnabled(Integer code) {
        return ENABLED.getCode().equals(code);
    }
}
