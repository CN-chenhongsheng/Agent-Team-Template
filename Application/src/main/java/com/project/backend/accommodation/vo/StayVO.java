package com.project.backend.accommodation.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.backend.common.vo.StudentBasicInfoVO;
import com.project.core.vo.ApprovalProgress;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 留宿管理展示VO
 *
 * @author 陈鸿昇
 * @since 2026-01-06
 */
@Data
@Schema(description = "留宿管理信息响应")
public class StayVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "学生ID")
    private Long studentId;

    @Schema(description = "学生基本信息（嵌套）")
    private StudentBasicInfoVO studentInfo;

    @Schema(description = "房间ID")
    private Long roomId;

    @Schema(description = "房间编码")
    private String roomCode;

    @Schema(description = "床位ID")
    private Long bedId;

    @Schema(description = "床位编码")
    private String bedCode;

    @Schema(description = "申请日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate applyDate;

    @Schema(description = "留宿开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate stayStartDate;

    @Schema(description = "留宿结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate stayEndDate;

    @Schema(description = "状态：1待审核 2已通过 3已拒绝 4已完成")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "审批实例ID")
    private Long approvalInstanceId;

    @Schema(description = "审批进度信息")
    private ApprovalProgress approvalProgress;

    @Schema(description = "审核人ID")
    private Long approverId;

    @Schema(description = "审核人姓名")
    private String approverName;

    @Schema(description = "审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approveTime;

    @Schema(description = "审核意见")
    private String approveOpinion;

    @Schema(description = "留宿理由（必填）")
    private String stayReason;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "家长姓名（留宿申请填写）")
    private String parentName;

    @Schema(description = "家长电话（留宿申请填写）")
    private String parentPhone;

    @Schema(description = "家长是否同意：agree-同意 disagree-不同意")
    private String parentAgree;

    @Schema(description = "家长是否同意文本")
    private String parentAgreeText;

    @Schema(description = "本人签名图片URL")
    private String signature;

    @Schema(description = "附件图片列表（JSON字符串数组）")
    private String images;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
