package com.project.backend.approval.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 审批操作DTO
 * 
 * @author 陈鸿昇
 * @since 2026-01-17
 */
@Data
@Schema(description = "审批操作请求")
public class ApprovalActionDTO {

    @Schema(description = "审批实例ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "审批实例ID不能为空")
    private Long instanceId;

    @Schema(description = "操作类型：1通过 2拒绝", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "操作类型不能为空")
    private Integer action;

    @Schema(description = "审批意见")
    private String opinion;
}
