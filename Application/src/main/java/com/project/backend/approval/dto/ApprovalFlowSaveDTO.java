package com.project.backend.approval.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 审批流程保存DTO
 * 
 * @author 陈鸿昇
 * @since 2026-01-17
 */
@Data
@Schema(description = "审批流程保存请求")
public class ApprovalFlowSaveDTO {

    @Schema(description = "流程ID（编辑时必传）")
    private Long id;

    @Schema(description = "流程名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "流程名称不能为空")
    private String flowName;

    @Schema(description = "流程编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "流程编码不能为空")
    private String flowCode;

    @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "业务类型不能为空")
    private String businessType;

    @Schema(description = "流程描述")
    private String description;

    @Schema(description = "状态：0停用 1启用")
    private Integer status = 1;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "流程节点列表")
    @NotNull(message = "流程节点不能为空")
    private List<ApprovalNodeSaveDTO> nodes;
}
