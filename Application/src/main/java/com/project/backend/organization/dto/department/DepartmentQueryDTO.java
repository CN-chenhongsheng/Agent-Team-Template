package com.project.backend.organization.dto.department;

import com.project.core.dto.BaseQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 院系查询DTO
 * 
 * @author 陈鸿昇
 * @since 2025-12-31
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "院系查询条件")
public class DepartmentQueryDTO extends BaseQueryDTO {

    @Schema(description = "院系编码（模糊查询）")
    private String deptCode;

    @Schema(description = "院系名称（模糊查询）")
    private String deptName;

    @Schema(description = "所属校区编码")
    private String campusCode;

    @Schema(description = "状态：1启用 0停用")
    private Integer status;
}
