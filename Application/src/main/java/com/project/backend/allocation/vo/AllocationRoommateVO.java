package com.project.backend.allocation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 分配模块室友信息VO
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Data
@Schema(description = "分配模块室友信息VO")
public class AllocationRoommateVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "学生ID")
    private Long studentId;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "性别：1男 2女")
    private Integer gender;

    @Schema(description = "班级名称")
    private String className;

    @Schema(description = "作息时间：0早睡早起 1正常 2晚睡晚起 3夜猫子")
    private Integer sleepSchedule;

    @Schema(description = "社交偏好：0喜欢安静 1中等 2喜欢热闹")
    private Integer socialPreference;

    /**
     * 获取作息时间文本
     */
    public String getSleepScheduleText() {
        if (sleepSchedule == null) {
            return "未知";
        }
        return switch (sleepSchedule) {
            case 0 -> "早睡早起";
            case 1 -> "正常作息";
            case 2 -> "晚睡晚起";
            case 3 -> "夜猫子";
            default -> "未知";
        };
    }

    /**
     * 获取社交偏好文本
     */
    public String getSocialPreferenceText() {
        if (socialPreference == null) {
            return "未知";
        }
        return switch (socialPreference) {
            case 0 -> "喜欢安静";
            case 1 -> "中等";
            case 2 -> "喜欢热闹";
            default -> "未知";
        };
    }
}
