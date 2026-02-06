package com.project.backend.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.project.backend.student.entity.Student;
import com.project.backend.common.service.StudentInfoEnricher;
import com.project.backend.common.vo.StudentBasicInfoVO;
import com.project.backend.organization.entity.Campus;
import com.project.backend.organization.entity.Class;
import com.project.backend.organization.entity.Department;
import com.project.backend.organization.entity.Major;
import com.project.backend.organization.mapper.CampusMapper;
import com.project.backend.organization.mapper.ClassMapper;
import com.project.backend.organization.mapper.DepartmentMapper;
import com.project.backend.organization.mapper.MajorMapper;
import com.project.backend.room.entity.Bed;
import com.project.backend.room.entity.Floor;
import com.project.backend.room.entity.Room;
import com.project.backend.room.mapper.BedMapper;
import com.project.backend.room.mapper.FloorMapper;
import com.project.backend.room.mapper.RoomMapper;
import com.project.backend.util.DictUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 学生信息填充器实现
 * 构建 StudentBasicInfoVO 并注入到业务 VO 的 studentInfo 字段
 *
 * @author 陈鸿昇
 * @since 2026-01-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentInfoEnricherImpl implements StudentInfoEnricher {

    private final CampusMapper campusMapper;
    private final DepartmentMapper departmentMapper;
    private final MajorMapper majorMapper;
    private final ClassMapper classMapper;
    private final FloorMapper floorMapper;
    private final RoomMapper roomMapper;
    private final BedMapper bedMapper;

    @Override
    public void enrichStudentInfo(Student student, Object vo) {
        enrichStudentInfo(student, vo, "campusName");
    }

    @Override
    public void enrichStudentInfo(Student student, Object vo, String campusNameFieldName) {
        if (student == null || vo == null) {
            return;
        }
        try {
            StudentBasicInfoVO built = buildStudentBasicInfo(student);
            setFieldValue(vo, "studentInfo", built);
        } catch (Exception e) {
            log.error("填充学生信息失败: studentId={}, voClass={}", student.getId(), vo.getClass().getName(), e);
        }
    }

    /**
     * 从 Student 实体构建 StudentBasicInfoVO（含查库填充 campusName、deptName 等）
     */
    private StudentBasicInfoVO buildStudentBasicInfo(Student student) {
        StudentBasicInfoVO info = new StudentBasicInfoVO();

        info.setStudentName(student.getStudentName());
        info.setStudentNo(student.getStudentNo());
        info.setGender(student.getGender());
        info.setGenderText(DictUtils.getLabel("sys_user_sex", student.getGender(), "未知"));
        info.setPhone(student.getPhone());
        info.setIdCard(student.getIdCard());
        info.setEmail(student.getEmail());
        info.setBirthDate(student.getBirthDate());
        info.setSchoolingLength(student.getSchoolingLength());
        info.setNation(student.getNation());
        info.setPoliticalStatus(student.getPoliticalStatus());
        info.setCampusCode(student.getCampusCode());
        info.setFloorCode(student.getFloorCode());
        info.setAcademicStatus(student.getAcademicStatus());
        info.setAcademicStatusText(DictUtils.getLabel("academic_status", student.getAcademicStatus(), "未知"));
        info.setDeptCode(student.getDeptCode());
        info.setMajorCode(student.getMajorCode());
        info.setClassId(student.getClassId());
        info.setClassCode(student.getClassCode());
        info.setFloorId(student.getFloorId());
        info.setRoomId(student.getRoomId());
        info.setBedId(student.getBedId());
        info.setEnrollmentYear(student.getEnrollmentYear());
        info.setCurrentGrade(student.getCurrentGrade());
        info.setHomeAddress(student.getHomeAddress());
        info.setEmergencyContact(student.getEmergencyContact());
        info.setEmergencyPhone(student.getEmergencyPhone());
        info.setParentName(student.getParentName());
        info.setParentPhone(student.getParentPhone());

        if (StrUtil.isNotBlank(student.getCampusCode())) {
            LambdaQueryWrapper<Campus> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Campus::getCampusCode, student.getCampusCode());
            Campus campus = campusMapper.selectOne(wrapper);
            if (campus != null) {
                info.setCampusName(campus.getCampusName());
            }
        }

        if (StrUtil.isNotBlank(student.getDeptCode())) {
            LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Department::getDeptCode, student.getDeptCode());
            Department department = departmentMapper.selectOne(wrapper);
            if (department != null) {
                info.setDeptName(department.getDeptName());
            }
        }

        if (StrUtil.isNotBlank(student.getMajorCode())) {
            LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Major::getMajorCode, student.getMajorCode());
            Major major = majorMapper.selectOne(wrapper);
            if (major != null) {
                info.setMajorName(major.getMajorName());
            }
        }

        if (student.getClassId() != null) {
            Class classEntity = classMapper.selectById(student.getClassId());
            if (classEntity != null) {
                info.setClassName(classEntity.getClassName());
            }
        }

        if (student.getFloorId() != null) {
            Floor floor = floorMapper.selectById(student.getFloorId());
            if (floor != null) {
                info.setFloorName(floor.getFloorName());
            }
        } else if (StrUtil.isNotBlank(student.getFloorCode())) {
            LambdaQueryWrapper<Floor> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Floor::getFloorCode, student.getFloorCode());
            Floor floor = floorMapper.selectOne(wrapper);
            if (floor != null) {
                info.setFloorName(floor.getFloorName());
            }
        }

        if (student.getRoomId() != null) {
            Room room = roomMapper.selectById(student.getRoomId());
            if (room != null) {
                info.setRoomCode(room.getRoomCode());
                info.setRoomName(room.getRoomNumber());
            }
        } else if (StrUtil.isNotBlank(student.getRoomCode())) {
            LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Room::getRoomCode, student.getRoomCode());
            Room room = roomMapper.selectOne(wrapper);
            if (room != null) {
                info.setRoomCode(room.getRoomCode());
                info.setRoomName(room.getRoomNumber());
            }
        }

        if (student.getBedId() != null) {
            Bed bed = bedMapper.selectById(student.getBedId());
            if (bed != null) {
                info.setBedCode(bed.getBedCode());
                info.setBedName(bed.getBedNumber());
            }
        } else if (StrUtil.isNotBlank(student.getBedCode())) {
            LambdaQueryWrapper<Bed> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Bed::getBedCode, student.getBedCode());
            Bed bed = bedMapper.selectOne(wrapper);
            if (bed != null) {
                info.setBedCode(bed.getBedCode());
                info.setBedName(bed.getBedNumber());
            }
        }

        return info;
    }

    private void setFieldValue(Object obj, String fieldName, Object value) {
        if (obj == null || StrUtil.isBlank(fieldName)) {
            return;
        }
        try {
            BeanUtil.setFieldValue(obj, fieldName, value);
        } catch (Exception e) {
            log.debug("设置字段失败: fieldName={}, value={}, error={}", fieldName, value, e.getMessage());
        }
    }
}
