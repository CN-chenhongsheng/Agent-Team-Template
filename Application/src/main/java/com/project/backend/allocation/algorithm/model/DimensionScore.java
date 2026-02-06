package com.project.backend.allocation.algorithm.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 单维度匹配得分
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "单维度匹配得分")
public class DimensionScore implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "维度名称：sleep/smoking/cleanliness/social/study/entertainment")
    private String dimension;

    @Schema(description = "维度中文名称")
    private String dimensionName;

    @Schema(description = "该维度原始得分(0-100)")
    private Integer score;

    @Schema(description = "配置的权重百分比")
    private Integer weight;

    @Schema(description = "加权后得分 = score * weight / 100")
    private BigDecimal weightedScore;

    @Schema(description = "该维度的冲突原因列表")
    @Builder.Default
    private List<String> conflicts = new ArrayList<>();

    @Schema(description = "该维度的匹配优势列表")
    @Builder.Default
    private List<String> advantages = new ArrayList<>();

    /**
     * 计算加权得分
     */
    public void calculateWeightedScore() {
        if (score != null && weight != null) {
            this.weightedScore = BigDecimal.valueOf(score)
                    .multiply(BigDecimal.valueOf(weight))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
    }

    /**
     * 添加冲突原因
     */
    public void addConflict(String conflict) {
        if (this.conflicts == null) {
            this.conflicts = new ArrayList<>();
        }
        this.conflicts.add(conflict);
    }

    /**
     * 添加匹配优势
     */
    public void addAdvantage(String advantage) {
        if (this.advantages == null) {
            this.advantages = new ArrayList<>();
        }
        this.advantages.add(advantage);
    }
}
