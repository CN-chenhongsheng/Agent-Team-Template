package com.project.backend.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 用户导入结果
 *
 * @author 陈鸿昇
 * @since 2026-11-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户导入结果")
public class UserImportResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "实际处理的数据行数")
    private Integer totalRows;

    @Schema(description = "成功数量")
    private Integer successCount;

    @Schema(description = "失败数量")
    private Integer failCount;

    @Schema(description = "错误明细（最多返回前 200 条）")
    private List<UserImportErrorVO> errors;
}
