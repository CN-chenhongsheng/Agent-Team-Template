package com.project.backend.approval.dto;

import com.project.core.dto.BaseQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 审批实例查询DTO
 * 
 * @author 陈鸿昇
 * @since 2026-01-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "审批实例查询条件")
public class ApprovalInstanceQueryDTO extends BaseQueryDTO {

    @Schema(description = "业务类型")
    private String businessType;

    @Schema(description = "申请人姓名（模糊查询）")
    private String applicantName;

    @Schema(description = "流程名称（模糊查询）")
    private String flowName;

    @Schema(description = "状态：1进行中 2已通过 3已拒绝 4已撤回")
    private Integer status;
}
