package com.project.backend.school.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 学期保存DTO
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Data
@Schema(description = "学期保存参数")
public class SemesterSaveDTO {

    @Schema(description = "主键ID（编辑时必填）")
    private Long id;

    @Schema(description = "学期编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "学期编码不能为空")
    private String semesterCode;

    @Schema(description = "学期名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "学期名称不能为空")
    private String semesterName;

    @Schema(description = "开始日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    @Schema(description = "结束日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    @Schema(description = "学期类型")
    private String semesterType;
}
