package com.project.backend.allocation.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.backend.student.entity.Student;
import com.project.backend.student.mapper.StudentMapper;
import com.project.backend.allocation.dto.result.AllocationResultQueryDTO;
import com.project.backend.allocation.entity.AllocationResult;
import com.project.backend.allocation.entity.AllocationTask;
import com.project.backend.allocation.mapper.AllocationResultMapper;
import com.project.backend.allocation.mapper.AllocationTaskMapper;
import com.project.backend.allocation.service.AllocationResultService;
import com.project.backend.allocation.vo.AllocationResultVO;
import com.project.backend.allocation.vo.AllocationRoommateVO;
import com.project.core.exception.BusinessException;
import com.project.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分配结果服务实现
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AllocationResultServiceImpl extends ServiceImpl<AllocationResultMapper, AllocationResult>
        implements AllocationResultService {

    private final AllocationResultMapper resultMapper;
    private final AllocationTaskMapper taskMapper;
    private final StudentMapper studentMapper;

    private static final Map<Integer, String> RESULT_STATUS_NAMES = Map.of(
            0, "待确认",
            1, "已确认",
            2, "已拒绝",
            3, "已调整"
    );

    @Override
    @Transactional(readOnly = true)
    public PageResult<AllocationResultVO> pageList(AllocationResultQueryDTO queryDTO) {
        LambdaQueryWrapper<AllocationResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getTaskId() != null, AllocationResult::getTaskId, queryDTO.getTaskId())
                .like(StrUtil.isNotBlank(queryDTO.getStudentNo()),
                        AllocationResult::getStudentNo, queryDTO.getStudentNo())
                .like(StrUtil.isNotBlank(queryDTO.getStudentName()),
                        AllocationResult::getStudentName, queryDTO.getStudentName())
                .eq(StrUtil.isNotBlank(queryDTO.getRoomCode()),
                        AllocationResult::getAllocatedRoomCode, queryDTO.getRoomCode())
                .eq(queryDTO.getStatus() != null, AllocationResult::getStatus, queryDTO.getStatus())
                .ge(queryDTO.getMinScore() != null, AllocationResult::getMatchScore, queryDTO.getMinScore())
                .le(queryDTO.getMaxScore() != null, AllocationResult::getMatchScore, queryDTO.getMaxScore())
                .orderByAsc(AllocationResult::getMatchScore);

        Page<AllocationResult> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        resultMapper.selectPage(page, wrapper);

        List<AllocationResultVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .toList();

        return PageResult.build(voList, page.getTotal(), queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    @Transactional(readOnly = true)
    public AllocationResultVO getDetailById(Long id) {
        AllocationResult result = getById(id);
        if (result == null) {
            throw new BusinessException("分配结果不存在");
        }
        return convertToVO(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmResults(Long taskId, List<Long> resultIds) {
        updateResultsStatus(taskId, resultIds, 1, null, LocalDateTime.now());

        // 更新任务确认数
        updateTaskConfirmCount(taskId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmAll(Long taskId) {
        LambdaQueryWrapper<AllocationResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AllocationResult::getTaskId, taskId)
                .eq(AllocationResult::getStatus, 0);
        List<AllocationResult> results = resultMapper.selectList(wrapper);

        List<Long> resultIds = results.stream().map(AllocationResult::getId).collect(Collectors.toList());
        return confirmResults(taskId, resultIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rejectResults(Long taskId, List<Long> resultIds, String reason) {
        updateResultsStatus(taskId, resultIds, 2, reason, null);
        return true;
    }

    /**
     * 批量更新分配结果状态（抽离的公共方法）
     *
     * @param taskId 任务ID
     * @param resultIds 结果ID列表
     * @param status 目标状态（1=已确认，2=已拒绝）
     * @param reason 调整原因（可选）
     * @param confirmTime 确认时间（可选）
     */
    private void updateResultsStatus(Long taskId, List<Long> resultIds, Integer status,
                                     String reason, LocalDateTime confirmTime) {
        for (Long resultId : resultIds) {
            AllocationResult result = resultMapper.selectById(resultId);
            if (result != null && result.getTaskId().equals(taskId)) {
                result.setStatus(status);
                if (reason != null) {
                    result.setAdjustReason(reason);
                }
                if (confirmTime != null) {
                    result.setConfirmTime(confirmTime);
                }
                resultMapper.updateById(result);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean adjustResult(Long resultId, Long newBedId, String reason) {
        AllocationResult result = resultMapper.selectById(resultId);
        if (result == null) {
            throw new BusinessException("分配结果不存在");
        }

        result.setStatus(3); // 已调整
        result.setAdjustedBedId(newBedId);
        result.setAdjustReason(reason);
        resultMapper.updateById(result);

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AllocationResultVO> getProblemList(Long taskId, Integer threshold) {
        LambdaQueryWrapper<AllocationResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AllocationResult::getTaskId, taskId)
                .lt(AllocationResult::getMatchScore, threshold)
                .orderByAsc(AllocationResult::getMatchScore);

        List<AllocationResult> results = resultMapper.selectList(wrapper);
        return results.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AllocationResultVO getMyResult(Long studentId) {
        // 查询最新的已确认分配结果
        LambdaQueryWrapper<AllocationResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AllocationResult::getStudentId, studentId)
                .eq(AllocationResult::getStatus, 1) // 已确认
                .orderByDesc(AllocationResult::getCreateTime)
                .last("LIMIT 1");

        AllocationResult result = resultMapper.selectOne(wrapper);
        if (result == null) {
            return null;
        }

        return convertToVO(result);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AllocationRoommateVO> getRoommates(Long studentId) {
        // 获取学生的房间ID
        Student student = studentMapper.selectById(studentId);
        if (student == null || student.getRoomId() == null) {
            return new ArrayList<>();
        }

        // 查询同房间的学生
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getRoomId, student.getRoomId())
                .ne(Student::getId, studentId)
                .eq(Student::getDeleted, 0);

        List<Student> roommates = studentMapper.selectList(wrapper);

        return roommates.stream()
                .map(this::convertToAllocationRoommateVO)
                .collect(Collectors.toList());
    }

    // ==================== 私有方法 ====================

    private AllocationResultVO convertToVO(AllocationResult result) {
        AllocationResultVO vo = new AllocationResultVO();
        BeanUtil.copyProperties(result, vo);
        vo.setRoommateCount(result.getRoommateIds() != null ? result.getRoommateIds().size() : 0);
        vo.calculateMatchScoreLevel();
        vo.setStatusName(RESULT_STATUS_NAMES.getOrDefault(result.getStatus(), "未知"));
        return vo;
    }

    private AllocationRoommateVO convertToAllocationRoommateVO(Student student) {
        AllocationRoommateVO vo = new AllocationRoommateVO();
        vo.setStudentId(student.getId());
        vo.setStudentName(student.getStudentName());
        vo.setStudentNo(student.getStudentNo());
        vo.setGender(student.getGender());
        vo.setClassName(student.getClassCode());
        vo.setSleepSchedule(student.getSleepSchedule());
        vo.setSocialPreference(student.getSocialPreference());
        return vo;
    }

    private void updateTaskConfirmCount(Long taskId) {
        int confirmedCount = resultMapper.countByStatus(taskId, 1);
        AllocationTask task = taskMapper.selectById(taskId);
        if (task != null) {
            task.setConfirmedCount(confirmedCount);
            if (confirmedCount >= task.getAllocatedCount()) {
                task.setStatus(4); // 全部确认
            } else if (confirmedCount > 0) {
                task.setStatus(3); // 部分确认
            }
            taskMapper.updateById(task);
        }
    }
}
