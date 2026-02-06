package com.project.backend.student.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.backend.student.dto.student.StudentLifestyleDTO;
import com.project.backend.student.dto.student.StudentQueryDTO;
import com.project.backend.student.dto.student.StudentSaveDTO;
import com.project.backend.student.entity.Student;
import com.project.backend.student.mapper.StudentMapper;
import com.project.backend.student.service.StudentBatchLoadContext;
import com.project.backend.student.service.StudentService;
import com.project.backend.student.vo.StudentVO;
import com.project.core.exception.BusinessException;
import com.project.core.result.PageResult;
import com.project.backend.organization.entity.Campus;
import com.project.backend.organization.entity.Class;
import com.project.backend.organization.entity.Department;
import com.project.backend.organization.entity.Major;
import com.project.backend.organization.mapper.CampusMapper;
import com.project.backend.organization.mapper.ClassMapper;
import com.project.backend.organization.mapper.DepartmentMapper;
import com.project.backend.organization.mapper.MajorMapper;
import com.project.backend.room.entity.Floor;
import com.project.backend.room.entity.Room;
import com.project.backend.room.entity.Bed;
import com.project.backend.room.mapper.FloorMapper;
import com.project.backend.room.mapper.RoomMapper;
import com.project.backend.room.mapper.BedMapper;
import com.project.backend.util.DictUtils;
import com.project.backend.util.LifestyleTextConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 学生Service实现
 * 
 * @author 陈鸿昇
 * @since 2026-01-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {

    private final CampusMapper campusMapper;
    private final DepartmentMapper departmentMapper;
    private final MajorMapper majorMapper;
    private final ClassMapper classMapper;
    private final FloorMapper floorMapper;
    private final RoomMapper roomMapper;
    private final BedMapper bedMapper;
    private final StudentCacheManager cacheManager;

    @Override
    public PageResult<StudentVO> pageList(StudentQueryDTO queryDTO) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(queryDTO.getStudentNo()), Student::getStudentNo, queryDTO.getStudentNo())
               .like(StrUtil.isNotBlank(queryDTO.getStudentName()), Student::getStudentName, queryDTO.getStudentName())
               .like(StrUtil.isNotBlank(queryDTO.getPhone()), Student::getPhone, queryDTO.getPhone())
               .eq(queryDTO.getGender() != null, Student::getGender, queryDTO.getGender())
               .eq(StrUtil.isNotBlank(queryDTO.getCampusCode()), Student::getCampusCode, queryDTO.getCampusCode())
               .eq(StrUtil.isNotBlank(queryDTO.getDeptCode()), Student::getDeptCode, queryDTO.getDeptCode())
               .eq(StrUtil.isNotBlank(queryDTO.getMajorCode()), Student::getMajorCode, queryDTO.getMajorCode())
               .eq(queryDTO.getClassId() != null, Student::getClassId, queryDTO.getClassId())
               .eq(queryDTO.getBedId() != null, Student::getBedId, queryDTO.getBedId())
               .eq(StrUtil.isNotBlank(queryDTO.getNation()), Student::getNation, queryDTO.getNation())
               .eq(StrUtil.isNotBlank(queryDTO.getPoliticalStatus()), Student::getPoliticalStatus, queryDTO.getPoliticalStatus())
               .eq(queryDTO.getAcademicStatus() != null, Student::getAcademicStatus, queryDTO.getAcademicStatus())
               .eq(queryDTO.getStatus() != null, Student::getStatus, queryDTO.getStatus())
               .orderByDesc(Student::getCreateTime);

        Page<Student> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page(page, wrapper);

        List<Student> records = page.getRecords();
        StudentBatchLoadContext context = batchLoadRelatedData(records);

        List<StudentVO> voList = records.stream()
                .map(student -> convertToVO(student, context))
                .collect(Collectors.toList());

        return PageResult.build(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public StudentVO getDetailById(Long id) {
        Student student = getById(id);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }
        StudentBatchLoadContext context = batchLoadRelatedData(List.of(student));
        return convertToVO(student, context);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveStudent(StudentSaveDTO saveDTO) {
        // 检查学号是否重复
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getStudentNo, saveDTO.getStudentNo())
               .eq(Student::getDeleted, 0);
        if (saveDTO.getId() != null) {
            wrapper.ne(Student::getId, saveDTO.getId());
        }
        if (count(wrapper) > 0) {
            throw new BusinessException("学号已存在");
        }

        Student student = new Student();
        BeanUtil.copyProperties(saveDTO, student);

        if (saveDTO.getId() == null) {
            // 新增时默认状态为启用
            if (student.getStatus() == null) {
                student.setStatus(1);
            }
            return save(student);
        } else {
            return updateById(student);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteStudent(Long id) {
        if (id == null) {
            throw new BusinessException("学生ID不能为空");
        }
        return removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDelete(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("学生ID不能为空");
        }
        return removeByIds(Arrays.asList(ids));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateLifestyle(Long studentId, StudentLifestyleDTO dto) {
        if (studentId == null) {
            throw new BusinessException("学生ID不能为空");
        }
        Student student = getById(studentId);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }

        // 更新生活习惯字段
        if (dto.getSmokingStatus() != null) {
            student.setSmokingStatus(dto.getSmokingStatus());
        }
        if (dto.getSmokingTolerance() != null) {
            student.setSmokingTolerance(dto.getSmokingTolerance());
        }
        if (dto.getSleepSchedule() != null) {
            student.setSleepSchedule(dto.getSleepSchedule());
        }
        if (dto.getSleepQuality() != null) {
            student.setSleepQuality(dto.getSleepQuality());
        }
        if (dto.getSnores() != null) {
            student.setSnores(dto.getSnores());
        }
        if (dto.getSensitiveToLight() != null) {
            student.setSensitiveToLight(dto.getSensitiveToLight());
        }
        if (dto.getSensitiveToSound() != null) {
            student.setSensitiveToSound(dto.getSensitiveToSound());
        }
        if (dto.getCleanlinessLevel() != null) {
            student.setCleanlinessLevel(dto.getCleanlinessLevel());
        }
        if (dto.getBedtimeCleanup() != null) {
            student.setBedtimeCleanup(dto.getBedtimeCleanup());
        }
        if (dto.getSocialPreference() != null) {
            student.setSocialPreference(dto.getSocialPreference());
        }
        if (dto.getAllowVisitors() != null) {
            student.setAllowVisitors(dto.getAllowVisitors());
        }
        if (dto.getPhoneCallTime() != null) {
            student.setPhoneCallTime(dto.getPhoneCallTime());
        }
        if (dto.getStudyInRoom() != null) {
            student.setStudyInRoom(dto.getStudyInRoom());
        }
        if (dto.getStudyEnvironment() != null) {
            student.setStudyEnvironment(dto.getStudyEnvironment());
        }
        if (dto.getComputerUsageTime() != null) {
            student.setComputerUsageTime(dto.getComputerUsageTime());
        }
        if (dto.getGamingPreference() != null) {
            student.setGamingPreference(dto.getGamingPreference());
        }
        if (dto.getMusicPreference() != null) {
            student.setMusicPreference(dto.getMusicPreference());
        }
        if (dto.getMusicVolume() != null) {
            student.setMusicVolume(dto.getMusicVolume());
        }
        if (dto.getEatInRoom() != null) {
            student.setEatInRoom(dto.getEatInRoom());
        }

        return updateById(student);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        if (id == null) {
            throw new BusinessException("学生ID不能为空");
        }
        Student student = getById(id);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }
        student.setStatus(status);
        return updateById(student);
    }

    private StudentVO convertToVO(Student student, StudentBatchLoadContext context) {
        StudentVO vo = new StudentVO();
        BeanUtil.copyProperties(student, vo);
        vo.setStatusText(DictUtils.getLabel("sys_user_status", student.getStatus(), "未知"));
        vo.setGenderText(DictUtils.getLabel("sys_user_sex", student.getGender(), "未知"));
        vo.setAcademicStatusText(DictUtils.getLabel("academic_status", student.getAcademicStatus(), "未知"));

        if (StrUtil.isNotBlank(student.getCampusCode())) {
            Campus campus = context.getCampusByCode().get(student.getCampusCode());
            if (campus != null) {
                vo.setCampusName(campus.getCampusName());
            }
        }

        if (StrUtil.isNotBlank(student.getDeptCode())) {
            Department department = context.getDeptByCode().get(student.getDeptCode());
            if (department != null) {
                vo.setDeptName(department.getDeptName());
            }
        }

        if (StrUtil.isNotBlank(student.getMajorCode())) {
            Major major = context.getMajorByCode().get(student.getMajorCode());
            if (major != null) {
                vo.setMajorName(major.getMajorName());
            }
        }

        if (student.getClassId() != null) {
            Class classEntity = context.getClassById().get(student.getClassId());
            if (classEntity != null) {
                vo.setClassName(classEntity.getClassName());
            }
        }

        if (student.getFloorId() != null) {
            Floor floor = context.getFloorById().get(student.getFloorId());
            if (floor != null) {
                vo.setFloorName(floor.getFloorName());
            }
        } else if (StrUtil.isNotBlank(student.getFloorCode())) {
            Floor floor = context.getFloorByCode().get(student.getFloorCode());
            if (floor != null) {
                vo.setFloorName(floor.getFloorName());
            }
        }

        if (student.getRoomId() != null) {
            Room room = context.getRoomById().get(student.getRoomId());
            if (room != null) {
                vo.setRoomName(room.getRoomNumber());
            }
        } else if (StrUtil.isNotBlank(student.getRoomCode())) {
            Room room = context.getRoomByCode().get(student.getRoomCode());
            if (room != null) {
                vo.setRoomName(room.getRoomNumber());
            }
        }

        if (student.getBedId() != null) {
            Bed bed = context.getBedById().get(student.getBedId());
            if (bed != null) {
                vo.setBedName(bed.getBedNumber());
            }
        } else if (StrUtil.isNotBlank(student.getBedCode())) {
            Bed bed = context.getBedByCode().get(student.getBedCode());
            if (bed != null) {
                vo.setBedName(bed.getBedNumber());
            }
        }

        vo.setSmokingStatusText(LifestyleTextConverter.getSmokingStatusText(student.getSmokingStatus()));
        vo.setSmokingToleranceText(LifestyleTextConverter.getSmokingToleranceText(student.getSmokingTolerance()));
        vo.setSleepScheduleText(LifestyleTextConverter.getSleepScheduleText(student.getSleepSchedule()));
        vo.setSleepQualityText(LifestyleTextConverter.getSleepQualityText(student.getSleepQuality()));
        vo.setSnoresText(LifestyleTextConverter.getSnoresText(student.getSnores()));
        vo.setSensitiveToLightText(LifestyleTextConverter.getSensitiveToLightText(student.getSensitiveToLight()));
        vo.setSensitiveToSoundText(LifestyleTextConverter.getSensitiveToSoundText(student.getSensitiveToSound()));
        vo.setCleanlinessLevelText(LifestyleTextConverter.getCleanlinessLevelText(student.getCleanlinessLevel()));
        vo.setBedtimeCleanupText(LifestyleTextConverter.getBedtimeCleanupText(student.getBedtimeCleanup()));
        vo.setSocialPreferenceText(LifestyleTextConverter.getSocialPreferenceText(student.getSocialPreference()));
        vo.setAllowVisitorsText(LifestyleTextConverter.getAllowVisitorsText(student.getAllowVisitors()));
        vo.setPhoneCallTimeText(LifestyleTextConverter.getPhoneCallTimeText(student.getPhoneCallTime()));
        vo.setStudyInRoomText(LifestyleTextConverter.getStudyInRoomText(student.getStudyInRoom()));
        vo.setStudyEnvironmentText(LifestyleTextConverter.getStudyEnvironmentText(student.getStudyEnvironment()));
        vo.setComputerUsageTimeText(LifestyleTextConverter.getComputerUsageTimeText(student.getComputerUsageTime()));
        vo.setGamingPreferenceText(LifestyleTextConverter.getGamingPreferenceText(student.getGamingPreference()));
        vo.setMusicPreferenceText(LifestyleTextConverter.getMusicPreferenceText(student.getMusicPreference()));
        vo.setMusicVolumeText(LifestyleTextConverter.getMusicVolumeText(student.getMusicVolume()));
        vo.setEatInRoomText(LifestyleTextConverter.getEatInRoomText(student.getEatInRoom()));

        return vo;
    }

    private StudentBatchLoadContext batchLoadRelatedData(List<Student> students) {
        StudentBatchLoadContext context = new StudentBatchLoadContext();
        if (students.isEmpty()) {
            return context;
        }

        Set<String> campusCodes = new HashSet<>();
        Set<String> deptCodes = new HashSet<>();
        Set<String> majorCodes = new HashSet<>();
        Set<Long> classIds = new HashSet<>();
        Set<Long> floorIds = new HashSet<>();
        Set<String> floorCodes = new HashSet<>();
        Set<Long> roomIds = new HashSet<>();
        Set<String> roomCodes = new HashSet<>();
        Set<Long> bedIds = new HashSet<>();
        Set<String> bedCodes = new HashSet<>();

        for (Student student : students) {
            if (StrUtil.isNotBlank(student.getCampusCode())) {
                campusCodes.add(student.getCampusCode());
            }
            if (StrUtil.isNotBlank(student.getDeptCode())) {
                deptCodes.add(student.getDeptCode());
            }
            if (StrUtil.isNotBlank(student.getMajorCode())) {
                majorCodes.add(student.getMajorCode());
            }
            if (student.getClassId() != null) {
                classIds.add(student.getClassId());
            }
            if (student.getFloorId() != null) {
                floorIds.add(student.getFloorId());
            }
            if (StrUtil.isNotBlank(student.getFloorCode())) {
                floorCodes.add(student.getFloorCode());
            }
            if (student.getRoomId() != null) {
                roomIds.add(student.getRoomId());
            }
            if (StrUtil.isNotBlank(student.getRoomCode())) {
                roomCodes.add(student.getRoomCode());
            }
            if (student.getBedId() != null) {
                bedIds.add(student.getBedId());
            }
            if (StrUtil.isNotBlank(student.getBedCode())) {
                bedCodes.add(student.getBedCode());
            }
        }

        if (!campusCodes.isEmpty()) {
            Set<String> uncachedCodes = new HashSet<>();
            for (String code : campusCodes) {
                Campus cached = cacheManager.getCampus(code);
                if (cached != null) {
                    context.getCampusByCode().put(code, cached);
                } else {
                    uncachedCodes.add(code);
                }
            }
            if (!uncachedCodes.isEmpty()) {
                LambdaQueryWrapper<Campus> wrapper = new LambdaQueryWrapper<>();
                wrapper.in(Campus::getCampusCode, uncachedCodes);
                List<Campus> campuses = campusMapper.selectList(wrapper);
                campuses.forEach(c -> {
                    context.getCampusByCode().put(c.getCampusCode(), c);
                    cacheManager.putCampus(c.getCampusCode(), c);
                });
            }
        }

        if (!deptCodes.isEmpty()) {
            Set<String> uncachedCodes = new HashSet<>();
            for (String code : deptCodes) {
                Department cached = cacheManager.getDept(code);
                if (cached != null) {
                    context.getDeptByCode().put(code, cached);
                } else {
                    uncachedCodes.add(code);
                }
            }
            if (!uncachedCodes.isEmpty()) {
                LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
                wrapper.in(Department::getDeptCode, uncachedCodes);
                List<Department> depts = departmentMapper.selectList(wrapper);
                depts.forEach(d -> {
                    context.getDeptByCode().put(d.getDeptCode(), d);
                    cacheManager.putDept(d.getDeptCode(), d);
                });
            }
        }

        if (!majorCodes.isEmpty()) {
            Set<String> uncachedCodes = new HashSet<>();
            for (String code : majorCodes) {
                Major cached = cacheManager.getMajor(code);
                if (cached != null) {
                    context.getMajorByCode().put(code, cached);
                } else {
                    uncachedCodes.add(code);
                }
            }
            if (!uncachedCodes.isEmpty()) {
                LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
                wrapper.in(Major::getMajorCode, uncachedCodes);
                List<Major> majors = majorMapper.selectList(wrapper);
                majors.forEach(m -> {
                    context.getMajorByCode().put(m.getMajorCode(), m);
                    cacheManager.putMajor(m.getMajorCode(), m);
                });
            }
        }

        if (!classIds.isEmpty()) {
            Set<Long> uncachedIds = new HashSet<>();
            for (Long id : classIds) {
                Class cached = cacheManager.getClass(id);
                if (cached != null) {
                    context.getClassById().put(id, cached);
                } else {
                    uncachedIds.add(id);
                }
            }
            if (!uncachedIds.isEmpty()) {
                List<Class> classes = classMapper.selectBatchIds(uncachedIds);
                classes.forEach(c -> {
                    context.getClassById().put(c.getId(), c);
                    cacheManager.putClass(c.getId(), c);
                });
            }
        }

        if (!floorIds.isEmpty()) {
            Set<Long> uncachedIds = new HashSet<>();
            for (Long id : floorIds) {
                Floor cached = cacheManager.getFloor(id);
                if (cached != null) {
                    context.getFloorById().put(id, cached);
                } else {
                    uncachedIds.add(id);
                }
            }
            if (!uncachedIds.isEmpty()) {
                List<Floor> floors = floorMapper.selectBatchIds(uncachedIds);
                floors.forEach(f -> {
                    context.getFloorById().put(f.getId(), f);
                    cacheManager.putFloor(f.getId(), f);
                });
            }
        }

        if (!floorCodes.isEmpty()) {
            LambdaQueryWrapper<Floor> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(Floor::getFloorCode, floorCodes);
            List<Floor> floors = floorMapper.selectList(wrapper);
            floors.forEach(f -> context.getFloorByCode().put(f.getFloorCode(), f));
        }

        if (!roomIds.isEmpty()) {
            Set<Long> uncachedIds = new HashSet<>();
            for (Long id : roomIds) {
                Room cached = cacheManager.getRoom(id);
                if (cached != null) {
                    context.getRoomById().put(id, cached);
                } else {
                    uncachedIds.add(id);
                }
            }
            if (!uncachedIds.isEmpty()) {
                List<Room> rooms = roomMapper.selectBatchIds(uncachedIds);
                rooms.forEach(r -> {
                    context.getRoomById().put(r.getId(), r);
                    cacheManager.putRoom(r.getId(), r);
                });
            }
        }

        if (!roomCodes.isEmpty()) {
            LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(Room::getRoomCode, roomCodes);
            List<Room> rooms = roomMapper.selectList(wrapper);
            rooms.forEach(r -> context.getRoomByCode().put(r.getRoomCode(), r));
        }

        if (!bedIds.isEmpty()) {
            Set<Long> uncachedIds = new HashSet<>();
            for (Long id : bedIds) {
                Bed cached = cacheManager.getBed(id);
                if (cached != null) {
                    context.getBedById().put(id, cached);
                } else {
                    uncachedIds.add(id);
                }
            }
            if (!uncachedIds.isEmpty()) {
                List<Bed> beds = bedMapper.selectBatchIds(uncachedIds);
                beds.forEach(b -> {
                    context.getBedById().put(b.getId(), b);
                    cacheManager.putBed(b.getId(), b);
                });
            }
        }

        if (!bedCodes.isEmpty()) {
            LambdaQueryWrapper<Bed> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(Bed::getBedCode, bedCodes);
            List<Bed> beds = bedMapper.selectList(wrapper);
            beds.forEach(b -> context.getBedByCode().put(b.getBedCode(), b));
        }

        return context;
    }
}
