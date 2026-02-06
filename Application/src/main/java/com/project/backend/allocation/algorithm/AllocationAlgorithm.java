package com.project.backend.allocation.algorithm;

import com.project.backend.student.entity.Student;
import com.project.backend.allocation.algorithm.model.AllocationResultDTO;
import com.project.backend.allocation.entity.AllocationConfig;
import com.project.backend.room.entity.Bed;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 分配算法接口
 * 定义智能分配算法的通用接口
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
public interface AllocationAlgorithm {

    /**
     * 获取算法类型标识
     */
    String getAlgorithmType();

    /**
     * 获取算法名称
     */
    String getAlgorithmName();

    /**
     * 获取算法描述
     */
    String getDescription();

    /**
     * 获取算法优点
     */
    String getAdvantages();

    /**
     * 获取算法缺点
     */
    String getDisadvantages();

    /**
     * 获取预估耗时说明
     *
     * @param studentCount 学生数量
     * @return 耗时说明
     */
    String getEstimatedTime(int studentCount);

    /**
     * 是否推荐使用
     */
    default boolean isRecommended() {
        return false;
    }

    /**
     * 执行分配算法
     *
     * @param students         待分配学生列表
     * @param roomBedMap       可用床位映射（房间ID -> 该房间的可用床位列表）
     * @param roomStudentMap   房间现有学生映射（房间ID -> 该房间现有学生列表）
     * @param config           分配配置
     * @param progressCallback 进度回调（可选）
     * @return 分配结果列表
     */
    List<AllocationResultDTO> allocate(
            List<Student> students,
            Map<Long, List<Bed>> roomBedMap,
            Map<Long, List<Student>> roomStudentMap,
            AllocationConfig config,
            Consumer<AllocationProgress> progressCallback
    );

    /**
     * 分配进度信息
     */
    class AllocationProgress {
        private int totalStudents;
        private int processedCount;
        private int successCount;
        private int failedCount;
        private String currentStage;
        private int progressPercent;

        public AllocationProgress() {}

        public AllocationProgress(int totalStudents, int processedCount, int successCount,
                                  int failedCount, String currentStage) {
            this.totalStudents = totalStudents;
            this.processedCount = processedCount;
            this.successCount = successCount;
            this.failedCount = failedCount;
            this.currentStage = currentStage;
            this.progressPercent = totalStudents > 0 ? (processedCount * 100 / totalStudents) : 0;
        }

        // Getters and Setters
        public int getTotalStudents() { return totalStudents; }
        public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }
        public int getProcessedCount() { return processedCount; }
        public void setProcessedCount(int processedCount) { this.processedCount = processedCount; }
        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }
        public int getFailedCount() { return failedCount; }
        public void setFailedCount(int failedCount) { this.failedCount = failedCount; }
        public String getCurrentStage() { return currentStage; }
        public void setCurrentStage(String currentStage) { this.currentStage = currentStage; }
        public int getProgressPercent() { return progressPercent; }
        public void setProgressPercent(int progressPercent) { this.progressPercent = progressPercent; }
    }
}
