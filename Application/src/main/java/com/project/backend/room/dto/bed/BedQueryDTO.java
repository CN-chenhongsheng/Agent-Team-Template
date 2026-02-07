package com.project.backend.room.dto.bed;

import com.project.core.dto.BaseQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 床位查询DTO
 * 
 * @author 陈鸿昇
 * @since 2026-01-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "床位查询条件")
public class BedQueryDTO extends BaseQueryDTO {

    @Schema(description = "床位编码（模糊查询）")
    private String bedCode;

    @Schema(description = "床位号（模糊查询）")
    private String bedNumber;

    @Schema(description = "所属房间ID")
    private Long roomId;

    @Schema(description = "所属房间编码")
    private String roomCode;

    @Schema(description = "所属楼层ID")
    private Long floorId;

    @Schema(description = "所属楼层编码")
    private String floorCode;

    @Schema(description = "所属校区编码")
    private String campusCode;

    @Schema(description = "床位位置（字典bed_position）")
    private String bedPosition;

    @Schema(description = "床位状态：1空闲 2已占用 3维修中 4已预订")
    private Integer bedStatus;

    @Schema(description = "状态：1启用 0停用")
    private Integer status;
}
