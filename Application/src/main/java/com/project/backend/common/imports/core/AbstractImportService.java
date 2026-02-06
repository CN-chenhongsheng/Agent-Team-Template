package com.project.backend.common.imports.core;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.backend.common.imports.dto.ProcessedRow;
import com.project.backend.common.imports.dto.RawRow;
import com.project.backend.common.imports.listener.ImportParallelListener;
import com.project.backend.student.dto.imports.ImportError;
import com.project.backend.student.dto.imports.ImportResult;
import com.project.backend.student.dto.imports.ImportTaskVO;
import com.project.backend.student.dto.imports.TaskIdResponse;
import com.project.backend.student.service.ImportProgressService;
import com.project.core.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * 抽象导入服务基类
 * 封装完整的导入流程框架：文件读取、多线程解析、并行写库、事务管理、结果汇总
 *
 * 职责分离：
 * - 框架负责：文件读取、多线程调度、进度推送、事务管理、结果汇总
 * - 子类负责：业务逻辑（字段转换、数据校验、级联解析）
 *
 * @param <DTO>     Excel 行 DTO 类型
 * @param <Entity>  数据库实体类型
 * @param <Context> 导入上下文类型（包含映射、缓存等）
 * @param <M>       Mapper 类型（必须继承 BaseMapper<Entity>）
 * @author 陈鸿昇
 * @since 2026-02-06
 */
