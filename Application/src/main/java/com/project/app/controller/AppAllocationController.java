package com.project.app.controller;

import com.project.backend.allocation.service.AllocationResultService;
import com.project.backend.allocation.vo.AllocationResultVO;
import com.project.backend.allocation.vo.AllocationRoommateVO;
import com.project.core.context.UserContext;
import com.project.core.result.R;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 小程序端-分配结果Controller
 *
 * @author 陈鸿昇
 * @since 2026-02-02
 */
@Slf4j
@RestController
@RequestMapping("/v1/app/allocation")
@RequiredArgsConstructor
@Tag(name = "小程序-分配结果", description = "学生查看分配结果")
public class AppAllocationController {

    private final AllocationResultService resultService;

    /**
     * 查看我的分配结果
     */
    @GetMapping("/my-result")
    @Operation(summary = "查看我的分配结果")
    public R<AllocationResultVO> getMyResult() {
        Long studentId = UserContext.getUserId();
        AllocationResultVO result = resultService.getMyResult(studentId);

        if (result == null) {
            return R.ok("暂无分配结果", null);
        }
        return R.ok(result);
    }

    /**
     * 查看室友信息
     */
    @GetMapping("/roommates")
    @Operation(summary = "查看室友信息")
    public R<List<AllocationRoommateVO>> getRoommates() {
        Long studentId = UserContext.getUserId();
        List<AllocationRoommateVO> roommates = resultService.getRoommates(studentId);

        if (roommates.isEmpty()) {
            return R.ok("暂无室友信息", roommates);
        }
        return R.ok(roommates);
    }
}
