package com.project.backend.student.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.project.backend.common.imports.dto.RawRow;
import com.project.backend.common.imports.listener.ImportParallelListener;
import com.project.backend.student.dto.imports.StudentImportDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.function.BiConsumer;

/**
 * 学生导入并行解析监听器
 *
 * @deprecated 已迁移到通用框架，请使用 {@link ImportParallelListener}
 * @author 陈鸿昇
 * @since 2026-02-05
 */
@Deprecated
@Slf4j
public class StudentImportParallelListener extends AnalysisEventListener<StudentImportDTO> {

    private final BlockingQueue<RawRow<StudentImportDTO>> dtoQueue;

    /**
     * 进度回调：(已读取行数, 预估总行数) -> void
     * 可为 null，表示不需要进度回调
     */
    private final BiConsumer<Integer, Integer> progressCallback;

    /**
     * 预估总行数（用于计算百分比）
     */
    private final Integer estimatedTotalRows;

    /**
     * 进度回调间隔（每隔多少行回调一次）
     */
    private static final int PROGRESS_CALLBACK_INTERVAL = 5000;

    /**
     * 已读取的有效行数（非空行）
     */
    private int readRowCount = 0;

    /**
     * 构造函数（无进度回调，兼容旧代码）
     */
    public StudentImportParallelListener(BlockingQueue<RawRow<StudentImportDTO>> dtoQueue) {
        this(dtoQueue, null, null);
    }

    /**
     * 构造函数（带进度回调）
     *
     * @param dtoQueue           数据队列
     * @param progressCallback   进度回调，参数为 (已读取行数, 预估总行数)
     * @param estimatedTotalRows 预估总行数
     */
    public StudentImportParallelListener(
            BlockingQueue<RawRow<StudentImportDTO>> dtoQueue,
            BiConsumer<Integer, Integer> progressCallback,
            Integer estimatedTotalRows) {
        this.dtoQueue = dtoQueue;
        this.progressCallback = progressCallback;
        this.estimatedTotalRows = estimatedTotalRows;
    }

    @Override
    public void invoke(StudentImportDTO data, AnalysisContext context) {
        Integer row = context.readRowHolder().getRowIndex() + 1;
        if (data == null) {
            return;
        }

        // 复用原有"跳过完全空行"的规则，减少无效处理
        String studentNo = data.getStudentNo();
        String studentName = data.getStudentName();
        String campusName = data.getCampusName();
        String deptName = data.getDeptName();
        String majorName = data.getMajorName();
        String className = data.getClassName();

        boolean isEmptyRow = isBlank(studentNo)
                && isBlank(studentName)
                && isBlank(campusName)
                && isBlank(deptName)
                && isBlank(majorName)
                && isBlank(className);

        if (isEmptyRow) {
            // 完全空行，跳过不处理（不记录错误，不加入队列）
            return;
        }

        try {
            dtoQueue.put(RawRow.data(row, data));
            readRowCount++;

            // 定期触发进度回调
            if (progressCallback != null && readRowCount % PROGRESS_CALLBACK_INTERVAL == 0) {
                progressCallback.accept(readRowCount, estimatedTotalRows);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("学生导入并行监听器被中断，行号: {}, 错误: {}", row, e.getMessage());
            throw new RuntimeException("学生导入被中断", e);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 最后一次进度回调，确保前端能看到读取完成
        if (progressCallback != null) {
            progressCallback.accept(readRowCount, estimatedTotalRows);
        }
        log.info("Excel 读取完成，共读取有效行数: {}", readRowCount);
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 获取已读取的有效行数
     */
    public int getReadRowCount() {
        return readRowCount;
    }
}