@Slf4j
public abstract class AbstractImportService<DTO, Entity, Context extends ImportContext, M extends BaseMapper<Entity>>
        extends ServiceImpl<M, Entity> {

    @Autowired
    protected ImportProgressService importProgressService;

    /**
     * 自身的代理对象，用于触发 Spring 事务等 AOP 能力
     */
    @Lazy
    @Autowired
    protected AbstractImportService<DTO, Entity, Context, M> selfProxy;

    @Value("${file.upload-dir:./uploads}")
    protected String uploadDir;

    @Value("${import.global.max-worker-count:0}")
    protected int maxWorkerCountConfig;

    @Value("${import.global.queue-capacity:0}")
    protected int queueCapacityConfig;

    @Value("${import.global.batch-save-size:0}")
    protected int batchSaveSizeConfig;

    protected static final String UPLOAD_PREFIX = "uploads/";
    protected static final int BATCH_SAVE_SIZE = 5000;
    protected static final int QUEUE_CAPACITY = 5000;
    protected static final ConcurrentHashMap<String, ImportTaskVO> TASK_MAP = new ConcurrentHashMap<>();
    protected static final ExecutorService IMPORT_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "import-task");
        t.setDaemon(false);
        return t;
    });

    // ========== 抽象方法（子类实现业务逻辑） ==========

    /**
     * 解析文件URL为本地路径
     */
    protected abstract Path resolveFileUrlToPath(String fileUrl);

    /**
     * 构建导入上下文（预加载映射、缓存等）
     */
    protected abstract Context buildContext(String taskId, Integer estimatedTotalRows);

    /**
     * 将 DTO 转换为实体（业务校验 + 字段转换 + 级联解析）
     *
     * @param dto            Excel 行 DTO
     * @param row            行号
     * @param context        导入上下文
     * @param errors         错误列表（行级错误）
     * @param batchKeys      批次唯一键集合（用于批内去重）
     * @return 实体对象（校验失败时返回 null）
     */
    protected abstract Entity convertDtoToEntity(
            DTO dto, int row, Context context, List<ImportError> errors, Set<String> batchKeys);

    /**
     * 获取 DTO 类型（用于 EasyExcel 读取）
     */
    protected abstract Class<DTO> getDtoClass();

    /**
     * 获取同步/异步判断阈值（默认 5000 行）
     */
    protected int getSyncThreshold() {
        return 5000;
    }

    /**
     * 判断是否为空行（由业务决定）
     */
    protected abstract Predicate<DTO> getEmptyRowPredicate();

    // ========== 通用导入流程（复用学生导入逻辑） ==========

    /**
     * 导入主入口
     */
    public Object importFromExcel(String fileUrl, Integer totalRows) {
        log.info("开始导入数据，fileUrl: {}, frontendTotalRows: {}", fileUrl, totalRows);
        try {
            Path path = resolveFileUrlToPath(fileUrl);
            log.info("解析后的文件路径: {}", path);
            if (!Files.exists(path)) {
                log.error("文件不存在: {}", path);
                throw new BusinessException("文件不存在: " + path);
            }
            if (!Files.isRegularFile(path)) {
                log.error("不是常规文件: {}", path);
                throw new BusinessException("不是常规文件: " + path);
            }

            // 优化：先快速估算文件大小，大文件直接异步处理，避免完整读取导致超时
            long fileSize = Files.size(path);
            log.info("文件大小: {} bytes ({} MB)", fileSize, fileSize / 1024 / 1024);

            // 如果文件大于10MB，直接异步处理（避免统计行数耗时过长）
            long largeFileThreshold = 10 * 1024 * 1024; // 10MB
            if (fileSize > largeFileThreshold) {
                log.info("文件较大（{} MB），直接提交异步任务", fileSize / 1024 / 1024);
                String taskId = submitImportTask(fileUrl, totalRows);
                return new TaskIdResponse(taskId);
            }

            // 小文件才统计行数：优先使用前端扫描得到的 totalRows，避免重复完整解析 Excel
            int rowCount;
            if (totalRows != null && totalRows > 0) {
                rowCount = totalRows;
                log.info("文件较小，使用前端 totalRows 作为行数判断: {}, 同步阈值: {}", rowCount, getSyncThreshold());
            } else {
                log.info("文件较小，开始快速统计行数...");
                rowCount = countRowsFast(path);
                log.info("文件总行数: {}, 同步阈值: {}", rowCount, getSyncThreshold());
            }
            if (rowCount > getSyncThreshold()) {
                log.info("行数超过阈值，提交异步任务");
                String taskId = submitImportTask(fileUrl, totalRows);
                return new TaskIdResponse(taskId);
            }
            log.info("行数未超过阈值，同步导入");
            // 使用带事务的方法，通过代理对象调用以确保事务生效
            ImportResult result = selfProxy.importFromExcelWithTransaction(fileUrl, totalRows, null);
            log.info("导入完成，成功: {}, 失败: {}", result.getSuccessCount(), result.getFailCount());
            return result;
        } catch (BusinessException e) {
            log.error("导入失败（业务异常）: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("导入失败（系统异常）: {}", e.getMessage(), e);
            throw new BusinessException("导入失败: " + e.getMessage());
        }
    }

    /**
     * 提交异步导入任务
     */
    public String submitImportTask(String fileUrl, Integer totalRows) {
        String taskId = UUID.randomUUID().toString().replace("-", "");
        ImportTaskVO vo = ImportTaskVO.builder()
                .taskId(taskId)
                .status("processing")
                .progressPercent(0)
                .build();
        TASK_MAP.put(taskId, vo);
        IMPORT_EXECUTOR.execute(() -> {
            try {
                log.info("异步导入任务开始，taskId: {}, frontendTotalRows: {}", taskId, totalRows);

                // SSE 推送：阶段1 - 解析文件
                if (totalRows != null && totalRows > 0) {
                    importProgressService.pushStage(taskId, "parsing", "文件解析完成，共 " + totalRows + " 行", totalRows);
                } else {
                    // 大文件：不预先统计行数，解析阶段只显示已处理行数
                    importProgressService.pushStage(taskId, "parsing", "正在解析文件...", null);
                    log.info("大文件导入：跳过预统计行数，解析完成后获取实际行数");
                }

                // 通过代理对象调用带事务的方法，确保异步导入同样受事务管理
                ImportResult result = selfProxy.importFromExcelWithTransaction(fileUrl, totalRows, taskId);
                ImportTaskVO v = TASK_MAP.get(taskId);
                if (v != null) {
                    v.setStatus("success");
                    v.setProgressPercent(100);
                    // 任务完成时返回结果；错误列表超过 500 条时只返回前 500 条，避免响应过大
                    ImportResult toReturn = result;
                    if (result.getErrors() != null && result.getErrors().size() > 500) {
                        log.info("失败原因较多（共 {} 条），仅返回前 500 条供前端展示", result.getErrors().size());
                        toReturn = ImportResult.builder()
                                .totalRows(result.getTotalRows())
                                .successCount(result.getSuccessCount())
                                .failCount(result.getFailCount())
                                .errors(new ArrayList<>(result.getErrors().subList(0, 500)))
                                .build();
                    }
                    v.setResult(toReturn);
                    log.info("异步导入任务完成，taskId: {}, 成功: {}, 失败: {}", taskId, result.getSuccessCount(), result.getFailCount());

                    // SSE 推送：阶段3 - 完成
                    importProgressService.pushComplete(taskId, "success", toReturn);
                }
            } catch (Exception e) {
                log.error("异步导入失败，taskId: {}, 错误: {}", taskId, e.getMessage(), e);
                ImportTaskVO v = TASK_MAP.get(taskId);
                if (v != null) {
                    v.setStatus("failed");
                    v.setProgressPercent(0);
                    // 失败时返回错误信息（限制错误列表大小，避免响应过大）
                    List<ImportError> errors = List.of(ImportError.builder()
                            .row(0)
                            .column("系统")
                            .message("导入异常: " + e.getMessage())
                            .build());
                    ImportResult failResult = ImportResult.builder()
                            .totalRows(0)
                            .successCount(0)
                            .failCount(0)
                            .errors(errors)
                            .build();
                    v.setResult(failResult);

                    // SSE 推送：失败
                    importProgressService.pushError(taskId, "导入失败: " + e.getMessage());
                }
            }
        });
        return taskId;
    }

    /**
     * 获取异步任务状态
     */
    public ImportTaskVO getTask(String taskId) {
        return TASK_MAP.get(taskId);
    }

    /**
     * 导入入口（两阶段：先校验，后写库）
     */
    @Transactional(rollbackFor = Exception.class)
    public ImportResult importFromExcelWithTransaction(String fileUrl, Integer totalRows, String taskId) {
        log.info("开始事务性导入数据，fileUrl: {}, taskId: {}", fileUrl, taskId);
        try {
            Path path = resolveFileUrlToPath(fileUrl);
            log.info("解析后的文件路径(事务内): {}", path);
            if (!Files.exists(path)) {
                log.error("文件不存在: {}", path);
                throw new BusinessException("文件不存在: " + path);
            }
            if (!Files.isRegularFile(path)) {
                log.error("不是常规文件: {}", path);
                throw new BusinessException("不是常规文件: " + path);
            }
            ImportResult result = doParallelImport(path, taskId, totalRows);
            log.info("事务性导入完成（多线程解析 + 多线程写库），成功: {}, 失败: {}", result.getSuccessCount(), result.getFailCount());
            return result;
        } catch (BusinessException e) {
            log.error("事务性导入失败（业务异常）: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("事务性导入失败（系统异常）: {}", e.getMessage(), e);
            throw new BusinessException("导入失败: " + e.getMessage());
        }
    }

    /**
     * 快速统计行数（限制最大读取行数，避免大文件耗时过长）
     */
    private int countRowsFast(Path path) {
        final int[] count = {0};
        final int maxCount = getSyncThreshold() + 1000; // 超过阈值+1000行就停止统计
        try (InputStream is = Files.newInputStream(path)) {
            EasyExcel.read(is, getDtoClass(), new com.alibaba.excel.event.AnalysisEventListener<DTO>() {
                @Override
                public void invoke(DTO data, com.alibaba.excel.context.AnalysisContext context) {
                    count[0]++;
                    // 如果超过最大统计行数，停止读取（通过抛出异常）
                    if (count[0] > maxCount) {
                        throw new RuntimeException("行数超过统计上限，需要异步处理");
                    }
                }

                @Override
                public void doAfterAllAnalysed(com.alibaba.excel.context.AnalysisContext context) {
                }
            }).sheet().doRead();
        } catch (RuntimeException e) {
            // 如果是我们主动抛出的异常（行数超过上限），返回上限值+1
            if (e.getMessage() != null && e.getMessage().contains("行数超过统计上限")) {
                log.info("文件行数超过快速统计上限（{}），将异步处理", maxCount);
                return maxCount + 1;
            }
            log.warn("统计行数失败: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("统计行数失败: {}", e.getMessage());
        }
        return count[0];
    }

    /**
     * 多线程解析 + 多线程写库的核心导入流程
     */
    private ImportResult doParallelImport(Path path, String taskId, Integer frontendTotalRows) {
        // 文件大小（用于估算总行数和动态计算 worker 数量）
        long fileSizeBytes = 0L;
        long fileSizeMB = 0L;
        try {
            fileSizeBytes = Files.size(path);
            fileSizeMB = Math.max(1L, fileSizeBytes / (1024 * 1024));
            log.info("导入文件大小: {} bytes ({} MB)", fileSizeBytes, fileSizeMB);
        } catch (Exception e) {
            log.warn("获取导入文件大小失败: {}", e.getMessage());
        }

        // 确定总行数
        Integer estimatedTotalRows = null;
        if (frontendTotalRows != null && frontendTotalRows > 0) {
            estimatedTotalRows = frontendTotalRows;
            log.info("使用前端传入的 totalRows 计算进度: {}", estimatedTotalRows);
        } else {
            // 大文件：不估算，解析阶段只显示已处理行数，解析完成后获取实际行数
            log.info("大文件导入：解析阶段不显示进度条，解析完成后获取实际行数");
        }

        Context context = buildContext(taskId, estimatedTotalRows);

        ImportResult result = ImportResult.builder()
                .totalRows(0)
                .successCount(0)
                .failCount(0)
                .errors(new ArrayList<>())
                .build();

        // 阶段1：多线程解析与业务校验，仅收集合法实体与错误信息，不进行任何数据库写入
        List<Entity> validEntities = new ArrayList<>();
        int validationFailCount = 0;

        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int maxByCpu = Math.max(2, availableProcessors - 1);
        int maxByConfig = maxWorkerCountConfig > 0 ? maxWorkerCountConfig : 8;
        int upperBound = Math.min(maxByCpu, maxByConfig);

        // 根据文件大小动态调整期望 worker 数量
        int suggested;
        if (fileSizeMB <= 5) {
            suggested = 2;
        } else if (fileSizeMB <= 20) {
            suggested = 4;
        } else if (fileSizeMB <= 80) {
            suggested = 6;
        } else {
            suggested = 8;
        }

        int workerCount = Math.max(2, Math.min(suggested, upperBound));

        // 队列容量和批量大小使用全局配置（<=0 时回退到默认常量）
        int queueCapacity = queueCapacityConfig > 0 ? queueCapacityConfig : QUEUE_CAPACITY;
        int batchSaveSize = batchSaveSizeConfig > 0 ? batchSaveSizeConfig : BATCH_SAVE_SIZE;

        log.info(
                "并行导入配置：cores={}, fileSizeMB={}, maxWorkerCountConfig={}, maxByCpu={}, suggested={}, actualWorkerCount={}, queueCapacity={}, batchSaveSize={}",
                availableProcessors, fileSizeMB, maxWorkerCountConfig, maxByCpu, suggested, workerCount,
                queueCapacity, batchSaveSize);

        BlockingQueue<RawRow<DTO>> dtoQueue = new ArrayBlockingQueue<>(queueCapacity);
        // 使用无界队列存放解析结果，避免在大文件场景下 resultQueue 填满导致 worker 阻塞、进而卡死 Excel 读取
        BlockingQueue<ProcessedRow<Entity>> resultQueue = new LinkedBlockingQueue<>();
        ExecutorService workerPool = Executors.newFixedThreadPool(workerCount, r -> {
            Thread t = new Thread(r, "import-worker");
            t.setDaemon(true);
            return t;
        });

        // 启动 worker 线程：只做 CPU 运算，不访问数据库
        for (int i = 0; i < workerCount; i++) {
            workerPool.execute(() -> {
                while (true) {
                    try {
                        RawRow<DTO> raw = dtoQueue.take();
                        if (raw.isPoison()) {
                            break;
                        }
                        ProcessedRow<Entity> processed = processSingleRow(raw, context);
                        resultQueue.put(processed);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("导入 worker 线程被中断: {}", e.getMessage());
                        break;
                    } catch (Exception e) {
                        log.error("处理导入行时发生异常: {}", e.getMessage(), e);
                    }
                }
            });
        }

        // 为 lambda 捕获的 final 变量
        final String finalTaskId = taskId;
        final Integer finalEstimatedTotalRows = estimatedTotalRows;

        try {
            // 当前事务线程作为 ExcelReader：读取 Excel 并生产 RawRow
            BiConsumer<Integer, Integer> progressCallback = null;
            if (finalTaskId != null) {
                progressCallback = (readRows, totalRows) -> {
                    if (totalRows != null && totalRows > 0) {
                        // 有总行数：显示准确的百分比进度
                        int parsingProgress = Math.min(99, (int) Math.round(readRows * 100.0 / totalRows));
                        importProgressService.pushProgress(finalTaskId, parsingProgress, readRows, totalRows);
                        log.debug("Excel 解析进度推送，taskId: {}, 已读取: {} 行, 总行数: {}, 解析进度: {}%",
                                finalTaskId, readRows, totalRows, parsingProgress);
                    } else {
                        // 大文件：不知道总行数，只推送已读取行数，前端显示"已解析 X 行"
                        importProgressService.pushProgress(finalTaskId, 0, readRows, 0);
                        log.debug("Excel 解析进度推送（大文件），taskId: {}, 已读取: {} 行", finalTaskId, readRows);
                    }
                };
            }

            // 保存 listener 引用，以便获取实际读取行数
            ImportParallelListener<DTO> listener = new ImportParallelListener<>(
                    dtoQueue, progressCallback, finalEstimatedTotalRows, getEmptyRowPredicate());
            try (InputStream is = Files.newInputStream(path)) {
                EasyExcel.read(is, getDtoClass(), listener)
                        .sheet()
                        .doRead();
            } catch (Exception e) {
                log.error("读取 Excel 失败: {}", e.getMessage(), e);
                throw new BusinessException("读取 Excel 失败: " + e.getMessage());
            }

            // Excel 读取完成，获取实际行数
            int actualReadRows = listener.getReadRowCount();
            log.info("Excel 解析完成，实际读取行数: {}", actualReadRows);

            // 如果之前没有总行数（大文件），现在用实际行数更新
            if (estimatedTotalRows == null || estimatedTotalRows <= 0) {
                estimatedTotalRows = actualReadRows;
                log.info("大文件导入：使用实际读取行数 {} 作为总行数", actualReadRows);
            }

            // 生产结束，投递毒丸让 worker 退出
            for (int i = 0; i < workerCount; i++) {
                dtoQueue.put(RawRow.poisonPill());
            }

            // 等待所有 worker 完成处理
            workerPool.shutdown();
            boolean terminated = workerPool.awaitTermination(1, TimeUnit.HOURS);
            if (!terminated) {
                log.warn("导入 worker 线程在预期时间内未全部结束，可能存在卡死情况");
            }

            // 阶段1汇总：消费解析结果队列，构建合法实体列表与错误列表，不写库
            ProcessedRow<Entity> pr;
            int parsedRows = 0;
            final int progressStep = 500;
            while ((pr = resultQueue.poll()) != null) {
                parsedRows++;
                if (pr.getErrors() != null && !pr.getErrors().isEmpty()) {
                    result.getErrors().addAll(pr.getErrors());
                }
                if (pr.getEntity() == null) {
                    validationFailCount++;
                } else {
                    validEntities.add(pr.getEntity());
                }
                // 异步任务：解析阶段也定期更新进度，让前端看到进度条在移动
                if (taskId != null && parsedRows % progressStep == 0) {
                    int success = validEntities.size();
                    int fail = validationFailCount;
                    synchronized (result) {
                        result.setSuccessCount(success);
                        result.setFailCount(fail);
                        result.setTotalRows(success + fail);
                    }
                    updateTaskProgress(taskId, result, estimatedTotalRows);
                }
            }

            // 若存在任何校验错误：直接返回结果，不进入写库阶段，保证"校验失败⇒0插入"
            if (!result.getErrors().isEmpty()) {
                int totalRows = validationFailCount + validEntities.size();
                result.setSuccessCount(0);
                result.setFailCount(validationFailCount);
                result.setTotalRows(totalRows);
                log.info("阶段1校验完成，存在错误 {} 条，合法记录 {} 条，跳过写库阶段", validationFailCount, validEntities.size());
                return result;
            }

            // 阶段2：多线程写库（多事务），仅在无校验错误时执行
            if (validEntities.isEmpty()) {
                log.info("阶段1校验通过但未发现任何合法记录，无需写库");
                result.setSuccessCount(0);
                result.setFailCount(0);
                result.setTotalRows(0);
                return result;
            }

            log.info("阶段1校验通过，准备进入阶段2写库，合法记录数: {}", validEntities.size());

            // SSE 推送：阶段2 - 开始导入（Excel 解析完成，开始写入数据库）
            if (taskId != null) {
                importProgressService.pushStage(taskId, "importing", "开始导入数据...", estimatedTotalRows);
            }

            performParallelWrite(validEntities, result, validationFailCount, taskId, estimatedTotalRows, batchSaveSize);

            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("导入中断: " + e.getMessage());
        } finally {
            workerPool.shutdownNow();
        }
    }

    /**
     * 阶段2：多线程写库（多事务），每批使用独立事务写入数据库
     */
    private void performParallelWrite(List<Entity> validEntities,
                                      ImportResult result,
                                      int validationFailCount,
                                      String taskId,
                                      Integer estimatedTotalRows,
                                      int batchSaveSize) {
        int totalValidRows = validEntities.size();
        if (totalValidRows == 0) {
            log.info("阶段2写库：没有需要写入的合法记录");
            result.setSuccessCount(0);
            result.setFailCount(validationFailCount);
            result.setTotalRows(validationFailCount);
            return;
        }

        // 写库阶段统计：成功条数、DB 错误条数、DB 错误详情
        AtomicInteger successCounter = new AtomicInteger(0);
        AtomicInteger dbErrorCounter = new AtomicInteger(0);
        List<ImportError> dbErrors = java.util.Collections.synchronizedList(new ArrayList<>());

        int cores = Runtime.getRuntime().availableProcessors();
        int maxByCpu = Math.max(1, cores - 1);
        int maxByConfig = maxWorkerCountConfig > 0 ? maxWorkerCountConfig : 8;
        int writerCount = Math.max(1, Math.min(maxByCpu, maxByConfig));

        log.info("阶段2写库线程配置：cores={}, maxByCpu={}, maxWorkerCountConfig={}, writerCount={}",
                cores, maxByCpu, maxWorkerCountConfig, writerCount);

        ExecutorService writePool = Executors.newFixedThreadPool(
                writerCount,
                r -> {
                    Thread t = new Thread(r, "import-writer");
                    t.setDaemon(true);
                    return t;
                });

        int totalBatches = (totalValidRows + batchSaveSize - 1) / batchSaveSize;
        List<Future<?>> futures = new ArrayList<>(totalBatches);

        for (int i = 0; i < totalBatches; i++) {
            final int fromIndex = i * batchSaveSize;
            final int toIndex = Math.min(fromIndex + batchSaveSize, totalValidRows);
            final List<Entity> batch = new ArrayList<>(validEntities.subList(fromIndex, toIndex));

            futures.add(writePool.submit(() -> {
                int batchSize = batch.size();
                try {
                    // 每批一个独立事务
                    selfProxy.saveBatchWithTransaction(batch);
                    int succ = successCounter.addAndGet(batchSize);

                    // 异步任务：按批次更新进度
                    if (taskId != null) {
                        int dbErr = dbErrorCounter.get();
                        synchronized (result) {
                            result.setSuccessCount(succ);
                            result.setFailCount(validationFailCount + dbErr);
                            int total = succ + validationFailCount + dbErr;
                            result.setTotalRows(total);
                        }
                        int estimateTotal = estimatedTotalRows != null
                                ? estimatedTotalRows
                                : (validationFailCount + totalValidRows);
                        updateTaskProgress(taskId, result, estimateTotal);
                    }
                } catch (Exception e) {
                    log.error("导入阶段2单批写库失败，批大小: {}, 错误: {}", batchSize, e.getMessage(), e);
                    dbErrorCounter.addAndGet(batchSize);
                    dbErrors.add(ImportError.builder()
                            .row(0)
                            .column("数据库写入")
                            .message("批量写入失败: " + e.getMessage())
                            .build());
                }
                return null;
            }));
        }

        // 等待所有写库任务完成
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("等待写库任务完成时被中断: {}", e.getMessage());
                throw new BusinessException("写库阶段被中断: " + e.getMessage());
            } catch (Exception e) {
                log.error("写库任务执行异常: {}", e.getMessage(), e);
                // 具体批次错误已在子任务中记录，这里仅记录汇总日志
            }
        }
        writePool.shutdown();

        int finalSuccess = successCounter.get();
        int finalDbError = dbErrorCounter.get();
        int totalRows = finalSuccess + validationFailCount + finalDbError;

        result.setSuccessCount(finalSuccess);
        result.setFailCount(validationFailCount + finalDbError);
        result.setTotalRows(totalRows);
        result.getErrors().addAll(dbErrors);

        log.info("阶段2写库完成，合法记录总数: {}, 成功: {}, DB错误: {}, 校验失败: {}",
                totalValidRows, finalSuccess, finalDbError, validationFailCount);
    }

    /**
     * 单批学生写入（独立事务），供多线程写库阶段使用
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveBatchWithTransaction(List<Entity> batch) {
        int batchSize = batchSaveSizeConfig > 0 ? batchSaveSizeConfig : BATCH_SAVE_SIZE;
        boolean ok = saveBatch(batch, batchSize);
        if (!ok) {
            throw new BusinessException("批量插入失败");
        }
    }

    /**
     * 更新异步任务进度
     */
    private void updateTaskProgress(String taskId, ImportResult result, Integer estimatedTotalRows) {
        ImportTaskVO vo = TASK_MAP.get(taskId);
        if (vo == null) {
            return;
        }

        int progressPercent = 0;
        int processedRows = (result.getTotalRows() != null ? result.getTotalRows() : 0);
        int totalRows = estimatedTotalRows != null ? estimatedTotalRows : 0;

        if (estimatedTotalRows != null && estimatedTotalRows > 0) {
            // 导入阶段进度：独立的 0-99%（完成时由 pushComplete 设置 100%）
            progressPercent = Math.min(99, (int) Math.round(processedRows * 100.0 / estimatedTotalRows));
        } else {
            // 如果没有预估总行数，根据已处理批次估算
            int processedBatches = processedRows / 1000;
            progressPercent = Math.min(99, processedBatches / 2); // 每 2000 行 1%
        }

        vo.setProgressPercent(progressPercent);

        // 进度更新时只更新统计信息，不返回错误列表（避免响应过大）
        if (vo.getResult() == null) {
            // 首次创建，只包含统计信息
            vo.setResult(ImportResult.builder()
                    .totalRows(result.getTotalRows())
                    .successCount(result.getSuccessCount())
                    .failCount(result.getFailCount())
                    .errors(new ArrayList<>()) // 进度更新时不返回错误列表
                    .build());
        } else {
            // 更新统计信息，保持错误列表为空
            vo.getResult().setTotalRows(result.getTotalRows());
            vo.getResult().setSuccessCount(result.getSuccessCount());
            vo.getResult().setFailCount(result.getFailCount());
        }

        // SSE 推送实时进度（包含成功/失败数）
        Integer successCount = result.getSuccessCount();
        Integer failCount = result.getFailCount();
        importProgressService.pushProgress(taskId, progressPercent, processedRows, totalRows, successCount, failCount);

        // 每10000行更新一次详细日志（避免日志过多）
        if (processedRows % 10000 == 0) {
            log.info("导入进度更新，taskId: {}, 已处理: {} 行, 预估总行数: {}, 成功: {}, 失败: {}, 进度: {}%",
                    taskId,
                    processedRows,
                    estimatedTotalRows,
                    (result.getSuccessCount() != null ? result.getSuccessCount() : 0),
                    (result.getFailCount() != null ? result.getFailCount() : 0),
                    progressPercent);
        }
    }

    /**
     * worker 线程使用的行级处理方法：DTO -> 实体 + 行级错误
     */
    private ProcessedRow<Entity> processSingleRow(RawRow<DTO> raw, Context context) {
        List<ImportError> rowErrors = new ArrayList<>();
        // 每行内的"批次唯一键去重"集合，仅用于该行上下文
        Set<String> batchKeys = new java.util.HashSet<>();
        Entity entity = convertDtoToEntity(raw.getDto(), raw.getRowIndex(), context, rowErrors, batchKeys);
        return new ProcessedRow<>(raw.getRowIndex(), entity, rowErrors);
    }
}
