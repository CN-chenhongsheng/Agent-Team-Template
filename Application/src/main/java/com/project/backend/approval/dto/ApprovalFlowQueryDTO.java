package com.project.backend.approval.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 审批流程查询DTO
 * 
 * @author 陈鸿昇
 * @since 2026-01-17
 */
@Data
@Schema(description = "审批流程查询条件")
public class ApprovalFlowQueryDTO {

    @Schema(description = "流程名称（模糊查询）")
    private String flowName;

    @Schema(description = "流程编码（模糊查询）")
    private String flowCode;

    @Schema(description = "业务类型")
    private String businessType;

    @Schema(description = "状态：0停用 1启用")
    private Integer status;

    @Schema(description = "当前页码", example = "1")
    private Long pageNum = 1L;

    @Schema(description = "每页条数", example = "10")
    private Long pageSize = 10L;
}
