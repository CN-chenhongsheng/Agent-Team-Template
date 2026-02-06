package com.project.backend.allocation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.project.backend.student.entity.Student;
import com.project.backend.student.mapper.StudentMapper;
import com.project.backend.allocation.algorithm.model.RoomMatchResult;
import com.project.backend.allocation.entity.AllocationConfig;
import com.project.backend.allocation.service.AllocationConfigService;
import com.project.backend.allocation.service.BedRecommendService;
import com.project.backend.allocation.service.CompatibilityService;
import com.project.backend.allocation.vo.BedRecommendVO;
import com.project.backend.room.entity.Bed;
import com.project.backend.room.entity.Room;
import com.project.backend.room.mapper.BedMapper;
import com.project.backend.room.mapper.RoomMapper;
import com.project.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 床位推荐服务实现
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BedRecommendServiceImpl implements BedRecommendService {

    private final StudentMapper studentMapper;
    private final BedMapper bedMapper;
    private final RoomMapper roomMapper;
    private final CompatibilityService compatibilityService;
    private final AllocationConfigService configService;

    @Override
    @Transactional(readOnly = true)
    public List<BedRecommendVO> recommendBeds(Long studentId, int limit) {
        Student student = getStudentOrThrow(studentId);
        AllocationConfig config = getDefaultConfig();

        // 查询空床位（不排除任何房间）
        List<Bed> availableBeds = queryAvailableBeds(student.getCampusCode(), null);
        if (availableBeds.isEmpty()) {
            return Collections.emptyList();
        }

        // 处理房间推荐（无最低分数要求）
        List<RoomMatchResult> results = processRoomRecommendations(
                student, availableBeds, config, null);

        return convertAndSort(results, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BedRecommendVO> recommendTransfer(Long studentId, int limit) {
        Student student = getStudentOrThrow(studentId);

        if (student.getBedId() == null) {
            throw new BusinessException("学生当前没有床位，请使用普通推荐");
        }

        AllocationConfig config = getDefaultConfig();

        // 计算当前房间的匹配度作为基准
        List<Student> currentRoommates = getRoommates(student.getRoomId(), studentId);
        RoomMatchResult currentMatch = compatibilityService.calculateRoomCompatibility(
                student, currentRoommates, config);
        BigDecimal currentScore = currentMatch.getAvgScore();

        // 查询其他房间的空床位（排除当前房间）
        List<Bed> availableBeds = queryAvailableBeds(student.getCampusCode(), student.getRoomId());
        if (availableBeds.isEmpty()) {
            return Collections.emptyList();
        }

        // 处理房间推荐（只推荐比当前房间匹配度更高的）
        List<RoomMatchResult> results = processRoomRecommendations(
                student, availableBeds, config, currentScore);

        return convertAndSort(results, limit);
    }

    // ==================== 抽离的公共方法 ====================

    /**
     * 获取学生信息，不存在则抛出异常
     */
    private Student getStudentOrThrow(Long studentId) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }
        return student;
    }

    /**
     * 查询可用床位
     *
     * @param campusCode 校区编码（可选）
     * @param excludeRoomId 排除的房间ID（可选，用于调宿推荐）
     * @return 可用床位列表
     */
    private List<Bed> queryAvailableBeds(String campusCode, Long excludeRoomId) {
        LambdaQueryWrapper<Bed> bedWrapper = new LambdaQueryWrapper<>();
        bedWrapper.eq(Bed::getBedStatus, 1)
                .eq(Bed::getStatus, 1)
                .eq(Bed::getDeleted, 0);

        if (campusCode != null) {
            bedWrapper.eq(Bed::getCampusCode, campusCode);
        }
        if (excludeRoomId != null) {
            bedWrapper.ne(Bed::getRoomId, excludeRoomId);
        }

        return bedMapper.selectList(bedWrapper);
    }

    /**
     * 处理房间推荐核心逻辑
     *
     * @param student 学生
     * @param availableBeds 可用床位列表
     * @param config 配置
     * @param minScore 最低匹配分数要求（用于调宿推荐，null表示无要求）
     * @return 匹配结果列表
     */
    private List<RoomMatchResult> processRoomRecommendations(
            Student student, List<Bed> availableBeds, AllocationConfig config, BigDecimal minScore) {

        // 按房间分组
        Map<Long, List<Bed>> roomBedMap = availableBeds.stream()
                .collect(Collectors.groupingBy(Bed::getRoomId));

        List<RoomMatchResult> results = new ArrayList<>();

        for (Map.Entry<Long, List<Bed>> entry : roomBedMap.entrySet()) {
            Long roomId = entry.getKey();
            List<Bed> beds = entry.getValue();

            if (beds.isEmpty()) {
                continue;
            }

            Bed bed = beds.get(0);
            List<Student> roommates = getRoommates(roomId, student.getId());

            // 检查性别约束
            if (!checkGenderConstraint(student, roommates, config)) {
                continue;
            }

            // 计算匹配度
            RoomMatchResult matchResult = compatibilityService.calculateRoomCompatibility(
                    student, roommates, config);
            matchResult.setRoomId(roomId);
            matchResult.setBedId(bed.getId());

            // 跳过有硬约束冲突的
            if (Boolean.TRUE.equals(matchResult.getHasHardConflict())) {
                continue;
            }

            // 如果有最低分数要求，只保留超过的结果
            if (minScore != null && matchResult.getAvgScore().compareTo(minScore) <= 0) {
                continue;
            }

            results.add(matchResult);
        }

        return results;
    }

    /**
     * 检查性别约束
     *
     * @param student 学生
     * @param roommates 室友列表
     * @param config 配置
     * @return 是否满足性别约束
     */
    private boolean checkGenderConstraint(Student student, List<Student> roommates, AllocationConfig config) {
        if (!Integer.valueOf(1).equals(config.getGenderConstraint()) || roommates.isEmpty()) {
            return true;
        }
        return roommates.stream()
                .allMatch(r -> Objects.equals(r.getGender(), student.getGender()));
    }

    /**
     * 排序并转换为VO
     */
    private List<BedRecommendVO> convertAndSort(List<RoomMatchResult> results, int limit) {
        return results.stream()
                .sorted((a, b) -> b.getAvgScore().compareTo(a.getAvgScore()))
                .limit(limit)
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    // ==================== 私有方法 ====================

    /**
     * 将 RoomMatchResult 转换为 BedRecommendVO
     */
    private BedRecommendVO convertToVO(RoomMatchResult result) {
        BedRecommendVO vo = new BedRecommendVO();
        vo.setRoomId(result.getRoomId());
        vo.setBedId(result.getBedId());
        vo.setMatchScore(result.getAvgScore());
        vo.setMinMatchScore(result.getMinScore());
        vo.setMaxMatchScore(result.getMaxScore());
        vo.setRoommateCount(result.getRoommateCount());
        vo.setAdvantages(result.getOverallAdvantages());
        vo.setConflicts(result.getOverallConflicts());

        // 获取床位和房间信息
        if (result.getBedId() != null) {
            Bed bed = bedMapper.selectById(result.getBedId());
            if (bed != null) {
                vo.setBedCode(bed.getBedCode());
            }
        }
        if (result.getRoomId() != null) {
            Room room = roomMapper.selectById(result.getRoomId());
            if (room != null) {
                vo.setRoomCode(room.getRoomCode());
                vo.setRoomNumber(room.getRoomNumber());
                vo.setFloorCode(room.getFloorCode());
            }
        }

        return vo;
    }

    /**
     * 获取房间内的室友
     */
    private List<Student> getRoommates(Long roomId, Long excludeStudentId) {
        if (roomId == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getRoomId, roomId)
                .ne(Student::getId, excludeStudentId)
                .eq(Student::getDeleted, 0);
        return studentMapper.selectList(wrapper);
    }

    /**
     * 获取默认配置
     */
    private AllocationConfig getDefaultConfig() {
        // 查找启用的默认配置
        LambdaQueryWrapper<AllocationConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AllocationConfig::getStatus, 1)
                .orderByAsc(AllocationConfig::getId)
                .last("LIMIT 1");
        AllocationConfig config = configService.getOne(wrapper);

        if (config == null) {
            // 返回一个默认配置
            config = new AllocationConfig();
            config.setSmokingConstraint(1);
            config.setGenderConstraint(1);
            config.setSleepHardConstraint(0);
            config.setSleepWeight(30);
            config.setSmokingWeight(20);
            config.setCleanlinessWeight(15);
            config.setSocialWeight(15);
            config.setStudyWeight(10);
            config.setEntertainmentWeight(10);
            config.setSameDeptBonus(5);
            config.setSameMajorBonus(10);
            config.setSameClassBonus(15);
        }

        return config;
    }
}
