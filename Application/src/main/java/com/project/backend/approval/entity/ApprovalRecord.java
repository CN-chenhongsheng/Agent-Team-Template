package com.project.backend.approval.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审批记录实体
 * 
 * @author 陈鸿昇
 * @since 2026-01-17
 */
@Data
@TableName("sys_approval_record")
@Schema(description = "审批记录实体")
public class ApprovalRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "审批实例ID")
    @TableField("instance_id")
    private Long instanceId;

    @Schema(description = "节点ID")
    @TableField("node_id")
    private Long nodeId;

    @Schema(description = "节点名称（冗余）")
    @TableField("node_name")
    private String nodeName;

    @Schema(description = "审批人ID")
    @TableField("approver_id")
    private Long approverId;

    @Schema(description = "审批人姓名（冗余）")
    @TableField("approver_name")
    private String approverName;

    @Schema(description = "操作类型：1通过 2拒绝")
    @TableField("action")
    private Integer action;

    @Schema(description = "审批意见")
    @TableField("opinion")
    private String opinion;

    @Schema(description = "审批时间")
    @TableField("approve_time")
    private LocalDateTime approveTime;

    @Schema(description = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
