package com.project.backend.approval.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 审批人展示VO
 * 
 * @author 陈鸿昇
 * @since 2026-01-17
 */
@Data
@Schema(description = "审批人信息响应")
public class ApprovalAssigneeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "节点ID")
    private Long nodeId;

    @Schema(description = "指派类型：1角色 2用户")
    private Integer assigneeType;

    @Schema(description = "指派类型文本")
    private String assigneeTypeText;

    @Schema(description = "角色ID或用户ID")
    private Long assigneeId;

    @Schema(description = "指派对象名称")
    private String assigneeName;

    @Schema(description = "排序顺序")
    private Integer sortOrder;
}
