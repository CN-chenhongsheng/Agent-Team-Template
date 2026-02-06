package com.project.backend.room.dto.bed;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 床位保存DTO
 * 
 * @author 陈鸿昇
 * @since 2026-01-03
 */
@Data
@Schema(description = "床位保存参数")
public class BedSaveDTO {

    @Schema(description = "主键ID（编辑时必填）")
    private Long id;

    @Schema(description = "床位编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "床位编码不能为空")
    private String bedCode;

    @Schema(description = "床位号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "床位号不能为空")
    private String bedNumber;

    @Schema(description = "所属房间ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "所属房间不能为空")
    private Long roomId;

    @Schema(description = "床位位置（字典bed_position）")
    private String bedPosition;

    @Schema(description = "床位状态：1空闲 2已占用 3维修中 4已预订")
    private Integer bedStatus;

    @Schema(description = "当前入住学生ID")
    private Long studentId;

    @Schema(description = "当前入住学生姓名")
    private String studentName;

    @Schema(description = "入住日期")
    private LocalDate checkInDate;

    @Schema(description = "退宿日期")
    private LocalDate checkOutDate;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态：1启用 0停用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
