package com.project.backend.allocation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 床位推荐VO
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Data
@Schema(description = "床位推荐信息")
public class BedRecommendVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "房间ID")
    private Long roomId;

    @Schema(description = "房间编码")
    private String roomCode;

    @Schema(description = "房间号")
    private String roomNumber;

    @Schema(description = "楼层编码")
    private String floorCode;

    @Schema(description = "床位ID")
    private Long bedId;

    @Schema(description = "床位编码")
    private String bedCode;

    @Schema(description = "平均匹配分")
    private BigDecimal matchScore;

    @Schema(description = "最低匹配分")
    private BigDecimal minMatchScore;

    @Schema(description = "最高匹配分")
    private BigDecimal maxMatchScore;

    @Schema(description = "匹配分等级")
    private String matchScoreLevel;

    @Schema(description = "现有室友数量")
    private Integer roommateCount;

    @Schema(description = "匹配优势")
    private List<String> advantages;

    @Schema(description = "潜在冲突")
    private List<String> conflicts;

    /**
     * 获取匹配分等级
     */
    public String getMatchScoreLevel() {
        if (matchScore == null) return "未知";
        int score = matchScore.intValue();
        if (score >= 80) return "优秀";
        if (score >= 60) return "良好";
        if (score >= 40) return "一般";
        return "较差";
    }
}
