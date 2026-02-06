package com.project.backend.allocation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 生活习惯问卷状态VO
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Data
@Schema(description = "生活习惯问卷状态信息")
public class LifestyleSurveyVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "学生ID")
    private Long studentId;

    @Schema(description = "问卷状态：0-未填写 1-已填写 2-已锁定")
    private Integer surveyStatus;

    @Schema(description = "问卷状态名称")
    private String surveyStatusName;

    @Schema(description = "首次提交时间")
    private LocalDateTime submitTime;

    @Schema(description = "最后修改时间")
    private LocalDateTime lastUpdateTime;

    @Schema(description = "锁定时间")
    private LocalDateTime lockTime;

    @Schema(description = "是否可以修改")
    private Boolean canModify;

    // ==================== 已填写的数据 ====================

    @Schema(description = "吸烟状态")
    private Integer smokingStatus;

    @Schema(description = "是否接受室友吸烟")
    private Integer smokingTolerance;

    @Schema(description = "作息时间")
    private Integer sleepSchedule;

    @Schema(description = "睡眠质量")
    private Integer sleepQuality;

    @Schema(description = "是否打呼噜")
    private Integer snores;

    @Schema(description = "是否对光线敏感")
    private Integer sensitiveToLight;

    @Schema(description = "是否对声音敏感")
    private Integer sensitiveToSound;

    @Schema(description = "整洁程度")
    private Integer cleanlinessLevel;

    @Schema(description = "睡前是否整理")
    private Integer bedtimeCleanup;

    @Schema(description = "社交偏好")
    private Integer socialPreference;

    @Schema(description = "是否允许访客")
    private Integer allowVisitors;

    @Schema(description = "电话时间偏好")
    private Integer phoneCallTime;

    @Schema(description = "是否在宿舍学习")
    private Integer studyInRoom;

    @Schema(description = "学习环境偏好")
    private Integer studyEnvironment;

    @Schema(description = "电脑使用时间")
    private Integer computerUsageTime;

    @Schema(description = "游戏偏好")
    private Integer gamingPreference;

    @Schema(description = "听音乐偏好")
    private Integer musicPreference;

    @Schema(description = "音乐音量偏好")
    private Integer musicVolume;

    @Schema(description = "是否在宿舍吃东西")
    private Integer eatInRoom;
}
