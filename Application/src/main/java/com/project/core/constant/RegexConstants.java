package com.project.core.constant;

/**
 * 正则表达式常量类
 * 统一管理项目中使用的正则表达式，避免重复定义和逻辑分叉
 *
 * @author 陈鸿昇
 * @since 2026-02-04
 */
public class RegexConstants {

    /**
     * 私有构造函数，防止实例化
     */
    private RegexConstants() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 手机号正则表达式（11位数字，1开头，第二位3-9）
     */
    public static final String MOBILE = "^1[3-9]\\d{9}$";

    /**
     * 邮箱正则表达式
     */
    public static final String EMAIL = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";

    /**
     * 用户名正则表达式（4-20位字母、数字、下划线）
     */
    public static final String USERNAME = "^[a-zA-Z0-9_]{4,20}$";

    /**
     * 日期格式正则表达式（YYYY-MM-DD）
     */
    public static final String DATE = "^\\d{4}-\\d{2}-\\d{2}$";

    /**
     * 年份格式正则表达式（YYYY）
     */
    public static final String YEAR = "^\\d{4}$";

    /**
     * 安全文件名正则表达式（字母、数字、点、下划线、连字符、中文字符）
     */
    public static final String SAFE_FILENAME = "^[a-zA-Z0-9._\\-\\u4e00-\\u9fa5]+$";
}
