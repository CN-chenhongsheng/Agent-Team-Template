package com.project.backend.approval.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 审批流程绑定DTO
 * 
 * @author 陈鸿昇
 * @since 2026-01-17
 */
@Data
@Schema(description = "审批流程绑定请求")
public class ApprovalFlowBindingDTO {

    @Schema(description = "绑定ID（编辑时必传）")
    private Long id;

    @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "业务类型不能为空")
    private String businessType;

    @Schema(description = "绑定的流程ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "流程ID不能为空")
    private Long flowId;

    @Schema(description = "状态：0停用 1启用")
    private Integer status = 1;

    @Schema(description = "备注")
    private String remark;
}
