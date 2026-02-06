package com.project.backend.allocation.dto.config;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 分配配置保存DTO
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Data
@Schema(description = "分配配置保存请求")
public class AllocationConfigSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "配置ID（编辑时必传）")
    private Long id;

    @Schema(description = "配置名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "配置名称不能为空")
    private String configName;

    // ==================== 硬约束开关 ====================

    @Schema(description = "吸烟硬约束：1-启用 0-禁用")
    @Min(value = 0, message = "吸烟硬约束值无效")
    @Max(value = 1, message = "吸烟硬约束值无效")
    private Integer smokingConstraint = 1;

    @Schema(description = "性别硬约束：1-启用 0-禁用")
    @Min(value = 0, message = "性别硬约束值无效")
    @Max(value = 1, message = "性别硬约束值无效")
    private Integer genderConstraint = 1;

    @Schema(description = "作息硬约束：1-启用 0-禁用")
    @Min(value = 0, message = "作息硬约束值无效")
    @Max(value = 1, message = "作息硬约束值无效")
    private Integer sleepHardConstraint = 0;

    // ==================== 维度权重 ====================

    @Schema(description = "睡眠维度权重（0-100）")
    @Min(value = 0, message = "权重不能小于0")
    @Max(value = 100, message = "权重不能大于100")
    private Integer sleepWeight = 30;

    @Schema(description = "吸烟维度权重（0-100）")
    @Min(value = 0, message = "权重不能小于0")
    @Max(value = 100, message = "权重不能大于100")
    private Integer smokingWeight = 20;

    @Schema(description = "整洁维度权重（0-100）")
    @Min(value = 0, message = "权重不能小于0")
    @Max(value = 100, message = "权重不能大于100")
    private Integer cleanlinessWeight = 15;

    @Schema(description = "社交维度权重（0-100）")
    @Min(value = 0, message = "权重不能小于0")
    @Max(value = 100, message = "权重不能大于100")
    private Integer socialWeight = 15;

    @Schema(description = "学习维度权重（0-100）")
    @Min(value = 0, message = "权重不能小于0")
    @Max(value = 100, message = "权重不能大于100")
    private Integer studyWeight = 10;

    @Schema(description = "娱乐维度权重（0-100）")
    @Min(value = 0, message = "权重不能小于0")
    @Max(value = 100, message = "权重不能大于100")
    private Integer entertainmentWeight = 10;

    // ==================== 算法配置 ====================

    @Schema(description = "算法类型：greedy/kmeans/annealing")
    private String algorithmType = "kmeans";

    // ==================== 加分项 ====================

    @Schema(description = "同院系加分")
    @Min(value = 0, message = "加分不能为负")
    private Integer sameDeptBonus = 5;

    @Schema(description = "同专业加分")
    @Min(value = 0, message = "加分不能为负")
    private Integer sameMajorBonus = 10;

    @Schema(description = "同班级加分")
    @Min(value = 0, message = "加分不能为负")
    private Integer sameClassBonus = 15;

    // ==================== 阈值 ====================

    @Schema(description = "最低匹配分阈值")
    @Min(value = 0, message = "阈值不能小于0")
    @Max(value = 100, message = "阈值不能大于100")
    private Integer minMatchScore = 60;

    @Schema(description = "备注")
    private String remark;
}
