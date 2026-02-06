package com.project.backend.allocation.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.backend.student.entity.Student;
import com.project.backend.student.mapper.StudentMapper;
import com.project.backend.allocation.dto.survey.LifestyleSurveyQueryDTO;
import com.project.backend.allocation.dto.survey.LifestyleSurveySaveDTO;
import com.project.backend.allocation.entity.LifestyleSurvey;
import com.project.backend.allocation.mapper.LifestyleSurveyMapper;
import com.project.backend.allocation.service.LifestyleSurveyService;
import com.project.backend.allocation.vo.LifestyleSurveyVO;
import com.project.backend.allocation.vo.survey.LifestyleSurveyDetailVO;
import com.project.backend.allocation.vo.survey.LifestyleSurveyListVO;
import com.project.backend.allocation.vo.survey.LifestyleSurveyStatisticsVO;
import com.project.core.exception.BusinessException;
import com.project.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 生活习惯问卷服务实现
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LifestyleSurveyServiceImpl extends ServiceImpl<LifestyleSurveyMapper, LifestyleSurvey>
        implements LifestyleSurveyService {

    private final StudentMapper studentMapper;
    private final LifestyleSurveyMapper surveyMapper;

    private static final Map<Integer, String> STATUS_NAMES = Map.of(
            0, "未填写",
            1, "已填写",
            2, "已锁定"
    );

    @Override
    @Transactional(readOnly = true)
    public PageResult<LifestyleSurveyListVO> pageList(LifestyleSurveyQueryDTO queryDTO) {
        // 构建学生查询条件
        LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(Student::getDeleted, 0);

        if (StrUtil.isNotBlank(queryDTO.getStudentNo())) {
            studentWrapper.like(Student::getStudentNo, queryDTO.getStudentNo());
        }
        if (StrUtil.isNotBlank(queryDTO.getStudentName())) {
            studentWrapper.like(Student::getStudentName, queryDTO.getStudentName());
        }
        if (StrUtil.isNotBlank(queryDTO.getClassCode())) {
            studentWrapper.eq(Student::getClassCode, queryDTO.getClassCode());
        }
        if (queryDTO.getEnrollmentYear() != null) {
            studentWrapper.eq(Student::getEnrollmentYear, queryDTO.getEnrollmentYear());
        }

        // 分页查询学生
        Page<Student> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<Student> studentPage = studentMapper.selectPage(page, studentWrapper);

        // 获取学生ID列表
        List<Long> studentIds = studentPage.getRecords().stream()
                .map(Student::getId)
                .collect(Collectors.toList());

        // 查询问卷状态
        Map<Long, LifestyleSurvey> surveyMap = new HashMap<>();
        if (!studentIds.isEmpty()) {
            LambdaQueryWrapper<LifestyleSurvey> surveyWrapper = new LambdaQueryWrapper<>();
            surveyWrapper.in(LifestyleSurvey::getStudentId, studentIds);
            List<LifestyleSurvey> surveys = surveyMapper.selectList(surveyWrapper);
            surveyMap = surveys.stream()
                    .collect(Collectors.toMap(LifestyleSurvey::getStudentId, s -> s));
        }

        // 根据填写状态筛选
        Map<Long, LifestyleSurvey> finalSurveyMap = surveyMap;
        List<LifestyleSurveyListVO> records = studentPage.getRecords().stream()
                .map(student -> {
                    LifestyleSurvey survey = finalSurveyMap.get(student.getId());
                    LifestyleSurveyListVO vo = new LifestyleSurveyListVO();
                    vo.setId(student.getId());
                    vo.setStudentId(student.getId());
                    vo.setStudentNo(student.getStudentNo());
                    vo.setStudentName(student.getStudentName());
                    vo.setClassName(student.getClassCode());
                    vo.setEnrollmentYear(student.getEnrollmentYear());

                    if (survey != null) {
                        vo.setFillStatus(survey.getSurveyStatus() >= 1 ? "filled" : "unfilled");
                        vo.setFillTime(survey.getSubmitTime() != null ? survey.getSubmitTime().toString() : null);
                        vo.setSurveyStatus(survey.getSurveyStatus());
                    } else {
                        vo.setFillStatus("unfilled");
                        vo.setSurveyStatus(0);
                    }

                    // 填充生活习惯数据
                    vo.setSleepSchedule(getSleepScheduleText(student.getSleepSchedule()));
                    vo.setSmokingStatus(getSmokingStatusText(student.getSmokingStatus()));
                    vo.setCleanlinessLevel(getCleanlinessText(student.getCleanlinessLevel()));
                    vo.setSocialPreference(getSocialText(student.getSocialPreference()));

                    return vo;
                })
                .filter(vo -> {
                    // 筛选填写状态
                    if (StrUtil.isNotBlank(queryDTO.getFillStatus())) {
                        return queryDTO.getFillStatus().equals(vo.getFillStatus());
                    }
                    return true;
                })
                .collect(Collectors.toList());

        return PageResult.build(records, studentPage.getTotal(), queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    @Transactional(readOnly = true)
    public LifestyleSurveyStatisticsVO getStatistics(Integer enrollmentYear, String classCode) {
        // 构建学生查询条件
        LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(Student::getDeleted, 0);
        if (enrollmentYear != null) {
            studentWrapper.eq(Student::getEnrollmentYear, enrollmentYear);
        }
        if (StrUtil.isNotBlank(classCode)) {
            studentWrapper.eq(Student::getClassCode, classCode);
        }

        // 统计学生总数
        long totalStudents = studentMapper.selectCount(studentWrapper);

        // 统计已填写数量
        long filledCount = surveyMapper.countFilledByCondition(enrollmentYear, classCode);

        LifestyleSurveyStatisticsVO vo = new LifestyleSurveyStatisticsVO();
        vo.setTotal((int) totalStudents);
        vo.setFilled((int) filledCount);
        vo.setUnfilled((int) (totalStudents - filledCount));
        vo.setFillRate(totalStudents > 0 ? (double) filledCount / totalStudents * 100 : 0);

        return vo;
    }

    @Override
    @Transactional(readOnly = true)
    public LifestyleSurveyDetailVO getDetailByStudentId(Long studentId) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }

        LambdaQueryWrapper<LifestyleSurvey> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LifestyleSurvey::getStudentId, studentId);
        LifestyleSurvey survey = surveyMapper.selectOne(wrapper);

        LifestyleSurveyDetailVO vo = new LifestyleSurveyDetailVO();
        vo.setStudentId(studentId);
        vo.setStudentNo(student.getStudentNo());
        vo.setStudentName(student.getStudentName());
        vo.setClassName(student.getClassCode());

        if (survey != null) {
            vo.setFillStatus(survey.getSurveyStatus() >= 1 ? "filled" : "unfilled");
            vo.setFillTime(survey.getSubmitTime() != null ? survey.getSubmitTime().toString() : null);
            vo.setLastUpdateTime(survey.getLastUpdateTime() != null ? survey.getLastUpdateTime().toString() : null);
            vo.setSurveyVersion(survey.getSurveyVersion());
        } else {
            vo.setFillStatus("unfilled");
        }

        // 填充所有生活习惯数据
        vo.setSmokingStatus(student.getSmokingStatus());
        vo.setSmokingStatusText(getSmokingStatusText(student.getSmokingStatus()));
        vo.setSmokingTolerance(student.getSmokingTolerance());
        vo.setSleepSchedule(student.getSleepSchedule());
        vo.setSleepScheduleText(getSleepScheduleText(student.getSleepSchedule()));
        vo.setSleepQuality(student.getSleepQuality());
        vo.setSnores(student.getSnores());
        vo.setSensitiveToLight(student.getSensitiveToLight());
        vo.setSensitiveToSound(student.getSensitiveToSound());
        vo.setCleanlinessLevel(student.getCleanlinessLevel());
        vo.setCleanlinessLevelText(getCleanlinessText(student.getCleanlinessLevel()));
        vo.setBedtimeCleanup(student.getBedtimeCleanup());
        vo.setSocialPreference(student.getSocialPreference());
        vo.setSocialPreferenceText(getSocialText(student.getSocialPreference()));
        vo.setAllowVisitors(student.getAllowVisitors());
        vo.setPhoneCallTime(student.getPhoneCallTime());
        vo.setStudyInRoom(student.getStudyInRoom());
        vo.setStudyEnvironment(student.getStudyEnvironment());
        vo.setComputerUsageTime(student.getComputerUsageTime());
        vo.setGamingPreference(student.getGamingPreference());
        vo.setMusicPreference(student.getMusicPreference());
        vo.setMusicVolume(student.getMusicVolume());
        vo.setEatInRoom(student.getEatInRoom());

        return vo;
    }

    @Override
    @Transactional(readOnly = true)
    public LifestyleSurveyVO getSurveyStatus(Long studentId) {
        // 获取学生信息
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }

        // 获取问卷状态
        LambdaQueryWrapper<LifestyleSurvey> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LifestyleSurvey::getStudentId, studentId);
        LifestyleSurvey survey = getOne(wrapper);

        LifestyleSurveyVO vo = new LifestyleSurveyVO();
        vo.setStudentId(studentId);

        if (survey != null) {
            vo.setSurveyStatus(survey.getSurveyStatus());
            vo.setSubmitTime(survey.getSubmitTime());
            vo.setLastUpdateTime(survey.getLastUpdateTime());
            vo.setLockTime(survey.getLockTime());
            vo.setCanModify(survey.getSurveyStatus() != 2); // 非锁定状态可修改
        } else {
            vo.setSurveyStatus(0);
            vo.setCanModify(true);
        }

        vo.setSurveyStatusName(STATUS_NAMES.getOrDefault(vo.getSurveyStatus(), "未知"));

        // 填充已有的生活习惯数据
        vo.setSmokingStatus(student.getSmokingStatus());
        vo.setSmokingTolerance(student.getSmokingTolerance());
        vo.setSleepSchedule(student.getSleepSchedule());
        vo.setSleepQuality(student.getSleepQuality());
        vo.setSnores(student.getSnores());
        vo.setSensitiveToLight(student.getSensitiveToLight());
        vo.setSensitiveToSound(student.getSensitiveToSound());
        vo.setCleanlinessLevel(student.getCleanlinessLevel());
        vo.setBedtimeCleanup(student.getBedtimeCleanup());
        vo.setSocialPreference(student.getSocialPreference());
        vo.setAllowVisitors(student.getAllowVisitors());
        vo.setPhoneCallTime(student.getPhoneCallTime());
        vo.setStudyInRoom(student.getStudyInRoom());
        vo.setStudyEnvironment(student.getStudyEnvironment());
        vo.setComputerUsageTime(student.getComputerUsageTime());
        vo.setGamingPreference(student.getGamingPreference());
        vo.setMusicPreference(student.getMusicPreference());
        vo.setMusicVolume(student.getMusicVolume());
        vo.setEatInRoom(student.getEatInRoom());

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitSurvey(Long studentId, LifestyleSurveySaveDTO dto) {
        // 检查是否可以修改
        if (!canModifySurvey(studentId)) {
            throw new BusinessException("问卷已锁定，无法修改");
        }

        // 更新学生表的生活习惯字段
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }

        BeanUtil.copyProperties(dto, student);
        studentMapper.updateById(student);

        // 更新问卷状态
        LambdaQueryWrapper<LifestyleSurvey> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LifestyleSurvey::getStudentId, studentId);
        LifestyleSurvey survey = getOne(wrapper);

        if (survey == null) {
            survey = new LifestyleSurvey();
            survey.setStudentId(studentId);
            survey.setSurveyStatus(1);
            survey.setSubmitTime(LocalDateTime.now());
            survey.setLastUpdateTime(LocalDateTime.now());
            survey.setSurveyVersion(1);
            save(survey);
        } else {
            survey.setSurveyStatus(1);
            survey.setLastUpdateTime(LocalDateTime.now());
            survey.setSurveyVersion(survey.getSurveyVersion() + 1);
            updateById(survey);
        }

        log.info("学生 {} 提交问卷成功", studentId);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canModifySurvey(Long studentId) {
        LambdaQueryWrapper<LifestyleSurvey> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LifestyleSurvey::getStudentId, studentId);
        LifestyleSurvey survey = getOne(wrapper);

        // 不存在或未锁定都可以修改
        return survey == null || survey.getSurveyStatus() != 2;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean lockSurvey(Long studentId) {
        LambdaQueryWrapper<LifestyleSurvey> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LifestyleSurvey::getStudentId, studentId);
        LifestyleSurvey survey = getOne(wrapper);

        if (survey != null) {
            survey.setSurveyStatus(2);
            survey.setLockTime(LocalDateTime.now());
            return updateById(survey);
        }
        return false;
    }

    // ==================== 辅助方法 ====================

    private String getSleepScheduleText(Integer value) {
        if (value == null) {
            return "未填写";
        }
        return switch (value) {
            case 0 -> "早睡早起(22:00前)";
            case 1 -> "正常作息(23:00左右)";
            case 2 -> "晚睡晚起(0:00后)";
            case 3 -> "夜猫子";
            default -> "未知";
        };
    }

    private String getSmokingStatusText(Integer value) {
        if (value == null) {
            return "未填写";
        }
        return value == 0 ? "不吸烟" : "吸烟";
    }

    private String getCleanlinessText(Integer value) {
        if (value == null) {
            return "未填写";
        }
        return switch (value) {
            case 1 -> "非常整洁";
            case 2 -> "整洁";
            case 3 -> "一般";
            case 4 -> "随意";
            case 5 -> "不整洁";
            default -> "未知";
        };
    }

    private String getSocialText(Integer value) {
        if (value == null) {
            return "未填写";
        }
        return switch (value) {
            case 0 -> "喜欢安静";
            case 1 -> "中等";
            case 2 -> "喜欢热闹";
            default -> "未知";
        };
    }
}
