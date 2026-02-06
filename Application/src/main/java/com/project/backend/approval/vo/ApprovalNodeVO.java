package com.project.backend.approval.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 审批节点展示VO
 * 
 * @author 陈鸿昇
 * @since 2026-01-17
 */
@Data
@Schema(description = "审批节点信息响应")
public class ApprovalNodeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "流程ID")
    private Long flowId;

    @Schema(description = "节点名称")
    private String nodeName;

    @Schema(description = "节点顺序")
    private Integer nodeOrder;

    @Schema(description = "节点类型：1串行 2会签 3或签")
    private Integer nodeType;

    @Schema(description = "节点类型文本")
    private String nodeTypeText;

    @Schema(description = "拒绝处理：1直接结束 2退回申请人 3退回上一节点")
    private Integer rejectAction;

    @Schema(description = "拒绝处理文本")
    private String rejectActionText;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "审批人列表")
    private List<ApprovalAssigneeVO> assignees;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
