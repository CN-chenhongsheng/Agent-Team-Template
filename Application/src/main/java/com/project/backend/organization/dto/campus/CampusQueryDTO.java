package com.project.backend.organization.dto.campus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 校区查询DTO
 * 
 * @author 陈鸿昇
 * @since 2025-12-31
 */
@Data
@Schema(description = "校区查询条件")
public class CampusQueryDTO {

    @Schema(description = "校区编码（模糊查询）")
    private String campusCode;

    @Schema(description = "校区名称（模糊查询）")
    private String campusName;

    @Schema(description = "状态：1启用 0停用")
    private Integer status;

    @Schema(description = "当前页码", example = "1")
    private Long pageNum = 1L;

    @Schema(description = "每页条数", example = "10")
    private Long pageSize = 10L;
}
