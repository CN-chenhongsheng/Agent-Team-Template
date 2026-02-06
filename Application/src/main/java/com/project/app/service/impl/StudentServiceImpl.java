package com.project.app.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.project.core.context.UserContext;
import com.project.core.exception.BusinessException;
import com.project.backend.student.dto.student.StudentLifestyleDTO;
import com.project.backend.student.entity.Student;
import com.project.backend.student.mapper.StudentMapper;
import com.project.app.service.StudentService;
import com.project.backend.accommodation.vo.student.DormInfoVO;
import com.project.backend.accommodation.vo.student.RoommateVO;
import com.project.backend.organization.entity.Campus;
import com.project.backend.organization.mapper.CampusMapper;
import com.project.backend.room.entity.Bed;
import com.project.backend.room.entity.Floor;
import com.project.backend.room.mapper.BedMapper;
import com.project.backend.room.mapper.FloorMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学生端Service实现
 *
 * @author 陈鸿昇
 * @since 2026-01-16
 */
@Slf4j
@Service("appStudentServiceImpl")
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentMapper studentMapper;
    private final CampusMapper campusMapper;
    private final FloorMapper floorMapper;
    private final BedMapper bedMapper;
    private final com.project.backend.student.service.StudentService studentService;

    @Override
    public DormInfoVO getCurrentStudentDormInfo() {
        // 获取当前登录用户
        String username = UserContext.getUsername();
        if (StrUtil.isBlank(username)) {
            throw new BusinessException("用户未登录");
        }

        // 根据学号查询学生信息
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getStudentNo, username);
        Student student = studentMapper.selectOne(wrapper);

        if (student == null) {
            throw new BusinessException("学生信息不存在");
        }

        DormInfoVO dormInfo = new DormInfoVO();

        // 查询校区名称
        if (StrUtil.isNotBlank(student.getCampusCode())) {
            LambdaQueryWrapper<Campus> campusWrapper = new LambdaQueryWrapper<>();
            campusWrapper.eq(Campus::getCampusCode, student.getCampusCode());
            Campus campus = campusMapper.selectOne(campusWrapper);
            if (campus != null) {
                dormInfo.setCampusName(campus.getCampusName());
            }
        }

        // 查询楼栋/楼层名称
        if (StrUtil.isNotBlank(student.getFloorCode())) {
            LambdaQueryWrapper<Floor> floorWrapper = new LambdaQueryWrapper<>();
            floorWrapper.eq(Floor::getFloorCode, student.getFloorCode());
            Floor floor = floorMapper.selectOne(floorWrapper);
            if (floor != null) {
                dormInfo.setBuildingName(floor.getFloorName());
                dormInfo.setFloorName(floor.getFloorNumber() + "楼");
            }
        }

        // 设置房间号和床位
        dormInfo.setRoomCode(student.getRoomCode());
        dormInfo.setBedCode(student.getBedCode());

        // 查询入住日期
        if (student.getBedId() != null) {
            Bed bed = bedMapper.selectById(student.getBedId());
            if (bed != null) {
                dormInfo.setCheckInDate(bed.getCheckInDate());
            }
        }

        return dormInfo;
    }

    @Override
    public List<RoommateVO> getCurrentStudentRoommates() {
        // 获取当前登录用户
        String username = UserContext.getUsername();
        if (StrUtil.isBlank(username)) {
            throw new BusinessException("用户未登录");
        }

        // 根据学号查询学生信息
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getStudentNo, username);
        Student currentStudent = studentMapper.selectOne(wrapper);

        if (currentStudent == null) {
            throw new BusinessException("学生信息不存在");
        }

        // 如果没有房间信息，返回空列表
        if (currentStudent.getRoomId() == null) {
            return new ArrayList<>();
        }

        // 查询同房间的其他学生
        LambdaQueryWrapper<Student> roommateWrapper = new LambdaQueryWrapper<>();
        roommateWrapper.eq(Student::getRoomId, currentStudent.getRoomId())
                .ne(Student::getId, currentStudent.getId())
                .eq(Student::getStatus, 1);

        List<Student> roommates = studentMapper.selectList(roommateWrapper);

        return roommates.stream().map(student -> {
            RoommateVO vo = new RoommateVO();
            vo.setId(student.getId());
            vo.setStudentNo(student.getStudentNo());
            vo.setStudentName(student.getStudentName());
            vo.setPhone(student.getPhone());
            vo.setBedCode(student.getBedCode());
            // 头像暂时为空，学生表没有头像字段
            vo.setAvatar(null);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public StudentLifestyleDTO getCurrentStudentHabits() {
        // 获取当前登录用户
        String username = UserContext.getUsername();
        if (StrUtil.isBlank(username)) {
            throw new BusinessException("用户未登录");
        }

        // 根据学号查询学生信息
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getStudentNo, username);
        Student student = studentMapper.selectOne(wrapper);

        if (student == null) {
            throw new BusinessException("学生信息不存在");
        }

        // 转换为DTO
        StudentLifestyleDTO dto = new StudentLifestyleDTO();
        BeanUtil.copyProperties(student, dto);
        return dto;
    }

    @Override
    public boolean updateCurrentStudentHabits(StudentLifestyleDTO dto) {
        // 获取当前登录用户
        String username = UserContext.getUsername();
        if (StrUtil.isBlank(username)) {
            throw new BusinessException("用户未登录");
        }

        // 根据学号查询学生信息
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getStudentNo, username);
        Student student = studentMapper.selectOne(wrapper);

        if (student == null) {
            throw new BusinessException("学生信息不存在");
        }

        // 调用更新方法
        return studentService.updateLifestyle(student.getId(), dto);
    }
}
