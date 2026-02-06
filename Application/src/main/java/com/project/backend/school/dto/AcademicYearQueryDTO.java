package com.project.backend.school.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 学年查询DTO
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Data
@Schema(description = "学年查询条件")
public class AcademicYearQueryDTO {

    @Schema(description = "学年编码（模糊查询）")
    private String yearCode;

    @Schema(description = "学年名称（模糊查询）")
    private String yearName;

    @Schema(description = "状态：1启用 0停用")
    private Integer status;

    @Schema(description = "当前页码", example = "1")
    private Long pageNum = 1L;

    @Schema(description = "每页条数", example = "10")
    private Long pageSize = 10L;
}
