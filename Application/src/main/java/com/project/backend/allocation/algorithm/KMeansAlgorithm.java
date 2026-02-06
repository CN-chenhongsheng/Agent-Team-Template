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
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * K-Means聚类分配算法
 * 策略：先将学生按生活习惯聚类，再将同类学生分配到同一房间
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
        if (studentCount <= 1000) {
            return "约5-15秒";
        } else if (studentCount <= 5000) {
            return "约30-60秒";
        } else {
            return "约1-2分钟";
        }
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

        log.info("开始K-Means聚类分配，学生数：{}，可用房间数：{}", students.size(), roomBedMap.size());

        List<AllocationResultDTO> results = new ArrayList<>();
        int totalStudents = students.size();

        // 1. 计算聚类数量K（大约等于房间数或学生数/4）
        int totalBeds = roomBedMap.values().stream().mapToInt(List::size).sum();
        int avgBedsPerRoom = 4; // 假设平均每房间4床
        int k = Math.max(1, Math.min(totalBeds / avgBedsPerRoom, students.size() / avgBedsPerRoom));

        if (progressCallback != null) {
            progressCallback.accept(new AllocationProgress(
                    totalStudents, 0, 0, 0, "正在进行聚类分析，K=" + k
            ));
        }

        // 2. 将学生向量化
        List<double[]> vectors = students.stream()
                .map(this::studentToVector)
                .collect(Collectors.toList());

        // 3. 执行K-Means聚类
        int[] clusterAssignments = kMeansClustering(vectors, k);

        if (progressCallback != null) {
            progressCallback.accept(new AllocationProgress(
                    totalStudents, 0, 0, 0, "聚类完成，正在分配床位"
            ));
        }

        // 4. 按聚类分组学生
        Map<Integer, List<Student>> clusters = new HashMap<>();
        for (int i = 0; i < students.size(); i++) {
            int cluster = clusterAssignments[i];
            clusters.computeIfAbsent(cluster, c -> new ArrayList<>()).add(students.get(i));
        }

        // 5. 复制可用床位和室友信息
        Map<Long, List<Bed>> availableBedMap = new HashMap<>();
        for (Map.Entry<Long, List<Bed>> entry : roomBedMap.entrySet()) {
            availableBedMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        Map<Long, List<Student>> currentRoomStudentMap = new HashMap<>();
        for (Map.Entry<Long, List<Student>> entry : roomStudentMap.entrySet()) {
            currentRoomStudentMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        // 6. 按聚类分配
        int processedCount = 0;
        int successCount = 0;
        int failedCount = 0;

        for (Map.Entry<Integer, List<Student>> clusterEntry : clusters.entrySet()) {
            List<Student> clusterStudents = clusterEntry.getValue();

            // 为该聚类的学生分配房间
            for (Student student : clusterStudents) {
                processedCount++;

                // 报告进度
                if (progressCallback != null && processedCount % 50 == 0) {
                    progressCallback.accept(new AllocationProgress(
                            totalStudents, processedCount, successCount, failedCount,
                            "正在分配第 " + processedCount + " 个学生"
                    ));
                }

                // 找最佳床位（优先找同聚类学生多的房间）
                BedMatch bestMatch = findBestBedForCluster(
                        student, clusterStudents, availableBedMap, currentRoomStudentMap, config);

                if (bestMatch != null && bestMatch.bed != null) {
                    Bed bed = bestMatch.bed;
                    RoomMatchResult matchResult = bestMatch.matchResult;

                    AllocationResultDTO result = AllocationResultDTO.builder()
                            .studentId(student.getId())
                            .studentNo(student.getStudentNo())
                            .studentName(student.getStudentName())
                            .gender(student.getGender() != null ? (student.getGender() == 1 ? "male" : "female") : null)
                            .deptCode(student.getDeptCode())
                            .majorCode(student.getMajorCode())
                            .classCode(student.getClassCode())
                            .bedId(bed.getId())
                            .roomId(bed.getRoomId())
                            .roomCode(bed.getRoomCode())
                            .floorId(bed.getFloorId())
                            .floorCode(bed.getFloorCode())
                            .matchScore(matchResult.getAvgScore())
                            .conflictReasons(matchResult.getOverallConflicts())
                            .advantages(matchResult.getOverallAdvantages())
                            .success(true)
                            .build();

                    results.add(result);
                    successCount++;

                    // 更新状态
                    availableBedMap.get(bed.getRoomId()).remove(bed);
                    if (availableBedMap.get(bed.getRoomId()).isEmpty()) {
                        availableBedMap.remove(bed.getRoomId());
                    }
                    currentRoomStudentMap.computeIfAbsent(bed.getRoomId(), r -> new ArrayList<>()).add(student);

                } else {
                    AllocationResultDTO result = AllocationResultDTO.builder()
                            .studentId(student.getId())
                            .studentNo(student.getStudentNo())
                            .studentName(student.getStudentName())
                            .success(false)
                            .failReason(bestMatch != null ? bestMatch.failReason : "无可用床位")
                            .build();

                    results.add(result);
                    failedCount++;
                }
            }
        }

        if (progressCallback != null) {
            progressCallback.accept(new AllocationProgress(
                    totalStudents, processedCount, successCount, failedCount, "分配完成"
            ));
        }

        log.info("K-Means聚类分配完成，成功：{}，失败：{}", successCount, failedCount);
        return results;
    }

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
        if (value == null) return 0.5; // 默认中间值
        return (double) (value - min) / (max - min);
    }

    /**
     * K-Means聚类算法实现
     */
    private int[] kMeansClustering(List<double[]> vectors, int k) {
        int n = vectors.size();
        int dim = vectors.get(0).length;
        int[] assignments = new int[n];
        Random random = new Random(42);

        // 随机初始化中心点
        double[][] centroids = new double[k][dim];
        Set<Integer> usedIndices = new HashSet<>();
        for (int i = 0; i < k; i++) {
            int idx;
            do {
                idx = random.nextInt(n);
            } while (usedIndices.contains(idx));
            usedIndices.add(idx);
            centroids[i] = Arrays.copyOf(vectors.get(idx), dim);
        }

        // 迭代
        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
            boolean changed = false;

            // 分配每个点到最近的中心
            for (int i = 0; i < n; i++) {
                int nearestCluster = 0;
                double minDist = Double.MAX_VALUE;
                for (int c = 0; c < k; c++) {
                    double dist = euclideanDistance(vectors.get(i), centroids[c]);
                    if (dist < minDist) {
                        minDist = dist;
                        nearestCluster = c;
                    }
                }
                if (assignments[i] != nearestCluster) {
                    assignments[i] = nearestCluster;
                    changed = true;
                }
            }

            if (!changed) break;

            // 更新中心点
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

    private double euclideanDistance(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    /**
     * 为聚类中的学生找最佳床位
     */
    private BedMatch findBestBedForCluster(
            Student student,
            List<Student> clusterStudents,
            Map<Long, List<Bed>> availableBedMap,
            Map<Long, List<Student>> roomStudentMap,
            AllocationConfig config) {

        BedMatch bestMatch = null;
        BigDecimal bestScore = BigDecimal.valueOf(-1);

        for (Map.Entry<Long, List<Bed>> entry : availableBedMap.entrySet()) {
            Long roomId = entry.getKey();
            List<Bed> beds = entry.getValue();

            if (beds.isEmpty()) continue;

            List<Student> roommates = roomStudentMap.getOrDefault(roomId, new ArrayList<>());

            RoomMatchResult matchResult = compatibilityService.calculateRoomCompatibility(
                    student, roommates, config);

            if (Boolean.TRUE.equals(matchResult.getHasHardConflict())) {
                continue;
            }

            // 计算综合得分（匹配分 + 同聚类加分）
            BigDecimal score = matchResult.getAvgScore();
            long sameClusterCount = roommates.stream()
                    .filter(clusterStudents::contains)
                    .count();
            score = score.add(BigDecimal.valueOf(sameClusterCount * 5)); // 每个同聚类室友加5分

            if (score.compareTo(bestScore) > 0) {
                bestScore = score;
                bestMatch = new BedMatch(beds.get(0), matchResult, null);
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
