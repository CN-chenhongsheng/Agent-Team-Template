package com.project.backend.allocation.vo.survey;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 生活习惯问卷列表VO
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Data
@Schema(description = "生活习惯问卷列表VO")
public class LifestyleSurveyListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "学生ID")
    private Long studentId;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "班级名称")
    private String className;

    @Schema(description = "入学年份")
    private Integer enrollmentYear;

    @Schema(description = "填写状态：filled/unfilled")
    private String fillStatus;

    @Schema(description = "填写时间")
    private String fillTime;

    @Schema(description = "问卷状态：0-未填写 1-已填写")
    private Integer surveyStatus;

    @Schema(description = "作息时间文本")
    private String sleepSchedule;

    @Schema(description = "吸烟状态文本")
    private String smokingStatus;

    @Schema(description = "整洁程度文本")
    private String cleanlinessLevel;

    @Schema(description = "社交偏好文本")
    private String socialPreference;
}
