package com.project.backend.allocation.dto.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 分配配置查询DTO
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Data
@Schema(description = "分配配置查询请求")
public class AllocationConfigQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "配置名称（模糊查询）")
    private String configName;

    @Schema(description = "算法类型")
    private String algorithmType;

    @Schema(description = "状态：1-启用 0-停用")
    private Integer status;

    @Schema(description = "当前页码", example = "1")
    private Long pageNum = 1L;

    @Schema(description = "每页条数", example = "10")
    private Long pageSize = 10L;
}
