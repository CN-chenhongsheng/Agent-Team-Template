package com.project.app.service;

import com.project.backend.student.dto.student.StudentLifestyleDTO;
import com.project.backend.accommodation.vo.student.DormInfoVO;
import com.project.backend.accommodation.vo.student.RoommateVO;

import java.util.List;

/**
 * 学生端Service
 *
 * @author 陈鸿昇
 * @since 2026-01-16
 */
public interface StudentService {

    /**
     * 获取当前学生的宿舍信息
     *
     * @return 宿舍信息
     */
    DormInfoVO getCurrentStudentDormInfo();

    /**
     * 获取当前学生的室友列表
     *
     * @return 室友列表
     */
    List<RoommateVO> getCurrentStudentRoommates();

    /**
     * 获取当前学生的生活习惯信息
     *
     * @return 生活习惯信息
     */
    StudentLifestyleDTO getCurrentStudentHabits();

    /**
     * 更新当前学生的生活习惯信息
     *
     * @param dto 生活习惯DTO
     * @return 是否成功
     */
    boolean updateCurrentStudentHabits(StudentLifestyleDTO dto);
}
