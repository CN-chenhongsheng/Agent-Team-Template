package com.project.backend.organization.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.project.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 校区实体
 * 
 * @author 陈鸿昇
 * @since 2025-12-31
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_campus")
@Schema(description = "校区实体")
public class Campus extends BaseEntity {

    @Schema(description = "校区编码")
    @TableField("campus_code")
    private String campusCode;

    @Schema(description = "校区名称")
    @TableField("campus_name")
    private String campusName;

    @Schema(description = "校区地址")
    @TableField("address")
    private String address;

    @Schema(description = "负责人")
    @TableField("manager")
    private String manager;

    @Schema(description = "状态：1启用 0停用")
    @TableField("status")
    private Integer status;

    @Schema(description = "排序序号")
    @TableField("sort")
    private Integer sort;
}
