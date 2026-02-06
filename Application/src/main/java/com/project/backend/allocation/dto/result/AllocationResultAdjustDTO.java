package com.project.backend.allocation.dto.result;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 分配结果调整DTO
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Data
@Schema(description = "分配结果调整请求")
public class AllocationResultAdjustDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "结果ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "结果ID不能为空")
    private Long resultId;

    @Schema(description = "新床位ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "新床位ID不能为空")
    private Long newBedId;

    @Schema(description = "调整原因")
    private String reason;
}
