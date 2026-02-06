package com.project.app.controller;

import com.project.backend.allocation.dto.survey.LifestyleSurveySaveDTO;
import com.project.backend.allocation.service.LifestyleSurveyService;
import com.project.backend.allocation.vo.LifestyleSurveyVO;
import com.project.core.context.UserContext;
import com.project.core.result.R;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 小程序端-生活习惯问卷Controller
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Slf4j
@RestController
@RequestMapping("/v1/app/lifestyle")
@RequiredArgsConstructor
@Tag(name = "小程序-生活习惯问卷", description = "学生填写生活习惯问卷")
public class AppLifestyleController {

    private final LifestyleSurveyService surveyService;

    /**
     * 获取问卷状态
     */
    @GetMapping("/status")
    @Operation(summary = "获取问卷状态")
    public R<LifestyleSurveyVO> getSurveyStatus() {
        Long studentId = UserContext.getUserId();
        LifestyleSurveyVO survey = surveyService.getSurveyStatus(studentId);
        return R.ok(survey);
    }

    /**
     * 提交问卷
     */
    @PostMapping("/submit")
    @Operation(summary = "提交问卷")
    public R<Void> submitSurvey(@Valid @RequestBody LifestyleSurveySaveDTO saveDTO) {
        Long studentId = UserContext.getUserId();
        boolean success = surveyService.submitSurvey(studentId, saveDTO);
        return success ? R.ok("问卷提交成功", null) : R.fail("问卷提交失败");
    }

    /**
     * 获取已填写的问卷数据
     */
    @GetMapping("/data")
    @Operation(summary = "获取已填写的问卷数据")
    public R<LifestyleSurveyVO> getSurveyData() {
        Long studentId = UserContext.getUserId();
        LifestyleSurveyVO survey = surveyService.getSurveyStatus(studentId);
        return R.ok(survey);
    }

    /**
     * 检查是否可以修改问卷
     */
    @GetMapping("/can-modify")
    @Operation(summary = "检查是否可以修改问卷")
    public R<Boolean> canModify() {
        Long studentId = UserContext.getUserId();
        boolean canModify = surveyService.canModifySurvey(studentId);
        return R.ok(canModify);
    }
}
