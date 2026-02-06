package com.project.backend.allocation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.project.backend.allocation.dto.survey.LifestyleSurveyQueryDTO;
import com.project.backend.allocation.dto.survey.LifestyleSurveySaveDTO;
import com.project.backend.allocation.entity.LifestyleSurvey;
import com.project.backend.allocation.vo.LifestyleSurveyVO;
import com.project.backend.allocation.vo.survey.LifestyleSurveyDetailVO;
import com.project.backend.allocation.vo.survey.LifestyleSurveyListVO;
import com.project.backend.allocation.vo.survey.LifestyleSurveyStatisticsVO;
import com.project.core.result.PageResult;

/**
 * 生活习惯问卷服务接口
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
public interface LifestyleSurveyService extends IService<LifestyleSurvey> {

    /**
     * 分页查询问卷列表（管理端）
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<LifestyleSurveyListVO> pageList(LifestyleSurveyQueryDTO queryDTO);

    /**
     * 获取问卷统计数据
     *
     * @param enrollmentYear 入学年份
     * @param classCode 班级编码
     * @return 统计数据
     */
    LifestyleSurveyStatisticsVO getStatistics(Integer enrollmentYear, String classCode);

    /**
     * 获取学生问卷详情（管理端）
     *
     * @param studentId 学生ID
     * @return 问卷详情
     */
    LifestyleSurveyDetailVO getDetailByStudentId(Long studentId);

    /**
     * 获取学生问卷状态及数据（小程序端）
     *
     * @param studentId 学生ID
     * @return 问卷状态和数据
     */
    LifestyleSurveyVO getSurveyStatus(Long studentId);

    /**
     * 提交问卷
     *
     * @param studentId 学生ID
     * @param dto 问卷数据
     * @return 是否成功
     */
    boolean submitSurvey(Long studentId, LifestyleSurveySaveDTO dto);

    /**
     * 判断是否可以修改问卷
     *
     * @param studentId 学生ID
     * @return 是否可以修改
     */
    boolean canModifySurvey(Long studentId);

    /**
     * 锁定问卷（分配确认后调用）
     *
     * @param studentId 学生ID
     * @return 是否成功
     */
    boolean lockSurvey(Long studentId);
}
