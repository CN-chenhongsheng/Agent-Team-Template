package com.project.backend.allocation.dto.task;

import com.project.core.dto.BaseQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分配任务查询DTO
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "分配任务查询请求")
public class AllocationTaskQueryDTO extends BaseQueryDTO {

    @Schema(description = "任务名称（模糊查询）")
    private String taskName;

    @Schema(description = "任务类型：1-批量分配 2-单个推荐 3-调宿优化")
    private Integer taskType;

    @Schema(description = "状态：0-待执行 1-执行中 2-已完成 3-部分确认 4-全部确认 5-已取消")
    private Integer status;

    @Schema(description = "目标入学年份")
    private Integer targetEnrollmentYear;

    @Schema(description = "配置ID")
    private Long configId;
}
