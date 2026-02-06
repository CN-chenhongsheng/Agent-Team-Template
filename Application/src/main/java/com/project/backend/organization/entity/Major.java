package com.project.backend.organization.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.project.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 专业实体
 * 
 * @author 陈鸿昇
 * @since 2025-12-31
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_major")
@Schema(description = "专业实体")
public class Major extends BaseEntity {

    @Schema(description = "专业编码")
    @TableField("major_code")
    private String majorCode;

    @Schema(description = "专业名称")
    @TableField("major_name")
    private String majorName;

    @Schema(description = "所属院系编码")
    @TableField("dept_code")
    private String deptCode;

    @Schema(description = "专业负责人")
    @TableField("director")
    private String director;

    @Schema(description = "学位类型（字典degree_type）")
    @TableField("type")
    private String type;

    @Schema(description = "学制")
    @TableField("duration")
    private String duration;

    @Schema(description = "培养目标")
    @TableField("goal")
    private String goal;

    @Schema(description = "状态：1启用 0停用")
    @TableField("status")
    private Integer status;
}
