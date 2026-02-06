package com.project.backend.util;

/**
 * 生活习惯文本转换工具
 * 将生活习惯的数值转换为可读的文本描述（使用字典系统）
 * 
 * @author 陈鸿昇
 * @since 2026-01-07
 */
public class LifestyleTextConverter {

    /**
     * 获取吸烟状态文本
     * @param status 0不吸烟 1吸烟
     */
    public static String getSmokingStatusText(Integer status) {
        return DictUtils.getLabel("student_smoking_status", status, "未填写");
    }

    /**
     * 获取是否接受室友吸烟文本
     * @param tolerance 0不接受 1接受
     */
    public static String getSmokingToleranceText(Integer tolerance) {
        return DictUtils.getLabel("student_smoking_tolerance", tolerance, "未填写");
    }

    /**
     * 获取作息时间文本
     * @param schedule 0早睡早起 1正常 2晚睡晚起 3夜猫子
     */
    public static String getSleepScheduleText(Integer schedule) {
        return DictUtils.getLabel("student_sleep_schedule", schedule, "未填写");
    }

    /**
     * 获取睡眠质量文本
     * @param quality 0浅睡易醒 1正常 2深睡
     */
    public static String getSleepQualityText(Integer quality) {
        return DictUtils.getLabel("student_sleep_quality", quality, "未填写");
    }

    /**
     * 获取是否打呼噜文本
     * @param snores 0不打 1打
     */
    public static String getSnoresText(Integer snores) {
        return DictUtils.getLabel("student_snores", snores, "未填写");
    }

    /**
     * 获取是否对光线敏感文本
     * @param sensitive 0不敏感 1敏感
     */
    public static String getSensitiveToLightText(Integer sensitive) {
        return DictUtils.getLabel("student_sensitive_to_light", sensitive, "未填写");
    }

    /**
     * 获取是否对声音敏感文本
     * @param sensitive 0不敏感 1敏感
     */
    public static String getSensitiveToSoundText(Integer sensitive) {
        return DictUtils.getLabel("student_sensitive_to_sound", sensitive, "未填写");
    }

    /**
     * 获取整洁程度文本
     * @param level 1非常整洁 2整洁 3一般 4随意 5不整洁
     */
    public static String getCleanlinessLevelText(Integer level) {
        return DictUtils.getLabel("student_cleanliness_level", level, "未填写");
    }

    /**
     * 获取睡前是否整理文本
     * @param cleanup 0不整理 1偶尔整理 2经常整理 3总是整理
     */
    public static String getBedtimeCleanupText(Integer cleanup) {
        return DictUtils.getLabel("student_bedtime_cleanup", cleanup, "未填写");
    }

    /**
     * 获取社交偏好文本
     * @param preference 1喜欢安静 2中等 3喜欢热闹
     */
    public static String getSocialPreferenceText(Integer preference) {
        return DictUtils.getLabel("student_social_preference", preference, "未填写");
    }

    /**
     * 获取是否允许室友带访客文本
     * @param allow 0不允许 1偶尔可以 2可以
     */
    public static String getAllowVisitorsText(Integer allow) {
        return DictUtils.getLabel("student_allow_visitors", allow, "未填写");
    }

    /**
     * 获取电话时间偏好文本
     * @param time 0喜欢在宿舍打电话 1偶尔在宿舍 2不在宿舍打电话
     */
    public static String getPhoneCallTimeText(Integer time) {
        return DictUtils.getLabel("student_phone_call_time", time, "未填写");
    }

    /**
     * 获取是否在宿舍学习文本
     * @param study 0不在 1偶尔 2经常 3总是
     */
    public static String getStudyInRoomText(Integer study) {
        return DictUtils.getLabel("student_study_in_room", study, "未填写");
    }

    /**
     * 获取学习环境偏好文本
     * @param environment 1需要安静 2需要轻音乐 3可以接受声音
     */
    public static String getStudyEnvironmentText(Integer environment) {
        return DictUtils.getLabel("student_study_environment", environment, "未填写");
    }

    /**
     * 获取电脑使用时间文本
     * @param time 0不用 1很少 2正常 3很多
     */
    public static String getComputerUsageTimeText(Integer time) {
        return DictUtils.getLabel("student_computer_usage_time", time, "未填写");
    }

    /**
     * 获取游戏偏好文本
     * @param preference 0不玩游戏 1偶尔玩游戏 2经常玩游戏
     */
    public static String getGamingPreferenceText(Integer preference) {
        return DictUtils.getLabel("student_gaming_preference", preference, "未填写");
    }

    /**
     * 获取听音乐偏好文本
     * @param preference 0不听 1偶尔听 2经常听
     */
    public static String getMusicPreferenceText(Integer preference) {
        return DictUtils.getLabel("student_music_preference", preference, "未填写");
    }

    /**
     * 获取音乐音量偏好文本
     * @param volume 1喜欢小声 2中等 3喜欢大声
     */
    public static String getMusicVolumeText(Integer volume) {
        return DictUtils.getLabel("student_music_volume", volume, "未填写");
    }

    /**
     * 获取是否在宿舍吃东西文本
     * @param eat 0不吃 1偶尔 2经常
     */
    public static String getEatInRoomText(Integer eat) {
        return DictUtils.getLabel("student_eat_in_room", eat, "未填写");
    }
}
