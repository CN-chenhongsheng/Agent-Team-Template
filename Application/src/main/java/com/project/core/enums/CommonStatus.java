package com.project.core.enums;

import lombok.Getter;

/**
 * 通用状态枚举
 *
 * @author 陈鸿昇
 * @since 2025-01-05
 */
@Getter
public enum CommonStatus {

    /**
     * 启用/正常/成功
     */
    ENABLED(1, "启用"),

    /**
     * 禁用/删除/失败
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

    CommonStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据状态码获取枚举
     *
     * @param code 状态码
     * @return 通用状态枚举
     */
    public static CommonStatus getByCode(Integer code) {
        for (CommonStatus status : CommonStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
