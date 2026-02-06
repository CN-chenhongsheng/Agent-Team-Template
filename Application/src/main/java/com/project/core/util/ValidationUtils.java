package com.project.core.util;

import com.project.core.constant.RegexConstants;
import cn.hutool.core.util.StrUtil;

/**
 * 数据格式校验工具类
 * 提供手机号、邮箱、身份证号等常用格式校验方法
 *
 * @author 陈鸿昇
 * @since 2026-02-04
 */
public class ValidationUtils {

    /**
     * 私有构造函数，防止实例化
     */
    private ValidationUtils() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 校验手机号格式（11位数字，1开头）
     *
     * @param phone 手机号
     * @return true 格式正确，false 格式错误或为空
     */
    public static boolean isValidMobile(String phone) {
        return StrUtil.isNotBlank(phone) && phone.matches(RegexConstants.MOBILE);
    }

    /**
     * 校验邮箱格式
     *
     * @param email 邮箱
     * @return true 格式正确，false 格式错误或为空
     */
    public static boolean isValidEmail(String email) {
        return StrUtil.isNotBlank(email) && email.matches(RegexConstants.EMAIL);
    }

    /**
     * 校验身份证号格式（18位，包含校验码）
     * 简单校验：18位，前17位数字，最后1位数字或X
     *
     * @param idCard 身份证号
     * @return true 格式正确，false 格式错误或为空
     */
    public static boolean isValidIdCard(String idCard) {
        if (StrUtil.isBlank(idCard) || idCard.length() != 18) {
            return false;
        }
        // 前17位必须是数字
        if (!idCard.substring(0, 17).matches("^\\d{17}$")) {
            return false;
        }
        // 最后1位是数字或X
        char last = Character.toUpperCase(idCard.charAt(17));
        return (last >= '0' && last <= '9') || last == 'X';
    }

    /**
     * 校验日期格式（YYYY-MM-DD）
     *
     * @param date 日期字符串
     * @return true 格式正确，false 格式错误或为空
     */
    public static boolean isValidDate(String date) {
        return StrUtil.isNotBlank(date) && date.matches(RegexConstants.DATE);
    }

    /**
     * 校验年份格式（YYYY）
     *
     * @param year 年份字符串
     * @return true 格式正确，false 格式错误或为空
     */
    public static boolean isValidYear(String year) {
        return StrUtil.isNotBlank(year) && year.matches(RegexConstants.YEAR);
    }
}
