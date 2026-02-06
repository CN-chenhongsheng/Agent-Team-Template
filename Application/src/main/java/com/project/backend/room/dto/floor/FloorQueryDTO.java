package com.project.backend.room.dto.floor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 楼层查询DTO
 * 
 * @author 陈鸿昇
 * @since 2026-01-03
 */
@Data
@Schema(description = "楼层查询条件")
public class FloorQueryDTO {

    @Schema(description = "楼层编码（模糊查询）")
    private String floorCode;

    @Schema(description = "楼层名称（模糊查询）")
    private String floorName;

    @Schema(description = "所属校区编码")
    private String campusCode;

    @Schema(description = "适用性别：1男 2女 3混合")
    private Integer genderType;

    @Schema(description = "状态：1启用 0停用")
    private Integer status;

    @Schema(description = "当前页码", example = "1")
    private Long pageNum = 1L;

    @Schema(description = "每页条数", example = "10")
    private Long pageSize = 10L;
}
