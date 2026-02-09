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
 * 模拟退火分配算法（优化版）
 * 策略：通过模拟物理退火过程寻找全局最优分配方案
 * 优化：Delta Scoring + 就地交换 + 自适应参数 + 分片并行
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SimulatedAnnealingAlgorithm implements AllocationAlgorithm {

    private final CompatibilityService compatibilityService;

    // 模拟退火参数
    private static final double INITIAL_TEMPERATURE = 1000.0;
    private static final double MIN_TEMPERATURE = 1.0;
    private static final int ITERATIONS_PER_TEMP = 100;

    @Override
    public String getAlgorithmType() {
        return "annealing";
    }

    @Override
    public String getAlgorithmName() {
        return "模拟退火算法";
    }

    @Override
    public String getDescription() {
        return "通过模拟物理退火过程，在解空间中搜索全局最优分配方案";
    }

    @Override
    public String getAdvantages() {
        return "能跳出局部最优，找到更好的全局解，结果最优";
    }

    @Override
    public String getDisadvantages() {
        return "速度较慢，适合对分配质量要求高的场景";
    }

    @Override
    public String getEstimatedTime(int studentCount) {
        if (studentCount <= 500) return "约5-15秒";
        if (studentCount <= 5000) return "约15-30秒";
        if (studentCount <= 50000) return "约30-90秒";
        if (studentCount <= 200000) return "约2-5分钟";
        return "约5-10分钟";
    }

    @Override
    public boolean isRecommended() {
        return false;
    }

    @Override
    public List<AllocationResultDTO> allocate(
            List<Student> students,
            Map<Long, List<Bed>> roomBedMap,
            Map<Long, List<Student>> roomStudentMap,
            AllocationConfig config,
            Consumer<AllocationProgress> progressCallback) {

        int tier = AlgorithmHelper.determineTier(students.size());
        log.info("模拟退火分配开始，学生数：{}，房间数：{}，使用 Tier {}", students.size(), roomBedMap.size(), tier);

        if (tier >= 2) {
            return allocatePartitioned(students, roomBedMap, roomStudentMap, config, progressCallback);
        }

        return allocateDirect(students, roomBedMap, roomStudentMap, config, progressCallback);
    }

    /**
     * Tier 2/3: 分片并行分配
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

        // 并行执行各分片
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
     * Tier 1: 直接优化分配（Delta Scoring + 就地交换）
     */
    private List<AllocationResultDTO> allocateDirect(
            List<Student> students,
            Map<Long, List<Bed>> roomBedMap,
            Map<Long, List<Student>> roomStudentMap,
            AllocationConfig config,
            Consumer<AllocationProgress> progressCallback) {

        int totalStudents = students.size();

        // 1. 预构建不可变索引（一次性）
        List<Bed> allBeds = roomBedMap.values().stream().flatMap(List::stream).collect(Collectors.toList());
        Map<Long, Bed> bedMap = AlgorithmHelper.buildBedMap(allBeds);
        Map<Long, Student> studentMap = AlgorithmHelper.buildStudentMap(students);
        Map<Long, Long> bedToRoomMap = AlgorithmHelper.buildBedToRoomMap(allBeds);

        if (allBeds.size() < students.size()) {
            log.warn("床位数量不足：需要{}，可用{}", students.size(), allBeds.size());
        }

        if (progressCallback != null) {
            progressCallback.accept(new AllocationProgress(totalStudents, 0, 0, 0, "正在生成初始解"));
        }

        // 2. 生成初始解（贪心分配），同时构建可变的房间学生索引
        Map<Long, Long> assignment = new HashMap<>(); // studentId -> bedId
        Map<Long, Set<Long>> roomStudentIds = new HashMap<>(); // roomId -> Set<studentId>（可变索引）

        // 初始化已有学生的房间索引
        for (Map.Entry<Long, List<Student>> entry : roomStudentMap.entrySet()) {
            Set<Long> ids = entry.getValue().stream().map(Student::getId).collect(Collectors.toCollection(HashSet::new));
            roomStudentIds.put(entry.getKey(), ids);
        }

        generateInitialSolution(students, allBeds, assignment, roomStudentIds, studentMap, bedMap, bedToRoomMap, config);

        // 3. 预计算每个房间的得分缓存
        Map<Long, Double> roomScoreCache = new HashMap<>();
        for (Long roomId : roomStudentIds.keySet()) {
            roomScoreCache.put(roomId, calcRoomScore(roomId, roomStudentIds, studentMap, config));
        }

        double currentTotalScore = roomScoreCache.values().stream().mapToDouble(Double::doubleValue).sum();
        double bestTotalScore = currentTotalScore;
        Map<Long, Long> bestAssignment = new HashMap<>(assignment);

        if (progressCallback != null) {
            progressCallback.accept(new AllocationProgress(
                    totalStudents, 0, 0, 0,
                    String.format("初始解得分: %.2f，开始优化", currentTotalScore)));
        }

        // 4. 自适应参数
        int maxSwaps = Math.min(students.size() * 20, 500_000);
        double coolingRate = Math.pow(MIN_TEMPERATURE / INITIAL_TEMPERATURE,
                (double) ITERATIONS_PER_TEMP / maxSwaps);

        // 5. 模拟退火主循环（Delta Scoring + 就地交换）
        double temperature = INITIAL_TEMPERATURE;
        int iteration = 0;
        Random random = new Random();
        List<Long> assignedStudentIds = new ArrayList<>(assignment.keySet());

        if (assignedStudentIds.size() < 2) {
            return convertToResults(assignment, students, bedMap, bedToRoomMap, roomStudentIds, studentMap, config, progressCallback);
        }

        while (temperature > MIN_TEMPERATURE && iteration < maxSwaps) {
            for (int i = 0; i < ITERATIONS_PER_TEMP && iteration < maxSwaps; i++) {
                iteration++;

                // 随机选两个已分配的学生
                int idx1 = random.nextInt(assignedStudentIds.size());
                int idx2 = random.nextInt(assignedStudentIds.size());
                while (idx2 == idx1) idx2 = random.nextInt(assignedStudentIds.size());

                Long studentId1 = assignedStudentIds.get(idx1);
                Long studentId2 = assignedStudentIds.get(idx2);
                Long bedId1 = assignment.get(studentId1);
                Long bedId2 = assignment.get(studentId2);
                Long roomId1 = bedToRoomMap.get(bedId1);
                Long roomId2 = bedToRoomMap.get(bedId2);

                // 同房间交换无意义
                if (Objects.equals(roomId1, roomId2)) continue;

                // === 就地交换 ===
                doSwap(assignment, roomStudentIds, studentId1, studentId2, bedId1, bedId2, roomId1, roomId2);

                // === Delta Scoring：只重算两个房间 ===
                double newScoreRoom1 = calcRoomScore(roomId1, roomStudentIds, studentMap, config);
                double newScoreRoom2 = calcRoomScore(roomId2, roomStudentIds, studentMap, config);
                double oldScoreRoom1 = roomScoreCache.getOrDefault(roomId1, 0.0);
                double oldScoreRoom2 = roomScoreCache.getOrDefault(roomId2, 0.0);
                double delta = (newScoreRoom1 + newScoreRoom2) - (oldScoreRoom1 + oldScoreRoom2);

                // 计算接受概率
                double acceptProbability = delta > 0 ? 1.0 : Math.exp(delta / temperature);

                if (random.nextDouble() < acceptProbability) {
                    // 接受交换，更新缓存
                    roomScoreCache.put(roomId1, newScoreRoom1);
                    roomScoreCache.put(roomId2, newScoreRoom2);
                    currentTotalScore += delta;

                    if (currentTotalScore > bestTotalScore) {
                        bestTotalScore = currentTotalScore;
                        bestAssignment = new HashMap<>(assignment);
                    }
                } else {
                    // 拒绝交换，回滚（注意：交换 studentId 顺序，因为 doSwap 总是从 room1 移除 student1）
                    // 当前状态：room1 有 student2，room2 有 student1
                    // 需要：room1 移除 student2 加回 student1，room2 移除 student1 加回 student2
                    doSwap(assignment, roomStudentIds, studentId2, studentId1, bedId1, bedId2, roomId1, roomId2);
                }
            }

            temperature *= coolingRate;

            // 报告进度
            if (progressCallback != null && iteration % 1000 == 0) {
                double progress = (double) iteration / maxSwaps;
                progressCallback.accept(new AllocationProgress(
                        totalStudents, (int) (totalStudents * progress), 0, 0,
                        String.format("优化中... 温度: %.1f, 迭代: %d/%d, 最优得分: %.2f",
                                temperature, iteration, maxSwaps, bestTotalScore)));
            }
        }

        if (progressCallback != null) {
            progressCallback.accept(new AllocationProgress(
                    totalStudents, totalStudents, 0, 0,
                    String.format("优化完成，最终得分: %.2f，迭代次数: %d", bestTotalScore, iteration)));
        }

        // 6. 用最优解重建 roomStudentIds，然后转换结果
        // 重建索引
        Map<Long, Set<Long>> bestRoomStudentIds = new HashMap<>();
        for (Map.Entry<Long, List<Student>> entry : roomStudentMap.entrySet()) {
            bestRoomStudentIds.put(entry.getKey(), entry.getValue().stream()
                    .map(Student::getId).collect(Collectors.toCollection(HashSet::new)));
        }
        for (Map.Entry<Long, Long> entry : bestAssignment.entrySet()) {
            Long roomId = bedToRoomMap.get(entry.getValue());
            if (roomId != null) {
                bestRoomStudentIds.computeIfAbsent(roomId, k -> new HashSet<>()).add(entry.getKey());
            }
        }

        return convertToResults(bestAssignment, students, bedMap, bedToRoomMap, bestRoomStudentIds, studentMap, config, progressCallback);
    }

    /**
     * 就地交换两个学生的床位和房间索引
     */
    private void doSwap(Map<Long, Long> assignment,
                        Map<Long, Set<Long>> roomStudentIds,
                        Long studentId1, Long studentId2,
                        Long bedId1, Long bedId2,
                        Long roomId1, Long roomId2) {
        // 交换 assignment
        assignment.put(studentId1, bedId2);
        assignment.put(studentId2, bedId1);
        // 更新房间索引
        Set<Long> room1Students = roomStudentIds.get(roomId1);
        Set<Long> room2Students = roomStudentIds.get(roomId2);
        if (room1Students != null) { room1Students.remove(studentId1); room1Students.add(studentId2); }
        if (room2Students != null) { room2Students.remove(studentId2); room2Students.add(studentId1); }
    }

    /**
     * 计算单个房间的匹配得分（用于 Delta Scoring）
     */
    private double calcRoomScore(Long roomId,
                                  Map<Long, Set<Long>> roomStudentIds,
                                  Map<Long, Student> studentMap,
                                  AllocationConfig config) {
        Set<Long> ids = roomStudentIds.get(roomId);
        if (ids == null || ids.size() <= 1) return 100.0; // 空房间或单人房满分

        List<Student> roommates = ids.stream()
                .map(studentMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (roommates.size() <= 1) return 100.0;

        // 计算所有学生对之间的平均匹配分
        double totalScore = 0;
        int pairCount = 0;
        for (int i = 0; i < roommates.size(); i++) {
            for (int j = i + 1; j < roommates.size(); j++) {
                RoomMatchResult mr = compatibilityService.calculateRoomCompatibility(
                        roommates.get(i), List.of(roommates.get(j)), config);
                totalScore += mr.getAvgScore().doubleValue();
                if (Boolean.TRUE.equals(mr.getHasHardConflict())) {
                    totalScore -= 100; // 硬约束惩罚
                }
                pairCount++;
            }
        }

        return pairCount > 0 ? totalScore / pairCount : 100.0;
    }

    /**
     * 生成初始解（使用贪心策略 + 性别分桶 + 空房间快速路径 + 早停）
     */
    private void generateInitialSolution(
            List<Student> students, List<Bed> allBeds,
            Map<Long, Long> assignment, Map<Long, Set<Long>> roomStudentIds,
            Map<Long, Student> studentMap, Map<Long, Bed> bedMap,
            Map<Long, Long> bedToRoomMap, AllocationConfig config) {

        // 按房间分组可用床位
        Map<Long, List<Bed>> availableBedsByRoom = new HashMap<>();
        for (Bed bed : allBeds) {
            availableBedsByRoom.computeIfAbsent(bed.getRoomId(), k -> new ArrayList<>()).add(bed);
        }

        // 构建室友列表（用于性别分桶）
        Map<Long, List<Student>> roomStudentListMap = new HashMap<>();
        for (Map.Entry<Long, Set<Long>> entry : roomStudentIds.entrySet()) {
            roomStudentListMap.put(entry.getKey(), entry.getValue().stream()
                    .map(studentMap::get).filter(Objects::nonNull).collect(Collectors.toList()));
        }

        // 性别分桶：只遍历同性别 + 空房间
        Map<Integer, Set<Long>> genderBuckets = AlgorithmHelper.bucketRoomsByGender(availableBedsByRoom, roomStudentListMap);

        // 空房间集合（快速路径：空房间得分 100，无需调匹配计算）
        Set<Long> emptyRoomIds = new HashSet<>();
        for (Long roomId : availableBedsByRoom.keySet()) {
            Set<Long> existing = roomStudentIds.get(roomId);
            if (existing == null || existing.isEmpty()) {
                emptyRoomIds.add(roomId);
            }
        }

        // 按习惯独特性排序：难匹配的优先分配
        List<Student> sorted = students.stream()
                .sorted(Comparator.comparingInt(this::getDistinctivenessScore).reversed())
                .collect(Collectors.toList());

        int count = 0;
        for (Student student : sorted) {
            count++;
            if (count % 1000 == 0) {
                log.debug("初始解生成中... {}/{}", count, sorted.size());
            }

            // 获取该学生的候选房间（同性别 + 空房间）
            Set<Long> candidateRoomIds = AlgorithmHelper.getCandidateRoomIds(student.getGender(), genderBuckets);

            Bed bestBed = null;
            BigDecimal bestScore = BigDecimal.valueOf(-1);

            // 快速路径：优先分配到空房间（得分固定 100，无需匹配计算）
            for (Long emptyRoomId : emptyRoomIds) {
                if (candidateRoomIds.contains(emptyRoomId)) {
                    List<Bed> beds = availableBedsByRoom.get(emptyRoomId);
                    if (beds != null && !beds.isEmpty()) {
                        bestBed = beds.get(0);
                        bestScore = BigDecimal.valueOf(100);
                        break;
                    }
                }
            }

            // 正常路径：空房间用完了才遍历有人的房间
            if (bestBed == null) {
                for (Long roomId : candidateRoomIds) {
                    if (emptyRoomIds.contains(roomId)) continue; // 空房间已在上面处理
                    List<Bed> beds = availableBedsByRoom.get(roomId);
                    if (beds == null || beds.isEmpty()) continue;

                    Set<Long> existingIds = roomStudentIds.getOrDefault(roomId, Set.of());
                    List<Student> roommates = existingIds.stream()
                            .map(studentMap::get).filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    RoomMatchResult mr = compatibilityService.calculateRoomCompatibility(student, roommates, config);
                    if (!Boolean.TRUE.equals(mr.getHasHardConflict()) && mr.getAvgScore().compareTo(bestScore) > 0) {
                        bestScore = mr.getAvgScore();
                        bestBed = beds.get(0);
                        // 早停：得分够高就不继续搜索
                        if (bestScore.compareTo(BigDecimal.valueOf(90)) >= 0) break;
                    }
                }
            }

            if (bestBed != null) {
                assignment.put(student.getId(), bestBed.getId());
                Long roomId = bedToRoomMap.get(bestBed.getId());

                // 更新可用床位
                List<Bed> roomBeds = availableBedsByRoom.get(roomId);
                if (roomBeds != null) {
                    roomBeds.remove(bestBed);
                    if (roomBeds.isEmpty()) {
                        availableBedsByRoom.remove(roomId);
                        // 从所有桶中移除满员房间
                        genderBuckets.values().forEach(set -> set.remove(roomId));
                        emptyRoomIds.remove(roomId);
                    }
                }

                // 更新房间学生索引
                roomStudentIds.computeIfAbsent(roomId, k -> new HashSet<>()).add(student.getId());

                // 空房间变为有人，更新性别桶
                if (emptyRoomIds.remove(roomId)) {
                    AlgorithmHelper.updateGenderBucket(roomId, student.getGender(), genderBuckets);
                }
            }
        }

        log.info("初始解生成完成，已分配 {} / {} 学生", assignment.size(), students.size());
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
     * 将分配方案转换为结果列表
     */
    private List<AllocationResultDTO> convertToResults(
            Map<Long, Long> assignment, List<Student> students,
            Map<Long, Bed> bedMap, Map<Long, Long> bedToRoomMap,
            Map<Long, Set<Long>> roomStudentIds, Map<Long, Student> studentMap,
            AllocationConfig config, Consumer<AllocationProgress> progressCallback) {

        List<AllocationResultDTO> results = new ArrayList<>();
        int successCount = 0, failedCount = 0;

        for (Student student : students) {
            Long bedId = assignment.get(student.getId());
            if (bedId != null) {
                Bed bed = bedMap.get(bedId);
                if (bed != null) {
                    Long roomId = bedToRoomMap.get(bedId);
                    Set<Long> roommateIds = roomStudentIds.getOrDefault(roomId, Set.of());
                    List<Student> roommates = roommateIds.stream()
                            .filter(id -> !id.equals(student.getId()))
                            .map(studentMap::get).filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    RoomMatchResult mr = compatibilityService.calculateRoomCompatibility(student, roommates, config);
                    AllocationResultDTO result = AlgorithmHelper.buildSuccess(student, bed, mr);
                    if (Boolean.TRUE.equals(mr.getHasHardConflict())) {
                        result.setSuccess(false);
                        result.setFailReason(mr.getHardConflictReason());
                        failedCount++;
                    } else {
                        successCount++;
                    }
                    results.add(result);
                    continue;
                }
            }

            results.add(AlgorithmHelper.buildFail(student, "无可用床位或所有床位存在硬约束冲突"));
            failedCount++;
        }

        if (progressCallback != null) {
            progressCallback.accept(new AllocationProgress(
                    students.size(), students.size(), successCount, failedCount, "转换结果完成"));
        }

        log.info("模拟退火分配完成，成功：{}，失败：{}", successCount, failedCount);
        return results;
    }
}
