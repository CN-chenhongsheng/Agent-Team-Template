package com.project.backend.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.project.backend.system.entity.Role;
import com.project.backend.system.entity.RoleMenu;
import com.project.backend.system.mapper.MenuMapper;
import com.project.backend.system.mapper.RoleMapper;
import com.project.backend.system.mapper.RoleMenuMapper;
import com.project.backend.system.mapper.UserRoleMapper;
import com.project.backend.system.service.impl.RoleServiceImpl;
import com.project.core.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 角色服务单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("角色服务单元测试")
class RoleServiceImplTest {

    @Mock
    private RoleMenuMapper roleMenuMapper;

    @Mock
    private MenuMapper menuMapper;

    @Mock
    private UserRoleMapper userRoleMapper;

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private RoleServiceImpl roleService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(roleService, "baseMapper", roleMapper);
    }

    @Test
    @DisplayName("分配空菜单数组应清空角色菜单")
    void assignMenusWithEmptyArrayClearsRoleMenus() {
        when(roleMenuMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);

        boolean result = roleService.assignMenus(6L, new Long[0]);

        assertThat(result).isTrue();
        verify(roleMenuMapper).deletePhysicallyByRoleId(6L);
        verify(roleMenuMapper).delete(any(LambdaQueryWrapper.class));
        verify(roleMenuMapper, never()).insert(any(RoleMenu.class));
    }

    @Test
    @DisplayName("不能删除超级管理员角色")
    void deleteSuperAdminRoleShouldFail() {
        Role role = new Role();
        role.setId(1L);
        role.setRoleCode("SUPER_ADMIN");
        when(roleMapper.selectById(1L)).thenReturn(role);

        assertThatThrownBy(() -> roleService.deleteRole(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不能删除超级管理员角色");

        verify(roleMapper, never()).deleteById(any());
    }
}
