package com.project.backend.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.core.context.UserContext;
import com.project.core.exception.BusinessException;
import com.project.core.util.TreeUtils;
import com.project.backend.system.dto.MenuQueryDTO;
import com.project.backend.system.dto.MenuSaveDTO;
import com.project.backend.system.entity.Menu;
import com.project.backend.system.entity.RoleMenu;
import com.project.backend.system.entity.UserMenu;
import com.project.backend.system.mapper.MenuMapper;
import com.project.backend.system.mapper.RoleMenuMapper;
import com.project.backend.system.mapper.UserMenuMapper;
import com.project.backend.system.service.MenuService;
import com.project.backend.util.DictUtils;
import com.project.backend.system.vo.MenuVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统菜单Service实现
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    private final RoleMenuMapper roleMenuMapper;
    private final UserMenuMapper userMenuMapper;

    /**
     * 查询菜单树形列表
     */
    @Override
    public List<MenuVO> treeList(MenuQueryDTO queryDTO) {
        // 查询所有菜单
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(queryDTO.getMenuName()), Menu::getMenuName, queryDTO.getMenuName())
               .eq(queryDTO.getMenuType() != null, Menu::getMenuType, queryDTO.getMenuType())
               .eq(queryDTO.getStatus() != null, Menu::getStatus, queryDTO.getStatus())
               .orderByAsc(Menu::getSort)
               .orderByAsc(Menu::getId);

        List<Menu> allMenus = list(wrapper);

        // 转换为VO并构建树形结构
        List<MenuVO> allMenuVOs = allMenus.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return TreeUtils.buildTree(
                allMenuVOs,
                0L,
                MenuVO::getParentId,
                MenuVO::getId,
                MenuVO::setChildren
        );
    }

    /**
     * 根据ID获取菜单详情
     */
    @Override
    public MenuVO getDetailById(Long id) {
        Menu menu = getById(id);
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }
        return convertToVO(menu);
    }

    /**
     * 保存菜单（新增或编辑）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveMenu(MenuSaveDTO saveDTO) {
        Menu menu = new Menu();
        BeanUtil.copyProperties(saveDTO, menu);

        if (saveDTO.getId() == null) {
            // 新增
            menu.setStatus(1); // 默认启用
            menu.setVisible(1); // 默认显示
            return save(menu);
        } else {
            // 编辑
            // 不能将父菜单设置为自己或自己的子菜单
            if (menu.getId().equals(menu.getParentId())) {
                throw new BusinessException("不能将父菜单设置为自身");
            }
            boolean result = updateById(menu);
            // 当显示状态、菜单状态或页面缓存为关闭时，级联更新所有子菜单
            if (result && (Integer.valueOf(0).equals(saveDTO.getVisible())
                    || Integer.valueOf(0).equals(saveDTO.getStatus())
                    || Integer.valueOf(0).equals(saveDTO.getKeepAlive()))) {
                cascadeUpdateChildrenVisibleStatusKeepAlive(
                        menu.getId(),
                        saveDTO.getVisible(),
                        saveDTO.getStatus(),
                        saveDTO.getKeepAlive());
            }
            return result;
        }
    }

    /**
     * 删除菜单
     * 级联删除所有子菜单及相关的角色菜单关系
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMenu(Long id) {
        if (id == null) {
            throw new BusinessException("菜单ID不能为空");
        }

        // 级联删除所有子菜单
        cascadeDeleteChildren(id);

        // 删除与该菜单关联的角色菜单关系
        LambdaQueryWrapper<RoleMenu> roleMenuWrapper = new LambdaQueryWrapper<>();
        roleMenuWrapper.eq(RoleMenu::getMenuId, id);
        roleMenuMapper.delete(roleMenuWrapper);
        log.info("删除菜单关联的角色菜单关系，菜单ID：{}", id);

        // 删除与该菜单关联的用户菜单关系
        deleteUserMenuRelations(id);

        // 删除菜单
        return removeById(id);
    }

    /**
     * 递归级联删除所有子菜单
     */
    private void cascadeDeleteChildren(Long parentId) {
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Menu::getParentId, parentId);
        List<Menu> children = list(wrapper);

        if (children != null && !children.isEmpty()) {
            for (Menu child : children) {
                // 先删除子菜单的角色菜单关系
                LambdaQueryWrapper<RoleMenu> roleMenuWrapper = new LambdaQueryWrapper<>();
                roleMenuWrapper.eq(RoleMenu::getMenuId, child.getId());
                roleMenuMapper.delete(roleMenuWrapper);
                log.info("删除子菜单关联的角色菜单关系，菜单ID：{}", child.getId());

                // 删除子菜单的用户菜单关系
                deleteUserMenuRelations(child.getId());

                // 递归删除子菜单的子菜单
                cascadeDeleteChildren(child.getId());

                // 删除子菜单
                removeById(child.getId());
                log.info("删除子菜单，菜单ID：{}，菜单名称：{}", child.getId(), child.getMenuName());
            }
        }
    }

    /**
     * 获取菜单树形选择器（用于上级菜单选择）
     */
    @Override
    public List<MenuVO> getMenuTreeSelect() {
        // 查询所有正常状态的菜单
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Menu::getStatus, 1)
               .in(Menu::getMenuType, "M", "C") // 只查询目录和菜单，不包括按钮
               .orderByAsc(Menu::getSort)
               .orderByAsc(Menu::getId);

        List<Menu> allMenus = list(wrapper);
        List<MenuVO> allMenuVOs = allMenus.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 添加顶级节点
        MenuVO topMenu = new MenuVO();
        topMenu.setId(0L);
        topMenu.setParentId(-1L);
        topMenu.setMenuName("顶级菜单");

        List<MenuVO> tree = TreeUtils.buildTree(
                allMenuVOs,
                0L,
                MenuVO::getParentId,
                MenuVO::getId,
                MenuVO::setChildren
        );

        List<MenuVO> result = new ArrayList<>();
        result.add(topMenu);
        result.addAll(tree);

        return result;
    }

    /**
     * 获取菜单树用于权限分配（包含所有类型，不包含顶级菜单）
     */
    @Override
    public List<MenuVO> getMenuTreeForPermission() {
        // 查询所有正常状态的菜单（包括目录、菜单和按钮）
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Menu::getStatus, 1)
               .in(Menu::getMenuType, "M", "C", "F") // 包含所有类型：目录、菜单和按钮
               .orderByAsc(Menu::getSort)
               .orderByAsc(Menu::getId);

        List<Menu> allMenus = list(wrapper);
        List<MenuVO> allMenuVOs = allMenus.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 构建菜单树（不包含顶级菜单节点）
        return TreeUtils.buildTree(
                allMenuVOs,
                0L,
                MenuVO::getParentId,
                MenuVO::getId,
                MenuVO::setChildren
        );
    }

    /**
     * 获取当前登录用户的菜单树
     */
    @Override
    public List<MenuVO> getUserMenuTree() {
        // 从UserContext 获取当前登录用户ID
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        // 查询用户有权限的菜单列表
        List<Menu> userMenus = baseMapper.selectMenusByUserId(userId);

        // 转换为VO并构建树形结构
        List<MenuVO> menuVOs = userMenus.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return TreeUtils.buildTree(
                menuVOs,
                0L,
                MenuVO::getParentId,
                MenuVO::getId,
                MenuVO::setChildren
        );
    }

    /**
     * 实体转VO
     */
    private MenuVO convertToVO(Menu menu) {
        MenuVO vo = new MenuVO();
        BeanUtil.copyProperties(menu, vo);

        // 菜单类型文本（保持硬编码，因为这是系统固定的枚举值，不需要字典）
        String menuType = menu.getMenuType();
        if (menuType != null) {
            switch (menuType) {
                case "M" -> vo.setMenuTypeText("目录");
                case "C" -> vo.setMenuTypeText("菜单");
                case "F" -> vo.setMenuTypeText("按钮");
                default -> vo.setMenuTypeText("未知");
            }
        }

        // 可见文本（保持硬编码，因为这是系统固定的显示/隐藏状态）
        vo.setVisibleText(menu.getVisible() == 1 ? "显示" : "隐藏");

        // 状态文本（使用字典数据）
        vo.setStatusText(DictUtils.getLabel("sys_user_status", menu.getStatus(), "未知"));

        return vo;
    }

    /**
     * 更新菜单状态
     * 如果状态改为关闭，则级联关闭该菜单下的所有子菜单，并删除相关的角色菜单关系
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        Menu menu = getById(id);
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }

        // 如果要启用菜单，需要检查父菜单是否启用
        if (status == 1 && menu.getParentId() != null && menu.getParentId() != 0) {
            Menu parentMenu = getById(menu.getParentId());
            if (parentMenu != null && parentMenu.getStatus() != null && parentMenu.getStatus() == 0) {
                throw new BusinessException("上级菜单处于停用状态，不允许启用菜单");
            }
        }

        menu.setStatus(status);
        boolean result = updateById(menu);

        // 如果状态改为关闭（0），则级联关闭所有子菜单，并删除角色菜单关联
        if (status == 0 && result) {
            // 删除当前菜单的角色菜单关系
            deleteRoleMenuRelations(id);

            // 级联关闭所有子菜单，并删除子菜单的角色菜单关联
            cascadeUpdateChildrenStatus(id, 0);
        }

        return result;
    }

    /**
     * 递归级联更新所有子菜单的状态
     * 如果状态为关闭，同时删除子菜单的角色菜单关联
     */
    private void cascadeUpdateChildrenStatus(Long parentId, Integer status) {
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Menu::getParentId, parentId);
        List<Menu> children = list(wrapper);

        if (children != null && !children.isEmpty()) {
            for (Menu child : children) {
                child.setStatus(status);
                updateById(child);

                // 如果状态为关闭，删除子菜单的角色菜单关联
                if (status == 0) {
                    deleteRoleMenuRelations(child.getId());
                }

                // 递归处理子菜单的子菜单
                cascadeUpdateChildrenStatus(child.getId(), status);
            }
        }
    }

    /**
     * 递归级联更新子菜单的显示状态、菜单状态、页面缓存（仅当父级为关闭时向下传递）
     * 当 status 为 0 时同时删除子菜单的角色菜单关联
     *
     * @param parentId  父菜单ID
     * @param visible   显示状态（仅当为 0 时覆盖子级）
     * @param status    菜单状态（仅当为 0 时覆盖子级）
     * @param keepAlive 页面缓存（仅当为 0 时覆盖子级）
     */
    private void cascadeUpdateChildrenVisibleStatusKeepAlive(
            Long parentId, Integer visible, Integer status, Integer keepAlive) {
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Menu::getParentId, parentId);
        List<Menu> children = list(wrapper);

        if (children != null && !children.isEmpty()) {
            for (Menu child : children) {
                boolean changed = false;
                if (visible != null && visible == 0) {
                    child.setVisible(0);
                    changed = true;
                }
                if (status != null && status == 0) {
                    child.setStatus(0);
                    changed = true;
                }
                if (keepAlive != null && keepAlive == 0) {
                    child.setKeepAlive(0);
                    changed = true;
                }
                if (changed) {
                    updateById(child);
                    if (status != null && status == 0) {
                        deleteRoleMenuRelations(child.getId());
                    }
                }
                cascadeUpdateChildrenVisibleStatusKeepAlive(
                        child.getId(), visible, status, keepAlive);
            }
        }
    }

    /**
     * 删除菜单的角色菜单关联关系
     * @param menuId 菜单ID
     */
    private void deleteRoleMenuRelations(Long menuId) {
        LambdaQueryWrapper<RoleMenu> roleMenuWrapper = new LambdaQueryWrapper<>();
        roleMenuWrapper.eq(RoleMenu::getMenuId, menuId);
        int deletedCount = roleMenuMapper.delete(roleMenuWrapper);
        if (deletedCount > 0) {
            log.info("删除菜单关联的角色菜单关系，菜单ID：{}，删除数量：{}", menuId, deletedCount);
        }
    }

    /**
     * 删除菜单的用户菜单关联关系
     * @param menuId 菜单ID
     */
    private void deleteUserMenuRelations(Long menuId) {
        LambdaQueryWrapper<UserMenu> userMenuWrapper = new LambdaQueryWrapper<>();
        userMenuWrapper.eq(UserMenu::getMenuId, menuId);
        int deletedCount = userMenuMapper.delete(userMenuWrapper);
        if (deletedCount > 0) {
            log.info("删除菜单关联的用户菜单关系，菜单ID：{}，删除数量：{}", menuId, deletedCount);
        }
    }
}
