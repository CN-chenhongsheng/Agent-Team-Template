package com.project.backend.allocation.dto.config;

import com.project.core.dto.BaseQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分配配置查询DTO
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "分配配置查询请求")
public class AllocationConfigQueryDTO extends BaseQueryDTO {

    @Schema(description = "配置名称（模糊查询）")
    private String configName;

    @Schema(description = "算法类型")
    private String algorithmType;

    @Schema(description = "状态：1-启用 0-停用")
    private Integer status;
}
