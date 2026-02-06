package com.project.app.controller;

import com.project.backend.allocation.service.BedRecommendService;
import com.project.backend.allocation.vo.BedRecommendVO;
import com.project.core.context.UserContext;
import com.project.core.result.R;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 小程序端-床位推荐Controller
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Slf4j
@RestController
@RequestMapping("/v1/app/recommend")
@RequiredArgsConstructor
@Tag(name = "小程序-床位推荐", description = "智能推荐床位")
public class AppBedRecommendController {

    private final BedRecommendService recommendService;

    /**
     * 推荐床位（未分配学生）
     */
    @GetMapping("/beds")
    @Operation(summary = "推荐床位（未分配学生）")
    public R<List<BedRecommendVO>> recommendBeds(
            @RequestParam(defaultValue = "5") Integer limit) {
        Long studentId = UserContext.getUserId();
        List<BedRecommendVO> voList = recommendService.recommendBeds(studentId, limit);
        return R.ok(voList);
    }

    /**
     * 调宿推荐
     */
    @GetMapping("/transfer")
    @Operation(summary = "调宿推荐")
    public R<List<BedRecommendVO>> recommendTransfer(
            @RequestParam(defaultValue = "5") Integer limit) {
        Long studentId = UserContext.getUserId();
        List<BedRecommendVO> voList = recommendService.recommendTransfer(studentId, limit);
        return R.ok(voList);
    }
}
