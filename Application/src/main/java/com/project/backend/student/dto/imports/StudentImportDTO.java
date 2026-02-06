package com.project.backend.student.dto.imports;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 学生导入 Excel 行 DTO
 * 列名与前端模板一致；模板中必填列表头会带 *（如 *学号），故关键列同时用 index 绑定，避免表头名不匹配。
 *
 * @author 陈鸿昇
 * @since 2026-02-04
 */
@Data
@Schema(description = "学生导入行数据")
public class StudentImportDTO implements Serializable {

    /** 学号：模板必填列表头可能为 *学号，用 index 保证能读到 */
    @ExcelProperty(index = 0)
    @Schema(description = "学号")
    private String studentNo;

    /** 姓名：模板必填列表头可能为 *姓名，用 index 保证能读到 */
    @ExcelProperty(index = 1)
    @Schema(description = "姓名")
    private String studentName;

    @ExcelProperty(index = 2)
    @Schema(description = "性别（标签：男/女）")
    private String gender;

    @ExcelProperty(index = 3)
    @Schema(description = "身份证号")
    private String idCard;

    @ExcelProperty(index = 4)
    @Schema(description = "手机号")
    private String phone;

    @ExcelProperty(index = 5)
    @Schema(description = "邮箱")
    private String email;

    @ExcelProperty(index = 6)
    @Schema(description = "出生日期")
    private String birthDate;

    @ExcelProperty(index = 7)
    @Schema(description = "民族")
    private String nation;

    @ExcelProperty(index = 8)
    @Schema(description = "政治面貌")
    private String politicalStatus;

    @ExcelProperty(index = 9)
    @Schema(description = "入学年份")
    private String enrollmentYear;

    @ExcelProperty(index = 10)
    @Schema(description = "学制")
    private String schoolingLength;

    @ExcelProperty(index = 11)
    @Schema(description = "当前年级")
    private String currentGrade;

    @ExcelProperty(index = 12)
    @Schema(description = "学籍状态（标签）")
    private String academicStatus;

    /** 校区：模板必填列表头可能为 *校区，用 index 保证能读到 */
    @ExcelProperty(index = 13)
    @Schema(description = "校区名称")
    private String campusName;

    /** 院系：模板必填列表头可能为 *院系，用 index 保证能读到 */
    @ExcelProperty(index = 14)
    @Schema(description = "院系名称")
    private String deptName;

    /** 专业：模板必填列表头可能为 *专业，用 index 保证能读到 */
    @ExcelProperty(index = 15)
    @Schema(description = "专业名称")
    private String majorName;

    /** 班级：模板必填列表头可能为 *班级，用 index 保证能读到 */
    @ExcelProperty(index = 16)
    @Schema(description = "班级名称")
    private String className;

    @ExcelProperty(index = 17)
    @Schema(description = "楼层名称")
    private String floorName;

    @ExcelProperty(index = 18)
    @Schema(description = "房间号")
    private String roomNumber;

    @ExcelProperty(index = 19)
    @Schema(description = "床位号")
    private String bedNumber;

    @ExcelProperty(index = 20)
    @Schema(description = "家长姓名")
    private String parentName;

    @ExcelProperty(index = 21)
    @Schema(description = "家长电话")
    private String parentPhone;

    @ExcelProperty(index = 22)
    @Schema(description = "紧急联系人")
    private String emergencyContact;

    @ExcelProperty(index = 23)
    @Schema(description = "紧急联系电话")
    private String emergencyPhone;

    @ExcelProperty(index = 24)
    @Schema(description = "家庭地址")
    private String homeAddress;

    @ExcelProperty(index = 25)
    @Schema(description = "是否吸烟（标签）")
    private String smokingStatus;

    @ExcelProperty(index = 26)
    @Schema(description = "接受室友吸烟（标签）")
    private String smokingTolerance;

    @ExcelProperty(index = 27)
    @Schema(description = "作息时间（标签）")
    private String sleepSchedule;

    @ExcelProperty(index = 28)
    @Schema(description = "睡眠质量（标签）")
    private String sleepQuality;

    @ExcelProperty(index = 29)
    @Schema(description = "是否打呼噜（标签）")
    private String snores;

    @ExcelProperty(index = 30)
    @Schema(description = "对光线敏感（标签）")
    private String sensitiveToLight;

    @ExcelProperty(index = 31)
    @Schema(description = "对声音敏感（标签）")
    private String sensitiveToSound;

    @ExcelProperty(index = 32)
    @Schema(description = "整洁程度（标签）")
    private String cleanlinessLevel;

    @ExcelProperty(index = 33)
    @Schema(description = "睡前整理习惯（标签）")
    private String bedtimeCleanup;

    @ExcelProperty(index = 34)
    @Schema(description = "社交偏好（标签）")
    private String socialPreference;

    @ExcelProperty(index = 35)
    @Schema(description = "允许访客（标签）")
    private String allowVisitors;

    @ExcelProperty(index = 36)
    @Schema(description = "宿舍打电话习惯（标签）")
    private String phoneCallTime;

    @ExcelProperty(index = 37)
    @Schema(description = "宿舍学习频率（标签）")
    private String studyInRoom;

    @ExcelProperty(index = 38)
    @Schema(description = "学习环境偏好（标签）")
    private String studyEnvironment;

    @ExcelProperty(index = 39)
    @Schema(description = "电脑使用时间（标签）")
    private String computerUsageTime;

    @ExcelProperty(index = 40)
    @Schema(description = "游戏频率（标签）")
    private String gamingPreference;

    @ExcelProperty(index = 41)
    @Schema(description = "听音乐频率（标签）")
    private String musicPreference;

    @ExcelProperty(index = 42)
    @Schema(description = "音乐音量偏好（标签）")
    private String musicVolume;

    @ExcelProperty(index = 43)
    @Schema(description = "宿舍吃东西习惯（标签）")
    private String eatInRoom;

    @ExcelProperty(index = 44)
    @Schema(description = "备注")
    private String remark;
}
