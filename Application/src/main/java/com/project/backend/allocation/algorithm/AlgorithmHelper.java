package com.project.backend.allocation.algorithm;

import com.project.backend.allocation.algorithm.model.AllocationResultDTO;
import com.project.backend.allocation.algorithm.model.RoomMatchResult;
import com.project.backend.allocation.entity.AllocationConfig;
import com.project.backend.room.entity.Bed;
import com.project.backend.student.entity.Student;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 分配算法公共工具类
 * 提供三个算法共用的 DTO 构建、性别分桶、数据分片等功能
 *
 * @author 陈鸿昇
 * @since 2026-02-08
 */
@Slf4j
public final class AlgorithmHelper {

    private AlgorithmHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    // ==================== DTO Builder ====================

    /**
     * 构建分配成功的结果 DTO
     */
    public static AllocationResultDTO buildSuccess(Student student, Bed bed, RoomMatchResult matchResult) {
        return AllocationResultDTO.builder()
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
    }

    /**
     * 构建分配失败的结果 DTO
     */
    public static AllocationResultDTO buildFail(Student student, String reason) {
        return AllocationResultDTO.builder()
                .studentId(student.getId())
                .studentNo(student.getStudentNo())
                .studentName(student.getStudentName())
                .success(false)
                .failReason(reason)
                .build();
    }

    // ==================== Gender Pre-Bucketing ====================

    /**
     * 按性别将房间分桶
     * 规则：已有学生的房间按学生性别分桶，空房间放入"通用"桶
     *
     * @return gender -> roomId Set 的映射（null key 表示通用/空房间）
     */
    public static Map<Integer, Set<Long>> bucketRoomsByGender(
            Map<Long, List<Bed>> roomBedMap,
            Map<Long, List<Student>> roomStudentMap) {

        Map<Integer, Set<Long>> buckets = new HashMap<>();
        // null key = 通用（空房间）
        buckets.put(null, new HashSet<>());
        buckets.put(1, new HashSet<>());   // 男
        buckets.put(2, new HashSet<>());   // 女

        for (Long roomId : roomBedMap.keySet()) {
            List<Student> existing = roomStudentMap.getOrDefault(roomId, List.of());
            if (existing.isEmpty()) {
                // 空房间，任意性别可分配
                buckets.get(null).add(roomId);
            } else {
                // 按第一个学生的性别分桶
                Integer gender = existing.get(0).getGender();
                buckets.computeIfAbsent(gender, k -> new HashSet<>()).add(roomId);
            }
        }

        return buckets;
    }

    /**
     * 获取学生可用的房间 ID 集合（同性别 + 空房间）
     */
    public static Set<Long> getCandidateRoomIds(Integer studentGender, Map<Integer, Set<Long>> genderBuckets) {
        Set<Long> candidates = new HashSet<>();
        // 同性别房间
        Set<Long> sameGender = genderBuckets.get(studentGender);
        if (sameGender != null) {
            candidates.addAll(sameGender);
        }
        // 空房间（通用）
        Set<Long> universal = genderBuckets.get(null);
        if (universal != null) {
            candidates.addAll(universal);
        }
        return candidates;
    }

    /**
     * 当学生被分配到空房间后，将该房间从通用桶移到对应性别桶
     */
    public static void updateGenderBucket(Long roomId, Integer studentGender, Map<Integer, Set<Long>> genderBuckets) {
        Set<Long> universal = genderBuckets.get(null);
        if (universal != null && universal.remove(roomId)) {
            genderBuckets.computeIfAbsent(studentGender, k -> new HashSet<>()).add(roomId);
        }
    }

    // ==================== Partition (Tier 2/3) ====================

    /**
     * 数据分片：按性别 + 校区将学生和房间切分成独立的分片
     * 每个分片可以独立并行分配
     */
    public static List<Partition> partitionStudentsAndRooms(
            List<Student> students,
            Map<Long, List<Bed>> roomBedMap,
            Map<Long, List<Student>> roomStudentMap) {

        // 按性别分组学生
        Map<Integer, List<Student>> studentsByGender = students.stream()
                .collect(Collectors.groupingBy(s -> s.getGender() != null ? s.getGender() : 0));

        // 按性别分组房间（通过现有学生的性别判断，空房间按需分配）
        Map<Integer, Map<Long, List<Bed>>> roomsByGender = new HashMap<>();
        Map<Long, List<Bed>> emptyRooms = new HashMap<>();

        for (Map.Entry<Long, List<Bed>> entry : roomBedMap.entrySet()) {
            Long roomId = entry.getKey();
            List<Student> existing = roomStudentMap.getOrDefault(roomId, List.of());
            if (existing.isEmpty()) {
                emptyRooms.put(roomId, entry.getValue());
            } else {
                Integer gender = existing.get(0).getGender();
                roomsByGender.computeIfAbsent(gender != null ? gender : 0, k -> new HashMap<>())
                        .put(roomId, entry.getValue());
            }
        }

        // 按比例将空房间分配给各性别
        distributeEmptyRooms(emptyRooms, roomsByGender, studentsByGender);

        // 构建分片
        List<Partition> partitions = new ArrayList<>();
        for (Map.Entry<Integer, List<Student>> entry : studentsByGender.entrySet()) {
            Integer gender = entry.getKey();
            List<Student> genderStudents = entry.getValue();
            Map<Long, List<Bed>> genderRooms = roomsByGender.getOrDefault(gender, Map.of());

            if (!genderStudents.isEmpty() && !genderRooms.isEmpty()) {
                // 复制室友信息（只包含该分片的房间）
                Map<Long, List<Student>> partitionRoomStudents = new HashMap<>();
                for (Long roomId : genderRooms.keySet()) {
                    List<Student> existing = roomStudentMap.get(roomId);
                    if (existing != null && !existing.isEmpty()) {
                        partitionRoomStudents.put(roomId, new ArrayList<>(existing));
                    }
                }

                partitions.add(new Partition(gender, genderStudents, genderRooms, partitionRoomStudents));
            }
        }

        log.info("数据分片完成，共 {} 个分片: {}", partitions.size(),
                partitions.stream().map(p -> String.format("gender=%d, students=%d, rooms=%d",
                        p.gender, p.students.size(), p.roomBedMap.size()))
                        .collect(Collectors.joining("; ")));

        return partitions;
    }

