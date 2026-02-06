package com.project.backend.allocation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 分配任务VO
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Data
@Schema(description = "分配任务信息")
public class AllocationTaskVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "任务ID")
    private Long id;

    @Schema(description = "任务名称")
    private String taskName;

    @Schema(description = "任务类型：1-批量分配 2-单个推荐 3-调宿优化")
    private Integer taskType;

    @Schema(description = "任务类型名称")
    private String taskTypeName;

    @Schema(description = "配置ID")
    private Long configId;

    @Schema(description = "配置名称")
    private String configName;

    // ==================== 分配范围 ====================

    @Schema(description = "目标入学年份")
    private Integer targetEnrollmentYear;

    @Schema(description = "目标性别")
    private String targetGender;

    @Schema(description = "目标校区编码")
    private String targetCampusCode;

    @Schema(description = "目标校区名称")
    private String targetCampusName;

    @Schema(description = "目标院系编码")
    private String targetDeptCode;

    @Schema(description = "目标院系名称")
    private String targetDeptName;

    @Schema(description = "目标专业编码")
    private String targetMajorCode;

    @Schema(description = "目标专业名称")
    private String targetMajorName;

    @Schema(description = "目标楼层ID列表")
    private List<Long> targetFloorIds;

    @Schema(description = "目标楼栋编码列表")
    private List<String> targetBuildingCodes;

    // ==================== 执行统计 ====================

    @Schema(description = "状态：0-待执行 1-执行中 2-已完成 3-部分确认 4-全部确认 5-已取消")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "待分配学生总数")
    private Integer totalStudents;

    @Schema(description = "可用床位总数")
    private Integer totalBeds;

    @Schema(description = "已分配数")
    private Integer allocatedCount;

    @Schema(description = "已确认数")
    private Integer confirmedCount;

    @Schema(description = "分配失败数")
    private Integer failedCount;

    @Schema(description = "低于阈值数")
    private Integer lowScoreCount;

    @Schema(description = "平均匹配分")
    private BigDecimal avgMatchScore;

    @Schema(description = "最低匹配分")
    private BigDecimal minMatchScore;

    @Schema(description = "最高匹配分")
    private BigDecimal maxMatchScore;

    // ==================== 时间 ====================

    @Schema(description = "开始执行时间")
    private LocalDateTime startTime;

    @Schema(description = "执行完成时间")
    private LocalDateTime endTime;

    @Schema(description = "执行耗时（秒）")
    private Integer executeDuration;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "创建人")
    private String createByName;
}
