package com.project.backend.organization.dto.major;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 专业保存DTO
 * 
 * @author 陈鸿昇
 * @since 2025-12-31
 */
@Data
@Schema(description = "专业保存参数")
public class MajorSaveDTO {

    @Schema(description = "主键ID（编辑时必填）")
    private Long id;

    @Schema(description = "专业编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "专业编码不能为空")
    private String majorCode;

    @Schema(description = "专业名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "专业名称不能为空")
    private String majorName;

    @Schema(description = "所属院系编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "所属院系不能为空")
    private String deptCode;

    @Schema(description = "专业负责人")
    private String director;

    @Schema(description = "学位类型（字典degree_type）")
    private String type;

    @Schema(description = "学制")
    private String duration;

    @Schema(description = "培养目标")
    private String goal;

    @Schema(description = "状态：1启用 0停用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;
}
