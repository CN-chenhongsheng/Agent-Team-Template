package com.project.backend.school.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 学年保存DTO
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Data
@Schema(description = "学年保存参数")
public class AcademicYearSaveDTO {

    @Schema(description = "主键ID（编辑时必填）")
    private Long id;

    @Schema(description = "学年编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "学年编码不能为空")
    private String yearCode;

    @Schema(description = "学年名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "学年名称不能为空")
    private String yearName;

    @Schema(description = "开始日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    @Schema(description = "结束日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    @Schema(description = "状态：1启用 0停用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "学期列表")
    @Valid
    private List<SemesterSaveDTO> semesters;
}
