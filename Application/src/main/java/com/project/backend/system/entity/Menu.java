package com.project.backend.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.project.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统菜单实体
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
@Schema(description = "系统菜单实体")
public class Menu extends BaseEntity {

    @Schema(description = "父菜单ID")
    @TableField("parent_id")
    private Long parentId;

    @Schema(description = "菜单名称")
    @TableField("menu_name")
    private String menuName;

    @Schema(description = "菜单类型：M目录 C菜单 F按钮")
    @TableField("menu_type")
    private String menuType;

    @Schema(description = "路由路径")
    @TableField("path")
    private String path;

    @Schema(description = "组件路径")
    @TableField("component")
    private String component;

    @Schema(description = "权限标识")
    @TableField("permission")
    private String permission;

    @Schema(description = "图标")
    @TableField("icon")
    private String icon;

    @Schema(description = "排序")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "是否可见：1显示 0隐藏")
    @TableField("visible")
    private Integer visible;

    @Schema(description = "状态：1正常 0停用")
    @TableField("status")
    private Integer status;

    @Schema(description = "页面缓存：1开启 0关闭")
    @TableField("keep_alive")
    private Integer keepAlive;
}
