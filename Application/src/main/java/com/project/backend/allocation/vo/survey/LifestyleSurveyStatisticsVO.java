package com.project.backend.allocation.vo.survey;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 生活习惯问卷统计VO
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Data
@Schema(description = "生活习惯问卷统计VO")
public class LifestyleSurveyStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "学生总数")
    private Integer total;

    @Schema(description = "已填写数量")
    private Integer filled;

    @Schema(description = "未填写数量")
    private Integer unfilled;

    @Schema(description = "填写率（百分比）")
    private Double fillRate;
}
