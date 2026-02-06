package com.project.backend.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.project.core.result.PageResult;
import com.project.backend.system.dto.RoleQueryDTO;
import com.project.backend.system.dto.RoleSaveDTO;
import com.project.backend.system.entity.Role;
import com.project.backend.system.vo.RolePermissionVO;
import com.project.backend.system.vo.RoleVO;

import java.util.List;

/**
 * 系统角色Service
 * 
 * @author 陈鸿昇
 * @since 2025-12-30
 */
public interface RoleService extends IService<Role> {

    /**
     * 分页查询角色列表
     * 
     * @param queryDTO 查询条件
     * @return 角色分页列表
     */
    PageResult<RoleVO> pageList(RoleQueryDTO queryDTO);

    /**
     * 查询所有角色列表（不分页）
     * 
     * @return 角色列表
     */
    List<RoleVO> listAll();

    /**
     * 根据ID获取角色详情
     * 
     * @param id 角色ID
     * @return 角色信息
     */
    RoleVO getDetailById(Long id);

    /**
     * 保存角色（新增或编辑）
     * 
     * @param saveDTO 角色保存DTO
     * @return 是否成功
     */
    boolean saveRole(RoleSaveDTO saveDTO);

    /**
     * 删除角色
     * 
     * @param id 角色ID
     * @return 是否成功
     */
    boolean deleteRole(Long id);

    /**
     * 批量删除角色
     * 
     * @param ids 角色ID列表
     * @return 是否成功
     */
    boolean batchDelete(Long[] ids);

    /**
     * 分配角色菜单权限
     * 
     * @param roleId 角色ID
     * @param menuIds 菜单ID列表
     * @return 是否成功
     */
    boolean assignMenus(Long roleId, Long[] menuIds);

    /**
     * 获取角色的菜单ID列表
     * 
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    List<Long> getRoleMenuIds(Long roleId);

    /**
     * 获取角色的菜单权限列表（包含菜单状态）
     * 
     * @param roleId 角色ID
     * @return 菜单权限列表（包含菜单ID和状态）
     */
    List<RolePermissionVO> getRolePermissions(Long roleId);

    /**
     * 更新角色状态
     * 
     * @param id 角色ID
     * @param status 状态：1正常 0停用
     * @return 是否成功
     */
    boolean updateStatus(Long id, Integer status);
}
