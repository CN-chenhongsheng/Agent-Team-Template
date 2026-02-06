package com.project.backend.system.controller;

import com.project.core.result.PageResult;
import com.project.core.result.R;
import com.project.backend.controller.base.BatchDeleteController;
import com.project.backend.system.dto.OperLogQueryDTO;
import com.project.backend.system.service.OperLogService;
import com.project.backend.system.vo.OperLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志控制器
 * 
 * @author 陈鸿昇
 * @since 2026-01-01
 */
@Slf4j
@RestController
@RequestMapping("/v1/system/oper-log")
@RequiredArgsConstructor
@Tag(name = "操作日志管理", description = "操作日志的查询、删除等")
public class OperLogController implements BatchDeleteController {

    private final OperLogService operLogService;

    @GetMapping("/page")
    @Operation(summary = "分页查询操作日志列表")
    public R<PageResult<OperLogVO>> page(OperLogQueryDTO queryDTO) {
        log.info("分页查询操作日志，参数：{}", queryDTO);
        PageResult<OperLogVO> result = operLogService.pageList(queryDTO);
        if (result != null) {
            return R.ok(result);
        } else {
            return R.fail("操作日志列表为空");
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询操作日志详情")
    @Parameter(name = "id", description = "日志ID", required = true)
    public R<OperLogVO> getDetail(@PathVariable Long id) {
        log.info("查询操作日志详情，ID：{}", id);
        OperLogVO operLogVO = operLogService.getDetailById(id);
        if (operLogVO != null) {
            return R.ok(operLogVO);
        } else {
            return R.fail("操作日志不存在");
        }
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除操作日志")
    public R<Void> batchDelete(@RequestBody Long[] ids) {
        log.info("批量删除操作日志，ID：{}", (Object) ids);
        boolean success = operLogService.batchDelete(ids);
        if (success) {
            return R.ok("操作日志批量删除成功", null);
        } else {
            return R.fail("操作日志批量删除失败");
        }
    }

    @DeleteMapping("/clean")
    @Operation(summary = "清空操作日志")
    public R<Void> clean() {
        log.info("清空操作日志");
        boolean success = operLogService.clean();
        if (success) {
            return R.ok("操作日志清空成功", null);
        } else {
            return R.fail("操作日志清空失败");
        }
    }

    @Override
    public String getEntityName() {
        return "操作日志";
    }

    @Override
    public boolean callBatchDelete(Long[] ids) {
        return operLogService.batchDelete(ids);
    }
}
