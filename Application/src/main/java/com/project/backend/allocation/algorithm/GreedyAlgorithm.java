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
 * 贪心分配算法
 * 策略：依次为每个学生找当前最优的床位
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GreedyAlgorithm implements AllocationAlgorithm {

    private final CompatibilityService compatibilityService;

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
        if (studentCount <= 1000) {
            return "约2-5秒";
        } else if (studentCount <= 5000) {
            return "约10-20秒";
        } else {
            return "约30-60秒";
        }
    }

    @Override
    public List<AllocationResultDTO> allocate(
            List<Student> students,
            Map<Long, List<Bed>> roomBedMap,
            Map<Long, List<Student>> roomStudentMap,
            AllocationConfig config,
            Consumer<AllocationProgress> progressCallback) {

        log.info("开始贪心分配，学生数：{}，可用房间数：{}", students.size(), roomBedMap.size());

        List<AllocationResultDTO> results = new ArrayList<>();
        int totalStudents = students.size();
        int processedCount = 0;
        int successCount = 0;
        int failedCount = 0;

        // 复制床位映射，避免修改原始数据
        Map<Long, List<Bed>> availableBedMap = new HashMap<>();
        for (Map.Entry<Long, List<Bed>> entry : roomBedMap.entrySet()) {
            availableBedMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        // 复制室友映射
        Map<Long, List<Student>> currentRoomStudentMap = new HashMap<>();
        for (Map.Entry<Long, List<Student>> entry : roomStudentMap.entrySet()) {
            currentRoomStudentMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        // 按习惯特征明显程度排序学生（可选优化）
        List<Student> sortedStudents = sortStudentsByDistinctiveness(students);

        // 遍历每个学生
        for (Student student : sortedStudents) {
            processedCount++;

            // 报告进度
            if (progressCallback != null && processedCount % 100 == 0) {
                progressCallback.accept(new AllocationProgress(
                        totalStudents, processedCount, successCount, failedCount,
                        "正在分配第 " + processedCount + " 个学生"
                ));
            }

            // 为该学生找最佳床位
            BedMatch bestMatch = findBestBed(student, availableBedMap, currentRoomStudentMap, config);

            if (bestMatch != null && bestMatch.bed != null) {
                // 分配成功
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
                        .roommateIds(matchResult.getRoommateMatches().stream()
                                .map(m -> m.getStudentBId())
                                .collect(Collectors.toList()))
                        .success(true)
                        .build();

                results.add(result);
                successCount++;

                // 更新状态：移除已分配床位，添加学生到房间
                availableBedMap.get(bed.getRoomId()).remove(bed);
                if (availableBedMap.get(bed.getRoomId()).isEmpty()) {
                    availableBedMap.remove(bed.getRoomId());
                }
                currentRoomStudentMap.computeIfAbsent(bed.getRoomId(), k -> new ArrayList<>()).add(student);

            } else {
                // 分配失败
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

        // 最终进度
        if (progressCallback != null) {
            progressCallback.accept(new AllocationProgress(
                    totalStudents, processedCount, successCount, failedCount,
                    "分配完成"
            ));
        }

        log.info("贪心分配完成，成功：{}，失败：{}", successCount, failedCount);
        return results;
    }

    /**
     * 为学生找最佳床位
     */
    private BedMatch findBestBed(
            Student student,
            Map<Long, List<Bed>> availableBedMap,
            Map<Long, List<Student>> roomStudentMap,
            AllocationConfig config) {

        BedMatch bestMatch = null;
        BigDecimal bestScore = BigDecimal.valueOf(-1);

        for (Map.Entry<Long, List<Bed>> entry : availableBedMap.entrySet()) {
            Long roomId = entry.getKey();
            List<Bed> beds = entry.getValue();

            if (beds.isEmpty()) continue;

            // 获取房间现有室友
            List<Student> roommates = roomStudentMap.getOrDefault(roomId, new ArrayList<>());

            // 计算与该房间的匹配度
            RoomMatchResult matchResult = compatibilityService.calculateRoomCompatibility(
                    student, roommates, config);

            // 跳过有硬约束冲突的房间
            if (Boolean.TRUE.equals(matchResult.getHasHardConflict())) {
                continue;
            }

            // 选择匹配分最高的房间
            if (matchResult.getAvgScore().compareTo(bestScore) > 0) {
                bestScore = matchResult.getAvgScore();
                Bed bestBed = beds.get(0); // 取该房间的第一个可用床位
                bestMatch = new BedMatch(bestBed, matchResult, null);
            }
        }

        if (bestMatch == null) {
            return new BedMatch(null, null, "没有符合条件的床位（可能存在硬约束冲突）");
        }

        return bestMatch;
    }

    /**
     * 按习惯特征明显程度排序（可选优化）
     * 特征越明显的学生越难匹配，优先处理
     */
    private List<Student> sortStudentsByDistinctiveness(List<Student> students) {
        // 简单实现：吸烟者、夜猫子、对声音敏感的优先
        return students.stream()
                .sorted((a, b) -> {
                    int scoreA = getDistinctivenessScore(a);
                    int scoreB = getDistinctivenessScore(b);
                    return Integer.compare(scoreB, scoreA); // 降序
                })
                .collect(Collectors.toList());
    }

    private int getDistinctivenessScore(Student s) {
        int score = 0;
        if (Integer.valueOf(1).equals(s.getSmokingStatus())) score += 10;
        if (Integer.valueOf(3).equals(s.getSleepSchedule())) score += 8; // 夜猫子
        if (Integer.valueOf(0).equals(s.getSleepSchedule())) score += 8; // 早睡早起
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
