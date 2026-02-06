package com.project.backend.accommodation.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.project.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 留宿管理实体
 * 
 * @author 陈鸿昇
 * @since 2026-01-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_stay")
@Schema(description = "留宿管理实体")
public class Stay extends BaseEntity {

    @Schema(description = "学生ID")
    @TableField("student_id")
    private Long studentId;

    @Schema(description = "学生姓名（冗余）")
    @TableField("student_name")
    private String studentName;

    @Schema(description = "学号（冗余）")
    @TableField("student_no")
    private String studentNo;

    @Schema(description = "校区编码")
    @TableField("campus_code")
    private String campusCode;

    @Schema(description = "楼层编码")
    @TableField("floor_code")
    private String floorCode;

    @Schema(description = "房间ID")
    @TableField("room_id")
    private Long roomId;

    @Schema(description = "房间编码")
    @TableField("room_code")
    private String roomCode;

    @Schema(description = "床位ID")
    @TableField("bed_id")
    private Long bedId;

    @Schema(description = "床位编码")
    @TableField("bed_code")
    private String bedCode;

    @Schema(description = "申请日期")
    @TableField("apply_date")
    private LocalDate applyDate;

    @Schema(description = "留宿开始日期")
    @TableField("stay_start_date")
    private LocalDate stayStartDate;

    @Schema(description = "留宿结束日期")
    @TableField("stay_end_date")
    private LocalDate stayEndDate;

    @Schema(description = "状态：1待审核 2已通过 3已拒绝 4已完成")
    @TableField("status")
    private Integer status;

    @Schema(description = "审批实例ID")
    @TableField("approval_instance_id")
    private Long approvalInstanceId;

    @Schema(description = "审核人ID")
    @TableField("approver_id")
    private Long approverId;

    @Schema(description = "审核人姓名")
    @TableField("approver_name")
    private String approverName;

    @Schema(description = "审核时间")
    @TableField("approve_time")
    private LocalDateTime approveTime;

    @Schema(description = "审核意见")
    @TableField("approve_opinion")
    private String approveOpinion;

    @Schema(description = "留宿理由（必填）")
    @TableField("stay_reason")
    private String stayReason;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "家长姓名")
    @TableField("parent_name")
    private String parentName;

    @Schema(description = "家长电话")
    @TableField("parent_phone")
    private String parentPhone;

    @Schema(description = "家长是否同意：agree-同意 disagree-不同意")
    @TableField("parent_agree")
    private String parentAgree;

    @Schema(description = "本人签名图片URL")
    @TableField("signature")
    private String signature;

    @Schema(description = "附件图片列表（JSON字符串数组）")
    @TableField("images")
    private String images;
}
