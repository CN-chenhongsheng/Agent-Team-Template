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
 * 调宿管理实体
 * 
 * @author 陈鸿昇
 * @since 2026-01-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_transfer")
@Schema(description = "调宿管理实体")
public class Transfer extends BaseEntity {

    @Schema(description = "学生ID")
    @TableField("student_id")
    private Long studentId;

    @Schema(description = "学生姓名（冗余）")
    @TableField("student_name")
    private String studentName;

    @Schema(description = "学号（冗余）")
    @TableField("student_no")
    private String studentNo;

    @Schema(description = "原校区编码")
    @TableField("original_campus_code")
    private String originalCampusCode;

    @Schema(description = "原楼层编码")
    @TableField("original_floor_code")
    private String originalFloorCode;

    @Schema(description = "原房间ID")
    @TableField("original_room_id")
    private Long originalRoomId;

    @Schema(description = "原房间编码")
    @TableField("original_room_code")
    private String originalRoomCode;

    @Schema(description = "原床位ID")
    @TableField("original_bed_id")
    private Long originalBedId;

    @Schema(description = "原床位编码")
    @TableField("original_bed_code")
    private String originalBedCode;

    @Schema(description = "目标校区编码")
    @TableField("target_campus_code")
    private String targetCampusCode;

    @Schema(description = "目标楼层编码")
    @TableField("target_floor_code")
    private String targetFloorCode;

    @Schema(description = "目标房间ID")
    @TableField("target_room_id")
    private Long targetRoomId;

    @Schema(description = "目标房间编码")
    @TableField("target_room_code")
    private String targetRoomCode;

    @Schema(description = "目标床位ID")
    @TableField("target_bed_id")
    private Long targetBedId;

    @Schema(description = "目标床位编码")
    @TableField("target_bed_code")
    private String targetBedCode;

    @Schema(description = "申请日期")
    @TableField("apply_date")
    private LocalDate applyDate;

    @Schema(description = "调宿日期")
    @TableField("transfer_date")
    private LocalDate transferDate;

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

    @Schema(description = "调宿原因")
    @TableField("transfer_reason")
    private String transferReason;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;
}
