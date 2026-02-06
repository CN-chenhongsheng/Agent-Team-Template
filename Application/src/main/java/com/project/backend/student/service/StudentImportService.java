package com.project.backend.student.service;

import com.project.backend.student.dto.imports.ImportResult;
import com.project.backend.student.dto.imports.ImportTaskVO;
import com.project.backend.student.dto.imports.TaskIdResponse;

/**
 * 学生导入服务
 * 支持同步（小批量）与异步（大批量）导入
 *
 * @author 陈鸿昇
 * @since 2026-02-04
 */
public interface StudentImportService {

    /**
     * 根据文件 URL 执行导入
     * 行数 ≤ 阈值时同步解析入库并返回结果；行数 > 阈值时提交异步任务并返回 taskId
     *
     * @param fileUrl 文件访问 URL（由分片上传 merge 返回）
     * @param totalRows 前端扫描得到的有效行数（可选）
     * @return 同步时返回 ImportResult；异步时返回 TaskIdResponse
     */
    Object importFromExcel(String fileUrl, Integer totalRows);

    /**
     * 导入入口（两阶段：先校验，后写库）
     *
     * @param fileUrl   文件访问 URL（由分片上传 merge 返回）
     * @param totalRows 前端扫描得到的有效行数（可选）
     * @param taskId    异步任务 ID，同步导入时为 null，仅用于进度更新
     * @return 导入结果，包含成功/失败统计与错误明细
     */
    ImportResult importFromExcelWithTransaction(String fileUrl, Integer totalRows, String taskId);

    /**
     * 单批学生写入（独立事务），供多线程写库阶段使用
     *
     * @param batch 单批学生列表
     */
    void saveStudentBatch(java.util.List<com.project.backend.student.entity.Student> batch);

    /**
     * 提交异步导入任务
     *
     * @param fileUrl 文件访问 URL
     * @param totalRows 前端扫描得到的有效行数（可选）
     * @return 任务 ID，用于轮询 GET /import/task/{taskId}
     */
    String submitImportTask(String fileUrl, Integer totalRows);

    /**
     * 查询异步任务状态与结果
     *
     * @param taskId 任务 ID
     * @return 任务 VO，含 status、result（完成时）
     */
    ImportTaskVO getTask(String taskId);
}
