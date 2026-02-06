package com.project.backend.student.dto.imports;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 导入错误明细
 *
 * @author 陈鸿昇
 * @since 2026-02-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "导入错误明细")
public class ImportError implements Serializable {

    @Schema(description = "行号（Excel 行，含表头）")
    private Integer row;

    @Schema(description = "列/字段名")
    private String column;

    @Schema(description = "错误信息")
    private String message;

    @Schema(description = "原值（可选）")
    private String value;
}
