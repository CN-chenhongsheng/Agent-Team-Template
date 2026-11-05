package com.project.backend.common.aspect;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 操作日志敏感字段脱敏测试
 */
@DisplayName("操作日志敏感字段脱敏测试")
class LogAspectTest {

    @Test
    @DisplayName("密码和令牌字段会被脱敏")
    void toSafeJson_masksSensitiveFields() throws Exception {
        LogAspect aspect = new LogAspect();
        Method method = LogAspect.class.getDeclaredMethod("toSafeJson", Object.class);
        method.setAccessible(true);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("username", "alice");
        payload.put("password", "plain-password");
        payload.put("oldPassword", "old-password");
        payload.put("newPassword", "new-password");
        payload.put("confirmPassword", "confirm-password");
        payload.put("nested", Map.of("token", "nested-token"));
        payload.put("items", List.of(Map.of("accessToken", "list-token")));

        String json = (String) method.invoke(aspect, payload);

        assertThat(json).contains("alice");
        assertThat(json).contains("******");
        assertThat(json).doesNotContain("plain-password");
        assertThat(json).doesNotContain("old-password");
        assertThat(json).doesNotContain("new-password");
        assertThat(json).doesNotContain("confirm-password");
        assertThat(json).doesNotContain("nested-token");
        assertThat(json).doesNotContain("list-token");
    }
}
