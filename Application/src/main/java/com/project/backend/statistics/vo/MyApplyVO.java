package com.project.backend.statistics.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 我的申请VO
 *
 * @author 陈鸿昇
 * @since 2026-01-29
 */
@Data
@Schema(description = "我的申请信息响应")
public class MyApplyVO implements Serializable {

    private static final long serialVersionUID = 1L;

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

    @Schema(description = "审批实例ID")
    private Long approvalInstanceId;

    @Schema(description = "审核意见")
    private String approveOpinion;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

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
}
