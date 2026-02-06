package com.project.backend.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 字典数据保存DTO（新增或编辑）
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Data
@Schema(description = "字典数据保存请求")
public class DictDataSaveDTO {

    @Schema(description = "字典数据ID（编辑时必传）")
    private Long id;

    @Schema(description = "字典编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典编码不能为空")
    private String dictCode;

    @Schema(description = "字典标签", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典标签不能为空")
    private String label;

    @Schema(description = "字典说明", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典值不能为空")
    private String value;

    @Schema(description = "CSS类名")
    private String cssClass;

    @Schema(description = "表格回显样式")
    private String listClass;

    @Schema(description = "排序", example = "0")
    private Integer sort;

    @Schema(description = "是否默认：1是 0否")
    private Integer isDefault;

    @Schema(description = "状态：1正常 0停用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
