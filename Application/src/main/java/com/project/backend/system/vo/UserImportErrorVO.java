package com.project.backend.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户导入错误明细
 *
 * @author 陈鸿昇
 * @since 2026-11-05
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户导入错误明细")
public class UserImportErrorVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Excel 行号")
    private Integer row;

    @Schema(description = "出错字段")
    private String field;

    @Schema(description = "错误信息")
    private String message;

    @Schema(description = "原始值")
    private String value;
}
