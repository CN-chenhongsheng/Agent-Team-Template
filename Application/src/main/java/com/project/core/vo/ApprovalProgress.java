package com.project.core.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 审批进度信息VO
 * 
 * @author 陈鸿昇
 * @since 2026-01-25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "审批进度信息")
public class ApprovalProgress implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "审批状态：1-待审核 2-已通过 3-已拒绝 4-已完成")
    private Integer status;

    @Schema(description = "审批状态文本")
    private String statusText;

    @Schema(description = "申请人姓名")
    private String applicantName;

    @Schema(description = "流程发起时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "当前审批节点名称")
    private String currentNodeName;

    @Schema(description = "下一审批人姓名")
    private String nextApproverName;

    @Schema(description = "审批进度描述文本")
    private String progressText;

    @Schema(description = "已完成节点数")
    private Integer completedNodes;

    @Schema(description = "节点总数")
    private Integer totalNodes;

    @Schema(description = "审批进度百分比")
    private Integer progressPercent;

    @Schema(description = "审批流程节点进度列表")
    private List<ApprovalProgressNode> nodeTimeline;
}
