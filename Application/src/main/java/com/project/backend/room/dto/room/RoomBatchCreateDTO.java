package com.project.backend.room.dto.room;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 房间批量创建DTO
 * 
 * @author 陈鸿昇
 * @since 2026-01-05
 */
@Data
@Schema(description = "房间批量创建参数")
public class RoomBatchCreateDTO {

    @Schema(description = "所属楼层ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "所属楼层不能为空")
    private Long floorId;

    @Schema(description = "楼层数列表（多选）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "请至少选择一个楼层数")
    private List<Integer> floorNumbers;

    @Schema(description = "每层生成数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "生成数量不能为空")
    @Min(value = 1, message = "生成数量至少为1")
    private Integer generateCount;

    @Schema(description = "房间类型（字典dormitory_room_type）")
    private String roomType;

    @Schema(description = "房间状态：1空闲 2已满 3维修中 4已预订")
    private Integer roomStatus;

    @Schema(description = "床位数（标准配置）")
    private Integer bedCount;

    @Schema(description = "房间面积（平方米）")
    private BigDecimal area;

    @Schema(description = "最多入住人数")
    private Integer maxOccupancy;

    @Schema(description = "状态：1启用 0停用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "是否有空调：1有 0无")
    private Integer hasAirConditioner;

    @Schema(description = "是否有独立卫生间：1有 0无")
    private Integer hasBathroom;

    @Schema(description = "是否有阳台：1有 0无")
    private Integer hasBalcony;

    @Schema(description = "备注")
    private String remark;
}
