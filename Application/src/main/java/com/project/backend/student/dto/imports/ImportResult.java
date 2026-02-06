package com.project.backend.student.dto.imports;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 学生导入结果
 *
 * @author 陈鸿昇
 * @since 2026-02-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "学生导入结果")
public class ImportResult implements Serializable {

    @Schema(description = "总行数（不含表头）")
    private Integer totalRows;

    @Schema(description = "成功条数")
    private Integer successCount;

    @Schema(description = "失败条数")
    private Integer failCount;

    @Schema(description = "错误明细列表")
    @Builder.Default
    private List<ImportError> errors = new ArrayList<>();
}
