package com.project.backend.allocation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 分配配置VO
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Data
@Schema(description = "分配配置信息")
public class AllocationConfigVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "配置ID")
    private Long id;

    @Schema(description = "配置名称")
    private String configName;

    // ==================== 硬约束 ====================

    @Schema(description = "吸烟硬约束：1-启用 0-禁用")
    private Integer smokingConstraint;

    @Schema(description = "性别硬约束：1-启用 0-禁用")
    private Integer genderConstraint;

    @Schema(description = "作息硬约束：1-启用 0-禁用")
    private Integer sleepHardConstraint;

    // ==================== 权重 ====================

    @Schema(description = "睡眠维度权重")
    private Integer sleepWeight;

    @Schema(description = "吸烟维度权重")
    private Integer smokingWeight;

    @Schema(description = "整洁维度权重")
    private Integer cleanlinessWeight;

    @Schema(description = "社交维度权重")
    private Integer socialWeight;

    @Schema(description = "学习维度权重")
    private Integer studyWeight;

    @Schema(description = "娱乐维度权重")
    private Integer entertainmentWeight;

    // ==================== 算法 ====================

    @Schema(description = "算法类型")
    private String algorithmType;

    @Schema(description = "算法类型名称")
    private String algorithmTypeName;

    // ==================== 加分项 ====================

    @Schema(description = "同院系加分")
    private Integer sameDeptBonus;

    @Schema(description = "同专业加分")
    private Integer sameMajorBonus;

    @Schema(description = "同班级加分")
    private Integer sameClassBonus;

    // ==================== 阈值 ====================

    @Schema(description = "最低匹配分阈值")
    private Integer minMatchScore;

    // ==================== 其他 ====================

    @Schema(description = "状态：1-启用 0-停用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
