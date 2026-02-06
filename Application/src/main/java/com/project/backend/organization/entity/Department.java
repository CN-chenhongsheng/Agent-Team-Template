package com.project.backend.organization.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.project.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 院系实体
 * 
 * @author 陈鸿昇
 * @since 2025-12-31
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_department")
@Schema(description = "院系实体")
public class Department extends BaseEntity {

    @Schema(description = "院系编码")
    @TableField("dept_code")
    private String deptCode;

    @Schema(description = "院系名称")
    @TableField("dept_name")
    private String deptName;

    @Schema(description = "所属校区编码")
    @TableField("campus_code")
    private String campusCode;

    @Schema(description = "院系领导")
    @TableField("leader")
    private String leader;

    @Schema(description = "联系电话")
    @TableField("phone")
    private String phone;

    @Schema(description = "排序")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "状态：1启用 0停用")
    @TableField("status")
    private Integer status;
}
