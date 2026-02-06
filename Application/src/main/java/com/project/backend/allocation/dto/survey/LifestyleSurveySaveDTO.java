package com.project.backend.allocation.dto.survey;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 生活习惯问卷提交DTO
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Data
@Schema(description = "生活习惯问卷提交请求")
public class LifestyleSurveySaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "吸烟状态：0不吸烟 1吸烟")
    private Integer smokingStatus;

    @Schema(description = "是否接受室友吸烟：0不接受 1接受")
    private Integer smokingTolerance;

    @Schema(description = "作息时间：0早睡早起 1正常 2晚睡晚起 3夜猫子")
    private Integer sleepSchedule;

    @Schema(description = "睡眠质量：0浅睡易醒 1正常 2深睡")
    private Integer sleepQuality;

    @Schema(description = "是否打呼噜：0不打 1打")
    private Integer snores;

    @Schema(description = "是否对光线敏感：0不敏感 1敏感")
    private Integer sensitiveToLight;

    @Schema(description = "是否对声音敏感：0不敏感 1敏感")
    private Integer sensitiveToSound;

    @Schema(description = "整洁程度：1非常整洁 2整洁 3一般 4随意 5不整洁")
    private Integer cleanlinessLevel;

    @Schema(description = "睡前是否整理：0不整理 1偶尔整理 2经常整理 3总是整理")
    private Integer bedtimeCleanup;

    @Schema(description = "社交偏好：0喜欢安静 1中等 2喜欢热闹")
    private Integer socialPreference;

    @Schema(description = "是否允许室友带访客：0不允许 1偶尔可以 2可以")
    private Integer allowVisitors;

    @Schema(description = "电话时间偏好：0不喜欢在宿舍打电话 1偶尔在宿舍打电话 2不介意")
    private Integer phoneCallTime;

    @Schema(description = "是否在宿舍学习：0不在 1偶尔 2经常 3总是")
    private Integer studyInRoom;

    @Schema(description = "学习环境偏好：0不需要安静 1需要安静 2需要轻音乐 3可以接受声音")
    private Integer studyEnvironment;

    @Schema(description = "电脑使用时间：0不用 1很少 2正常 3很多")
    private Integer computerUsageTime;

    @Schema(description = "游戏偏好：0不喜欢玩游戏 1偶尔玩游戏 2经常玩游戏")
    private Integer gamingPreference;

    @Schema(description = "听音乐偏好：0不听 1偶尔听 2经常听")
    private Integer musicPreference;

    @Schema(description = "音乐音量偏好：0喜欢小声 1中等 2喜欢大声")
    private Integer musicVolume;

    @Schema(description = "是否在宿舍吃东西：0不吃 1偶尔 2经常")
    private Integer eatInRoom;
}
