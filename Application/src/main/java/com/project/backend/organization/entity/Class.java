package com.project.backend.organization.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.project.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 班级实体
 * 
 * @author 陈鸿昇
 * @since 2025-12-31
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_class")
@Schema(description = "班级实体")
public class Class extends BaseEntity {

    @Schema(description = "班级编码")
    @TableField("class_code")
    private String classCode;

    @Schema(description = "班级名称")
    @TableField("class_name")
    private String className;

    @Schema(description = "所属专业编码")
    @TableField("major_code")
    private String majorCode;

    @Schema(description = "年级")
    @TableField("grade")
    private String grade;

    @Schema(description = "负责人姓名（冗余字段）")
    @TableField("teacher_name")
    private String teacherName;

    @Schema(description = "负责人ID（关联sys_user）")
    @TableField("teacher_id")
    private Long teacherId;

    @Schema(description = "入学年份")
    @TableField("enrollment_year")
    private Integer enrollmentYear;

    @Schema(description = "当前人数")
    @TableField("current_count")
    private Integer currentCount;

    @Schema(description = "状态：1启用 0停用")
    @TableField("status")
    private Integer status;
}
