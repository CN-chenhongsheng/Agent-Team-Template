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
 * 入住管理实体
 * 
 * @author 陈鸿昇
 * @since 2026-01-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_check_in")
@Schema(description = "入住管理实体")
public class CheckIn extends BaseEntity {

    @Schema(description = "学生ID")
    @TableField("student_id")
    private Long studentId;

    @Schema(description = "学生姓名（冗余）")
    @TableField("student_name")
    private String studentName;

    @Schema(description = "学号（冗余）")
    @TableField("student_no")
    private String studentNo;

    @Schema(description = "入住类型：1长期住宿 2临时住宿")
    @TableField("check_in_type")
    private Integer checkInType;

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

    @Schema(description = "入住日期")
    @TableField("check_in_date")
    private LocalDate checkInDate;

    @Schema(description = "预计退宿日期（临时住宿）")
    @TableField("expected_check_out_date")
    private LocalDate expectedCheckOutDate;

    @Schema(description = "状态：1待审核 2已通过 3已拒绝 4已入住")
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

    @Schema(description = "申请原因/备注")
    @TableField("apply_reason")
    private String applyReason;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;
}
