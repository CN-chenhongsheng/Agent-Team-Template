package com.project.backend.approval.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审批流程绑定展示VO
 * 
 * @author 陈鸿昇
 * @since 2026-01-17
 */
@Data
@Schema(description = "审批流程绑定信息响应")
public class ApprovalFlowBindingVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "业务类型")
    private String businessType;

    @Schema(description = "业务类型文本")
    private String businessTypeText;

    @Schema(description = "绑定的流程ID")
    private Long flowId;

    @Schema(description = "流程名称")
    private String flowName;

    @Schema(description = "流程编码")
    private String flowCode;

    @Schema(description = "状态：0停用 1启用")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
