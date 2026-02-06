package com.project.backend.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 菜单保存DTO（新增或编辑）
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Data
@Schema(description = "菜单保存请求")
public class MenuSaveDTO {

    @Schema(description = "菜单ID（编辑时必传）")
    private Long id;

    @Schema(description = "父菜单ID（顶级菜单为0）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "父菜单ID不能为空")
    private Long parentId;

    @Schema(description = "菜单名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "菜单名称不能为空")
    private String menuName;

    @Schema(description = "菜单类型：M目录 C菜单 F按钮", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "菜单类型不能为空")
    private String menuType;

    @Schema(description = "路由路径")
    private String path;

    @Schema(description = "组件路径")
    private String component;

    @Schema(description = "权限标识")
    private String permission;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "排序", example = "0")
    private Integer sort;

    @Schema(description = "是否可见：1显示 0隐藏")
    private Integer visible;

    @Schema(description = "状态：1正常 0停用")
    private Integer status;

    @Schema(description = "页面缓存：1开启 0关闭")
    private Integer keepAlive;
}
