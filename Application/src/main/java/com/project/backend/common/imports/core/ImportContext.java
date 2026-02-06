package com.project.backend.common.imports.core;

/**
 * 导入上下文接口
 * 提供导入过程中需要的共享数据（映射、缓存等）
 *
 * @author 陈鸿昇
 * @since 2026-02-06
 */
public interface ImportContext {

    /**
     * 获取异步任务ID（用于更新进度，同步导入时为null）
     */
    String getTaskId();

    /**
     * 获取预估总行数（用于计算进度，同步导入时为null）
     */
    Integer getEstimatedTotalRows();
}
