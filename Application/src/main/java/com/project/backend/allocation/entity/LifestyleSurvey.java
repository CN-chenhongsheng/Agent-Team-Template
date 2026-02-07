package com.project.backend.allocation.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 生活习惯问卷状态实体
 * 跟踪学生问卷填写状态（是否填写、是否锁定等）
 * 注意：此表不继承BaseEntity，因为不需要deleted字段
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Data
@EqualsAndHashCode
@TableName("sys_lifestyle_survey")
@Schema(description = "生活习惯问卷状态实体")
public class LifestyleSurvey implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "学生ID")
    @TableField("student_id")
    private Long studentId;

    @Schema(description = "状态：0-未填写 1-已填写 2-已锁定（分配后）")
    @TableField("survey_status")
    private Integer surveyStatus;

    @Schema(description = "首次提交时间")
    @TableField("submit_time")
    private LocalDateTime submitTime;

    @Schema(description = "最后修改时间")
    @TableField("last_update_time")
    private LocalDateTime lastUpdateTime;

    @Schema(description = "锁定时间（分配确认后锁定）")
    @TableField("lock_time")
    private LocalDateTime lockTime;

    @Schema(description = "问卷版本号")
    @TableField("survey_version")
    private Integer surveyVersion;

    @Schema(description = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
