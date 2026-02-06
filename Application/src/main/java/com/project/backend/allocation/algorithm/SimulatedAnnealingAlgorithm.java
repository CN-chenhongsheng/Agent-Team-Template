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
 * 模拟退火分配算法
 * 策略：通过模拟物理退火过程寻找全局最优分配方案
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
    private static final double COOLING_RATE = 0.995;
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
        if (studentCount <= 500) {
            return "约30-60秒";
        } else if (studentCount <= 2000) {
            return "约2-5分钟";
        } else {
            return "约5-10分钟";
        }
    }

    @Override
    public boolean isRecommended() {
        return false; // 不是默认推荐，因为速度较慢
    }

    @Override
    public List<AllocationResultDTO> allocate(
            List<Student> students,
            Map<Long, List<Bed>> roomBedMap,
            Map<Long, List<Student>> roomStudentMap,
            AllocationConfig config,
            Consumer<AllocationProgress> progressCallback) {

        log.info("开始模拟退火分配，学生数：{}，可用房间数：{}", students.size(), roomBedMap.size());

        int totalStudents = students.size();

        // 1. 收集所有可用床位
        List<Bed> allBeds = roomBedMap.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        if (allBeds.size() < students.size()) {
            log.warn("床位数量不足：需要{}，可用{}", students.size(), allBeds.size());
        }

        if (progressCallback != null) {
            progressCallback.accept(new AllocationProgress(
                    totalStudents, 0, 0, 0, "正在生成初始解"
            ));
        }

        // 2. 生成初始解（贪心分配）
        Map<Long, Long> assignment = generateInitialSolution(students, allBeds, roomStudentMap, config);

        // 3. 计算初始解的总得分
        double currentScore = calculateTotalScore(assignment, students, allBeds, roomStudentMap, config);
        double bestScore = currentScore;
        Map<Long, Long> bestAssignment = new HashMap<>(assignment);

        if (progressCallback != null) {
            progressCallback.accept(new AllocationProgress(
                    totalStudents, 0, 0, 0,
                    String.format("初始解得分: %.2f，开始优化", currentScore)
            ));
        }

        // 4. 模拟退火主循环
        double temperature = INITIAL_TEMPERATURE;
        int iteration = 0;
        Random random = new Random();

        while (temperature > MIN_TEMPERATURE) {
            for (int i = 0; i < ITERATIONS_PER_TEMP; i++) {
                iteration++;

                // 生成邻域解（交换两个学生的床位）
                Map<Long, Long> newAssignment = generateNeighbor(assignment, students, allBeds, random);

                // 计算新解的得分
                double newScore = calculateTotalScore(newAssignment, students, allBeds, roomStudentMap, config);

                // 计算接受概率
                double delta = newScore - currentScore;
                double acceptProbability = delta > 0 ? 1.0 : Math.exp(delta / temperature);

                // 决定是否接受新解
                if (random.nextDouble() < acceptProbability) {
                    assignment = newAssignment;
                    currentScore = newScore;

                    // 更新最优解
                    if (currentScore > bestScore) {
                        bestScore = currentScore;
                        bestAssignment = new HashMap<>(assignment);
                    }
                }
            }

            // 降温
            temperature *= COOLING_RATE;

            // 报告进度
            if (progressCallback != null && iteration % 500 == 0) {
                double progress = (INITIAL_TEMPERATURE - temperature) / (INITIAL_TEMPERATURE - MIN_TEMPERATURE);
                progressCallback.accept(new AllocationProgress(
                        totalStudents,
                        (int) (totalStudents * progress),
                        0, 0,
                        String.format("优化中... 温度: %.1f, 当前得分: %.2f, 最优得分: %.2f",
                                temperature, currentScore, bestScore)
                ));
            }
        }

        if (progressCallback != null) {
            progressCallback.accept(new AllocationProgress(
                    totalStudents, totalStudents, 0, 0,
                    String.format("优化完成，最终得分: %.2f", bestScore)
            ));
        }

        // 5. 将最优解转换为结果
        return convertToResults(bestAssignment, students, allBeds, roomStudentMap, config, progressCallback);
    }

    /**
     * 生成初始解（使用贪心策略）
     */
    private Map<Long, Long> generateInitialSolution(
            List<Student> students,
            List<Bed> allBeds,
            Map<Long, List<Student>> roomStudentMap,
            AllocationConfig config) {

        Map<Long, Long> assignment = new HashMap<>(); // studentId -> bedId
        Set<Long> usedBedIds = new HashSet<>();
        Map<Long, List<Student>> tempRoomStudentMap = new HashMap<>();

        // 复制现有室友信息
        for (Map.Entry<Long, List<Student>> entry : roomStudentMap.entrySet()) {
            tempRoomStudentMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        for (Student student : students) {
            Bed bestBed = null;
            BigDecimal bestScore = BigDecimal.valueOf(-1);

            for (Bed bed : allBeds) {
                if (usedBedIds.contains(bed.getId())) continue;

                List<Student> roommates = tempRoomStudentMap.getOrDefault(bed.getRoomId(), new ArrayList<>());
                RoomMatchResult matchResult = compatibilityService.calculateRoomCompatibility(
                        student, roommates, config);

                if (!Boolean.TRUE.equals(matchResult.getHasHardConflict())) {
                    if (matchResult.getAvgScore().compareTo(bestScore) > 0) {
                        bestScore = matchResult.getAvgScore();
                        bestBed = bed;
                    }
                }
            }

            if (bestBed != null) {
                assignment.put(student.getId(), bestBed.getId());
                usedBedIds.add(bestBed.getId());
                tempRoomStudentMap.computeIfAbsent(bestBed.getRoomId(), k -> new ArrayList<>()).add(student);
            }
        }

        return assignment;
    }

    /**
     * 生成邻域解（交换两个学生的床位）
     */
    private Map<Long, Long> generateNeighbor(
            Map<Long, Long> currentAssignment,
            List<Student> students,
            List<Bed> allBeds,
            Random random) {

        Map<Long, Long> newAssignment = new HashMap<>(currentAssignment);

        // 随机选择两个已分配的学生
        List<Long> assignedStudentIds = new ArrayList<>(currentAssignment.keySet());
        if (assignedStudentIds.size() < 2) {
            return newAssignment;
        }

        int idx1 = random.nextInt(assignedStudentIds.size());
        int idx2 = random.nextInt(assignedStudentIds.size());
        while (idx2 == idx1) {
            idx2 = random.nextInt(assignedStudentIds.size());
        }

        Long studentId1 = assignedStudentIds.get(idx1);
        Long studentId2 = assignedStudentIds.get(idx2);

        // 交换床位
        Long bedId1 = newAssignment.get(studentId1);
        Long bedId2 = newAssignment.get(studentId2);
        newAssignment.put(studentId1, bedId2);
        newAssignment.put(studentId2, bedId1);

        return newAssignment;
    }

    /**
     * 计算分配方案的总得分
     */
    private double calculateTotalScore(
            Map<Long, Long> assignment,
            List<Student> students,
            List<Bed> allBeds,
            Map<Long, List<Student>> originalRoomStudentMap,
            AllocationConfig config) {

        // 构建床位ID到床位的映射
        Map<Long, Bed> bedMap = allBeds.stream()
                .collect(Collectors.toMap(Bed::getId, b -> b));

        // 构建学生ID到学生的映射
        Map<Long, Student> studentMap = students.stream()
                .collect(Collectors.toMap(Student::getId, s -> s));

        // 构建当前房间的学生列表
        Map<Long, List<Student>> roomStudentMap = new HashMap<>();
        for (Map.Entry<Long, List<Student>> entry : originalRoomStudentMap.entrySet()) {
            roomStudentMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        for (Map.Entry<Long, Long> entry : assignment.entrySet()) {
            Long studentId = entry.getKey();
            Long bedId = entry.getValue();
            Bed bed = bedMap.get(bedId);
            if (bed != null) {
                Student student = studentMap.get(studentId);
                if (student != null) {
                    roomStudentMap.computeIfAbsent(bed.getRoomId(), k -> new ArrayList<>()).add(student);
                }
            }
        }

        // 计算每个学生的匹配得分
        double totalScore = 0;
        int count = 0;
        int hardConflicts = 0;

        for (Map.Entry<Long, Long> entry : assignment.entrySet()) {
            Long studentId = entry.getKey();
            Long bedId = entry.getValue();

            Student student = studentMap.get(studentId);
            Bed bed = bedMap.get(bedId);

            if (student == null || bed == null) continue;

            // 获取该房间的其他学生（排除自己）
            List<Student> roommates = roomStudentMap.getOrDefault(bed.getRoomId(), new ArrayList<>())
                    .stream()
                    .filter(s -> !s.getId().equals(studentId))
                    .collect(Collectors.toList());

            RoomMatchResult matchResult = compatibilityService.calculateRoomCompatibility(
                    student, roommates, config);

            if (Boolean.TRUE.equals(matchResult.getHasHardConflict())) {
                hardConflicts++;
                totalScore -= 100; // 硬约束冲突大幅扣分
            } else {
                totalScore += matchResult.getAvgScore().doubleValue();
            }
            count++;
        }

        // 惩罚硬约束冲突
        totalScore -= hardConflicts * 50;

        return count > 0 ? totalScore / count : 0;
    }

    /**
     * 将分配方案转换为结果列表
     */
    private List<AllocationResultDTO> convertToResults(
            Map<Long, Long> assignment,
            List<Student> students,
            List<Bed> allBeds,
            Map<Long, List<Student>> originalRoomStudentMap,
            AllocationConfig config,
            Consumer<AllocationProgress> progressCallback) {

        List<AllocationResultDTO> results = new ArrayList<>();

        // 构建映射
        Map<Long, Bed> bedMap = allBeds.stream()
                .collect(Collectors.toMap(Bed::getId, b -> b));
        Map<Long, Student> studentMap = students.stream()
                .collect(Collectors.toMap(Student::getId, s -> s));

        // 构建房间学生映射
        Map<Long, List<Student>> roomStudentMap = new HashMap<>();
        for (Map.Entry<Long, List<Student>> entry : originalRoomStudentMap.entrySet()) {
            roomStudentMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        for (Map.Entry<Long, Long> entry : assignment.entrySet()) {
            Long studentId = entry.getKey();
            Long bedId = entry.getValue();
            Bed bed = bedMap.get(bedId);
            if (bed != null) {
                Student student = studentMap.get(studentId);
                if (student != null) {
                    roomStudentMap.computeIfAbsent(bed.getRoomId(), k -> new ArrayList<>()).add(student);
                }
            }
        }

        int totalStudents = students.size();
        int successCount = 0;
        int failedCount = 0;

        for (Student student : students) {
            Long bedId = assignment.get(student.getId());

            if (bedId != null) {
                Bed bed = bedMap.get(bedId);
                if (bed != null) {
                    // 获取室友
                    List<Student> roommates = roomStudentMap.getOrDefault(bed.getRoomId(), new ArrayList<>())
                            .stream()
                            .filter(s -> !s.getId().equals(student.getId()))
                            .collect(Collectors.toList());

                    RoomMatchResult matchResult = compatibilityService.calculateRoomCompatibility(
                            student, roommates, config);

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
                            .success(!Boolean.TRUE.equals(matchResult.getHasHardConflict()))
                            .build();

                    if (result.isSuccess()) {
                        successCount++;
                    } else {
                        failedCount++;
                        result.setFailReason(matchResult.getHardConflictReason());
                    }

                    results.add(result);
                    continue;
                }
            }

            // 未分配成功
            AllocationResultDTO result = AllocationResultDTO.builder()
                    .studentId(student.getId())
                    .studentNo(student.getStudentNo())
                    .studentName(student.getStudentName())
                    .success(false)
                    .failReason("无可用床位或所有床位存在硬约束冲突")
                    .build();
            results.add(result);
            failedCount++;
        }

        if (progressCallback != null) {
            progressCallback.accept(new AllocationProgress(
                    totalStudents, totalStudents, successCount, failedCount, "转换结果完成"
            ));
        }

        log.info("模拟退火分配完成，成功：{}，失败：{}", successCount, failedCount);
        return results;
    }
}
