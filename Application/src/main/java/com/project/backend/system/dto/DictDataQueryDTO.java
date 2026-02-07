package com.project.backend.system.dto;

import com.project.core.dto.BaseQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典数据查询DTO
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "字典数据查询条件")
public class DictDataQueryDTO extends BaseQueryDTO {

    @Schema(description = "字典编码")
    private String dictCode;

    @Schema(description = "字典标签（模糊查询）")
    private String label;

    @Schema(description = "状态：1正常 0停用")
    private Integer status;
}
