package com.project.backend.common.imports.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务ID响应
 *
 * @author 陈鸿昇
 * @since 2026-02-06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskIdResponse {
    /**
     * 任务ID
     */
    private String taskId;
}
