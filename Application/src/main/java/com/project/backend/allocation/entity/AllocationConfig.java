package com.project.backend.allocation.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.project.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分配规则配置实体
 * 存储分配任务的配置参数（硬约束、权重、算法等）
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_allocation_config")
@Schema(description = "分配规则配置实体")
public class AllocationConfig extends BaseEntity {

    @Schema(description = "配置名称")
    @TableField("config_name")
    private String configName;

    // ==================== 硬约束开关 ====================

    @Schema(description = "吸烟硬约束：1-启用 0-禁用（吸烟者不能与不接受吸烟者同住）")
    @TableField("smoking_constraint")
    private Integer smokingConstraint;

    @Schema(description = "性别硬约束：1-启用 0-禁用（不同性别不能同住）")
    @TableField("gender_constraint")
    private Integer genderConstraint;

    @Schema(description = "作息硬约束：1-启用 0-禁用（作息差异≥3档不能同住）")
    @TableField("sleep_hard_constraint")
    private Integer sleepHardConstraint;

    // ==================== 维度权重（总和应为100） ====================

    @Schema(description = "睡眠维度权重（作息、打呼噜、睡眠敏感等）")
    @TableField("sleep_weight")
    private Integer sleepWeight;

    @Schema(description = "吸烟维度权重")
    @TableField("smoking_weight")
    private Integer smokingWeight;

    @Schema(description = "整洁维度权重")
    @TableField("cleanliness_weight")
    private Integer cleanlinessWeight;

    @Schema(description = "社交维度权重（社交偏好、访客、电话等）")
    @TableField("social_weight")
    private Integer socialWeight;

    @Schema(description = "学习维度权重")
    @TableField("study_weight")
    private Integer studyWeight;

    @Schema(description = "娱乐维度权重（电脑、游戏、音乐等）")
    @TableField("entertainment_weight")
    private Integer entertainmentWeight;

    // ==================== 算法配置 ====================

    @Schema(description = "算法类型：greedy-贪心算法 kmeans-聚类算法 annealing-模拟退火")
    @TableField("algorithm_type")
    private String algorithmType;

    // ==================== 额外加分项 ====================

    @Schema(description = "同院系加分")
    @TableField("same_dept_bonus")
    private Integer sameDeptBonus;

    @Schema(description = "同专业加分")
    @TableField("same_major_bonus")
    private Integer sameMajorBonus;

    @Schema(description = "同班级加分")
    @TableField("same_class_bonus")
    private Integer sameClassBonus;

    // ==================== 阈值配置 ====================

    @Schema(description = "最低匹配分阈值（低于此分需人工审核）")
    @TableField("min_match_score")
    private Integer minMatchScore;

    // ==================== 其他字段 ====================

    @Schema(description = "状态：1-启用 0-停用")
    @TableField("status")
    private Integer status;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;
}
