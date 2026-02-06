package com.project.backend.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.backend.student.entity.Student;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生Mapper
 * 
 * @author 陈鸿昇
 * @since 2026-01-06
 */
@Mapper
public interface StudentMapper extends BaseMapper<Student> {
}
