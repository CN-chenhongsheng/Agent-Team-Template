package com.project.backend.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 菜单查询DTO
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Data
@Schema(description = "菜单查询条件")
public class MenuQueryDTO {

    @Schema(description = "菜单名称（模糊查询）")
    private String menuName;

    @Schema(description = "菜单类型：M目录 C菜单 F按钮")
    private String menuType;

    @Schema(description = "状态：1正常 0停用")
    private Integer status;
}
