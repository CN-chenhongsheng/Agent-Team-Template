package com.project.backend.system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户与角色代码映射DTO
 * 用于根据角色代码查询用户列表
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserWithRoleCodeDTO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 角色代码
     */
    private String roleCode;
}
