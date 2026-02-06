package com.project.backend.common.service;

import com.project.backend.student.entity.Student;

/**
 * 学生信息填充器接口
 * 用于将学生详细信息填充到 VO 对象中
 *
 * @author 陈鸿昇
 * @since 2026-01-26
 */
public interface StudentInfoEnricher {

    /**
     * 填充学生基本信息到 VO
     *
     * @param student 学生实体
     * @param vo      目标 VO 对象（支持 CheckInVO、CheckOutVO、StayVO、TransferVO、BedVO、RepairVO 等）
     */
    void enrichStudentInfo(Student student, Object vo);

    /**
     * 填充学生基本信息到 VO，并填充校区名称（如果为空）
     *
     * @param student             学生实体
     * @param vo                  目标 VO 对象
     * @param campusNameFieldName 校区名称字段名（如 "campusName" 或 "originalCampusName"）
     */
    void enrichStudentInfo(Student student, Object vo, String campusNameFieldName);
}
