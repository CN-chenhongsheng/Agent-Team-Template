package com.project.backend.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.backend.system.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统菜单Mapper
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Mapper
public interface MenuMapper extends BaseMapper<Menu> {

    /**
     * 根据用户ID查询权限标识列表
     * 权限来源为用户菜单与生效角色菜单的交集
     *
     * @param userId 用户ID
     * @return 权限标识列表
     */
    @Select("SELECT DISTINCT m.permission FROM sys_menu m " +
            "INNER JOIN sys_user_menu um ON m.id = um.menu_id " +
            "WHERE um.user_id = #{userId} AND m.status = 1 AND m.permission IS NOT NULL AND m.permission != '' " +
            "AND m.deleted = 0 AND um.deleted = 0 " +
            "AND EXISTS (SELECT 1 FROM sys_user_role ur " +
            "            INNER JOIN sys_role r ON ur.role_id = r.id " +
            "            INNER JOIN sys_role_menu rm ON rm.role_id = r.id " +
            "            WHERE ur.user_id = #{userId} AND r.status = 1 " +
            "              AND r.deleted = 0 AND ur.deleted = 0 AND rm.deleted = 0 " +
            "              AND rm.menu_id = m.id)")
    List<String> selectPermissionsByUserId(Long userId);

    /**
     * 查询所有有效权限标识
     *
     * @return 权限标识列表
     */
    @Select("SELECT DISTINCT permission FROM sys_menu " +
            "WHERE status = 1 AND permission IS NOT NULL AND permission != '' AND deleted = 0")
    List<String> selectAllPermissions();

    /**
     * 根据用户ID查询该用户有权限的菜单列表（包含目录、菜单和按钮）
     * 权限来源为用户菜单与生效角色菜单的交集
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    @Select("SELECT DISTINCT m.* FROM sys_menu m " +
            "INNER JOIN sys_user_menu um ON m.id = um.menu_id " +
            "WHERE um.user_id = #{userId} AND m.status = 1 " +
            "AND m.deleted = 0 AND um.deleted = 0 " +
            "AND EXISTS (SELECT 1 FROM sys_user_role ur " +
            "            INNER JOIN sys_role r ON ur.role_id = r.id " +
            "            INNER JOIN sys_role_menu rm ON rm.role_id = r.id " +
            "            WHERE ur.user_id = #{userId} AND r.status = 1 " +
            "              AND r.deleted = 0 AND ur.deleted = 0 AND rm.deleted = 0 " +
            "              AND rm.menu_id = m.id) " +
            "ORDER BY m.sort ASC, m.id ASC")
    List<Menu> selectMenusByUserId(Long userId);
}
