package com.project.backend.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 角色用户VO
 * 包含角色信息和该角色的用户列表
 * 
 * @author 陈鸿昇
 * @since 2025-01-01
 */
@Data
@Schema(description = "角色用户信息响应")
public class RoleUserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "角色编码")
    private String roleCode;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "用户列表")
    private List<UserSimpleVO> users;
}
