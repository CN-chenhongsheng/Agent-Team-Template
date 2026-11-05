package com.project.core.exception;

import com.project.core.i18n.I18nUtils;
import com.project.core.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常
 * 
 * @author 陈鸿昇
 * @since 2025-12-30
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误信息
     */
    private String message;

    public BusinessException(String message) {
        this(ResultCode.BUSINESS_ERROR.getCode(), message);
    }

    public BusinessException(Integer code, String message) {
        super(I18nUtils.translate(message));
        this.code = code;
        this.message = I18nUtils.translate(message);
    }

    public BusinessException(ResultCode resultCode) {
        this(resultCode.getCode(), resultCode.getMessage());
    }

    @Override
    public String getMessage() {
        return message;
    }
}
