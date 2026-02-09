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
 * K-Means聚类分配算法（优化版）
 * 策略：先将学生按生活习惯聚类，再将同类学生分配到同一房间
 * 优化：KMeans++ 初始化、Set 替代 List.contains、Tier3 整簇批量分配、分片并行
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KMeansAlgorithm implements AllocationAlgorithm {

    private final CompatibilityService compatibilityService;

    private static final int MAX_ITERATIONS = 100;
    private static final BigDecimal EARLY_STOP_SCORE = BigDecimal.valueOf(90);

    @Override
    public String getAlgorithmType() {
        return "kmeans";
    }

    @Override
    public String getAlgorithmName() {
        return "聚类分配算法";
    }

    @Override
    public String getDescription() {
        return "先将学生按生活习惯聚类，再将同类学生分配到同一房间";
    }

    @Override
    public String getAdvantages() {
        return "均衡考虑所有学生特征，结果较为合理，推荐使用";
    }

    @Override
    public String getDisadvantages() {
        return "速度中等，聚类效果受参数影响";
    }

    @Override
    public String getEstimatedTime(int studentCount) {
        if (studentCount <= 1000) return "约3-8秒";
        if (studentCount <= 10000) return "约10-20秒";
        if (studentCount <= 50000) return "约20-40秒";
        if (studentCount <= 200000) return "约30-60秒";
        return "约1-2分钟";
    }

    @Override
    public boolean isRecommended() {
        return true;
    }

    @Override
    public List<AllocationResultDTO> allocate(
            List<Student> students,
            Map<Long, List<Bed>> roomBedMap,
            Map<Long, List<Student>> roomStudentMap,
            AllocationConfig config,
            Consumer<AllocationProgress> progressCallback) {

        int tier = AlgorithmHelper.determineTier(students.size());
        log.info("开始K-Means聚类分配，学生数：{}，可用房间数：{}，使用 Tier {}", students.size(), roomBedMap.size(), tier);

        if (tier == 3) {
            return allocateClusterBatch(students, roomBedMap, roomStudentMap, config, progressCallback);
        }
        if (tier == 2) {
            return allocatePartitioned(students, roomBedMap, roomStudentMap, config, progressCallback);
        }

        return allocateDirect(students, roomBedMap, roomStudentMap, config, progressCallback);
    }

    // ==================== Tier 2: 分片并行 ====================

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
            if (progressCallback != null) {
                progressCallback.accept(new AllocationProgress(
                        students.size(), students.size(), success, allResults.size() - success, "分片并行执行完成"));
            }
            return allResults;
        } catch (Exception e) {
            log.error("分片并行执行失败，回退到单线程", e);
            return allocateDirect(students, roomBedMap, roomStudentMap, config, progressCallback);
        } finally {
            pool.shutdown();
        }
    }

    // ==================== Tier 3: 先分片再整簇批量分配 ====================

    /**
     * Tier 3：先按性别分片保证硬约束，再对每个分片聚类 + 批量分配。
     * 同簇学生习惯相似，只需抽样检查匹配度而非逐对全量计算。
     */
    private List<AllocationResultDTO> allocateClusterBatch(
            List<Student> students,
            Map<Long, List<Bed>> roomBedMap,
            Map<Long, List<Student>> roomStudentMap,
            AllocationConfig config,
            Consumer<AllocationProgress> progressCallback) {

        int totalStudents = students.size();

        if (progressCallback != null) {
            progressCallback.accept(new AllocationProgress(totalStudents, 0, 0, 0, "Tier3: 按性别分片"));
        }

        // 1. 按性别分片（保证性别硬约束）
        List<AlgorithmHelper.Partition> partitions = AlgorithmHelper.partitionStudentsAndRooms(
                students, roomBedMap, roomStudentMap);

        log.info("Tier3 分片完成，共 {} 个分片", partitions.size());

        // 2. 每个分片独立聚类 + 批量分配
        List<AllocationResultDTO> allResults = new ArrayList<>();
        int processedTotal = 0;
        int successTotal = 0, failedTotal = 0;

        for (AlgorithmHelper.Partition partition : partitions) {
            if (partition.students.isEmpty()) continue;

            if (progressCallback != null) {
                progressCallback.accept(new AllocationProgress(
                        totalStudents, processedTotal, successTotal, failedTotal,
                        String.format("Tier3: 正在聚类分片（%d 学生）", partition.students.size())));
            }

            // 2a. 向量化 + 聚类
            List<double[]> vectors = partition.students.stream().map(this::studentToVector).collect(Collectors.toList());
            int avgBedsPerRoom = 4;
            int k = Math.max(1, partition.students.size() / avgBedsPerRoom);
            int[] clusterAssignments = kMeansClustering(vectors, k);

            // 2b. 按簇分组
            Map<Integer, List<Student>> clusters = new HashMap<>();
            for (int i = 0; i < partition.students.size(); i++) {
                clusters.computeIfAbsent(clusterAssignments[i], c -> new ArrayList<>()).add(partition.students.get(i));
            }

            // 2c. 复制可用床位
            Map<Long, List<Bed>> availableBeds = new HashMap<>();
            for (Map.Entry<Long, List<Bed>> entry : partition.roomBedMap.entrySet()) {
                availableBeds.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }

            // 维护房间当前学生（用于匹配计算）
            Map<Long, List<Student>> currentRoomStudents = new HashMap<>();
            for (Map.Entry<Long, List<Student>> entry : partition.roomStudentMap.entrySet()) {
                currentRoomStudents.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }

            // 2d. 空房间集合
            Set<Long> emptyRoomIds = new LinkedHashSet<>();
            for (Long roomId : availableBeds.keySet()) {
                List<Student> existing = currentRoomStudents.get(roomId);
                if (existing == null || existing.isEmpty()) {
                    emptyRoomIds.add(roomId);
                }
            }

            // 2e. 按簇分配：同簇学生尽量填入同一组连续房间
            for (Map.Entry<Integer, List<Student>> clusterEntry : clusters.entrySet()) {
                List<Student> clusterStudents = clusterEntry.getValue();

                for (Student student : clusterStudents) {
                    processedTotal++;
                    boolean assigned = false;

                    // 优先分配到空房间（无需匹配计算）
                    Iterator<Long> emptyIter = emptyRoomIds.iterator();
                    while (emptyIter.hasNext()) {
                        Long emptyRoomId = emptyIter.next();
                        List<Bed> beds = availableBeds.get(emptyRoomId);
                        if (beds != null && !beds.isEmpty()) {
                            Bed bed = beds.remove(beds.size() - 1);
                            RoomMatchResult mr = compatibilityService.calculateRoomCompatibility(
                                    student, List.of(), config);
                            allResults.add(AlgorithmHelper.buildSuccess(student, bed, mr));
                            successTotal++;
                            assigned = true;

                            currentRoomStudents.computeIfAbsent(emptyRoomId, x -> new ArrayList<>()).add(student);
                            if (beds.isEmpty()) {
                                availableBeds.remove(emptyRoomId);
                                emptyIter.remove();
                            } else {
                                emptyIter.remove(); // 不再是空房间
                            }
                            break;
                        } else {
                            emptyIter.remove();
                        }
                    }

                    // 空房间用完了，找有人的房间（跳过硬约束冲突）
                    if (!assigned) {
                        for (Iterator<Map.Entry<Long, List<Bed>>> it = availableBeds.entrySet().iterator(); it.hasNext(); ) {
                            Map.Entry<Long, List<Bed>> roomEntry = it.next();
                            Long roomId = roomEntry.getKey();
                            List<Bed> beds = roomEntry.getValue();
                            if (beds.isEmpty()) { it.remove(); continue; }

                            List<Student> roommates = currentRoomStudents.getOrDefault(roomId, List.of());

                            // 硬约束快速检查（只检查第一个室友，同簇学生间通常不冲突）
                            if (!roommates.isEmpty()) {
                                boolean hasConflict = compatibilityService.hasHardConflict(student, roommates.get(0), config);
                                if (hasConflict) continue;
                            }

                            Bed bed = beds.remove(beds.size() - 1);
                            RoomMatchResult mr = compatibilityService.calculateRoomCompatibility(student, roommates, config);

                            if (Boolean.TRUE.equals(mr.getHasHardConflict())) {
                                // 匹配后发现冲突，放回床位，跳过
                                beds.add(bed);
                                continue;
                            }

                            allResults.add(AlgorithmHelper.buildSuccess(student, bed, mr));
                            successTotal++;
                            assigned = true;
                            currentRoomStudents.computeIfAbsent(roomId, x -> new ArrayList<>()).add(student);
                            if (beds.isEmpty()) it.remove();
                            break;
                        }
                    }

                    if (!assigned) {
                        allResults.add(AlgorithmHelper.buildFail(student, "无可用床位或所有床位存在硬约束冲突"));
                        failedTotal++;
                    }
                }

                // 每处理完一个簇报告进度
                if (progressCallback != null && processedTotal % 500 == 0) {
                    progressCallback.accept(new AllocationProgress(
                            totalStudents, processedTotal, successTotal, failedTotal,
                            String.format("Tier3 批量分配中... %d/%d", processedTotal, totalStudents)));
                }
            }
        }

        if (progressCallback != null) {
            progressCallback.accept(new AllocationProgress(
                    totalStudents, totalStudents, successTotal, failedTotal, "Tier3 批量分配完成"));
        }

        log.info("K-Means Tier3 批量分配完成，成功：{}，失败：{}", successTotal, failedTotal);
        return allResults;
    }

    // ==================== Tier 1: 直接分配 ====================

    private List<AllocationResultDTO> allocateDirect(
            List<Student> students,
            Map<Long, List<Bed>> roomBedMap,
            Map<Long, List<Student>> roomStudentMap,
            AllocationConfig config,
            Consumer<AllocationProgress> progressCallback) {

        List<AllocationResultDTO> results = new ArrayList<>();
        int totalStudents = students.size();

        // 1. K
        int totalBeds = roomBedMap.values().stream().mapToInt(List::size).sum();
        int avgBedsPerRoom = 4;
        int k = Math.max(1, Math.min(totalBeds / avgBedsPerRoom, students.size() / avgBedsPerRoom));

        if (progressCallback != null) {
            progressCallback.accept(new AllocationProgress(totalStudents, 0, 0, 0, "正在进行聚类分析，K=" + k));
        }

        // 2. 向量化 + KMeans++ 聚类
        List<double[]> vectors = students.stream().map(this::studentToVector).collect(Collectors.toList());
        int[] clusterAssignments = kMeansClustering(vectors, k);

        if (progressCallback != null) {
            progressCallback.accept(new AllocationProgress(totalStudents, 0, 0, 0, "聚类完成，正在分配床位"));
        }

        // 3. 按聚类分组
        Map<Integer, List<Student>> clusters = new HashMap<>();
        for (int i = 0; i < students.size(); i++) {
            clusters.computeIfAbsent(clusterAssignments[i], c -> new ArrayList<>()).add(students.get(i));
        }

        // 4. 预构建 Set 用于 O(1) 聚类成员判断
        Map<Integer, Set<Long>> clusterStudentIdSets = new HashMap<>();
        for (Map.Entry<Integer, List<Student>> entry : clusters.entrySet()) {
            clusterStudentIdSets.put(entry.getKey(),
                    entry.getValue().stream().map(Student::getId).collect(Collectors.toSet()));
        }

        // 5. 复制可用数据
        Map<Long, List<Bed>> availableBedMap = new HashMap<>();
        for (Map.Entry<Long, List<Bed>> entry : roomBedMap.entrySet()) {
            availableBedMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        Map<Long, List<Student>> currentRoomStudentMap = new HashMap<>();
        for (Map.Entry<Long, List<Student>> entry : roomStudentMap.entrySet()) {
            currentRoomStudentMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        // 6. 性别分桶
        Map<Integer, Set<Long>> genderBuckets = AlgorithmHelper.bucketRoomsByGender(availableBedMap, currentRoomStudentMap);
        Set<Long> emptyRoomIds = new HashSet<>();
        for (Long roomId : availableBedMap.keySet()) {
            if (!currentRoomStudentMap.containsKey(roomId) || currentRoomStudentMap.get(roomId).isEmpty()) {
                emptyRoomIds.add(roomId);
            }
        }

        // 7. 按聚类分配
        int processedCount = 0, successCount = 0, failedCount = 0;

        for (Map.Entry<Integer, List<Student>> clusterEntry : clusters.entrySet()) {
            Integer clusterId = clusterEntry.getKey();
            List<Student> clusterStudents = clusterEntry.getValue();
            Set<Long> clusterIds = clusterStudentIdSets.get(clusterId);

            for (Student student : clusterStudents) {
                processedCount++;

                if (progressCallback != null && processedCount % 100 == 0) {
                    progressCallback.accept(new AllocationProgress(
                            totalStudents, processedCount, successCount, failedCount,
                            "正在分配第 " + processedCount + " 个学生"));
                }

                Set<Long> candidateRoomIds = AlgorithmHelper.getCandidateRoomIds(student.getGender(), genderBuckets);

                BedMatch bestMatch = findBestBedForCluster(
                        student, clusterIds, candidateRoomIds, emptyRoomIds,
                        availableBedMap, currentRoomStudentMap, config);

                if (bestMatch != null && bestMatch.bed != null) {
                    Bed bed = bestMatch.bed;
                    Long roomId = bed.getRoomId();
                    boolean wasEmpty = emptyRoomIds.contains(roomId);

                    results.add(AlgorithmHelper.buildSuccess(student, bed, bestMatch.matchResult));
                    successCount++;

                    availableBedMap.get(roomId).remove(bed);
                    if (availableBedMap.get(roomId).isEmpty()) {
                        availableBedMap.remove(roomId);
                        genderBuckets.values().forEach(set -> set.remove(roomId));
                    }
                    currentRoomStudentMap.computeIfAbsent(roomId, r -> new ArrayList<>()).add(student);

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
        }

        if (progressCallback != null) {
            progressCallback.accept(new AllocationProgress(
                    totalStudents, processedCount, successCount, failedCount, "分配完成"));
        }

        log.info("K-Means聚类分配完成，成功：{}，失败：{}", successCount, failedCount);
        return results;
    }

    // ==================== K-Means++ ====================

    /**
     * 将学生生活习惯转换为向量
     */
    private double[] studentToVector(Student s) {
        return new double[]{
                normalize(s.getSmokingStatus(), 0, 1),
                normalize(s.getSmokingTolerance(), 0, 1),
                normalize(s.getSleepSchedule(), 0, 3),
                normalize(s.getSleepQuality(), 0, 2),
                normalize(s.getSnores(), 0, 1),
                normalize(s.getSensitiveToLight(), 0, 1),
                normalize(s.getSensitiveToSound(), 0, 1),
                normalize(s.getCleanlinessLevel(), 1, 5),
                normalize(s.getBedtimeCleanup(), 0, 3),
                normalize(s.getSocialPreference(), 0, 2),
                normalize(s.getAllowVisitors(), 0, 2),
                normalize(s.getPhoneCallTime(), 0, 2),
                normalize(s.getStudyInRoom(), 0, 3),
                normalize(s.getStudyEnvironment(), 0, 3),
                normalize(s.getComputerUsageTime(), 0, 3),
                normalize(s.getGamingPreference(), 0, 2),
                normalize(s.getMusicPreference(), 0, 2),
                normalize(s.getMusicVolume(), 0, 2),
                normalize(s.getEatInRoom(), 0, 2)
        };
    }

    private double normalize(Integer value, int min, int max) {
        if (value == null) return 0.5;
        return max == min ? 0.5 : (double) (value - min) / (max - min);
    }

    /**
     * K-Means 聚类（KMeans++ 初始化）
     */
    private int[] kMeansClustering(List<double[]> vectors, int k) {
        int n = vectors.size();
        if (n == 0) return new int[0];
        int dim = vectors.get(0).length;
        k = Math.min(k, n); // k 不能超过样本数

        int[] assignments = new int[n];
        Random random = new Random(42);

        // === K-Means++ 初始化 ===
        double[][] centroids = new double[k][dim];
        // 第一个中心随机选
        centroids[0] = Arrays.copyOf(vectors.get(random.nextInt(n)), dim);

        double[] minDistances = new double[n];
        Arrays.fill(minDistances, Double.MAX_VALUE);

        for (int c = 1; c < k; c++) {
            // 更新到最近中心的距离
            double totalDist = 0;
            for (int i = 0; i < n; i++) {
                double dist = squaredEuclideanDistance(vectors.get(i), centroids[c - 1]);
                if (dist < minDistances[i]) {
                    minDistances[i] = dist;
                }
                totalDist += minDistances[i];
            }

            // 按距离的平方加权随机选下一个中心
            double threshold = random.nextDouble() * totalDist;
            double cumulative = 0;
            int selected = 0;
            for (int i = 0; i < n; i++) {
                cumulative += minDistances[i];
                if (cumulative >= threshold) {
                    selected = i;
                    break;
                }
            }
            centroids[c] = Arrays.copyOf(vectors.get(selected), dim);
        }

        // === 迭代 ===
        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
            boolean changed = false;

            // 分配
            for (int i = 0; i < n; i++) {
                int nearest = 0;
                double minDist = Double.MAX_VALUE;
                for (int c = 0; c < k; c++) {
                    double dist = squaredEuclideanDistance(vectors.get(i), centroids[c]);
                    if (dist < minDist) {
                        minDist = dist;
                        nearest = c;
                    }
                }
                if (assignments[i] != nearest) {
                    assignments[i] = nearest;
                    changed = true;
                }
            }

            if (!changed) break;

            // 更新中心
            int[] counts = new int[k];
            double[][] sums = new double[k][dim];
            for (int i = 0; i < n; i++) {
                int c = assignments[i];
                counts[c]++;
                for (int d = 0; d < dim; d++) {
                    sums[c][d] += vectors.get(i)[d];
                }
            }
            for (int c = 0; c < k; c++) {
                if (counts[c] > 0) {
                    for (int d = 0; d < dim; d++) {
                        centroids[c][d] = sums[c][d] / counts[c];
                    }
                }
            }
        }

        return assignments;
    }

    private double squaredEuclideanDistance(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b[i];
            sum += diff * diff;
        }
        return sum;
    }

    // ==================== 床位搜索 ====================

    /**
     * 为聚类中的学生找最佳床位（性别分桶 + 同聚类加分 + 早停）
     */
    private BedMatch findBestBedForCluster(
            Student student,
            Set<Long> clusterStudentIds,
            Set<Long> candidateRoomIds,
            Set<Long> emptyRoomIds,
            Map<Long, List<Bed>> availableBedMap,
            Map<Long, List<Student>> roomStudentMap,
            AllocationConfig config) {

        BedMatch bestMatch = null;
        BigDecimal bestScore = BigDecimal.valueOf(-1);

        // 空房间快速路径
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

        for (Long roomId : candidateRoomIds) {
            List<Bed> beds = availableBedMap.get(roomId);
            if (beds == null || beds.isEmpty()) continue;

            List<Student> roommates = roomStudentMap.getOrDefault(roomId, List.of());
            RoomMatchResult matchResult = compatibilityService.calculateRoomCompatibility(
                    student, roommates, config);

            if (Boolean.TRUE.equals(matchResult.getHasHardConflict())) continue;

            // 综合得分 = 匹配分 + 同聚类室友加分（O(1) Set.contains）
            BigDecimal score = matchResult.getAvgScore();
            long sameClusterCount = roommates.stream()
                    .filter(s -> clusterStudentIds.contains(s.getId()))
                    .count();
            score = score.add(BigDecimal.valueOf(sameClusterCount * 5));

            if (score.compareTo(bestScore) > 0) {
                bestScore = score;
                bestMatch = new BedMatch(beds.get(0), matchResult, null);

                if (bestScore.compareTo(EARLY_STOP_SCORE) >= 0) break;
            }
        }

        if (bestMatch == null) {
            return new BedMatch(null, null, "没有符合条件的床位");
        }
        return bestMatch;
    }

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
