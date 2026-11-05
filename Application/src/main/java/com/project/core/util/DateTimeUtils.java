package com.project.core.util;

import com.project.core.exception.BusinessException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 日期时间工具类
 * 统一处理查询参数中的日期解析，避免非法日期直接抛出 500
 *
 * @author 陈鸿昇
 * @since 2026-11-05
 */
public final class DateTimeUtils {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateTimeUtils() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 解析日期为当天开始时间
     *
     * @param date 日期字符串（yyyy-MM-dd）
     * @return 当天 00:00:00
     */
    public static LocalDateTime parseDayStart(String date) {
        return parse(date + " 00:00:00");
    }

    /**
     * 解析日期为当天结束时间
     *
     * @param date 日期字符串（yyyy-MM-dd）
     * @return 当天 23:59:59
     */
    public static LocalDateTime parseDayEnd(String date) {
        return parse(date + " 23:59:59");
    }

    private static LocalDateTime parse(String value) {
        try {
            return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new BusinessException("日期格式不正确，应为 yyyy-MM-dd");
        }
    }
}
