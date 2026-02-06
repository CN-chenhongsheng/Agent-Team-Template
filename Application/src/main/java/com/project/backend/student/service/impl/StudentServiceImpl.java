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
import java.util.List;
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

        List<StudentVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.build(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public StudentVO getDetailById(Long id) {
        Student student = getById(id);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }
        return convertToVO(student);
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

    /**
     * 实体转VO
     */
    private StudentVO convertToVO(Student student) {
        StudentVO vo = new StudentVO();
        BeanUtil.copyProperties(student, vo);
        vo.setStatusText(DictUtils.getLabel("sys_user_status", student.getStatus(), "未知"));
        vo.setGenderText(DictUtils.getLabel("sys_user_sex", student.getGender(), "未知"));
        vo.setAcademicStatusText(DictUtils.getLabel("academic_status", student.getAcademicStatus(), "未知"));

        // 查询校区名称
        if (StrUtil.isNotBlank(student.getCampusCode())) {
            LambdaQueryWrapper<Campus> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Campus::getCampusCode, student.getCampusCode());
            Campus campus = campusMapper.selectOne(wrapper);
            if (campus != null) {
                vo.setCampusName(campus.getCampusName());
            }
        }

        // 查询院系名称
        if (StrUtil.isNotBlank(student.getDeptCode())) {
            LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Department::getDeptCode, student.getDeptCode());
            Department department = departmentMapper.selectOne(wrapper);
            if (department != null) {
                vo.setDeptName(department.getDeptName());
            }
        }

        // 查询专业名称
        if (StrUtil.isNotBlank(student.getMajorCode())) {
            LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Major::getMajorCode, student.getMajorCode());
            Major major = majorMapper.selectOne(wrapper);
            if (major != null) {
                vo.setMajorName(major.getMajorName());
            }
        }

        // 查询班级名称
        if (student.getClassId() != null) {
            Class classEntity = classMapper.selectById(student.getClassId());
            if (classEntity != null) {
                vo.setClassName(classEntity.getClassName());
            }
        }

        // 查询楼层名称
        if (student.getFloorId() != null) {
            Floor floor = floorMapper.selectById(student.getFloorId());
            if (floor != null) {
                vo.setFloorName(floor.getFloorName());
            }
        } else if (StrUtil.isNotBlank(student.getFloorCode())) {
            // 如果没有floorId，尝试用floorCode查询
            LambdaQueryWrapper<Floor> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Floor::getFloorCode, student.getFloorCode());
            Floor floor = floorMapper.selectOne(wrapper);
            if (floor != null) {
                vo.setFloorName(floor.getFloorName());
            }
        }

        // 查询房间名称
        if (student.getRoomId() != null) {
            Room room = roomMapper.selectById(student.getRoomId());
            if (room != null) {
                vo.setRoomName(room.getRoomNumber());
            }
        } else if (StrUtil.isNotBlank(student.getRoomCode())) {
            // 如果没有roomId，尝试用roomCode查询
            LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Room::getRoomCode, student.getRoomCode());
            Room room = roomMapper.selectOne(wrapper);
            if (room != null) {
                vo.setRoomName(room.getRoomNumber());
            }
        }

        // 查询床位名称
        if (student.getBedId() != null) {
            Bed bed = bedMapper.selectById(student.getBedId());
            if (bed != null) {
                vo.setBedName(bed.getBedNumber());
            }
        } else if (StrUtil.isNotBlank(student.getBedCode())) {
            // 如果没有bedId，尝试用bedCode查询
            LambdaQueryWrapper<Bed> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Bed::getBedCode, student.getBedCode());
            Bed bed = bedMapper.selectOne(wrapper);
            if (bed != null) {
                vo.setBedName(bed.getBedNumber());
            }
        }

        // 转换生活习惯字段为文本
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
}
