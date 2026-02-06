package com.project.backend.school.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.project.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 学年实体
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_academic_year")
@Schema(description = "学年实体")
public class AcademicYear extends BaseEntity {

    @Schema(description = "学年编码")
    @TableField("year_code")
    private String yearCode;

    @Schema(description = "学年名称")
    @TableField("year_name")
    private String yearName;

    @Schema(description = "开始日期")
    @TableField("start_date")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    @TableField("end_date")
    private LocalDate endDate;

    @Schema(description = "状态：1启用 0停用")
    @TableField("status")
    private Integer status;
}
