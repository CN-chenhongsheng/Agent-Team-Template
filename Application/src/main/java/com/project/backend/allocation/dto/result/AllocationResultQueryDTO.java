package com.project.backend.allocation.dto.result;

import com.project.core.dto.BaseQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 分配结果查询DTO
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "分配结果查询请求")
public class AllocationResultQueryDTO extends BaseQueryDTO {

    @Schema(description = "任务ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long taskId;

    @Schema(description = "学号（模糊查询）")
    private String studentNo;

    @Schema(description = "学生姓名（模糊查询）")
    private String studentName;

    @Schema(description = "房间编码")
    private String roomCode;

    @Schema(description = "状态：0-待确认 1-已确认 2-已拒绝 3-已调整")
    private Integer status;

    @Schema(description = "最低匹配分（筛选匹配分>=此值的记录）")
    private BigDecimal minScore;

    @Schema(description = "最高匹配分（筛选匹配分<=此值的记录）")
    private BigDecimal maxScore;

    @Schema(description = "是否只查看问题记录（低于阈值的）")
    private Boolean problemOnly;
}
