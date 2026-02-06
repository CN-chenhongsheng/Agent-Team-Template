package com.project.backend.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 字典数据查询DTO
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Data
@Schema(description = "字典数据查询条件")
public class DictDataQueryDTO {

    @Schema(description = "字典编码")
    private String dictCode;

    @Schema(description = "字典标签（模糊查询）")
    private String label;

    @Schema(description = "状态：1正常 0停用")
    private Integer status;

    @Schema(description = "当前页码", example = "1")
    private Long pageNum = 1L;

    @Schema(description = "每页条数", example = "10")
    private Long pageSize = 10L;
}
