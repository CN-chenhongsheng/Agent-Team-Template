package com.project.backend.allocation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 分配执行进度VO
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Data
@Schema(description = "分配执行进度")
public class AllocationProgressVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "任务状态：0-待执行 1-执行中 2-已完成 5-已取消")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "总学生数")
    private Integer totalStudents;

    @Schema(description = "已处理学生数")
    private Integer processedCount;

    @Schema(description = "成功分配数")
    private Integer successCount;

    @Schema(description = "失败数")
    private Integer failedCount;

    @Schema(description = "进度百分比（0-100）")
    private Integer progressPercent;

    @Schema(description = "当前阶段描述")
    private String currentStage;

    @Schema(description = "预计剩余时间（秒）")
    private Integer estimatedRemaining;

    @Schema(description = "已执行时间（秒）")
    private Integer elapsedTime;

    @Schema(description = "是否完成")
    private Boolean completed;

    @Schema(description = "错误信息（如果执行失败）")
    private String errorMessage;
}
