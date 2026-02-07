package com.project.backend.approval.dto;

import com.project.core.dto.BaseQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 审批流程查询DTO
 * 
 * @author 陈鸿昇
 * @since 2026-01-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "审批流程查询条件")
public class ApprovalFlowQueryDTO extends BaseQueryDTO {

    @Schema(description = "流程名称（模糊查询）")
    private String flowName;

    @Schema(description = "流程编码（模糊查询）")
    private String flowCode;

    @Schema(description = "业务类型")
    private String businessType;

    @Schema(description = "状态：0停用 1启用")
    private Integer status;
}
