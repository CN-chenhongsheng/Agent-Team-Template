package com.project.backend.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.project.backend.system.dto.MenuQueryDTO;
import com.project.backend.system.dto.MenuSaveDTO;
import com.project.backend.system.entity.Menu;
import com.project.backend.system.vo.MenuVO;

import java.util.List;

/**
 * 系统菜单Service
 * 
 * @author 陈鸿昇
 * @since 2025-12-30
 */
public interface MenuService extends IService<Menu> {

    /**
     * 查询菜单树形列表
     * 
     * @param queryDTO 查询条件
     * @return 菜单树形列表
     */
    List<MenuVO> treeList(MenuQueryDTO queryDTO);

    /**
     * 根据ID获取菜单详情
     * 
     * @param id 菜单ID
     * @return 菜单信息
     */
    MenuVO getDetailById(Long id);

    /**
     * 保存菜单（新增或编辑）
     * 
     * @param saveDTO 菜单保存DTO
     * @return 是否成功
     */
    boolean saveMenu(MenuSaveDTO saveDTO);

    /**
     * 删除菜单
     * 
     * @param id 菜单ID
     * @return 是否成功
     */
    boolean deleteMenu(Long id);

    /**
     * 获取菜单树形选择器（用于上级菜单选择）
     * 
     * @return 菜单树形列表
     */
    List<MenuVO> getMenuTreeSelect();

    /**
     * 获取菜单树用于权限分配（包含所有类型，不包含顶级菜单）
     * 
     * @return 菜单树形列表
     */
    List<MenuVO> getMenuTreeForPermission();

    /**
     * 获取当前登录用户的菜单树
     * 根据用户的角色权限返回有权限查看的菜单
     * 
     * @return 用户菜单树形列表
     */
    List<MenuVO> getUserMenuTree();

    /**
     * 更新菜单状态
     * 
     * @param id 菜单ID
     * @param status 状态：1正常 0停用
     * @return 是否成功
     */
    boolean updateStatus(Long id, Integer status);
}
