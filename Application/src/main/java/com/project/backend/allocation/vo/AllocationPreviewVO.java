package com.project.backend.allocation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 分配预览VO
 * 在执行分配前，预览待分配的数据统计
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Data
@Schema(description = "分配预览信息")
public class AllocationPreviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // ==================== 学生统计 ====================

    @Schema(description = "符合条件的学生总数")
    private Integer totalStudents;

    @Schema(description = "已填写问卷的学生数")
    private Integer surveyFilledCount;

    @Schema(description = "未填写问卷的学生数")
    private Integer surveyUnfilledCount;

    @Schema(description = "问卷填写率（百分比）")
    private Double surveyFillRate;

    @Schema(description = "已分配床位的学生数（不参与本次分配）")
    private Integer alreadyAllocatedCount;

    @Schema(description = "待分配学生数（可参与本次分配）")
    private Integer toBeAllocatedCount;

    // ==================== 床位统计 ====================

    @Schema(description = "目标范围内的房间总数")
    private Integer totalRooms;

    @Schema(description = "可用床位总数")
    private Integer totalAvailableBeds;

    @Schema(description = "空房间数")
    private Integer emptyRoomCount;

    @Schema(description = "部分入住房间数")
    private Integer partialRoomCount;

    // ==================== 分配可行性 ====================

    @Schema(description = "床位是否充足")
    private Boolean bedsEnough;

    @Schema(description = "差额数量（正数表示床位多余，负数表示床位不足）")
    private Integer bedDifference;

    // ==================== 习惯分布统计 ====================

    @Schema(description = "作息时间分布")
    private Map<String, Integer> sleepScheduleDistribution;

    @Schema(description = "吸烟状态分布")
    private Map<String, Integer> smokingStatusDistribution;

    @Schema(description = "整洁程度分布")
    private Map<String, Integer> cleanlinessDistribution;

    @Schema(description = "社交偏好分布")
    private Map<String, Integer> socialPreferenceDistribution;

    // ==================== 警告信息 ====================

    @Schema(description = "警告信息列表")
    private List<String> warnings;

    @Schema(description = "是否可以执行分配")
    private Boolean canExecute;

    @Schema(description = "不能执行的原因")
    private String cannotExecuteReason;
}
