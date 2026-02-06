package com.project.backend.student.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学生展示VO
 * 
 * @author 陈鸿昇
 * @since 2026-01-06
 */
@Data
@Schema(description = "学生信息响应")
public class StudentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "姓名")
    private String studentName;

    @Schema(description = "性别（字典sys_user_sex）：0未知 1男 2女")
    private Integer gender;

    @Schema(description = "性别文本")
    private String genderText;

    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "出生日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @Schema(description = "民族")
    private String nation;

    @Schema(description = "政治面貌")
    private String politicalStatus;

    @Schema(description = "入学年份")
    private Integer enrollmentYear;

    @Schema(description = "学制（年）")
    private Integer schoolingLength;

    @Schema(description = "当前年级")
    private String currentGrade;

    @Schema(description = "学籍状态（字典academic_status）：1在读 2休学 3毕业 4退学")
    private Integer academicStatus;

    @Schema(description = "学籍状态文本")
    private String academicStatusText;

    @Schema(description = "家庭地址")
    private String homeAddress;

    @Schema(description = "紧急联系人")
    private String emergencyContact;

    @Schema(description = "紧急联系人电话")
    private String emergencyPhone;

    @Schema(description = "家长姓名")
    private String parentName;

    @Schema(description = "家长电话")
    private String parentPhone;

    @Schema(description = "校区编码")
    private String campusCode;

    @Schema(description = "校区名称")
    private String campusName;

    @Schema(description = "院系编码")
    private String deptCode;

    @Schema(description = "院系名称")
    private String deptName;

    @Schema(description = "专业编码")
    private String majorCode;

    @Schema(description = "专业名称")
    private String majorName;

    @Schema(description = "班级ID")
    private Long classId;

    @Schema(description = "班级编码")
    private String classCode;

    @Schema(description = "班级名称")
    private String className;

    @Schema(description = "楼层ID")
    private Long floorId;

    @Schema(description = "楼层编码")
    private String floorCode;

    @Schema(description = "楼层名称")
    private String floorName;

    @Schema(description = "房间ID")
    private Long roomId;

    @Schema(description = "房间编码")
    private String roomCode;

    @Schema(description = "房间名称")
    private String roomName;

    @Schema(description = "床位ID")
    private Long bedId;

    @Schema(description = "床位编码")
    private String bedCode;

    @Schema(description = "床位名称")
    private String bedName;

    @Schema(description = "状态：1启用 0停用")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // ========== 生活习惯字段 ==========
    @Schema(description = "吸烟状态：0不吸烟 1吸烟")
    private Integer smokingStatus;

    @Schema(description = "吸烟状态文本")
    private String smokingStatusText;

    @Schema(description = "是否接受室友吸烟：0不接受 1接受")
    private Integer smokingTolerance;

    @Schema(description = "是否接受室友吸烟文本")
    private String smokingToleranceText;

    @Schema(description = "作息时间：0早睡早起 1正常 2晚睡晚起 3夜猫子")
    private Integer sleepSchedule;

    @Schema(description = "作息时间文本")
    private String sleepScheduleText;

    @Schema(description = "睡眠质量：0浅睡易醒 1正常 2深睡")
    private Integer sleepQuality;

    @Schema(description = "睡眠质量文本")
    private String sleepQualityText;

    @Schema(description = "是否打呼噜：0不打 1打")
    private Integer snores;

    @Schema(description = "是否打呼噜文本")
    private String snoresText;

    @Schema(description = "是否对光线敏感：0不敏感 1敏感")
    private Integer sensitiveToLight;

    @Schema(description = "是否对光线敏感文本")
    private String sensitiveToLightText;

    @Schema(description = "是否对声音敏感：0不敏感 1敏感")
    private Integer sensitiveToSound;

    @Schema(description = "是否对声音敏感文本")
    private String sensitiveToSoundText;

    @Schema(description = "整洁程度：1非常整洁 2整洁 3一般 4随意 5不整洁")
    private Integer cleanlinessLevel;

    @Schema(description = "整洁程度文本")
    private String cleanlinessLevelText;

    @Schema(description = "睡前是否整理：0不整理 1偶尔整理 2经常整理 3总是整理")
    private Integer bedtimeCleanup;

    @Schema(description = "睡前是否整理文本")
    private String bedtimeCleanupText;

    @Schema(description = "社交偏好：0喜欢安静 1中等 2喜欢热闹")
    private Integer socialPreference;

    @Schema(description = "社交偏好文本")
    private String socialPreferenceText;

    @Schema(description = "是否允许室友带访客：0不允许 1偶尔可以 2可以")
    private Integer allowVisitors;

    @Schema(description = "是否允许室友带访客文本")
    private String allowVisitorsText;

    @Schema(description = "电话时间偏好：0不喜欢在宿舍打电话 1偶尔在宿舍打电话 2不在宿舍打电话")
    private Integer phoneCallTime;

    @Schema(description = "电话时间偏好文本")
    private String phoneCallTimeText;

    @Schema(description = "是否在宿舍学习：0不在 1偶尔 2经常 3总是")
    private Integer studyInRoom;

    @Schema(description = "是否在宿舍学习文本")
    private String studyInRoomText;

    @Schema(description = "学习环境偏好：0不需要安静 1需要安静 2需要轻音乐 3可以接受声音")
    private Integer studyEnvironment;

    @Schema(description = "学习环境偏好文本")
    private String studyEnvironmentText;

    @Schema(description = "电脑使用时间：0不用 1很少 2正常 3很多")
    private Integer computerUsageTime;

    @Schema(description = "电脑使用时间文本")
    private String computerUsageTimeText;

    @Schema(description = "游戏偏好：0不喜欢玩游戏 1偶尔玩游戏 2经常玩游戏")
    private Integer gamingPreference;

    @Schema(description = "游戏偏好文本")
    private String gamingPreferenceText;

    @Schema(description = "听音乐偏好：0不喜欢听音乐 1偶尔听音乐 2经常听音乐")
    private Integer musicPreference;

    @Schema(description = "听音乐偏好文本")
    private String musicPreferenceText;

    @Schema(description = "音乐音量偏好：0喜欢小声 1中等 2喜欢大声")
    private Integer musicVolume;

    @Schema(description = "音乐音量偏好文本")
    private String musicVolumeText;

    @Schema(description = "是否在宿舍吃东西：0不吃 1偶尔 2经常")
    private Integer eatInRoom;

    @Schema(description = "是否在宿舍吃东西文本")
    private String eatInRoomText;
}
