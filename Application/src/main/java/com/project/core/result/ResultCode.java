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
    DATA_NOT_EXIST(602, "数据不存在"),

    /**
     * 数据校验失败
     */
    DATA_VALIDATION_ERROR(603, "数据校验失败"),

    /**
     * 操作不允许
     */
    OPERATION_NOT_ALLOWED(604, "操作不允许"),

    /**
     * 资源已被锁定
     */
    RESOURCE_LOCKED(605, "资源已被锁定"),

    /**
     * 请求过于频繁
     */
    RATE_LIMIT_EXCEEDED(606, "请求过于频繁"),

    /**
     * 文件上传失败
     */
    FILE_UPLOAD_ERROR(607, "文件上传失败"),

    /**
     * 请求方法不支持
     */
    METHOD_NOT_ALLOWED(405, "请求方法不支持");

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

    /**
     * 根据 code 查找枚举值
     *
     * @param code 状态码
     * @return 对应的枚举值，未找到返回 null
     */
    public static ResultCode getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ResultCode rc : values()) {
            if (rc.code.equals(code)) {
                return rc;
            }
        }
        return null;
    }
}
