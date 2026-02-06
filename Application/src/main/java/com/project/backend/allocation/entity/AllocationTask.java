package com.project.backend.allocation.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.project.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 分配任务实体
 * 记录每次分配任务的基本信息和执行状态
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_allocation_task", autoResultMap = true)
@Schema(description = "分配任务实体")
public class AllocationTask extends BaseEntity {

    @Schema(description = "任务名称")
    @TableField("task_name")
    private String taskName;

    @Schema(description = "任务类型：1-批量分配 2-单个推荐 3-调宿优化")
    @TableField("task_type")
    private Integer taskType;

    @Schema(description = "使用的配置ID")
    @TableField("config_id")
    private Long configId;

    // ==================== 分配范围（学生筛选条件） ====================

    @Schema(description = "目标入学年份")
    @TableField("target_enrollment_year")
    private Integer targetEnrollmentYear;

    @Schema(description = "目标性别：male/female/不限则为空")
    @TableField("target_gender")
    private String targetGender;

    @Schema(description = "目标校区编码")
    @TableField("target_campus_code")
    private String targetCampusCode;

    @Schema(description = "目标院系编码")
    @TableField("target_dept_code")
    private String targetDeptCode;

    @Schema(description = "目标专业编码")
    @TableField("target_major_code")
    private String targetMajorCode;

    // ==================== 目标床位范围 ====================

    @Schema(description = "目标楼层ID列表")
    @TableField(value = "target_floor_ids", typeHandler = JacksonTypeHandler.class)
    private List<Long> targetFloorIds;

    @Schema(description = "目标楼栋编码列表")
    @TableField(value = "target_building_codes", typeHandler = JacksonTypeHandler.class)
    private List<String> targetBuildingCodes;

    // ==================== 执行统计 ====================

    @Schema(description = "状态：0-待执行 1-执行中 2-已完成 3-部分确认 4-全部确认 5-已取消")
    @TableField("status")
    private Integer status;

    @Schema(description = "待分配学生总数")
    @TableField("total_students")
    private Integer totalStudents;

    @Schema(description = "可用床位总数")
    @TableField("total_beds")
    private Integer totalBeds;

    @Schema(description = "已分配数")
    @TableField("allocated_count")
    private Integer allocatedCount;

    @Schema(description = "已确认数")
    @TableField("confirmed_count")
    private Integer confirmedCount;

    @Schema(description = "分配失败数（无法匹配）")
    @TableField("failed_count")
    private Integer failedCount;

    @Schema(description = "低于阈值数")
    @TableField("low_score_count")
    private Integer lowScoreCount;

    @Schema(description = "平均匹配分")
    @TableField("avg_match_score")
    private BigDecimal avgMatchScore;

    @Schema(description = "最低匹配分")
    @TableField("min_match_score")
    private BigDecimal minMatchScore;

    @Schema(description = "最高匹配分")
    @TableField("max_match_score")
    private BigDecimal maxMatchScore;

    // ==================== 执行时间 ====================

    @Schema(description = "开始执行时间")
    @TableField("start_time")
    private LocalDateTime startTime;

    @Schema(description = "执行完成时间")
    @TableField("end_time")
    private LocalDateTime endTime;

    @Schema(description = "执行耗时（秒）")
    @TableField("execute_duration")
    private Integer executeDuration;

    // ==================== 其他字段 ====================

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;
}
