package com.project.backend.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.core.exception.BusinessException;
import com.project.core.result.PageResult;
import com.project.backend.system.dto.RoleQueryDTO;
import com.project.backend.system.dto.RoleSaveDTO;
import com.project.backend.system.entity.Menu;
import com.project.backend.system.entity.Role;
import com.project.backend.system.entity.RoleMenu;
import com.project.backend.system.entity.UserRole;
import com.project.backend.system.mapper.MenuMapper;
import com.project.backend.system.mapper.RoleMapper;
import com.project.backend.system.mapper.RoleMenuMapper;
import com.project.backend.system.mapper.UserRoleMapper;
import com.project.backend.system.service.RoleService;
import com.project.core.util.BusinessRuleUtils;
import com.project.backend.util.DictUtils;
import com.project.backend.system.vo.RolePermissionVO;
import com.project.backend.system.vo.RoleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统角色Service实现
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final RoleMenuMapper roleMenuMapper;
    private final MenuMapper menuMapper;
    private final UserRoleMapper userRoleMapper;

    /**
     * 分页查询角色列表
     */
    @Override
    public PageResult<RoleVO> pageList(RoleQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(queryDTO.getRoleCode()), Role::getRoleCode, queryDTO.getRoleCode())
               .like(StrUtil.isNotBlank(queryDTO.getRoleName()), Role::getRoleName, queryDTO.getRoleName())
               .eq(queryDTO.getStatus() != null, Role::getStatus, queryDTO.getStatus())
               .orderByAsc(Role::getSort)
               .orderByDesc(Role::getCreateTime);

        // 分页查询
        Page<Role> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page(page, wrapper);

        // 转换为VO
        List<RoleVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.build(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 查询所有角色列表（不分页）
     */
    @Override
    public List<RoleVO> listAll() {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getStatus, 1) // 只查询正常状态的角色
               .orderByAsc(Role::getSort)
               .orderByDesc(Role::getCreateTime);

        return list(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取角色详情
     */
    @Override
    public RoleVO getDetailById(Long id) {
        Role role = getById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        RoleVO vo = convertToVO(role);

        // 查询角色的菜单权限
        vo.setMenuIds(getRoleMenuIds(id));

        return vo;
    }

    /**
     * 保存角色（新增或编辑）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveRole(RoleSaveDTO saveDTO) {
        // 检查角色编码是否重复
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getRoleCode, saveDTO.getRoleCode());
        if (saveDTO.getId() != null) {
            wrapper.ne(Role::getId, saveDTO.getId());
        }
        if (count(wrapper) > 0) {
            throw new BusinessException("角色编码已存在");
        }

        Role role = new Role();
        BeanUtil.copyProperties(saveDTO, role);

        // 编辑时，如果是超级管理员，不允许设置为停用
        if (saveDTO.getId() != null) {
            Role existingRole = getById(saveDTO.getId());
            if (existingRole != null) {
                BusinessRuleUtils.validateSuperAdminRoleStatus(existingRole.getRoleCode(), saveDTO.getStatus());
            }
        }

        boolean success;
        if (saveDTO.getId() == null) {
            // 新增
            role.setStatus(1); // 默认启用
            success = save(role);
        } else {
            // 编辑
            success = updateById(role);
        }

        // TODO: 保存角色菜单关联
        if (success && saveDTO.getMenuIds() != null && saveDTO.getMenuIds().length > 0) {
            assignMenus(role.getId(), saveDTO.getMenuIds());
        }

        return success;
    }

    /**
     * 删除角色
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(Long id) {
        if (id == null) {
            throw new BusinessException("角色ID不能为空");
        }

        Role role = getById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 不能删除超级管理员角色
        BusinessRuleUtils.validateNotSuperAdminRole(role.getRoleCode(), "不能删除超级管理员角色");

        // 删除用户角色关联
        LambdaQueryWrapper<UserRole> userRoleWrapper = new LambdaQueryWrapper<>();
        userRoleWrapper.eq(UserRole::getRoleId, id);
        int deletedUserRoles = userRoleMapper.delete(userRoleWrapper);
        log.info("删除角色时，清理用户角色关联 {} 条", deletedUserRoles);

        // 删除角色菜单关联
        LambdaQueryWrapper<RoleMenu> roleMenuWrapper = new LambdaQueryWrapper<>();
        roleMenuWrapper.eq(RoleMenu::getRoleId, id);
        roleMenuMapper.delete(roleMenuWrapper);

        return removeById(id);
    }

    /**
     * 批量删除角色
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDelete(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("角色ID不能为空");
        }

        // 检查是否包含超级管理员角色
        for (Long id : ids) {
            Role role = getById(id);
            if (role != null) {
                BusinessRuleUtils.validateNotSuperAdminRole(role.getRoleCode(), "不能删除超级管理员角色");
            }
        }

        // 批量删除用户角色关联
        LambdaQueryWrapper<UserRole> userRoleWrapper = new LambdaQueryWrapper<>();
        userRoleWrapper.in(UserRole::getRoleId, Arrays.asList(ids));
        int deletedUserRoles = userRoleMapper.delete(userRoleWrapper);
        log.info("批量删除角色时，清理用户角色关联 {} 条", deletedUserRoles);

        // 批量删除角色菜单关联
        LambdaQueryWrapper<RoleMenu> roleMenuWrapper = new LambdaQueryWrapper<>();
        roleMenuWrapper.in(RoleMenu::getRoleId, Arrays.asList(ids));
        roleMenuMapper.delete(roleMenuWrapper);

        return removeByIds(Arrays.asList(ids));
    }

    /**
     * 分配角色菜单权限
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignMenus(Long roleId, Long[] menuIds) {
        log.info("分配角色菜单权限，角色ID：{}，菜单IDs：{}", roleId, Arrays.toString(menuIds));

        if (roleId == null) {
            throw new BusinessException("角色ID不能为空");
        }

        // 1. 先物理删除该角色所有已软删除的旧记录（避免唯一索引冲突）
        // 使用原生 SQL 直接删除 deleted=1 的记录
        roleMenuMapper.deletePhysicallyByRoleId(roleId);

        // 2. 软删除该角色当前活跃的菜单权限
        LambdaQueryWrapper<RoleMenu> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(RoleMenu::getRoleId, roleId);
        roleMenuMapper.delete(deleteWrapper);

        // 3. 批量插入新的菜单权限
        if (menuIds != null && menuIds.length > 0) {
            for (Long menuId : menuIds) {
                RoleMenu roleMenu = new RoleMenu();
                roleMenu.setRoleId(roleId);
                roleMenu.setMenuId(menuId);
                roleMenuMapper.insert(roleMenu);
            }
        }

        return true;
    }

    /**
     * 获取角色的菜单ID列表
     */
    @Override
    public List<Long> getRoleMenuIds(Long roleId) {
        if (roleId == null) {
            return List.of();
        }

        LambdaQueryWrapper<RoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleMenu::getRoleId, roleId);

        List<RoleMenu> roleMenus = roleMenuMapper.selectList(wrapper);

        return roleMenus.stream()
                .map(RoleMenu::getMenuId)
                .collect(Collectors.toList());
    }

    /**
     * 获取角色的菜单权限列表（包含菜单状态）
     */
    public List<RolePermissionVO> getRolePermissions(Long roleId) {
        if (roleId == null) {
            return List.of();
        }

        // 查询角色关联的菜单
        LambdaQueryWrapper<RoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleMenu::getRoleId, roleId);
        List<RoleMenu> roleMenus = roleMenuMapper.selectList(wrapper);

        if (roleMenus.isEmpty()) {
            return List.of();
        }

        // 批量查询菜单状态
        List<Long> menuIds = roleMenus.stream()
                .map(RoleMenu::getMenuId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<Menu> menuWrapper = new LambdaQueryWrapper<>();
        menuWrapper.in(Menu::getId, menuIds);
        List<Menu> menus = menuMapper.selectList(menuWrapper);

        // 构建菜单ID到状态的映射
        java.util.Map<Long, Integer> menuStatusMap = menus.stream()
                .collect(Collectors.toMap(Menu::getId, Menu::getStatus, (v1, v2) -> v1));

        // 构建返回结果
        return roleMenus.stream()
                .map(roleMenu -> {
                    RolePermissionVO vo = new RolePermissionVO();
                    vo.setMenuId(roleMenu.getMenuId());
                    // 如果菜单已被删除，状态设为停用
                    vo.setStatus(menuStatusMap.getOrDefault(roleMenu.getMenuId(), 0));
                    return vo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 更新角色状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        Role role = getById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 使用通用工具类验证超级管理员角色状态
        BusinessRuleUtils.validateSuperAdminRoleStatus(role.getRoleCode(), status);

        // 如果是停用角色，删除所有用户与该角色的关联
        if (status != null && status == 0) {
            LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserRole::getRoleId, id);
            int deletedCount = userRoleMapper.delete(wrapper);
            log.info("角色停用，已清理 {} 条用户角色关联", deletedCount);
        }

        role.setStatus(status);
        return updateById(role);
    }

    /**
     * 实体转VO
     */
    private RoleVO convertToVO(Role role) {
        RoleVO vo = new RoleVO();
        BeanUtil.copyProperties(role, vo);

        // 状态文本（使用字典数据）
        vo.setStatusText(DictUtils.getLabel("sys_user_status", role.getStatus(), "未知"));

        return vo;
    }
}
