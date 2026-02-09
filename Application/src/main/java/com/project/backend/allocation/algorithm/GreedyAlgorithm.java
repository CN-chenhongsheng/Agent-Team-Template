package com.project.backend.allocation.algorithm;

import com.project.backend.student.entity.Student;
import com.project.backend.allocation.algorithm.model.AllocationResultDTO;
import com.project.backend.allocation.algorithm.model.RoomMatchResult;
import com.project.backend.allocation.entity.AllocationConfig;
import com.project.backend.allocation.service.CompatibilityService;
import com.project.backend.room.entity.Bed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 贪心分配算法（优化版）
 * 策略：依次为每个学生找当前最优的床位
 * 优化：性别分桶、空房间快速路径、早停、分片并行
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GreedyAlgorithm implements AllocationAlgorithm {

    private final CompatibilityService compatibilityService;

    /** 早停阈值：匹配分 >= 此值时停止搜索 */
    private static final BigDecimal EARLY_STOP_SCORE = BigDecimal.valueOf(90);

    @Override
    public String getAlgorithmType() {
        return "greedy";
    }

    @Override
    public String getAlgorithmName() {
        return "贪心算法";
    }

    @Override
    public String getDescription() {
        return "依次为每个学生找当前最优的床位，速度快但可能非全局最优";
    }

    @Override
    public String getAdvantages() {
        return "速度极快，适合快速预览和时间紧迫场景";
    }

    @Override
    public String getDisadvantages() {
        return "结果依赖处理顺序，可能非全局最优解";
    }

    @Override
    public String getEstimatedTime(int studentCount) {
        if (studentCount <= 1000) return "约1-2秒";
        if (studentCount <= 10000) return "约3-5秒";
        if (studentCount <= 50000) return "约5-10秒";
        if (studentCount <= 200000) return "约15-30秒";
        return "约30-60秒";
    }

    @Override
    public List<AllocationResultDTO> allocate(
            List<Student> students,
            Map<Long, List<Bed>> roomBedMap,
            Map<Long, List<Student>> roomStudentMap,
            AllocationConfig config,
            Consumer<AllocationProgress> progressCallback) {

        int tier = AlgorithmHelper.determineTier(students.size());
        log.info("开始贪心分配，学生数：{}，可用房间数：{}，使用 Tier {}", students.size(), roomBedMap.size(), tier);

        if (tier >= 2) {
            return allocatePartitioned(students, roomBedMap, roomStudentMap, config, progressCallback);
        }

        return allocateDirect(students, roomBedMap, roomStudentMap, config, progressCallback);
    }

    /**
     * Tier 2/3: 分片并行
     */
    private List<AllocationResultDTO> allocatePartitioned(
            List<Student> students,
            Map<Long, List<Bed>> roomBedMap,
            Map<Long, List<Student>> roomStudentMap,
            AllocationConfig config,
            Consumer<AllocationProgress> progressCallback) {

        List<AlgorithmHelper.Partition> partitions = AlgorithmHelper.partitionStudentsAndRooms(
                students, roomBedMap, roomStudentMap);

        if (progressCallback != null) {
            progressCallback.accept(new AllocationProgress(
                    students.size(), 0, 0, 0,
                    String.format("已分为 %d 个分片，开始并行执行", partitions.size())));
        }

        ForkJoinPool pool = new ForkJoinPool(Math.min(partitions.size(), Runtime.getRuntime().availableProcessors()));
        try {
            List<AllocationResultDTO> allResults = pool.submit(() ->
                    partitions.parallelStream()
                            .flatMap(p -> allocateDirect(p.students, p.roomBedMap, p.roomStudentMap, config, null).stream())
                            .collect(Collectors.toList())
            ).get();

            int success = (int) allResults.stream().filter(AllocationResultDTO::isSuccess).count();
            int failed = allResults.size() - success;
            if (progressCallback != null) {
                progressCallback.accept(new AllocationProgress(
                        students.size(), students.size(), success, failed, "分片并行执行完成"));
            }
            return allResults;
        } catch (Exception e) {
            log.error("分片并行执行失败，回退到单线程", e);
            return allocateDirect(students, roomBedMap, roomStudentMap, config, progressCallback);
        } finally {
            pool.shutdown();
        }
    }

    /**
     * Tier 1: 直接贪心分配（性别分桶 + 空房间快速路径 + 早停）
     */
    private List<AllocationResultDTO> allocateDirect(
            List<Student> students,
            Map<Long, List<Bed>> roomBedMap,
            Map<Long, List<Student>> roomStudentMap,
            AllocationConfig config,
            Consumer<AllocationProgress> progressCallback) {

        List<AllocationResultDTO> results = new ArrayList<>();
        int totalStudents = students.size();
        int processedCount = 0, successCount = 0, failedCount = 0;

        // 复制可用床位和室友映射
        Map<Long, List<Bed>> availableBedMap = new HashMap<>();
        for (Map.Entry<Long, List<Bed>> entry : roomBedMap.entrySet()) {
            availableBedMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        Map<Long, List<Student>> currentRoomStudentMap = new HashMap<>();
        for (Map.Entry<Long, List<Student>> entry : roomStudentMap.entrySet()) {
            currentRoomStudentMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        // 性别分桶
        Map<Integer, Set<Long>> genderBuckets = AlgorithmHelper.bucketRoomsByGender(availableBedMap, currentRoomStudentMap);

        // 维护空房间集合（用于快速路径）
        Set<Long> emptyRoomIds = new HashSet<>();
        for (Long roomId : availableBedMap.keySet()) {
            if (!currentRoomStudentMap.containsKey(roomId) || currentRoomStudentMap.get(roomId).isEmpty()) {
                emptyRoomIds.add(roomId);
            }
        }

        // 按习惯特征明显程度排序（难匹配的优先）
        List<Student> sortedStudents = sortStudentsByDistinctiveness(students);

        for (Student student : sortedStudents) {
            processedCount++;

            if (progressCallback != null && processedCount % 100 == 0) {
                progressCallback.accept(new AllocationProgress(
                        totalStudents, processedCount, successCount, failedCount,
                        "正在分配第 " + processedCount + " 个学生"));
            }

            // 获取该学生的候选房间（同性别 + 空房间）
            Set<Long> candidateRoomIds = AlgorithmHelper.getCandidateRoomIds(student.getGender(), genderBuckets);

            BedMatch bestMatch = findBestBed(student, candidateRoomIds, emptyRoomIds,
                    availableBedMap, currentRoomStudentMap, config);

            if (bestMatch != null && bestMatch.bed != null) {
                Bed bed = bestMatch.bed;
                Long roomId = bed.getRoomId();
                boolean wasEmpty = emptyRoomIds.contains(roomId);

                results.add(AlgorithmHelper.buildSuccess(student, bed, bestMatch.matchResult));
                successCount++;

                // 更新状态
                availableBedMap.get(roomId).remove(bed);
                if (availableBedMap.get(roomId).isEmpty()) {
                    availableBedMap.remove(roomId);
                    // 从所有桶中移除满员房间
                    genderBuckets.values().forEach(set -> set.remove(roomId));
                }
                currentRoomStudentMap.computeIfAbsent(roomId, k -> new ArrayList<>()).add(student);

                // 如果之前是空房间，更新桶
                if (wasEmpty) {
                    emptyRoomIds.remove(roomId);
                    AlgorithmHelper.updateGenderBucket(roomId, student.getGender(), genderBuckets);
                }
            } else {
                results.add(AlgorithmHelper.buildFail(student,
                        bestMatch != null ? bestMatch.failReason : "无可用床位"));
                failedCount++;
            }
        }

        if (progressCallback != null) {
            progressCallback.accept(new AllocationProgress(
                    totalStudents, processedCount, successCount, failedCount, "分配完成"));
        }

        log.info("贪心分配完成，成功：{}，失败：{}", successCount, failedCount);
        return results;
    }

    /**
     * 为学生找最佳床位（性别分桶 + 空房间快速路径 + 早停）
     */
    private BedMatch findBestBed(
            Student student,
            Set<Long> candidateRoomIds,
            Set<Long> emptyRoomIds,
            Map<Long, List<Bed>> availableBedMap,
            Map<Long, List<Student>> roomStudentMap,
            AllocationConfig config) {

        BedMatch bestMatch = null;
        BigDecimal bestScore = BigDecimal.valueOf(-1);

        // 快速路径：如果有空房间且候选中包含，直接取一个（匹配分固定100）
        for (Long emptyRoomId : emptyRoomIds) {
            if (candidateRoomIds.contains(emptyRoomId)) {
                List<Bed> beds = availableBedMap.get(emptyRoomId);
                if (beds != null && !beds.isEmpty()) {
                    RoomMatchResult emptyResult = compatibilityService.calculateRoomCompatibility(
                            student, List.of(), config);
                    return new BedMatch(beds.get(0), emptyResult, null);
                }
            }
        }

        // 正常路径：遍历候选房间
        for (Long roomId : candidateRoomIds) {
            List<Bed> beds = availableBedMap.get(roomId);
            if (beds == null || beds.isEmpty()) continue;

            List<Student> roommates = roomStudentMap.getOrDefault(roomId, List.of());
            RoomMatchResult matchResult = compatibilityService.calculateRoomCompatibility(
                    student, roommates, config);

            if (Boolean.TRUE.equals(matchResult.getHasHardConflict())) continue;

            if (matchResult.getAvgScore().compareTo(bestScore) > 0) {
                bestScore = matchResult.getAvgScore();
                bestMatch = new BedMatch(beds.get(0), matchResult, null);

                // 早停：分数够好就不继续找了
                if (bestScore.compareTo(EARLY_STOP_SCORE) >= 0) break;
            }
        }

        if (bestMatch == null) {
            return new BedMatch(null, null, "没有符合条件的床位（可能存在硬约束冲突）");
        }
        return bestMatch;
    }

    /**
     * 按习惯特征明显程度排序（难匹配的优先处理）
     */
    private List<Student> sortStudentsByDistinctiveness(List<Student> students) {
        return students.stream()
                .sorted(Comparator.comparingInt(this::getDistinctivenessScore).reversed())
                .collect(Collectors.toList());
    }

    private int getDistinctivenessScore(Student s) {
        int score = 0;
        if (Integer.valueOf(1).equals(s.getSmokingStatus())) score += 10;
        if (Integer.valueOf(3).equals(s.getSleepSchedule())) score += 8;
        if (Integer.valueOf(0).equals(s.getSleepSchedule())) score += 8;
        if (Integer.valueOf(1).equals(s.getSensitiveToSound())) score += 5;
        if (Integer.valueOf(1).equals(s.getSnores())) score += 5;
        return score;
    }

    /**
     * 床位匹配结果
     */
    private static class BedMatch {
        Bed bed;
        RoomMatchResult matchResult;
        String failReason;

        BedMatch(Bed bed, RoomMatchResult matchResult, String failReason) {
            this.bed = bed;
            this.matchResult = matchResult;
            this.failReason = failReason;
        }
    }
}
