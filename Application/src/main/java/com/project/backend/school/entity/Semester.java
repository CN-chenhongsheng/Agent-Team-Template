package com.project.backend.school.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.project.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 学期实体
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_semester")
@Schema(description = "学期实体")
public class Semester extends BaseEntity {

    @Schema(description = "所属学年ID")
    @TableField("academic_year_id")
    private Long academicYearId;

    @Schema(description = "学期编码")
    @TableField("semester_code")
    private String semesterCode;

    @Schema(description = "学期名称")
    @TableField("semester_name")
    private String semesterName;

    @Schema(description = "开始日期")
    @TableField("start_date")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    @TableField("end_date")
    private LocalDate endDate;

    @Schema(description = "学期类型")
    @TableField("semester_type")
    private String semesterType;
}
