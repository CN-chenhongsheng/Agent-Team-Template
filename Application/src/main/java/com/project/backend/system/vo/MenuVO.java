package com.project.backend.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单展示VO
 * 
 * @author 陈鸿昇
 * @since 2025-12-30
 */
@Data
@Schema(description = "菜单信息响应")
public class MenuVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "父菜单ID")
    private Long parentId;

    @Schema(description = "菜单名称")
    private String menuName;

    @Schema(description = "菜单类型：M目录 C菜单 F按钮")
    private String menuType;

    @Schema(description = "菜单类型文本")
    private String menuTypeText;

    @Schema(description = "路由路径")
    private String path;

    @Schema(description = "组件路径")
    private String component;

    @Schema(description = "权限标识")
    private String permission;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "是否可见：1显示 0隐藏")
    private Integer visible;

    @Schema(description = "可见文本")
    private String visibleText;

    @Schema(description = "状态：1正常 0停用")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "页面缓存：1开启 0关闭")
    private Integer keepAlive;

    @Schema(description = "子菜单列表")
    private List<MenuVO> children;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
