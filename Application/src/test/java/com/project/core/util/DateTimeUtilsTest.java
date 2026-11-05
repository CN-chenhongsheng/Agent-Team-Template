package com.project.core.util;

import com.project.core.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 日期时间工具单元测试
 */
@DisplayName("日期时间工具单元测试")
class DateTimeUtilsTest {

    @Test
    @DisplayName("日期解析为当天开始和结束时间")
    void parseDayStartAndEnd() {
        assertThat(DateTimeUtils.parseDayStart("2026-11-05"))
                .isEqualTo(LocalDateTime.of(2026, 11, 5, 0, 0, 0));
        assertThat(DateTimeUtils.parseDayEnd("2026-11-05"))
                .isEqualTo(LocalDateTime.of(2026, 11, 5, 23, 59, 59));
    }

    @Test
    @DisplayName("非法日期抛出业务异常")
    void invalidDateThrowsBusinessException() {
        assertThatThrownBy(() -> DateTimeUtils.parseDayStart("2026/11/05"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("日期格式不正确");
    }
}
