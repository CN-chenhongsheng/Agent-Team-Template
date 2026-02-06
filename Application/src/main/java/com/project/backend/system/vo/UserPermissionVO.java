package com.project.backend.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户权限VO
 * 包含菜单ID和状态信息
 * 
 * @author 陈鸿昇
 * @since 2025-01-06
 */
@Data
@Schema(description = "用户权限信息响应")
public class UserPermissionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "菜单ID")
    private Long menuId;

    @Schema(description = "菜单状态：1正常 0停用")
    private Integer status;
}
