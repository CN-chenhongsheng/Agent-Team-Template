package com.project.backend.repair.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.project.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 报修管理实体
 *
 * @author 陈鸿昇
 * @since 2026-01-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_repair")
@Schema(description = "报修管理实体")
public class Repair extends BaseEntity {

    @Schema(description = "学生ID")
    @TableField("student_id")
    private Long studentId;

    @Schema(description = "房间ID")
    @TableField("room_id")
    private Long roomId;

    @Schema(description = "床位ID")
    @TableField("bed_id")
    private Long bedId;

    @Schema(description = "维修类型：1-水电 2-门窗 3-家具 4-网络 5-其他")
    @TableField("repair_type")
    private Integer repairType;

    @Schema(description = "故障描述")
    @TableField("fault_description")
    private String faultDescription;

    @Schema(description = "故障图片（JSON数组）")
    @TableField("fault_images")
    private String faultImages;

    @Schema(description = "紧急程度：1-一般 2-紧急 3-非常紧急")
    @TableField("urgent_level")
    private Integer urgentLevel;

    @Schema(description = "状态：1-待接单 2-已接单 3-维修中 4-已完成 5-已取消")
    @TableField("status")
    private Integer status;

    @Schema(description = "维修人员ID")
    @TableField("repair_person_id")
    private Long repairPersonId;

    @Schema(description = "维修人员姓名")
    @TableField("repair_person_name")
    private String repairPersonName;

    @Schema(description = "预约时间")
    @TableField("appointment_time")
    private LocalDateTime appointmentTime;

    @Schema(description = "完成时间")
    @TableField("complete_time")
    private LocalDateTime completeTime;

    @Schema(description = "维修结果描述")
    @TableField("repair_result")
    private String repairResult;

    @Schema(description = "维修后图片（JSON数组）")
    @TableField("repair_images")
    private String repairImages;

    @Schema(description = "评分：1-5星")
    @TableField("rating")
    private Integer rating;

    @Schema(description = "评价内容")
    @TableField("rating_comment")
    private String ratingComment;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;
}
