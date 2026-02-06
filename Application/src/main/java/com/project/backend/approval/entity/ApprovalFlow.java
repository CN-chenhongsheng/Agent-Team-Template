package com.project.backend.approval.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.project.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 审批流程定义实体
 * 
 * @author 陈鸿昇
 * @since 2026-01-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_approval_flow")
@Schema(description = "审批流程定义实体")
public class ApprovalFlow extends BaseEntity {

    @Schema(description = "流程名称")
    @TableField("flow_name")
    private String flowName;

    @Schema(description = "流程编码（唯一）")
    @TableField("flow_code")
    private String flowCode;

    @Schema(description = "业务类型")
    @TableField("business_type")
    private String businessType;

    @Schema(description = "流程描述")
    @TableField("description")
    private String description;

    @Schema(description = "状态：0停用 1启用")
    @TableField("status")
    private Integer status;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;
}
