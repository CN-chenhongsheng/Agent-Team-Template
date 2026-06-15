package com.project.backend.common.imports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 导入任务VO
 *
 * @author 陈鸿昇
 * @since 2026-02-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportTaskVO {
    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 任务状态：processing-处理中, completed-已完成, failed-失败
     */
    private String status;

    /**
     * 进度百分比（0-100）
     */
    private Integer progress;

    /**
     * 进度百分比（0-100）- 别名
     */
    private Integer progressPercent;

    /**
     * 导入结果
     */
    private ImportResult result;

    /**
     * 错误消息（任务失败时）
     */
    private String errorMessage;
}
