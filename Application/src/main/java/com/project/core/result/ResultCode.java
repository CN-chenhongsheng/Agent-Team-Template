package com.project.core.result;

import lombok.Getter;

/**
 * 统一响应状态码枚举
 * 
 * @author 陈鸿昇
 * @since 2025-12-30
 */
@Getter
public enum ResultCode {

    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 失败
     */
    ERROR(500, "操作失败"),

    /**
     * 参数错误
     */
    PARAM_ERROR(400, "参数错误"),

    /**
     * 未授权
     */
    UNAUTHORIZED(401, "未登录或登录已过期"),

    /**
     * 无权限
     */
    FORBIDDEN(403, "无权限访问"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),

    /**
     * 业务异常
     */
    BUSINESS_ERROR(600, "业务异常"),

    /**
     * 数据已存在
     */
    DATA_EXIST(601, "数据已存在"),

    /**
     * 数据不存在
     */
    DATA_NOT_EXIST(602, "数据不存在");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 获取状态码
     */
    public Integer getCode() {
        return code;
    }

    /**
     * 获取提示信息
     */
    public String getMessage() {
        return message;
    }
}
