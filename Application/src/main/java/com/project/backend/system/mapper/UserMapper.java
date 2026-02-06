package com.project.backend.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.backend.system.dto.UserWithRoleCodeDTO;
import com.project.backend.system.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统用户Mapper
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据角色代码列表查询用户列表
     *
     * @param roleCodes 角色代码列表
     * @return 用户列表（包含角色代码）
     */
    @Select("<script>" +
            "SELECT DISTINCT u.id, u.username, u.nickname, u.phone, u.email, " +
            "REPLACE(REPLACE(TRIM(r.role_code), CHAR(13), ''), CHAR(10), '') as roleCode " +
            "FROM sys_user u " +
            "INNER JOIN sys_user_role ur ON u.id = ur.user_id " +
            "INNER JOIN sys_role r ON ur.role_id = r.id " +
            "WHERE REPLACE(REPLACE(TRIM(r.role_code), CHAR(13), ''), CHAR(10), '') IN " +
            "<foreach collection='roleCodes' item='roleCode' open='(' separator=',' close=')'>" +
            "#{roleCode}" +
            "</foreach> " +
            "AND u.status = 1 AND r.status = 1 " +
            "AND u.deleted = 0 AND ur.deleted = 0 AND r.deleted = 0 " +
            "ORDER BY REPLACE(REPLACE(TRIM(r.role_code), CHAR(13), ''), CHAR(10), ''), u.id" +
            "</script>")
    List<UserWithRoleCodeDTO> selectUsersByRoleCodes(@Param("roleCodes") List<String> roleCodes);
}
