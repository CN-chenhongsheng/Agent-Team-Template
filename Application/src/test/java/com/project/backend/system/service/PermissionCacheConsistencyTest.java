package com.project.backend.system.service;

import com.project.backend.system.mapper.MenuMapper;
import com.project.backend.system.mapper.RoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * 权限缓存命中与失效一致性测试
 * 用 @SpyBean 包装真实 Mapper，通过调用次数断言缓存命中/失效是否真正生效。
 * 不加 @Transactional：RedisCacheManager 配置了 transactionAware()，缓存的写入/失效会
 * 推迟到事务提交后才真正执行；若测试方法处于一个最终回滚的事务中，缓存操作永远不会落地，
 * 会让缓存命中断言产生假阴性。TEST_ROLE_ID/TEST_MENU_ID 的状态更新都是幂等操作（目标状态与
 * 种子数据当前状态相同），因此无事务回滚保护也不会破坏数据。
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("权限缓存一致性测试")
class PermissionCacheConsistencyTest {

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private MenuService menuService;

    @Autowired
    private CacheManager cacheManager;

    @SpyBean
    private RoleMapper roleMapper;

    @SpyBean
    private MenuMapper menuMapper;

    private static final Long SUPER_ADMIN_USER_ID = 1L;
    private static final Long TEST_ROLE_ID = 6L;
    private static final Long TEST_MENU_ID = 49L;

    @BeforeEach
    void clearCaches() {
        cacheManager.getCache("userRoles").clear();
        cacheManager.getCache("userPermissions").clear();
        cacheManager.getCache("userMenuTree").clear();
    }

    @Test
    @DisplayName("重复查询角色编码，第二次应命中缓存不再查库")
    void getRoleCodes_hitsCacheOnSecondCall() {
        List<String> first = permissionService.getRoleCodes(SUPER_ADMIN_USER_ID);
        List<String> second = permissionService.getRoleCodes(SUPER_ADMIN_USER_ID);

        assertThat(first).isNotEmpty().isEqualTo(second);
        verify(roleMapper, times(1)).selectRoleCodesByUserId(SUPER_ADMIN_USER_ID);
    }

    @Test
    @DisplayName("重复查询权限编码，第二次应命中缓存不再查库")
    void getPermissionCodes_hitsCacheOnSecondCall() {
        List<String> first = permissionService.getPermissionCodes(SUPER_ADMIN_USER_ID);
        List<String> second = permissionService.getPermissionCodes(SUPER_ADMIN_USER_ID);

        assertThat(first).isNotEmpty().isEqualTo(second);
        verify(menuMapper, times(1)).selectAllPermissions();
    }

    @Test
    @DisplayName("角色状态变更后，角色缓存失效并重新查库")
    void roleUpdateStatus_evictsRoleCodesCache() {
        permissionService.getRoleCodes(SUPER_ADMIN_USER_ID);
        verify(roleMapper, times(1)).selectRoleCodesByUserId(SUPER_ADMIN_USER_ID);

        roleService.updateStatus(TEST_ROLE_ID, 1);

        permissionService.getRoleCodes(SUPER_ADMIN_USER_ID);
        verify(roleMapper, times(2)).selectRoleCodesByUserId(SUPER_ADMIN_USER_ID);
    }

    @Test
    @DisplayName("菜单状态变更后，权限缓存失效并重新查库")
    void menuUpdateStatus_evictsPermissionsCache() {
        permissionService.getPermissionCodes(SUPER_ADMIN_USER_ID);
        verify(menuMapper, times(1)).selectAllPermissions();

        menuService.updateStatus(TEST_MENU_ID, 1);

        permissionService.getPermissionCodes(SUPER_ADMIN_USER_ID);
        verify(menuMapper, times(2)).selectAllPermissions();
    }
}
