package com.project.backend.student.dto.imports;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 异步导入任务 VO
 *
 * @author 陈鸿昇
 * @since 2026-02-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "异步导入任务")
public class ImportTaskVO implements Serializable {

    @Schema(description = "任务 ID")
    private String taskId;

    @Schema(description = "状态：pending-待处理 processing-处理中 success-成功 failed-失败")
    private String status;

    @Schema(description = "进度百分比（0-100，可选）")
    private Integer progressPercent;

    @Schema(description = "完成时的导入结果")
    private ImportResult result;
}
