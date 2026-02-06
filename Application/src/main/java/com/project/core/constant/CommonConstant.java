package com.project.core.constant;

/**
 * 通用常量
 *
 * @author 陈鸿昇
 * @since 2025-01-05
 */
public class CommonConstant {

    /**
     * 成功标记
     */
    public static final Integer SUCCESS = 200;

    /**
     * 失败标记
     */
    public static final Integer ERROR = 500;

    /**
     * 登录成功标记
     */
    public static final String LOGIN_SUCCESS = "Success";

    /**
     * 登录失败标记
     */
    public static final String LOGIN_FAIL = "Error";

    /**
     * 验证码Redis key
     */
    public static final String CAPTCHA_CODE_KEY = "captcha_codes:";

    /**
     * 登录用户Redis key
     */
    public static final String LOGIN_TOKEN_KEY = "login_tokens:";

    /**
     * 防重提交Redis key
     */
    public static final String REPEAT_SUBMIT_KEY = "repeat_submit:";

    /**
     * 限流Redis key
     */
    public static final String RATE_LIMIT_KEY = "rate_limit:";

    /**
     * 默认页码
     */
    public static final Integer DEFAULT_CURRENT = 1;

    /**
     * 默认每页条数
     */
    public static final Integer DEFAULT_SIZE = 10;

    /**
     * 逻辑删除 - 未删除
     */
    public static final Integer NOT_DELETED = 0;

    /**
     * 逻辑删除 - 已删除
     */
    public static final Integer DELETED = 1;

    /**
     * UTF-8 字符集
     */
    public static final String UTF8 = "UTF-8";

    /**
     * GBK 字符集
     */
    public static final String GBK = "GBK";

    /**
     * http请求
     */
    public static final String HTTP = "http://";

    /**
     * https请求
     */
    public static final String HTTPS = "https://";

    private CommonConstant() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
