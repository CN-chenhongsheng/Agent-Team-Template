package com.project.backend.common.imports.dto;

import lombok.Data;

/**
 * 原始行数据：行号 + DTO 或用于结束信号的毒丸
 *
 * @author 陈鸿昇
 * @since 2026-02-06
 */
@Data
public class RawRow<DTO> {
    private final int rowIndex;
    private final DTO dto;
    private final boolean poison;

    private RawRow(int rowIndex, DTO dto, boolean poison) {
        this.rowIndex = rowIndex;
        this.dto = dto;
        this.poison = poison;
    }

    public static <DTO> RawRow<DTO> data(int rowIndex, DTO dto) {
        return new RawRow<>(rowIndex, dto, false);
    }

    public static <DTO> RawRow<DTO> poisonPill() {
        return new RawRow<>(-1, null, true);
    }

    public boolean isPoison() {
        return poison;
    }
}
