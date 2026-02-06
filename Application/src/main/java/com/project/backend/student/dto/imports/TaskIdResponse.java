package com.project.backend.student.dto.imports;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 异步任务 ID 响应
 *
 * @author 陈鸿昇
 * @since 2026-02-04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "异步任务 ID 响应")
public class TaskIdResponse implements Serializable {

    @Schema(description = "任务 ID，用于轮询查询任务状态")
    private String taskId;
}
