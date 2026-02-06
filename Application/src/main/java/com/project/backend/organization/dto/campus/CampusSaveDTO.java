package com.project.backend.organization.dto.campus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 校区保存DTO
 * 
 * @author 陈鸿昇
 * @since 2025-12-31
 */
@Data
@Schema(description = "校区保存参数")
public class CampusSaveDTO {

    @Schema(description = "主键ID（编辑时必填）")
    private Long id;

    @Schema(description = "校区编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "校区编码不能为空")
    private String campusCode;

    @Schema(description = "校区名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "校区名称不能为空")
    private String campusName;

    @Schema(description = "校区地址")
    private String address;

    @Schema(description = "负责人")
    private String manager;

    @Schema(description = "排序序号")
    private Integer sort;

    @Schema(description = "状态：1启用 0停用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;
}
