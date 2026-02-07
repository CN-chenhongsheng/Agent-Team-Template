package com.project.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 分页查询基类 DTO
 * <p>
 * 所有分页查询 DTO 应继承此类，统一分页参数定义
 * </p>
 *
 * @author 陈鸿昇
 * @since 2026-02-07
 */
@Data
@Schema(description = "分页查询基类")
public class BaseQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "当前页码", example = "1")
    private Long pageNum = 1L;

    @Schema(description = "每页条数", example = "10")
    private Long pageSize = 10L;

    @Schema(description = "搜索关键字")
    private String keyword;
}
