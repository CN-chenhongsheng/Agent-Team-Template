package com.project.backend.allocation.algorithm.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 两个学生之间的匹配结果
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "两个学生之间的匹配结果")
public class MatchResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "学生A的ID")
    private Long studentAId;

    @Schema(description = "学生B的ID")
    private Long studentBId;

    @Schema(description = "总匹配分数(0-100)")
    private BigDecimal totalScore;

    @Schema(description = "各维度得分详情")
    @Builder.Default
    private Map<String, DimensionScore> dimensions = new HashMap<>();

    @Schema(description = "所有冲突原因汇总")
    @Builder.Default
    private List<String> conflicts = new ArrayList<>();

    @Schema(description = "所有匹配优势汇总")
    @Builder.Default
    private List<String> advantages = new ArrayList<>();

    @Schema(description = "是否存在硬约束冲突（如果为true，则不能同住）")
    private Boolean hasHardConflict;

    @Schema(description = "硬约束冲突原因（如果hasHardConflict=true）")
    private String hardConflictReason;

    /**
     * 计算总分（汇总各维度加权得分）
     */
    public void calculateTotalScore() {
        if (dimensions == null || dimensions.isEmpty()) {
            this.totalScore = BigDecimal.ZERO;
            return;
        }

        BigDecimal total = BigDecimal.ZERO;
        for (DimensionScore ds : dimensions.values()) {
            if (ds.getWeightedScore() != null) {
                total = total.add(ds.getWeightedScore());
            }
        }
        this.totalScore = total;
    }

    /**
     * 汇总所有维度的冲突和优势
     */
    public void aggregateConflictsAndAdvantages() {
        this.conflicts = new ArrayList<>();
        this.advantages = new ArrayList<>();

        if (dimensions != null) {
            for (DimensionScore ds : dimensions.values()) {
                if (ds.getConflicts() != null) {
                    this.conflicts.addAll(ds.getConflicts());
                }
                if (ds.getAdvantages() != null) {
                    this.advantages.addAll(ds.getAdvantages());
                }
            }
        }
    }

    /**
     * 添加维度得分
     */
    public void addDimensionScore(DimensionScore dimensionScore) {
        if (this.dimensions == null) {
            this.dimensions = new HashMap<>();
        }
        this.dimensions.put(dimensionScore.getDimension(), dimensionScore);
    }

    /**
     * 转换为可存储的Map格式（用于JSON存储）
     */
    public Map<String, Object> toStorageMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("totalScore", totalScore);
        map.put("hasHardConflict", hasHardConflict);
        map.put("hardConflictReason", hardConflictReason);

        Map<String, Object> dimMap = new HashMap<>();
        if (dimensions != null) {
            for (Map.Entry<String, DimensionScore> entry : dimensions.entrySet()) {
                DimensionScore ds = entry.getValue();
                Map<String, Object> dimDetail = new HashMap<>();
                dimDetail.put("score", ds.getScore());
                dimDetail.put("weight", ds.getWeight());
                dimDetail.put("weightedScore", ds.getWeightedScore());
                dimDetail.put("conflicts", ds.getConflicts());
                dimDetail.put("advantages", ds.getAdvantages());
                dimMap.put(entry.getKey(), dimDetail);
            }
        }
        map.put("dimensions", dimMap);

        return map;
    }
}
