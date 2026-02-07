package com.project.backend.student.dto.student;

import com.project.core.dto.BaseQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学生查询DTO
 * 
 * @author 陈鸿昇
 * @since 2026-01-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "学生查询条件")
public class StudentQueryDTO extends BaseQueryDTO {

    @Schema(description = "学号（模糊查询）")
    private String studentNo;

    @Schema(description = "姓名（模糊查询）")
    private String studentName;

    @Schema(description = "手机号（模糊查询）")
    private String phone;

    @Schema(description = "性别：1男 2女")
    private Integer gender;

    @Schema(description = "校区编码")
    private String campusCode;

    @Schema(description = "院系编码")
    private String deptCode;

    @Schema(description = "专业编码")
    private String majorCode;

    @Schema(description = "班级ID")
    private Long classId;

    @Schema(description = "床位ID")
    private Long bedId;

    @Schema(description = "民族（字典student_nation）")
    private String nation;

    @Schema(description = "政治面貌（字典student_political_status）")
    private String politicalStatus;

    @Schema(description = "学籍状态（字典academic_status）：1在读 2休学 3毕业 4退学")
    private Integer academicStatus;

    @Schema(description = "状态：1启用 0停用")
    private Integer status;
}
