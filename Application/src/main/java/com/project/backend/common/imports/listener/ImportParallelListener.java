package com.project.backend.common.imports.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.project.backend.common.imports.dto.RawRow;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * 通用导入并行解析监听器
 * 仅负责将 Excel 行封装为 RawRow 并投递到阻塞队列，不直接写库
 * 支持实时进度回调，用于 SSE 推送
 *
 * @author 陈鸿昇
 * @since 2026-02-06
 */
@Slf4j
public class ImportParallelListener<DTO> extends AnalysisEventListener<DTO> {

    private final BlockingQueue<RawRow<DTO>> dtoQueue;

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
     * 空行判断器（由业务决定如何判断空行）
     */
    private final Predicate<DTO> emptyRowPredicate;

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
    public ImportParallelListener(BlockingQueue<RawRow<DTO>> dtoQueue,
                                   Predicate<DTO> emptyRowPredicate) {
        this(dtoQueue, null, null, emptyRowPredicate);
    }

    /**
     * 构造函数（带进度回调）
     *
     * @param dtoQueue           数据队列
     * @param progressCallback   进度回调，参数为 (已读取行数, 预估总行数)
     * @param estimatedTotalRows 预估总行数
     * @param emptyRowPredicate  空行判断器
     */
    public ImportParallelListener(
            BlockingQueue<RawRow<DTO>> dtoQueue,
            BiConsumer<Integer, Integer> progressCallback,
            Integer estimatedTotalRows,
            Predicate<DTO> emptyRowPredicate) {
        this.dtoQueue = dtoQueue;
        this.progressCallback = progressCallback;
        this.estimatedTotalRows = estimatedTotalRows;
        this.emptyRowPredicate = emptyRowPredicate != null ? emptyRowPredicate : dto -> false;
    }

    @Override
    public void invoke(DTO data, AnalysisContext context) {
        Integer row = context.readRowHolder().getRowIndex() + 1;
        if (data == null) {
            return;
        }

        // 判断是否为空行（由业务决定）
        if (emptyRowPredicate.test(data)) {
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
            log.warn("导入并行监听器被中断，行号: {}, 错误: {}", row, e.getMessage());
            throw new RuntimeException("导入被中断", e);
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

    /**
     * 获取已读取的有效行数
     */
    public int getReadRowCount() {
        return readRowCount;
    }
}
