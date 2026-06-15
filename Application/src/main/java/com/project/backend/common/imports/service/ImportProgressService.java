package com.project.backend.common.imports.service;

import com.project.backend.common.imports.dto.ImportResult;

/**
 * 导入进度服务接口（空实现，用于编译通过）
 * 实际业务中需要实现 SSE 推送等功能
 *
 * @author 陈鸿昇
 * @since 2026-02-06
 */
public interface ImportProgressService {

    /**
     * 推送导入进度
     *
     * @param taskId       任务ID
     * @param progress     进度百分比（0-100）
     * @param processedRows 已处理行数
     * @param totalRows    总行数
     * @param successCount 成功数
     * @param failCount    失败数
     */
    void pushProgress(String taskId, Integer progress, Integer processedRows, Integer totalRows,
                     Integer successCount, Integer failCount);

    /**
     * 推送导入进度（重载方法，用于解析阶段）
     *
     * @param taskId       任务ID
     * @param progress     进度百分比（0-100）
     * @param processedRows 已处理行数
     * @param totalRows    总行数
     */
    default void pushProgress(String taskId, int progress, Integer processedRows, Integer totalRows) {
        pushProgress(taskId, progress, processedRows, totalRows, null, null);
    }

    /**
     * 推送阶段信息
     *
     * @param taskId  任务ID
     * @param stage   阶段名称（parsing/importing）
     * @param message 阶段消息
     * @param totalRows 总行数（可为null）
     */
    void pushStage(String taskId, String stage, String message, Integer totalRows);

    /**
     * 推送导入完成
     *
     * @param taskId 任务ID
     * @param status 状态（success/failed）
     * @param result 导入结果
     */
    void pushComplete(String taskId, String status, ImportResult result);

    /**
     * 推送导入失败
     *
     * @param taskId       任务ID
     * @param errorMessage 错误消息
     */
    void pushError(String taskId, String errorMessage);
}
