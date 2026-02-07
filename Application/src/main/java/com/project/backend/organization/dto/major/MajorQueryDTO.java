package com.project.backend.organization.dto.major;

import com.project.core.dto.BaseQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 专业查询DTO
 * 
 * @author 陈鸿昇
 * @since 2025-12-31
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "专业查询条件")
public class MajorQueryDTO extends BaseQueryDTO {

    @Schema(description = "专业编码（模糊查询）")
    private String majorCode;

    @Schema(description = "专业名称（模糊查询）")
    private String majorName;

    @Schema(description = "所属院系编码")
    private String deptCode;

    @Schema(description = "所属校区编码（冗余查询条件）")
    private String campusCode;

    @Schema(description = "状态：1启用 0停用")
    private Integer status;
}
