package com.project.backend.allocation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.project.backend.student.entity.Student;
import com.project.backend.student.mapper.StudentMapper;
import com.project.backend.allocation.algorithm.model.DimensionScore;
import com.project.backend.allocation.algorithm.model.MatchResult;
import com.project.backend.allocation.algorithm.model.RoomMatchResult;
import com.project.backend.allocation.entity.AllocationConfig;
import com.project.backend.allocation.service.CompatibilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * 匹配度计算服务实现
 * 实现学生之间基于生活习惯的匹配度计算
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompatibilityServiceImpl implements CompatibilityService {

    private final StudentMapper studentMapper;

    @Override
    @Transactional(readOnly = true)
    public MatchResult calculateCompatibility(Student studentA, Student studentB, AllocationConfig config) {
        MatchResult result = MatchResult.builder()
                .studentAId(studentA.getId())
                .studentBId(studentB.getId())
                .hasHardConflict(false)
                .build();

        // 1. 首先检查硬约束
        String hardConflict = checkHardConstraints(studentA, studentB, config);
        if (hardConflict != null) {
            result.setHasHardConflict(true);
            result.setHardConflictReason(hardConflict);
            result.setTotalScore(BigDecimal.ZERO);
            return result;
        }

        // 2. 计算各维度得分
        // 睡眠维度
        DimensionScore sleepScore = calculateSleepDimension(studentA, studentB);
        sleepScore.setWeight(config.getSleepWeight());
        sleepScore.calculateWeightedScore();
        result.addDimensionScore(sleepScore);

        // 吸烟维度
        DimensionScore smokingScore = calculateSmokingDimension(studentA, studentB);
        smokingScore.setWeight(config.getSmokingWeight());
        smokingScore.calculateWeightedScore();
        result.addDimensionScore(smokingScore);

        // 整洁维度
        DimensionScore cleanlinessScore = calculateCleanlinessDimension(studentA, studentB);
        cleanlinessScore.setWeight(config.getCleanlinessWeight());
        cleanlinessScore.calculateWeightedScore();
        result.addDimensionScore(cleanlinessScore);

        // 社交维度
        DimensionScore socialScore = calculateSocialDimension(studentA, studentB);
        socialScore.setWeight(config.getSocialWeight());
        socialScore.calculateWeightedScore();
        result.addDimensionScore(socialScore);

        // 学习维度
        DimensionScore studyScore = calculateStudyDimension(studentA, studentB);
        studyScore.setWeight(config.getStudyWeight());
        studyScore.calculateWeightedScore();
        result.addDimensionScore(studyScore);

        // 娱乐维度
        DimensionScore entertainmentScore = calculateEntertainmentDimension(studentA, studentB);
        entertainmentScore.setWeight(config.getEntertainmentWeight());
        entertainmentScore.calculateWeightedScore();
        result.addDimensionScore(entertainmentScore);

        // 3. 计算总分
        result.calculateTotalScore();

        // 4. 额外加分项（同院系/专业/班级）
        BigDecimal bonus = calculateBonus(studentA, studentB, config);
        if (bonus.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal newTotal = result.getTotalScore().add(bonus);
            // 总分不超过100
            if (newTotal.compareTo(BigDecimal.valueOf(100)) > 0) {
                newTotal = BigDecimal.valueOf(100);
            }
            result.setTotalScore(newTotal);
        }

        // 5. 汇总冲突和优势
        result.aggregateConflictsAndAdvantages();

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasHardConflict(Student studentA, Student studentB, AllocationConfig config) {
        return checkHardConstraints(studentA, studentB, config) != null;
    }

    @Override
    @Transactional(readOnly = true)
    public RoomMatchResult calculateRoomCompatibility(Student student, List<Student> roommates, AllocationConfig config) {
        RoomMatchResult result = RoomMatchResult.builder()
                .studentId(student.getId())
                .build();

        if (roommates == null || roommates.isEmpty()) {
            // 空房间，默认满分
            result.setAvgScore(BigDecimal.valueOf(100));
            result.setMinScore(BigDecimal.valueOf(100));
            result.setMaxScore(BigDecimal.valueOf(100));
            result.setRoommateCount(0);
            result.setHasHardConflict(false);
            result.getOverallAdvantages().add("空房间，可自由入住");
            return result;
        }

        // 计算与每个室友的匹配度
        for (Student roommate : roommates) {
            MatchResult matchResult = calculateCompatibility(student, roommate, config);
            result.addRoommateMatch(matchResult);
        }

        // 计算综合结果
        result.calculateFromRoommateMatches();

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public RoomMatchResult calculateRoomCompatibilityByRoomId(Student student, Long roomId, Long bedId, AllocationConfig config) {
        // 查询房间现有室友（排除待分配学生自己）
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getRoomId, roomId)
                .ne(Student::getId, student.getId())
                .eq(Student::getDeleted, 0);
        List<Student> roommates = studentMapper.selectList(wrapper);

        RoomMatchResult result = calculateRoomCompatibility(student, roommates, config);
        result.setRoomId(roomId);
        result.setBedId(bedId);

        return result;
    }

    // ==================== 硬约束检查 ====================

    /**
     * 检查硬约束冲突
     *
     * @return 冲突原因，如果无冲突返回null
     */
    private String checkHardConstraints(Student a, Student b, AllocationConfig config) {
        // 1. 性别约束
        if (Integer.valueOf(1).equals(config.getGenderConstraint())) {
            if (!Objects.equals(a.getGender(), b.getGender())) {
                return "性别不同，不能同住";
            }
        }

        // 2. 吸烟约束
        if (Integer.valueOf(1).equals(config.getSmokingConstraint())) {
            // A吸烟，B不接受
            if (Integer.valueOf(1).equals(a.getSmokingStatus())
                    && Integer.valueOf(0).equals(b.getSmokingTolerance())) {
                return "吸烟者与不接受吸烟者不能同住";
            }
            // B吸烟，A不接受
            if (Integer.valueOf(1).equals(b.getSmokingStatus())
                    && Integer.valueOf(0).equals(a.getSmokingTolerance())) {
                return "吸烟者与不接受吸烟者不能同住";
            }
        }

        // 3. 作息硬约束（差异≥3档）
        if (Integer.valueOf(1).equals(config.getSleepHardConstraint())) {
            int sleepA = a.getSleepSchedule() != null ? a.getSleepSchedule() : 1;
            int sleepB = b.getSleepSchedule() != null ? b.getSleepSchedule() : 1;
            if (Math.abs(sleepA - sleepB) >= 3) {
                return "作息时间差异过大（早睡早起vs夜猫子）";
            }
        }

        return null;
    }

    // ==================== 各维度计算 ====================

    /**
     * 睡眠维度计算
     */
    private DimensionScore calculateSleepDimension(Student a, Student b) {
        DimensionScore score = DimensionScore.builder()
                .dimension("sleep")
                .dimensionName("睡眠习惯")
                .score(100)
                .build();

        int points = 100;

        // 1. 作息时间匹配（0-3档）
        int scheduleA = getIntValue(a.getSleepSchedule(), 1);
        int scheduleB = getIntValue(b.getSleepSchedule(), 1);
        int scheduleDiff = Math.abs(scheduleA - scheduleB);

        if (scheduleDiff == 0) {
            score.addAdvantage("作息时间完全一致");
        } else if (scheduleDiff == 1) {
            points -= 10;
            score.addConflict("作息时间略有差异");
        } else if (scheduleDiff == 2) {
            points -= 25;
            score.addConflict("作息时间差异较大");
        } else {
            points -= 40;
            score.addConflict("作息时间差异很大");
        }

        // 2. 打呼噜 vs 声音敏感
        if (Integer.valueOf(1).equals(a.getSnores()) && Integer.valueOf(1).equals(b.getSensitiveToSound())) {
            points -= 25;
            score.addConflict("你打呼噜，室友对声音敏感");
        }
        if (Integer.valueOf(1).equals(b.getSnores()) && Integer.valueOf(1).equals(a.getSensitiveToSound())) {
            points -= 25;
            score.addConflict("室友打呼噜，你对声音敏感");
        }
        if (Integer.valueOf(0).equals(a.getSnores()) && Integer.valueOf(0).equals(b.getSnores())) {
            score.addAdvantage("都不打呼噜");
        }

        // 3. 睡眠质量 vs 光线敏感
        if (Integer.valueOf(0).equals(a.getSleepQuality()) && Integer.valueOf(1).equals(b.getSensitiveToLight())) {
            points -= 10;
        }
        if (Integer.valueOf(0).equals(b.getSleepQuality()) && Integer.valueOf(1).equals(a.getSensitiveToLight())) {
            points -= 10;
        }

        score.setScore(Math.max(0, points));
        return score;
    }

    /**
     * 吸烟维度计算
     */
    private DimensionScore calculateSmokingDimension(Student a, Student b) {
        DimensionScore score = DimensionScore.builder()
                .dimension("smoking")
                .dimensionName("吸烟习惯")
                .score(100)
                .build();

        int points = 100;

        boolean aSmokes = Integer.valueOf(1).equals(a.getSmokingStatus());
        boolean bSmokes = Integer.valueOf(1).equals(b.getSmokingStatus());
        boolean aTolerant = Integer.valueOf(1).equals(a.getSmokingTolerance());
        boolean bTolerant = Integer.valueOf(1).equals(b.getSmokingTolerance());

        if (!aSmokes && !bSmokes) {
            score.addAdvantage("都不吸烟");
        } else if (aSmokes && bSmokes) {
            score.addAdvantage("都吸烟，习惯相似");
        } else {
            if (aSmokes && !bTolerant) {
                points -= 40;
                score.addConflict("你吸烟，室友不接受吸烟");
            } else if (aSmokes && bTolerant) {
                points -= 10;
                score.addConflict("你吸烟，但室友可以接受");
            }
            if (bSmokes && !aTolerant) {
                points -= 40;
                score.addConflict("室友吸烟，你不接受吸烟");
            } else if (bSmokes && aTolerant) {
                points -= 10;
                score.addConflict("室友吸烟，但你可以接受");
            }
        }

        score.setScore(Math.max(0, points));
        return score;
    }

    /**
     * 整洁维度计算
     */
    private DimensionScore calculateCleanlinessDimension(Student a, Student b) {
        DimensionScore score = DimensionScore.builder()
                .dimension("cleanliness")
                .dimensionName("整洁程度")
                .score(100)
                .build();

        int points = 100;

        int cleanA = getIntValue(a.getCleanlinessLevel(), 3);
        int cleanB = getIntValue(b.getCleanlinessLevel(), 3);
        int cleanDiff = Math.abs(cleanA - cleanB);

        if (cleanDiff == 0) {
            score.addAdvantage("整洁程度完全一致");
        } else if (cleanDiff == 1) {
            points -= 10;
        } else if (cleanDiff == 2) {
            points -= 20;
            score.addConflict("整洁程度有差异");
        } else {
            points -= 35;
            score.addConflict("整洁程度差异较大");
        }

        int cleanupA = getIntValue(a.getBedtimeCleanup(), 1);
        int cleanupB = getIntValue(b.getBedtimeCleanup(), 1);
        int cleanupDiff = Math.abs(cleanupA - cleanupB);

        if (cleanupDiff >= 2) {
            points -= 10;
        }

        score.setScore(Math.max(0, points));
        return score;
    }

    /**
     * 社交维度计算
     */
    private DimensionScore calculateSocialDimension(Student a, Student b) {
        DimensionScore score = DimensionScore.builder()
                .dimension("social")
                .dimensionName("社交偏好")
                .score(100)
                .build();

        int points = 100;

        int socialA = getIntValue(a.getSocialPreference(), 1);
        int socialB = getIntValue(b.getSocialPreference(), 1);
        int socialDiff = Math.abs(socialA - socialB);

        if (socialDiff == 0) {
            if (socialA == 0) {
                score.addAdvantage("都喜欢安静的环境");
            } else if (socialA == 2) {
                score.addAdvantage("都喜欢热闹的氛围");
            }
        } else if (socialDiff == 2) {
            points -= 25;
            score.addConflict("社交偏好差异较大（安静vs热闹）");
        } else {
            points -= 10;
        }

        int visitorA = getIntValue(a.getAllowVisitors(), 1);
        int visitorB = getIntValue(b.getAllowVisitors(), 1);
        if (visitorA == 0 && visitorB == 2) {
            points -= 15;
            score.addConflict("你不允许访客，室友经常带访客");
        }
        if (visitorB == 0 && visitorA == 2) {
            points -= 15;
            score.addConflict("室友不允许访客，你经常带访客");
        }

        int phoneA = getIntValue(a.getPhoneCallTime(), 1);
        int phoneB = getIntValue(b.getPhoneCallTime(), 1);
        if (phoneA == 0 && phoneB == 2) {
            points -= 10;
        }
        if (phoneB == 0 && phoneA == 2) {
            points -= 10;
        }

        score.setScore(Math.max(0, points));
        return score;
    }

    /**
     * 学习维度计算
     */
    private DimensionScore calculateStudyDimension(Student a, Student b) {
        DimensionScore score = DimensionScore.builder()
                .dimension("study")
                .dimensionName("学习习惯")
                .score(100)
                .build();

        int points = 100;

        int studyA = getIntValue(a.getStudyInRoom(), 1);
        int studyB = getIntValue(b.getStudyInRoom(), 1);
        int envA = getIntValue(a.getStudyEnvironment(), 1);
        int envB = getIntValue(b.getStudyEnvironment(), 1);

        if ((studyA >= 2 && envA == 1) && envB == 3) {
            points -= 20;
            score.addConflict("你需要安静学习，室友学习时接受声音");
        }
        if ((studyB >= 2 && envB == 1) && envA == 3) {
            points -= 20;
            score.addConflict("室友需要安静学习，你学习时接受声音");
        }

        if (Math.abs(studyA - studyB) <= 1 && Math.abs(envA - envB) <= 1) {
            score.addAdvantage("学习习惯相近");
        }

        score.setScore(Math.max(0, points));
        return score;
    }

    /**
     * 娱乐维度计算
     */
    private DimensionScore calculateEntertainmentDimension(Student a, Student b) {
        DimensionScore score = DimensionScore.builder()
                .dimension("entertainment")
                .dimensionName("娱乐习惯")
                .score(100)
                .build();

        int points = 100;

        int computerA = getIntValue(a.getComputerUsageTime(), 2);
        int computerB = getIntValue(b.getComputerUsageTime(), 2);
        if (Math.abs(computerA - computerB) >= 2) {
            points -= 10;
        }

        int gameA = getIntValue(a.getGamingPreference(), 1);
        int gameB = getIntValue(b.getGamingPreference(), 1);
        if (gameA == 2 && gameB == 0) {
            points -= 15;
            score.addConflict("你经常玩游戏，室友不玩游戏");
        }
        if (gameB == 2 && gameA == 0) {
            points -= 15;
            score.addConflict("室友经常玩游戏，你不玩游戏");
        }

        int musicA = getIntValue(a.getMusicPreference(), 1);
        int musicB = getIntValue(b.getMusicPreference(), 1);
        int volumeA = getIntValue(a.getMusicVolume(), 1);
        int volumeB = getIntValue(b.getMusicVolume(), 1);

        if (musicA == 2 && volumeA == 2 && musicB == 0) {
            points -= 15;
            score.addConflict("你经常大声听音乐");
        }
        if (musicB == 2 && volumeB == 2 && musicA == 0) {
            points -= 15;
            score.addConflict("室友经常大声听音乐");
        }

        int eatA = getIntValue(a.getEatInRoom(), 1);
        int eatB = getIntValue(b.getEatInRoom(), 1);
        if (Math.abs(eatA - eatB) >= 2) {
            points -= 5;
        }

        score.setScore(Math.max(0, points));
        return score;
    }

    // ==================== 加分项计算 ====================

    private BigDecimal calculateBonus(Student a, Student b, AllocationConfig config) {
        BigDecimal bonus = BigDecimal.ZERO;

        if (a.getClassCode() != null && a.getClassCode().equals(b.getClassCode())) {
            bonus = bonus.add(BigDecimal.valueOf(config.getSameClassBonus() != null ? config.getSameClassBonus() : 0));
        } else if (a.getMajorCode() != null && a.getMajorCode().equals(b.getMajorCode())) {
            bonus = bonus.add(BigDecimal.valueOf(config.getSameMajorBonus() != null ? config.getSameMajorBonus() : 0));
        } else if (a.getDeptCode() != null && a.getDeptCode().equals(b.getDeptCode())) {
            bonus = bonus.add(BigDecimal.valueOf(config.getSameDeptBonus() != null ? config.getSameDeptBonus() : 0));
        }

        return bonus;
    }

    // ==================== 工具方法 ====================

    private int getIntValue(Integer value, int defaultValue) {
        return value != null ? value : defaultValue;
    }
}
