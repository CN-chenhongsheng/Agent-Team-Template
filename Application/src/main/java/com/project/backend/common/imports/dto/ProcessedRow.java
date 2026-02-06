package com.project.backend.common.imports.dto;

import com.project.backend.student.dto.imports.ImportError;
import lombok.Data;

import java.util.List;

/**
 * 处理后的行结果：行号 + 可插入的实体（可能为 null）+ 行级错误列表
 *
 * @author 陈鸿昇
 * @since 2026-02-06
 */
@Data
public class ProcessedRow<Entity> {
    private final int rowIndex;
    private final Entity entity;
    private final List<ImportError> errors;

    public ProcessedRow(int rowIndex, Entity entity, List<ImportError> errors) {
        this.rowIndex = rowIndex;
        this.entity = entity;
        this.errors = errors;
    }
}
