package com.project.backend.system.service;

import com.project.core.context.UserContext;
import com.project.core.exception.BusinessException;
import com.project.backend.system.dto.ChangePasswordDTO;
import com.project.backend.system.dto.UserSaveDTO;
import com.project.backend.system.entity.User;
import com.project.backend.system.mapper.MenuMapper;
import com.project.backend.system.mapper.RoleMapper;
import com.project.backend.system.mapper.RoleMenuMapper;
import com.project.backend.system.mapper.UserMapper;
import com.project.backend.system.mapper.UserMenuMapper;
import com.project.backend.system.mapper.UserRoleMapper;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

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
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务测试")
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRoleMapper userRoleMapper;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private UserMenuMapper userMenuMapper;

    @Mock
    private RoleMenuMapper roleMenuMapper;

    @Mock
    private MenuMapper menuMapper;

    @Mock
    private UserOnlineService userOnlineService;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserSaveDTO userSaveDTO;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);
        ReflectionTestUtils.setField(userService, "defaultPassword", "123456");

        testUser = new User();
        testUser.setId(2L);
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
        userSaveDTO.setUsername("newuser");
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
        UserServiceImpl spyService = org.mockito.Mockito.spy(userService);
        ReflectionTestUtils.setField(spyService, "baseMapper", userMapper);
        doReturn(0L).when(spyService).count(any());
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(100L);
            return 1;
        });

        boolean result = spyService.saveUser(userSaveDTO);

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
        when(userMapper.selectById(2L)).thenReturn(testUser);
        when(roleMapper.selectRoleIdsByUserId(2L)).thenReturn(Collections.emptyList());
        when(roleMapper.selectRoleNamesByUserId(2L)).thenReturn(Collections.emptyList());
        when(userOnlineService.isUserOnline(2L)).thenReturn(false);

        UserVO userVO = userService.getDetailById(2L);

        assertThat(userVO).isNotNull();
        assertThat(userVO.getUsername()).isEqualTo("testuser");
        verify(userMapper, times(1)).selectById(2L);
    }

    @Test
    @DisplayName("根据ID获取用户-用户不存在")
    void testGetDetailById_NotFound() {
        when(userMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> userService.getDetailById(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户不存在");
    }

    @Test
    @DisplayName("更新用户-成功")
    void testUpdateUser_Success() {
        UserSaveDTO updateUserDTO = new UserSaveDTO();
        updateUserDTO.setId(2L);
        updateUserDTO.setUsername("testuser");
        updateUserDTO.setNickname("更新后的用户");
        updateUserDTO.setPhone("13900139000");

        UserServiceImpl spyService = org.mockito.Mockito.spy(userService);
        ReflectionTestUtils.setField(spyService, "baseMapper", userMapper);
        doReturn(0L).when(spyService).count(any());
        when(userMapper.selectById(2L)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        boolean result = spyService.saveUser(updateUserDTO);

        assertThat(result).isTrue();
        verify(userMapper, times(1)).selectById(2L);
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    @DisplayName("删除用户-成功")
    void testDeleteUser_Success() {
        when(userMapper.selectById(2L)).thenReturn(testUser);
        when(roleMapper.selectRoleCodesByUserId(2L)).thenReturn(Collections.emptyList());
        when(userRoleMapper.delete(any())).thenReturn(1);
        when(userMenuMapper.delete(any())).thenReturn(1);

        UserServiceImpl spyService = org.mockito.Mockito.spy(userService);
        doReturn(true).when(spyService).removeById(2L);

        boolean result = spyService.deleteUser(2L);

        assertThat(result).isTrue();
        verify(spyService, times(1)).removeById(2L);
    }

    @Test
    @DisplayName("不能删除超级管理员用户")
    void deleteSuperAdminUserShouldFail() {
        User superAdmin = new User();
        superAdmin.setId(1L);
        superAdmin.setUsername("superAdmin");

        when(userMapper.selectById(1L)).thenReturn(superAdmin);
        when(roleMapper.selectRoleCodesByUserId(1L)).thenReturn(List.of("SUPER_ADMIN"));

        assertThatThrownBy(() -> userService.deleteUser(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不能删除超级管理员");

        verify(userMapper, never()).deleteById(any(User.class));
    }

    @Test
    @DisplayName("修改密码-成功")
    void testChangePassword_Success() {
        // Given
        String oldPassword = "password123";
        String newPassword = "newPassword456";

        // 设置 UserContext
        UserContext.LoginUser loginUser = new UserContext.LoginUser();
        loginUser.setUserId(2L);
        UserContext.setUser(loginUser);

        testUser.setPassword(cn.hutool.crypto.digest.BCrypt.hashpw(oldPassword));

        when(userMapper.selectById(2L)).thenReturn(testUser);
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
        verify(userMapper, times(1)).selectById(2L);
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    @DisplayName("修改密码-旧密码错误")
    void testChangePassword_OldPasswordIncorrect() {
        String correctPassword = "password123";
        String wrongPassword = "wrongPassword";
        String newPassword = "newPassword456";

        UserContext.LoginUser loginUser = new UserContext.LoginUser();
        loginUser.setUserId(2L);
        UserContext.setUser(loginUser);

        testUser.setPassword(cn.hutool.crypto.digest.BCrypt.hashpw(correctPassword));

        when(userMapper.selectById(2L)).thenReturn(testUser);

        // 创建 ChangePasswordDTO（使用错误的旧密码）
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setOldPassword(wrongPassword);
        changePasswordDTO.setNewPassword(newPassword);
        changePasswordDTO.setConfirmPassword(newPassword);

        // When & Then
        assertThatThrownBy(() -> userService.changeCurrentUserPassword(changePasswordDTO))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("当前密码错误");

        verify(userMapper, times(1)).selectById(2L);
        verify(userMapper, never()).updateById(any(User.class));
    }
}
