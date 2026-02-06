package com.project.backend.approval.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 审批记录查询DTO
 * 
 * @author 陈鸿昇
 * @since 2026-01-17
 */
@Data
@Schema(description = "审批记录查询条件")
public class ApprovalRecordQueryDTO {

    @Schema(description = "业务类型")
    private String businessType;

    @Schema(description = "申请人姓名（模糊查询）")
    private String applicantName;

    @Schema(description = "审批人姓名（模糊查询）")
    private String approverName;

    @Schema(description = "操作类型：1通过 2拒绝")
    private Integer action;

    @Schema(description = "当前页码", example = "1")
    private Long pageNum = 1L;

    @Schema(description = "每页条数", example = "10")
    private Long pageSize = 10L;
}
