package com.project.backend.allocation.algorithm.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 分配结果DTO
 * 算法返回的单条分配结果
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "单条分配结果")
public class AllocationResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "学生ID")
    private Long studentId;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "性别")
    private String gender;

    @Schema(description = "院系编码")
    private String deptCode;

    @Schema(description = "专业编码")
    private String majorCode;

    @Schema(description = "班级编码")
    private String classCode;

    @Schema(description = "分配的床位ID")
    private Long bedId;

    @Schema(description = "分配的房间ID")
    private Long roomId;

    @Schema(description = "房间编码")
    private String roomCode;

    @Schema(description = "楼层ID")
    private Long floorId;

    @Schema(description = "楼层编码")
    private String floorCode;

    @Schema(description = "匹配分数")
    private BigDecimal matchScore;

    @Schema(description = "匹配详情")
    private Map<String, Object> matchDetails;

    @Schema(description = "冲突原因")
    private List<String> conflictReasons;

    @Schema(description = "匹配优势")
    private List<String> advantages;

    @Schema(description = "室友ID列表")
    private List<Long> roommateIds;

    @Schema(description = "室友姓名列表")
    private List<String> roommateNames;

    @Schema(description = "是否分配成功")
    private boolean success;

    @Schema(description = "失败原因（如果分配失败）")
    private String failReason;
}
