package com.project.backend.school.dto;

import com.project.core.dto.BaseQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学年查询DTO
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "学年查询条件")
public class AcademicYearQueryDTO extends BaseQueryDTO {

    @Schema(description = "学年编码（模糊查询）")
    private String yearCode;

    @Schema(description = "学年名称（模糊查询）")
    private String yearName;

    @Schema(description = "状态：1启用 0停用")
    private Integer status;
}
