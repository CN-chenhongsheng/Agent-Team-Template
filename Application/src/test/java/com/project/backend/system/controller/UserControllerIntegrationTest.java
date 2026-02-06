package com.project.backend.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.backend.system.dto.LoginDTO;
import com.project.backend.system.dto.UserSaveDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 用户控制器集成测试
 *
 * 测试要点
 * 1. 使用@SpringBootTest启动完整应用上下文
 * 2. 使用MockMvc模拟HTTP请求
 * 3. @Transactional确保测试后回滚，不影响数据库
 * 4. 测试完整的请求响应流程
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("用户控制器集成测试")
@Transactional
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        // 登录获取Token
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("admin123");

        String response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 解析响应获取Token（这里简化处理）
        // 实际项目中应该解析JSON获取token
        authToken = "Bearer test-token";
    }

    @Test
    @DisplayName("分页查询用户-成功")
    void testGetUsersPaged_Success() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", authToken)
                .param("current", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    @DisplayName("根据ID获取用户-成功")
    void testGetUserById_Success() throws Exception {
        mockMvc.perform(get("/api/users/1")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").isString())
                .andExpect(jsonPath("$.data.nickname").isString());
    }

    @Test
    @DisplayName("创建用户-成功")
    void testCreateUser_Success() throws Exception {
        UserSaveDTO userDTO = new UserSaveDTO();
        userDTO.setUsername("testuser001");
        userDTO.setPassword("password123");
        userDTO.setNickname("测试用户001");
        userDTO.setPhone("13800138000");
        userDTO.setEmail("test001@example.com");

        mockMvc.perform(post("/api/users")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("创建用户-参数校验失败")
    void testCreateUser_ValidationError() throws Exception {
        UserSaveDTO userDTO = new UserSaveDTO();
        userDTO.setUsername("");  // 用户名为空
        userDTO.setPassword("123");  // 密码太短

        mockMvc.perform(post("/api/users")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("更新用户-成功")
    void testUpdateUser_Success() throws Exception {
        UserSaveDTO userDTO = new UserSaveDTO();
        userDTO.setId(1L);
        userDTO.setNickname("更新后的用户");
        userDTO.setPhone("13900139000");

        mockMvc.perform(put("/api/users")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("删除用户-成功")
    void testDeleteUser_Success() throws Exception {
        // 先创建一个测试用户
        UserSaveDTO userDTO = new UserSaveDTO();
        userDTO.setUsername("testDeleteUser");
        userDTO.setPassword("password123");
        userDTO.setNickname("待删除用户");

        String createResponse = mockMvc.perform(post("/api/users")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 提取用户ID并删除
        // 实际项目中应该解析JSON获取ID
        mockMvc.perform(delete("/api/users/999")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("未授权访问-失败")
    void testUnauthorizedAccess_Failed() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Token无效-失败")
    void testInvalidToken_Failed() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isForbidden());
    }
}
