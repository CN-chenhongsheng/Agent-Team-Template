package com.project.backend.room.dto.room;

import com.project.core.dto.BaseQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 房间查询DTO
 * 
 * @author 陈鸿昇
 * @since 2026-01-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "房间查询条件")
public class RoomQueryDTO extends BaseQueryDTO {

    @Schema(description = "房间编码（模糊查询）")
    private String roomCode;

    @Schema(description = "房间号（模糊查询）")
    private String roomNumber;

    @Schema(description = "所属楼层ID")
    private Long floorId;

    @Schema(description = "所属楼层编码")
    private String floorCode;

    @Schema(description = "所属校区编码")
    private String campusCode;

    @Schema(description = "房间类型")
    private String roomType;

    @Schema(description = "房间状态：1空闲 2已满 3维修中 4已预订")
    private Integer roomStatus;

    @Schema(description = "状态：1启用 0停用")
    private Integer status;
}
