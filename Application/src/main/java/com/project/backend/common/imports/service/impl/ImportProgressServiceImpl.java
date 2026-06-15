package com.project.backend.common.imports.service.impl;

import com.project.backend.common.imports.dto.ImportResult;
import com.project.backend.common.imports.service.ImportProgressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 导入进度服务实现（空实现，用于编译通过）
 *
 * @author 陈鸿昇
 * @since 2026-02-06
 */
@Slf4j
@Service
public class ImportProgressServiceImpl implements ImportProgressService {

    @Override
    public void pushProgress(String taskId, Integer progress, Integer processedRows, Integer totalRows,
                            Integer successCount, Integer failCount) {
        log.debug("导入进度: taskId={}, progress={}%, processed={}/{}, success={}, fail={}",
                taskId, progress, processedRows, totalRows, successCount, failCount);
    }

    @Override
    public void pushStage(String taskId, String stage, String message, Integer totalRows) {
        log.info("导入阶段: taskId={}, stage={}, message={}, totalRows={}",
                taskId, stage, message, totalRows);
    }

    @Override
    public void pushComplete(String taskId, String status, ImportResult result) {
        log.info("导入完成: taskId={}, status={}, success={}, fail={}",
                taskId, status, result.getSuccessCount(), result.getFailCount());
    }

    @Override
    public void pushError(String taskId, String errorMessage) {
        log.error("导入失败: taskId={}, error={}", taskId, errorMessage);
    }
}
