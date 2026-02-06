package com.project.backend.allocation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.backend.allocation.entity.LifestyleSurvey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 生活习惯问卷状态Mapper
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Mapper
public interface LifestyleSurveyMapper extends BaseMapper<LifestyleSurvey> {

    /**
     * 查询已填写问卷的学生ID列表
     *
     * @param studentIds 学生ID列表
     * @return 已填写问卷的学生ID列表
     */
    @Select({
        "<script>",
        "SELECT student_id FROM sys_lifestyle_survey",
        "WHERE survey_status >= 1 AND student_id IN",
        "<foreach collection='studentIds' item='id' open='(' separator=',' close=')'>",
        "#{id}",
        "</foreach>",
        "</script>"
    })
    List<Long> selectFilledStudentIds(@Param("studentIds") List<Long> studentIds);

    /**
     * 统计未填写问卷的学生数量
     *
     * @param studentIds 学生ID列表
     * @return 未填写数量
     */
    @Select({
        "<script>",
        "SELECT COUNT(*) FROM sys_lifestyle_survey",
        "WHERE survey_status = 0 AND student_id IN",
        "<foreach collection='studentIds' item='id' open='(' separator=',' close=')'>",
        "#{id}",
        "</foreach>",
        "</script>"
    })
    int countUnfilledSurveys(@Param("studentIds") List<Long> studentIds);

    /**
     * 根据条件统计已填写问卷数量
     *
     * @param enrollmentYear 入学年份（可选）
     * @param classCode      班级编码（可选）
     * @return 已填写数量
     */
    @Select({
        "<script>",
        "SELECT COUNT(DISTINCT s.student_id) FROM sys_lifestyle_survey s",
        "INNER JOIN sys_student st ON s.student_id = st.id AND st.deleted = 0",
        "WHERE s.survey_status >= 1",
        "<if test='enrollmentYear != null'>",
        "AND st.enrollment_year = #{enrollmentYear}",
        "</if>",
        "<if test='classCode != null and classCode != \"\"'>",
        "AND st.class_code = #{classCode}",
        "</if>",
        "</script>"
    })
    long countFilledByCondition(@Param("enrollmentYear") Integer enrollmentYear, @Param("classCode") String classCode);
}
