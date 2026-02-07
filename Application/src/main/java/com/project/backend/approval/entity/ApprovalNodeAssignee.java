package com.project.backend.approval.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.project.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 审批节点审批人表
 *
 * @author 陈鸿昇
 * @since 2026-01-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_approval_node_assignee")
@Schema(description = "审批节点审批人实体")
public class ApprovalNodeAssignee extends BaseEntity {

    @Schema(description = "节点ID")
    @TableField("node_id")
    private Long nodeId;

    @Schema(description = "指派类型：1角色 2用户")
    @TableField("assignee_type")
    private Integer assigneeType;

    @Schema(description = "角色ID或用户ID")
    @TableField("assignee_id")
    private Long assigneeId;

    @Schema(description = "排序顺序")
    @TableField("sort_order")
    private Integer sortOrder;
}
