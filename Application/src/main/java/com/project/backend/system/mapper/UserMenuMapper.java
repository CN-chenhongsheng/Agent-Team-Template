package com.project.backend.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.backend.system.entity.UserMenu;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户菜单关联Mapper
 *
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Mapper
public interface UserMenuMapper extends BaseMapper<UserMenu> {

    /**
     * 物理删除指定用户的所有已软删除记录
     *
     * @param userId 用户ID
     * @return 删除的记录数
     */
    @Delete("DELETE FROM sys_user_menu WHERE user_id = #{userId} AND deleted = 1")
    int deletePhysicallyByUserId(@Param("userId") Long userId);
}
