package com.project.backend.room.dto.floor;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 楼层保存DTO
 * 
 * @author 陈鸿昇
 * @since 2026-01-03
 */
@Data
@Schema(description = "楼层保存参数")
public class FloorSaveDTO {

    @Schema(description = "主键ID（编辑时必填）")
    private Long id;

    @Schema(description = "楼层编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "楼层编码不能为空")
    private String floorCode;

    @Schema(description = "楼层名称")
    private String floorName;

    @Schema(description = "楼层数（数字）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "楼层数不能为空")
    private Integer floorNumber;

    @Schema(description = "所属校区编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "所属校区不能为空")
    private String campusCode;

    @Schema(description = "适用性别：1男 2女 3混合", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "适用性别不能为空")
    private Integer genderType;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态：1启用 0停用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
