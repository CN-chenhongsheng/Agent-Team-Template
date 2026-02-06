package com.project.backend.room.dto.bed;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 床位批量创建DTO
 * 
 * @author 陈鸿昇
 * @since 2026-01-05
 */
@Data
@Schema(description = "床位批量创建参数")
public class BedBatchCreateDTO {

    @Schema(description = "所属房间ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "所属房间不能为空")
    private Long roomId;

    @Schema(description = "生成数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "生成数量不能为空")
    @Min(value = 1, message = "生成数量至少为1")
    private Integer generateCount;

    @Schema(description = "床位位置")
    private String bedPosition;

    @Schema(description = "床位状态：1空闲 2已占用 3维修中 4已预订")
    private Integer bedStatus;

    @Schema(description = "入住日期")
    private LocalDate checkInDate;

    @Schema(description = "退宿日期")
    private LocalDate checkOutDate;

    @Schema(description = "状态：1启用 0停用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
