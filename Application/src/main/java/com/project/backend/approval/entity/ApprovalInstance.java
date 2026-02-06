package com.project.backend.approval.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.project.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 审批实例实体
 * 
 * @author 陈鸿昇
 * @since 2026-01-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_approval_instance")
@Schema(description = "审批实例实体")
public class ApprovalInstance extends BaseEntity {

    @Schema(description = "流程ID")
    @TableField("flow_id")
    private Long flowId;

    @Schema(description = "流程名称（冗余）")
    @TableField("flow_name")
    private String flowName;

    @Schema(description = "业务类型")
    @TableField("business_type")
    private String businessType;

    @Schema(description = "业务数据ID")
    @TableField("business_id")
    private Long businessId;

    @Schema(description = "申请人ID")
    @TableField("applicant_id")
    private Long applicantId;

    @Schema(description = "申请人姓名（冗余）")
    @TableField("applicant_name")
    private String applicantName;

    @Schema(description = "当前节点ID")
    @TableField("current_node_id")
    private Long currentNodeId;

    @Schema(description = "当前节点名称（冗余）")
    @TableField("current_node_name")
    private String currentNodeName;

    @Schema(description = "状态：1进行中 2已通过 3已拒绝 4已撤回")
    @TableField("status")
    private Integer status;

    @Schema(description = "流程开始时间")
    @TableField("start_time")
    private LocalDateTime startTime;

    @Schema(description = "流程结束时间")
    @TableField("end_time")
    private LocalDateTime endTime;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;
}
