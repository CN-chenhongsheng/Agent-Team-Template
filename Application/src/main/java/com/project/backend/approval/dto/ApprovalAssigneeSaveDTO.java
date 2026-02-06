package com.project.backend.approval.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 审批人保存DTO
 * 
 * @author 陈鸿昇
 * @since 2026-01-17
 */
@Data
@Schema(description = "审批人保存请求")
public class ApprovalAssigneeSaveDTO {

    @Schema(description = "指派类型：1角色 2用户", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "指派类型不能为空")
    private Integer assigneeType;

    @Schema(description = "角色ID或用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "指派对象不能为空")
    private Long assigneeId;

    @Schema(description = "指派对象名称（前端显示用）")
    private String assigneeName;
}