    /**
     * 按学生数量比例分配空房间
     */
    private static void distributeEmptyRooms(
            Map<Long, List<Bed>> emptyRooms,
            Map<Integer, Map<Long, List<Bed>>> roomsByGender,
            Map<Integer, List<Student>> studentsByGender) {

        if (emptyRooms.isEmpty()) return;

        int totalStudents = studentsByGender.values().stream().mapToInt(List::size).sum();
        if (totalStudents == 0) return;

        List<Map.Entry<Long, List<Bed>>> emptyRoomList = new ArrayList<>(emptyRooms.entrySet());
        int assigned = 0;

        for (Map.Entry<Integer, List<Student>> entry : studentsByGender.entrySet()) {
            Integer gender = entry.getKey();
            int genderCount = entry.getValue().size();
            int roomsForGender = (int) Math.round((double) genderCount / totalStudents * emptyRoomList.size());

            Map<Long, List<Bed>> genderRoomMap = roomsByGender.computeIfAbsent(gender, k -> new HashMap<>());
            int end = Math.min(assigned + roomsForGender, emptyRoomList.size());
            for (int i = assigned; i < end; i++) {
                Map.Entry<Long, List<Bed>> room = emptyRoomList.get(i);
                genderRoomMap.put(room.getKey(), room.getValue());
            }
            assigned = end;
        }

        // 剩余的分给第一个有学生的性别
        if (assigned < emptyRoomList.size() && !studentsByGender.isEmpty()) {
            Integer firstGender = studentsByGender.keySet().iterator().next();
            Map<Long, List<Bed>> genderRoomMap = roomsByGender.computeIfAbsent(firstGender, k -> new HashMap<>());
            for (int i = assigned; i < emptyRoomList.size(); i++) {
                Map.Entry<Long, List<Bed>> room = emptyRoomList.get(i);
                genderRoomMap.put(room.getKey(), room.getValue());
            }
        }
    }

    // ==================== Index Builders ====================

    /**
     * 构建 bedId -> Bed 的查找 Map
     */
    public static Map<Long, Bed> buildBedMap(List<Bed> allBeds) {
        return allBeds.stream().collect(Collectors.toMap(Bed::getId, b -> b, (a, b) -> a));
    }

    /**
     * 构建 studentId -> Student 的查找 Map
     */
    public static Map<Long, Student> buildStudentMap(List<Student> students) {
        return students.stream().collect(Collectors.toMap(Student::getId, s -> s, (a, b) -> a));
    }

    /**
     * 构建 bedId -> roomId 的查找 Map
     */
    public static Map<Long, Long> buildBedToRoomMap(List<Bed> allBeds) {
        return allBeds.stream().collect(Collectors.toMap(Bed::getId, Bed::getRoomId, (a, b) -> a));
    }

    // ==================== Tier Threshold ====================

    /** Tier 1 上限：直接优化模式 */
    public static final int TIER1_MAX = 50_000;
    /** Tier 2 上限：分片并行模式 */
    public static final int TIER2_MAX = 200_000;
    /** 超过 TIER2_MAX 则使用 Tier 3：向量索引批量模式 */

    /**
     * 判断应使用的层级
     */
    public static int determineTier(int studentCount) {
        if (studentCount <= TIER1_MAX) return 1;
        if (studentCount <= TIER2_MAX) return 2;
        return 3;
    }

    // ==================== Partition Data Class ====================

    /**
     * 数据分片，持有一个独立的学生+房间子集
     */
    public static class Partition {
        public final int gender;
        public final List<Student> students;
        public final Map<Long, List<Bed>> roomBedMap;
        public final Map<Long, List<Student>> roomStudentMap;

        public Partition(int gender, List<Student> students,
                         Map<Long, List<Bed>> roomBedMap,
                         Map<Long, List<Student>> roomStudentMap) {
            this.gender = gender;
            this.students = students;
            this.roomBedMap = roomBedMap;
            this.roomStudentMap = roomStudentMap;
        }
    }
}
