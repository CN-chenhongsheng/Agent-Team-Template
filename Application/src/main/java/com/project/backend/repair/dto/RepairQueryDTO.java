package com.project.backend.repair.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 报修查询DTO
 *
 * @author 陈鸿昇
 * @since 2026-01-29
 */
@Data
@Schema(description = "报修查询DTO")
public class RepairQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "当前页码", example = "1")
    private Long pageNum = 1L;

    @Schema(description = "每页条数", example = "10")
    private Long pageSize = 10L;

    @Schema(description = "学生ID")
    private Long studentId;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "房间编码")
    private String roomCode;

    @Schema(description = "维修类型：1-水电 2-门窗 3-家具 4-网络 5-其他")
    private Integer repairType;

    @Schema(description = "紧急程度：1-一般 2-紧急 3-非常紧急")
    private Integer urgentLevel;

    @Schema(description = "状态：1-待接单 2-已接单 3-维修中 4-已完成 5-已取消")
    private Integer status;

    @Schema(description = "维修人员ID")
    private Long repairPersonId;

    @Schema(description = "维修人员姓名")
    private String repairPersonName;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "关键词搜索（学生姓名/学号/房间编码）")
    private String keyword;
}
