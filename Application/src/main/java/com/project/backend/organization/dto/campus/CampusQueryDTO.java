package com.project.backend.organization.dto.campus;

import com.project.core.dto.BaseQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 校区查询DTO
 * 
 * @author 陈鸿昇
 * @since 2025-12-31
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "校区查询条件")
public class CampusQueryDTO extends BaseQueryDTO {

    @Schema(description = "校区编码（模糊查询）")
    private String campusCode;

    @Schema(description = "校区名称（模糊查询）")
    private String campusName;

    @Schema(description = "状态：1启用 0停用")
    private Integer status;
}
