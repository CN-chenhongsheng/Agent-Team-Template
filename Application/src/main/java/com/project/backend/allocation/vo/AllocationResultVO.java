package com.project.backend.allocation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 分配结果VO
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Data
@Schema(description = "分配结果信息")
public class AllocationResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "结果ID")
    private Long id;

    @Schema(description = "任务ID")
    private Long taskId;

    // ==================== 学生信息 ====================

    @Schema(description = "学生ID")
    private Long studentId;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "性别")
    private String gender;

    @Schema(description = "性别名称")
    private String genderName;

    @Schema(description = "院系编码")
    private String deptCode;

    @Schema(description = "院系名称")
    private String deptName;

    @Schema(description = "专业编码")
    private String majorCode;

    @Schema(description = "专业名称")
    private String majorName;

    @Schema(description = "班级编码")
    private String classCode;

    @Schema(description = "班级名称")
    private String className;

    // ==================== 分配目标 ====================

    @Schema(description = "分配的床位ID")
    private Long allocatedBedId;

    @Schema(description = "分配的房间ID")
    private Long allocatedRoomId;

    @Schema(description = "房间编码")
    private String allocatedRoomCode;

    @Schema(description = "楼层ID")
    private Long allocatedFloorId;

    @Schema(description = "楼层编码")
    private String allocatedFloorCode;

    @Schema(description = "楼栋名称")
    private String buildingName;

    // ==================== 匹配信息 ====================

    @Schema(description = "匹配分数(0-100)")
    private BigDecimal matchScore;

    @Schema(description = "匹配分数等级：优秀/良好/一般/较差")
    private String matchScoreLevel;

    @Schema(description = "匹配详情")
    private Map<String, Object> matchDetails;

    @Schema(description = "冲突原因列表")
    private List<String> conflictReasons;

    @Schema(description = "匹配优势列表")
    private List<String> advantages;

    // ==================== 室友信息 ====================

    @Schema(description = "室友ID列表")
    private List<Long> roommateIds;

    @Schema(description = "室友姓名列表")
    private List<String> roommateNames;

    @Schema(description = "室友数量")
    private Integer roommateCount;

    // ==================== 状态 ====================

    @Schema(description = "状态：0-待确认 1-已确认 2-已拒绝 3-已调整")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "调整后的床位ID")
    private Long adjustedBedId;

    @Schema(description = "调整原因")
    private String adjustReason;

    @Schema(description = "确认时间")
    private LocalDateTime confirmTime;

    @Schema(description = "确认人")
    private String confirmByName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 根据匹配分数计算等级
     */
    public void calculateMatchScoreLevel() {
        if (matchScore == null) {
            this.matchScoreLevel = "未知";
            return;
        }
        int score = matchScore.intValue();
        if (score >= 80) {
            this.matchScoreLevel = "优秀";
        } else if (score >= 60) {
            this.matchScoreLevel = "良好";
        } else if (score >= 40) {
            this.matchScoreLevel = "一般";
        } else {
            this.matchScoreLevel = "较差";
        }
    }
}
