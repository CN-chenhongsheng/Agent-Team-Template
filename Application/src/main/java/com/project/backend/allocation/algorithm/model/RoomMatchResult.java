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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 学生与房间（多室友）的综合匹配结果
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "学生与房间的综合匹配结果")
public class RoomMatchResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "目标学生ID")
    private Long studentId;

    @Schema(description = "目标房间ID")
    private Long roomId;

    @Schema(description = "目标床位ID")
    private Long bedId;

    @Schema(description = "与所有室友的平均匹配分")
    private BigDecimal avgScore;

    @Schema(description = "最低匹配分（与最不匹配的室友）")
    private BigDecimal minScore;

    @Schema(description = "最高匹配分（与最匹配的室友）")
    private BigDecimal maxScore;

    @Schema(description = "与每个室友的详细匹配结果")
    @Builder.Default
    private List<MatchResult> roommateMatches = new ArrayList<>();

    @Schema(description = "整体冲突原因（去重汇总）")
    @Builder.Default
    private List<String> overallConflicts = new ArrayList<>();

    @Schema(description = "整体匹配优势（去重汇总）")
    @Builder.Default
    private List<String> overallAdvantages = new ArrayList<>();

    @Schema(description = "是否存在硬约束冲突（与任一室友）")
    private Boolean hasHardConflict;

    @Schema(description = "硬约束冲突详情")
    @Builder.Default
    private List<String> hardConflictReasons = new ArrayList<>();

    @Schema(description = "室友数量")
    private Integer roommateCount;

    /**
     * 从室友匹配结果列表计算综合结果
     */
    public void calculateFromRoommateMatches() {
        if (roommateMatches == null || roommateMatches.isEmpty()) {
            this.avgScore = BigDecimal.valueOf(100); // 空房间默认满分
            this.minScore = BigDecimal.valueOf(100);
            this.maxScore = BigDecimal.valueOf(100);
            this.roommateCount = 0;
            this.hasHardConflict = false;
            return;
        }

        this.roommateCount = roommateMatches.size();
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal min = BigDecimal.valueOf(100);
        BigDecimal max = BigDecimal.ZERO;
        this.hasHardConflict = false;
        this.hardConflictReasons = new ArrayList<>();

        // 用于去重
        Set<String> conflictSet = new LinkedHashSet<>();
        Set<String> advantageSet = new LinkedHashSet<>();

        for (MatchResult mr : roommateMatches) {
            BigDecimal score = mr.getTotalScore() != null ? mr.getTotalScore() : BigDecimal.ZERO;
            total = total.add(score);

            if (score.compareTo(min) < 0) {
                min = score;
            }
            if (score.compareTo(max) > 0) {
                max = score;
            }

            // 检查硬约束冲突
            if (Boolean.TRUE.equals(mr.getHasHardConflict())) {
                this.hasHardConflict = true;
                if (mr.getHardConflictReason() != null) {
                    this.hardConflictReasons.add(mr.getHardConflictReason());
                }
            }

            // 收集冲突和优势
            if (mr.getConflicts() != null) {
                conflictSet.addAll(mr.getConflicts());
            }
            if (mr.getAdvantages() != null) {
                advantageSet.addAll(mr.getAdvantages());
            }
        }

        this.avgScore = total.divide(BigDecimal.valueOf(roommateCount), 2, RoundingMode.HALF_UP);
        this.minScore = min;
        this.maxScore = max;
        this.overallConflicts = new ArrayList<>(conflictSet);
        this.overallAdvantages = new ArrayList<>(advantageSet);
    }

    /**
     * 添加室友匹配结果
     */
    public void addRoommateMatch(MatchResult matchResult) {
        if (this.roommateMatches == null) {
            this.roommateMatches = new ArrayList<>();
        }
        this.roommateMatches.add(matchResult);
    }

    /**
     * 获取硬约束冲突原因（返回第一条，兼容旧接口）
     */
    public String getHardConflictReason() {
        if (hardConflictReasons != null && !hardConflictReasons.isEmpty()) {
            return hardConflictReasons.get(0);
        }
        return null;
    }
}
