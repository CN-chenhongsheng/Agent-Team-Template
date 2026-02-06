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
     * 只返回用户直接分配的权限（不包含角色权限）
     *
     * @param userId 用户ID
     * @return 权限标识列表
     */
    @Select("SELECT DISTINCT m.permission FROM sys_menu m " +
            "INNER JOIN sys_user_menu um ON m.id = um.menu_id " +
            "WHERE um.user_id = #{userId} AND m.status = 1 AND m.permission IS NOT NULL AND m.permission != '' " +
            "AND m.deleted = 0 AND um.deleted = 0")
    List<String> selectPermissionsByUserId(Long userId);

    /**
     * 根据用户ID查询该用户有权限的菜单列表（包含目录、菜单和按钮）
     * 只返回用户直接分配的权限（不包含角色权限）
     * 返回所有正常状态的菜单（包括visible=0的隐藏菜单和按钮权限）
     * 前端会根据visible字段决定是否显示在菜单中，按钮权限用于权限控制
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    @Select("SELECT DISTINCT m.* FROM sys_menu m " +
            "INNER JOIN sys_user_menu um ON m.id = um.menu_id " +
            "WHERE um.user_id = #{userId} AND m.status = 1 " +
            "AND m.deleted = 0 AND um.deleted = 0 " +
            "ORDER BY m.sort ASC, m.id ASC")
    List<Menu> selectMenusByUserId(Long userId);
}
