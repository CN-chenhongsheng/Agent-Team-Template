package com.project.backend.allocation.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.backend.student.entity.Student;
import com.project.backend.student.mapper.StudentMapper;
import com.project.backend.allocation.algorithm.AlgorithmFactory;
import com.project.backend.allocation.algorithm.AllocationAlgorithm;
import com.project.backend.allocation.algorithm.model.AllocationResultDTO;
import com.project.backend.allocation.dto.task.AllocationTaskQueryDTO;
import com.project.backend.allocation.dto.task.AllocationTaskSaveDTO;
import com.project.backend.allocation.entity.AllocationConfig;
import com.project.backend.allocation.entity.AllocationResult;
import com.project.backend.allocation.entity.AllocationTask;
import com.project.backend.allocation.mapper.AllocationResultMapper;
import com.project.backend.allocation.mapper.AllocationTaskMapper;
import com.project.backend.allocation.service.AllocationConfigService;
import com.project.backend.allocation.service.AllocationProgressService;
import com.project.backend.allocation.service.AllocationTaskService;
import com.project.backend.allocation.vo.AllocationPreviewVO;
import com.project.backend.allocation.vo.AllocationProgressVO;
import com.project.backend.allocation.vo.AllocationTaskVO;
import com.project.backend.room.entity.Bed;
import com.project.backend.room.mapper.BedMapper;
import com.project.core.exception.BusinessException;
import com.project.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 分配任务服务实现
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AllocationTaskServiceImpl extends ServiceImpl<AllocationTaskMapper, AllocationTask>
        implements AllocationTaskService {

    private final AllocationConfigService configService;
    private final AlgorithmFactory algorithmFactory;
    private final StudentMapper studentMapper;
    private final BedMapper bedMapper;
    private final AllocationResultMapper resultMapper;
    private final AllocationProgressService allocationProgressService;

    // 任务进度缓存
    private final ConcurrentHashMap<Long, AllocationProgressVO> progressCache = new ConcurrentHashMap<>();

    private static final Map<Integer, String> TASK_TYPE_NAMES = Map.of(
            1, "批量分配",
            2, "单个推荐",
            3, "调宿优化"
    );

    private static final Map<Integer, String> STATUS_NAMES = Map.of(
            0, "待执行",
            1, "执行中",
            2, "已完成",
            3, "部分确认",
            4, "全部确认",
            5, "已取消"
    );

    @Override
    @Transactional(readOnly = true)
    public PageResult<AllocationTaskVO> pageList(AllocationTaskQueryDTO queryDTO) {
        LambdaQueryWrapper<AllocationTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(queryDTO.getTaskName()),
                        AllocationTask::getTaskName, queryDTO.getTaskName())
                .eq(queryDTO.getTaskType() != null, AllocationTask::getTaskType, queryDTO.getTaskType())
                .eq(queryDTO.getStatus() != null, AllocationTask::getStatus, queryDTO.getStatus())
                .eq(queryDTO.getConfigId() != null, AllocationTask::getConfigId, queryDTO.getConfigId())
                .eq(queryDTO.getTargetEnrollmentYear() != null,
                        AllocationTask::getTargetEnrollmentYear, queryDTO.getTargetEnrollmentYear())
                .orderByDesc(AllocationTask::getCreateTime);

        Page<AllocationTask> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page(page, wrapper);

        List<AllocationTaskVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .toList();

        return PageResult.build(voList, page.getTotal(), queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    @Transactional(readOnly = true)
    public AllocationTaskVO getDetailById(Long id) {
        AllocationTask task = getById(id);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        return convertToVO(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveTask(AllocationTaskSaveDTO saveDTO) {
        // 验证配置存在
        AllocationConfig config = configService.getById(saveDTO.getConfigId());
        if (config == null) {
            throw new BusinessException("配置不存在");
        }

        // 编辑模式：ID 存在时走更新逻辑
        if (saveDTO.getId() != null) {
            AllocationTask existing = getById(saveDTO.getId());
            if (existing == null) {
                throw new BusinessException("任务不存在");
            }
            if (existing.getStatus() != 0) {
                throw new BusinessException("只能编辑待执行的任务");
            }
            // 只更新允许修改的字段，保留 id、status、创建信息等
            BeanUtil.copyProperties(saveDTO, existing, "id", "status",
                    "totalStudents", "totalBeds", "allocatedCount", "failedCount",
                    "confirmedCount", "avgMatchScore", "minMatchScore", "maxMatchScore",
                    "lowScoreCount", "startTime", "endTime", "executeDuration",
                    "createTime", "createBy", "deleted");
            return updateById(existing);
        }

        // 新增模式
        AllocationTask task = new AllocationTask();
        BeanUtil.copyProperties(saveDTO, task);
        task.setStatus(0); // 待执行

        return save(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTask(Long id) {
        AllocationTask task = getById(id);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        if (task.getStatus() == 1) {
            throw new BusinessException("执行中的任务无法删除");
        }
        return removeById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public AllocationPreviewVO previewTask(AllocationTaskSaveDTO saveDTO) {
        AllocationPreviewVO preview = new AllocationPreviewVO();
        List<String> warnings = new ArrayList<>();

        // 1. 统计符合条件的学生
        LambdaQueryWrapper<Student> studentWrapper = buildStudentQueryWrapper(saveDTO);
        List<Student> students = studentMapper.selectList(studentWrapper);
        preview.setTotalStudents(students.size());

        // 统计问卷填写情况
        long filledCount = students.stream()
                .filter(s -> s.getSmokingStatus() != null || s.getSleepSchedule() != null)
                .count();
        preview.setSurveyFilledCount((int) filledCount);
        preview.setSurveyUnfilledCount(students.size() - (int) filledCount);
        preview.setSurveyFillRate(students.size() > 0 ? (filledCount * 100.0 / students.size()) : 0);

        // 已分配学生
        long alreadyAllocated = students.stream().filter(s -> s.getBedId() != null).count();
        preview.setAlreadyAllocatedCount((int) alreadyAllocated);
        preview.setToBeAllocatedCount(students.size() - (int) alreadyAllocated);

        // 2. 统计可用床位
        LambdaQueryWrapper<Bed> bedWrapper = buildBedQueryWrapper(saveDTO);
        bedWrapper.eq(Bed::getBedStatus, 1); // 空闲
        List<Bed> beds = bedMapper.selectList(bedWrapper);
        preview.setTotalAvailableBeds(beds.size());

        Set<Long> roomIds = beds.stream().map(Bed::getRoomId).collect(Collectors.toSet());
        preview.setTotalRooms(roomIds.size());

        // 3. 床位是否充足
        int toAllocate = preview.getToBeAllocatedCount();
        int availableBeds = beds.size();
        preview.setBedsEnough(availableBeds >= toAllocate);
        preview.setBedDifference(availableBeds - toAllocate);

        // 4. 警告信息
        if (preview.getSurveyUnfilledCount() > 0) {
            warnings.add("有 " + preview.getSurveyUnfilledCount() + " 名学生未填写生活习惯问卷，将不参与智能分配");
        }
        if (!preview.getBedsEnough()) {
            warnings.add("床位不足，缺少 " + Math.abs(preview.getBedDifference()) + " 个床位");
        }
        if (preview.getToBeAllocatedCount() == 0) {
            warnings.add("没有待分配的学生");
        }

        preview.setWarnings(warnings);
        preview.setCanExecute(preview.getToBeAllocatedCount() > 0 && preview.getBedsEnough());
        if (!preview.getCanExecute()) {
            preview.setCannotExecuteReason(preview.getToBeAllocatedCount() == 0 ? "没有待分配的学生" : "床位不足");
        }

        return preview;
    }

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void executeTask(Long taskId) {
        AllocationTask task = getById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        if (task.getStatus() != 0) {
            throw new BusinessException("任务状态不正确，只能执行待执行的任务");
        }

        // 更新状态为执行中
        task.setStatus(1);
        task.setStartTime(LocalDateTime.now());
        updateById(task);

        // 初始化进度
        AllocationProgressVO progress = new AllocationProgressVO();
        progress.setTaskId(taskId);
        progress.setStatus(1);
        progress.setStatusName("执行中");
        progress.setCurrentStage("正在准备数据...");
        progress.setProgressPercent(0);
        progress.setCompleted(false);
        progressCache.put(taskId, progress);

        try {
            // 1. 获取配置
            AllocationConfig config = configService.getById(task.getConfigId());
            if (config == null) {
                throw new BusinessException("配置不存在");
            }

            // 2. 获取待分配学生
            LambdaQueryWrapper<Student> studentWrapper = buildStudentQueryWrapperFromTask(task);
            studentWrapper.isNull(Student::getBedId); // 未分配的
            studentWrapper.isNotNull(Student::getSleepSchedule); // 已填问卷的
            List<Student> students = studentMapper.selectList(studentWrapper);

            task.setTotalStudents(students.size());
            progress.setTotalStudents(students.size());

            // 3. 获取可用床位
            LambdaQueryWrapper<Bed> bedWrapper = buildBedQueryWrapperFromTask(task);
            bedWrapper.eq(Bed::getBedStatus, 1);
            List<Bed> beds = bedMapper.selectList(bedWrapper);
            task.setTotalBeds(beds.size());

            // 按房间分组床位
            Map<Long, List<Bed>> roomBedMap = beds.stream()
                    .collect(Collectors.groupingBy(Bed::getRoomId));

            // 获取房间现有学生
            Set<Long> roomIds = roomBedMap.keySet();
            Map<Long, List<Student>> roomStudentMap = new HashMap<>();
            if (!roomIds.isEmpty()) {
                LambdaQueryWrapper<Student> roommateWrapper = new LambdaQueryWrapper<>();
                roommateWrapper.in(Student::getRoomId, roomIds);
                List<Student> existingStudents = studentMapper.selectList(roommateWrapper);
                roomStudentMap = existingStudents.stream()
                        .filter(s -> s.getRoomId() != null)
                        .collect(Collectors.groupingBy(Student::getRoomId));
            }

            // 4. 执行算法
            AllocationAlgorithm algorithm = algorithmFactory.getAlgorithm(config.getAlgorithmType());
            List<AllocationResultDTO> results = algorithm.allocate(
                    students, roomBedMap, roomStudentMap, config,
                    p -> {
                        progress.setProcessedCount(p.getProcessedCount());
                        progress.setSuccessCount(p.getSuccessCount());
                        progress.setFailedCount(p.getFailedCount());
                        progress.setCurrentStage(p.getCurrentStage());
                        progress.setProgressPercent(p.getProgressPercent());
                        // SSE 实时推送（有订阅者时才推送）
                        if (allocationProgressService.hasSubscribers(taskId)) {
                            allocationProgressService.pushProgress(taskId, progress);
                        }
                    }
            );

            // 5. 保存结果
            int successCount = 0;
            int failedCount = 0;
            BigDecimal totalScore = BigDecimal.ZERO;

            for (AllocationResultDTO dto : results) {
                AllocationResult result = new AllocationResult();
                result.setTaskId(taskId);
                result.setStudentId(dto.getStudentId());
                result.setStudentNo(dto.getStudentNo());
                result.setStudentName(dto.getStudentName());
                result.setGender(dto.getGender());
                result.setDeptCode(dto.getDeptCode());
                result.setMajorCode(dto.getMajorCode());
                result.setClassCode(dto.getClassCode());
                result.setAllocatedBedId(dto.getBedId());
                result.setAllocatedRoomId(dto.getRoomId());
                result.setAllocatedRoomCode(dto.getRoomCode());
                result.setAllocatedFloorId(dto.getFloorId());
                result.setAllocatedFloorCode(dto.getFloorCode());
                result.setMatchScore(dto.getMatchScore());
                result.setConflictReasons(dto.getConflictReasons());
                result.setAdvantages(dto.getAdvantages());
                result.setRoommateIds(dto.getRoommateIds());
                result.setStatus(0); // 待确认

                resultMapper.insert(result);

                if (dto.isSuccess()) {
                    successCount++;
                    if (dto.getMatchScore() != null) {
                        totalScore = totalScore.add(dto.getMatchScore());
                    }
                } else {
                    failedCount++;
                }
            }

            // 6. 更新任务统计
            task.setAllocatedCount(successCount);
            task.setFailedCount(failedCount);
            if (successCount > 0) {
                task.setAvgMatchScore(totalScore.divide(BigDecimal.valueOf(successCount), 2, java.math.RoundingMode.HALF_UP));
            }
            task.setMinMatchScore(resultMapper.calculateMinMatchScore(taskId));
            task.setMaxMatchScore(resultMapper.calculateMaxMatchScore(taskId));
            task.setLowScoreCount(resultMapper.countLowScoreResults(taskId, BigDecimal.valueOf(config.getMinMatchScore())));

            task.setStatus(2); // 已完成
            task.setEndTime(LocalDateTime.now());
            task.setExecuteDuration((int) java.time.Duration.between(task.getStartTime(), task.getEndTime()).getSeconds());
            updateById(task);

            // 更新进度
            progress.setStatus(2);
            progress.setStatusName("已完成");
            progress.setSuccessCount(successCount);
            progress.setFailedCount(failedCount);
            progress.setProgressPercent(100);
            progress.setCurrentStage("分配完成");
            progress.setCompleted(true);

            log.info("任务 {} 执行完成，成功：{}，失败：{}", taskId, successCount, failedCount);

            // SSE 推送完成事件
            allocationProgressService.pushComplete(taskId, progress);

        } catch (Exception e) {
            log.error("任务执行失败", e);
            task.setStatus(5); // 取消
            task.setEndTime(LocalDateTime.now());
            updateById(task);

            progress.setStatus(5);
            progress.setStatusName("执行失败");
            progress.setErrorMessage(e.getMessage());
            progress.setCompleted(true);

            // SSE 推送错误事件
            allocationProgressService.pushError(taskId, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AllocationProgressVO getTaskProgress(Long taskId) {
        AllocationProgressVO progress = progressCache.get(taskId);
        if (progress != null) {
            return progress;
        }

        // 从数据库查询任务状态
        AllocationTask task = getById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        progress = new AllocationProgressVO();
        progress.setTaskId(taskId);
        progress.setStatus(task.getStatus());
        progress.setStatusName(STATUS_NAMES.getOrDefault(task.getStatus(), "未知"));
        progress.setTotalStudents(task.getTotalStudents());
        progress.setSuccessCount(task.getAllocatedCount());
        progress.setFailedCount(task.getFailedCount());
        progress.setCompleted(task.getStatus() >= 2);

        if (task.getStatus() == 2) {
            progress.setProgressPercent(100);
            progress.setCurrentStage("分配完成");
        }

        return progress;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelTask(Long taskId) {
        AllocationTask task = getById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        if (task.getStatus() != 0 && task.getStatus() != 2) {
            throw new BusinessException("只能取消待执行或已完成的任务");
        }

        task.setStatus(5);
        return updateById(task);
    }

    // ==================== 私有方法 ====================

    private AllocationTaskVO convertToVO(AllocationTask task) {
        AllocationTaskVO vo = new AllocationTaskVO();
        BeanUtil.copyProperties(task, vo);
        vo.setTaskTypeName(TASK_TYPE_NAMES.getOrDefault(task.getTaskType(), "未知"));
        vo.setStatusName(STATUS_NAMES.getOrDefault(task.getStatus(), "未知"));

        // 获取配置名称
        if (task.getConfigId() != null) {
            AllocationConfig config = configService.getById(task.getConfigId());
            if (config != null) {
                vo.setConfigName(config.getConfigName());
            }
        }

        return vo;
    }

    /**
     * 构建学生查询条件（从DTO）
     */
    private LambdaQueryWrapper<Student> buildStudentQueryWrapper(AllocationTaskSaveDTO dto) {
        return buildStudentQueryWrapper(
                dto.getTargetEnrollmentYear(),
                dto.getTargetGender(),
                dto.getTargetCampusCode(),
                dto.getTargetDeptCode(),
                dto.getTargetMajorCode()
        );
    }

    /**
     * 构建学生查询条件（从Task实体）
     */
    private LambdaQueryWrapper<Student> buildStudentQueryWrapperFromTask(AllocationTask task) {
        return buildStudentQueryWrapper(
                task.getTargetEnrollmentYear(),
                task.getTargetGender(),
                task.getTargetCampusCode(),
                task.getTargetDeptCode(),
                task.getTargetMajorCode()
        );
    }

    /**
     * 构建学生查询条件（通用方法）
     *
     * @param enrollmentYear 入学年份
     * @param gender 性别（male/female）
     * @param campusCode 校区编码
     * @param deptCode 院系编码
     * @param majorCode 专业编码
     * @return 查询条件
     */
    private LambdaQueryWrapper<Student> buildStudentQueryWrapper(
            Integer enrollmentYear, String gender, String campusCode,
            String deptCode, String majorCode) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(enrollmentYear != null, Student::getEnrollmentYear, enrollmentYear)
                .eq(StrUtil.isNotBlank(gender),
                        Student::getGender, "male".equals(gender) ? 1 : 2)
                .eq(StrUtil.isNotBlank(campusCode), Student::getCampusCode, campusCode)
                .eq(StrUtil.isNotBlank(deptCode), Student::getDeptCode, deptCode)
                .eq(StrUtil.isNotBlank(majorCode), Student::getMajorCode, majorCode)
                .eq(Student::getStatus, 1)
                .eq(Student::getDeleted, 0);
        return wrapper;
    }

    /**
     * 构建床位查询条件（从DTO）
     */
    private LambdaQueryWrapper<Bed> buildBedQueryWrapper(AllocationTaskSaveDTO dto) {
        return buildBedQueryWrapper(dto.getTargetCampusCode(), dto.getTargetFloorIds());
    }

    /**
     * 构建床位查询条件（从Task实体）
     */
    private LambdaQueryWrapper<Bed> buildBedQueryWrapperFromTask(AllocationTask task) {
        return buildBedQueryWrapper(task.getTargetCampusCode(), task.getTargetFloorIds());
    }

    /**
     * 构建床位查询条件（通用方法）
     *
     * @param campusCode 校区编码
     * @param floorIds 楼层ID列表
     * @return 查询条件
     */
    private LambdaQueryWrapper<Bed> buildBedQueryWrapper(String campusCode, List<Long> floorIds) {
        LambdaQueryWrapper<Bed> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(campusCode), Bed::getCampusCode, campusCode)
                .in(floorIds != null && !floorIds.isEmpty(), Bed::getFloorId, floorIds)
                .eq(Bed::getStatus, 1)
                .eq(Bed::getDeleted, 0);
        return wrapper;
    }
}
