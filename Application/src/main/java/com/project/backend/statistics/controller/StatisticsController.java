package com.project.backend.statistics.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.project.backend.statistics.dto.MyApplyQueryDTO;
import com.project.backend.statistics.service.StudentStatisticsService;
import com.project.backend.statistics.vo.ApplyDetailVO;
import com.project.backend.statistics.vo.MyApplyVO;
import com.project.backend.statistics.vo.StudentHomeStatisticsVO;
import com.project.core.result.PageResult;
import com.project.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 统计Controller
 *
 * @author 陈鸿昇
 * @since 2026-01-29
 */
@Slf4j
@RestController
@RequestMapping("/v1/statistics")
@RequiredArgsConstructor
@Tag(name = "统计管理", description = "提供学生首页统计和申请记录查询功能")
public class StatisticsController {

    private final StudentStatisticsService statisticsService;

    /**
     * 获取学生首页统计信息
     *
     * @return 学生首页统计信息
     */
    @GetMapping("/student-home")
    @Operation(summary = "获取学生首页统计信息", description = "获取当前登录学生首页展示的统计信息（基本信息+宿舍信息）")
    public R<StudentHomeStatisticsVO> getStudentHomeStatistics() {
        // 从 Token 中获取当前登录学生的 ID
        Long studentId = StpUtil.getLoginIdAsLong();
        log.info("获取学生端首页统计数据，学生ID：{}", studentId);

        StudentHomeStatisticsVO vo = statisticsService.getStudentHomeStatistics(studentId);
        return R.ok(vo);
    }

    /**
     * 获取我的申请列表
     *
     * @param queryDTO 查询条件
     * @return 申请列表（分页或限制条数）
     */
    @GetMapping("/my-applies")
    @Operation(summary = "获取我的申请列表", description = "获取当前登录学生的所有申请记录（入住/调宿/退宿/留宿），支持分页或限制条数")
    public R<PageResult<MyApplyVO>> getMyApplies(
            @Parameter(description = "查询条件")
            @ModelAttribute MyApplyQueryDTO queryDTO) {
        // 从 Token 中获取当前登录学生的 ID
        Long studentId = StpUtil.getLoginIdAsLong();
        log.info("获取我的申请列表，学生ID：{}，查询条件：{}", studentId, queryDTO);

        // 设置学生ID到查询条件
        queryDTO.setStudentId(studentId);

        PageResult<MyApplyVO> pageResult = statisticsService.getMyApplies(queryDTO);
        return R.ok(pageResult);
    }

    /**
     * 获取申请详情
     *
     * @param applyId 申请ID
     * @param applyType 申请类型（check_in/transfer/check_out/stay）
     * @return 申请详情
     */
    @GetMapping("/apply-detail")
    @Operation(summary = "获取申请详情", description = "获取指定申请的详细信息，包括审批流程步骤")
    public R<ApplyDetailVO> getApplyDetail(
            @Parameter(description = "申请ID", required = true)
            @RequestParam Long applyId,
            @Parameter(description = "申请类型：check_in-入住 transfer-调宿 check_out-退宿 stay-留宿", required = true)
            @RequestParam String applyType) {
        // 从 Token 中获取当前登录学生的 ID
        Long studentId = StpUtil.getLoginIdAsLong();
        log.info("获取申请详情，学生ID：{}，申请ID：{}，申请类型：{}", studentId, applyId, applyType);

        ApplyDetailVO vo = statisticsService.getApplyDetail(studentId, applyId, applyType);
        return R.ok(vo);
    }
}
