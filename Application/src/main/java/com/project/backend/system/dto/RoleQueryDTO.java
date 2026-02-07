package com.project.backend.system.dto;

import com.project.core.dto.BaseQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色查询DTO
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "角色查询条件")
public class RoleQueryDTO extends BaseQueryDTO {

    @Schema(description = "角色编码（模糊查询）")
    private String roleCode;

    @Schema(description = "角色名称（模糊查询）")
    private String roleName;

    @Schema(description = "状态：1正常 0停用")
    private Integer status;
}
