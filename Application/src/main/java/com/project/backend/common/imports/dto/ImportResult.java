package com.project.backend.common.imports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 导入结果
 *
 * @author 陈鸿昇
 * @since 2026-02-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResult {
    /**
     * 总行数
     */
    private Integer totalRows;

    /**
     * 成功数
     */
    private Integer successCount;

    /**
     * 失败数
     */
    private Integer failCount;

    /**
     * 错误列表
     */
    private List<ImportError> errors = new ArrayList<>();

    /**
     * 构造器：仅统计信息
     */
    public ImportResult(Integer totalRows, Integer successCount, Integer failCount) {
        this.totalRows = totalRows;
        this.successCount = successCount;
        this.failCount = failCount;
        this.errors = new ArrayList<>();
    }
}
