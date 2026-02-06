package com.project.backend.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.project.core.result.PageResult;
import com.project.backend.system.dto.RoleUserQueryDTO;
import com.project.backend.system.dto.UserQueryDTO;
import com.project.backend.system.dto.UserResetPasswordDTO;
import com.project.backend.system.dto.UserSaveDTO;
import com.project.backend.system.entity.User;
import com.project.backend.system.vo.UserPermissionVO;
import com.project.backend.system.vo.UserSimpleVO;
import com.project.backend.system.vo.UserVO;

import java.util.List;
import java.util.Map;

/**
 * 系统用户Service
 * 
 * @author 陈鸿昇
 * @since 2025-12-30
 */
public interface UserService extends IService<User> {

    /**
     * 分页查询用户列表
     * 
     * @param queryDTO 查询条件
     * @return 用户分页列表
     */
    PageResult<UserVO> pageList(UserQueryDTO queryDTO);

    /**
     * 根据ID获取用户详情
     * 
     * @param id 用户ID
     * @return 用户信息
     */
    UserVO getDetailById(Long id);

    /**
     * 保存用户（新增或编辑）
     * 
     * @param saveDTO 用户保存DTO
     * @return 是否成功
     */
    boolean saveUser(UserSaveDTO saveDTO);

    /**
     * 删除用户
     * 
     * @param id 用户ID
     * @return 是否成功
     */
    boolean deleteUser(Long id);

    /**
     * 批量删除用户
     * 
     * @param ids 用户ID列表
     * @return 是否成功
     */
    boolean batchDelete(Long[] ids);

    /**
     * 重置密码
     * 
     * @param resetDTO 重置密码DTO
     * @return 是否成功
     */
    boolean resetPassword(UserResetPasswordDTO resetDTO);

    /**
     * 修改用户状态
     * 
     * @param id 用户ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateStatus(Long id, Integer status);

    /**
     * 获取当前用户个人信息
     * 
     * @return 用户详细信息
     */
    UserVO getCurrentUserProfile();

    /**
     * 更新当前用户个人信息
     * 
     * @param profileDTO 个人信息DTO
     * @return 是否成功
     */
    boolean updateCurrentUserProfile(com.project.backend.system.dto.UserProfileDTO profileDTO);

    /**
     * 修改当前用户密码
     * 
     * @param changePasswordDTO 修改密码DTO
     * @return 是否成功
     */
    boolean changeCurrentUserPassword(com.project.backend.system.dto.ChangePasswordDTO changePasswordDTO);

    /**
     * 根据角色代码列表查询用户列表
     * 
     * @param queryDTO 角色代码查询DTO
     * @return Map格式，key为角色代码，value为该角色的用户列表
     */
    Map<String, List<UserSimpleVO>> getUsersByRoleCodes(RoleUserQueryDTO queryDTO);

    /**
     * 分配用户菜单权限
     * 
     * @param userId 用户ID
     * @param menuIds 菜单ID数组
     * @return 是否成功
     */
    boolean assignMenus(Long userId, Long[] menuIds);

    /**
     * 获取用户的菜单ID列表
     * 
     * @param userId 用户ID
     * @return 菜单ID列表
     */
    List<Long> getUserMenuIds(Long userId);

    /**
     * 获取用户的权限列表（包含菜单状态，用于显示）
     * 
     * @param userId 用户ID
     * @return 权限列表
     */
    List<UserPermissionVO> getUserPermissions(Long userId);

    /**
     * 获取用户可选的菜单ID列表（用户所有角色的权限并集）
     * 
     * @param userId 用户ID
     * @return 可选的菜单ID列表
     */
    List<Long> getUserAvailableMenuIds(Long userId);
}
