package com.project.backend.organization.dto.classes;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 班级保存DTO
 * 
 * @author 陈鸿昇
 * @since 2025-12-31
 */
@Data
@Schema(description = "班级保存参数")
public class ClassSaveDTO {

    @Schema(description = "主键ID（编辑时必填）")
    private Long id;

    @Schema(description = "班级编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "班级编码不能为空")
    private String classCode;

    @Schema(description = "班级名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "班级名称不能为空")
    private String className;

    @Schema(description = "所属专业编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "所属专业不能为空")
    private String majorCode;

    @Schema(description = "年级")
    private String grade;

    @Schema(description = "负责人姓名")
    private String teacherName;

    @Schema(description = "负责人ID")
    private Long teacherId;

    @Schema(description = "入学年份")
    private Integer enrollmentYear;

    @Schema(description = "状态：1启用 0停用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;
}
