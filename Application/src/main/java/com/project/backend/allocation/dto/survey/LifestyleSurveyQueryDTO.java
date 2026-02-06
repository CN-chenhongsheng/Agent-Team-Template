package com.project.backend.allocation.dto.survey;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 生活习惯问卷查询DTO
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Data
@Schema(description = "生活习惯问卷查询请求")
public class LifestyleSurveyQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "学号（模糊查询）")
    private String studentNo;

    @Schema(description = "学生姓名（模糊查询）")
    private String studentName;

    @Schema(description = "班级编码")
    private String classCode;

    @Schema(description = "入学年份")
    private Integer enrollmentYear;

    @Schema(description = "填写状态：filled/unfilled")
    private String fillStatus;

    @Schema(description = "当前页码", example = "1")
    private Long pageNum = 1L;

    @Schema(description = "每页条数", example = "10")
    private Long pageSize = 10L;
}
