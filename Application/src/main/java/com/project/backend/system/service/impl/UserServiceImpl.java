package com.project.backend.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.core.context.UserContext;
import com.project.core.exception.BusinessException;
import com.project.core.result.PageResult;
import com.project.backend.system.dto.ChangePasswordDTO;
import com.project.backend.system.dto.RoleUserQueryDTO;
import com.project.backend.system.dto.UserProfileDTO;
import com.project.backend.system.dto.UserQueryDTO;
import com.project.backend.system.dto.UserResetPasswordDTO;
import com.project.backend.system.dto.UserSaveDTO;
import com.project.backend.system.dto.UserWithRoleCodeDTO;
import com.project.backend.system.entity.Menu;
import com.project.backend.system.entity.User;
import com.project.backend.system.entity.UserMenu;
import com.project.backend.system.entity.UserRole;
import com.project.backend.system.entity.RoleMenu;
import com.project.backend.system.mapper.MenuMapper;
import com.project.backend.system.mapper.RoleMapper;
import com.project.backend.system.mapper.RoleMenuMapper;
import com.project.backend.system.mapper.UserMapper;
import com.project.backend.system.mapper.UserMenuMapper;
import com.project.backend.system.mapper.UserRoleMapper;
import com.project.backend.system.service.UserService;
import com.project.backend.system.service.UserOnlineService;
import com.project.core.util.BusinessRuleUtils;
import com.project.backend.util.DictUtils;
import com.project.backend.system.vo.UserPermissionVO;
import com.project.backend.system.vo.UserSimpleVO;
import com.project.backend.system.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统用户Service实现
 * 
 * @author 陈鸿昇
 * @since 2025-12-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final UserMenuMapper userMenuMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final MenuMapper menuMapper;
    private final UserOnlineService userOnlineService;

    /**
     * 系统默认密码（从配置文件读取）
     */
    @Value("${system.default-password:123456}")
    private String defaultPassword;

    /**
     * 用户状态常量
     */
    private static final int STATUS_ENABLED = 1;
    private static final int STATUS_DISABLED = 0;

    /**
     * 分页查询用户列表
     */
    @Override
    public PageResult<UserVO> pageList(UserQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(queryDTO.getUsername()), User::getUsername, queryDTO.getUsername())
               .like(StrUtil.isNotBlank(queryDTO.getNickname()), User::getNickname, queryDTO.getNickname())
               .like(StrUtil.isNotBlank(queryDTO.getPhone()), User::getPhone, queryDTO.getPhone())
               .eq(StrUtil.isNotBlank(queryDTO.getManageScope()), User::getManageScope, queryDTO.getManageScope())
               .eq(queryDTO.getStatus() != null, User::getStatus, queryDTO.getStatus())
               .orderByDesc(User::getCreateTime);

        // 分页查询
        Page<User> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page(page, wrapper);

        // 转换为VO
        List<UserVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.build(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 根据ID获取用户详情
     */
    @Override
    public UserVO getDetailById(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return convertToVO(user);
    }

    /**
     * 保存用户（新增或编辑）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveUser(UserSaveDTO saveDTO) {
        validateUsername(saveDTO);
        validateSuperAdminStatus(saveDTO);

        User user = new User();
        BeanUtil.copyProperties(saveDTO, user);

        boolean isNewUser = saveDTO.getId() == null;
        boolean success;
        Long userId;

        if (isNewUser) {
            userId = createNewUser(user, saveDTO.getPassword());
            success = userId != null;
        } else {
            userId = saveDTO.getId();
            updateUserPassword(user, saveDTO.getPassword());
            success = updateById(user);
        }

        if (success && saveDTO.getRoleIds() != null) {
            saveUserRoles(userId, saveDTO.getRoleIds());
        }

        return success;
    }

    /**
     * 验证用户名是否重复
     */
    private void validateUsername(UserSaveDTO saveDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, saveDTO.getUsername())
               .eq(User::getDeleted, 0);
        if (saveDTO.getId() != null) {
            wrapper.ne(User::getId, saveDTO.getId());
        }
        if (count(wrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }
    }

    /**
     * 验证超级管理员状态
     */
    private void validateSuperAdminStatus(UserSaveDTO saveDTO) {
        if (saveDTO.getId() != null) {
            User existingUser = getById(saveDTO.getId());
            if (existingUser != null) {
                BusinessRuleUtils.validateSuperAdminUserStatus(existingUser.getUsername(), saveDTO.getStatus());
            }
        }
    }

    /**
     * 创建新用户
     * 如果密码为空，则使用系统默认密码
     */
    private Long createNewUser(User user, String password) {
        // 如果密码为空，使用系统默认密码
        String finalPassword = StrUtil.isBlank(password) ? defaultPassword : password;

        user.setPassword(BCrypt.hashpw(finalPassword));
        user.setStatus(STATUS_ENABLED);
        save(user);

        // 如果使用了默认密码，记录日志
        if (StrUtil.isBlank(password)) {
            log.info("用户【{}】创建成功，使用系统默认密码", user.getUsername());
        }

        return user.getId();
    }

    /**
     * 更新用户密码
     */
    private void updateUserPassword(User user, String password) {
        if (StrUtil.isNotBlank(password)) {
            user.setPassword(BCrypt.hashpw(password));
        } else {
            user.setPassword(null);
        }
    }

    /**
     * 保存用户角色关联
     */
    private void saveUserRoles(Long userId, Long[] roleIds) {
        List<Long> oldRoleIds = getOldRoleIds(userId);

        // 删除用户原有的角色关联
        deleteUserRoles(userId);

        // 批量插入新的角色关联
        List<Long> newRoleIds = roleIds != null ? Arrays.asList(roleIds) : List.of();
        insertUserRoles(userId, newRoleIds);

        // 自动更新用户权限
        autoUpdateUserMenusOnRoleChange(userId, oldRoleIds, newRoleIds);
    }

    /**
     * 获取用户原有的角色ID列表
     */
    private List<Long> getOldRoleIds(Long userId) {
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId);
        return userRoleMapper.selectList(wrapper).stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());
    }

    /**
     * 删除用户角色关联
     */
    private void deleteUserRoles(Long userId) {
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId);
        userRoleMapper.delete(wrapper);
    }

    /**
     * 批量插入用户角色关联
     */
    private void insertUserRoles(Long userId, List<Long> roleIds) {
        roleIds.forEach(roleId -> {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        });
    }

    /**
     * 用户角色变更时自动更新用户权限
     * 
     * @param userId 用户ID
     * @param oldRoleIds 原有角色ID列表
     * @param newRoleIds 新角色ID列表
     */
    private void autoUpdateUserMenusOnRoleChange(Long userId, List<Long> oldRoleIds, List<Long> newRoleIds) {
        // 1. 获取新角色的权限并集
        List<Long> newRoleMenuIds = getRoleMenuIdsUnion(newRoleIds);

        // 2. 获取用户当前已分配的权限
        List<Long> currentUserMenuIds = getUserMenuIds(userId);

        // 3. 计算需要添加的权限（新角色权限中，用户还没有的）
        List<Long> menuIdsToAdd = newRoleMenuIds.stream()
                .filter(menuId -> !currentUserMenuIds.contains(menuId))
                .collect(Collectors.toList());

        // 4. 添加新角色的权限到用户权限中
        if (!menuIdsToAdd.isEmpty()) {
            for (Long menuId : menuIdsToAdd) {
                UserMenu userMenu = new UserMenu();
                userMenu.setUserId(userId);
                userMenu.setMenuId(menuId);
                userMenuMapper.insert(userMenu);
            }
            log.info("用户角色变更，自动添加权限，用户ID：{}，新增权限数：{}", userId, menuIdsToAdd.size());
        }

        // 5. 清理用户权限中不再属于任何角色的权限
        if (!newRoleIds.isEmpty()) {
            List<Long> menuIdsToRemove = currentUserMenuIds.stream()
                    .filter(menuId -> !newRoleMenuIds.contains(menuId))
                    .collect(Collectors.toList());

            if (!menuIdsToRemove.isEmpty()) {
                LambdaQueryWrapper<UserMenu> deleteWrapper = new LambdaQueryWrapper<>();
                deleteWrapper.eq(UserMenu::getUserId, userId)
                        .in(UserMenu::getMenuId, menuIdsToRemove);
                userMenuMapper.delete(deleteWrapper);
                log.info("用户角色变更，清理无效权限，用户ID：{}，清理权限数：{}", userId, menuIdsToRemove.size());
            }
        } else {
            // 如果用户没有角色了，清空所有用户权限
            LambdaQueryWrapper<UserMenu> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(UserMenu::getUserId, userId);
            userMenuMapper.delete(deleteWrapper);
            log.info("用户角色变更，清空所有权限，用户ID：{}", userId);
        }
    }

    /**
     * 获取多个角色的权限并集
     */
    private List<Long> getRoleMenuIdsUnion(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }

        LambdaQueryWrapper<RoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(RoleMenu::getRoleId, roleIds);
        List<RoleMenu> roleMenus = roleMenuMapper.selectList(wrapper);

        return roleMenus.stream()
                .map(RoleMenu::getMenuId)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 删除用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long id) {
        if (id == null) {
            throw new BusinessException("用户ID不能为空");
        }

        // 检查是否可以删除
        validateUserDeletion(id);

        // 删除用户角色关联
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, id);
        userRoleMapper.delete(wrapper);

        // 删除用户菜单关联
        LambdaQueryWrapper<UserMenu> userMenuWrapper = new LambdaQueryWrapper<>();
        userMenuWrapper.eq(UserMenu::getUserId, id);
        userMenuMapper.delete(userMenuWrapper);

        // 删除用户
        return removeById(id);
    }

    /**
     * 批量删除用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDelete(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("用户ID不能为空");
        }

        // 逐个检查是否可以删除
        for (Long id : ids) {
            validateUserDeletion(id);
        }

        // 批量删除用户角色关联
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(UserRole::getUserId, Arrays.asList(ids));
        userRoleMapper.delete(wrapper);

        // 批量删除用户菜单关联
        LambdaQueryWrapper<UserMenu> userMenuWrapper = new LambdaQueryWrapper<>();
        userMenuWrapper.in(UserMenu::getUserId, Arrays.asList(ids));
        userMenuMapper.delete(userMenuWrapper);

        // 批量删除用户
        return removeByIds(Arrays.asList(ids));
    }

    /**
     * 验证用户是否可以删除
     *
     * @param userId 用户ID
     */
    private void validateUserDeletion(Long userId) {
        // 1. 检查用户是否存�?
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 使用通用工具类验证业务规则（超级管理员、不能删除自己等）
        BusinessRuleUtils.validateUserDeletion(userId, user.getUsername());

        // 3. 可以根据需要添加更多业务规则
        // 例如：检查用户是否有关联的业务数据等
    }

    /**
     * 重置密码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(UserResetPasswordDTO resetDTO) {
        User user = getById(resetDTO.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 加密新密码
        user.setPassword(BCrypt.hashpw(resetDTO.getNewPassword()));
        return updateById(user);
    }

    /**
     * 修改用户状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 使用通用工具类验证超级管理员用户状态
        BusinessRuleUtils.validateSuperAdminUserStatus(user.getUsername(), status);

        user.setStatus(status);
        return updateById(user);
    }

    /**
     * 实体转VO
     */
    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        BeanUtil.copyProperties(user, vo);

        // 状态文本（使用字典数据）
        vo.setStatusText(DictUtils.getLabel("sys_user_status", user.getStatus(), "未知"));

        // 性别文本（使用字典）
        vo.setGenderText(DictUtils.getLabel("sys_user_sex", user.getGender(), "未知"));

        // 查询用户角色信息
        vo.setRoleIds(roleMapper.selectRoleIdsByUserId(user.getId()));
        vo.setRoleNames(roleMapper.selectRoleNamesByUserId(user.getId()));

        // 在线状态
        vo.setIsOnline(userOnlineService.isUserOnline(user.getId()));

        return vo;
    }

    /**
     * 获取当前用户个人信息
     */
    @Override
    public UserVO getCurrentUserProfile() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }
        return getDetailById(userId);
    }

    /**
     * 更新当前用户个人信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCurrentUserProfile(UserProfileDTO profileDTO) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 更新允许用户自己修改的字段
        updateUserProfileFields(user, profileDTO);

        return updateById(user);
    }

    /**
     * 更新用户个人信息字段
     */
    private void updateUserProfileFields(User user, UserProfileDTO profileDTO) {
        if (StrUtil.isNotBlank(profileDTO.getNickname())) {
            user.setNickname(profileDTO.getNickname());
        }
        if (profileDTO.getAvatar() != null) {
            user.setAvatar(profileDTO.getAvatar());
        }
        if (profileDTO.getEmail() != null) {
            user.setEmail(profileDTO.getEmail());
        }
        if (profileDTO.getPhone() != null) {
            user.setPhone(profileDTO.getPhone());
        }
        if (profileDTO.getGender() != null) {
            user.setGender(profileDTO.getGender());
        }
        if (profileDTO.getAddress() != null) {
            user.setAddress(profileDTO.getAddress());
        }
        if (profileDTO.getIntroduction() != null) {
            user.setIntroduction(profileDTO.getIntroduction());
        }
    }

    /**
     * 修改当前用户密码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changeCurrentUserPassword(ChangePasswordDTO changePasswordDTO) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        // 验证新密码和确认密码是否一致
        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            throw new BusinessException("新密码和确认密码不一致");
        }

        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证旧密码是否正确
        if (!BCrypt.checkpw(changePasswordDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException("当前密码错误");
        }

        // 更新密码
        user.setPassword(BCrypt.hashpw(changePasswordDTO.getNewPassword()));
        return updateById(user);
    }

    /**
     * 根据角色代码列表查询用户列表
     */
    @Override
    public Map<String, List<UserSimpleVO>> getUsersByRoleCodes(RoleUserQueryDTO queryDTO) {
        Map<String, List<UserSimpleVO>> result = new HashMap<>();

        if (queryDTO == null || queryDTO.getRoleCodes() == null || queryDTO.getRoleCodes().isEmpty()) {
            return result;
        }

        // 查询所有指定角色的用户
        List<UserWithRoleCodeDTO> userWithRoleCodes = baseMapper.selectUsersByRoleCodes(queryDTO.getRoleCodes());

        // 按角色代码分组
        Map<String, List<UserWithRoleCodeDTO>> userGroupByRole = userWithRoleCodes.stream()
                .collect(Collectors.groupingBy(UserWithRoleCodeDTO::getRoleCode));

        // 构建返回结果，key为角色代码，value为用户列表
        for (String roleCode : queryDTO.getRoleCodes()) {
            List<UserWithRoleCodeDTO> users = userGroupByRole.get(roleCode);
            if (users != null && !users.isEmpty()) {
                List<UserSimpleVO> userList = users.stream().map(userWithRole -> {
                    UserSimpleVO userVO = new UserSimpleVO();
                    userVO.setId(userWithRole.getId());
                    userVO.setUsername(userWithRole.getUsername());
                    userVO.setNickname(userWithRole.getNickname());
                    userVO.setPhone(userWithRole.getPhone());
                    userVO.setEmail(userWithRole.getEmail());
                    return userVO;
                }).collect(Collectors.toList());
                result.put(roleCode, userList);
            } else {
                // 如果角色没有用户，返回空数组
                result.put(roleCode, List.of());
            }
        }

        return result;
    }

    /**
     * 分配用户菜单权限
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignMenus(Long userId, Long[] menuIds) {
        log.info("分配用户菜单权限，用户ID：{}，菜单IDs：{}", userId, Arrays.toString(menuIds));

        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        // 验证要分配的权限是否在可选范围内
        validateMenuIds(userId, menuIds);

        // 删除用户原有的菜单权限
        deleteUserMenus(userId);

        // 批量插入新的菜单权限
        if (menuIds != null && menuIds.length > 0) {
            insertUserMenus(userId, Arrays.asList(menuIds));
        }

        return true;
    }

    /**
     * 验证菜单ID是否在用户可选范围内
     */
    private void validateMenuIds(Long userId, Long[] menuIds) {
        if (menuIds == null || menuIds.length == 0) {
            return;
        }

        List<Long> availableMenuIds = getUserAvailableMenuIds(userId);
        for (Long menuId : menuIds) {
            if (!availableMenuIds.contains(menuId)) {
                throw new BusinessException("菜单ID " + menuId + " 不在用户可选权限范围内");
            }
        }
    }

    /**
     * 删除用户菜单关联
     */
    private void deleteUserMenus(Long userId) {
        // 1. 先物理删除该用户所有已软删除的旧记录（避免唯一索引冲突）
        userMenuMapper.deletePhysicallyByUserId(userId);

        // 2. 软删除该用户当前活跃的菜单权限
        LambdaQueryWrapper<UserMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserMenu::getUserId, userId);
        userMenuMapper.delete(wrapper);
    }

    /**
     * 批量插入用户菜单关联
     */
    private void insertUserMenus(Long userId, List<Long> menuIds) {
        menuIds.forEach(menuId -> {
            UserMenu userMenu = new UserMenu();
            userMenu.setUserId(userId);
            userMenu.setMenuId(menuId);
            userMenuMapper.insert(userMenu);
        });
    }

    /**
     * 获取用户的菜单ID列表
     */
    @Override
    public List<Long> getUserMenuIds(Long userId) {
        if (userId == null) {
            return List.of();
        }

        LambdaQueryWrapper<UserMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserMenu::getUserId, userId);

        List<UserMenu> userMenus = userMenuMapper.selectList(wrapper);

        return userMenus.stream()
                .map(UserMenu::getMenuId)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户的权限列表（包含菜单状态，用于显示）
     */
    @Override
    public List<UserPermissionVO> getUserPermissions(Long userId) {
        if (userId == null) {
            return List.of();
        }

        // 查询用户关联的菜单
        LambdaQueryWrapper<UserMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserMenu::getUserId, userId);
        List<UserMenu> userMenus = userMenuMapper.selectList(wrapper);

        if (userMenus.isEmpty()) {
            return List.of();
        }

        // 批量查询菜单状态
        List<Long> menuIds = userMenus.stream()
                .map(UserMenu::getMenuId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<Menu> menuWrapper = new LambdaQueryWrapper<>();
        menuWrapper.in(Menu::getId, menuIds);
        List<Menu> menus = menuMapper.selectList(menuWrapper);

        // 构建菜单ID到状态的映射
        Map<Long, Integer> menuStatusMap = menus.stream()
                .collect(Collectors.toMap(Menu::getId, Menu::getStatus, (v1, v2) -> v1));

        // 构建返回结果
        return userMenus.stream()
                .map(userMenu -> {
                    UserPermissionVO vo = new UserPermissionVO();
                    vo.setMenuId(userMenu.getMenuId());
                    vo.setStatus(menuStatusMap.getOrDefault(userMenu.getMenuId(), 0));
                    return vo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取用户可选的菜单ID列表（用户所有角色的权限并集）
     */
    @Override
    public List<Long> getUserAvailableMenuIds(Long userId) {
        if (userId == null) {
            return List.of();
        }

        // 1. 获取用户的所有角色ID
        LambdaQueryWrapper<UserRole> userRoleWrapper = new LambdaQueryWrapper<>();
        userRoleWrapper.eq(UserRole::getUserId, userId);
        List<UserRole> userRoles = userRoleMapper.selectList(userRoleWrapper);

        if (userRoles.isEmpty()) {
            return List.of();
        }

        List<Long> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());

        // 2. 获取这些角色的权限并集
        return getRoleMenuIdsUnion(roleIds);
    }
}
