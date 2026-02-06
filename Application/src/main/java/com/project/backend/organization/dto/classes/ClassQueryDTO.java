package com.project.backend.organization.dto.classes;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 班级查询DTO
 * 
 * @author 陈鸿昇
 * @since 2025-12-31
 */
@Data
@Schema(description = "班级查询条件")
public class ClassQueryDTO {

    @Schema(description = "班级编码（模糊查询）")
    private String classCode;

    @Schema(description = "班级名称（模糊查询）")
    private String className;

    @Schema(description = "所属专业编码")
    private String majorCode;

    @Schema(description = "所属院系编码（冗余查询条件）")
    private String deptCode;

    @Schema(description = "所属校区编码（冗余查询条件）")
    private String campusCode;

    @Schema(description = "年级")
    private String grade;

    @Schema(description = "入学年份")
    private Integer enrollmentYear;

    @Schema(description = "状态：1启用 0停用")
    private Integer status;

    @Schema(description = "当前页码", example = "1")
    private Long pageNum = 1L;

    @Schema(description = "每页条数", example = "10")
    private Long pageSize = 10L;
}
