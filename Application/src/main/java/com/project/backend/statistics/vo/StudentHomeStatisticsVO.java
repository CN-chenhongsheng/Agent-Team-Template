package com.project.backend.statistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 学生首页统计信息VO
 *
 * @author 陈鸿昇
 * @since 2026-01-29
 */
@Data
@Schema(description = "学生首页统计信息响应")
public class StudentHomeStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "学生ID")
    private Long studentId;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "性别：0未知 1男 2女")
    private Integer gender;

    @Schema(description = "性别文本")
    private String genderText;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "当前年级")
    private String currentGrade;

    @Schema(description = "院系名称")
    private String deptName;

    @Schema(description = "专业名称")
    private String majorName;

    @Schema(description = "班级名称")
    private String className;

    @Schema(description = "宿舍信息（如果未分配宿舍则为null）")
    private DormInfo dormInfo;

    /**
     * 宿舍信息内部类
     */
    @Data
    @Schema(description = "宿舍信息")
    public static class DormInfo implements Serializable {

        private static final long serialVersionUID = 1L;

        @Schema(description = "校区名称")
        private String campusName;

        @Schema(description = "楼层名称")
        private String floorName;

        @Schema(description = "房间号")
        private String roomNumber;

        @Schema(description = "床位号")
        private String bedNumber;

        @Schema(description = "完整宿舍地址（格式：校区-楼层-房间号-床位号）")
        private String fullAddress;
    }
}
