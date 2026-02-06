package com.project.backend.system.service;

import com.project.core.context.UserContext;
import com.project.backend.system.dto.ChangePasswordDTO;
import com.project.backend.system.dto.UserSaveDTO;
import com.project.backend.system.entity.User;
import com.project.backend.system.mapper.UserMapper;
import com.project.backend.system.service.impl.UserServiceImpl;
import com.project.backend.system.vo.UserVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 用户服务测试
 *
 * 测试要点
 * 1. 使用Mockito模拟依赖
 * 2. 测试正常场景和异常场景
 * 3. 验证方法调用次数
 * 4. 使用AssertJ进行断言
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务测试")
public class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserSaveDTO userSaveDTO;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("$2a$10$encodedPassword");
        testUser.setNickname("测试用户");
        testUser.setPhone("13800138000");
        testUser.setEmail("test@example.com");
        testUser.setStatus(1);
        testUser.setCreateTime(LocalDateTime.now());
        testUser.setUpdateTime(LocalDateTime.now());
        testUser.setDeleted(0);

        userSaveDTO = new UserSaveDTO();
        userSaveDTO.setUsername("testuser");
        userSaveDTO.setPassword("password123");
        userSaveDTO.setNickname("测试用户");
        userSaveDTO.setPhone("13800138000");
        userSaveDTO.setEmail("test@example.com");
        userSaveDTO.setStatus(1);
    }

    @AfterEach
    void tearDown() {
        // 清理 UserContext，防止测试之间的干扰
        UserContext.clear();
    }

    @Test
    @DisplayName("创建用户-成功")
    void testCreateUser_Success() {
        // Given
        // 使用 spy mock count 方法（因 count 是 ServiceImpl 的方法）
        UserServiceImpl spyService = org.mockito.Mockito.spy(userService);
        doReturn(0L).when(spyService).count(any());
        when(userMapper.insert(any(User.class))).thenReturn(1);

        // When
        boolean result = spyService.saveUser(userSaveDTO);

        // Then
        assertThat(result).isTrue();
        verify(spyService, times(1)).count(any());
        verify(userMapper, times(1)).insert(any(User.class));
    }

    @Test
    @DisplayName("创建用户-用户名已存在")
    void testCreateUser_UsernameExists() {
        // Given
        // 使用 spy mock count 方法返回 1（表示用户名已存在）
        UserServiceImpl spyService = org.mockito.Mockito.spy(userService);
        doReturn(1L).when(spyService).count(any());

        // When & Then
        assertThatThrownBy(() -> spyService.saveUser(userSaveDTO))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("用户名已存在");

        verify(spyService, times(1)).count(any());
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    @DisplayName("根据ID获取用户-成功")
    void testGetDetailById_Success() {
        // Given
        when(userMapper.selectById(1L)).thenReturn(testUser);

        // When
        UserVO userVO = userService.getDetailById(1L);

        // Then
        assertThat(userVO).isNotNull();
        assertThat(userVO.getUsername()).isEqualTo("testuser");
        assertThat(userVO.getNickname()).isEqualTo("测试用户");
        verify(userMapper, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("根据ID获取用户-用户不存在")
    void testGetDetailById_NotFound() {
        // Given
        when(userMapper.selectById(999L)).thenReturn(null);

        // When
        UserVO userVO = userService.getDetailById(999L);

        // Then
        assertThat(userVO).isNull();
        verify(userMapper, times(1)).selectById(999L);
    }

    @Test
    @DisplayName("更新用户-成功")
    void testUpdateUser_Success() {
        // Given
        UserSaveDTO updateUserDTO = new UserSaveDTO();
        updateUserDTO.setId(1L);
        updateUserDTO.setNickname("更新后的用户");
        updateUserDTO.setPhone("13900139000");

        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // When
        boolean result = userService.saveUser(updateUserDTO);

        // Then
        assertThat(result).isTrue();
        verify(userMapper, times(1)).selectById(1L);
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    @DisplayName("删除用户-成功")
    void testDeleteUser_Success() {
        // Given
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userMapper.deleteById(1L)).thenReturn(1);

        // When
        boolean result = userService.deleteUser(1L);

        // Then
        assertThat(result).isTrue();
        verify(userMapper, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("分页查询用户-成功")
    void testPageList_Success() {
        // Given
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setNickname("用户2");

        when(userMapper.selectList(any())).thenReturn(Arrays.asList(testUser, user2));

        // When
        // UserQueryDTO需要根据实际情况创建
        // List<UserVO> users = userService.pageList(queryDTO);

        // 这里暂时注释掉，因为没有UserQueryDTO的具体实现
        // TODO: 实现分页查询测试
    }

    @Test
    @DisplayName("修改密码-成功")
    void testChangePassword_Success() {
        // Given
        String oldPassword = "password123";
        String newPassword = "newPassword456";

        // 设置 UserContext
        UserContext.LoginUser loginUser = new UserContext.LoginUser();
        loginUser.setUserId(1L);
        UserContext.setUser(loginUser);

        // 使用真实 BCrypt 加密旧密码（Hutool BCrypt）
        testUser.setPassword(cn.hutool.crypto.digest.BCrypt.hashpw(oldPassword));

        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // 创建 ChangePasswordDTO
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setOldPassword(oldPassword);
        changePasswordDTO.setNewPassword(newPassword);
        changePasswordDTO.setConfirmPassword(newPassword);

        // When
        boolean result = userService.changeCurrentUserPassword(changePasswordDTO);

        // Then
        assertThat(result).isTrue();
        verify(userMapper, times(1)).selectById(1L);
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    @DisplayName("修改密码-旧密码错误")
    void testChangePassword_OldPasswordIncorrect() {
        // Given
        String correctPassword = "password123";
        String wrongPassword = "wrongPassword";
        String newPassword = "newPassword456";

        // 设置 UserContext
        UserContext.LoginUser loginUser = new UserContext.LoginUser();
        loginUser.setUserId(1L);
        UserContext.setUser(loginUser);

        // 使用真实 BCrypt 加密正确密码（Hutool BCrypt）
        testUser.setPassword(cn.hutool.crypto.digest.BCrypt.hashpw(correctPassword));

        when(userMapper.selectById(1L)).thenReturn(testUser);

        // 创建 ChangePasswordDTO（使用错误的旧密码）
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setOldPassword(wrongPassword);
        changePasswordDTO.setNewPassword(newPassword);
        changePasswordDTO.setConfirmPassword(newPassword);

        // When & Then
        assertThatThrownBy(() -> userService.changeCurrentUserPassword(changePasswordDTO))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("当前密码错误");

        verify(userMapper, times(1)).selectById(1L);
        verify(userMapper, never()).updateById(any(User.class));
    }
}
