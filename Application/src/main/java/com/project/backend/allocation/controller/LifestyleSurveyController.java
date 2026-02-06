package com.project.backend.allocation.controller;

import com.project.backend.allocation.dto.survey.LifestyleSurveyQueryDTO;
import com.project.backend.allocation.service.LifestyleSurveyService;
import com.project.backend.allocation.vo.survey.LifestyleSurveyDetailVO;
import com.project.backend.allocation.vo.survey.LifestyleSurveyListVO;
import com.project.backend.allocation.vo.survey.LifestyleSurveyStatisticsVO;
import com.project.core.result.PageResult;
import com.project.core.result.R;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 问卷管理Controller（管理端）
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Slf4j
@RestController
@RequestMapping("/v1/system/allocation/survey")
@RequiredArgsConstructor
@Tag(name = "问卷管理", description = "管理端-学生生活习惯问卷管理")
public class LifestyleSurveyController {

    private final LifestyleSurveyService surveyService;

    /**
     * 分页查询问卷列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询问卷列表")
    public R<PageResult<LifestyleSurveyListVO>> pageList(LifestyleSurveyQueryDTO queryDTO) {
        PageResult<LifestyleSurveyListVO> result = surveyService.pageList(queryDTO);
        return R.ok(result);
    }

    /**
     * 获取问卷统计数据
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取问卷统计数据")
    public R<LifestyleSurveyStatisticsVO> getStatistics(
            @RequestParam(required = false) Integer enrollmentYear,
            @RequestParam(required = false) String classCode) {
        LifestyleSurveyStatisticsVO statistics = surveyService.getStatistics(enrollmentYear, classCode);
        return R.ok(statistics);
    }

    /**
     * 获取学生问卷详情
     */
    @GetMapping("/{studentId}")
    @Operation(summary = "获取学生问卷详情")
    public R<LifestyleSurveyDetailVO> getDetailByStudentId(@PathVariable Long studentId) {
        LifestyleSurveyDetailVO detail = surveyService.getDetailByStudentId(studentId);
        return R.ok(detail);
    }
}
