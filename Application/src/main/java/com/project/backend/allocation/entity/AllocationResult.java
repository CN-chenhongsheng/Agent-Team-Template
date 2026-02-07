package com.project.backend.allocation.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 分配结果实体
 * 存储每次分配任务的详细结果（每个学生的分配信息）
 * 注意：此表不继承BaseEntity，因为不需要deleted字段（结果随任务一起管理）
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Data
@EqualsAndHashCode
@TableName(value = "sys_allocation_result", autoResultMap = true)
@Schema(description = "分配结果实体")
public class AllocationResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "任务ID")
    @TableField("task_id")
    private Long taskId;

    // ==================== 学生信息（冗余） ====================

    @Schema(description = "学生ID")
    @TableField("student_id")
    private Long studentId;

    @Schema(description = "学号")
    @TableField("student_no")
    private String studentNo;

    @Schema(description = "学生姓名")
    @TableField("student_name")
    private String studentName;

    @Schema(description = "性别")
    @TableField("gender")
    private String gender;

    @Schema(description = "院系编码")
    @TableField("dept_code")
    private String deptCode;

    @Schema(description = "专业编码")
    @TableField("major_code")
    private String majorCode;

    @Schema(description = "班级编码")
    @TableField("class_code")
    private String classCode;

    // ==================== 分配目标 ====================

    @Schema(description = "分配的床位ID")
    @TableField("allocated_bed_id")
    private Long allocatedBedId;

    @Schema(description = "分配的房间ID")
    @TableField("allocated_room_id")
    private Long allocatedRoomId;

    @Schema(description = "房间编码")
    @TableField("allocated_room_code")
    private String allocatedRoomCode;

    @Schema(description = "楼层ID")
    @TableField("allocated_floor_id")
    private Long allocatedFloorId;

    @Schema(description = "楼层编码")
    @TableField("allocated_floor_code")
    private String allocatedFloorCode;

    // ==================== 匹配信息 ====================

    @Schema(description = "匹配分数(0-100)")
    @TableField("match_score")
    private BigDecimal matchScore;

    @Schema(description = "匹配详情（各维度得分）")
    @TableField(value = "match_details", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> matchDetails;

    @Schema(description = "不匹配/冲突原因列表")
    @TableField(value = "conflict_reasons", typeHandler = JacksonTypeHandler.class)
    private List<String> conflictReasons;

    @Schema(description = "匹配优势列表")
    @TableField(value = "advantages", typeHandler = JacksonTypeHandler.class)
    private List<String> advantages;

    // ==================== 室友信息（冗余） ====================

    @Schema(description = "室友ID列表")
    @TableField(value = "roommate_ids", typeHandler = JacksonTypeHandler.class)
    private List<Long> roommateIds;

    @Schema(description = "室友姓名列表")
    @TableField(value = "roommate_names", typeHandler = JacksonTypeHandler.class)
    private List<String> roommateNames;

    // ==================== 状态管理 ====================

    @Schema(description = "状态：0-待确认 1-已确认 2-已拒绝 3-已调整")
    @TableField("status")
    private Integer status;

    @Schema(description = "调整后的床位ID")
    @TableField("adjusted_bed_id")
    private Long adjustedBedId;

    @Schema(description = "调整原因")
    @TableField("adjust_reason")
    private String adjustReason;

    @Schema(description = "确认时间")
    @TableField("confirm_time")
    private LocalDateTime confirmTime;

    @Schema(description = "确认人ID")
    @TableField("confirm_by")
    private Long confirmBy;

    // ==================== 时间字段 ====================

    @Schema(description = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
