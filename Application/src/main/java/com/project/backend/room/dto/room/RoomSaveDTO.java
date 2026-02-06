package com.project.backend.room.dto.room;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 房间保存DTO
 * 
 * @author 陈鸿昇
 * @since 2026-01-03
 */
@Data
@Schema(description = "房间保存参数")
public class RoomSaveDTO {

    @Schema(description = "主键ID（编辑时必填）")
    private Long id;

    @Schema(description = "房间编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "房间编码不能为空")
    private String roomCode;

    @Schema(description = "房间号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "房间号不能为空")
    private String roomNumber;

    @Schema(description = "所属楼层ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "所属楼层不能为空")
    private Long floorId;

    @Schema(description = "所属楼层数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "所属楼层数不能为空")
    private Integer floorNumber;

    @Schema(description = "房间类型（字典room_type）")
    private String roomType;

    @Schema(description = "床位数（标准配置）")
    private Integer bedCount;

    @Schema(description = "最大入住人数")
    private Integer maxOccupancy;

    @Schema(description = "房间面积（平方米）")
    private BigDecimal area;

    @Schema(description = "是否有空调：1有 0无")
    private Integer hasAirConditioner;

    @Schema(description = "是否有独立卫生间：1有 0无")
    private Integer hasBathroom;

    @Schema(description = "是否有阳台：1有 0无")
    private Integer hasBalcony;

    @Schema(description = "房间状态：1空闲 2已满 3维修中 4已预订")
    private Integer roomStatus;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态：1启用 0停用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
