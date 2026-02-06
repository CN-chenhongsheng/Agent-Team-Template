package com.project.backend.organization.dto.department;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 院系保存DTO
 * 
 * @author 陈鸿昇
 * @since 2025-12-31
 */
@Data
@Schema(description = "院系保存参数")
public class DepartmentSaveDTO {

    @Schema(description = "主键ID（编辑时必填）")
    private Long id;

    @Schema(description = "院系编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "院系编码不能为空")
    private String deptCode;

    @Schema(description = "院系名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "院系名称不能为空")
    private String deptName;

    @Schema(description = "所属校区编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "所属校区不能为空")
    private String campusCode;

    @Schema(description = "院系领导")
    private String leader;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态：1启用 0停用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;
}
