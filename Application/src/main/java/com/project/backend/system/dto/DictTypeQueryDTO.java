package com.project.backend.system.dto;

import com.project.core.dto.BaseQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典类型查询DTO
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "字典类型查询条件")
public class DictTypeQueryDTO extends BaseQueryDTO {

    @Schema(description = "字典名称（模糊查询）")
    private String dictName;

    @Schema(description = "字典编码（模糊查询）")
    private String dictCode;

    @Schema(description = "状态：1正常 0停用")
    private Integer status;
}
