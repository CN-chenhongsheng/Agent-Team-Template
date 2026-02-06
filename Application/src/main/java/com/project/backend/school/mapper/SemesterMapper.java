package com.project.backend.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.backend.school.entity.Semester;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学期Mapper
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Mapper
public interface SemesterMapper extends BaseMapper<Semester> {
}
