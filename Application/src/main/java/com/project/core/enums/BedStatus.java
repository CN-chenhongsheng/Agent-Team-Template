package com.project.core.enums;

import lombok.Getter;

/**
 * 床位状态枚举
 *
 * @author 陈鸿昇
 * @since 2025-01-05
 */
@Getter
public enum BedStatus {

    /**
     * 空闲
     */
    AVAILABLE(1, "空闲"),

    /**
     * 已占用
     */
    OCCUPIED(2, "占用"),

    /**
     * 维护中
     */
    MAINTENANCE(3, "维护中");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 状态描述
     */
    private final String description;

    BedStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据状态码获取枚举
     *
     * @param code 状态码
     * @return 床位状态枚举
     */
    public static BedStatus getByCode(Integer code) {
        for (BedStatus status : BedStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断是否可用
     *
     * @param code 状态码
     * @return true-可用 false-不可用
     */
    public static boolean isAvailable(Integer code) {
        return AVAILABLE.getCode().equals(code);
    }
}
