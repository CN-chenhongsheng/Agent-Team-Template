package com.project.backend.system.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.backend.system.dto.LoginDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 系统鉴权集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("系统鉴权集成测试")
class SystemAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String loginAndGetToken(String username, String password) throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername(username);
        loginDTO.setPassword(password);

        String response = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode node = objectMapper.readTree(response);
        return node.path("data").path("token").asText();
    }

    @Test
    @DisplayName("超管可访问用户列表")
    void superAdminCanViewUsers() throws Exception {
        String token = loginAndGetToken("superAdmin", "admin123");

        mockMvc.perform(get("/v1/system/user/page")
                        .header("Authorization", "Bearer " + token)
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("未登录返回401")
    void unauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/v1/system/user/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("仅有查看权限时删除用户返回403")
    void viewerCannotDeleteUser() throws Exception {
        String adminToken = loginAndGetToken("superAdmin", "admin123");

        mockMvc.perform(put("/v1/system/user/2/permissions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1,2,3,4]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 权限变更后会踢出会话，需重新登录
        String viewerToken = loginAndGetToken("testuser", "admin123");

        mockMvc.perform(delete("/v1/system/user/999")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("登录后可获取权限列表")
    void loginReturnsPermissions() throws Exception {
        String token = loginAndGetToken("superAdmin", "admin123");

        String response = mockMvc.perform(get("/v1/auth/userinfo")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode permissions = objectMapper.readTree(response).path("data").path("permissions");
        assertThat(permissions.isArray()).isTrue();
        assertThat(permissions.size()).isGreaterThan(0);
    }
}
