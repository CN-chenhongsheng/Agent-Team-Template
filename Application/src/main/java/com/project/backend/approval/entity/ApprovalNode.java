package com.project.backend.approval.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.project.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 审批流程节点实体
 * 
 * @author 陈鸿昇
 * @since 2026-01-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_approval_node")
@Schema(description = "审批流程节点实体")
public class ApprovalNode extends BaseEntity {

    @Schema(description = "流程ID")
    @TableField("flow_id")
    private Long flowId;

    @Schema(description = "节点名称")
    @TableField("node_name")
    private String nodeName;

    @Schema(description = "节点顺序")
    @TableField("node_order")
    private Integer nodeOrder;

    @Schema(description = "节点类型：1串行 2会签(所有人通过) 3或签(任一人通过)")
    @TableField("node_type")
    private Integer nodeType;

    @Schema(description = "拒绝处理：1直接结束 2退回申请人 3退回上一节点")
    @TableField("reject_action")
    private Integer rejectAction;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;
}
