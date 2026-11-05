package com.project.backend.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户导入请求
 *
 * @author 陈鸿昇
 * @since 2026-11-05
 */
@Data
@Schema(description = "用户导入请求")
public class UserImportDTO {

    @Schema(description = "上传后的 Excel 文件 URL", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "文件地址不能为空")
    private String fileUrl;

    @Schema(description = "前端扫描到的总行数（可选）")
    private Integer totalRows;
}
