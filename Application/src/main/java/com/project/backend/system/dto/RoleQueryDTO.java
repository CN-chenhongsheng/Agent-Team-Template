package com.project.backend.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 角色查询DTO
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Data
@Schema(description = "角色查询条件")
public class RoleQueryDTO {

    @Schema(description = "角色编码（模糊查询）")
    private String roleCode;

    @Schema(description = "角色名称（模糊查询）")
    private String roleName;

    @Schema(description = "状态：1正常 0停用")
    private Integer status;

    @Schema(description = "当前页码", example = "1")
    private Long pageNum = 1L;

    @Schema(description = "每页条数", example = "10")
    private Long pageSize = 10L;
}
