package com.project.backend.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.backend.system.entity.RoleMenu;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 角色菜单关联Mapper
 *
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenu> {

    /**
     * 物理删除指定角色的所有已软删除记录
     *
     * @param roleId 角色ID
     * @return 删除的记录数
     */
    @Delete("DELETE FROM sys_role_menu WHERE role_id = #{roleId} AND deleted = 1")
    int deletePhysicallyByRoleId(@Param("roleId") Long roleId);
}
