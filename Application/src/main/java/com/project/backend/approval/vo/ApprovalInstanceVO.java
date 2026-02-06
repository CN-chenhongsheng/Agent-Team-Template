package com.project.backend.approval.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.backend.common.vo.StudentBasicInfoVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 审批实例展示VO
 *
 * @author 陈鸿昇
 * @since 2026-01-17
 */
@Data
@Schema(description = "审批实例信息响应")
public class ApprovalInstanceVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "流程ID")
    private Long flowId;

    @Schema(description = "流程名称")
    private String flowName;

    @Schema(description = "业务类型")
    private String businessType;

    @Schema(description = "业务类型文本")
    private String businessTypeText;

    @Schema(description = "业务数据ID")
    private Long businessId;

    @Schema(description = "申请人ID")
    private Long applicantId;

    @Schema(description = "申请人姓名")
    private String applicantName;

    @Schema(description = "学生基本信息（当业务类型为学生相关业务时返回）")
    private StudentBasicInfoVO studentInfo;

    @Schema(description = "当前节点ID")
    private Long currentNodeId;

    @Schema(description = "当前节点名称")
    private String currentNodeName;

    @Schema(description = "状态：1进行中 2已通过 3已拒绝 4已撤回")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "流程开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "流程结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "流程节点列表（含审批状态）")
    private List<ApprovalNodeVO> nodes;

    @Schema(description = "审批记录列表")
    private List<ApprovalRecordVO> records;

    @Schema(description = "当前用户是否可审批")
    private Boolean canApprove;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
