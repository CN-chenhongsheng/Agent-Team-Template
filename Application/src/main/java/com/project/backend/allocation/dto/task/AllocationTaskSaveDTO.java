package com.project.backend.allocation.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分配任务保存DTO
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Data
@Schema(description = "分配任务创建请求")
public class AllocationTaskSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "任务名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "任务名称不能为空")
    private String taskName;

    @Schema(description = "任务类型：1-批量分配 2-单个推荐 3-调宿优化", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "任务类型不能为空")
    private Integer taskType;

    @Schema(description = "使用的配置ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "配置ID不能为空")
    private Long configId;

    // ==================== 学生筛选条件 ====================

    @Schema(description = "目标入学年份")
    private Integer targetEnrollmentYear;

    @Schema(description = "目标性别：male/female/不限则为空")
    private String targetGender;

    @Schema(description = "目标校区编码")
    private String targetCampusCode;

    @Schema(description = "目标院系编码")
    private String targetDeptCode;

    @Schema(description = "目标专业编码")
    private String targetMajorCode;

    // ==================== 床位范围 ====================

    @Schema(description = "目标楼层ID列表")
    private List<Long> targetFloorIds;

    @Schema(description = "目标楼栋编码列表")
    private List<String> targetBuildingCodes;

    @Schema(description = "备注")
    private String remark;
}
