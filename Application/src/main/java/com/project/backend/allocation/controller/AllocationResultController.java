package com.project.backend.allocation.controller;

import com.project.backend.allocation.dto.result.AllocationResultAdjustDTO;
import com.project.backend.allocation.dto.result.AllocationResultQueryDTO;
import com.project.backend.allocation.service.AllocationResultService;
import com.project.backend.allocation.vo.AllocationResultVO;
import com.project.core.annotation.Log;
import com.project.core.result.PageResult;
import com.project.core.result.R;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分配结果管理Controller
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Slf4j
@RestController
@RequestMapping("/v1/system/allocation/result")
@RequiredArgsConstructor
@Tag(name = "分配结果管理", description = "分配结果的查询、确认、调整")
public class AllocationResultController {

    private final AllocationResultService resultService;

    /**
     * 分页查询分配结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询分配结果")
    public R<PageResult<AllocationResultVO>> pageList(AllocationResultQueryDTO queryDTO) {
        PageResult<AllocationResultVO> result = resultService.pageList(queryDTO);
        return R.ok(result);
    }

    /**
     * 获取结果详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取结果详情")
    public R<AllocationResultVO> getDetailById(@PathVariable Long id) {
        AllocationResultVO detail = resultService.getDetailById(id);
        return R.ok(detail);
    }

    /**
     * 确认分配结果
     */
    @PutMapping("/confirm")
    @Operation(summary = "确认分配结果")
    @Log(title = "确认分配结果", businessType = 2)
    public R<Void> confirmResults(@RequestParam Long taskId, @RequestBody List<Long> resultIds) {
        boolean success = resultService.confirmResults(taskId, resultIds);
        return success ? R.ok("确认成功", null) : R.fail("确认失败");
    }

    /**
     * 确认全部分配结果
     */
    @PutMapping("/confirm-all")
    @Operation(summary = "确认全部分配结果")
    @Log(title = "确认全部分配结果", businessType = 2)
    public R<Void> confirmAll(@RequestParam Long taskId) {
        boolean success = resultService.confirmAll(taskId);
        return success ? R.ok("全部确认成功", null) : R.fail("确认失败");
    }

    /**
     * 拒绝分配结果
     */
    @PutMapping("/reject")
    @Operation(summary = "拒绝分配结果")
    @Log(title = "拒绝分配结果", businessType = 2)
    public R<Void> rejectResults(
            @RequestParam Long taskId,
            @RequestBody List<Long> resultIds,
            @RequestParam(required = false) String reason) {
        boolean success = resultService.rejectResults(taskId, resultIds, reason);
        return success ? R.ok("已拒绝", null) : R.fail("操作失败");
    }

    /**
     * 调整分配
     */
    @PutMapping("/adjust")
    @Operation(summary = "调整分配")
    @Log(title = "调整分配结果", businessType = 2)
    public R<Void> adjustResult(@Valid @RequestBody AllocationResultAdjustDTO adjustDTO) {
        boolean success = resultService.adjustResult(
                adjustDTO.getResultId(),
                adjustDTO.getNewBedId(),
                adjustDTO.getReason()
        );
        return success ? R.ok("调整成功", null) : R.fail("调整失败");
    }

    /**
     * 获取问题清单（低匹配度）
     */
    @GetMapping("/problem-list")
    @Operation(summary = "获取问题清单（低匹配度）")
    public R<List<AllocationResultVO>> getProblemList(
            @RequestParam Long taskId,
            @RequestParam(defaultValue = "60") Integer threshold) {
        List<AllocationResultVO> problemList = resultService.getProblemList(taskId, threshold);
        return R.ok(problemList);
    }
}
