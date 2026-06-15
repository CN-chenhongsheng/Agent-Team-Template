package com.project.backend.common.imports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 导入错误信息
 *
 * @author 陈鸿昇
 * @since 2026-02-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportError {
    /**
     * 行号（从1开始）
     */
    private Integer rowIndex;

    /**
     * 字段名
     */
    private String field;

    /**
     * 错误消息
     */
    private String message;

    /**
     * Builder 别名方法：row -> rowIndex
     */
    public static class ImportErrorBuilder {
        public ImportErrorBuilder row(Integer row) {
            this.rowIndex = row;
            return this;
        }

        public ImportErrorBuilder column(String column) {
            this.field = column;
            return this;
        }
    }

    /**
     * 构造器：仅消息
     */
    public ImportError(String message) {
        this.message = message;
    }

    /**
     * 构造器：行号 + 消息
     */
    public ImportError(Integer rowIndex, String message) {
        this.rowIndex = rowIndex;
        this.message = message;
    }
}
