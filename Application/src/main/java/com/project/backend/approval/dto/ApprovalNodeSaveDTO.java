package com.project.backend.approval.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 审批节点保存DTO
 * 
 * @author 陈鸿昇
 * @since 2026-01-17
 */
@Data
@Schema(description = "审批节点保存请求")
public class ApprovalNodeSaveDTO {

    @Schema(description = "节点ID（编辑时使用）")
    private Long id;

    @Schema(description = "节点名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "节点名称不能为空")
    private String nodeName;

    @Schema(description = "节点顺序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "节点顺序不能为空")
    private Integer nodeOrder;

    @Schema(description = "节点类型：1串行 2会签 3或签")
    private Integer nodeType = 1;

    @Schema(description = "拒绝处理：1直接结束 2退回申请人 3退回上一节点")
    private Integer rejectAction = 1;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "审批人列表")
    @NotNull(message = "审批人不能为空")
    private List<ApprovalAssigneeSaveDTO> assignees;
}
