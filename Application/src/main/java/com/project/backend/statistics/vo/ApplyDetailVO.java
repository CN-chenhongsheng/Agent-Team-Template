package com.project.backend.statistics.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 申请详情VO
 * 统一的申请详情响应，支持四种申请类型（入住/调宿/退宿/留宿）
 *
 * @author 陈鸿昇
 * @since 2026-01-29
 */
@Data
@Schema(description = "申请详情信息响应")
public class ApplyDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // ========== 基础信息 ==========
    @Schema(description = "申请ID")
    private Long id;

    @Schema(description = "申请类型：check_in-入住 transfer-调宿 check_out-退宿 stay-留宿")
    private String applyType;

    @Schema(description = "申请类型文本")
    private String applyTypeText;

    @Schema(description = "状态：1待审核 2已通过 3已拒绝 4已完成")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "申请日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate applyDate;

    @Schema(description = "申请原因/理由")
    private String reason;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approveTime;

    // ========== 审批流程信息 ==========
    @Schema(description = "审批实例ID")
    private Long approvalInstanceId;

    @Schema(description = "审批实例状态：1进行中 2已通过 3已拒绝 4已撤回")
    private Integer approvalStatus;

    @Schema(description = "审批实例状态文本")
    private String approvalStatusText;

    @Schema(description = "流程名称")
    private String flowName;

    @Schema(description = "当前审批节点名称")
    private String currentNodeName;

    @Schema(description = "审批意见")
    private String approveOpinion;

    @Schema(description = "审批步骤列表")
    private List<ApprovalStepVO> approvalSteps;

    // ========== 入住申请特有字段 ==========
    @Schema(description = "入住类型：1长期住宿 2临时住宿（仅入住申请）")
    private Integer checkInType;

    @Schema(description = "入住类型文本")
    private String checkInTypeText;

    @Schema(description = "预计退宿日期（临时住宿）")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expectedCheckOutDate;

    @Schema(description = "入住日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkInDate;

    @Schema(description = "目标床位ID（仅入住申请）")
    private Long targetBedId;

    @Schema(description = "目标校区名称（仅入住申请）")
    private String targetCampusName;

    @Schema(description = "目标楼层名称（仅入住申请）")
    private String targetFloorName;

    @Schema(description = "目标房间号（仅入住申请）")
    private String targetRoomNumber;

    @Schema(description = "目标床位号（仅入住申请）")
    private String targetBedNumber;

    @Schema(description = "目标宿舍完整地址（仅入住申请）")
    private String targetDormFullAddress;

    // ========== 调宿申请特有字段 ==========
    @Schema(description = "原宿舍地址（仅调宿申请）")
    private String originalDormAddress;

    @Schema(description = "目标宿舍地址（仅调宿申请）")
    private String targetDormAddress;

    @Schema(description = "调宿日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate transferDate;

    // ========== 退宿申请特有字段 ==========
    @Schema(description = "退宿日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkOutDate;

    @Schema(description = "退宿原因（仅退宿申请）")
    private String checkOutReason;

    // ========== 留宿申请特有字段 ==========
    @Schema(description = "留宿开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate stayStartDate;

    @Schema(description = "留宿结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate stayEndDate;

    @Schema(description = "家长姓名（仅留宿申请）")
    private String parentName;

    @Schema(description = "家长电话（仅留宿申请）")
    private String parentPhone;

    @Schema(description = "家长是否同意（仅留宿申请）：agree-同意 disagree-不同意")
    private String parentAgree;

    @Schema(description = "家长是否同意文本")
    private String parentAgreeText;

    @Schema(description = "留宿原因（仅留宿申请）")
    private String stayReason;

    /**
     * 审批步骤VO（内部类）
     */
    @Data
    @Schema(description = "审批步骤信息")
    public static class ApprovalStepVO implements Serializable {

        private static final long serialVersionUID = 1L;

        @Schema(description = "步骤序号")
        private Integer stepOrder;

        @Schema(description = "节点ID")
        private Long nodeId;

        @Schema(description = "节点名称")
        private String nodeName;

        @Schema(description = "审批人ID")
        private Long approverId;

        @Schema(description = "审批人姓名")
        private String approverName;

        @Schema(description = "审批状态：1待审批 2已通过 3已拒绝")
        private Integer status;

        @Schema(description = "审批状态文本")
        private String statusText;

        @Schema(description = "操作类型：1通过 2拒绝")
        private Integer action;

        @Schema(description = "操作文本")
        private String actionText;

        @Schema(description = "审批意见")
        private String opinion;

        @Schema(description = "审批时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime approveTime;
    }
}
