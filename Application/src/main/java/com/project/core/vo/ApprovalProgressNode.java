package com.project.core.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审批进度节点信息VO
 * 
 * @author 陈鸿昇
 * @since 2026-01-25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "审批进度节点信息")
public class ApprovalProgressNode implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "节点ID")
    private Long nodeId;

    @Schema(description = "节点名称")
    private String nodeName;

    @Schema(description = "审批人姓名（多个用顿号分隔）")
    private String assigneeNames;

    @Schema(description = "节点状态：1-待处理 2-已通过 3-已拒绝")
    private Integer status;

    @Schema(description = "节点状态文本")
    private String statusText;

    @Schema(description = "审批动作文本")
    private String actionText;

    @Schema(description = "审批时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approveTime;
}
